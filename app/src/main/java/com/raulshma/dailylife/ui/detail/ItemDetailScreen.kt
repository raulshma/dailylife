package com.raulshma.dailylife.ui.detail

import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.raulshma.dailylife.domain.AIFeature
import com.raulshma.dailylife.domain.AIModelCapability
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.OccurrenceStats
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferPdfUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.LocalAnimatedVisibilityScope
import com.raulshma.dailylife.ui.LocalSharedTransitionScope
import com.raulshma.dailylife.ui.TimestampFormatter
import com.raulshma.dailylife.ui.TypeBadge
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.components.CompletionRipple
import com.raulshma.dailylife.ui.components.ShimmerBox
import com.raulshma.dailylife.ui.inferLocationMapTile
import com.raulshma.dailylife.ui.inferLocationPreview
import com.raulshma.dailylife.ui.rememberDecryptedMediaUri
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.media.UriFileResolver
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.staggerDelay
import kotlin.random.Random
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    onBodyChanged: ((String) -> Unit)? = null,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToItem: (Long) -> Unit = {},
    onViewHistory: () -> Unit = {},
    onStartFocusTimer: () -> Unit = {},
    isAiEnabled: Boolean = false,
    isFeatureAvailable: (com.raulshma.dailylife.domain.AIFeature) -> Boolean = { false },
    aiGeneratedTitle: String = "",
    aiGeneratedDescription: String = "",
    aiGeneratedTags: List<String> = emptyList(),
    aiMood: com.raulshma.dailylife.domain.MoodResult? = null,
    aiPhotoDescription: String = "",
    aiAudioSummary: String = "",
    isAiGenerating: Boolean = false,
    aiError: String? = null,
    onGenerateTitle: (String, String?, String?) -> Unit = { _, _, _ -> },
    onGenerateDescription: (String, String) -> Unit = { _, _ -> },
    onSuggestTags: (String, String) -> Unit = { _, _ -> },
    onAnalyzeMood: (String, String) -> Unit = { _, _ -> },
    onDescribePhoto: (String) -> Unit = {},
    onSummarizeAudio: (String) -> Unit = {},
    onApplyTitle: (Long, String) -> Unit = { _, _ -> },
    onApplyTags: (Long, Set<String>) -> Unit = { _, _ -> },
    onApplyDescription: (Long, String) -> Unit = { _, _ -> },
    onClearAiError: () -> Unit = {},
    onCancelAi: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val occurrenceStats = item.occurrenceStats()
    val hasVisualMedia = item.inferImagePreviewUrl() != null ||
        item.inferVideoPlaybackUrl() != null ||
        item.inferLocationPreview() != null ||
        item.inferPdfUrl() != null
    val hasVideoMedia = item.inferVideoPlaybackUrl() != null
    val hasAudioMedia = item.inferAudioUrl() != null
    val isPdfItem = item.inferPdfUrl() != null

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
    val density = androidx.compose.ui.platform.LocalDensity.current
    val detailSheetMaxHeight = if (isWideLayout) 0.72f else 0.64f
    val detailBottomPadding = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    } + 28.dp
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

    BackHandler(enabled = showDetails) {
        showDetails = false
        registerChromeInteraction()
    }

    val itemGestureModifier = if (showDetails) {
        Modifier
    } else {
        Modifier.pointerInput(item.id, isPdfItem, isZoomedIn) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val canHandleVerticalDrag = !isPdfItem || down.position.y > size.height * 0.72f
                dragAxisLocked = false
                isHorizontalDrag = false
                dragAccumulator = 0f
                totalDx = 0f
                totalDy = 0f

                val pointerId = down.id
                var previousPosition = down.position

                while (true) {
                    val event = awaitPointerEvent()
                    val activePointers = event.changes.count { it.pressed }

                    if (activePointers >= 2 || isZoomedIn) {
                        continue
                    }

                    val change = event.changes.firstOrNull { it.id == pointerId }

                    if (change == null || !change.pressed) {
                        if (isHorizontalDrag) {
                            val swipeThreshold = size.width * 0.25f
                            val currentIndex = navigableItemIds.indexOf(item.id)
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
                        } else if (canHandleVerticalDrag) {
                            when {
                                dragAccumulator <= -120f -> {
                                    showDetails = true
                                    chromeVisible = true
                                    dragVisualOffsetPx = 0f
                                }

                                dragAccumulator >= 180f -> {
                                    onBack()
                                }

                                abs(dragAccumulator) >= 24f -> {
                                    chromeVisible = dragAccumulator < 0f
                                    chromeInteractionTick += 1
                                }
                            }
                            dragAccumulator = 0f
                            dragVisualOffsetPx = 0f
                        } else {
                            dragAccumulator = 0f
                            dragVisualOffsetPx = 0f
                        }
                        dragAxisLocked = false
                        break
                    }

                    val dragAmount = change.position - previousPosition
                    previousPosition = change.position

                    if (!dragAxisLocked) {
                        totalDx += dragAmount.x
                        totalDy += dragAmount.y
                        if (kotlin.math.abs(totalDx) > kotlin.math.abs(totalDy) * 1.2f && kotlin.math.abs(totalDx) > 10f) {
                            dragAxisLocked = true
                            isHorizontalDrag = true
                        } else if (canHandleVerticalDrag && kotlin.math.abs(totalDy) > kotlin.math.abs(totalDx) * 1.2f && kotlin.math.abs(totalDy) > 10f) {
                            dragAxisLocked = true
                            isHorizontalDrag = false
                        }
                    }

                    if (dragAxisLocked) {
                        if (isHorizontalDrag) {
                            change.consume()
                            horizontalDragOffset += dragAmount.x
                        } else if (canHandleVerticalDrag) {
                            change.consume()
                            dragAccumulator += dragAmount.y
                            if (dragAmount.y < -8f) {
                                chromeVisible = true
                            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(itemGestureModifier)
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
            visible = chromeVisible && !showDetails,
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
                        DetailBottomAction(
                            icon = Icons.Filled.PushPin,
                            label = if (item.isPinned) "Pinned" else "Pin",
                            tint = if (item.isPinned) MaterialTheme.colorScheme.tertiary else Color.White,
                            onClick = {
                                registerChromeInteraction()
                                onPinnedToggled()
                            },
                        )
                        DetailBottomAction(
                            icon = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            label = if (item.isFavorite) "Saved" else "Save",
                            tint = if (item.isFavorite) MaterialTheme.colorScheme.tertiary else Color.White,
                            onClick = {
                                registerChromeInteraction()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onFavoriteToggled()
                            },
                            animate = item.isFavorite,
                        )
                        DetailBottomAction(
                            icon = Icons.Filled.Edit,
                            label = "Edit",
                            tint = Color.White,
                            onClick = {
                                registerChromeInteraction()
                                onEdit()
                            },
                        )
                        DetailBottomAction(
                            icon = Icons.Filled.Delete,
                            label = "Delete",
                            tint = Color.White,
                            onClick = {
                                registerChromeInteraction()
                                showDeleteDialog = true
                            },
                        )
                        DetailBottomAction(
                            label = "Done",
                            tint = if (justCompleted) Color(0xFF4CAF50) else Color.White,
                            onClick = {
                                registerChromeInteraction()
                                onCompleted()
                                justCompleted = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        ) {
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
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Details", color = Color.White)
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
                .fillMaxHeight(detailSheetMaxHeight)
                .align(Alignment.BottomCenter),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(dismissNestedScrollConnection),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TypeBadge(type = item.type, boxSize = 40.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${item.type.label} - ${item.createdAt.format(TimestampFormatter)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = {
                                showDetails = false
                                registerChromeInteraction()
                            },
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close details")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = detailBottomPadding),
                    ) {
                        item {
                            DetailContentSection(
                                item = item,
                                occurrenceStats = occurrenceStats,
                                globalSettings = globalSettings,
                                contentVisible = contentVisible,
                                onNotificationsChanged = onNotificationsChanged,
                                onViewHistory = onViewHistory,
                                onBodyChanged = onBodyChanged,
                                isAiEnabled = isAiEnabled,
                                isFeatureAvailable = isFeatureAvailable,
                                aiGeneratedTitle = aiGeneratedTitle,
                                aiGeneratedDescription = aiGeneratedDescription,
                                aiGeneratedTags = aiGeneratedTags,
                                aiMood = aiMood,
                                aiPhotoDescription = aiPhotoDescription,
                                aiAudioSummary = aiAudioSummary,
                                isAiGenerating = isAiGenerating,
                                aiError = aiError,
                                onGenerateTitle = onGenerateTitle,
                                onGenerateDescription = onGenerateDescription,
                                onSuggestTags = onSuggestTags,
                                onAnalyzeMood = onAnalyzeMood,
                                onDescribePhoto = onDescribePhoto,
                                onSummarizeAudio = onSummarizeAudio,
                                onApplyTitle = onApplyTitle,
                                onApplyTags = onApplyTags,
                                onApplyDescription = onApplyDescription,
                                onClearAiError = onClearAiError,
                                onCancelAi = onCancelAi,
                            )
                        }
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
    val pdfUrl = item.inferPdfUrl()
    val location = item.inferLocationPreview()
    val mapTile = item.inferLocationMapTile()

    val decryptedImage = rememberDecryptedMediaUri(imageUrl)
    val decryptedVideo = rememberDecryptedMediaUri(videoUrl)
    val decryptedAudio = rememberDecryptedMediaUri(audioUrl)
    val decryptedPdf = rememberDecryptedMediaUri(pdfUrl)
    val thumbhashPreview = remember(item.id, item.title, item.body) {
        item.thumbhashPreview()
    }
    val waitingForVisualMedia = (imageUrl != null || videoUrl != null) &&
        decryptedImage == null &&
        decryptedVideo == null

    val hasVisual = decryptedImage != null || decryptedVideo != null || location != null || decryptedPdf != null
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
                                mapTile = mapTile,
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

                decryptedPdf != null -> {
                    onVisualBrightnessMeasured(0.62f)
                    if (heavyReady) {
                        PdfDetailViewer(
                            pdfUrl = decryptedPdf,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        ThumbhashLoadingPlaceholder(
                            preview = thumbhashPreview,
                            isVideo = false,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                pdfUrl != null -> {
                    onVisualBrightnessMeasured(thumbhashPreview?.luminance ?: 0.58f)
                    ThumbhashLoadingPlaceholder(
                        preview = thumbhashPreview,
                        isVideo = false,
                        modifier = Modifier.fillMaxSize(),
                    )
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

@Composable
private fun PdfDetailViewer(pdfUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pageBitmaps by remember(pdfUrl) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(pdfUrl) {
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val pfd = openPdfDescriptor(context, pdfUrl) ?: return@launch
            try {
                val renderer = PdfRenderer(pfd)
                val bitmaps = mutableListOf<Bitmap>()
                val pageCount = renderer.pageCount.coerceAtMost(50)
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmaps.add(bitmap)
                }
                renderer.close()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    pageBitmaps = bitmaps
                }
            } catch (_: Throwable) {
            } finally {
                runCatching { pfd.close() }
            }
        }
        onDispose {
            job.cancel()
            pageBitmaps.forEach { runCatching { it.recycle() } }
            pageBitmaps = emptyList()
        }
    }

    if (pageBitmaps.isEmpty()) {
        Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            ShimmerBox(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                baseColor = Color.DarkGray,
                highlightColor = Color.White.copy(alpha = 0.08f),
            )
        }
    } else {
        Box(modifier = modifier.background(Color.Black)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(pageBitmaps, key = { it.hashCode() }) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF page",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentScale = ContentScale.FillWidth,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (scale > 1.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                if (newScale <= 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = newScale
                                    offset += pan
                                }
                            }
                        }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        scale = (scale * 1.3f).coerceAtMost(5f)
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Zoom in", tint = Color.White)
                }
                FilledTonalIconButton(
                    onClick = {
                        val newScale = scale / 1.3f
                        if (newScale <= 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = newScale
                        }
                    },
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Zoom out", tint = Color.White)
                }
            }
        }
    }
}

private fun openPdfDescriptor(context: Context, pdfUrl: String): ParcelFileDescriptor? {
    return runCatching {
        val uri = Uri.parse(pdfUrl)
        when (uri.scheme) {
            "content" -> context.contentResolver.openFileDescriptor(uri, "r")
            "file" -> uri.path?.let { File(it) }?.let {
                ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY)
            }
            else -> null
        }
    }.getOrNull()
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
        val bars = withContext(Dispatchers.IO) {
            AudioWaveformGenerator().generateWaveform(context, Uri.parse(audioUrl), barCount)
        }
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
    onBodyChanged: ((String) -> Unit)? = null,
    isAiEnabled: Boolean = false,
    isFeatureAvailable: (com.raulshma.dailylife.domain.AIFeature) -> Boolean = { false },
    aiGeneratedTitle: String = "",
    aiGeneratedDescription: String = "",
    aiGeneratedTags: List<String> = emptyList(),
    aiMood: com.raulshma.dailylife.domain.MoodResult? = null,
    aiPhotoDescription: String = "",
    aiAudioSummary: String = "",
    isAiGenerating: Boolean = false,
    aiError: String? = null,
    onGenerateTitle: (String, String?, String?) -> Unit = { _, _, _ -> },
    onGenerateDescription: (String, String) -> Unit = { _, _ -> },
    onSuggestTags: (String, String) -> Unit = { _, _ -> },
    onAnalyzeMood: (String, String) -> Unit = { _, _ -> },
    onDescribePhoto: (String) -> Unit = {},
    onSummarizeAudio: (String) -> Unit = {},
    onApplyTitle: (Long, String) -> Unit = { _, _ -> },
    onApplyTags: (Long, Set<String>) -> Unit = { _, _ -> },
    onApplyDescription: (Long, String) -> Unit = { _, _ -> },
    onClearAiError: () -> Unit = {},
    onCancelAi: () -> Unit = {},
) {
    val context = LocalContext.current
    val displayBody = remember(item.id, item.title, item.body) { item.displayBody() }

    val attachmentUrl = remember(item.id, item.title, item.body) {
        item.inferImagePreviewUrl()
            ?: item.inferVideoPlaybackUrl()
            ?: item.inferAudioUrl()
            ?: item.inferPdfUrl()
    }
    val attachmentSize = remember(attachmentUrl) {
        attachmentUrl?.let { getAttachmentSize(context, it) }
    }
    val decryptedAttachmentUri = rememberDecryptedMediaUri(attachmentUrl)

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                DailyLifeTween.content(),
                initialOffsetY = { it / 3 }
            ),
        ) {
            DetailAtAGlanceCard(
                item = item,
                occurrenceStats = occurrenceStats,
                globalSettings = globalSettings,
                canCopy = displayBody.isNotBlank() || item.aiSummary?.isNotBlank() == true,
                onCopy = { copyItemToClipboard(context, item) },
                onShare = { shareItem(context, item, decryptedAttachmentUri) },
            )
        }

        if (displayBody.isNotBlank()) {
            val isChecklist = displayBody.lines().any { it.startsWith("- [") }
            if (item.type == com.raulshma.dailylife.domain.LifeItemType.Task && isChecklist && onBodyChanged != null) {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                        DailyLifeTween.content(),
                        initialOffsetY = { it / 3 }
                    ),
                ) {
                    DetailChecklistCard(
                        body = displayBody,
                        onChecklistChanged = onBodyChanged,
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                        DailyLifeTween.content(),
                        initialOffsetY = { it / 3 }
                    ),
                ) {
                    DetailTextCard(
                        title = "Content",
                        icon = Icons.Filled.Info,
                        text = displayBody,
                    )
                }
            }
        }

        val savedAiSummary = item.aiSummary?.trim().orEmpty()
        if (savedAiSummary.isNotBlank()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                DetailTextCard(
                    title = "Summarized",
                    icon = Icons.Filled.SmartToy,
                    text = savedAiSummary,
                )
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
                DetailTagsCard(tags = item.tags)
            }
        }

        if (isAiEnabled) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                AIToolsSection(
                    item = item,
                    isFeatureAvailable = isFeatureAvailable,
                    aiGeneratedTitle = aiGeneratedTitle,
                    aiGeneratedDescription = aiGeneratedDescription,
                    aiGeneratedTags = aiGeneratedTags,
                    aiMood = aiMood,
                    aiPhotoDescription = aiPhotoDescription,
                    aiAudioSummary = aiAudioSummary,
                    isAiGenerating = isAiGenerating,
                    aiError = aiError,
                    onGenerateTitle = onGenerateTitle,
                    onGenerateDescription = onGenerateDescription,
                    onSuggestTags = onSuggestTags,
                    onAnalyzeMood = onAnalyzeMood,
                    onDescribePhoto = onDescribePhoto,
                    onSummarizeAudio = onSummarizeAudio,
                    onApplyTitle = onApplyTitle,
                    onApplyTags = onApplyTags,
                    onApplyDescription = onApplyDescription,
                    onClearAiError = onClearAiError,
                    onCancelAi = onCancelAi,
                )
            }
        }

        val attachmentFormat = remember(attachmentUrl) { extractAttachmentFormat(attachmentUrl) }

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
            attachmentSize?.let { add("Size" to formatFileSize(it)) }
            attachmentFormat?.let { add("Format" to it) }
        }

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(
                androidx.compose.animation.core.tween(
                    durationMillis = DailyLifeDuration.SHORT,
                    delayMillis = staggerDelay(4, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                )
            ) + slideInVertically(
                androidx.compose.animation.core.tween(
                    durationMillis = DailyLifeDuration.MEDIUM,
                    delayMillis = staggerDelay(4, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                ),
                initialOffsetY = { it / 4 }
            ),
        ) {
            DetailMetadataCard(metadataItems = metadataItems)
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
            DetailNotificationCard(
                enabled = item.notificationSettings.enabled,
                timeText = effectiveTime.format(com.raulshma.dailylife.ui.TimeFormatter),
                onEnabledChanged = {
                    onNotificationsChanged(item.notificationSettings.copy(enabled = it))
                },
            )
        }

        if (item.completionHistory.isNotEmpty()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade<Float>()) + slideInVertically(
                    DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SectionHeader(icon = Icons.Filled.Done, title = "Completion history")

                        item.completionHistory
                            .sortedByDescending { it.completedAt }
                            .take(3)
                            .forEachIndexed { index, record ->
                                if (index > 0) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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

                        TextButton(
                            onClick = onViewHistory,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (item.completionHistory.size > 3) {
                                    "View all ${item.completionHistory.size} entries"
                                } else {
                                    "View history"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AIToolsSection(
    item: LifeItem,
    isFeatureAvailable: (AIFeature) -> Boolean,
    aiGeneratedTitle: String,
    aiGeneratedDescription: String,
    aiGeneratedTags: List<String>,
    aiMood: com.raulshma.dailylife.domain.MoodResult?,
    aiPhotoDescription: String,
    aiAudioSummary: String,
    isAiGenerating: Boolean,
    aiError: String?,
    onGenerateTitle: (String, String?, String?) -> Unit,
    onGenerateDescription: (String, String) -> Unit,
    onSuggestTags: (String, String) -> Unit,
    onAnalyzeMood: (String, String) -> Unit,
    onDescribePhoto: (String) -> Unit,
    onSummarizeAudio: (String) -> Unit,
    onApplyTitle: (Long, String) -> Unit,
    onApplyTags: (Long, Set<String>) -> Unit,
    onApplyDescription: (Long, String) -> Unit,
    onClearAiError: () -> Unit,
    onCancelAi: () -> Unit,
) {
    val hasPhoto = item.inferImagePreviewUrl() != null
    val hasVideo = item.inferVideoPlaybackUrl() != null
    val hasAudio = item.inferAudioUrl() != null
    val hasBody = item.displayBody().isNotBlank()
    val imageUrl = item.inferImagePreviewUrl() ?: item.inferVideoPlaybackUrl()
    val audioUrl = item.inferAudioUrl() ?: item.inferVideoPlaybackUrl()
    var appliedLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appliedLabel) {
        if (appliedLabel != null) {
            delay(2000L)
            appliedLabel = null
        }
    }

    LaunchedEffect(aiError) {
        if (aiError != null) {
            delay(5000L)
            onClearAiError()
        }
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Generative Tools",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (isAiGenerating) {
                    Spacer(modifier = Modifier.weight(1f))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    IconButton(onClick = onCancelAi, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(visible = appliedLabel != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = appliedLabel ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            AnimatedVisibility(visible = aiError != null && !isAiGenerating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { onClearAiError() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = aiError ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AIActionChip(
                    label = "Generate Title",
                    icon = Icons.Filled.Edit,
                    enabled = isFeatureAvailable(AIFeature.SMART_TITLE) && (hasBody || hasPhoto || hasAudio),
                    disabledReason = when {
                        !hasBody && !hasPhoto && !hasAudio -> "No text, image, or audio content"
                        else -> "Requires a compatible model"
                    },
                    onClick = { onGenerateTitle(item.displayBody(), imageUrl, audioUrl) },
                )

                AIActionChip(
                    label = "Generate Description",
                    icon = Icons.Filled.Edit,
                    enabled = isFeatureAvailable(AIFeature.SUMMARIZE) && hasBody,
                    disabledReason = when {
                        !hasBody -> "No text content"
                        else -> "Requires text model"
                    },
                    onClick = { onGenerateDescription(item.title, item.displayBody()) },
                )

                AIActionChip(
                    label = "Suggest Tags",
                    icon = Icons.Filled.Tag,
                    enabled = isFeatureAvailable(AIFeature.TAG_SUGGESTION) && hasBody,
                    disabledReason = when {
                        !hasBody -> "No text content"
                        else -> "Requires text model"
                    },
                    onClick = { onSuggestTags(item.title, item.displayBody()) },
                )

                AIActionChip(
                    label = "Analyze Mood",
                    icon = Icons.Filled.SmartToy,
                    enabled = isFeatureAvailable(AIFeature.MOOD_ANALYSIS) && hasBody,
                    disabledReason = when {
                        !hasBody -> "No text content"
                        else -> "Requires text model"
                    },
                    onClick = { onAnalyzeMood(item.title, item.displayBody()) },
                )

                if (hasPhoto || hasVideo) {
                    AIActionChip(
                        label = "Describe Photo",
                        icon = Icons.Filled.Image,
                        enabled = isFeatureAvailable(AIFeature.PHOTO_DESCRIPTION) && imageUrl != null,
                        disabledReason = when {
                            imageUrl == null -> "No image available"
                            else -> "Requires vision model"
                        },
                        onClick = { imageUrl?.let(onDescribePhoto) },
                    )
                }

                if (hasAudio || hasVideo) {
                    AIActionChip(
                        label = "Summarize Audio",
                        icon = Icons.Filled.Mic,
                        enabled = isFeatureAvailable(AIFeature.AUDIO_SUMMARY) && audioUrl != null,
                        disabledReason = when {
                            audioUrl == null -> "No audio available"
                            else -> "Requires audio model"
                        },
                        onClick = { audioUrl?.let(onSummarizeAudio) },
                    )
                }
            }

            if (aiGeneratedTitle.isNotBlank()) {
                AIResultRow(
                    label = "Suggested title",
                    value = aiGeneratedTitle,
                    onApply = {
                        onApplyTitle(item.id, aiGeneratedTitle.trim())
                        appliedLabel = "Title updated"
                    },
                )
            }

            if (aiGeneratedDescription.isNotBlank()) {
                AIResultRow(
                    label = "Generated description",
                    value = aiGeneratedDescription,
                    onApply = {
                        onApplyDescription(item.id, aiGeneratedDescription.trim())
                        appliedLabel = "Description applied"
                    },
                )
            }

            if (aiGeneratedTags.isNotEmpty()) {
                AIResultRow(
                    label = "Suggested tags",
                    value = aiGeneratedTags.joinToString(", "),
                    onApply = {
                        onApplyTags(item.id, aiGeneratedTags.toSet())
                        appliedLabel = "Tags updated"
                    },
                )
            }

            if (aiMood != null) {
                AIResultRow(
                    label = "Detected mood",
                    value = "${aiMood.moodLabel.replaceFirstChar { it.uppercase() }} (${(aiMood.confidence * 100).toInt()}%)",
                    onApply = {
                        val moodText = "Mood: ${aiMood.moodLabel.replaceFirstChar { c -> c.uppercase() }} (${(aiMood.confidence * 100).toInt()}%)"
                        onApplyDescription(item.id, moodText)
                        appliedLabel = "Mood saved"
                    },
                )
            }

            if (aiPhotoDescription.isNotBlank()) {
                AIResultRow(
                    label = "Photo description",
                    value = aiPhotoDescription,
                    onApply = {
                        onApplyDescription(item.id, aiPhotoDescription.trim())
                        appliedLabel = "Photo description applied"
                    },
                )
            }

            if (aiAudioSummary.isNotBlank()) {
                AIResultRow(
                    label = "Audio summary",
                    value = aiAudioSummary,
                    onApply = {
                        onApplyDescription(item.id, aiAudioSummary.trim())
                        appliedLabel = "Audio summary applied"
                    },
                )
            }
        }
    }
}

@Composable
private fun AIActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    disabledReason: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        shape = shape,
        color = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.clip(shape).then(
            if (enabled) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )
        }
    }
}

@Composable
private fun AIResultRow(
    label: String,
    value: String,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = if (value.isNotBlank()) value else "Generating...",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isNotBlank()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        if (value.isNotBlank()) {
            FilledTonalButton(
                onClick = onApply,
                modifier = Modifier.align(Alignment.End),
            ) {
                Icon(
                    Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply")
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
private fun DetailBottomAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    animate: Boolean = false,
    iconContent: (@Composable () -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (animate) 1.12f else 1f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Bouncy,
        label = "bottomActionScale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.widthIn(min = 48.dp),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            if (iconContent != null) {
                iconContent()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailAtAGlanceCard(
    item: LifeItem,
    occurrenceStats: OccurrenceStats,
    globalSettings: NotificationSettings,
    canCopy: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val effectiveTime = item.notificationSettings.timeOverride
        ?: globalSettings.preferredTime
    val status = item.taskStatus?.label
        ?: if (item.reminderAt != null) "Reminder" else item.type.label
    val cadence = when {
        item.isRecurring -> item.recurrenceRule.frequency.label
        item.reminderAt != null -> item.reminderAt.format(TimestampFormatter)
        item.notificationSettings.enabled -> "Notify ${effectiveTime.format(com.raulshma.dailylife.ui.TimeFormatter)}"
        else -> "No schedule"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailPill(label = "Status", value = status)
                DetailPill(label = "Schedule", value = cadence)
                DetailPill(label = "Completed", value = occurrenceStats.completedCount.toString())
                if (occurrenceStats.currentStreak > 0) {
                    DetailPill(label = "Streak", value = occurrenceStats.currentStreak.toString())
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onCopy,
                    enabled = canCopy,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }
                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun DetailPill(
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        modifier = Modifier.widthIn(min = 124.dp, max = 220.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private enum class ChecklistItemState(val prefix: String) {
    NotStarted("- [ ] "),
    InProgress("- [-] "),
    Done("- [x] ");

    fun next(): ChecklistItemState = when (this) {
        NotStarted -> InProgress
        InProgress -> Done
        Done -> NotStarted
    }

    companion object {
        fun fromLine(line: String): ChecklistItemState = when {
            line.startsWith(Done.prefix, ignoreCase = true) -> Done
            line.startsWith(InProgress.prefix, ignoreCase = true) -> InProgress
            line.startsWith(NotStarted.prefix) -> NotStarted
            else -> NotStarted
        }

        fun stripPrefix(line: String): String {
            for (state in entries) {
                if (line.startsWith(state.prefix, ignoreCase = true)) return line.substring(state.prefix.length)
            }
            return line
        }
    }
}

@Composable
private fun DetailChecklistCard(
    body: String,
    onChecklistChanged: (String) -> Unit,
) {
    val items = remember(body) {
        body.split("\n").mapNotNull { line ->
            if (line.startsWith("- [")) {
                val state = ChecklistItemState.fromLine(line)
                val text = ChecklistItemState.stripPrefix(line)
                Triple(text, state, line)
            } else null
        }
    }
    val completedCount = items.count { it.second == ChecklistItemState.Done }
    val totalCount = items.size

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Checklist",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (completedCount == totalCount) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (totalCount > 0) {
                val progress = completedCount.toFloat() / totalCount
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (completedCount == totalCount) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            items.forEachIndexed { index, (text, state, _) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newState = state.next()
                            val newItems = body.split("\n").toMutableList()
                            val checklistIndex = newItems.indexOfFirst { it.startsWith("- [") }
                            var found = 0
                            for (i in checklistIndex until newItems.size) {
                                if (newItems[i].startsWith("- [")) {
                                    if (found == index) {
                                        val stripped = ChecklistItemState.stripPrefix(newItems[i])
                                        newItems[i] = newState.prefix + stripped
                                        break
                                    }
                                    found++
                                }
                            }
                            onChecklistChanged(newItems.joinToString("\n"))
                        }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TripleStateCheckbox(state = state)
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (state) {
                            ChecklistItemState.Done -> MaterialTheme.colorScheme.onSurfaceVariant
                            ChecklistItemState.InProgress -> MaterialTheme.colorScheme.onSurface
                            ChecklistItemState.NotStarted -> MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (state == ChecklistItemState.Done)
                            androidx.compose.ui.text.style.TextDecoration.LineThrough
                        else null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TripleStateCheckbox(state: ChecklistItemState) {
    val borderWidth = 2.dp
    val size = 22.dp
    when (state) {
        ChecklistItemState.NotStarted ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .border(borderWidth, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(4.dp)),
            )
        ChecklistItemState.InProgress ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .border(borderWidth, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        ChecklistItemState.Done ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
    }
}

@Composable
private fun DetailTextCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailTagsCard(tags: Set<String>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeader(icon = Icons.Filled.Tag, title = "Tags")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
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
}

@Composable
private fun DetailMetadataCard(metadataItems: List<Pair<String, String>>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(icon = Icons.Filled.Info, title = "Details")
            metadataItems.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                DetailLine(label = label, value = value)
            }
        }
    }
}

@Composable
private fun DetailNotificationCard(
    enabled: Boolean,
    timeText: String,
    onEnabledChanged: (Boolean) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (enabled) {
                    Icons.Filled.Notifications
                } else {
                    Icons.Filled.NotificationsOff
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (enabled) "Item alerts at $timeText" else "Item alerts are off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

private fun copyItemToClipboard(context: Context, item: LifeItem) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(item.title, buildShareText(item)))
}

private fun shareItem(context: Context, item: LifeItem, attachmentUri: String? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        if (attachmentUri != null) {
            val uri = Uri.parse(attachmentUri)
            val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
                ?: inferMimeType(attachmentUri)
                ?: "*/*"
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, buildShareText(item))
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share item"))
    }
}

private fun buildShareText(item: LifeItem): String {
    return buildString {
        appendLine(item.title)
        appendLine(item.createdAt.format(TimestampFormatter))
        val body = item.displayBody()
        if (body.isNotBlank()) {
            appendLine()
            appendLine(body)
        }
        item.aiSummary?.trim()?.takeIf { it.isNotBlank() }?.let { summary ->
            appendLine()
            appendLine("Summarized")
            appendLine(summary)
        }
        if (item.tags.isNotEmpty()) {
            appendLine()
            appendLine(item.tags.joinToString(" ") { "#$it" })
        }
    }.trim()
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.1f GB".format(gb)
}

private fun getAttachmentSize(context: Context, uriString: String): Long? {
    return runCatching {
        val uri = Uri.parse(uriString)
        when (uri.scheme) {
            "file", "content" -> {
                UriFileResolver.resolveToFile(uri, context)?.length()?.takeIf { it > 0 }
            }
            else -> null
        }
    }.getOrNull()
}

private fun inferMimeType(uriString: String): String? {
    val effective = if (uriString.endsWith(".enc", ignoreCase = true)) {
        uriString.removeSuffix(".enc")
    } else {
        uriString
    }
    return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(
        effective.substringAfterLast('.', "").lowercase()
    )
}

private fun extractAttachmentFormat(uriString: String?): String? {
    if (uriString.isNullOrBlank()) return null
    val effective = if (uriString.endsWith(".enc", ignoreCase = true)) {
        uriString.removeSuffix(".enc")
    } else {
        uriString
    }
    val extension = effective.substringAfterLast('.', "").trim().lowercase()
    return extension.takeIf { it.isNotBlank() }?.uppercase()
}

@Composable
private fun rememberDismissNestedScrollConnection(
    onDismiss: () -> Unit,
    dismissThresholdPx: Float = 80f,
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
                    } else if (available.y < 0) {
                        accumulatedDrag = (accumulatedDrag + available.y).coerceAtLeast(0f)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (available.y > 0 && accumulatedDrag > dismissThresholdPx * 0.3f) {
                    onDismiss()
                    accumulatedDrag = 0f
                }
                return Velocity.Zero
            }
        }
    }
}
