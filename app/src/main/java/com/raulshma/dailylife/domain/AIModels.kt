package com.raulshma.dailylife.domain

enum class AIModelCapability {
    TEXT_GENERATION,
    TOOL_CALLING,
    VISION,
    AUDIO,
}

data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val downloadUrl: String,
    val fileSizeBytes: Long,
    val capabilities: Set<AIModelCapability>,
    val ramRequiredMb: Int,
    val isMultimodal: Boolean,
)

sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progress: Float) : ModelDownloadState()
    data object Downloaded : ModelDownloadState()
    data class DownloadFailed(val error: String) : ModelDownloadState()
}

enum class AIFeature {
    SMART_TITLE,
    TAG_SUGGESTION,
    SUMMARIZE,
    REFLECTION,
    MOOD_ANALYSIS,
    NL_SEARCH,
    PHOTO_DESCRIPTION,
    AUDIO_SUMMARY,
    CHAT,
    WRITING_ASSISTANT,
}

enum class WritingTone(val label: String) {
    FORMAL("Formal"),
    CASUAL("Casual"),
    CONCISE("Concise"),
    CREATIVE("Creative"),
    FIX_GRAMMAR("Fix grammar"),
}

data class MoodResult(
    val moodLabel: String,
    val confidence: Float,
)

data class ChatMessage(
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class ChatRole {
    USER,
    ASSISTANT,
}

fun AIFeature.requiredCapabilities(): Set<AIModelCapability> = when (this) {
    AIFeature.SMART_TITLE -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.TAG_SUGGESTION -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.SUMMARIZE -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.REFLECTION -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.MOOD_ANALYSIS -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.NL_SEARCH -> setOf(AIModelCapability.TEXT_GENERATION, AIModelCapability.TOOL_CALLING)
    AIFeature.PHOTO_DESCRIPTION -> setOf(AIModelCapability.VISION)
    AIFeature.AUDIO_SUMMARY -> setOf(AIModelCapability.AUDIO)
    AIFeature.CHAT -> setOf(AIModelCapability.TEXT_GENERATION)
    AIFeature.WRITING_ASSISTANT -> setOf(AIModelCapability.TEXT_GENERATION)
}

fun AIModel.supports(feature: AIFeature): Boolean {
    return feature.requiredCapabilities().all { it in capabilities }
}
