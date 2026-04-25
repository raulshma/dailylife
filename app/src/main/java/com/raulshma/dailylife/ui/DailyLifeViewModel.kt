package com.raulshma.dailylife.ui

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.FileBackedDailyLifeRepository
import com.raulshma.dailylife.data.InMemoryDailyLifeRepository
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.notifications.AndroidReminderScheduler
import com.raulshma.dailylife.notifications.NoopReminderScheduler
import com.raulshma.dailylife.notifications.ReminderScheduler
import java.io.File
import java.time.LocalDate

class DailyLifeViewModel(
    private val repository: DailyLifeRepository = InMemoryDailyLifeRepository(),
    private val reminderScheduler: ReminderScheduler = NoopReminderScheduler,
) : ViewModel() {
    val state = repository.state

    init {
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

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyLifeViewModel::class.java)) {
                val storeFile = File(appContext.filesDir, "dailylife/local-store.properties")
                return DailyLifeViewModel(
                    repository = FileBackedDailyLifeRepository(storeFile),
                    reminderScheduler = AndroidReminderScheduler(appContext),
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
