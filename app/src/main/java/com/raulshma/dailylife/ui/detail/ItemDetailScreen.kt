package com.raulshma.dailylife.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.SubcomposeAsyncImage
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.OccurrenceStats
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.LocalAnimatedVisibilityScope
import com.raulshma.dailylife.ui.LocalSharedTransitionScope
import com.raulshma.dailylife.ui.TimestampFormatter
import com.raulshma.dailylife.ui.TypeBadge
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.inferLocationPreview
import com.raulshma.dailylife.ui.rememberDecryptedMediaUri
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.staggerDelay
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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
    val haptic = LocalHapticFeedback.current
    val occurrenceStats = item.occurrenceStats()
    val hasVisualMedia = item.inferImagePreviewUrl() != null ||
        item.inferVideoPlaybackUrl() != null ||
        item.inferLocationPreview() != null
    val hasAudioMedia = item.inferAudioUrl() != null

    var contentVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var dragAccumulator by remember { mutableStateOf(0f) }
    var dragVisualOffsetPx by remember { mutableStateOf(0f) }
    var chromeInteractionTick by remember { mutableStateOf(0) }
    var visualBrightnessHint by remember(item.id) { mutableStateOf<Float?>(null) }
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val registerChromeInteraction: () -> Unit = {
        chromeVisible = true
        chromeInteractionTick += 1
    }

    val mediaSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.media(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }
    val titleSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.title(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }
    val badgeSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.typeBadge(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }

    val chromeEnter = remember {
        fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)) +
            slideInVertically(animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing)) { it / 10 }
    }
    val chromeExit = remember {
        fadeOut(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) +
            slideOutVertically(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) { -it / 16 }
    }

    val fallbackBrightness = when {
        hasVisualMedia -> 0.62f
        hasAudioMedia -> 0.54f
        else -> MaterialTheme.colorScheme.background.luminance().coerceIn(0.35f, 0.8f)
    }
    val scrimBrightness = (visualBrightnessHint ?: fallbackBrightness).coerceIn(0f, 1f)
    val topScrimStrong = (0.30f + (scrimBrightness * 0.52f)).coerceIn(0.30f, 0.84f)
    val topScrimWeak = (topScrimStrong * 0.42f).coerceIn(0.14f, 0.40f)
    val bottomScrimMid = (topScrimStrong * 0.55f).coerceIn(0.20f, 0.52f)
    val bottomScrimStrong = (topScrimStrong * 0.92f).coerceIn(0.28f, 0.84f)
    val isWideLayout = LocalConfiguration.current.screenWidthDp >= 700

    val horizontalChromePadding: Dp = if (isWideLayout) 24.dp else 8.dp
    val topChromeMaxWidth: Dp = if (isWideLayout) 980.dp else 10_000.dp
    val bottomChromeMaxWidth: Dp = if (isWideLayout) 760.dp else 10_000.dp
    val dragVisualOffset = animateFloatAsState(
        targetValue = dragVisualOffsetPx,
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        label = "dragVisualOffset",
    ).value
    val dragProgress = (dragVisualOffset / 700f).coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
        delay(DailyLifeDuration.SHORT.toLong())
        contentVisible = true
    }

    LaunchedEffect(chromeVisible, showDetailsSheet, item.id, chromeInteractionTick) {
        if (chromeVisible && !showDetailsSheet) {
            delay(2800L)
            chromeVisible = false
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(showDetailsSheet) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount: Float ->
                        dragAccumulator += dragAmount
                        if (dragAmount < -2f) {
                            chromeVisible = true
                        }
                        if (!showDetailsSheet) {
                            if (dragAmount > 0f && !chromeVisible) {
                                dragVisualOffsetPx = (dragVisualOffsetPx + (dragAmount * 0.9f)).coerceAtMost(360f)
                            } else if (dragAmount < 0f) {
                                dragVisualOffsetPx = (dragVisualOffsetPx + (dragAmount * 0.6f)).coerceAtLeast(0f)
                            }
                        }
                    },
                    onDragEnd = {
                        when {
                            dragAccumulator <= -120f && !showDetailsSheet -> {
                                showDetailsSheet = true
                                chromeVisible = true
                                dragVisualOffsetPx = 0f
                            }
                            dragAccumulator >= 220f && !showDetailsSheet && !chromeVisible -> {
                                onBack()
                            }
                            abs(dragAccumulator) >= 24f && !showDetailsSheet -> {
                                chromeVisible = dragAccumulator < 0f
                                chromeInteractionTick += 1
                            }
                        }
                        dragAccumulator = 0f
                        dragVisualOffsetPx = 0f
                    },
                    onDragCancel = {
                        dragAccumulator = 0f
                        dragVisualOffsetPx = 0f
                    },
                )
            }
    ) {
        AttachmentHeroSection(
            item = item,
            onVisualBrightnessMeasured = { measured ->
                visualBrightnessHint = measured
            },
            modifier = Modifier
                .then(mediaSharedModifier)
                .fillMaxSize()
                .graphicsLayer {
                    translationY = dragVisualOffset
                    val scale = 1f - (dragProgress * 0.07f)
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - (dragProgress * 0.08f)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    chromeVisible = !chromeVisible
                    chromeInteractionTick += 1
                },
        )

        AnimatedVisibility(
            visible = chromeVisible,
            enter = chromeEnter,
            exit = chromeExit,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = dragVisualOffset * 0.34f
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = topScrimStrong),
                                    Color.Black.copy(alpha = topScrimWeak),
                                    Color.Transparent,
                                ),
                                startY = 0f,
                                endY = if (isWideLayout) 340f else 280f,
                            )
                        )
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = topChromeMaxWidth)
                        .align(Alignment.Center)
                        .statusBarsPadding()
                        .padding(horizontal = horizontalChromePadding, vertical = if (isWideLayout) 8.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                    Text(
                        text = item.title,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .then(titleSharedModifier),
                    )
                    TypeBadge(type = item.type, modifier = badgeSharedModifier)
                }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = bottomScrimMid),
                                    Color.Black.copy(alpha = bottomScrimStrong),
                                ),
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = horizontalChromePadding, vertical = if (isWideLayout) 10.dp else 6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = bottomChromeMaxWidth)
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = if (isWideLayout) {
                            Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                        } else {
                            Arrangement.SpaceEvenly
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedActionButton(
                            icon = Icons.Filled.PushPin,
                            contentDescription = if (item.isPinned) "Unpin" else "Pin",
                            tint = if (item.isPinned) MaterialTheme.colorScheme.tertiary else Color.White,
                            onClick = {
                                registerChromeInteraction()
                                onPinnedToggled()
                            },
                        )
                        AnimatedActionButton(
                            icon = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                            tint = if (item.isFavorite) MaterialTheme.colorScheme.tertiary else Color.White,
                            onClick = {
                                registerChromeInteraction()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onFavoriteToggled()
                            },
                            animate = item.isFavorite,
                        )
                        IconButton(onClick = {
                            registerChromeInteraction()
                            onEdit()
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit item", tint = Color.White)
                        }
                        IconButton(onClick = {
                            registerChromeInteraction()
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete item", tint = Color.White)
                        }
                        IconButton(onClick = {
                            registerChromeInteraction()
                            onCompleted()
                        }) {
                            Icon(Icons.Filled.Done, contentDescription = "Mark complete", tint = Color.White)
                        }
                    }

                    TextButton(
                        onClick = {
                            showDetailsSheet = true
                            registerChromeInteraction()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Swipe up for details", color = Color.White)
                    }
                }
            }
        }

        if (!showDetailsSheet) {
            AnimatedVisibility(
                visible = !chromeVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)) +
                    slideInVertically(animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing)) { it / 3 },
                exit = fadeOut(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) { it / 3 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.46f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            showDetailsSheet = true
                            registerChromeInteraction()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.85f)),
                    )
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !chromeVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
        ) {
            Text(
                text = "Swipe down to go back",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }

    if (showDetailsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailsSheet = false
                registerChromeInteraction()
            },
            sheetState = detailsSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
            ) {
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
}

