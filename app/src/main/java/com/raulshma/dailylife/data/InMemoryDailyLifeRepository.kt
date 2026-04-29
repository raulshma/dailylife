package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.DailyLifeFilters
import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.domain.StorageOperation
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.stepDays
import com.raulshma.dailylife.domain.BackupSnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InMemoryDailyLifeRepository(
    seedItems: List<LifeItem> = SampleLifeItems.create(),
    private val store: DailyLifeStore? = null,
    private val persistScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val persistDebounceMs: Long = PERSIST_DEBOUNCE_MS,
) {
    private val _state = MutableStateFlow(
        InMemoryState(
            items = seedItems,
            notificationSettings = NotificationSettings(),
        ),
    )
    val state: StateFlow<InMemoryState> = _state.asStateFlow()

    private var nextId = (seedItems.maxOfOrNull { it.id } ?: 0L) + 1L

    private var persistJob: Job? = null

    init {
        store?.let { s ->
            persistScope.launch {
                val loadResult = runCatching { s.load() }
                val persistedState = loadResult.getOrNull()
                _state.update { current ->
                    current.copy(
                        items = persistedState?.items ?: current.items,
                        notificationSettings = persistedState?.notificationSettings ?: current.notificationSettings,
                        storageError = loadResult.exceptionOrNull()?.toStorageError(StorageOperation.Load),
                    )
                }
                persistedState?.nextId?.let { nid ->
                    nextId = nid.coerceAtLeast(nextId)
                }
            }
        }
    }

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
        updateStoredState(persist = true) { current -> current.copy(items = listOf(item) + current.items) }
        return item
    }

    fun updateSearchQuery(query: String) { updateFilters { it.copy(query = query) } }
    fun selectType(type: LifeItemType?) { updateFilters { it.copy(selectedType = type) } }
    fun selectTag(tag: String?) { updateFilters { it.copy(selectedTag = tag) } }
    fun updateDateRange(start: LocalDate?, end: LocalDate?) { updateFilters { it.copy(dateRangeStart = start, dateRangeEnd = end) } }
    fun toggleFavoritesOnly() { updateFilters { it.copy(favoritesOnly = !it.favoritesOnly) } }
    fun clearFilters() { _state.update { current -> current.copy(filters = DailyLifeFilters()) } }
    fun selectCollection(itemIds: Set<Long>?) { updateFilters { it.copy(collectionItemIds = itemIds) } }
    fun toggleShowArchived() { updateFilters { it.copy(showArchived = !it.showArchived) } }

    fun toggleFavorite(itemId: Long) { updateItem(itemId) { it.copy(isFavorite = !it.isFavorite) } }
    fun togglePinned(itemId: Long) { updateItem(itemId) { it.copy(isPinned = !it.isPinned) } }
    fun updateTaskStatus(itemId: Long, status: TaskStatus) { updateItem(itemId) { it.copy(taskStatus = status) } }

    fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate = LocalDate.now(),
        completedAt: LocalDateTime = LocalDateTime.now(),
        latitude: Double? = null,
        longitude: Double? = null,
        batteryLevel: Int? = null,
        appVersion: String? = null,
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

    fun updateNotificationSettings(settings: NotificationSettings) {
        updateStoredState(persist = true) { current -> current.copy(notificationSettings = settings) }
    }

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        updateItem(itemId) { it.copy(notificationSettings = settings) }
    }

    fun rolloverMissedOccurrences(referenceDate: LocalDate = LocalDate.now()) {
        val currentItems = _state.value.items
        val updatedItems = currentItems.map { item ->
            if (!item.isRecurring) return@map item
            val completedDates = item.completionHistory.filterNot { it.missed }.map { it.occurrenceDate }.toSet()
            val missedDates = item.completionHistory.filter { it.missed }.map { it.occurrenceDate }.toSet()
            val startDate = item.reminderAt?.toLocalDate() ?: item.createdAt.toLocalDate()
            val stepDays = item.recurrenceRule.stepDays()
            val newMissedRecords = mutableListOf<CompletionRecord>()
            var occurrenceDate = startDate
            while (!occurrenceDate.isAfter(referenceDate)) {
                if (occurrenceDate.isBefore(referenceDate) && occurrenceDate !in completedDates && occurrenceDate !in missedDates) {
                    newMissedRecords += CompletionRecord(
                        itemId = item.id,
                        occurrenceDate = occurrenceDate,
                        completedAt = occurrenceDate.atTime(0, 0),
                        missed = true,
                    )
                }
                occurrenceDate = occurrenceDate.plusDays(stepDays)
            }
            if (newMissedRecords.isNotEmpty()) item.copy(completionHistory = item.completionHistory + newMissedRecords) else item
        }
        if (updatedItems != currentItems) {
            updateStoredState(persist = true) { current -> current.copy(items = updatedItems) }
        }
    }

    fun clearStorageError() { _state.update { it.copy(storageError = null) } }

    fun updateItem(draft: LifeItemDraft, itemId: Long): LifeItem {
        updateItem(itemId) { existing ->
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

    fun deleteItem(itemId: Long) {
        updateStoredState(persist = true) { current ->
            current.copy(items = current.items.filter { it.id != itemId })
        }
    }

    fun updateCompletionRecord(itemId: Long, record: CompletionRecord) {
        updateItem(itemId) { item ->
            item.copy(
                completionHistory = item.completionHistory.map {
                    if (it.occurrenceDate == record.occurrenceDate && it.completedAt == record.completedAt) record else it
                },
            )
        }
    }

    fun deleteCompletionRecord(itemId: Long, occurrenceDate: LocalDate, completedAt: LocalDateTime) {
        updateItem(itemId) { item ->
            item.copy(completionHistory = item.completionHistory.filterNot { it.occurrenceDate == occurrenceDate && it.completedAt == completedAt })
        }
    }

    fun importSnapshot(snapshot: BackupSnapshot) {
        updateStoredState(persist = true) { current ->
            val existingIds = current.items.map { it.id }.toSet()
            val newItems = snapshot.items.filter { it.id !in existingIds }
            val existingDates = current.items.flatMap { it.completionHistory }.map {
                Pair(it.itemId, it.occurrenceDate to it.completedAt)
            }.toSet()
            val mergedItems = snapshot.items.map { snapshotItem ->
                val existing = current.items.firstOrNull { it.id == snapshotItem.id }
                if (existing != null) {
                    val newRecords = snapshotItem.completionHistory.filter { record ->
                        Pair(snapshotItem.id, record.occurrenceDate to record.completedAt) !in existingDates
                    }
                    existing.copy(completionHistory = existing.completionHistory + newRecords)
                } else {
                    snapshotItem
                }
            }
            current.copy(
                items = current.items + newItems,
                notificationSettings = snapshot.notificationSettings,
            )
        }
    }

    fun toggleArchive(itemId: Long) { updateItem(itemId) { it.copy(isArchived = !it.isArchived) } }

    private fun updateFilters(block: (DailyLifeFilters) -> DailyLifeFilters) {
        _state.update { current -> current.copy(filters = block(current.filters)) }
    }

    private fun updateItem(itemId: Long, block: (LifeItem) -> LifeItem) {
        updateStoredState(persist = true) { current ->
            current.copy(items = current.items.map { item -> if (item.id == itemId) block(item) else item })
        }
    }

    private fun updateStoredState(persist: Boolean = false, block: (InMemoryState) -> InMemoryState) {
        var updatedState: InMemoryState? = null
        _state.update { current -> block(current).also { updatedState = it } }
        if (persist) { updatedState?.let { schedulePersist(it) } }
    }

    private fun schedulePersist(state: InMemoryState) {
        val writableStore = store ?: return
        persistJob?.cancel()
        persistJob = persistScope.launch {
            delay(persistDebounceMs)
            runCatching {
                writableStore.save(PersistedDailyLifeState(items = state.items, notificationSettings = state.notificationSettings, nextId = nextId))
            }.onSuccess {
                if (state.storageError?.operation == StorageOperation.Save) { clearStorageError() }
            }.onFailure { error ->
                _state.update { current -> current.copy(storageError = error.toStorageError(StorageOperation.Save)) }
            }
        }
    }

    private fun LifeItemType.defaultTaskStatus(): TaskStatus? = if (this == LifeItemType.Task) TaskStatus.Open else null
    private fun normalizeTags(tags: Set<String>): Set<String> = tags.map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotEmpty() }.toSet()

    private fun Throwable.toStorageError(operation: StorageOperation): StorageError {
        val reason = localizedMessage?.takeIf { it.isNotBlank() } ?: this::class.java.simpleName
        val message = when (operation) {
            StorageOperation.Load -> "DailyLife couldn't load local data. Starter data is shown for now. Reason: $reason"
            StorageOperation.Save -> "DailyLife couldn't save local changes. The latest edit is visible, but may not survive app restart. Reason: $reason"
        }
        return StorageError(operation = operation, message = message)
    }

    companion object {
        private const val PERSIST_DEBOUNCE_MS = 500L
    }
}

