package com.raulshma.dailylife.ui.photos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferPdfUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.components.icon
import com.raulshma.dailylife.ui.components.inferLocationMapTile
import com.raulshma.dailylife.ui.components.inferLocationPreview
import com.raulshma.dailylife.ui.components.rememberAudioWaveform
import com.raulshma.dailylife.ui.components.rememberDecryptedMediaUri
import com.raulshma.dailylife.ui.components.rememberPdfThumbnail
import com.raulshma.dailylife.ui.components.rememberVideoThumbnail
import com.raulshma.dailylife.ui.components.OpenStreetMapPreview
import com.raulshma.dailylife.ui.theme.DailyLifeSpring
import android.net.Uri

@Composable
internal fun ItemPreview(item: LifeItem, autoplayVideo: Boolean = false) {
    when (item.type) {
        LifeItemType.Photo -> ImagePreview(item = item)
        LifeItemType.Video -> VideoPreview(item = item, autoplay = autoplayVideo)
        LifeItemType.Audio -> AudioPreview(item = item)
        LifeItemType.Location -> LocationPreview(item = item)
        LifeItemType.Pdf -> PdfPreview(item = item)
        LifeItemType.Mixed -> {
            val previewType = rememberMixedPreviewType(item)
            when (previewType) {
                PreviewType.Image -> ImagePreview(item = item)
                PreviewType.Pdf -> PdfPreview(item = item)
                PreviewType.Location -> LocationPreview(item = item)
                PreviewType.Video -> VideoPreview(item = item, autoplay = autoplayVideo)
                PreviewType.Audio -> AudioPreview(item = item)
                PreviewType.Text -> TextPreview(item = item)
            }
        }

        else -> {
            val previewType = rememberFallbackPreviewType(item)
            when (previewType) {
                PreviewType.Image -> ImagePreview(item = item)
                PreviewType.Pdf -> PdfPreview(item = item)
                PreviewType.Video -> VideoPreview(item = item, autoplay = autoplayVideo)
                PreviewType.Audio -> AudioPreview(item = item)
                PreviewType.Location -> LocationPreview(item = item)
                PreviewType.Text -> TextPreview(item = item)
            }
        }
    }
}

private enum class PreviewType { Image, Pdf, Location, Video, Audio, Text }

@Composable
private fun rememberMixedPreviewType(item: LifeItem): PreviewType {
    return remember(item.id, item.title, item.body) {
        when {
            item.inferImagePreviewUrl() != null -> PreviewType.Image
            item.inferPdfUrl() != null -> PreviewType.Pdf
            item.inferLocationPreview() != null -> PreviewType.Location
            item.inferVideoPlaybackUrl() != null -> PreviewType.Video
            item.inferAudioUrl() != null -> PreviewType.Audio
            else -> PreviewType.Text
        }
    }
}

@Composable
private fun rememberFallbackPreviewType(item: LifeItem): PreviewType {
    return remember(item.id, item.title, item.body) {
        when {
            item.inferImagePreviewUrl() != null -> PreviewType.Image
            item.inferPdfUrl() != null -> PreviewType.Pdf
            item.inferVideoPlaybackUrl() != null -> PreviewType.Video
            item.inferAudioUrl() != null -> PreviewType.Audio
            item.inferLocationPreview() != null -> PreviewType.Location
            else -> PreviewType.Text
        }
    }
}

