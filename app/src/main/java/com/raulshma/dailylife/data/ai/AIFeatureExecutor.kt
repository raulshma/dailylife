package com.raulshma.dailylife.data.ai

import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.AIFeature
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.MoodResult
import com.raulshma.dailylife.domain.WritingTone
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.supports
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIFeatureExecutor @Inject constructor(
    private val engineService: LiteRTEngineService,
    private val modelManager: ModelManager,
    private val chatRepository: AIChatRepository,
) {
    suspend fun ensureModelForFeature(feature: AIFeature): AIModel? {
        val model = modelManager.getDefaultModel() ?: return null
        if (!model.supports(feature)) return null
        if (engineService.loadedModelId != model.id) {
            engineService.loadModel(model).getOrNull() ?: return null
        }
        return model
    }

    fun isFeatureAvailable(feature: AIFeature): Boolean {
        val model = modelManager.getDefaultModel() ?: return false
        return model.supports(feature)
    }

    private fun <T> Flow<T>.withMetrics(
        feature: AIFeature,
        inputCharCount: Int,
        conversationId: Long? = null,
    ): Flow<T> {
        val featureName = feature.name
        val modelId = engineService.loadedModelId
        var startTime = 0L
        var firstTokenTime: Long? = null
        var isFirstEmission = true
        var outputLength = 0
        var hadError = false
        var errorMsg: String? = null

        return this
            .onStart { startTime = System.currentTimeMillis() }
            .map { value ->
                if (isFirstEmission) {
                    firstTokenTime = System.currentTimeMillis()
                    isFirstEmission = false
                }
                if (value is String) outputLength = value.length
                value
            }
            .onCompletion { throwable ->
                val totalTime = System.currentTimeMillis() - startTime
                val ttft = firstTokenTime?.let { it - startTime }
                if (throwable != null) {
                    hadError = true
                    errorMsg = throwable.message
                }
                chatRepository.recordMetrics(
                    feature = featureName,
                    modelId = modelId,
                    timeToFirstTokenMs = ttft,
                    totalGenerationMs = totalTime,
                    inputCharCount = inputCharCount,
                    outputCharCount = outputLength,
                    isError = hadError,
                    errorMessage = errorMsg,
                    conversationId = conversationId,
                )
            }
    }

    suspend fun generateSmartTitle(body: String): Flow<String> {
        val model = ensureModelForFeature(AIFeature.SMART_TITLE)
            ?: return flowOf("")
        val prompt = """Generate a short, concise title (max 8 words) for a journal entry with the following content. Return ONLY the title, nothing else.

Content:
${body.take(1000)}"""
        return engineService.generateText(prompt)
            .withMetrics(AIFeature.SMART_TITLE, prompt.length)
    }

    suspend fun suggestTags(title: String, body: String): Flow<List<String>> {
        val model = ensureModelForFeature(AIFeature.TAG_SUGGESTION)
            ?: return flowOf(emptyList())
        val prompt = """Suggest up to 5 relevant tags for this journal entry. Return ONLY comma-separated tags, nothing else. Use lowercase, no hashtags.

Title: ${title.take(200)}
Content: ${body.take(1000)}"""
        return engineService.generateText(prompt).map { response ->
            response.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() && it.length <= 30 }
                .distinct()
                .take(5)
        }.withMetrics(AIFeature.TAG_SUGGESTION, prompt.length)
    }

    suspend fun summarizeEntry(title: String, body: String): Flow<String> {
        val model = ensureModelForFeature(AIFeature.SUMMARIZE)
            ?: return flowOf("")
        val prompt = """Summarize this journal entry in 2-3 concise sentences. Focus on the key points and emotions expressed.

Title: ${title.take(200)}
Content: ${body.take(2000)}"""
        return engineService.generateText(prompt)
            .withMetrics(AIFeature.SUMMARIZE, prompt.length)
    }

    suspend fun analyzeMood(title: String, body: String): Flow<MoodResult> {
        val model = ensureModelForFeature(AIFeature.MOOD_ANALYSIS)
            ?: return flowOf(MoodResult(moodLabel = "neutral", confidence = 0f))
        val prompt = """Analyze the emotional tone of this journal entry. Respond with EXACTLY this format: MOOD_LABEL|CONFIDENCE

Where MOOD_LABEL is one of: happy, sad, anxious, calm, energetic, angry, grateful, neutral
And CONFIDENCE is a number between 0.0 and 1.0

Title: ${title.take(200)}
Content: ${body.take(1000)}"""
        return engineService.generateText(prompt).map { response ->
            val parts = response.trim().split("|")
            val label = parts.getOrNull(0)?.trim()?.lowercase() ?: "neutral"
            val confidence = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0.5f
            MoodResult(
                moodLabel = label,
                confidence = confidence.coerceIn(0f, 1f),
            )
        }.withMetrics(AIFeature.MOOD_ANALYSIS, prompt.length)
    }

    suspend fun generateReflection(entries: List<LifeItem>, startDate: LocalDate, endDate: LocalDate): Flow<String> {
        val model = ensureModelForFeature(AIFeature.REFLECTION)
            ?: return flowOf("")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val entriesSummary = entries.take(30).joinToString("\n") { entry ->
            val date = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM d"))
            val cleanBody = entry.displayBody().take(200)
            "- [$date] ${entry.title.take(100)}: $cleanBody"
        }
        val prompt = """Based on these journal entries from ${startDate.format(dateFormatter)} to ${endDate.format(dateFormatter)}, write a thoughtful reflection. Identify patterns, highlight memorable moments, and offer gentle insights. Write in a warm, supportive tone.

Entries:
$entriesSummary"""
        return engineService.generateText(prompt, systemInstruction = "You are a compassionate personal journal assistant. Provide thoughtful reflections based on the user's journal entries. Be supportive and insightful.")
            .withMetrics(AIFeature.REFLECTION, prompt.length)
    }

    suspend fun describePhoto(imageBytes: ByteArray): Flow<String> {
        val model = ensureModelForFeature(AIFeature.PHOTO_DESCRIPTION)
            ?: return flowOf("")
        val prompt = "Describe this photo in 2-3 sentences. Focus on the main subjects, setting, and mood of the image."
        return engineService.generateWithImage(prompt, imageBytes)
            .withMetrics(AIFeature.PHOTO_DESCRIPTION, prompt.length)
    }

    suspend fun summarizeAudio(audioBytes: ByteArray): Flow<String> {
        val model = ensureModelForFeature(AIFeature.AUDIO_SUMMARY)
            ?: return flowOf("")
        val prompt = "Listen to this audio recording and provide a brief summary of its content in 2-3 sentences."
        return engineService.generateWithAudio(prompt, audioBytes)
            .withMetrics(AIFeature.AUDIO_SUMMARY, prompt.length)
    }

    suspend fun chatWithJournal(
        message: String,
        contextEntries: List<LifeItem>,
        conversationId: Long? = null,
    ): Flow<String> {
        val model = ensureModelForFeature(AIFeature.CHAT)
            ?: return flowOf("")
        val context = if (contextEntries.isNotEmpty()) {
            "Recent journal context:\n" + contextEntries.take(10).joinToString("\n") { entry ->
                val date = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM d"))
                "- [$date] ${entry.title}: ${entry.displayBody().take(150)}"
            } + "\n\n"
        } else ""

        val fullPrompt = "$context$message"
        return engineService.chat(
            messages = listOf("user" to fullPrompt),
            systemInstruction = "You are a helpful personal journal assistant. You help the user reflect on their journal entries, answer questions about their past activities, and provide supportive insights. Be concise and warm. If the user asks about their journal, refer to the provided context.",
        ).withMetrics(AIFeature.CHAT, fullPrompt.length, conversationId)
    }

    suspend fun rewriteText(text: String, tone: WritingTone): Flow<String> {
        val model = ensureModelForFeature(AIFeature.WRITING_ASSISTANT)
            ?: return flowOf("")
        val toneInstruction = when (tone) {
            WritingTone.FORMAL -> "Rewrite in a formal, professional tone"
            WritingTone.CASUAL -> "Rewrite in a casual, friendly tone"
            WritingTone.CONCISE -> "Rewrite to be more concise and direct"
            WritingTone.CREATIVE -> "Rewrite in a creative, expressive style"
            WritingTone.FIX_GRAMMAR -> "Fix any grammar, spelling, or punctuation errors without changing the style"
        }
        val prompt = """$toneInstruction. Return ONLY the rewritten text, nothing else.

Text:
${text.take(2000)}"""
        return engineService.generateText(prompt)
            .withMetrics(AIFeature.WRITING_ASSISTANT, prompt.length)
    }

    suspend fun naturalLanguageSearch(query: String, availableTags: List<String>): Flow<String> {
        val model = ensureModelForFeature(AIFeature.NL_SEARCH)
            ?: return flowOf("")
        val tagsList = availableTags.take(50).joinToString(", ")
        val prompt = """Convert this natural language search query into structured filters for a journal app. Return ONLY a JSON object with these possible fields (omit fields that aren't relevant):
- "query": text to search for in titles and content
- "type": one of "Thought", "Note", "Task", "Reminder", "Photo", "Video", "Audio", "Location", "Pdf", "Mixed"
- "tags": array of relevant tags from the available list
- "favoritesOnly": true if user wants favorites only
- "dateRange": {"start": "YYYY-MM-DD", "end": "YYYY-MM-DD"} if a date range is specified

Available tags: $tagsList

User query: $query"""
        return engineService.generateText(prompt)
            .withMetrics(AIFeature.NL_SEARCH, prompt.length)
    }
}
