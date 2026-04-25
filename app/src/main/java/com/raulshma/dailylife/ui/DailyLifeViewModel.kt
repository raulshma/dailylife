package com.raulshma.dailylife.ui

import androidx.lifecycle.ViewModel
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyLifeViewModel @Inject constructor(
    private val repository: DailyLifeRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val state = repository.state

    init {
        repository.rolloverMissedOccurrences()
        syncReminderSchedule()
    }

    fun addItem(draft: LifeItemDraft) {
        repository.addItem(draft)
        syncReminderSchedule()
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

    fun updateDateRange(start: LocalDate?, end: LocalDate?) {
        repository.updateDateRange(start, end)
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
        syncReminderSchedule()
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        repository.updateNotificationSettings(settings)
        syncReminderSchedule()
    }

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        repository.updateItemNotifications(itemId, settings)
        syncReminderSchedule()
    }

    fun clearStorageError() {
        repository.clearStorageError()
    }

    private fun syncReminderSchedule() {
        val currentState = repository.state.value
        reminderScheduler.sync(
            items = currentState.items,
            settings = currentState.notificationSettings,
        )
    }
}
