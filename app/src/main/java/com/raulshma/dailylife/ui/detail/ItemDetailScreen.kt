package com.raulshma.dailylife.ui.detail

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.SubcomposeAsyncImage
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.OccurrenceStats
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.DateFormatter
import com.raulshma.dailylife.ui.TimestampFormatter
import com.raulshma.dailylife.ui.TypeBadge
import com.raulshma.dailylife.ui.inferLocationPreview
import com.raulshma.dailylife.ui.isMediaLike
import com.raulshma.dailylife.ui.LocalAnimatedVisibilityScope
import com.raulshma.dailylife.ui.LocalSharedTransitionScope
import com.raulshma.dailylife.ui.rememberDecryptedMediaUri
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.staggerDelay
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    item: LifeItem,
    globalSettings: NotificationSettings,
    onBack: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onCompleted: () -> Unit,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val haptic = LocalHapticFeedback.current
    val occurrenceStats = item.occurrenceStats()

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    var contentVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(DailyLifeDuration.SHORT.toLong())
        contentVisible = true
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete item") },
            text = { Text("Are you sure you want to delete \"${item.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LargeTopAppBar(
            title = {
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                AnimatedActionButton(
                    icon = Icons.Filled.PushPin,
                    contentDescription = if (item.isPinned) "Unpin" else "Pin",
                    tint = if (item.isPinned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onPinnedToggled,
                )
                AnimatedActionButton(
                    icon = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                    tint = if (item.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFavoriteToggled()
                    },
                    animate = item.isFavorite,
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit item")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete item")
                }
                IconButton(onClick = onCompleted) {
                    Icon(Icons.Filled.Done, contentDescription = "Mark complete")
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ),
            modifier = Modifier.statusBarsPadding(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            val hasMediaContent = item.type.isMediaLike() ||
                item.inferImagePreviewUrl() != null ||
                item.inferVideoPlaybackUrl() != null ||
                item.inferAudioUrl() != null ||
                item.inferLocationPreview() != null
            if (hasMediaContent) {
                AttachmentHeroSection(item = item)
                Spacer(modifier = Modifier.height(20.dp))
            }

            DetailContentSection(
                item = item,
                occurrenceStats = occurrenceStats,
                globalSettings = globalSettings,
                contentVisible = contentVisible,
                onNotificationsChanged = onNotificationsChanged,
            )
        }
    }
}

@Composable
private fun AttachmentHeroSection(item: LifeItem) {
    val context = LocalContext.current
    val imageUrl = item.inferImagePreviewUrl()
    val videoUrl = item.inferVideoPlaybackUrl()
    val audioUrl = item.inferAudioUrl()
    val location = item.inferLocationPreview()

    val decryptedImage = rememberDecryptedMediaUri(imageUrl)
    val decryptedVideo = rememberDecryptedMediaUri(videoUrl)
    val decryptedAudio = rememberDecryptedMediaUri(audioUrl)

    when {
        decryptedImage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        decryptedImage.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(intent)
                        }
                    },
            ) {
                SubcomposeAsyncImage(
                    model = decryptedImage,
                    contentDescription = "Image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "View full image",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        decryptedVideo != null -> {
            DetailVideoPlayer(videoUrl = decryptedVideo)
        }

        decryptedAudio != null -> {
            DetailAudioPlayer(audioUrl = decryptedAudio, title = item.title)
        }

        location != null -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        val gmmIntentUri = Uri.parse("geo:${location.first},${location.second}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(mapIntent) }
                            .recoverCatching {
                                val fallback = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(fallback)
                            }
                    },
            ) {
                com.raulshma.dailylife.ui.OpenStreetMapPreview(
                    latitude = location.first,
                    longitude = location.second,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Open in maps",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        else -> {
            val displayBody = item.displayBody()
            if (displayBody.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = displayBody,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val parsedVideoUri = remember(videoUrl) { Uri.parse(videoUrl) }
    val localVideoFile = remember(videoUrl) { resolveLocalMediaFile(context, parsedVideoUri) }
    var isPlaying by remember { mutableStateOf(false) }
    var hadPlaybackError by remember { mutableStateOf(false) }

    val mediaUri = remember(localVideoFile, parsedVideoUri) {
        localVideoFile?.let(Uri::fromFile) ?: parsedVideoUri
    }

    val exoPlayer = remember(mediaUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    hadPlaybackError = true
                    isPlaying = false
                }
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            update = { view ->
                view.player = exoPlayer
            }
        )

        if (hadPlaybackError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Playback unavailable",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (!isPlaying) {
            FilledTonalIconButton(
                onClick = {
                    exoPlayer.playWhenReady = true
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        ) {
            FilledTonalIconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        ) {
            FilledTonalIconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        isPlaying = false
                    } else {
                        exoPlayer.play()
                        isPlaying = true
                    }
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        DisposableEffect(exoPlayer, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        exoPlayer.pause()
                        isPlaying = false
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        if (!hadPlaybackError) {
                            exoPlayer.playWhenReady = true
                            isPlaying = exoPlayer.isPlaying
                        }
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }
    }
}

private fun resolveLocalMediaFile(context: android.content.Context, uri: Uri): File? {
    return when (uri.scheme) {
        "file" -> uri.path?.let { File(it) }?.takeIf { it.exists() }
        "content" -> {
            runCatching {
                val projection = arrayOf(android.provider.MediaStore.MediaColumns.DATA)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(0)
                        if (path != null) {
                            val f = File(path)
                            if (f.exists()) return f
                        }
                    }
                }
            }

            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                val possiblePath = segments.joinToString("/")

                val cacheFile = File(context.cacheDir, possiblePath)
                if (cacheFile.exists()) return cacheFile

                if (segments.first() == "cache_media") {
                    val relative = segments.drop(1).joinToString("/")
                    val mappedCacheFile = File(context.cacheDir, "media/$relative")
                    if (mappedCacheFile.exists()) return mappedCacheFile
                }

                val filesFile = File(context.filesDir, possiblePath)
                if (filesFile.exists()) return filesFile
            }

            null
        }
        else -> null
    }
}

