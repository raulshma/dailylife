package com.raulshma.dailylife.ui.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechTranscriber(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null
    private var shouldContinueListening = false
    private val committedTranscript = StringBuilder()
    private var onTranscriptChanged: ((String) -> Unit)? = null

    val isListening: Boolean
        get() = shouldContinueListening

    fun start(onTranscriptChanged: (String) -> Unit): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return false
        }

        this.onTranscriptChanged = onTranscriptChanged
        shouldContinueListening = true
        committedTranscript.clear()

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(createRecognitionListener())
            }
        }

        startListeningSession()
        return true
    }

    fun stop(): String {
        shouldContinueListening = false
        recognizer?.stopListening()
        return committedTranscript.toString().trim()
    }

    fun cancel() {
        shouldContinueListening = false
        recognizer?.cancel()
        committedTranscript.clear()
        publishTranscript("")
    }

    fun destroy() {
        shouldContinueListening = false
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
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
            },
        )
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                if (shouldContinueListening) {
                    startListeningSession()
                }
            }

            override fun onResults(results: Bundle?) {
                val recognized = extractFirstResult(results) ?: ""
                appendToCommittedTranscript(recognized)
                if (shouldContinueListening) {
                    startListeningSession()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = extractFirstResult(partialResults) ?: ""
                publishTranscript(buildCombinedTranscript(partial))
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }

    private fun appendToCommittedTranscript(newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty()) {
            publishTranscript(committedTranscript.toString())
            return
        }

        if (committedTranscript.isNotEmpty()) {
            committedTranscript.append(' ')
        }
        committedTranscript.append(trimmed)
        publishTranscript(committedTranscript.toString())
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
}
