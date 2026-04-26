package com.raulshma.dailylife.ui.capture

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecorder(private val context: Context) {
    private var outputFile: File? = null
    private var recordingThread: Thread? = null

    @Volatile
    var isRecording: Boolean = false
        private set

    @Volatile
    private var cancelled = false

    /** Live amplitude (0f–1f), updated from the capture thread. */
    @Volatile
    var currentAmplitude: Float = 0f
        private set

    /** Elapsed recording time in milliseconds. */
    @Volatile
    var elapsedMs: Long = 0L
        private set

    private var recordingStartTime: Long = 0L

    fun startRecording(): Uri? {
        if (isRecording) return null

        val dir = File(context.filesDir, "media/audio").apply { mkdirs() }
        val wavFile = File(dir, "${System.currentTimeMillis()}.wav")
        outputFile = wavFile
        cancelled = false
        currentAmplitude = 0f
        elapsedMs = 0L

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize <= 0) {
            outputFile = null
            return null
        }

        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize * 2,
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            outputFile = null
            return null
        }

        record.startRecording()
        isRecording = true
        recordingStartTime = System.currentTimeMillis()

        recordingThread = Thread {
            try {
                captureLoop(record, bufferSize, wavFile, sampleRate)
            } finally {
                record.release()
            }
        }.apply { start() }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", wavFile)
    }

    private fun captureLoop(record: AudioRecord, bufferSize: Int, wavFile: File, sampleRate: Int) {
        val rawFile = File(wavFile.parent, "${wavFile.nameWithoutExtension}.pcm")
        val buffer = ByteArray(bufferSize)

        try {
            FileOutputStream(rawFile).use { out ->
                while (isRecording) {
                    val read = record.read(buffer, 0, bufferSize)
                    if (read > 0) {
                        out.write(buffer, 0, read)
                        // Compute RMS amplitude for live visualization
                        var sumSquares = 0.0
                        val sampleCount = read / 2
                        for (i in 0 until sampleCount) {
                            val low = buffer[i * 2].toInt() and 0xFF
                            val high = buffer[i * 2 + 1].toInt()
                            val sample = (high shl 8 or low).toShort().toInt()
                            sumSquares += (sample.toDouble() * sample.toDouble())
                        }
                        val rms = kotlin.math.sqrt(sumSquares / sampleCount).toFloat()
                        currentAmplitude = (rms / 32768f).coerceIn(0f, 1f)
                        elapsedMs = System.currentTimeMillis() - recordingStartTime
                    }
                }
            }
            record.stop()
            if (!cancelled) {
                convertPcmToWav(rawFile, wavFile, sampleRate, 1, 16)
            }
        } finally {
            rawFile.delete()
        }
    }

    fun stopRecording(): Uri? {
        if (!isRecording) return null
        isRecording = false
        joinThread()
        currentAmplitude = 0f
        val file = outputFile ?: return null
        return if (file.exists() && file.length() > 44) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            null
        }
    }

    /** Returns the raw File path of the last recording, if available. */
    fun getOutputFilePath(): String? = outputFile?.takeIf { it.exists() }?.absolutePath

    fun discardLastRecording() {
        outputFile?.delete()
        outputFile = null
    }

    fun clearLastRecordingReference() {
        outputFile = null
    }

    fun cancelRecording() {
        if (!isRecording) return
        cancelled = true
        isRecording = false
        joinThread()
        currentAmplitude = 0f
        elapsedMs = 0L
        outputFile?.delete()
        outputFile = null
    }

    private fun joinThread() {
        try {
            recordingThread?.join(3000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        recordingThread = null
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val pcmData = pcmFile.readBytes()
        if (pcmData.isEmpty()) {
            wavFile.delete()
            return
        }

        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
            putInt(36 + pcmData.size)
            put(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))
            put(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
            putInt(16)
            putShort(1)
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
            putInt(pcmData.size)
        }.array()

        FileOutputStream(wavFile).use { out ->
            out.write(header)
            out.write(pcmData)
        }
    }
}
