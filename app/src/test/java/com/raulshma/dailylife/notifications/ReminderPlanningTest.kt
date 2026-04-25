package com.raulshma.dailylife.notifications

import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderPlanningTest {
    @Test
    fun disabledGlobalNotificationsDoNotScheduleReminders() {
        val item = reminderItem(
            reminderAt = LocalDateTime.of(2026, 4, 26, 9, 0),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(globalEnabled = false),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        assertNull(request)
    }

    @Test
    fun disabledItemNotificationsDoNotScheduleReminders() {
        val item = reminderItem(
            reminderAt = LocalDateTime.of(2026, 4, 26, 9, 0),
            notificationSettings = ItemNotificationSettings(enabled = false),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        assertNull(request)
    }

    @Test
    fun oneTimeReminderSchedulesAtExactFutureReminderTime() {
        val item = reminderItem(
            title = "Call insurance",
            body = "Ask about renewal paperwork.",
            reminderAt = LocalDateTime.of(2026, 4, 26, 10, 30),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(defaultSnoozeMinutes = 20),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        requireNotNull(request)
        assertEquals(1L, request.itemId)
        assertEquals("Call insurance", request.title)
        assertEquals("Ask about renewal paperwork.", request.body)
        assertEquals(LocalDateTime.of(2026, 4, 26, 10, 30), request.triggerAt)
        assertEquals(LocalDateTime.of(2026, 4, 26, 10, 30), request.dueAt)
        assertEquals(0, request.windowMinutes)
        assertEquals(20, request.snoozeMinutes)
    }

    @Test
    fun pastOneTimeReminderIsNotScheduled() {
        val item = reminderItem(
            reminderAt = LocalDateTime.of(2026, 4, 24, 10, 30),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        assertNull(request)
    }

    @Test
    fun completedOneTimeReminderIsNotScheduled() {
        val item = reminderItem(
            reminderAt = LocalDateTime.of(2026, 4, 26, 10, 30),
            taskStatus = TaskStatus.Done,
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        assertNull(request)
    }

    @Test
    fun recurringReminderUsesOverrideTimeFlexibleWindowAndItemSnooze() {
        val item = reminderItem(
            title = "Stretch",
            createdAt = LocalDateTime.of(2026, 4, 20, 8, 0),
            reminderAt = LocalDateTime.of(2026, 4, 20, 9, 0),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            notificationSettings = ItemNotificationSettings(
                timeOverride = LocalTime.of(18, 30),
                flexibleWindowMinutes = 45,
                snoozeMinutes = 60,
            ),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(
                preferredTime = LocalTime.of(7, 0),
                flexibleWindowMinutes = 20,
                defaultSnoozeMinutes = 10,
                batchNotifications = true,
            ),
            now = LocalDateTime.of(2026, 4, 25, 12, 0),
        )

        requireNotNull(request)
        assertEquals(LocalDateTime.of(2026, 4, 25, 17, 45), request.triggerAt)
        assertEquals(LocalDateTime.of(2026, 4, 25, 18, 30), request.dueAt)
        assertEquals(45, request.windowMinutes)
        assertEquals(60, request.snoozeMinutes)
        assertEquals(true, request.batchNotifications)
    }

    @Test
    fun weeklyReminderSkipsToNextOccurrence() {
        val item = reminderItem(
            createdAt = LocalDateTime.of(2026, 4, 20, 8, 0),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Weekly),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(preferredTime = LocalTime.of(9, 0)),
            now = LocalDateTime.of(2026, 4, 25, 12, 0),
        )

        requireNotNull(request)
        assertEquals(LocalDateTime.of(2026, 4, 27, 9, 0), request.dueAt)
    }

    @Test
    fun recurringReminderSkipsCompletedOccurrenceDate() {
        val item = reminderItem(
            createdAt = LocalDateTime.of(2026, 4, 25, 8, 0),
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Daily),
            completionHistory = listOf(
                completionRecord(LocalDate.of(2026, 4, 25)),
            ),
        )

        val request = item.nextReminderRequest(
            globalSettings = NotificationSettings(preferredTime = LocalTime.of(18, 0)),
            now = LocalDateTime.of(2026, 4, 25, 9, 0),
        )

        requireNotNull(request)
        assertEquals(LocalDateTime.of(2026, 4, 26, 18, 0), request.dueAt)
    }
}

private fun reminderItem(
    title: String = "Reminder",
    body: String = "",
    createdAt: LocalDateTime = LocalDateTime.of(2026, 4, 25, 8, 0),
    reminderAt: LocalDateTime? = null,
    recurrenceRule: RecurrenceRule = RecurrenceRule(),
    notificationSettings: ItemNotificationSettings = ItemNotificationSettings(),
    taskStatus: TaskStatus? = null,
    completionHistory: List<CompletionRecord> = emptyList(),
): LifeItem = LifeItem(
    id = 1L,
    type = LifeItemType.Reminder,
    title = title,
    body = body,
    createdAt = createdAt,
    taskStatus = taskStatus,
    reminderAt = reminderAt,
    recurrenceRule = recurrenceRule,
    notificationSettings = notificationSettings,
    completionHistory = completionHistory,
)

private fun completionRecord(date: LocalDate): CompletionRecord =
    CompletionRecord(
        itemId = 1L,
        occurrenceDate = date,
        completedAt = date.atTime(8, 0),
    )
