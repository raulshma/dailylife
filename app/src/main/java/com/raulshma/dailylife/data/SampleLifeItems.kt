package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDateTime
import java.time.LocalTime

internal object SampleLifeItems {
    fun create(now: LocalDateTime = LocalDateTime.now()): List<LifeItem> = listOf(
        LifeItem(
            id = 1L,
            type = LifeItemType.Thought,
            title = "Morning reset",
            body = "Write the three things that would make today feel grounded.",
            createdAt = now.minusHours(2),
            tags = setOf("journal", "mind"),
            isPinned = true,
        ),
        LifeItem(
            id = 2L,
            type = LifeItemType.Task,
            title = "Review weekly errands",
            body = "Check pantry, pharmacy list, and the repair appointment.",
            createdAt = now.minusDays(1).plusHours(3),
            tags = setOf("home", "planning"),
            taskStatus = TaskStatus.InProgress,
            recurrenceRule = RecurrenceRule(RecurrenceFrequency.Weekly),
            notificationSettings = ItemNotificationSettings(
                enabled = true,
                timeOverride = LocalTime.of(18, 30),
                snoozeMinutes = 60,
            ),
        ),
        LifeItem(
            id = 3L,
            type = LifeItemType.Photo,
            title = "Balcony basil",
            body = "https://picsum.photos/seed/balconybasil/900/1200 A visual note for how the plant looked after trimming.",
            createdAt = now.minusDays(2).plusMinutes(20),
            tags = setOf("home", "garden"),
            isFavorite = true,
        ),
        LifeItem(
            id = 4L,
            type = LifeItemType.Reminder,
            title = "Call insurance",
            body = "Ask about renewal paperwork and premium changes.",
            createdAt = now.minusDays(3),
            tags = setOf("admin"),
            reminderAt = now.plusDays(1).withHour(10).withMinute(0),
            notificationSettings = ItemNotificationSettings(enabled = true),
        ),
        LifeItem(
            id = 5L,
            type = LifeItemType.Video,
            title = "Evening walk clip",
            body = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            createdAt = now.minusDays(1).minusHours(4),
            tags = setOf("health", "walk"),
        ),
        LifeItem(
            id = 6L,
            type = LifeItemType.Location,
            title = "New coffee place",
            body = "geo:40.73061,-73.935242",
            createdAt = now.minusDays(4).plusHours(2),
            tags = setOf("food", "map"),
            isFavorite = true,
        ),
        LifeItem(
            id = 7L,
            type = LifeItemType.Audio,
            title = "Grocery voice memo",
            body = "Remember oats, eggs, and basil seeds for the balcony planters.",
            createdAt = now.minusHours(9),
            tags = setOf("shopping"),
        ),
    )
}
