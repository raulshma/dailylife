package com.raulshma.dailylife.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.backup.S3BackupRepository
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.security.EncryptionProgress
import com.raulshma.dailylife.data.security.MediaDecryptCoordinator
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.S3BackupSettings
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.notifications.GeofenceManager
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DailyLifeViewModel @Inject constructor(
    private val repository: DailyLifeRepository,
    private val reminderScheduler: ReminderScheduler,
    private val mediaEncryptionManager: MediaEncryptionManager,
    val decryptCoordinator: MediaDecryptCoordinator,
    private val s3BackupRepository: S3BackupRepository,
    private val roomStore: RoomDailyLifeStore,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val state = repository.state

    private val _s3BackupSettings = MutableStateFlow(S3BackupSettings())
    val s3BackupSettings: StateFlow<S3BackupSettings> = _s3BackupSettings.asStateFlow()

    private val _lastBackupResult = MutableStateFlow<BackupResult?>(null)
    val lastBackupResult: StateFlow<BackupResult?> = _lastBackupResult.asStateFlow()

    private val _encryptionProgress = MutableStateFlow<EncryptionProgress?>(null)
    val encryptionProgress: StateFlow<EncryptionProgress?> = _encryptionProgress.asStateFlow()

    private var saveJob: Job? = null

    init {
        repository.rolloverMissedOccurrences()
        syncReminderSchedule()
        viewModelScope.launch { _s3BackupSettings.value = roomStore.loadS3BackupSettings() }
    }

    fun addItem(draft: LifeItemDraft) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val encryptedBody = withContext(Dispatchers.IO) {
                mediaEncryptionManager.encryptMediaInTextWithProgress(
                    draft.body, context,
                    onProgress = { progress -> _encryptionProgress.value = progress },
                )
            }
            val encryptedDraft = draft.copy(body = encryptedBody)
            repository.addItem(encryptedDraft)
            syncReminderSchedule()
            _encryptionProgress.value = null
        }
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

    fun selectCollection(itemIds: Set<Long>?) {
        repository.selectCollection(itemIds)
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
        val location = lastKnownLocation()
        val batteryLevel = currentBatteryLevel()
        val appVersion = currentAppVersion()
        repository.markOccurrenceCompleted(
            itemId = itemId,
            occurrenceDate = LocalDate.now(),
            latitude = location?.first,
            longitude = location?.second,
            batteryLevel = batteryLevel,
            appVersion = appVersion,
        )
        syncReminderSchedule()
    }

    private fun lastKnownLocation(): Pair<Double, Double>? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        var bestLocation: android.location.Location? = null
        for (provider in locationManager.getProviders(true)) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            } catch (_: SecurityException) {}
        }
        return bestLocation?.let { Pair(it.latitude, it.longitude) }
    }

    private fun currentBatteryLevel(): Int? {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return null
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .takeIf { it in 0..100 }
    }

    private fun currentAppVersion(): String? {
        return runCatching {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            pi.versionName
        }.getOrNull()
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

    fun updateS3BackupSettings(settings: S3BackupSettings) {
        _s3BackupSettings.value = settings
        viewModelScope.launch { roomStore.saveS3BackupSettings(settings) }
    }

    fun performS3Backup() {
        viewModelScope.launch {
            val currentState = repository.state.value
            val settings = _s3BackupSettings.value
            val snapshot = BackupSnapshot(
                items = currentState.items,
                notificationSettings = currentState.notificationSettings,
                exportedAt = Instant.now(),
            )
            val mediaPaths: List<String> = currentState.items
                .flatMap { item ->
                    listOfNotNull(item.inferImagePreviewUrl(), item.inferVideoPlaybackUrl())
                        .filter { it.startsWith("file://") || it.startsWith("content://") }
                }
            _lastBackupResult.value = s3BackupRepository.performBackup(snapshot, mediaPaths, settings)
        }
    }

    fun clearBackupResult() {
        _lastBackupResult.value = null
    }

    fun updateItem(itemId: Long, draft: LifeItemDraft) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val encryptedBody = withContext(Dispatchers.IO) {
                mediaEncryptionManager.encryptMediaInTextWithProgress(
                    draft.body, context,
                    onProgress = { progress -> _encryptionProgress.value = progress },
                )
            }
            val encryptedDraft = draft.copy(body = encryptedBody)
            repository.updateItem(encryptedDraft, itemId)
            syncReminderSchedule()
            _encryptionProgress.value = null
        }
    }

    fun deleteItem(itemId: Long) {
        repository.deleteItem(itemId)
        syncReminderSchedule()
    }

    fun updateCompletionRecord(itemId: Long, record: CompletionRecord) {
        repository.updateCompletionRecord(itemId, record)
    }

    fun deleteCompletionRecord(itemId: Long, occurrenceDate: LocalDate, completedAt: LocalDateTime) {
        repository.deleteCompletionRecord(itemId, occurrenceDate, completedAt)
    }

    private fun syncReminderSchedule() {
        val currentState = repository.state.value
        reminderScheduler.sync(
            items = currentState.items,
            settings = currentState.notificationSettings,
        )
        geofenceManager.syncGeofences(currentState.items)
    }

    fun restoreFromS3() {
        viewModelScope.launch {
            val settings = _s3BackupSettings.value
            val snapshot = s3BackupRepository.restoreLatestSnapshot(settings)
            if (snapshot != null) {
                _lastBackupResult.value = BackupResult.Success(
                    itemsBackedUp = snapshot.items.size,
                    mediaFilesBackedUp = 0,
                )
                repository.importSnapshot(snapshot)
                syncReminderSchedule()
            } else {
                _lastBackupResult.value = BackupResult.Failure("No backup found or restore failed")
            }
        }
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch {
            runCatching {
                val snapshot = com.raulshma.dailylife.data.backup.SnapshotDeserializer.deserialize(json)
                repository.importSnapshot(snapshot)
                syncReminderSchedule()
                _lastBackupResult.value = BackupResult.Success(
                    itemsBackedUp = snapshot.items.size,
                    mediaFilesBackedUp = 0,
                )
            }.getOrElse { error ->
                _lastBackupResult.value = BackupResult.Failure(error.message ?: "Import failed")
            }
        }
    }

    fun toggleArchive(itemId: Long) {
        repository.toggleArchive(itemId)
    }

    fun toggleShowArchived() {
        repository.toggleShowArchived()
    }
}
