package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.DailyLifeFilters
import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryDailyLifeRepository(
    seedItems: List<LifeItem> = SampleLifeItems.create(),
) {
    private val _state = MutableStateFlow(DailyLifeState(items = seedItems))
    val state: StateFlow<DailyLifeState> = _state.asStateFlow()

    private var nextId = (seedItems.maxOfOrNull { it.id } ?: 0L) + 1L

    fun addItem(draft: LifeItemDraft): LifeItem {
        val now = LocalDateTime.now()
        val item = LifeItem(
            id = nextId++,
            type = draft.type,
            title = draft.title.ifBlank { draft.type.label },
            body = draft.body,
            createdAt = now,
            tags = normalizeTags(draft.tags),
            isFavorite = draft.isFavorite,
            isPinned = draft.isPinned,
            taskStatus = draft.taskStatus ?: draft.type.defaultTaskStatus(),
            reminderAt = draft.reminderAt,
            recurrenceRule = draft.recurrenceRule,
            notificationSettings = draft.notificationSettings,
        )

        _state.update { current -> current.copy(items = listOf(item) + current.items) }
        return item
    }

    fun updateSearchQuery(query: String) {
        updateFilters { it.copy(query = query) }
    }

    fun selectType(type: LifeItemType?) {
        updateFilters { it.copy(selectedType = type) }
    }

    fun selectTag(tag: String?) {
        updateFilters { it.copy(selectedTag = tag) }
    }

    fun toggleFavoritesOnly() {
        updateFilters { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun clearFilters() {
        _state.update { current -> current.copy(filters = DailyLifeFilters()) }
    }

    fun toggleFavorite(itemId: Long) {
        updateItem(itemId) { it.copy(isFavorite = !it.isFavorite) }
    }

    fun togglePinned(itemId: Long) {
        updateItem(itemId) { it.copy(isPinned = !it.isPinned) }
    }

    fun updateTaskStatus(itemId: Long, status: TaskStatus) {
        updateItem(itemId) { it.copy(taskStatus = status) }
    }

    fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate = LocalDate.now(),
        completedAt: LocalDateTime = LocalDateTime.now(),
    ) {
        updateItem(itemId) { item ->
            val record = CompletionRecord(
                itemId = item.id,
                occurrenceDate = occurrenceDate,
                completedAt = completedAt,
            )
            item.copy(
                taskStatus = if (item.type == LifeItemType.Task) TaskStatus.Done else item.taskStatus,
                completionHistory = listOf(record) + item.completionHistory,
            )
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _state.update { current -> current.copy(notificationSettings = settings) }
    }

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        updateItem(itemId) { it.copy(notificationSettings = settings) }
    }

    private fun updateFilters(block: (DailyLifeFilters) -> DailyLifeFilters) {
        _state.update { current -> current.copy(filters = block(current.filters)) }
    }

    private fun updateItem(itemId: Long, block: (LifeItem) -> LifeItem) {
        _state.update { current ->
            current.copy(
                items = current.items.map { item ->
                    if (item.id == itemId) block(item) else item
                },
            )
        }
    }

    private fun LifeItemType.defaultTaskStatus(): TaskStatus? =
        if (this == LifeItemType.Task) TaskStatus.Open else null

    private fun normalizeTags(tags: Set<String>): Set<String> =
        tags.map { it.trim().removePrefix("#").lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
}

private object SampleLifeItems {
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
            body = "A visual note for how the plant looked after trimming.",
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
    )
}
