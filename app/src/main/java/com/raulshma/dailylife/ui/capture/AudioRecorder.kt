package com.raulshma.dailylife.ui.capture

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    var isRecording: Boolean = false
        private set

    fun startRecording(): Uri? {
        if (isRecording) return null

        val dir = File(context.filesDir, "media/audio").apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.m4a")
        outputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                release()
                recorder = null
                outputFile = null
                return null
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun stopRecording(): Uri? {
        if (!isRecording) return null

        return try {
            recorder?.stop()
            outputFile?.let {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
            }
        } catch (e: RuntimeException) {
            null
        } finally {
            recorder?.release()
            recorder = null
            isRecording = false
        }
    }

    fun discardLastRecording() {
        outputFile?.delete()
        outputFile = null
    }

    fun clearLastRecordingReference() {
        outputFile = null
    }

    fun cancelRecording() {
        if (!isRecording) return
        try {
            recorder?.stop()
        } catch (_: RuntimeException) {
        } finally {
            recorder?.release()
            recorder = null
            outputFile?.delete()
            outputFile = null
            isRecording = false
        }
    }
}
