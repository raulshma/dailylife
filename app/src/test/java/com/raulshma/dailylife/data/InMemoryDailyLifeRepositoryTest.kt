package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.StorageOperation
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InMemoryDailyLifeRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun addItemNormalizesTagsAndCreatesTaskStatus() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())

        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Pay rent",
                tags = setOf(" Home ", "#Admin", "admin"),
            ),
        )

        assertEquals(TaskStatus.Open, item.taskStatus)
        assertEquals(setOf("home", "admin"), item.tags)
        assertEquals(item, repository.state.value.items.single())
    }

    @Test
    fun filtersBySearchTypeTagAndFavorites() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Note,
                title = "Insurance paperwork",
                tags = setOf("admin"),
                isFavorite = true,
            ),
        )
        repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Photo,
                title = "Basil progress",
                tags = setOf("garden"),
            ),
        )

        repository.updateSearchQuery("insurance")
        repository.selectType(LifeItemType.Note)
        repository.selectTag("admin")
        repository.toggleFavoritesOnly()

        val visibleItems = repository.state.value.visibleItems
        assertEquals(1, visibleItems.size)
        assertEquals("Insurance paperwork", visibleItems.single().title)
    }

    @Test
    fun filtersByInclusiveDateRange() {
        val repository = InMemoryDailyLifeRepository(
            seedItems = listOf(
                LifeItem(
                    id = 1L,
                    type = LifeItemType.Note,
                    title = "Outside range",
                    body = "",
                    createdAt = LocalDateTime.of(2026, 4, 23, 10, 0),
                ),
                LifeItem(
                    id = 2L,
                    type = LifeItemType.Note,
                    title = "Range start",
                    body = "",
                    createdAt = LocalDateTime.of(2026, 4, 24, 10, 0),
                ),
                LifeItem(
                    id = 3L,
                    type = LifeItemType.Note,
                    title = "Range end",
                    body = "",
                    createdAt = LocalDateTime.of(2026, 4, 25, 10, 0),
                ),
            ),
        )

        repository.updateDateRange(
            start = LocalDate.of(2026, 4, 24),
            end = LocalDate.of(2026, 4, 25),
        )

        assertEquals(
            listOf("Range end", "Range start"),
            repository.state.value.visibleItems.map { it.title },
        )
    }

    @Test
    fun markOccurrenceCompletedStoresHistoryAndCompletesTask() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        val item = repository.addItem(
            LifeItemDraft(type = LifeItemType.Task, title = "Stretch"),
        )

        repository.markOccurrenceCompleted(item.id, LocalDate.of(2026, 4, 25))

        val updated = repository.state.value.items.single()
        assertEquals(TaskStatus.Done, updated.taskStatus)
        assertEquals(1, updated.completionHistory.size)
        assertEquals(LocalDate.of(2026, 4, 25), updated.completionHistory.single().occurrenceDate)
    }

    @Test
    fun occurrenceStatsCountPastMissesAndCurrentStreak() {
        val item = LifeItem(
            id = 1L,
            type = LifeItemType.Task,
            title = "Stretch",
            body = "",
            createdAt = LocalDateTime.of(2026, 4, 20, 8, 0),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            completionHistory = listOf(
                completionRecord(itemId = 1L, date = LocalDate.of(2026, 4, 22)),
                completionRecord(itemId = 1L, date = LocalDate.of(2026, 4, 23)),
                completionRecord(itemId = 1L, date = LocalDate.of(2026, 4, 25)),
            ),
        )

        val stats = item.occurrenceStats(referenceDate = LocalDate.of(2026, 4, 25))

        assertEquals(3, stats.completedCount)
        assertEquals(3, stats.missedCount)
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun occurrenceStatsDoesNotBreakStreakForIncompleteToday() {
        val item = LifeItem(
            id = 1L,
            type = LifeItemType.Task,
            title = "Stretch",
            body = "",
            createdAt = LocalDateTime.of(2026, 4, 22, 8, 0),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            completionHistory = listOf(
                completionRecord(itemId = 1L, date = LocalDate.of(2026, 4, 23)),
                completionRecord(itemId = 1L, date = LocalDate.of(2026, 4, 24)),
            ),
        )

        val stats = item.occurrenceStats(referenceDate = LocalDate.of(2026, 4, 25))

        assertEquals(2, stats.completedCount)
        assertEquals(1, stats.missedCount)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun notificationPreferencesCanBeUpdatedGloballyAndPerItem() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        val item = repository.addItem(LifeItemDraft(title = "Water plants"))

        repository.updateNotificationSettings(
            NotificationSettings(
                globalEnabled = false,
                preferredTime = LocalTime.of(20, 30),
                defaultSnoozeMinutes = 60,
            ),
        )
        repository.updateItemNotifications(item.id, ItemNotificationSettings(enabled = false))

        assertFalse(repository.state.value.notificationSettings.globalEnabled)
        assertEquals(LocalTime.of(20, 30), repository.state.value.notificationSettings.preferredTime)
        assertFalse(repository.state.value.items.single().notificationSettings.enabled)
        assertTrue(repository.state.value.items.single().completionHistory.isEmpty())
    }

    @Test
    fun fileBackedRepositoryRestoresItemsSettingsAndNextId() {
        val storeFile = temporaryFolder.root.resolve("daily-life.properties")
        val repository = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = FileDailyLifeStore(storeFile),
            persistDebounceMs = 0,
            persistScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )
        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Stretch",
                body = "Ten minutes after coffee.",
                tags = setOf(" Health ", "#Morning"),
                reminderAt = LocalDateTime.of(2026, 4, 26, 9, 30),
            ),
        )

        repository.toggleFavorite(item.id)
        repository.togglePinned(item.id)
        repository.updateItemNotifications(
            item.id,
            ItemNotificationSettings(
                enabled = false,
                timeOverride = LocalTime.of(7, 45),
                flexibleWindowMinutes = 15,
                snoozeMinutes = 30,
            ),
        )
        repository.markOccurrenceCompleted(
            itemId = item.id,
            occurrenceDate = LocalDate.of(2026, 4, 25),
            completedAt = LocalDateTime.of(2026, 4, 25, 8, 0),
        )
        repository.updateNotificationSettings(
            NotificationSettings(
                globalEnabled = false,
                preferredTime = LocalTime.of(20, 30),
                flexibleWindowMinutes = 20,
                defaultSnoozeMinutes = 45,
                batchNotifications = true,
                respectDoNotDisturb = false,
            ),
        )

        Thread.sleep(100)
        val restored = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = FileDailyLifeStore(storeFile),
        )
        Thread.sleep(100)
        val restoredItem = restored.state.value.items.single()

        assertEquals(item.id, restoredItem.id)
        assertEquals(LifeItemType.Task, restoredItem.type)
        assertEquals("Stretch", restoredItem.title)
        assertEquals("Ten minutes after coffee.", restoredItem.body)
        assertEquals(setOf("health", "morning"), restoredItem.tags)
        assertTrue(restoredItem.isFavorite)
        assertTrue(restoredItem.isPinned)
        assertEquals(TaskStatus.Done, restoredItem.taskStatus)
        assertEquals(LocalDateTime.of(2026, 4, 26, 9, 30), restoredItem.reminderAt)
        assertEquals(LocalTime.of(7, 45), restoredItem.notificationSettings.timeOverride)
        assertFalse(restoredItem.notificationSettings.enabled)
        assertEquals(15, restoredItem.notificationSettings.flexibleWindowMinutes)
        assertEquals(30, restoredItem.notificationSettings.snoozeMinutes)
        assertEquals(1, restoredItem.completionHistory.size)
        assertEquals(
            LocalDate.of(2026, 4, 25),
            restoredItem.completionHistory.single().occurrenceDate,
        )
        assertFalse(restored.state.value.notificationSettings.globalEnabled)
        assertEquals(LocalTime.of(20, 30), restored.state.value.notificationSettings.preferredTime)
        assertTrue(restored.state.value.notificationSettings.batchNotifications)
        assertFalse(restored.state.value.notificationSettings.respectDoNotDisturb)

        val nextItem = restored.addItem(LifeItemDraft(title = "Plan dinner"))

        assertEquals(item.id + 1L, nextItem.id)
    }
    @Test
    fun loadErrorsAreSurfacedAndCanBeDismissed() {
        val repository = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = ThrowingLoadStore,
        )
        Thread.sleep(100)

        val storageError = repository.state.value.storageError

        assertEquals(StorageOperation.Load, storageError?.operation)
        assertTrue(storageError?.message.orEmpty().contains("couldn't load"))
        assertTrue(storageError?.message.orEmpty().contains("disk unavailable"))

        repository.clearStorageError()

        assertNull(repository.state.value.storageError)
    }

    @Test
    fun saveErrorsAreSurfacedWithoutDroppingVisibleChanges() {
        val store = ToggleSaveStore(failSaves = true)
        val repository = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = store,
            persistDebounceMs = 0,
        )

        repository.addItem(LifeItemDraft(title = "Plan dinner"))
        Thread.sleep(50)

        val storageError = repository.state.value.storageError
        assertEquals(StorageOperation.Save, storageError?.operation)
        assertTrue(storageError?.message.orEmpty().contains("couldn't save"))
        assertTrue(storageError?.message.orEmpty().contains("disk full"))
        assertEquals("Plan dinner", repository.state.value.items.single().title)
    }

    @Test
    fun successfulSaveClearsPreviousSaveError() {
        val store = ToggleSaveStore(failSaves = true)
        val repository = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = store,
            persistDebounceMs = 0,
        )
        repository.addItem(LifeItemDraft(title = "Plan dinner"))
        Thread.sleep(50)

        store.failSaves = false
        repository.addItem(LifeItemDraft(title = "Buy basil"))
        Thread.sleep(50)

        assertNull(repository.state.value.storageError)
        assertEquals(2, repository.state.value.items.size)
    }

    @Test
    fun rolloverMissedOccurrencesAddsMissedRecordsForPastDates() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Daily stretch",
                recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            ),
        )
        val referenceDate = item.createdAt.toLocalDate().plusDays(3)

        repository.rolloverMissedOccurrences(referenceDate = referenceDate)

        val updated = repository.state.value.items.single()
        val missedRecords = updated.completionHistory.filter { it.missed }
        assertEquals(3, missedRecords.size)
        assertTrue(missedRecords.all { it.occurrenceDate.isBefore(referenceDate) })
    }

    @Test
    fun rolloverMissedOccurrencesDoesNotDuplicateExistingMissedRecords() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Daily stretch",
                recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            ),
        )
        val referenceDate = item.createdAt.toLocalDate().plusDays(2)

        repository.rolloverMissedOccurrences(referenceDate = referenceDate)
        val firstRollover = repository.state.value.items.single().completionHistory.filter { it.missed }.size

        repository.rolloverMissedOccurrences(referenceDate = referenceDate)
        val secondRollover = repository.state.value.items.single().completionHistory.filter { it.missed }.size

        assertEquals(firstRollover, secondRollover)
    }

    @Test
    fun rolloverMissedOccurrencesSkipsCompletedDates() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Daily stretch",
                recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            ),
        )
        repository.markOccurrenceCompleted(
            itemId = item.id,
            occurrenceDate = item.createdAt.toLocalDate(),
        )

        repository.rolloverMissedOccurrences(referenceDate = item.createdAt.toLocalDate().plusDays(2))

        val updated = repository.state.value.items.single()
        val missedOnCompletedDate = updated.completionHistory.any {
            it.missed && it.occurrenceDate == item.createdAt.toLocalDate()
        }
        assertFalse(missedOnCompletedDate)
        assertEquals(1, updated.completionHistory.filter { it.missed }.size)
    }

    @Test
    fun rolloverMissedOccurrencesDoesNothingForNonRecurringItems() {
        val repository = InMemoryDailyLifeRepository(seedItems = emptyList())
        repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Note,
                title = "One-time note",
            ),
        )

        repository.rolloverMissedOccurrences(referenceDate = LocalDate.now().plusDays(1))

        assertEquals(0, repository.state.value.items.single().completionHistory.size)
    }
}

private object ThrowingLoadStore : DailyLifeStore {
    override suspend fun load(): PersistedDailyLifeState? {
        error("disk unavailable")
    }

    override suspend fun save(snapshot: PersistedDailyLifeState) = Unit
}

private class ToggleSaveStore(
    var failSaves: Boolean,
) : DailyLifeStore {
    override suspend fun load(): PersistedDailyLifeState? = null

    override suspend fun save(snapshot: PersistedDailyLifeState) {
        if (failSaves) error("disk full")
    }
}
private fun completionRecord(
    itemId: Long,
    date: LocalDate,
    missed: Boolean = false,
): CompletionRecord = CompletionRecord(
    itemId = itemId,
    occurrenceDate = date,
    completedAt = date.atTime(8, 0),
    missed = missed,
)
