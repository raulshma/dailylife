package com.raulshma.dailylife.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.security.MediaDecryptCoordinator
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferPdfUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.LocalDecryptCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberDecryptedMediaUri(uriString: String?): String? {
    val coordinator = LocalDecryptCoordinator.current
    return produceState<String?>(initialValue = null, key1 = uriString) {
        value = if (uriString == null) {
            null
        } else if (uriString.endsWith(".enc")) {
            coordinator.decrypt(uriString)
        } else {
            uriString
        }
    }.value
}

@Composable
internal fun rememberVideoThumbnail(item: LifeItem): String? {
    val coordinator = LocalDecryptCoordinator.current
    val videoUrl = rememberDecryptedMediaUri(item.inferVideoPlaybackUrl())
    return produceState<String?>(initialValue = null, key1 = item.id, key2 = videoUrl) {
        value = if (videoUrl == null) {
            null
        } else {
            coordinator.generateVideoThumbnail(videoUrl)
        }
    }.value
}

@Composable
internal fun rememberAudioWaveform(item: LifeItem): List<Float> {
    val context = LocalContext.current
    val rawAudioUrl = remember(item.id, item.title, item.body) {
        item.inferAudioUrl()
            ?: item.body.split("\\s+".toRegex()).firstOrNull { it.startsWith("content://") || it.startsWith("file://") }
    }
    val decryptedUrl = rememberDecryptedMediaUri(rawAudioUrl)
    return produceState<List<Float>>(initialValue = emptyList(), key1 = item.id, key2 = decryptedUrl) {
        value = if (decryptedUrl == null) {
            emptyList()
        } else {
            withContext(Dispatchers.IO) {
                val generator = AudioWaveformGenerator()
                generator.generateWaveform(context, Uri.parse(decryptedUrl), barCount = 8)?.toList() ?: emptyList()
            }
        }
    }.value
}

@Composable
internal fun rememberPdfThumbnail(item: LifeItem): String? {
    val coordinator = LocalDecryptCoordinator.current
    val rawPdfUrl = remember(item.id, item.title, item.body) { item.inferPdfUrl() }
    val pdfUrl = rememberDecryptedMediaUri(rawPdfUrl)
    return produceState<String?>(initialValue = null, key1 = item.id, key2 = pdfUrl) {
        value = if (pdfUrl == null) {
            null
        } else {
            coordinator.generatePdfThumbnail(pdfUrl)
        }
    }.value
}
