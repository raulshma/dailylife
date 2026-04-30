package com.raulshma.dailylife.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.AIEnrichmentProcessor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.data.ai.ModelManager
import com.raulshma.dailylife.data.backup.S3BackupRepository
import com.raulshma.dailylife.data.media.UriFileResolver
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.security.EncryptionProgress
import com.raulshma.dailylife.data.security.MediaDecryptCoordinator
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.EnrichmentSettings
import com.raulshma.dailylife.domain.EnrichmentTask
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
import com.raulshma.dailylife.domain.AIFeature
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
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
    val aiChatRepository: com.raulshma.dailylife.data.ai.AIChatRepository,
    val enrichmentProcessor: AIEnrichmentProcessor,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    companion object {
        private const val TAG = "DailyLifeViewModel"
    }
    val state = repository.state
    val pagingItems = repository.pagingItems.cachedIn(viewModelScope)
    val allTags = repository.allTags
    val snapshotStats = repository.snapshotStats
    val collectionCounts = repository.collectionCounts
    val taggedItemsForGraph = repository.taggedItemsForGraph
    val allItemIds = repository.allItemIds

    private val _recentEntries = MutableStateFlow<List<LifeItem>>(emptyList())
    val recentEntries: StateFlow<List<LifeItem>> = _recentEntries.asStateFlow()

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

    init {
        viewModelScope.launch {
            _recentEntries.value = repository.getAllItems()
        }
    }

    private fun refreshRecentEntries() {
        viewModelScope.launch {
            _recentEntries.value = repository.getAllItems()
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        modelManager.setAiEnabled(enabled)
    }

    fun unloadModel() {
        viewModelScope.launch {
            engineService.unloadModel()
        }
    }

    private val _aiSmartTitle = MutableStateFlow("")
    val aiSmartTitle = _aiSmartTitle.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    fun clearAiError() {
        _aiError.value = null
    }

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

    private val _aiInferredType = MutableStateFlow<LifeItemType?>(null)
    val aiInferredType = _aiInferredType.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    private var aiJob: Job? = null
    private var aiTypeInferenceJob: Job? = null
    private var aiGeneration = 0

    private inline fun startAiJob(
        crossinline block: suspend () -> Unit,
    ) {
        aiJob?.cancel()
        aiGeneration++
        val myGeneration = aiGeneration
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.w(TAG, "AI operation failed", e)
                    _aiError.value = e.message
                }
            } finally {
                if (aiGeneration == myGeneration) {
                    _isAiGenerating.value = false
                }
            }
        }
    }

    init {
        viewModelScope.launch { repository.rolloverMissedOccurrences() }
        viewModelScope.launch { syncReminderSchedule() }
        viewModelScope.launch { _s3BackupSettings.value = roomStore.loadS3BackupSettings() }
    }

    fun selectItem(itemId: Long, preload: LifeItem? = null) {
        if (_selectedItem.value?.id != itemId) {
            cancelAiGeneration()
            clearAiState()
        }
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
            val item = repository.addItem(encryptedDraft)
            syncReminderSchedule()
            refreshRecentEntries()
            _encryptionProgress.value = null
            if (enrichmentProcessor.isAutoEnrichEnabled()) {
                enrichmentProcessor.enrichSingleItem(item.id)
            }
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
            refreshRecentEntries()
            _encryptionProgress.value = null
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            syncReminderSchedule()
            refreshRecentEntries()
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
                refreshRecentEntries()
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
                refreshRecentEntries()
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
        viewModelScope.launch {
            repository.toggleArchive(itemId)
            refreshRecentEntries()
        }
    }

    fun toggleShowArchived() {
        repository.toggleShowArchived()
    }

    private suspend fun refreshSelectedItem(itemId: Long) {
        _selectedItem.value = repository.getItem(itemId)
    }

    fun isFeatureAvailable(feature: AIFeature): Boolean {
        return aiExecutor.isFeatureAvailable(feature)
    }

    fun applyAiTitle(itemId: Long, title: String) {
        viewModelScope.launch {
            repository.updateItemTitle(itemId, title)
            refreshSelectedItem(itemId)
            _aiSmartTitle.value = ""
        }
    }

    fun applyAiTags(itemId: Long, tags: Set<String>) {
        viewModelScope.launch {
            repository.updateItemTags(itemId, tags)
            refreshSelectedItem(itemId)
            _aiTagSuggestions.value = emptyList()
        }
    }

    fun applyAiSummary(itemId: Long, summary: String) {
        viewModelScope.launch {
            repository.updateItemAiSummary(itemId, summary)
            refreshSelectedItem(itemId)
            _aiSummary.value = ""
            _aiPhotoDescription.value = ""
            _aiAudioSummary.value = ""
        }
    }

    fun fetchModelCatalog() {
        viewModelScope.launch { modelManager.fetchCatalog() }
    }

    fun generateSmartTitle(
        body: String,
        imageUri: String? = null,
        audioUri: String? = null,
    ) {
        _aiSmartTitle.value = ""
        _aiError.value = null
        startAiJob {
            val imageBytes = imageUri?.let { readImageBytesForAi(it) }
            if (imageUri != null && imageBytes == null) {
                _aiError.value = "Failed to read image"
                return@startAiJob
            }

            val audioBytes = when {
                audioUri == null -> null
                else -> {
                    val audioFilePath = resolveMediaFilePath(audioUri)
                    when {
                        audioFilePath != null -> File(audioFilePath).takeIf { it.exists() }?.readBytes()
                        else -> readMediaBytes(audioUri)
                    }
                }
            }
            if (audioUri != null && audioBytes == null) {
                _aiError.value = "Failed to read audio"
                return@startAiJob
            }

            aiExecutor.generateSmartTitle(body, imageBytes, audioBytes).collect { _aiSmartTitle.value = it }
        }
    }

    fun suggestTags(title: String, body: String) {
        _aiTagSuggestions.value = emptyList()
        _aiError.value = null
        startAiJob {
            aiExecutor.suggestTags(title, body).collect { _aiTagSuggestions.value = it }
        }
    }

    fun summarizeEntry(title: String, body: String) {
        _aiSummary.value = ""
        _aiError.value = null
        startAiJob {
            aiExecutor.summarizeEntry(title, body).collect { _aiSummary.value = it }
        }
    }

    fun analyzeMood(title: String, body: String) {
        _aiMood.value = null
        _aiError.value = null
        startAiJob {
            aiExecutor.analyzeMood(title, body).collect { _aiMood.value = it }
        }
    }

    fun rewriteText(text: String, tone: WritingTone) {
        _aiRewrittenText.value = ""
        _aiError.value = null
        startAiJob {
            aiExecutor.rewriteText(text, tone).collect { _aiRewrittenText.value = it }
        }
    }

    fun describePhoto(imageBytes: ByteArray) {
        _aiPhotoDescription.value = ""
        _aiError.value = null
        startAiJob {
            aiExecutor.describePhoto(imageBytes).collect { _aiPhotoDescription.value = it }
        }
    }

    fun summarizeAudio(audioBytes: ByteArray) {
        _aiAudioSummary.value = ""
        _aiError.value = null
        startAiJob {
            aiExecutor.summarizeAudio(audioBytes).collect { _aiAudioSummary.value = it }
        }
    }

    fun describePhotoFromUri(uriString: String) {
        _aiPhotoDescription.value = ""
        _aiError.value = null
        startAiJob {
            val bytes = readImageBytesForAi(uriString)
            if (bytes != null) {
                aiExecutor.describePhoto(bytes).collect { _aiPhotoDescription.value = it }
            } else {
                _aiError.value = "Failed to read image"
            }
        }
    }

    fun summarizeAudioFromUri(uriString: String) {
        _aiAudioSummary.value = ""
        _aiError.value = null
        startAiJob {
            val audioFilePath = resolveMediaFilePath(uriString)
            if (audioFilePath != null) {
                aiExecutor.summarizeAudioFile(audioFilePath).collect { _aiAudioSummary.value = it }
            } else {
                val bytes = readMediaBytes(uriString)
                if (bytes != null) {
                    aiExecutor.summarizeAudio(bytes).collect { _aiAudioSummary.value = it }
                } else {
                    _aiError.value = "Failed to read audio"
                }
            }
        }
    }

    private val maxAiImageDimension = 384
    private val maxAiImageBytes = 600_000

    private data class ImageBytes(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
    )

    private suspend fun readImageBytesForAi(uriString: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(uriString)
                val resolvedUri = mediaEncryptionManager.decryptToCache(uri, context) ?: uri
                val rawBytes = context.contentResolver.openInputStream(resolvedUri)?.use { it.readBytes() }
                    ?: return@withContext null
                val bitmap = decodeBitmapForAi(rawBytes, maxAiImageDimension) ?: return@withContext null
                val imageBytes = bitmap.toPngByteArrayWithLimit(maxAiImageDimension, maxAiImageBytes)
                Log.i(
                    TAG,
                    "Image for AI: ${bitmap.width}x${bitmap.height} -> ${imageBytes.width}x${imageBytes.height}, ${imageBytes.bytes.size} bytes",
                )
                bitmap.recycle()
                imageBytes.bytes
            }.getOrNull()
        }
    }

    private fun decodeBitmapForAi(rawBytes: ByteArray, maxDimension: Int): Bitmap? {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeBitmapWithImageDecoder(rawBytes, maxDimension) ?: decodeBitmapWithBitmapFactory(rawBytes, maxDimension)
        } else {
            decodeBitmapWithBitmapFactory(rawBytes, maxDimension)
        }
        return bitmap?.asArgb8888()
    }

    private fun decodeBitmapWithImageDecoder(rawBytes: ByteArray, maxDimension: Int): Bitmap? {
        return try {
            val source = android.graphics.ImageDecoder.createSource(java.nio.ByteBuffer.wrap(rawBytes))
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
                decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
                decoder.setMemorySizePolicy(android.graphics.ImageDecoder.MEMORY_POLICY_LOW_RAM)
                val size = info.size
                val scale = maxDimension.toFloat() / maxOf(size.width, size.height)
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (size.width * scale).toInt().coerceAtLeast(1),
                        (size.height * scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "ImageDecoder decode failed", e)
            null
        }
    }

    private fun decodeBitmapWithBitmapFactory(rawBytes: ByteArray, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
    }

    private fun Bitmap.asArgb8888(): Bitmap {
        if (config == Bitmap.Config.ARGB_8888) return this
        val normalized = copy(Bitmap.Config.ARGB_8888, false)
        if (normalized != null) recycle()
        return normalized ?: this
    }

    private fun Bitmap.toPngByteArrayWithLimit(maxDimension: Int, maxBytes: Int): ImageBytes {
        val needsResize = width > maxDimension || height > maxDimension
        var current = if (needsResize) {
            val scale = maxDimension.toFloat() / maxOf(width, height)
            val targetW = (width * scale).toInt().coerceAtLeast(1)
            val targetH = (height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(this, targetW, targetH, true)
        } else {
            this
        }
        var currentW = current.width
        var currentH = current.height
        while (true) {
            val bytes = current.compressToPng()
            if (bytes.size <= maxBytes || (currentW <= 128 && currentH <= 128)) {
                if (current !== this) current.recycle()
                return ImageBytes(bytes, currentW, currentH)
            }
            val scale = kotlin.math.sqrt(maxBytes.toFloat() / bytes.size.toFloat())
                .coerceIn(0.5f, 0.85f)
            val nextW = (currentW * scale).toInt().coerceAtLeast(1)
            val nextH = (currentH * scale).toInt().coerceAtLeast(1)
            val next = Bitmap.createScaledBitmap(current, nextW, nextH, true)
            if (current !== this) current.recycle()
            current = next
            currentW = nextW
            currentH = nextH
        }
    }

    private fun Bitmap.compressToPng(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqSize: Int): Int {
        var inSampleSize = 1
        while (width / inSampleSize > reqSize || height / inSampleSize > reqSize) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private suspend fun readMediaBytes(uriString: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(uriString)
                val resolvedUri = mediaEncryptionManager.decryptToCache(uri, context) ?: uri
                context.contentResolver.openInputStream(resolvedUri)?.use { it.readBytes() }
            }.getOrNull()
        }
    }

    private suspend fun resolveMediaFilePath(uriString: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(uriString)
                val resolvedUri = mediaEncryptionManager.decryptToCache(uri, context) ?: uri
                UriFileResolver.resolveToFile(resolvedUri, context)
                    ?.takeIf { it.exists() && it.length() > 44 }
                    ?.absolutePath
            }.getOrNull()
        }
    }

    fun naturalLanguageSearch(query: String) {
        aiJob?.cancel()
        _aiSearchFilters.value = ""
        _aiError.value = null
        _isAiGenerating.value = true
        aiJob = viewModelScope.launch {
            try {
                val tags = allTags.firstOrNull() ?: emptyList()
                aiExecutor.naturalLanguageSearch(query, tags).collect { _aiSearchFilters.value = it }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.w(TAG, "naturalLanguageSearch failed", e)
                    _aiError.value = e.message
                }
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun cancelAiGeneration() {
        aiJob?.cancel()
        aiTypeInferenceJob?.cancel()
        engineService.cancelGeneration()
        aiGeneration++
        _isAiGenerating.value = false
    }

    fun inferTypeWithAI(title: String, body: String) {
        aiTypeInferenceJob?.cancel()
        engineService.cancelGeneration()
        _aiInferredType.value = null
        aiTypeInferenceJob = viewModelScope.launch {
            try {
                aiExecutor.inferType(title, body).collect { _aiInferredType.value = it }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.w(TAG, "inferTypeWithAI failed", e)
                }
            }
        }
    }

    val enrichmentProgress = enrichmentProcessor.progress
    val enrichmentSettings = enrichmentProcessor.settings

    fun updateEnrichmentSettings(settings: EnrichmentSettings) {
        enrichmentProcessor.updateSettings(settings)
    }

    fun startEnrichmentBatch() {
        enrichmentProcessor.startBatch()
    }

    fun pauseEnrichment() {
        enrichmentProcessor.pause()
    }

    fun resumeEnrichment() {
        enrichmentProcessor.resume()
    }

    fun cancelEnrichment() {
        enrichmentProcessor.cancel()
    }

    fun resetEnrichmentProgress() {
        enrichmentProcessor.resetProgress()
    }

    private var enrichmentHistoryJob: Job? = null
    private val _enrichmentHistory = MutableStateFlow<List<EnrichmentTask>>(emptyList())
    val enrichmentHistory: StateFlow<List<EnrichmentTask>> = _enrichmentHistory.asStateFlow()

    fun loadEnrichmentHistory() {
        enrichmentHistoryJob?.cancel()
        enrichmentHistoryJob = viewModelScope.launch {
            _enrichmentHistory.value = repository.getRecentEnrichmentHistory()
        }
    }

    fun clearEnrichmentHistory() {
        viewModelScope.launch {
            repository.clearEnrichmentHistory()
            _enrichmentHistory.value = emptyList()
        }
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
        _aiInferredType.value = null
    }
}
