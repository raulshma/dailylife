package com.raulshma.dailylife.data.ai

import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.EngineState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LiteRTEngineService"

@Singleton
class LiteRTEngineService @Inject constructor(
    private val modelManager: ModelManager,
) {
    private var engine: Engine? = null
    private var currentModelId: String? = null
    private val mutex = Mutex()
    private var currentConversation: Conversation? = null

    private val _engineState = MutableStateFlow<EngineState>(EngineState.Idle)
    val engineState = _engineState.asStateFlow()

    val isEngineReady: Boolean
        get() = _engineState.value is EngineState.Ready

    val loadedModelId: String?
        get() = currentModelId

    val loadedModelName: String?
        get() = (_engineState.value as? EngineState.Ready)?.modelName

    suspend fun loadModel(model: AIModel): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (currentModelId == model.id && engine != null) {
                _engineState.value = EngineState.Ready(model.name)
                return@withContext Result.success(Unit)
            }
            unloadModelInternal()
            try {
                _engineState.value = EngineState.LoadingModel(model.name)
                Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
                val modelPath = modelManager.modelFile(model.id).absolutePath
                val cacheDir = modelManager.modelCacheDir(model.id).absolutePath
                val config = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.CPU(),
                    cacheDir = cacheDir,
                )
                _engineState.value = EngineState.Initializing(model.name)
                val eng = Engine(config)
                eng.initialize()
                engine = eng
                currentModelId = model.id
                _engineState.value = EngineState.Ready(model.name)
                Log.i(TAG, "Model loaded: ${model.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load model: ${model.id}", e)
                _engineState.value = EngineState.Error(e.message ?: "Unknown error")
                Result.failure(e)
            }
        }
    }

    suspend fun unloadModel() = mutex.withLock {
        withContext(Dispatchers.IO) {
            unloadModelInternal()
        }
    }

    private fun unloadModelInternal() {
        currentConversation?.close()
        currentConversation = null
        engine?.close()
        engine = null
        currentModelId = null
        _engineState.value = EngineState.Idle
    }

    fun generateText(
        prompt: String,
        systemInstruction: String? = null,
    ): Flow<String> = flow {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction?.let { Contents.of(it) }
                )
            )
            currentConversation?.close()
            currentConversation = conversation
            try {
                val fullResponse = StringBuilder()
                conversation.sendMessageAsync(prompt).collect { message ->
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        fullResponse.append(text)
                        emit(fullResponse.toString())
                    }
                }
            } finally {
                conversation.close()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun generateWithImage(
        prompt: String,
        imageBytes: ByteArray,
        systemInstruction: String? = null,
    ): Flow<String> = flow {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction?.let { Contents.of(it) }
                )
            )
            currentConversation?.close()
            currentConversation = conversation
            try {
                val fullResponse = StringBuilder()
                val contents = Contents.of(
                    Content.Text(prompt),
                    Content.ImageBytes(imageBytes),
                )
                conversation.sendMessageAsync(contents).collect { message ->
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        fullResponse.append(text)
                        emit(fullResponse.toString())
                    }
                }
            } finally {
                conversation.close()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun generateWithAudio(
        prompt: String,
        audioBytes: ByteArray,
        systemInstruction: String? = null,
    ): Flow<String> = flow {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction?.let { Contents.of(it) }
                )
            )
            currentConversation?.close()
            currentConversation = conversation
            try {
                val fullResponse = StringBuilder()
                val contents = Contents.of(
                    Content.Text(prompt),
                    Content.AudioBytes(audioBytes),
                )
                conversation.sendMessageAsync(contents).collect { message ->
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        fullResponse.append(text)
                        emit(fullResponse.toString())
                    }
                }
            } finally {
                conversation.close()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun chat(
        messages: List<Pair<String, String>>,
        systemInstruction: String? = null,
    ): Flow<String> = flow {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val initialMessages = messages.dropLast(1).map { (role, text) ->
                when (role) {
                    "user" -> com.google.ai.edge.litertlm.Message.user(text)
                    "model", "assistant" -> com.google.ai.edge.litertlm.Message.model(text)
                    else -> com.google.ai.edge.litertlm.Message.user(text)
                }
            }
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction?.let { Contents.of(it) },
                    initialMessages = initialMessages,
                )
            )
            currentConversation?.close()
            currentConversation = conversation
            try {
                val lastMessage = messages.last().second
                val fullResponse = StringBuilder()
                conversation.sendMessageAsync(lastMessage).collect { message ->
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        fullResponse.append(text)
                        emit(fullResponse.toString())
                    }
                }
            } finally {
                conversation.close()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun cancelGeneration() {
        currentConversation?.cancelProcess()
    }
}
