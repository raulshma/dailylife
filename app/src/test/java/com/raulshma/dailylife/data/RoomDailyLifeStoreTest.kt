package com.raulshma.dailylife.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class RoomDailyLifeStoreTest {

    private fun createInMemoryDatabase(): DailyLifeDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DailyLifeDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @Test
    fun loadReturnsNullWhenDatabaseIsEmpty() {
        val database = createInMemoryDatabase()
        val store = RoomDailyLifeStore(database)

        val result = store.load()

        assertNull(result)
    }

    @Test
    fun saveAndLoadRoundTripPreservesItemsAndSettings() {
        val database = createInMemoryDatabase()
        val store = RoomDailyLifeStore(database)

        val item = LifeItem(
            id = 1L,
            type = LifeItemType.Task,
            title = "Stretch",
            body = "Ten minutes after coffee.",
            createdAt = LocalDateTime.of(2026, 4, 25, 8, 0),
            tags = setOf("health", "morning"),
            isFavorite = true,
            isPinned = true,
            taskStatus = TaskStatus.Done,
            reminderAt = LocalDateTime.of(2026, 4, 26, 9, 30),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily, interval = 1),
            notificationSettings = ItemNotificationSettings(
                enabled = false,
                timeOverride = LocalTime.of(7, 45),
                flexibleWindowMinutes = 15,
                snoozeMinutes = 30,
            ),
            completionHistory = listOf(
                CompletionRecord(
                    itemId = 1L,
                    occurrenceDate = LocalDate.of(2026, 4, 25),
                    completedAt = LocalDateTime.of(2026, 4, 25, 8, 0),
                ),
            ),
        )
        val settings = NotificationSettings(
            globalEnabled = false,
            preferredTime = LocalTime.of(20, 30),
            flexibleWindowMinutes = 20,
            defaultSnoozeMinutes = 45,
            batchNotifications = true,
            respectDoNotDisturb = false,
        )
        val snapshot = PersistedDailyLifeState(
            items = listOf(item),
            notificationSettings = settings,
            nextId = 2L,
        )

        store.save(snapshot)
        val loaded = store.load()

        assertNotNull(loaded)
        assertEquals(1, loaded!!.items.size)
        val loadedItem = loaded.items.single()
        assertEquals(1L, loadedItem.id)
        assertEquals(LifeItemType.Task, loadedItem.type)
        assertEquals("Stretch", loadedItem.title)
        assertEquals("Ten minutes after coffee.", loadedItem.body)
        assertEquals(LocalDateTime.of(2026, 4, 25, 8, 0), loadedItem.createdAt)
        assertEquals(setOf("health", "morning"), loadedItem.tags)
        assertTrue(loadedItem.isFavorite)
        assertTrue(loadedItem.isPinned)
        assertEquals(TaskStatus.Done, loadedItem.taskStatus)
        assertEquals(LocalDateTime.of(2026, 4, 26, 9, 30), loadedItem.reminderAt)
        assertEquals(RecurrenceFrequency.Daily, loadedItem.recurrenceRule.frequency)
        assertEquals(1, loadedItem.recurrenceRule.interval)
        assertFalse(loadedItem.notificationSettings.enabled)
        assertEquals(LocalTime.of(7, 45), loadedItem.notificationSettings.timeOverride)
        assertEquals(15, loadedItem.notificationSettings.flexibleWindowMinutes)
        assertEquals(30, loadedItem.notificationSettings.snoozeMinutes)
        assertEquals(1, loadedItem.completionHistory.size)
        assertEquals(LocalDate.of(2026, 4, 25), loadedItem.completionHistory.single().occurrenceDate)
        assertFalse(loadedItem.completionHistory.single().missed)

        assertFalse(loaded.notificationSettings.globalEnabled)
        assertEquals(LocalTime.of(20, 30), loaded.notificationSettings.preferredTime)
        assertEquals(20, loaded.notificationSettings.flexibleWindowMinutes)
        assertEquals(45, loaded.notificationSettings.defaultSnoozeMinutes)
        assertTrue(loaded.notificationSettings.batchNotifications)
        assertFalse(loaded.notificationSettings.respectDoNotDisturb)
    }

    @Test
    fun overwriteReplacesPreviousData() {
        val database = createInMemoryDatabase()
        val store = RoomDailyLifeStore(database)

        store.save(
            PersistedDailyLifeState(
                items = listOf(
                    LifeItem(
                        id = 1L,
                        type = LifeItemType.Note,
                        title = "First",
                        body = "",
                        createdAt = LocalDateTime.now(),
                    ),
                ),
                notificationSettings = NotificationSettings(),
                nextId = 2L,
            ),
        )

        store.save(
            PersistedDailyLifeState(
                items = listOf(
                    LifeItem(
                        id = 2L,
                        type = LifeItemType.Note,
                        title = "Second",
                        body = "",
                        createdAt = LocalDateTime.now(),
                    ),
                ),
                notificationSettings = NotificationSettings(globalEnabled = false),
                nextId = 3L,
            ),
        )

        val loaded = store.load()

        assertNotNull(loaded)
        assertEquals(1, loaded!!.items.size)
        assertEquals("Second", loaded.items.single().title)
        assertFalse(loaded.notificationSettings.globalEnabled)
    }

    @Test
    fun emptyTagsRoundTrip() {
        val database = createInMemoryDatabase()
        val store = RoomDailyLifeStore(database)

        val item = LifeItem(
            id = 1L,
            type = LifeItemType.Thought,
            title = "Untagged",
            body = "",
            createdAt = LocalDateTime.now(),
            tags = emptySet(),
        )

        store.save(
            PersistedDailyLifeState(
                items = listOf(item),
                notificationSettings = NotificationSettings(),
                nextId = 2L,
            ),
        )

        val loaded = store.load()
        assertNotNull(loaded)
        assertEquals(emptySet<String>(), loaded!!.items.single().tags)
    }

    @Test
    fun nullableFieldsRoundTrip() {
        val database = createInMemoryDatabase()
        val store = RoomDailyLifeStore(database)

        val item = LifeItem(
            id = 1L,
            type = LifeItemType.Note,
            title = "Minimal",
            body = "",
            createdAt = LocalDateTime.now(),
            taskStatus = null,
            reminderAt = null,
            notificationSettings = ItemNotificationSettings(),
            completionHistory = emptyList(),
        )

        store.save(
            PersistedDailyLifeState(
                items = listOf(item),
                notificationSettings = NotificationSettings(),
                nextId = 2L,
            ),
        )

        val loaded = store.load()
        assertNotNull(loaded)
        val loadedItem = loaded!!.items.single()
        assertNull(loadedItem.taskStatus)
        assertNull(loadedItem.reminderAt)
        assertTrue(loadedItem.completionHistory.isEmpty())
    }
}
