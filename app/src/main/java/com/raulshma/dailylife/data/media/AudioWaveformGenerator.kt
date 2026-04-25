package com.raulshma.dailylife.data.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * Generates waveform amplitude data from audio files using MediaExtractor + MediaCodec.
 * Returns a list of normalized amplitude values (0.0 to 1.0) for visualization.
 */
class AudioWaveformGenerator {

    /**
     * Extracts [barCount] amplitude values from the audio at [uri].
     * Returns null if the file cannot be decoded.
     */
    fun generateWaveform(uri: Uri, barCount: Int = 32): List<Float>? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(uri.toString())
            val trackIndex = selectAudioTrack(extractor)
            if (trackIndex < 0) return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val samples = decodeSamples(extractor, decoder, durationUs)
            decoder.stop()
            decoder.release()
            extractor.release()

            if (samples.isEmpty()) return null
            downsampleToBars(samples, barCount)
        } catch (e: Exception) {
            extractor.release()
            null
        }
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i
        }
        return -1
    }

    private fun decodeSamples(extractor: MediaExtractor, decoder: MediaCodec, durationUs: Long): List<Float> {
        val samples = mutableListOf<Float>()
        val bufferInfo = MediaCodec.BufferInfo()
        var sawInputEOS = false
        var sawOutputEOS = false

        while (!sawOutputEOS) {
            if (!sawInputEOS) {
                val inputBufferId = decoder.dequeueInputBuffer(10_000)
                if (inputBufferId >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferId) ?: continue
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        sawInputEOS = true
                    } else {
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0)
                        extractor.advance()
                    }
                }
            }

            val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (outputBufferId >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferId)
                if (outputBuffer != null && bufferInfo.size > 0) {
                    val chunk = extractAmplitudes(outputBuffer, bufferInfo.size)
                    samples.addAll(chunk)
                }
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    sawOutputEOS = true
                }
                decoder.releaseOutputBuffer(outputBufferId, false)
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Format changed, can query new format if needed
            }
        }
        return samples
    }

    private fun extractAmplitudes(buffer: ByteBuffer, size: Int): List<Float> {
        val amplitudes = mutableListOf<Float>()
        val samples = size / 2 // 16-bit PCM
        var i = 0
        while (i < samples) {
            // Read little-endian 16-bit signed sample
            val low = buffer.get().toInt() and 0xFF
            val high = buffer.get().toInt()
            val sample = (high shl 8 or low).toShort().toInt()
            amplitudes.add(kotlin.math.abs(sample) / 32768f)
            i++
        }
        return amplitudes
    }

    private fun downsampleToBars(samples: List<Float>, barCount: Int): List<Float> {
        if (samples.isEmpty() || barCount <= 0) return emptyList()
        val samplesPerBar = samples.size / barCount
        if (samplesPerBar <= 0) return samples

        return (0 until barCount).map { barIndex ->
            val start = barIndex * samplesPerBar
            val end = kotlin.math.min(start + samplesPerBar, samples.size)
            val chunk = samples.subList(start, end)
            val rms = sqrt(chunk.map { it * it }.average()).toFloat()
            rms.coerceIn(0f, 1f)
        }
    }
}
