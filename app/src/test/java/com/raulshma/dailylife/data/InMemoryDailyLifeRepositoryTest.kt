package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        )
        val item = repository.addItem(
            LifeItemDraft(
                type = LifeItemType.Task,
                title = "Stretch",
                body = "Ten minutes after coffee.",
                tags = setOf(" Health ", "#Morning"),
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

        val restored = InMemoryDailyLifeRepository(
            seedItems = emptyList(),
            store = FileDailyLifeStore(storeFile),
        )
        val restoredItem = restored.state.value.items.single()

        assertEquals(item.id, restoredItem.id)
        assertEquals(LifeItemType.Task, restoredItem.type)
        assertEquals("Stretch", restoredItem.title)
        assertEquals("Ten minutes after coffee.", restoredItem.body)
        assertEquals(setOf("health", "morning"), restoredItem.tags)
        assertTrue(restoredItem.isFavorite)
        assertTrue(restoredItem.isPinned)
        assertEquals(TaskStatus.Done, restoredItem.taskStatus)
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
}
