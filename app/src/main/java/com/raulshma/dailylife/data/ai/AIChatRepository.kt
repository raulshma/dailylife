package com.raulshma.dailylife.data.ai

import com.raulshma.dailylife.data.db.AIChatMessageEntity
import com.raulshma.dailylife.data.db.AIConversationDao
import com.raulshma.dailylife.data.db.AIConversationEntity
import com.raulshma.dailylife.data.db.AIMetricsEntity
import com.raulshma.dailylife.data.db.DailyMetricsSummary
import com.raulshma.dailylife.data.db.FeatureMetricsSummary
import com.raulshma.dailylife.data.db.ModelMetricsSummary
import com.raulshma.dailylife.domain.AIMetric
import com.raulshma.dailylife.domain.AIConversation
import com.raulshma.dailylife.domain.ChatMessage
import com.raulshma.dailylife.domain.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatRepository @Inject constructor(
    private val dao: AIConversationDao,
) {
    val allConversations: Flow<List<AIConversation>> =
        dao.getAllConversations().map { list -> list.map { it.toDomain() } }

    fun getConversation(id: Long): Flow<AIConversation?> =
        dao.getConversationFlow(id).map { it?.toDomain() }

    fun getMessages(conversationId: Long): Flow<List<ChatMessage>> =
        dao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getConversationOnce(id: Long): AIConversation? = withContext(Dispatchers.IO) {
        dao.getConversation(id)?.toDomain()
    }

    suspend fun createConversation(title: String, modelId: String? = null): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.insertConversation(
            AIConversationEntity(
                title = title,
                modelId = modelId,
                createdAt = now,
                updatedAt = now,
                messageCount = 0,
            ),
        )
    }

    suspend fun addUserMessage(conversationId: Long, content: String): ChatMessage {
        val message = ChatMessage(ChatRole.USER, content)
        withContext(Dispatchers.IO) {
            val entity = AIChatMessageEntity(
                conversationId = conversationId,
                role = "USER",
                content = content,
                timestamp = message.timestamp,
            )
            val conv = dao.getConversation(conversationId)
            val title = if (conv?.messageCount == 0) {
                content.take(60).let { if (content.length > 60) "$it..." else it }
            } else null
            dao.addMessageAndUpdateConversation(entity, title)
        }
        return message
    }

    suspend fun addAssistantMessage(conversationId: Long, content: String): ChatMessage {
        val message = ChatMessage(ChatRole.ASSISTANT, content)
        withContext(Dispatchers.IO) {
            dao.addMessageAndUpdateConversation(
                AIChatMessageEntity(
                    conversationId = conversationId,
                    role = "ASSISTANT",
                    content = content,
                    timestamp = message.timestamp,
                ),
            )
        }
        return message
    }

    suspend fun deleteConversation(conversationId: Long) = withContext(Dispatchers.IO) {
        dao.deleteConversation(conversationId)
    }

    suspend fun togglePin(conversationId: Long) = withContext(Dispatchers.IO) {
        val conv = dao.getConversation(conversationId) ?: return@withContext
        dao.updatePinned(conversationId, !conv.isPinned)
    }

    suspend fun recordMetrics(
        feature: String,
        modelId: String?,
        timeToFirstTokenMs: Long?,
        totalGenerationMs: Long,
        inputCharCount: Int,
        outputCharCount: Int,
        isError: Boolean = false,
        errorMessage: String? = null,
        conversationId: Long? = null,
    ) = withContext(Dispatchers.IO) {
        dao.insertMetrics(
            AIMetricsEntity(
                conversationId = conversationId,
                feature = feature,
                modelId = modelId,
                timeToFirstTokenMs = timeToFirstTokenMs,
                totalGenerationMs = totalGenerationMs,
                inputCharCount = inputCharCount,
                outputCharCount = outputCharCount,
                isError = isError,
                errorMessage = errorMessage,
            ),
        )
    }

    suspend fun getMetricsByFeature(sinceTimestamp: Long): List<FeatureMetricsSummary> =
        withContext(Dispatchers.IO) { dao.getMetricsByFeature(sinceTimestamp) }

    suspend fun getTotalRequestsSince(sinceTimestamp: Long): Int =
        withContext(Dispatchers.IO) { dao.getMetricsCountSince(sinceTimestamp) }

    suspend fun getTotalOutputCharsSince(sinceTimestamp: Long): Long =
        withContext(Dispatchers.IO) { dao.getTotalOutputCharsSince(sinceTimestamp) ?: 0L }

    suspend fun getAvgLatencySince(sinceTimestamp: Long): Double? =
        withContext(Dispatchers.IO) { dao.getAvgLatencySince(sinceTimestamp) }

    suspend fun getAvgTtftSince(sinceTimestamp: Long): Double? =
        withContext(Dispatchers.IO) { dao.getAvgTtftSince(sinceTimestamp) }

    suspend fun getErrorCountSince(sinceTimestamp: Long): Int =
        withContext(Dispatchers.IO) { dao.getErrorCountSince(sinceTimestamp) }

    suspend fun getRecentMetrics(limit: Int = 50): List<AIMetric> =
        withContext(Dispatchers.IO) {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            dao.getRecentMetrics(weekAgo, limit).map { it.toDomain() }
        }

    suspend fun getPaginatedMetrics(limit: Int = 20, offset: Int = 0): List<AIMetric> =
        withContext(Dispatchers.IO) {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            dao.getPaginatedMetrics(weekAgo, limit, offset).map { it.toDomain() }
        }

    suspend fun getMetricsByModel(sinceTimestamp: Long): List<ModelMetricsSummary> =
        withContext(Dispatchers.IO) { dao.getMetricsByModel(sinceTimestamp) }

    suspend fun getTotalInputCharsSince(sinceTimestamp: Long): Long =
        withContext(Dispatchers.IO) { dao.getTotalInputCharsSince(sinceTimestamp) ?: 0L }

    suspend fun getAvgTokensPerSecSince(sinceTimestamp: Long): Double? =
        withContext(Dispatchers.IO) { dao.getAvgTokensPerSecSince(sinceTimestamp) }

    suspend fun getDailyMetrics(sinceTimestamp: Long): List<DailyMetricsSummary> =
        withContext(Dispatchers.IO) { dao.getDailyMetrics(sinceTimestamp) }

    suspend fun clearMetrics() = withContext(Dispatchers.IO) {
        dao.deleteAllMetrics()
    }

    private suspend fun AIConversationEntity.toDomain(): AIConversation {
        val lastMessage = runCatching { dao.getLastMessage(id) }.getOrNull()
        return AIConversation(
            id = id,
            title = title,
            modelId = modelId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            messageCount = messageCount,
            isPinned = isPinned,
            lastMessagePreview = lastMessage?.content?.take(80)?.let {
                if (lastMessage.content.length > 80) "$it..." else it
            } ?: "",
        )
    }

    private fun AIChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
        role = ChatRole.valueOf(role),
        content = content,
        timestamp = timestamp,
    )

    private fun AIMetricsEntity.toDomain(): AIMetric = AIMetric(
        id = id,
        conversationId = conversationId,
        feature = feature,
        modelId = modelId,
        timeToFirstTokenMs = timeToFirstTokenMs,
        totalGenerationMs = totalGenerationMs,
        inputCharCount = inputCharCount,
        outputCharCount = outputCharCount,
        isError = isError,
        errorMessage = errorMessage,
        createdAt = createdAt,
    )
}