@Composable
private fun AttachmentHeroSection(
    item: LifeItem,
    onVisualBrightnessMeasured: (Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    .then(modifier)
                    .background(Color.Black),
            ) {
                SubcomposeAsyncImage(
                    model = decryptedImage,
                    contentDescription = "Image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    onSuccess = { state ->
                        onVisualBrightnessMeasured(drawableBrightnessLuminance(state.result.drawable))
                    },
                    onError = {
                        onVisualBrightnessMeasured(null)
                    },
                )
            }
        }

        decryptedVideo != null -> {
            onVisualBrightnessMeasured(null)
            DetailVideoPlayer(
                videoUrl = decryptedVideo,
                modifier = modifier,
            )
        }

        decryptedAudio != null -> {
            onVisualBrightnessMeasured(null)
            DetailAudioPlayer(
                audioUrl = decryptedAudio,
                title = item.title,
                modifier = modifier,
            )
        }

        location != null -> {
            onVisualBrightnessMeasured(0.56f)
            Box(
                modifier = Modifier
                    .then(modifier)
                    .background(Color.Black),
            ) {
                com.raulshma.dailylife.ui.OpenStreetMapPreview(
                    latitude = location.first,
                    longitude = location.second,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        else -> {
            onVisualBrightnessMeasured(0.62f)
            val displayBody = item.displayBody()
            if (displayBody.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .then(modifier)
                        .background(Color.Black)
                        .padding(16.dp),
                ) {
                    Text(
                        text = displayBody,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private fun drawableBrightnessLuminance(drawable: Drawable): Float? {
    return runCatching {
        val srcWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: 64
        val srcHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: 64
        val sampleWidth = srcWidth.coerceAtMost(48)
        val sampleHeight = srcHeight.coerceAtMost(48)

        val bitmap = Bitmap.createBitmap(sampleWidth, sampleHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sampleWidth, sampleHeight)
        drawable.draw(canvas)

        var totalLuminance = 0f
        var countedPixels = 0

        val pixels = IntArray(sampleWidth * sampleHeight)
        bitmap.getPixels(pixels, 0, sampleWidth, 0, 0, sampleWidth, sampleHeight)
        for (argb in pixels) {
            val alpha = (argb ushr 24) and 0xff
            if (alpha < 16) continue
            val r = ((argb ushr 16) and 0xff) / 255f
            val g = ((argb ushr 8) and 0xff) / 255f
            val b = (argb and 0xff) / 255f
            val pixelLuminance = (0.2126f * r) + (0.7152f * g) + (0.0722f * b)
            totalLuminance += pixelLuminance
            countedPixels += 1
        }

        bitmap.recycle()

        if (countedPixels == 0) null else (totalLuminance / countedPixels).coerceIn(0f, 1f)
    }.getOrNull()
}

@Composable
private fun DetailVideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val parsedVideoUri = remember(videoUrl) { Uri.parse(videoUrl) }
    val localVideoFile = remember(videoUrl) { resolveLocalMediaFile(context, parsedVideoUri) }
    var isPlaying by remember { mutableStateOf(false) }
    var hadPlaybackError by remember { mutableStateOf(false) }
    var durationMs by remember { mutableStateOf(0L) }
    var positionMs by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableStateOf(0L) }

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

                override fun onPlaybackStateChanged(state: Int) {
                    durationMs = duration.coerceAtLeast(0L)
                    if (!isSeeking) {
                        positionMs = currentPosition.coerceAtLeast(0L)
                        seekPositionMs = positionMs
                    }
                }
            })
        }
    }

    LaunchedEffect(exoPlayer, isSeeking) {
        while (true) {
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            if (!isSeeking) {
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                seekPositionMs = positionMs
            }
            delay(250L)
        }
    }

    val sliderMax = remember(durationMs) { durationMs.coerceAtLeast(1L).toFloat() }
    val sliderValue = if (isSeeking) seekPositionMs.toFloat() else positionMs.toFloat().coerceAtMost(sliderMax)

    Box(
        modifier = modifier
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 40.dp, end = 40.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Slider(
                modifier = Modifier.height(18.dp),
                value = sliderValue,
                onValueChange = { value ->
                    isSeeking = true
                    seekPositionMs = value.toLong().coerceIn(0L, durationMs.coerceAtLeast(0L))
                },
                onValueChangeFinished = {
                    val target = seekPositionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))
                    exoPlayer.seekTo(target)
                    positionMs = target
                    isSeeking = false
                },
                valueRange = 0f..sliderMax,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatPlaybackTime((if (isSeeking) seekPositionMs else positionMs) / 1000L),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f),
                )
                Text(
                    text = formatPlaybackTime(durationMs / 1000L),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f),
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
                    .size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
        ) {
            FilledTonalIconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen",
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
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
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(16.dp),
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

private fun formatPlaybackTime(totalSeconds: Long): String {
    val seconds = totalSeconds.coerceAtLeast(0L)
    val minutesPart = seconds / 60L
    val secondsPart = seconds % 60L
    return "$minutesPart:${secondsPart.toString().padStart(2, '0')}"
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
private fun DetailAudioPlayer(audioUrl: String, title: String, modifier: Modifier = Modifier) {
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
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
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
