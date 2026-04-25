package com.raulshma.dailylife.ui

import androidx.lifecycle.ViewModel
import com.raulshma.dailylife.data.InMemoryDailyLifeRepository
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate

class DailyLifeViewModel(
    private val repository: InMemoryDailyLifeRepository = InMemoryDailyLifeRepository(),
) : ViewModel() {
    val state = repository.state

    fun addItem(draft: LifeItemDraft) {
        repository.addItem(draft)
    }

    fun updateSearchQuery(query: String) {
        repository.updateSearchQuery(query)
    }

    fun selectType(type: LifeItemType?) {
        repository.selectType(type)
    }

    fun selectTag(tag: String?) {
        repository.selectTag(tag)
    }

    fun toggleFavoritesOnly() {
        repository.toggleFavoritesOnly()
    }

    fun clearFilters() {
        repository.clearFilters()
    }

    fun toggleFavorite(itemId: Long) {
        repository.toggleFavorite(itemId)
    }

    fun togglePinned(itemId: Long) {
        repository.togglePinned(itemId)
    }

    fun updateTaskStatus(itemId: Long, status: TaskStatus) {
        repository.updateTaskStatus(itemId, status)
    }

    fun markOccurrenceCompleted(itemId: Long) {
        repository.markOccurrenceCompleted(itemId, LocalDate.now())
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        repository.updateNotificationSettings(settings)
    }

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        repository.updateItemNotifications(itemId, settings)
    }
}
