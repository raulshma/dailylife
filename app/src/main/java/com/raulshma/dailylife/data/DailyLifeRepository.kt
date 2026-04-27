package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.StateFlow

interface DailyLifeRepository {
    val state: StateFlow<DailyLifeState>

    fun addItem(draft: LifeItemDraft): LifeItem

    fun updateSearchQuery(query: String)

    fun selectType(type: LifeItemType?)

    fun selectTag(tag: String?)

    fun updateDateRange(start: LocalDate?, end: LocalDate?)

    fun toggleFavoritesOnly()

    fun clearFilters()

    fun toggleFavorite(itemId: Long)

    fun togglePinned(itemId: Long)

    fun updateTaskStatus(itemId: Long, status: TaskStatus)

    fun markOccurrenceCompleted(
        itemId: Long,
        occurrenceDate: LocalDate = LocalDate.now(),
        completedAt: LocalDateTime = LocalDateTime.now(),
        latitude: Double? = null,
        longitude: Double? = null,
        batteryLevel: Int? = null,
        appVersion: String? = null,
    )

    fun updateNotificationSettings(settings: NotificationSettings)

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings)

    fun rolloverMissedOccurrences(referenceDate: java.time.LocalDate = java.time.LocalDate.now())

    fun clearStorageError()

    fun updateItem(draft: LifeItemDraft, itemId: Long): LifeItem

    fun deleteItem(itemId: Long)
}
