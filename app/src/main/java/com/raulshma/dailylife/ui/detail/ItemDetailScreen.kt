package com.raulshma.dailylife.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.raulshma.dailylife.ui.components.CompletionRipple
import com.raulshma.dailylife.ui.components.ShimmerBox
import com.raulshma.dailylife.ui.inferLocationPreview
import com.raulshma.dailylife.ui.rememberDecryptedMediaUri
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.staggerDelay
import kotlin.random.Random
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CompletionTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ItemDetailScreen(
    item: LifeItem,
    globalSettings: NotificationSettings,
    navigableItemIds: List<Long> = emptyList(),
    onBack: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onCompleted: () -> Unit,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToItem: (Long) -> Unit = {},
    onViewHistory: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val occurrenceStats = item.occurrenceStats()
    val hasVisualMedia = item.inferImagePreviewUrl() != null ||
        item.inferVideoPlaybackUrl() != null ||
        item.inferLocationPreview() != null
    val hasVideoMedia = item.inferVideoPlaybackUrl() != null
    val hasAudioMedia = item.inferAudioUrl() != null

    var contentVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }
    var heavyReady by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val screenHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val detailsProgress by animateFloatAsState(
        targetValue = if (showDetails) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = LinearOutSlowInEasing),
        label = "detailsProgress",
    )
    var dragAccumulator by remember { mutableStateOf(0f) }
    var dragVisualOffsetPx by remember { mutableStateOf(0f) }
    var chromeInteractionTick by remember { mutableStateOf(0) }
    var visualBrightnessHint by remember(item.id) { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()
    var horizontalDragOffset by remember(item.id) { mutableStateOf(0f) }
    var totalDx by remember { mutableStateOf(0f) }
    var totalDy by remember { mutableStateOf(0f) }
    var dragAxisLocked by remember { mutableStateOf(false) }
    var isHorizontalDrag by remember { mutableStateOf(false) }
    var justCompleted by remember { mutableStateOf(false) }
    var isZoomedIn by remember { mutableStateOf(false) }
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val registerChromeInteraction: () -> Unit = {
        chromeVisible = true
        chromeInteractionTick += 1
    }

    val dismissNestedScrollConnection = rememberDismissNestedScrollConnection(
        onDismiss = {
            showDetails = false
            registerChromeInteraction()
        },
    )

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
        fadeIn(animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)) +
            slideInVertically(animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)) { it / 10 } +
            androidx.compose.animation.scaleIn(initialScale = 0.96f, animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing))
    }
    val chromeExit = remember {
        fadeOut(animationSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing)) +
            slideOutVertically(animationSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing)) { -it / 16 }
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

    LaunchedEffect(Unit) {
        delay(DailyLifeDuration.SHORT.toLong())
        heavyReady = true
    }

    LaunchedEffect(chromeVisible, showDetails, item.id, chromeInteractionTick) {
        if (chromeVisible && !showDetails) {
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
            .pointerInput(showDetails) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    dragAxisLocked = false
                    isHorizontalDrag = false
                    dragAccumulator = 0f
                    totalDx = 0f
                    totalDy = 0f

                    val pointerId = down.id
                    var previousPosition = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers =
                            event.changes.count { it.pressed }

                        if (activePointers >= 2 || isZoomedIn) {
                            // multi-touch (pinch-to-zoom) or zoomed-in pan:
                            // pass events through, don't consume
                            continue
                        }

                        val change =
                            event.changes.firstOrNull { it.id == pointerId }

                        if (change == null || !change.pressed) {
                            // pointer up — handle drag end
                            if (isHorizontalDrag) {
                                val swipeThreshold =
                                    size.width * 0.25f
                                val currentIndex =
                                    navigableItemIds.indexOf(item.id)
                                val navigateTo = when {
                                    horizontalDragOffset < -swipeThreshold && currentIndex != -1 && currentIndex < navigableItemIds.lastIndex -> navigableItemIds[currentIndex + 1]
                                    horizontalDragOffset > swipeThreshold && currentIndex != -1 && currentIndex > 0 -> navigableItemIds[currentIndex - 1]
                                    else -> null
                                }
                                if (navigateTo != null) {
                                    onNavigateToItem(navigateTo)
                                } else {
                                    scope.launch {
                                        Animatable(horizontalDragOffset).animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = 220,
                                                easing = LinearOutSlowInEasing,
                                            ),
                                        ) {
                                            horizontalDragOffset = value
                                        }
                                    }
                                }
                            } else {
                                 when {
                                    dragAccumulator <= -120f && !showDetails -> {
                                        showDetails = true
                                        chromeVisible = true
                                        dragVisualOffsetPx = 0f
                                    }

                                    dragAccumulator >= 220f && !showDetails && !chromeVisible -> {
                                        onBack()
                                    }

                                     abs(dragAccumulator) >= 24f && !showDetails -> {
                                        chromeVisible =
                                            dragAccumulator < 0f
                                        chromeInteractionTick += 1
                                    }
                                }
                                dragAccumulator = 0f
                                dragVisualOffsetPx = 0f
                            }
                            dragAxisLocked = false
                            break
                        }

                        val dragAmount =
                            change.position - previousPosition
                        previousPosition = change.position

                        if (!dragAxisLocked) {
                            totalDx += dragAmount.x
                            totalDy += dragAmount.y
                            if (kotlin.math.abs(totalDx) > kotlin.math.abs(totalDy) * 1.5f && kotlin.math.abs(totalDx) > 10f) {
                                dragAxisLocked = true
                                isHorizontalDrag = true
                            } else if (kotlin.math.abs(totalDy) > kotlin.math.abs(totalDx) * 1.5f && kotlin.math.abs(totalDy) > 10f) {
                                dragAxisLocked = true
                                isHorizontalDrag = false
                            }
                        }

                        if (dragAxisLocked) {
                            change.consume()
                            if (isHorizontalDrag) {
                                horizontalDragOffset += dragAmount.x
                            } else {
                                dragAccumulator += dragAmount.y
                                if (dragAmount.y < -2f) {
                                    chromeVisible = true
                                }
                                if (!showDetails) {
                                    if (dragAmount.y > 0f && !chromeVisible) {
                                        dragVisualOffsetPx =
                                            (dragVisualOffsetPx + (dragAmount.y * 0.9f)).coerceAtMost(360f)
                                    } else if (dragAmount.y < 0f) {
                                        dragVisualOffsetPx =
                                            (dragVisualOffsetPx + (dragAmount.y * 0.6f)).coerceAtLeast(0f)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .graphicsLayer {
                translationX = horizontalDragOffset
            }
    ) {
        AttachmentHeroSection(
            item = item,
            onVisualBrightnessMeasured = { measured ->
                visualBrightnessHint = measured
            },
            heavyReady = heavyReady,
            onZoomChanged = { zoomed -> isZoomedIn = zoomed },
            modifier = Modifier
                .then(mediaSharedModifier)
                .fillMaxSize()
                .graphicsLayer {
                    // Push hero up when details are revealed (Google Photos style)
                    val heroTargetY = -screenHeightPx * 0.48f
                    translationY = dragVisualOffset + (heroTargetY - dragVisualOffset) * detailsProgress
                    val dragScale = 1f - (dragProgress * 0.07f)
                    val detailsScale = 0.94f
                    val s = dragScale + (detailsScale - dragScale) * detailsProgress
                    scaleX = s
                    scaleY = s
                    val dragAlpha = 1f - (dragProgress * 0.08f)
                    val detailsAlpha = 0.90f
                    alpha = dragAlpha + (detailsAlpha - dragAlpha) * detailsProgress
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
                        translationY = dragVisualOffset * 0.34f + ((-screenHeightPx * 0.48f * 0.34f) - dragVisualOffset * 0.34f) * detailsProgress
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
                            justCompleted = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }) {
                            val checkScale by animateFloatAsState(
                                targetValue = if (justCompleted) 1.35f else 1f,
                                animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Bouncy,
                                label = "completeScale",
                            )
                            val checkTint by animateColorAsState(
                                targetValue = if (justCompleted) Color(0xFF4CAF50) else Color.White,
                                animationSpec = tween(durationMillis = 300),
                                label = "completeTint",
                            )
                            Box(contentAlignment = Alignment.Center) {
                                CompletionRipple(triggered = justCompleted)
                                Icon(
                                    Icons.Filled.Done,
                                    contentDescription = "Mark complete",
                                    tint = checkTint,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = checkScale
                                            scaleY = checkScale
                                        }
                                        .size(24.dp),
                                )
                            }
                        }

                        LaunchedEffect(justCompleted) {
                            if (justCompleted) {
                                delay(800L)
                                justCompleted = false
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            showDetails = true
                            registerChromeInteraction()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Swipe up for details", color = Color.White)
                    }
                }
            }
        }

        if (!showDetails) {
            AnimatedVisibility(
                visible = !chromeVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)) +
                    slideInVertically(animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing)) { it / 3 },
                exit = fadeOut(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 170, easing = FastOutLinearInEasing)) { it / 3 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (hasVideoMedia) 64.dp else 10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.46f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            showDetails = true
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
            visible = !chromeVisible && !showDetails,
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

        AnimatedVisibility(
            visible = showDetails,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 320, easing = LinearOutSlowInEasing),
            ) { it },
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing),
            ) { it },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.54f)
                .align(Alignment.BottomCenter),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(dismissNestedScrollConnection),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                                ),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp),
                    ) {
                        DetailContentSection(
                            item = item,
                            occurrenceStats = occurrenceStats,
                            globalSettings = globalSettings,
                            contentVisible = contentVisible,
                            onNotificationsChanged = onNotificationsChanged,
                            onViewHistory = onViewHistory,
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun AttachmentHeroSection(
    item: LifeItem,
    onVisualBrightnessMeasured: (Float?) -> Unit,
    heavyReady: Boolean,
    onZoomChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val imageUrl = item.inferImagePreviewUrl()
    val videoUrl = item.inferVideoPlaybackUrl()
    val audioUrl = item.inferAudioUrl()
    val location = item.inferLocationPreview()

    val decryptedImage = rememberDecryptedMediaUri(imageUrl)
    val decryptedVideo = rememberDecryptedMediaUri(videoUrl)
    val decryptedAudio = rememberDecryptedMediaUri(audioUrl)
    val thumbhashPreview = remember(item.id, item.title, item.body) {
        item.thumbhashPreview()
    }
    val waitingForVisualMedia = (imageUrl != null || videoUrl != null) &&
        decryptedImage == null &&
        decryptedVideo == null

    val hasVisual = decryptedImage != null || decryptedVideo != null || location != null
    val displayBody = item.displayBody()
    val hasText = displayBody.isNotBlank()

    if (decryptedAudio != null && !hasVisual) {
        onVisualBrightnessMeasured(null)
    }

    Column(modifier = modifier) {
        if (decryptedAudio != null && hasVisual) {
            DetailAudioPlayer(
                audioUrl = decryptedAudio,
                title = item.title,
                isPlaying = null,
                onPlayingChanged = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                waitingForVisualMedia -> {
                    onVisualBrightnessMeasured(thumbhashPreview?.luminance ?: 0.58f)
                    ThumbhashLoadingPlaceholder(
                        preview = thumbhashPreview,
                        isVideo = videoUrl != null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                decryptedImage != null -> {
                    var scale by remember(decryptedImage) { mutableStateOf(1f) }
                    var offset by remember(decryptedImage) { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .pointerInput(decryptedImage) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                                    val zoomed = newScale > 1.05f
                                    onZoomChanged(zoomed)
                                    if (!zoomed) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = newScale
                                        offset += pan
                                    }
                                }
                            },
                    ) {
                        SubcomposeAsyncImage(
                            model = decryptedImage,
                            contentDescription = "Image preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                },
                            contentScale = ContentScale.Fit,
                            loading = {
                                ThumbhashLoadingPlaceholder(
                                    preview = thumbhashPreview,
                                    isVideo = false,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            },
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
                    onVisualBrightnessMeasured(thumbhashPreview?.luminance)
                    if (heavyReady) {
                        DetailVideoPlayer(
                            videoUrl = decryptedVideo,
                            onZoomChanged = onZoomChanged,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        ThumbhashLoadingPlaceholder(
                            preview = thumbhashPreview,
                            isVideo = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                location != null -> {
                    onVisualBrightnessMeasured(0.56f)
                    if (heavyReady) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                        ) {
                            com.raulshma.dailylife.ui.OpenStreetMapPreview(
                                latitude = location.first,
                                longitude = location.second,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }

                else -> {
                    onVisualBrightnessMeasured(0.62f)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (decryptedAudio != null) {
                            var isPlaying by remember { mutableStateOf(false) }
                            DetailAudioPlayer(
                                audioUrl = decryptedAudio,
                                title = item.title,
                                isPlaying = isPlaying,
                                onPlayingChanged = { isPlaying = it },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (hasText) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        if (hasText) {
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
    }
}

private data class ThumbhashPreview(
    val colors: List<Color>,
    val luminance: Float,
)

@Composable
private fun ThumbhashLoadingPlaceholder(
    preview: ThumbhashPreview?,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    val fallbackColors = if (isVideo) {
        listOf(
            Color(0xFF0F1114),
            Color(0xFF171A20),
            Color(0xFF0F1114),
        )
    } else {
        listOf(
            Color(0xFF171717),
            Color(0xFF222222),
            Color(0xFF171717),
        )
    }
    val colors = preview?.colors?.takeIf { it.size >= 3 } ?: fallbackColors

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = colors,
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp),
            baseColor = Color.Transparent,
            highlightColor = Color.White.copy(alpha = 0.12f),
        )

        if (isVideo) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(46.dp),
            )
        }
    }
}

private fun LifeItem.thumbhashPreview(): ThumbhashPreview? {
    val token = extractThumbhashToken() ?: return null
    val bytes = decodeThumbhashBytes(token) ?: return null
    if (bytes.isEmpty()) return null

    fun channelAt(index: Int): Float =
        (bytes[index % bytes.size].toInt() and 0xFF) / 255f

    val c1 = Color(channelAt(0), channelAt(1), channelAt(2), 1f)
    val c2 = Color(channelAt(3), channelAt(4), channelAt(5), 1f)
    val c3 = Color(channelAt(6), channelAt(7), channelAt(8), 1f)
    val luma = ((0.2126f * c1.red) + (0.7152f * c1.green) + (0.0722f * c1.blue))
        .coerceIn(0f, 1f)

    return ThumbhashPreview(
        colors = listOf(c1, c2, c3),
        luminance = luma,
    )
}

private fun LifeItem.extractThumbhashToken(): String? {
    val source = listOf(title, body).joinToString(" ")
    val queryPattern = Regex("""(?:[?&]|\b)(?:thumbhash|thumb_hash|thumb)=(?<value>[^&#\s]+)""", RegexOption.IGNORE_CASE)
    val inlinePattern = Regex("""\bthumbhash\s*[:=]\s*(?<value>[A-Za-z0-9_\-+/=]+)""", RegexOption.IGNORE_CASE)
    val queryMatch = queryPattern.find(source)?.groups?.get("value")?.value
    if (!queryMatch.isNullOrBlank()) return queryMatch
    val inlineMatch = inlinePattern.find(source)?.groups?.get("value")?.value
    return inlineMatch?.takeIf { it.isNotBlank() }
}

private fun decodeThumbhashBytes(raw: String): ByteArray? {
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
private fun DetailVideoPlayer(videoUrl: String, onZoomChanged: (Boolean) -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val parsedVideoUri = remember(videoUrl) { Uri.parse(videoUrl) }
    val localVideoFile = remember(videoUrl) { resolveLocalMediaFile(context, parsedVideoUri) }
    var isPlaying by remember { mutableStateOf(false) }
    var userPaused by remember { mutableStateOf(false) }
    var hadPlaybackError by remember { mutableStateOf(false) }
    var durationMs by remember { mutableStateOf(0L) }
    var positionMs by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableStateOf(0L) }
    var scale by remember(videoUrl) { mutableStateOf(1f) }
    var offset by remember(videoUrl) { mutableStateOf(Offset.Zero) }

    val mediaUri = remember(localVideoFile, parsedVideoUri) {
        localVideoFile?.let(Uri::fromFile) ?: parsedVideoUri
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(mediaUri, lifecycleOwner) {
        val player = ExoPlayer.Builder(context).build().apply {
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
                    userPaused = false
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
        exoPlayer = player
        playerView?.player = player

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                    isPlaying = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!hadPlaybackError && !userPaused) {
                        player.playWhenReady = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerView?.player = null
            exoPlayer = null
            player.release()
        }
    }

    LaunchedEffect(exoPlayer, isSeeking) {
        val player = exoPlayer ?: return@LaunchedEffect
        while (true) {
            durationMs = player.duration.coerceAtLeast(0L)
            if (!isSeeking) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(videoUrl) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        val zoomed = newScale > 1.05f
                        onZoomChanged(zoomed)
                        if (!zoomed) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = newScale
                            offset += pan
                        }
                    }
                },
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        playerView = this
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                }
            )
        }

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
                    exoPlayer?.playWhenReady = true
                    userPaused = false
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.70f),
                        ),
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val player = exoPlayer ?: return@FilledTonalIconButton
                        if (player.isPlaying) {
                            player.pause()
                            userPaused = true
                        } else {
                            player.play()
                            userPaused = false
                        }
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(18.dp),
                    )
                }

                Slider(
                    modifier = Modifier.weight(1f).height(16.dp),
                    value = sliderValue,
                    onValueChange = { value ->
                        isSeeking = true
                        seekPositionMs = value.toLong().coerceIn(0L, durationMs.coerceAtLeast(0L))
                    },
                    onValueChangeFinished = {
                        val target = seekPositionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))
                        exoPlayer?.seekTo(target)
                        positionMs = target
                        isSeeking = false
                    },
                    valueRange = 0f..sliderMax,
                )

                FilledTonalIconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Open with",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
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
private fun DetailAudioPlayer(
    audioUrl: String,
    title: String,
    isPlaying: Boolean?,
    onPlayingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var localIsPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    val barCount = 48
    var waveformBars by remember { mutableStateOf<FloatArray?>(null) }

    val effectiveIsPlaying = isPlaying ?: localIsPlaying

    DisposableEffect(audioUrl) {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(context, Uri.parse(audioUrl))
            mp.prepare()
            duration = mp.duration
            mediaPlayer = mp
        } catch (_: Exception) {
            mp.release()
        }
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying == true || localIsPlaying) stop()
                release()
            }
            mediaPlayer = null
            duration = 0
            currentPosition = 0
            localIsPlaying = false
            if (isPlaying != null) onPlayingChanged(false)
        }
    }

    LaunchedEffect(audioUrl) {
        val bars = AudioWaveformGenerator().generateWaveform(context, Uri.parse(audioUrl), barCount)
        waveformBars = bars
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val mp = mediaPlayer
                        if (mp == null) return@FilledTonalIconButton
                        if (mp.isPlaying) {
                            mp.pause()
                            localIsPlaying = false
                            onPlayingChanged(false)
                        } else {
                            mp.start()
                            localIsPlaying = true
                            onPlayingChanged(true)
                        }
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = if (effectiveIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (effectiveIsPlaying) "Pause" else "Play",
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

            WaveformVisualizer(
                bars = waveformBars,
                barCount = barCount,
                isPlaying = effectiveIsPlaying,
                progress = if (duration > 0) currentPosition / duration.toFloat() else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )
        }
    }

    LaunchedEffect(effectiveIsPlaying) {
        while (effectiveIsPlaying) {
            mediaPlayer?.let { mp ->
                currentPosition = mp.currentPosition
            }
            delay(200L)
        }
    }
}

