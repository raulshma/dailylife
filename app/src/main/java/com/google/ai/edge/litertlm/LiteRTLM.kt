package com.google.ai.edge.litertlm

enum class LogSeverity {
    ERROR,
}

class EngineConfig(
    val modelPath: String,
    val cacheDir: String? = null,
)

class Engine(config: EngineConfig) : AutoCloseable {
    fun initialize() {}
    fun createConversation(config: ConversationConfig = ConversationConfig()): Conversation = Conversation()
    override fun close() {}

    companion object {
        fun setNativeMinLogSeverity(severity: LogSeverity) {}
    }
}

class ConversationConfig(
    val systemInstruction: String? = null,
)

class Conversation : AutoCloseable {
    fun sendMessage(content: String): Message = Message()
    fun sendMessage(contents: List<Content>): Message = Message()
    fun sendMessageAsync(content: String): kotlinx.coroutines.flow.Flow<Message> = kotlinx.coroutines.flow.emptyFlow()
    fun sendMessageAsync(contents: List<Content>): kotlinx.coroutines.flow.Flow<Message> = kotlinx.coroutines.flow.emptyFlow()
    fun cancelProcess() {}
    override fun close() {}
}

class Message {
    val contents: Contents? = null
}

class Contents {
    val items: List<Content> = emptyList()
}

sealed class Content {
    data class Text(val text: String) : Content()
    data class ImageBytes(val bytes: ByteArray) : Content()
    data class AudioBytes(val bytes: ByteArray) : Content()
}
