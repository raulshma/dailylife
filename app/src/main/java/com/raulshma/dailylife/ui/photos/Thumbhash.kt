package com.raulshma.dailylife.ui.photos

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.ui.components.ShimmerBox

internal data class ThumbhashPreviewPalette(
    val colors: List<Color>,
)

@Composable
internal fun ThumbhashShimmerPlaceholder(
    preview: ThumbhashPreviewPalette?,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    val fallbackColors = if (isVideo) {
        listOf(
            Color(0xFF1B2231),
            Color(0xFF242E44),
            Color(0xFF1B2231),
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
        )
    }
    val colors = preview?.colors?.takeIf { it.size >= 3 } ?: fallbackColors

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(colors = colors),
            )
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            baseColor = Color.Transparent,
            highlightColor = Color.White.copy(alpha = 0.14f),
        )
    }
}

internal fun LifeItem.thumbhashPreviewPalette(): ThumbhashPreviewPalette? {
    val token = extractThumbhashToken() ?: return null
    val bytes = decodeThumbhashBytes(token) ?: return null
    if (bytes.isEmpty()) return null

    fun channelAt(index: Int): Float =
        (bytes[index % bytes.size].toInt() and 0xFF) / 255f

    val c1 = Color(channelAt(0), channelAt(1), channelAt(2), 1f)
    val c2 = Color(channelAt(3), channelAt(4), channelAt(5), 1f)
    val c3 = Color(channelAt(6), channelAt(7), channelAt(8), 1f)

    return ThumbhashPreviewPalette(colors = listOf(c1, c2, c3))
}

internal fun LifeItem.extractThumbhashToken(): String? {
    val source = listOf(title, body).joinToString(" ")
    val queryPattern = Regex("""(?:[?&]|\b)(?:thumbhash|thumb_hash|thumb)=(?<value>[^&#\s]+)""", RegexOption.IGNORE_CASE)
    val inlinePattern = Regex("""\bthumbhash\s*[:=]\s*(?<value>[A-Za-z0-9_\-+/=]+)""", RegexOption.IGNORE_CASE)
    val queryMatch = queryPattern.find(source)?.groups?.get("value")?.value
    if (!queryMatch.isNullOrBlank()) return queryMatch
    val inlineMatch = inlinePattern.find(source)?.groups?.get("value")?.value
    return inlineMatch?.takeIf { it.isNotBlank() }
}

internal fun decodeThumbhashBytes(raw: String): ByteArray? {
    val normalized = raw.trim()
        .replace('-', '+')
        .replace('_', '/')
        .replace("%2B", "+", ignoreCase = true)
        .replace("%2F", "/", ignoreCase = true)
    if (normalized.isBlank()) return null

    val padded = when (normalized.length % 4) {
        2 -> "$normalized=="
        3 -> "$normalized="
        else -> normalized
    }
    return runCatching { Base64.decode(padded, Base64.DEFAULT) }.getOrNull()
}