@Composable
private fun DetailAudioPlayer(audioUrl: String, title: String) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    LaunchedEffect(audioUrl) {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(context, Uri.parse(audioUrl))
            mp.prepare()
            duration = mp.duration
            mediaPlayer = mp
        } catch (_: Exception) {
            mp.release()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val mp = mediaPlayer
                        if (mp == null) return@FilledTonalIconButton
                        if (mp.isPlaying) {
                            mp.pause()
                            isPlaying = false
                        } else {
                            mp.start()
                            isPlaying = true
                        }
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val elapsed = currentPosition / 1000
                    val total = duration / 1000
                    Text(
                        text = "${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')} / ${total / 60}:${(total % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(audioUrl))
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Open externally",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { mp ->
                currentPosition = mp.currentPosition
            }
            delay(200L)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContentSection(
    item: LifeItem,
    occurrenceStats: OccurrenceStats,
    globalSettings: NotificationSettings,
    contentVisible: Boolean,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(DailyLifeTween.fade<Float>()) + slideInVertically(
                DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
                initialOffsetY = { it / 3 }
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TypeBadge(type = item.type)
                Column {
                    Text(
                        text = item.type.label,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = item.createdAt.format(TimestampFormatter),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        if (item.displayBody().isNotBlank()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item.displayBody(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (item.tags.isNotEmpty()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item.tags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text("#$tag") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        val metadataItems = buildList {
            add("Favorite" to if (item.isFavorite) "Yes" else "No")
            add("Pinned" to if (item.isPinned) "Yes" else "No")
            item.taskStatus?.let { add("Task status" to it.label) }
            item.reminderAt?.let { add("Reminder" to it.format(TimestampFormatter)) }
            if (item.isRecurring) {
                add("Recurrence" to item.recurrenceRule.frequency.label)
            }
            add("Completions" to occurrenceStats.completedCount.toString())
            if (item.isRecurring || occurrenceStats.missedCount > 0) {
                add("Missed" to occurrenceStats.missedCount.toString())
                add("Current streak" to occurrenceStats.currentStreak.toString())
            }
        }

        metadataItems.forEachIndexed { index, (label, value) ->
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(
                    androidx.compose.animation.core.tween(
                        durationMillis = DailyLifeDuration.SHORT,
                        delayMillis = staggerDelay(index, baseDelayMs = 40),
                        easing = DailyLifeEasing.Enter,
                    )
                ) + slideInVertically(
                    androidx.compose.animation.core.tween(
                        durationMillis = DailyLifeDuration.MEDIUM,
                        delayMillis = staggerDelay(index, baseDelayMs = 40),
                        easing = DailyLifeEasing.Enter,
                    ),
                    initialOffsetY = { it / 4 }
                ),
            ) {
                DetailLine(label = label, value = value)
            }
        }

        val effectiveTime = item.notificationSettings.timeOverride
            ?: globalSettings.preferredTime
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(
                androidx.compose.animation.core.tween(
                    durationMillis = DailyLifeDuration.SHORT,
                    delayMillis = staggerDelay(metadataItems.size, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                )
            ) + slideInVertically(
                androidx.compose.animation.core.tween(
                    durationMillis = DailyLifeDuration.MEDIUM,
                    delayMillis = staggerDelay(metadataItems.size, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                ),
                initialOffsetY = { it / 4 }
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (item.notificationSettings.enabled) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Item notifications at ${effectiveTime.format(com.raulshma.dailylife.ui.TimeFormatter)}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = item.notificationSettings.enabled,
                    onCheckedChange = {
                        onNotificationsChanged(item.notificationSettings.copy(enabled = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AnimatedActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    animate: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (animate) 1.2f else 1f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Bouncy,
        label = "actionScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (animate) 0f else -15f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Bouncy,
        label = "actionRotation"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
                .size(24.dp)
        )
    }
}
