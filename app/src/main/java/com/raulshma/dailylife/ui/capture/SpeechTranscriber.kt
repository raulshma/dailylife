package com.raulshma.dailylife.ui.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechTranscriber(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null
    private var shouldContinueListening = false
    private val committedTranscript = StringBuilder()
    private var onTranscriptChanged: ((String) -> Unit)? = null
    private var onErrorInfo: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var consecutiveErrors = 0

    val isListening: Boolean
        get() = shouldContinueListening

    fun start(onTranscriptChanged: (String) -> Unit, onError: ((String) -> Unit)? = null): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return false
        }

        this.onTranscriptChanged = onTranscriptChanged
        this.onErrorInfo = onError
        shouldContinueListening = true
        consecutiveErrors = 0
        committedTranscript.clear()

        mainHandler.post {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                    it.setRecognitionListener(createRecognitionListener())
                }
            }
            startListeningSession()
        }
        return true
    }

    fun stop(): String {
        shouldContinueListening = false
        mainHandler.post { recognizer?.stopListening() }
        return committedTranscript.toString().trim()
    }

    fun cancel() {
        shouldContinueListening = false
        mainHandler.post { recognizer?.cancel() }
        committedTranscript.clear()
        publishTranscript("")
    }

    fun destroy() {
        shouldContinueListening = false
        mainHandler.post {
            recognizer?.cancel()
            recognizer?.destroy()
            recognizer = null
        }
        committedTranscript.clear()
        onTranscriptChanged = null
    }

    private fun startListeningSession() {
        if (!shouldContinueListening) return

        recognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30_000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3_000L)
            },
        )
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                consecutiveErrors = 0
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                if (!shouldContinueListening) return
                val errorName = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                    SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
                    SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                    SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
                    SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
                    else -> "ERROR_UNKNOWN($error)"
                }
                // NO_MATCH and SPEECH_TIMEOUT are normal - just restart silently
                val isRecoverableError = error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                if (!isRecoverableError) {
                    consecutiveErrors++
                    onErrorInfo?.invoke("Speech error: $errorName (attempt $consecutiveErrors)")
                }
                if (consecutiveErrors <= MAX_CONSECUTIVE_ERRORS) {
                    val delay = if (isRecoverableError) 100L else ERROR_RETRY_DELAY_MS
                    mainHandler.postDelayed({ startListeningSession() }, delay)
                } else {
                    onErrorInfo?.invoke("Speech recognition stopped after too many errors.")
                }
            }

            override fun onResults(results: Bundle?) {
                consecutiveErrors = 0
                val recognized = extractFirstResult(results) ?: ""
                appendToCommittedTranscript(recognized)
                if (shouldContinueListening) {
                    mainHandler.post { startListeningSession() }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = extractFirstResult(partialResults) ?: ""
                mainHandler.post { publishTranscript(buildCombinedTranscript(partial)) }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }

    private fun appendToCommittedTranscript(newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty()) {
            mainHandler.post { publishTranscript(committedTranscript.toString()) }
            return
        }

        if (committedTranscript.isNotEmpty()) {
            committedTranscript.append(' ')
        }
        committedTranscript.append(trimmed)
        mainHandler.post { publishTranscript(committedTranscript.toString()) }
    }

    private fun buildCombinedTranscript(partialText: String): String {
        val committed = committedTranscript.toString().trim()
        val partial = partialText.trim()

        return when {
            committed.isEmpty() -> partial
            partial.isEmpty() -> committed
            else -> "$committed $partial"
        }
    }

    private fun publishTranscript(text: String) {
        onTranscriptChanged?.invoke(text.trim())
    }

    private fun extractFirstResult(bundle: Bundle?): String? {
        return bundle
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
    }

    companion object {
        private const val MAX_CONSECUTIVE_ERRORS = 15
        private const val ERROR_RETRY_DELAY_MS = 800L
    }
}
