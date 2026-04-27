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
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.domain.StorageOperation
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.stepDays
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryDailyLifeRepository(
    seedItems: List<LifeItem> = SampleLifeItems.create(),
    private val store: DailyLifeStore? = null,
) : DailyLifeRepository {
    private val loadResult = runCatching { store?.load() }
    private val persistedState = loadResult.getOrNull()
    private val _state = MutableStateFlow(
        DailyLifeState(
            items = persistedState?.items ?: seedItems,
            notificationSettings = persistedState?.notificationSettings ?: NotificationSettings(),
            storageError = loadResult.exceptionOrNull()?.toStorageError(StorageOperation.Load),
        ),
    )
    override val state: StateFlow<DailyLifeState> = _state.asStateFlow()

    private var nextId = persistedState?.nextId
        ?: ((seedItems.maxOfOrNull { it.id } ?: 0L) + 1L)

    override fun addItem(draft: LifeItemDraft): LifeItem {
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

        updateStoredState { current -> current.copy(items = listOf(item) + current.items) }
        return item
    }

    override fun updateSearchQuery(query: String) {
        updateFilters { it.copy(query = query) }
    }

    override fun selectType(type: LifeItemType?) {
        updateFilters { it.copy(selectedType = type) }
    }

    override fun selectTag(tag: String?) {
        updateFilters { it.copy(selectedTag = tag) }
    }

    override fun updateDateRange(start: LocalDate?, end: LocalDate?) {
        updateFilters {
            it.copy(
                dateRangeStart = start,
                dateRangeEnd = end,
            )
        }
    }

    override fun toggleFavoritesOnly() {
        updateFilters { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    override fun clearFilters() {
        _state.update { current -> current.copy(filters = DailyLifeFilters()) }
    }

    override fun toggleFavorite(itemId: Long) {
        updateItem(itemId) { it.copy(isFavorite = !it.isFavorite) }
    }

    override fun togglePinned(itemId: Long) {
        updateItem(itemId) { it.copy(isPinned = !it.isPinned) }
    }

    override fun updateTaskStatus(itemId: Long, status: TaskStatus) {
        updateItem(itemId) { it.copy(taskStatus = status) }
    }

    override fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate,
        completedAt: LocalDateTime,
        latitude: Double?,
        longitude: Double?,
        batteryLevel: Int?,
        appVersion: String?,
    ) {
        updateItem(itemId) { item ->
            val record = CompletionRecord(
                itemId = item.id,
                occurrenceDate = occurrenceDate,
                completedAt = completedAt,
                latitude = latitude,
                longitude = longitude,
                batteryLevel = batteryLevel,
                appVersion = appVersion,
            )
            item.copy(
                taskStatus = if (item.type == LifeItemType.Task) TaskStatus.Done else item.taskStatus,
                completionHistory = listOf(record) + item.completionHistory,
            )
        }
    }

    fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate = LocalDate.now(),
        latitude: Double? = null,
        longitude: Double? = null,
        batteryLevel: Int? = null,
        appVersion: String? = null,
    ) {
        markOccurrenceCompleted(
            itemId = itemId,
            occurrenceDate = occurrenceDate,
            completedAt = LocalDateTime.now(),
            latitude = latitude,
            longitude = longitude,
            batteryLevel = batteryLevel,
            appVersion = appVersion,
        )
    }

    override fun updateNotificationSettings(settings: NotificationSettings) {
        updateStoredState { current -> current.copy(notificationSettings = settings) }
    }

    override fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        updateItem(itemId) { it.copy(notificationSettings = settings) }
    }

    override fun rolloverMissedOccurrences(referenceDate: LocalDate) {
        val currentItems = _state.value.items
        val updatedItems = currentItems.map { item ->
            if (!item.isRecurring) return@map item

            val completedDates = item.completionHistory
                .filterNot { it.missed }
                .map { it.occurrenceDate }
                .toSet()
            val missedDates = item.completionHistory
                .filter { it.missed }
                .map { it.occurrenceDate }
                .toSet()
            val startDate = item.reminderAt?.toLocalDate() ?: item.createdAt.toLocalDate()
            val stepDays = item.recurrenceRule.stepDays()

            val newMissedRecords = mutableListOf<CompletionRecord>()
            var occurrenceDate = startDate
            while (!occurrenceDate.isAfter(referenceDate)) {
                if (occurrenceDate.isBefore(referenceDate) &&
                    occurrenceDate !in completedDates &&
                    occurrenceDate !in missedDates
                ) {
                    newMissedRecords += CompletionRecord(
                        itemId = item.id,
                        occurrenceDate = occurrenceDate,
                        completedAt = occurrenceDate.atTime(0, 0),
                        missed = true,
                    )
                }
                occurrenceDate = occurrenceDate.plusDays(stepDays)
            }

            if (newMissedRecords.isNotEmpty()) {
                item.copy(completionHistory = item.completionHistory + newMissedRecords)
            } else {
                item
            }
        }

        if (updatedItems != currentItems) {
            updateStoredState { current -> current.copy(items = updatedItems) }
        }
    }

    override fun clearStorageError() {
        _state.update { current -> current.copy(storageError = null) }
    }

    override fun updateItem(draft: LifeItemDraft, itemId: Long): LifeItem {
        val updated = updateItem(itemId) { existing ->
            existing.copy(
                type = draft.type,
                title = draft.title.ifBlank { draft.type.label },
                body = draft.body,
                tags = normalizeTags(draft.tags),
                isFavorite = draft.isFavorite,
                isPinned = draft.isPinned,
                taskStatus = draft.taskStatus ?: existing.taskStatus,
                reminderAt = draft.reminderAt ?: existing.reminderAt,
                recurrenceRule = draft.recurrenceRule,
                notificationSettings = draft.notificationSettings,
            )
        }
        return _state.value.items.first { it.id == itemId }
    }

    override fun deleteItem(itemId: Long) {
        updateStoredState { current ->
            current.copy(items = current.items.filter { it.id != itemId })
        }
    }

    private fun updateFilters(block: (DailyLifeFilters) -> DailyLifeFilters) {
        _state.update { current -> current.copy(filters = block(current.filters)) }
    }

    private fun updateItem(itemId: Long, block: (LifeItem) -> LifeItem) {
        updateStoredState { current ->
            current.copy(
                items = current.items.map { item ->
                    if (item.id == itemId) block(item) else item
                },
            )
        }
    }

    private fun updateStoredState(block: (DailyLifeState) -> DailyLifeState) {
        var updatedState: DailyLifeState? = null
        _state.update { current ->
            block(current).also { updatedState = it }
        }
        updatedState?.let { persist(it) }
    }

    private fun persist(state: DailyLifeState) {
        val writableStore = store ?: return

        runCatching {
            writableStore.save(
                PersistedDailyLifeState(
                    items = state.items,
                    notificationSettings = state.notificationSettings,
                    nextId = nextId,
                ),
            )
        }.onSuccess {
            if (state.storageError?.operation == StorageOperation.Save) {
                clearStorageError()
            }
        }.onFailure { error ->
            _state.update { current ->
                current.copy(storageError = error.toStorageError(StorageOperation.Save))
            }
        }
    }

    private fun LifeItemType.defaultTaskStatus(): TaskStatus? =
        if (this == LifeItemType.Task) TaskStatus.Open else null

    private fun normalizeTags(tags: Set<String>): Set<String> =
        tags.map { it.trim().removePrefix("#").lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun Throwable.toStorageError(operation: StorageOperation): StorageError {
        val reason = localizedMessage?.takeIf { it.isNotBlank() }
            ?: this::class.java.simpleName
        val message = when (operation) {
            StorageOperation.Load ->
                "DailyLife couldn't load local data. Starter data is shown for now. Reason: $reason"
            StorageOperation.Save ->
                "DailyLife couldn't save local changes. The latest edit is visible, but may not survive app restart. Reason: $reason"
        }
        return StorageError(operation = operation, message = message)
    }
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
