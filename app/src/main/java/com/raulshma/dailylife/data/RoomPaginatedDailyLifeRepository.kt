package com.raulshma.dailylife.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.raulshma.dailylife.data.db.DailyLifeDao
import com.raulshma.dailylife.data.db.LifeItemWithCompletions
import com.raulshma.dailylife.data.db.toEntity
import com.raulshma.dailylife.data.db.toLifeItem
import com.raulshma.dailylife.data.db.toNotificationSettings
import com.raulshma.dailylife.domain.BackupSnapshot
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
import com.raulshma.dailylife.domain.SnapshotStats
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.domain.StorageOperation
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.stepDays
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomPaginatedDailyLifeRepository(
    private val dao: DailyLifeDao,
    private val persistScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : DailyLifeRepository {

    private val _state = MutableStateFlow(DailyLifeState())
    override val state: StateFlow<DailyLifeState> = _state.asStateFlow()

    @OptIn(FlowPreview::class)
    override val pagingItems: Flow<PagingData<LifeItem>> = _state
        .map { it.filters }
        .debounce(80L)
        .distinctUntilChanged()
        .flatMapLatest { filters ->
            Pager(PagingConfig(pageSize = 30, enablePlaceholders = false)) {
                createPagingSource(filters)
            }.flow.map { pagingData -> pagingData.map { it.toLifeItem() } }
        }

    override val allTags: Flow<List<String>> = dao.allTagStrings()
        .map { tagStrings ->
            tagStrings
                .flatMap { it.split(",").map { t -> t.trim() }.filter { it.isNotEmpty() } }
                .distinct()
                .sorted()
        }

    override val snapshotStats: Flow<SnapshotStats> = combine(
        dao.itemCount(),
        dao.completedCount(),
        allTags,
    ) { itemCount, completedCount, tags ->
        SnapshotStats(
            itemCount = itemCount,
            tagCount = tags.size,
            completedCount = completedCount,
        )
    }

    override val collectionCounts: Flow<CollectionCounts> = combine(
        dao.favoriteCount(),
        dao.videoCount(),
        dao.pdfCount(),
        dao.placeCount(),
        dao.notesCount(),
    ) { favorites, videos, pdfs, places, notes ->
        CollectionCounts(
            favorites = favorites,
            videos = videos,
            pdfs = pdfs,
            places = places,
            notes = notes,
        )
    }

    override val taggedItemsForGraph: Flow<List<LifeItem>> = dao.taggedItemsForGraph()
        .map { items -> items.map { it.toLifeItem() } }

    override val allItemIds: Flow<List<Long>> = dao.allItemIds()

    private var nextId: Long = 0L

    init {
        persistScope.launch {
            runCatching {
                val settingsEntity = dao.getNotificationSettings()
                _state.update { current ->
                    current.copy(
                        notificationSettings = settingsEntity?.toNotificationSettings() ?: NotificationSettings(),
                    )
                }
                val items = dao.getAllItemsWithCompletions()
                nextId = (items.maxOfOrNull { it.item.id } ?: 0L) + 1L
            }.onFailure { error ->
                _state.update { current ->
                    current.copy(storageError = error.toStorageError(StorageOperation.Load))
                }
            }
        }
    }

    override fun updateSearchQuery(query: String) {
        updateFilters { it.copy(query = query) }
    }

    override fun selectType(type: LifeItemType?) {
        updateFilters { it.copy(selectedType = type, selectedTypes = null) }
    }

    override fun selectTag(tag: String?) {
        updateFilters { it.copy(selectedTag = tag) }
    }

    override fun updateDateRange(start: LocalDate?, end: LocalDate?) {
        updateFilters { it.copy(dateRangeStart = start, dateRangeEnd = end) }
    }

    override fun toggleFavoritesOnly() {
        updateFilters { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    override fun clearFilters() {
        _state.update { it.copy(filters = DailyLifeFilters()) }
    }

    override fun selectCollection(itemIds: Set<Long>?) {
        updateFilters { it.copy(collectionItemIds = itemIds) }
    }

    override fun selectTypes(types: Set<LifeItemType>?) {
        updateFilters { it.copy(selectedTypes = types, selectedType = null) }
    }

    override suspend fun addItem(draft: LifeItemDraft): LifeItem {
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
        withContext(Dispatchers.IO) {
            dao.insertItem(item.toEntity())
        }
        return item
    }

    override suspend fun toggleFavorite(itemId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId) ?: return@withContext
        val updated = existing.item.copy(isFavorite = !existing.item.isFavorite)
        dao.insertItem(updated)
    }

    override suspend fun togglePinned(itemId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId) ?: return@withContext
        val updated = existing.item.copy(isPinned = !existing.item.isPinned)
        dao.insertItem(updated)
    }

    override suspend fun updateTaskStatus(itemId: Long, status: TaskStatus) =
        withContext(Dispatchers.IO) {
            val existing = dao.getItemById(itemId) ?: return@withContext
            val updated = existing.item.copy(taskStatus = status.name)
            dao.insertItem(updated)
        }

    override suspend fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate,
        completedAt: LocalDateTime,
        latitude: Double?,
        longitude: Double?,
        batteryLevel: Int?,
        appVersion: String?,
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId) ?: return@withContext
        val record = CompletionRecord(
            itemId = itemId,
            occurrenceDate = occurrenceDate,
            completedAt = completedAt,
            latitude = latitude,
            longitude = longitude,
            batteryLevel = batteryLevel,
            appVersion = appVersion,
        )
        dao.insertCompletionRecords(listOf(record.toEntity()))
        if (existing.item.type == LifeItemType.Task.name) {
            val updated = existing.item.copy(taskStatus = TaskStatus.Done.name)
            dao.insertItem(updated)
        }
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        withContext(Dispatchers.IO) {
            dao.insertNotificationSettings(settings.toEntity())
        }
        _state.update { it.copy(notificationSettings = settings) }
    }

    override suspend fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) =
        withContext(Dispatchers.IO) {
            val existing = dao.getItemById(itemId) ?: return@withContext
            val updated = existing.item.copy(
                notificationEnabled = settings.enabled,
                notificationTimeOverride = settings.timeOverride?.toString(),
                notificationFlexibleWindow = settings.flexibleWindowMinutes,
                notificationSnoozeMinutes = settings.snoozeMinutes,
                geofenceLatitude = settings.geofenceLatitude,
                geofenceLongitude = settings.geofenceLongitude,
                geofenceRadiusMeters = settings.geofenceRadiusMeters,
                geofenceTrigger = settings.geofenceTrigger.name,
            )
            dao.insertItem(updated)
        }

    override suspend fun rolloverMissedOccurrences(referenceDate: LocalDate) =
        withContext(Dispatchers.IO) {
            val recurringItems = dao.getRecurringItemsWithCompletions()
            val newRecords = mutableListOf<com.raulshma.dailylife.data.db.CompletionRecordEntity>()
            for (itemWithCompletions in recurringItems) {
                val entity = itemWithCompletions.item
                val completions = itemWithCompletions.completions

                val completedDates = completions
                    .filterNot { it.missed }
                    .mapNotNull { runCatching { LocalDate.parse(it.occurrenceDate) }.getOrNull() }
                    .toSet()
                val missedDates = completions
                    .filter { it.missed }
                    .mapNotNull { runCatching { LocalDate.parse(it.occurrenceDate) }.getOrNull() }
                    .toSet()

                val frequency = try {
                    RecurrenceFrequency.valueOf(entity.recurrenceFrequency)
                } catch (_: Exception) {
                    RecurrenceFrequency.None
                }
                if (frequency == RecurrenceFrequency.None) continue

                val rule = RecurrenceRule(
                    frequency = frequency,
                    interval = entity.recurrenceInterval.coerceAtLeast(1),
                )
                val startDate = entity.reminderAt?.let {
                    runCatching { LocalDateTime.parse(it) }?.getOrNull()?.toLocalDate()
                } ?: runCatching { LocalDateTime.parse(entity.createdAt) }?.getOrNull()?.toLocalDate() ?: continue

                val stepDays = rule.stepDays()
                var occurrenceDate = startDate
                while (!occurrenceDate.isAfter(referenceDate)) {
                    if (occurrenceDate.isBefore(referenceDate) &&
                        occurrenceDate !in completedDates &&
                        occurrenceDate !in missedDates
                    ) {
                        newRecords.add(
                            com.raulshma.dailylife.data.db.CompletionRecordEntity(
                                itemId = entity.id,
                                occurrenceDate = occurrenceDate.toString(),
                                completedAt = occurrenceDate.atTime(0, 0).toString(),
                                missed = true,
                            )
                        )
                    }
                    occurrenceDate = occurrenceDate.plusDays(stepDays)
                }
            }
            if (newRecords.isNotEmpty()) {
                dao.insertCompletionRecords(newRecords)
            }
        }

    override fun clearStorageError() {
        _state.update { it.copy(storageError = null) }
    }

    override suspend fun updateItem(draft: LifeItemDraft, itemId: Long): LifeItem =
        withContext(Dispatchers.IO) {
            val existing = dao.getItemById(itemId) ?: throw NoSuchElementException("Item $itemId not found")
            val updated = existing.item.copy(
                type = draft.type.name,
                title = draft.title.ifBlank { draft.type.label },
                body = draft.body,
                tags = normalizeTags(draft.tags).sorted().joinToString(","),
                isFavorite = draft.isFavorite,
                isPinned = draft.isPinned,
                taskStatus = draft.taskStatus?.name ?: existing.item.taskStatus,
                reminderAt = draft.reminderAt?.toString() ?: existing.item.reminderAt,
                recurrenceFrequency = draft.recurrenceRule.frequency.name,
                recurrenceInterval = draft.recurrenceRule.interval.coerceAtLeast(1),
                recurrenceDaysOfWeek = draft.recurrenceRule.daysOfWeek.joinToString(",") { it.name },
                recurrenceDayOfWeek = draft.recurrenceRule.dayOfWeek?.name,
                recurrenceWeekOfMonth = draft.recurrenceRule.weekOfMonth?.name,
                notificationEnabled = draft.notificationSettings.enabled,
                notificationTimeOverride = draft.notificationSettings.timeOverride?.toString(),
                notificationFlexibleWindow = draft.notificationSettings.flexibleWindowMinutes,
                notificationSnoozeMinutes = draft.notificationSettings.snoozeMinutes,
                geofenceLatitude = draft.notificationSettings.geofenceLatitude,
                geofenceLongitude = draft.notificationSettings.geofenceLongitude,
                geofenceRadiusMeters = draft.notificationSettings.geofenceRadiusMeters,
                geofenceTrigger = draft.notificationSettings.geofenceTrigger.name,
            )
            dao.insertItem(updated)
            dao.getItemById(itemId)!!.toLifeItem()
        }

    override suspend fun deleteItem(itemId: Long) = withContext(Dispatchers.IO) {
        dao.deleteItemCascade(itemId)
    }

    override suspend fun updateCompletionRecord(itemId: Long, record: CompletionRecord) =
        withContext(Dispatchers.IO) {
            dao.deleteCompletionRecordByKey(
                itemId,
                record.occurrenceDate.toString(),
                record.completedAt.toString(),
            )
            dao.insertCompletionRecords(listOf(record.toEntity()))
        }

    override suspend fun deleteCompletionRecord(
        itemId: Long,
        occurrenceDate: LocalDate,
        completedAt: LocalDateTime,
    ) = withContext(Dispatchers.IO) {
        dao.deleteCompletionRecordByKey(itemId, occurrenceDate.toString(), completedAt.toString())
    }

    override suspend fun importSnapshot(snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        val existingItems = dao.getAllItemsWithCompletions()
        val existingMap = existingItems.associateBy { it.item.id }
        val existingDates = existingItems.flatMap { iwc ->
            iwc.completions.map { Triple(it.itemId, it.occurrenceDate, it.completedAt) }
        }.toSet()

        val itemEntities = mutableListOf<com.raulshma.dailylife.data.db.LifeItemEntity>()
        val completionEntities = mutableListOf<com.raulshma.dailylife.data.db.CompletionRecordEntity>()

        for (snapshotItem in snapshot.items) {
            val entity = snapshotItem.toEntity()
            val existing = existingMap[snapshotItem.id]
            if (existing != null) {
                val newRecords = snapshotItem.completionHistory.filter { record ->
                    Triple(snapshotItem.id, record.occurrenceDate.toString(), record.completedAt.toString()) !in existingDates
                }
                completionEntities.addAll(newRecords.map { it.toEntity() })
            } else {
                itemEntities.add(entity)
                completionEntities.addAll(snapshotItem.completionHistory.map { it.toEntity() })
            }
        }

        if (itemEntities.isNotEmpty()) {
            dao.insertItems(itemEntities)
        }
        if (completionEntities.isNotEmpty()) {
            dao.insertCompletionRecords(completionEntities)
        }
        dao.insertNotificationSettings(snapshot.notificationSettings.toEntity())
        nextId = ((existingMap.keys + snapshot.items.map { it.id }).maxOrNull() ?: 0L) + 1L
    }

    override suspend fun toggleArchive(itemId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId) ?: return@withContext
        val updated = existing.item.copy(isArchived = !existing.item.isArchived)
        dao.insertItem(updated)
    }

    override fun toggleShowArchived() {
        updateFilters { it.copy(showArchived = !it.showArchived) }
    }

    override suspend fun getItem(id: Long): LifeItem? = withContext(Dispatchers.IO) {
        dao.getItemById(id)?.toLifeItem()
    }

    override suspend fun getAllItems(): List<LifeItem> = withContext(Dispatchers.IO) {
        dao.getAllItemsWithCompletions().map { it.toLifeItem() }
    }

    private fun updateFilters(block: (DailyLifeFilters) -> DailyLifeFilters) {
        _state.update { current -> current.copy(filters = block(current.filters)) }
    }

    private fun createPagingSource(filters: DailyLifeFilters): androidx.paging.PagingSource<Int, LifeItemWithCompletions> {
        val itemIdsCsv = filters.collectionItemIds?.takeIf { it.isNotEmpty() }?.joinToString(",")
        val typesCsv = filters.selectedTypes?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }

        return dao.filteredPagingSource(
            query = filters.query.trim().lowercase(),
            type = filters.selectedType?.name,
            typesCsv = typesCsv,
            tag = filters.selectedTag,
            start = filters.dateRangeStart?.toString(),
            end = filters.dateRangeEnd?.toString(),
            favoritesOnly = filters.favoritesOnly,
            showArchived = filters.showArchived,
            itemIdsCsv = itemIdsCsv,
        )
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
                "DailyLife couldn't load local data. Reason: $reason"
            StorageOperation.Save ->
                "DailyLife couldn't save local changes. Reason: $reason"
        }
        return StorageError(operation = operation, message = message)
    }

    private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
        var retryCount = 0
        while (retryCount < 100) {
            val current = value
            val updated = block(current)
            if (compareAndSet(current, updated)) break
            retryCount++
        }
    }
}
