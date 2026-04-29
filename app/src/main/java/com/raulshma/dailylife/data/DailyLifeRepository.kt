package com.raulshma.dailylife.data

import androidx.paging.PagingData
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.DailyLifeFilters
import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.SnapshotStats
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DailyLifeRepository {
    val pagingItems: Flow<PagingData<LifeItem>>
    val state: StateFlow<DailyLifeState>
    val allTags: Flow<List<String>>
    val snapshotStats: Flow<SnapshotStats>
    val collectionCounts: Flow<CollectionCounts>
    val taggedItemsForGraph: Flow<List<LifeItem>>
    val allItemIds: Flow<List<Long>>

    fun updateSearchQuery(query: String)
    fun selectType(type: LifeItemType?)
    fun selectTag(tag: String?)
    fun updateDateRange(start: LocalDate?, end: LocalDate?)
    fun toggleFavoritesOnly()
    fun clearFilters()
    fun selectCollection(itemIds: Set<Long>?)
    fun selectTypes(types: Set<LifeItemType>?)

    suspend fun addItem(draft: LifeItemDraft): LifeItem
    suspend fun toggleFavorite(itemId: Long)
    suspend fun togglePinned(itemId: Long)
    suspend fun updateTaskStatus(itemId: Long, status: TaskStatus)
    suspend fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate = LocalDate.now(),
        completedAt: LocalDateTime = LocalDateTime.now(),
        latitude: Double? = null,
        longitude: Double? = null,
        batteryLevel: Int? = null,
        appVersion: String? = null,
    )
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    suspend fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings)
    suspend fun rolloverMissedOccurrences(referenceDate: LocalDate = LocalDate.now())
    fun clearStorageError()
    suspend fun updateItem(draft: LifeItemDraft, itemId: Long): LifeItem
    suspend fun deleteItem(itemId: Long)
    suspend fun updateCompletionRecord(itemId: Long, record: CompletionRecord)
    suspend fun deleteCompletionRecord(itemId: Long, occurrenceDate: LocalDate, completedAt: LocalDateTime)
    suspend fun importSnapshot(snapshot: BackupSnapshot)
    suspend fun toggleArchive(itemId: Long)
    fun toggleShowArchived()
    suspend fun getItem(id: Long): LifeItem?
    suspend fun getAllItems(): List<LifeItem>
}

data class CollectionCounts(
    val favorites: Int = 0,
    val videos: Int = 0,
    val pdfs: Int = 0,
    val places: Int = 0,
    val notes: Int = 0,
)