@Composable
private fun TextPreview(item: LifeItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = item.type.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = item.type.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Text(
            text = item.displayBody().ifBlank { "No notes yet" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ImagePreview(item: LifeItem) {
    val context = LocalContext.current
    val rawImageUrl = remember(item.id, item.title, item.body) { item.inferImagePreviewUrl() }
    val imageUrl = rememberDecryptedMediaUri(rawImageUrl)
    val thumbhashPreview = remember(item.id, item.title, item.body) { item.thumbhashPreviewPalette() }

    val imageRequest = remember(imageUrl) {
        imageUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(600, 600)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        }
    }
    val painter = rememberAsyncImagePainter(model = imageRequest)
    val painterState = painter.state

    if (imageUrl != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painter,
                contentDescription = "Image preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            when (painterState) {
                is coil.compose.AsyncImagePainter.State.Loading ->
                    ThumbhashShimmerPlaceholder(
                        preview = thumbhashPreview,
                        isVideo = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                is coil.compose.AsyncImagePainter.State.Error ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = "Photo placeholder",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Photo",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                else -> {}
            }
        }
    } else if (rawImageUrl != null) {
        ThumbhashShimmerPlaceholder(
            preview = thumbhashPreview,
            isVideo = false,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Photo placeholder",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Photo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun VideoPreview(item: LifeItem, autoplay: Boolean = false) {
    val context = LocalContext.current
    val rawVideoUrl = remember(item.id, item.title, item.body) { item.inferVideoPlaybackUrl() }
    val videoUrl = rememberDecryptedMediaUri(rawVideoUrl)
    val thumbhashPreview = remember(item.id, item.title, item.body) { item.thumbhashPreviewPalette() }

    if (autoplay && videoUrl != null) {
        val lifecycleOwner = LocalLifecycleOwner.current
        var videoView by remember { mutableStateOf<android.widget.VideoView?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    setVideoURI(Uri.parse(videoUrl))
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(0f, 0f)
                        start()
                    }
                    setOnErrorListener { _, _, _ -> true }
                    videoView = this
                }
            }
        )

        DisposableEffect(videoView, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> videoView?.pause()
                    Lifecycle.Event.ON_RESUME -> videoView?.let { if (!it.isPlaying) it.start() }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                videoView?.stopPlayback()
            }
        }
    } else {
        val rawImageUrl = remember(item.id, item.title, item.body) { item.inferImagePreviewUrl() }
        val imageUrl = rememberDecryptedMediaUri(rawImageUrl)
        val thumbUrl = rememberVideoThumbnail(item)
        val displayUrl = imageUrl ?: thumbUrl

        val imageRequest = remember(displayUrl) {
            displayUrl?.let {
                ImageRequest.Builder(context)
                    .data(it)
                    .size(600, 600)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build()
            }
        }
        val painter = rememberAsyncImagePainter(model = imageRequest)
        val painterState = painter.state

        Box(modifier = Modifier.fillMaxSize()) {
            if (displayUrl != null && videoUrl != null) {
                Image(
                    painter = painter,
                    contentDescription = "Video preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                when (painterState) {
                    is coil.compose.AsyncImagePainter.State.Loading ->
                        ThumbhashShimmerPlaceholder(
                            preview = thumbhashPreview,
                            isVideo = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    else -> {}
                }
            } else if (rawVideoUrl != null) {
                ThumbhashShimmerPlaceholder(
                    preview = thumbhashPreview,
                    isVideo = true,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "View playback",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun AudioPreview(item: LifeItem) {
    val waveform = rememberAudioWaveform(item)
    val targetHeights = if (waveform.isNotEmpty()) {
        waveform.map { (16.dp + (it * 24).dp).coerceAtLeast(4.dp) }
    } else {
        remember(item.id) {
            listOf(8.dp, 16.dp, 10.dp, 22.dp, 14.dp, 20.dp, 12.dp, 18.dp)
        }
    }
    val animatedHeights = targetHeights.mapIndexed { index, target ->
        androidx.compose.animation.core.animateFloatAsState(
            targetValue = target.value,
            animationSpec = DailyLifeSpring.Gentle,
            label = "audioBar-$index"
        ).value
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Audio recording",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Audio",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            animatedHeights.forEach { height ->
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(height.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
        if (item.displayBody().isNotBlank()) {
            Text(
                text = item.displayBody(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LocationPreview(item: LifeItem) {
    val location = remember(item.id, item.title, item.body) { item.inferLocationPreview() }
    val mapTile = remember(item.id, item.title, item.body) { item.inferLocationMapTile() }
    if (location != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            OpenStreetMapPreview(
                latitude = location.first,
                longitude = location.second,
                mapTile = mapTile,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.58f), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "OpenStreetMap",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    } else {
        TextPreview(item = item)
    }
}

@Composable
private fun PdfPreview(item: LifeItem) {
    val context = LocalContext.current
    val rawPdfUrl = remember(item.id, item.title, item.body) { item.inferPdfUrl() }
    val pdfUrl = rememberDecryptedMediaUri(rawPdfUrl)
    val thumbhashPreview = remember(item.id, item.title, item.body) { item.thumbhashPreviewPalette() }
    val thumbUrl = rememberPdfThumbnail(item)
    val displayUrl = thumbUrl ?: pdfUrl

    val imageRequest = remember(displayUrl) {
        displayUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(600, 600)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        }
    }
    val painter = rememberAsyncImagePainter(model = imageRequest)
    val painterState = painter.state

    Box(modifier = Modifier.fillMaxSize()) {
        if (displayUrl != null) {
            Image(
                painter = painter,
                contentDescription = "PDF preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            when (painterState) {
                is coil.compose.AsyncImagePainter.State.Loading ->
                    ThumbhashShimmerPlaceholder(
                        preview = thumbhashPreview,
                        isVideo = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                else -> {}
            }
        } else if (rawPdfUrl != null) {
            ThumbhashShimmerPlaceholder(
                preview = thumbhashPreview,
                isVideo = false,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PictureAsPdf,
                contentDescription = "PDF document",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = "View PDF",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}
