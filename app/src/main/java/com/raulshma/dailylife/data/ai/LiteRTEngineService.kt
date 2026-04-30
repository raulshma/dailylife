package com.raulshma.dailylife.data.ai

import android.util.Log
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.EngineState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                delay(150)
                Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
                val modelPath = modelManager.modelFile(model.id).absolutePath
                val cacheDir = modelManager.modelCacheDir(model.id).absolutePath
                val config = EngineConfig(
                    modelPath = modelPath,
                    cacheDir = cacheDir,
                )
                _engineState.value = EngineState.Initializing(model.name)
                delay(100)
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

    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
    ): Flow<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction,
                )
            )
            currentConversation?.close()
            currentConversation = conversation

            val resultFlow = MutableStateFlow("")
            val fullResponse = StringBuilder()

            conversation.sendMessageAsync(prompt).collect { message ->
                val text = message.contents?.items?.firstOrNull()?.let {
                    (it as? Content.Text)?.text
                } ?: ""
                if (text.isNotEmpty()) {
                    fullResponse.append(text)
                    resultFlow.value = fullResponse.toString()
                }
            }
            conversation.close()
            resultFlow
        }
    }

    suspend fun generateWithImage(
        prompt: String,
        imageBytes: ByteArray,
        systemInstruction: String? = null,
    ): Flow<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction,
                )
            )
            currentConversation?.close()
            currentConversation = conversation

            val resultFlow = MutableStateFlow("")
            val fullResponse = StringBuilder()

            val contents = listOf(
                Content.Text(prompt),
                Content.ImageBytes(imageBytes),
            )
            conversation.sendMessageAsync(contents).collect { message ->
                val text = message.contents?.items?.firstOrNull()?.let {
                    (it as? Content.Text)?.text
                } ?: ""
                if (text.isNotEmpty()) {
                    fullResponse.append(text)
                    resultFlow.value = fullResponse.toString()
                }
            }
            conversation.close()
            resultFlow
        }
    }

    suspend fun generateWithAudio(
        prompt: String,
        audioBytes: ByteArray,
        systemInstruction: String? = null,
    ): Flow<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction,
                )
            )
            currentConversation?.close()
            currentConversation = conversation

            val resultFlow = MutableStateFlow("")
            val fullResponse = StringBuilder()

            val contents = listOf(
                Content.Text(prompt),
                Content.AudioBytes(audioBytes),
            )
            conversation.sendMessageAsync(contents).collect { message ->
                val text = message.contents?.items?.firstOrNull()?.let {
                    (it as? Content.Text)?.text
                } ?: ""
                if (text.isNotEmpty()) {
                    fullResponse.append(text)
                    resultFlow.value = fullResponse.toString()
                }
            }
            conversation.close()
            resultFlow
        }
    }

    suspend fun chat(
        messages: List<Pair<String, String>>,
        systemInstruction: String? = null,
    ): Flow<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val eng = engine ?: throw IllegalStateException("No model loaded")
            val conversation = eng.createConversation(
                ConversationConfig(
                    systemInstruction = systemInstruction,
                )
            )
            currentConversation?.close()
            currentConversation = conversation

            val lastMessage = messages.last().second
            val resultFlow = MutableStateFlow("")
            val fullResponse = StringBuilder()

            conversation.sendMessageAsync(lastMessage).collect { message ->
                val text = message.contents?.items?.firstOrNull()?.let {
                    (it as? Content.Text)?.text
                } ?: ""
                if (text.isNotEmpty()) {
                    fullResponse.append(text)
                    resultFlow.value = fullResponse.toString()
                }
            }
            conversation.close()
            resultFlow
        }
    }

    fun cancelGeneration() {
        currentConversation?.cancelProcess()
    }
}
