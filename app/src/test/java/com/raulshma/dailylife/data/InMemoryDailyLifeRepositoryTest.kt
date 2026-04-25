package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryDailyLifeRepositoryTest {
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
}