data class InMemoryState(
    val items: List<LifeItem> = emptyList(),
    val filters: DailyLifeFilters = DailyLifeFilters(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val storageError: StorageError? = null,
) {
    val visibleItems: List<LifeItem> = items.filter { item ->
        val matchesType = filters.selectedType == null || item.type == filters.selectedType
        val matchesTypes = filters.selectedTypes == null || item.type in filters.selectedTypes
        val matchesTag = filters.selectedTag == null || filters.selectedTag in item.tags
        val matchesQuery = filters.query.isBlank() || item.title.contains(filters.query, ignoreCase = true) || item.body.contains(filters.query, ignoreCase = true)
        val matchesFavorites = !filters.favoritesOnly || item.isFavorite
        val matchesDateStart = filters.dateRangeStart == null || !item.createdAt.toLocalDate().isBefore(filters.dateRangeStart)
        val matchesDateEnd = filters.dateRangeEnd == null || !item.createdAt.toLocalDate().isAfter(filters.dateRangeEnd)
        val matchesCollection = filters.collectionItemIds == null || item.id in filters.collectionItemIds
        val matchesArchived = filters.showArchived || !item.isArchived
        matchesType && matchesTypes && matchesTag && matchesQuery && matchesFavorites && matchesDateStart && matchesDateEnd && matchesCollection && matchesArchived
    }.sortedByDescending { it.createdAt }
}