@Composable
private fun WaveformVisualizer(
    bars: FloatArray?,
    barCount: Int,
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val randomOffsets = remember(barCount) {
        FloatArray(barCount) { 0.6f + 0.4f * Random.nextFloat() }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (bars != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                bars.forEachIndexed { index, amplitude ->
                    val isPast = index / barCount.toFloat() <= progress
                    val targetHeightRatio = if (isPlaying) {
                        (amplitude * randomOffsets[index]).coerceIn(0.05f, 1f)
                    } else {
                        amplitude.coerceIn(0.05f, 1f)
                    }
                    val animatedHeight by animateFloatAsState(
                        targetValue = targetHeightRatio,
                        animationSpec = tween(durationMillis = if (isPlaying) 180 else 300, easing = LinearOutSlowInEasing),
                        label = "waveformBar$index",
                    )
                    val barColor = if (isPast) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(999.dp))
                            .background(barColor),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(barCount) { index ->
                    val placeholderAmp = 0.3f + 0.2f * kotlin.math.sin(index * 0.5f)
                    val targetHeightRatio = if (isPlaying) {
                        (placeholderAmp * randomOffsets[index]).coerceIn(0.1f, 0.8f)
                    } else {
                        placeholderAmp.coerceIn(0.1f, 0.5f)
                    }
                    val animatedHeight by animateFloatAsState(
                        targetValue = targetHeightRatio,
                        animationSpec = tween(durationMillis = if (isPlaying) 180 else 300, easing = LinearOutSlowInEasing),
                        label = "waveformPlaceholder$index",
                    )
                    val isPast = index / barCount.toFloat() <= progress
                    val barColor = if (isPast) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(999.dp))
                            .background(barColor),
                    )
                }
            }
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
    onViewHistory: () -> Unit = {},
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

        if (item.completionHistory.isNotEmpty()) {
            HorizontalDivider()

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade<Float>()) + slideInVertically(
                    DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Completion History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    item.completionHistory
                        .sortedByDescending { it.completedAt }
                        .take(3)
                        .forEach { record ->
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (record.missed) {
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (record.missed) "Missed" else "Completed",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (record.missed) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                        )
                                        Text(
                                            text = record.completedAt.format(CompletionTimeFormatter),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (record.latitude != null && record.longitude != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    text = "%.2f, %.2f".format(record.latitude, record.longitude),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        if (record.batteryLevel != null) {
                                            Text(
                                                text = "Battery: ${record.batteryLevel}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    if (item.completionHistory.size > 3) {
                        TextButton(
                            onClick = onViewHistory,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("View all ${item.completionHistory.size} entries")
                        }
                    } else {
                        TextButton(
                            onClick = onViewHistory,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("View history")
                        }
                    }
                }
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

@Composable
private fun rememberDismissNestedScrollConnection(
    onDismiss: () -> Unit,
    dismissThresholdPx: Float = 120f,
): NestedScrollConnection {
    var accumulatedDrag by remember { mutableStateOf(0f) }
    return remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y > 0) {
                        accumulatedDrag += available.y
                        if (accumulatedDrag > dismissThresholdPx) {
                            onDismiss()
                            accumulatedDrag = 0f
                        }
                        return available
                    } else {
                        accumulatedDrag = 0f
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (available.y > 0 && accumulatedDrag > dismissThresholdPx * 0.4f) {
                    onDismiss()
                    accumulatedDrag = 0f
                }
                return Velocity.Zero
            }
        }
    }
}
