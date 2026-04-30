package com.raulshma.dailylife.domain

enum class EnrichmentFeature {
    SMART_TITLE,
    TAGS,
    DESCRIPTION,
    PHOTO_DESCRIPTION,
    AUDIO_SUMMARY,
}

fun EnrichmentFeature.requiredCapabilities(): Set<com.raulshma.dailylife.domain.AIModelCapability> = when (this) {
    EnrichmentFeature.SMART_TITLE, EnrichmentFeature.TAGS, EnrichmentFeature.DESCRIPTION ->
        setOf(com.raulshma.dailylife.domain.AIModelCapability.TEXT_GENERATION)
    EnrichmentFeature.PHOTO_DESCRIPTION ->
        setOf(com.raulshma.dailylife.domain.AIModelCapability.VISION)
    EnrichmentFeature.AUDIO_SUMMARY ->
        setOf(com.raulshma.dailylife.domain.AIModelCapability.AUDIO)
}

data class EnrichmentSettings(
    val enabled: Boolean = false,
    val features: Set<EnrichmentFeature> = setOf(EnrichmentFeature.SMART_TITLE, EnrichmentFeature.TAGS),
    val eligibleTypes: Set<LifeItemType> = setOf(
        LifeItemType.Thought, LifeItemType.Note, LifeItemType.Task, LifeItemType.Reminder,
    ),
    val skipArchived: Boolean = true,
    val processingDelayMs: Long = 500L,
)

enum class EnrichmentProcessorStatus {
    IDLE,
    PROCESSING,
    PAUSED,
    DONE,
    CANCELLED,
    ERROR,
}

data class EnrichmentProgress(
    val status: EnrichmentProcessorStatus = EnrichmentProcessorStatus.IDLE,
    val totalItems: Int = 0,
    val processedItems: Int = 0,
    val currentItemId: Long? = null,
    val currentFeature: EnrichmentFeature? = null,
    val failedItems: Int = 0,
    val startTimeMs: Long? = null,
    val endTimeMs: Long? = null,
) {
    val isActive: Boolean get() = status == EnrichmentProcessorStatus.PROCESSING || status == EnrichmentProcessorStatus.PAUSED
    val progressFraction: Float get() = if (totalItems > 0) processedItems.toFloat() / totalItems else 0f
}

enum class EnrichmentTaskStatus {
    COMPLETED,
    FAILED,
    SKIPPED,
}

data class EnrichmentTask(
    val id: Long = 0,
    val itemId: Long,
    val feature: EnrichmentFeature,
    val status: EnrichmentTaskStatus,
    val modelId: String? = null,
    val processingTimeMs: Long? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)
