package com.raulshma.dailylife.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AIConversationDao {

    @Query("SELECT * FROM ai_conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<AIConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE id = :conversationId")
    suspend fun getConversation(conversationId: Long): AIConversationEntity?

    @Query("SELECT * FROM ai_conversations WHERE id = :conversationId")
    fun getConversationFlow(conversationId: Long): Flow<AIConversationEntity?>

    @Query("SELECT * FROM ai_chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<AIChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationOnce(conversationId: Long): List<AIChatMessageEntity>

    @Query("SELECT * FROM ai_chat_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: Long): AIChatMessageEntity?

    @Insert
    suspend fun insertConversation(conversation: AIConversationEntity): Long

    @Insert
    suspend fun insertMessage(message: AIChatMessageEntity): Long

    @Insert
    suspend fun insertMetrics(metric: AIMetricsEntity): Long

    @Query("""
        UPDATE ai_conversations SET
            title = :title,
            updatedAt = :updatedAt,
            messageCount = :messageCount
        WHERE id = :conversationId
    """)
    suspend fun updateConversation(
        conversationId: Long,
        title: String,
        updatedAt: Long,
        messageCount: Int,
    )

    @Query("UPDATE ai_conversations SET isPinned = :isPinned WHERE id = :conversationId")
    suspend fun updatePinned(conversationId: Long, isPinned: Boolean)

    @Query("DELETE FROM ai_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM ai_metrics")
    suspend fun deleteAllMetrics()

    @Query("SELECT COUNT(*) FROM ai_conversations")
    fun conversationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ai_chat_messages")
    fun messageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ai_metrics")
    fun metricsCount(): Flow<Int>

    @Query("""
        SELECT feature, COUNT(*) as count,
               AVG(totalGenerationMs) as avgLatencyMs,
               SUM(outputCharCount) as totalOutputChars,
               AVG(timeToFirstTokenMs) as avgTtftMs,
               SUM(inputCharCount) as totalInputChars,
               SUM(CASE WHEN isError = 1 THEN 1 ELSE 0 END) as errorCount
        FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
        GROUP BY feature
    """)
    suspend fun getMetricsByFeature(sinceTimestamp: Long): List<FeatureMetricsSummary>

    @Query("""
        SELECT COUNT(*) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
    """)
    suspend fun getMetricsCountSince(sinceTimestamp: Long): Int

    @Query("""
        SELECT SUM(outputCharCount) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
    """)
    suspend fun getTotalOutputCharsSince(sinceTimestamp: Long): Long?

    @Query("""
        SELECT AVG(totalGenerationMs) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp AND isError = 0
    """)
    suspend fun getAvgLatencySince(sinceTimestamp: Long): Double?

    @Query("""
        SELECT AVG(timeToFirstTokenMs) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp AND isError = 0 AND timeToFirstTokenMs IS NOT NULL
    """)
    suspend fun getAvgTtftSince(sinceTimestamp: Long): Double?

    @Query("""
        SELECT COUNT(*) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp AND isError = 1
    """)
    suspend fun getErrorCountSince(sinceTimestamp: Long): Int

    @Query("""
        SELECT * FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentMetrics(sinceTimestamp: Long, limit: Int = 50): List<AIMetricsEntity>

    @Query("""
        SELECT date(createdAt / 1000, 'unixepoch', 'localtime') as day,
               COUNT(*) as count,
               SUM(outputCharCount) as totalChars,
               AVG(totalGenerationMs) as avgLatencyMs,
               SUM(CASE WHEN isError = 1 THEN 1 ELSE 0 END) as errorCount
        FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
        GROUP BY day
        ORDER BY day DESC
    """)
    suspend fun getDailyMetrics(sinceTimestamp: Long): List<DailyMetricsSummary>

    @Query("""
        SELECT modelId, COUNT(*) as count,
               AVG(totalGenerationMs) as avgLatencyMs,
               AVG(timeToFirstTokenMs) as avgTtftMs,
               SUM(outputCharCount) as totalOutputChars
        FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp AND modelId IS NOT NULL
        GROUP BY modelId
        ORDER BY count DESC
    """)
    suspend fun getMetricsByModel(sinceTimestamp: Long): List<ModelMetricsSummary>

    @Query("""
        SELECT * FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPaginatedMetrics(sinceTimestamp: Long, limit: Int = 20, offset: Int = 0): List<AIMetricsEntity>

    @Query("""
        SELECT SUM(inputCharCount) FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp
    """)
    suspend fun getTotalInputCharsSince(sinceTimestamp: Long): Long?

    @Query("""
        SELECT AVG(CASE WHEN totalGenerationMs > 0 THEN (outputCharCount * 1.0 / totalGenerationMs * 1000) END)
        FROM ai_metrics
        WHERE createdAt >= :sinceTimestamp AND isError = 0
    """)
    suspend fun getAvgTokensPerSecSince(sinceTimestamp: Long): Double?

    @Transaction
    suspend fun createConversationWithMessage(
        conversation: AIConversationEntity,
        message: AIChatMessageEntity,
    ): Long {
        val convId = insertConversation(conversation)
        insertMessage(message.copy(conversationId = convId))
        updateConversation(convId, conversation.title, conversation.updatedAt, 1)
        return convId
    }

    @Transaction
    suspend fun addMessageAndUpdateConversation(
        message: AIChatMessageEntity,
        title: String? = null,
    ) {
        insertMessage(message)
        val existing = getConversation(message.conversationId) ?: return
        val messages = getMessagesForConversationOnce(message.conversationId)
        val newTitle = title ?: existing.title
        val now = System.currentTimeMillis()
        updateConversation(message.conversationId, newTitle, now, messages.size)
    }
}

data class FeatureMetricsSummary(
    val feature: String,
    val count: Int,
    val avgLatencyMs: Double?,
    val totalOutputChars: Long?,
    val avgTtftMs: Double?,
    val totalInputChars: Long?,
    val errorCount: Int,
)

data class DailyMetricsSummary(
    val day: String,
    val count: Int,
    val totalChars: Long?,
    val avgLatencyMs: Double?,
    val errorCount: Int,
)

data class ModelMetricsSummary(
    val modelId: String,
    val count: Int,
    val avgLatencyMs: Double?,
    val avgTtftMs: Double?,
    val totalOutputChars: Long?,
)
