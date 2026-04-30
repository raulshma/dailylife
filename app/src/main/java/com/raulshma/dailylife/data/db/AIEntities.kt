package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_conversations",
    indices = [
        Index("updatedAt"),
        Index("isPinned"),
        Index(value = ["isPinned", "updatedAt"]),
    ],
)
data class AIConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val modelId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val isPinned: Boolean = false,
)

@Entity(
    tableName = "ai_chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = AIConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("conversationId"),
        Index(value = ["conversationId", "timestamp"]),
    ],
)
data class AIChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "ai_metrics",
    indices = [
        Index("conversationId"),
        Index("feature"),
        Index("createdAt"),
    ],
)
data class AIMetricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long? = null,
    val feature: String,
    val modelId: String? = null,
    val timeToFirstTokenMs: Long? = null,
    val totalGenerationMs: Long,
    val inputCharCount: Int,
    val outputCharCount: Int,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
