package com.raulshma.dailylife.data.media

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.sqrt

class AudioWaveformGenerator {

    fun generateWaveform(context: Context, uri: Uri, barCount: Int = 32): FloatArray? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(context, uri, null)
            val trackIndex = selectAudioTrack(extractor)
            if (trackIndex < 0) return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val bars = decodeToBars(extractor, decoder, durationUs, barCount)
            decoder.stop()
            decoder.release()
            extractor.release()

            bars
        } catch (_: Exception) {
            runCatching { extractor.release() }
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

    private fun decodeToBars(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        durationUs: Long,
        barCount: Int,
    ): FloatArray? {
        val rmsAccumulators = FloatArray(barCount)
        val sampleCounts = IntArray(barCount)
        val bufferInfo = MediaCodec.BufferInfo()
        var sawInputEOS = false
        var sawOutputEOS = false
        var totalSamplesDecoded = 0L
        val estimatedTotalSamples = (durationUs / 1_000_000.0 * 44100).toLong().coerceAtLeast(1)

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
                    accumulateRms(outputBuffer, bufferInfo.size, totalSamplesDecoded, estimatedTotalSamples, rmsAccumulators, sampleCounts)
                    totalSamplesDecoded += bufferInfo.size / 2L
                }
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    sawOutputEOS = true
                }
                decoder.releaseOutputBuffer(outputBufferId, false)
            }
        }

        var hasData = false
        val result = FloatArray(barCount) { i ->
            if (sampleCounts[i] > 0) {
                hasData = true
                val rms = sqrt(rmsAccumulators[i] / sampleCounts[i]).coerceIn(0f, 1f)
                rms
            } else {
                0f
            }
        }
        return if (hasData) result else null
    }

    private fun accumulateRms(
        buffer: ByteBuffer,
        size: Int,
        samplesSoFar: Long,
        estimatedTotal: Long,
        rmsAccumulators: FloatArray,
        sampleCounts: IntArray,
    ) {
        val barCount = rmsAccumulators.size
        val samplesInChunk = size / 2
        var i = 0
        while (i < samplesInChunk) {
            val low = buffer.get().toInt() and 0xFF
            val high = buffer.get().toInt()
            val sample = abs((high shl 8 or low).toShort().toInt()) / 32768f
            val sampleIndex = samplesSoFar + i
            val barIndex = ((sampleIndex * barCount) / estimatedTotal).toInt().coerceIn(0, barCount - 1)
            rmsAccumulators[barIndex] += sample * sample
            sampleCounts[barIndex]++
            i++
        }
    }
}
