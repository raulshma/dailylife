package com.raulshma.dailylife.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class LifeItemType(val label: String) {
    Thought("Thought"),
    Note("Note"),
    Task("Task"),
    Reminder("Reminder"),
    Photo("Photo"),
    Video("Video"),
    Audio("Audio"),
    Location("Location"),
    Mixed("Mixed"),
}

enum class TaskStatus(val label: String) {
    Open("Open"),
    InProgress("In progress"),
    Done("Done"),
}

enum class RecurrenceFrequency(val label: String) {
    None("None"),
    Daily("Daily"),
    Weekly("Weekly"),
    Custom("Custom"),
}

data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.None,
    val interval: Int = 1,
)

data class NotificationSettings(
    val globalEnabled: Boolean = true,
    val preferredTime: LocalTime = LocalTime.of(9, 0),
    val flexibleWindowMinutes: Int = 0,
    val defaultSnoozeMinutes: Int = 10,
    val batchNotifications: Boolean = false,
    val respectDoNotDisturb: Boolean = true,
)

data class ItemNotificationSettings(
    val enabled: Boolean = true,
    val timeOverride: LocalTime? = null,
    val flexibleWindowMinutes: Int? = null,
    val snoozeMinutes: Int? = null,
)

data class CompletionRecord(
    val itemId: Long,
    val occurrenceDate: LocalDate,
    val completedAt: LocalDateTime,
    val missed: Boolean = false,
)

data class LifeItem(
    val id: Long,
    val type: LifeItemType,
    val title: String,
    val body: String,
    val createdAt: LocalDateTime,
    val tags: Set<String> = emptySet(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val taskStatus: TaskStatus? = null,
    val reminderAt: LocalDateTime? = null,
    val recurrenceRule: RecurrenceRule = RecurrenceRule(),
    val notificationSettings: ItemNotificationSettings = ItemNotificationSettings(),
    val completionHistory: List<CompletionRecord> = emptyList(),
) {
    val isRecurring: Boolean
        get() = recurrenceRule.frequency != RecurrenceFrequency.None
}

data class LifeItemDraft(
    val type: LifeItemType = LifeItemType.Thought,
    val title: String = "",
    val body: String = "",
    val tags: Set<String> = emptySet(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val taskStatus: TaskStatus? = null,
    val reminderAt: LocalDateTime? = null,
    val recurrenceRule: RecurrenceRule = RecurrenceRule(),
    val notificationSettings: ItemNotificationSettings = ItemNotificationSettings(),
)

data class DailyLifeFilters(
    val query: String = "",
    val selectedType: LifeItemType? = null,
    val selectedTag: String? = null,
    val favoritesOnly: Boolean = false,
)

data class DailyLifeState(
    val items: List<LifeItem> = emptyList(),
    val filters: DailyLifeFilters = DailyLifeFilters(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
) {
    val allTags: List<String>
        get() = items.flatMap { it.tags }.distinct().sorted()

    val visibleItems: List<LifeItem>
        get() = items
            .filter { item -> item.matches(filters) }
            .sortedWith(
                compareByDescending<LifeItem> { it.isPinned }
                    .thenByDescending { it.createdAt },
            )
}

private fun LifeItem.matches(filters: DailyLifeFilters): Boolean {
    val normalizedQuery = filters.query.trim().lowercase()
    val matchesQuery = normalizedQuery.isEmpty() ||
        title.lowercase().contains(normalizedQuery) ||
        body.lowercase().contains(normalizedQuery) ||
        type.label.lowercase().contains(normalizedQuery) ||
        tags.any { tag -> tag.lowercase().contains(normalizedQuery) }

    return matchesQuery &&
        (filters.selectedType == null || type == filters.selectedType) &&
        (filters.selectedTag == null || tags.contains(filters.selectedTag)) &&
        (!filters.favoritesOnly || isFavorite)
}
