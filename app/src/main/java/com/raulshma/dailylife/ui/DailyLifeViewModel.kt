package com.raulshma.dailylife.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.data.ai.ModelManager
import com.raulshma.dailylife.data.backup.S3BackupRepository
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.security.EncryptionProgress
import com.raulshma.dailylife.data.security.MediaDecryptCoordinator
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.MoodResult
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.S3BackupSettings
import com.raulshma.dailylife.data.CollectionCounts
import com.raulshma.dailylife.domain.SnapshotStats
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.domain.WritingTone
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
import kotlinx.coroutines.flow.firstOrNull
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
    val modelManager: ModelManager,
    val aiExecutor: AIFeatureExecutor,
    val engineService: LiteRTEngineService,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val state = repository.state
    val pagingItems = repository.pagingItems.cachedIn(viewModelScope)
    val allTags = repository.allTags
    val snapshotStats = repository.snapshotStats
    val collectionCounts = repository.collectionCounts
    val taggedItemsForGraph = repository.taggedItemsForGraph
    val allItemIds = repository.allItemIds

    private val _s3BackupSettings = MutableStateFlow(S3BackupSettings())
    val s3BackupSettings: StateFlow<S3BackupSettings> = _s3BackupSettings.asStateFlow()

    private val _lastBackupResult = MutableStateFlow<BackupResult?>(null)
    val lastBackupResult: StateFlow<BackupResult?> = _lastBackupResult.asStateFlow()

    private val _encryptionProgress = MutableStateFlow<EncryptionProgress?>(null)
    val encryptionProgress: StateFlow<EncryptionProgress?> = _encryptionProgress.asStateFlow()

    private val _selectedItem = MutableStateFlow<LifeItem?>(null)
    val selectedItem: StateFlow<LifeItem?> = _selectedItem.asStateFlow()

    private var saveJob: Job? = null

    val isAiEnabled: StateFlow<Boolean> = modelManager.aiEnabled
    val engineState = engineService.engineState

    fun setAiEnabled(enabled: Boolean) {
        modelManager.setAiEnabled(enabled)
    }

    private val _aiSmartTitle = MutableStateFlow("")
    val aiSmartTitle = _aiSmartTitle.asStateFlow()

    private val _aiTagSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiTagSuggestions = _aiTagSuggestions.asStateFlow()

    private val _aiSummary = MutableStateFlow("")
    val aiSummary = _aiSummary.asStateFlow()

    private val _aiRewrittenText = MutableStateFlow("")
    val aiRewrittenText = _aiRewrittenText.asStateFlow()

    private val _aiMood = MutableStateFlow<MoodResult?>(null)
    val aiMood = _aiMood.asStateFlow()

    private val _aiPhotoDescription = MutableStateFlow("")
    val aiPhotoDescription = _aiPhotoDescription.asStateFlow()

    private val _aiAudioSummary = MutableStateFlow("")
    val aiAudioSummary = _aiAudioSummary.asStateFlow()

    private val _aiSearchFilters = MutableStateFlow("")
    val aiSearchFilters = _aiSearchFilters.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    private var aiJob: Job? = null

    init {
        viewModelScope.launch { repository.rolloverMissedOccurrences() }
        viewModelScope.launch { syncReminderSchedule() }
        viewModelScope.launch { _s3BackupSettings.value = roomStore.loadS3BackupSettings() }
    }

    fun selectItem(itemId: Long, preload: LifeItem? = null) {
        if (preload != null) _selectedItem.value = preload
        viewModelScope.launch {
            _selectedItem.value = repository.getItem(itemId)
        }
    }

    fun clearSelectedItem() {
        _selectedItem.value = null
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

    fun selectTypes(types: Set<LifeItemType>?) {
        repository.selectTypes(types)
    }

    fun toggleFavorite(itemId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(itemId)
            refreshSelectedItem(itemId)
        }
    }

    fun togglePinned(itemId: Long) {
        viewModelScope.launch {
            repository.togglePinned(itemId)
            refreshSelectedItem(itemId)
        }
    }

    fun updateTaskStatus(itemId: Long, status: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(itemId, status)
            refreshSelectedItem(itemId)
        }
    }

    fun markOccurrenceCompleted(itemId: Long) {
        val location = lastKnownLocation()
        val batteryLevel = currentBatteryLevel()
        val appVersion = currentAppVersion()
        viewModelScope.launch {
            repository.markOccurrenceCompleted(
                itemId = itemId,
                occurrenceDate = LocalDate.now(),
                latitude = location?.first,
                longitude = location?.second,
                batteryLevel = batteryLevel,
                appVersion = appVersion,
            )
            refreshSelectedItem(itemId)
            syncReminderSchedule()
        }
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
        viewModelScope.launch {
            repository.updateNotificationSettings(settings)
            syncReminderSchedule()
        }
    }

    fun updateItemNotifications(itemId: Long, settings: ItemNotificationSettings) {
        viewModelScope.launch {
            repository.updateItemNotifications(itemId, settings)
            syncReminderSchedule()
        }
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
            val allItems = repository.getAllItems()
            val settings = _s3BackupSettings.value
            val currentState = state.value
            val snapshot = BackupSnapshot(
                items = allItems,
                notificationSettings = currentState.notificationSettings,
                exportedAt = Instant.now(),
            )
            val mediaPaths: List<String> = allItems
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
            refreshSelectedItem(itemId)
            syncReminderSchedule()
            _encryptionProgress.value = null
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            syncReminderSchedule()
        }
    }

    fun updateCompletionRecord(itemId: Long, record: CompletionRecord) {
        viewModelScope.launch { repository.updateCompletionRecord(itemId, record) }
    }

    fun deleteCompletionRecord(itemId: Long, occurrenceDate: LocalDate, completedAt: LocalDateTime) {
        viewModelScope.launch { repository.deleteCompletionRecord(itemId, occurrenceDate, completedAt) }
    }

    private suspend fun syncReminderSchedule() {
        val items = repository.getAllItems()
        val settings = state.value.notificationSettings
        reminderScheduler.sync(items = items, settings = settings)
        geofenceManager.syncGeofences(items)
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
        viewModelScope.launch { repository.toggleArchive(itemId) }
    }

    fun toggleShowArchived() {
        repository.toggleShowArchived()
    }

    private suspend fun refreshSelectedItem(itemId: Long) {
        _selectedItem.value = repository.getItem(itemId)
    }

    fun fetchModelCatalog() {
        viewModelScope.launch { modelManager.fetchCatalog() }
    }

    fun generateSmartTitle(body: String) {
        aiJob?.cancel()
        _aiSmartTitle.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.generateSmartTitle(body).collect { _aiSmartTitle.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun suggestTags(title: String, body: String) {
        aiJob?.cancel()
        _aiTagSuggestions.value = emptyList()
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.suggestTags(title, body).collect { _aiTagSuggestions.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun summarizeEntry(title: String, body: String) {
        aiJob?.cancel()
        _aiSummary.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.summarizeEntry(title, body).collect { _aiSummary.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun analyzeMood(title: String, body: String) {
        aiJob?.cancel()
        _aiMood.value = null
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.analyzeMood(title, body).collect { _aiMood.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun rewriteText(text: String, tone: WritingTone) {
        aiJob?.cancel()
        _aiRewrittenText.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.rewriteText(text, tone).collect { _aiRewrittenText.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun describePhoto(imageBytes: ByteArray) {
        aiJob?.cancel()
        _aiPhotoDescription.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.describePhoto(imageBytes).collect { _aiPhotoDescription.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun summarizeAudio(audioBytes: ByteArray) {
        aiJob?.cancel()
        _aiAudioSummary.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                aiExecutor.summarizeAudio(audioBytes).collect { _aiAudioSummary.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun naturalLanguageSearch(query: String) {
        aiJob?.cancel()
        _aiSearchFilters.value = ""
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                val tags = allTags.firstOrNull() ?: emptyList()
                aiExecutor.naturalLanguageSearch(query, tags).collect { _aiSearchFilters.value = it }
            } catch (_: Exception) {
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun cancelAiGeneration() {
        aiJob?.cancel()
        engineService.cancelGeneration()
        _isAiGenerating.value = false
    }

    fun clearAiState() {
        _aiSmartTitle.value = ""
        _aiTagSuggestions.value = emptyList()
        _aiSummary.value = ""
        _aiRewrittenText.value = ""
        _aiMood.value = null
        _aiPhotoDescription.value = ""
        _aiAudioSummary.value = ""
        _aiSearchFilters.value = ""
    }
}
