package com.raulshma.dailylife.data.ai

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.media.UriFileResolver
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.EnrichmentFeature
import com.raulshma.dailylife.domain.EnrichmentProgress
import com.raulshma.dailylife.domain.EnrichmentProcessorStatus
import com.raulshma.dailylife.domain.EnrichmentTask
import com.raulshma.dailylife.domain.EnrichmentTaskStatus
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.requiredCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEnrichmentProcessor @Inject constructor(
    private val repository: DailyLifeRepository,
    private val featureExecutor: AIFeatureExecutor,
    private val modelManager: ModelManager,
    private val chatRepository: AIChatRepository,
    private val encryptionManager: MediaEncryptionManager,
    @ApplicationContext private val appContext: Context,
) {
    companion object {
        private const val PREFS_NAME = "ai_enrichment_prefs"
        private const val KEY_ENABLED = "enrichment_enabled"
        private const val KEY_FEATURES = "enrichment_features"
        private const val KEY_ELIGIBLE_TYPES = "enrichment_eligible_types"
        private const val KEY_SKIP_ARCHIVED = "enrichment_skip_archived"
        private const val KEY_DELAY_MS = "enrichment_delay_ms"
    }

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val processorJob = AtomicReference<Job?>(null)
    private var paused = false

    private val _progress = MutableStateFlow(EnrichmentProgress())
    val progress: StateFlow<EnrichmentProgress> = _progress.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<com.raulshma.dailylife.domain.EnrichmentSettings> = _settings.asStateFlow()

    private fun loadSettings(): com.raulshma.dailylife.domain.EnrichmentSettings {
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        val featuresCsv = prefs.getString(KEY_FEATURES, null)
        val typesCsv = prefs.getString(KEY_ELIGIBLE_TYPES, null)
        val skipArchived = prefs.getBoolean(KEY_SKIP_ARCHIVED, true)
        val delayMs = prefs.getLong(KEY_DELAY_MS, 500L)

        val features = featuresCsv?.split(",")
            ?.mapNotNull { runCatching { EnrichmentFeature.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?: setOf(EnrichmentFeature.SMART_TITLE, EnrichmentFeature.TAGS)

        val types = typesCsv?.split(",")
            ?.mapNotNull { runCatching { com.raulshma.dailylife.domain.LifeItemType.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?: com.raulshma.dailylife.domain.LifeItemType.entries.toSet()

        return com.raulshma.dailylife.domain.EnrichmentSettings(
            enabled = enabled,
            features = features,
            eligibleTypes = types,
            skipArchived = skipArchived,
            processingDelayMs = delayMs,
        )
    }

    fun updateSettings(settings: com.raulshma.dailylife.domain.EnrichmentSettings) {
        prefs.edit().apply {
            putBoolean(KEY_ENABLED, settings.enabled)
            putString(KEY_FEATURES, settings.features.joinToString(",") { it.name })
            putString(KEY_ELIGIBLE_TYPES, settings.eligibleTypes.joinToString(",") { it.name })
            putBoolean(KEY_SKIP_ARCHIVED, settings.skipArchived)
            putLong(KEY_DELAY_MS, settings.processingDelayMs)
            apply()
        }
        _settings.value = settings
    }

    fun isAutoEnrichEnabled(): Boolean = _settings.value.enabled && _settings.value.features.isNotEmpty()

    fun startBatch(itemIds: List<Long>? = null) {
        val currentJob = processorJob.get()
        if (currentJob?.isActive == true) return

        val currentSettings = _settings.value
        if (currentSettings.features.isEmpty()) return
        if (!modelManager.isAiEnabled()) return

        paused = false
        processorJob.set(
            scope.launch {
                try {
                    _progress.value = EnrichmentProgress(
                        status = EnrichmentProcessorStatus.PROCESSING,
                        startTimeMs = System.currentTimeMillis(),
                    )

                    val ids = itemIds ?: repository.getUnenrichedItemIds(
                        features = currentSettings.features,
                        types = currentSettings.eligibleTypes,
                        includeArchived = !currentSettings.skipArchived,
                    )

                    _progress.value = _progress.value.copy(totalItems = ids.size)

                    for (itemId in ids) {
                        if (!isActive) break
                        while (paused) {
                            delay(200)
                            if (!isActive) break
                        }
                        if (!isActive) break

                        if (!modelManager.isAiEnabled()) {
                            _progress.value = _progress.value.copy(
                                status = EnrichmentProcessorStatus.CANCELLED,
                                endTimeMs = System.currentTimeMillis(),
                            )
                            return@launch
                        }

                        _progress.value = _progress.value.copy(currentItemId = itemId)
                        processItem(itemId, currentSettings)
                        _progress.value = _progress.value.copy(
                            processedItems = _progress.value.processedItems + 1,
                        )

                        delay(currentSettings.processingDelayMs)
                    }

                    if (isActive) {
                        _progress.value = _progress.value.copy(
                            status = EnrichmentProcessorStatus.DONE,
                            currentItemId = null,
                            currentFeature = null,
                            endTimeMs = System.currentTimeMillis(),
                        )
                    }
                } catch (e: CancellationException) {
                    _progress.value = _progress.value.copy(
                        status = EnrichmentProcessorStatus.CANCELLED,
                        currentItemId = null,
                        currentFeature = null,
                        endTimeMs = System.currentTimeMillis(),
                    )
                } catch (e: Exception) {
                    _progress.value = _progress.value.copy(
                        status = EnrichmentProcessorStatus.ERROR,
                        currentItemId = null,
                        currentFeature = null,
                        endTimeMs = System.currentTimeMillis(),
                    )
                }
            },
        )
    }

    fun pause() {
        paused = true
        _progress.value = _progress.value.copy(status = EnrichmentProcessorStatus.PAUSED)
    }

    fun resume() {
        paused = false
        _progress.value = _progress.value.copy(status = EnrichmentProcessorStatus.PROCESSING)
    }

    fun cancel() {
        paused = false
        processorJob.getAndSet(null)?.cancel()
        _progress.value = EnrichmentProgress(
            status = EnrichmentProcessorStatus.CANCELLED,
            processedItems = _progress.value.processedItems,
            totalItems = _progress.value.totalItems,
            failedItems = _progress.value.failedItems,
            endTimeMs = System.currentTimeMillis(),
        )
    }

    fun enrichSingleItem(itemId: Long) {
        if (!isAutoEnrichEnabled()) return
        if (!modelManager.isAiEnabled()) return

        scope.launch {
            try {
                processItem(itemId, _settings.value)
            } catch (_: CancellationException) {
            } catch (_: Exception) {
            }
        }
    }

    fun resetProgress() {
        _progress.value = EnrichmentProgress()
    }

    private suspend fun processItem(
        itemId: Long,
        settings: com.raulshma.dailylife.domain.EnrichmentSettings,
    ) {
        val item = repository.getItem(itemId) ?: return

        val model = modelManager.getDefaultModel()
        if (model == null) {
            recordTask(itemId, EnrichmentFeature.SMART_TITLE, EnrichmentTaskStatus.SKIPPED, null, "No model")
            return
        }

        for (feature in settings.features) {
            if (!scope.isActive) break
            if (!shouldEnrichFeature(item, feature, model)) {
                recordTask(itemId, feature, EnrichmentTaskStatus.SKIPPED, null, null)
                continue
            }

            _progress.value = _progress.value.copy(currentFeature = feature)

            val startTime = System.currentTimeMillis()
            try {
                val result = when (feature) {
                    EnrichmentFeature.SMART_TITLE -> enrichTitle(item)
                    EnrichmentFeature.TAGS -> enrichTags(item)
                    EnrichmentFeature.DESCRIPTION -> enrichDescription(item)
                    EnrichmentFeature.PHOTO_DESCRIPTION -> enrichPhoto(item)
                    EnrichmentFeature.AUDIO_SUMMARY -> enrichAudio(item)
                }
                val processingTime = System.currentTimeMillis() - startTime
                if (result) {
                    recordTask(itemId, feature, EnrichmentTaskStatus.COMPLETED, processingTime, null)
                } else {
                    recordTask(itemId, feature, EnrichmentTaskStatus.SKIPPED, null, null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val processingTime = System.currentTimeMillis() - startTime
                recordTask(itemId, feature, EnrichmentTaskStatus.FAILED, processingTime, e.message)
                _progress.value = _progress.value.copy(
                    failedItems = _progress.value.failedItems + 1,
                )
            }
        }
    }

    private fun shouldEnrichFeature(
        item: LifeItem,
        feature: EnrichmentFeature,
        model: AIModel,
    ): Boolean {
        if (!feature.requiredCapabilities().all { it in model.capabilities }) return false

        return when (feature) {
            EnrichmentFeature.SMART_TITLE -> {
                item.displayBody().isNotBlank() && item.title == item.type.label
            }
            EnrichmentFeature.TAGS -> {
                item.displayBody().isNotBlank() && item.tags.isEmpty()
            }
            EnrichmentFeature.DESCRIPTION -> {
                item.displayBody().isNotBlank() && item.aiSummary.isNullOrBlank()
            }
            EnrichmentFeature.PHOTO_DESCRIPTION -> {
                val uri = item.inferImagePreviewUrl()
                uri != null && item.aiSummary.isNullOrBlank()
            }
            EnrichmentFeature.AUDIO_SUMMARY -> {
                val uri = item.inferAudioUrl()
                uri != null && item.aiSummary.isNullOrBlank()
            }
        }
    }

    private suspend fun enrichTitle(item: LifeItem): Boolean {
        val body = item.displayBody()
        val imageUri = item.inferImagePreviewUrl()
        val audioUri = item.inferAudioUrl()
        val imageBytes = if (imageUri != null) readMediaBytes(imageUri) else null
        val audioBytes = if (audioUri != null) readMediaBytes(audioUri) else null
        val fullTitle = StringBuilder()
        featureExecutor.generateSmartTitle(body, imageBytes, audioBytes).collect { fullTitle.append(it) }
        val title = fullTitle.toString().trim()
        if (title.isBlank() || title == item.title) return false
        repository.updateItemTitle(item.id, title)
        return true
    }

    private suspend fun enrichTags(item: LifeItem): Boolean {
        val body = item.displayBody()
        var tags: List<String>? = null
        featureExecutor.suggestTags(item.title, body).collect { tags = it }
        val newTags = tags ?: return false
        if (newTags.isEmpty()) return false
        repository.updateItemTags(item.id, newTags.toSet())
        return true
    }

    private suspend fun enrichDescription(item: LifeItem): Boolean {
        val body = item.displayBody()
        val fullSummary = StringBuilder()
        featureExecutor.summarizeEntry(item.title, body).collect { fullSummary.append(it) }
        val summary = fullSummary.toString().trim()
        if (summary.isBlank()) return false
        repository.updateItemAiSummary(item.id, summary)
        return true
    }

    private suspend fun enrichPhoto(item: LifeItem): Boolean {
        val uriString = item.inferImagePreviewUrl() ?: return false
        val imageBytes = readMediaBytes(uriString) ?: return false
        val fullDescription = StringBuilder()
        featureExecutor.describePhoto(imageBytes).collect { fullDescription.append(it) }
        val description = fullDescription.toString().trim()
        if (description.isBlank()) return false
        repository.updateItemAiSummary(item.id, description)
        return true
    }

    private suspend fun enrichAudio(item: LifeItem): Boolean {
        val uriString = item.inferAudioUrl() ?: return false
        val fullSummary = StringBuilder()
        val audioFilePath = resolveMediaFilePath(uriString)
        if (audioFilePath != null) {
            featureExecutor.summarizeAudioFile(audioFilePath).collect { fullSummary.append(it) }
        } else {
            val audioBytes = readMediaBytes(uriString) ?: return false
            featureExecutor.summarizeAudio(audioBytes).collect { fullSummary.append(it) }
        }
        val summary = fullSummary.toString().trim()
        if (summary.isBlank()) return false
        repository.updateItemAiSummary(item.id, summary)
        return true
    }

    private suspend fun resolveMediaFilePath(uriString: String): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val decryptedUri = encryptionManager.decryptToCache(Uri.parse(uriString), appContext)
                    ?: Uri.parse(uriString)
                UriFileResolver.resolveToFile(decryptedUri, appContext)
                    ?.takeIf { it.exists() && it.length() > 44 }
                    ?.absolutePath
            }.getOrNull()
        }
    }

    private suspend fun readMediaBytes(uriString: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val decryptedUri = encryptionManager.decryptToCache(Uri.parse(uriString), appContext)
                    ?: Uri.parse(uriString)
                val file = decryptedUri.path?.let { java.io.File(it) }
                    ?: return@withContext null
                file.readBytes()
            }.getOrNull()
        }
    }

    private suspend fun recordTask(
        itemId: Long,
        feature: EnrichmentFeature,
        status: EnrichmentTaskStatus,
        processingTimeMs: Long?,
        errorMessage: String?,
    ) {
        val now = System.currentTimeMillis()
        val task = EnrichmentTask(
            itemId = itemId,
            feature = feature,
            status = status,
            modelId = modelManager.getDefaultModel()?.id,
            processingTimeMs = processingTimeMs,
            errorMessage = errorMessage,
            createdAt = now,
            completedAt = if (status != EnrichmentTaskStatus.SKIPPED) now else null,
        )
        repository.recordEnrichmentTask(task)
    }
}
