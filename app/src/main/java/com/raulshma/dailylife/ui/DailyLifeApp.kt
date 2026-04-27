package com.raulshma.dailylife.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Slider
import kotlinx.coroutines.delay
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.BackHandler
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.media.MediaThumbnailGenerator
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.domain.DailyLifeFilters
import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.OccurrenceStats
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.S3BackupSettings
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.capture.AudioRecorder
import com.raulshma.dailylife.ui.capture.LocationPickerSheet
import com.raulshma.dailylife.ui.capture.SpeechTranscriber
import com.raulshma.dailylife.ui.capture.hasAudioPermission
import com.raulshma.dailylife.ui.capture.hasCameraPermission
import com.raulshma.dailylife.ui.capture.rememberMediaCaptureLauncher
import com.raulshma.dailylife.ui.components.AnimatedCounter
import com.raulshma.dailylife.ui.components.PressableCard
import com.raulshma.dailylife.ui.components.ShimmerBox
import com.raulshma.dailylife.ui.components.StaggeredEnter
import com.raulshma.dailylife.ui.components.rememberStaggeredVisibility
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.detail.ItemDetailScreen
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

internal val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
internal val DateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
internal val FilterDateFormatter = DateTimeFormatter.ofPattern("MMM d")
internal val TimestampFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
private val DefaultReminderTime = LocalTime.of(9, 0)

private enum class HomeTab(
    val label: String,
    val icon: ImageVector,
) {
    Photos(label = "Photos", icon = Icons.Filled.PhotoLibrary),
    Search(label = "Search", icon = Icons.Filled.Search),
    Collections(label = "Collections", icon = Icons.Filled.Category),
    Graph(label = "Graph", icon = Icons.Filled.AccountTree),
}

private sealed class Screen {
    data class Main(val tab: HomeTab) : Screen()
    data class Detail(val itemId: Long) : Screen()
    data class CompletionHistory(val itemId: Long) : Screen()
}

internal val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
internal val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

internal data class QuickAddDraft(
    val typeName: String = LifeItemType.Thought.name,
    val title: String = "",
    val body: String = "",
    val tags: String = "",
    val favorite: Boolean = false,
    val pinned: Boolean = false,
    val reminderDate: String = "",
    val reminderTime: String = "",
    val notificationsEnabled: Boolean = true,
    val overrideTime: String = "",
    val recurring: Boolean = false,
    val showAdvanced: Boolean = false,
    val showReminderOptions: Boolean = false,
)

internal val QuickAddDraftSaver = mapSaver(
    save = {
        mapOf(
            "typeName" to it.typeName,
            "title" to it.title,
            "body" to it.body,
            "tags" to it.tags,
            "favorite" to it.favorite,
            "pinned" to it.pinned,
            "reminderDate" to it.reminderDate,
            "reminderTime" to it.reminderTime,
            "notificationsEnabled" to it.notificationsEnabled,
            "overrideTime" to it.overrideTime,
            "recurring" to it.recurring,
            "showAdvanced" to it.showAdvanced,
            "showReminderOptions" to it.showReminderOptions,
        )
    },
    restore = {
        QuickAddDraft(
            typeName = it["typeName"] as? String ?: LifeItemType.Thought.name,
            title = it["title"] as? String ?: "",
            body = it["body"] as? String ?: "",
            tags = it["tags"] as? String ?: "",
            favorite = it["favorite"] as? Boolean ?: false,
            pinned = it["pinned"] as? Boolean ?: false,
            reminderDate = it["reminderDate"] as? String ?: "",
            reminderTime = it["reminderTime"] as? String ?: "",
            notificationsEnabled = it["notificationsEnabled"] as? Boolean ?: true,
            overrideTime = it["overrideTime"] as? String ?: "",
            recurring = it["recurring"] as? Boolean ?: false,
            showAdvanced = it["showAdvanced"] as? Boolean ?: false,
            showReminderOptions = it["showReminderOptions"] as? Boolean ?: false,
        )
    },
)

private const val DRAFT_PREFS_NAME = "quick_add_draft_prefs"

private fun saveDraftToPrefs(context: Context, draft: QuickAddDraft) {
    val prefs = context.getSharedPreferences(DRAFT_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().apply {
        putString("typeName", draft.typeName)
        putString("title", draft.title)
        putString("body", draft.body)
        putString("tags", draft.tags)
        putBoolean("favorite", draft.favorite)
        putBoolean("pinned", draft.pinned)
        putString("reminderDate", draft.reminderDate)
        putString("reminderTime", draft.reminderTime)
        putBoolean("notificationsEnabled", draft.notificationsEnabled)
        putString("overrideTime", draft.overrideTime)
        putBoolean("recurring", draft.recurring)
        putBoolean("showAdvanced", draft.showAdvanced)
        putBoolean("showReminderOptions", draft.showReminderOptions)
        apply()
    }
}

private fun loadDraftFromPrefs(context: Context): QuickAddDraft {
    val prefs = context.getSharedPreferences(DRAFT_PREFS_NAME, Context.MODE_PRIVATE)
    return QuickAddDraft(
        typeName = prefs.getString("typeName", null) ?: LifeItemType.Thought.name,
        title = prefs.getString("title", null) ?: "",
        body = prefs.getString("body", null) ?: "",
        tags = prefs.getString("tags", null) ?: "",
        favorite = prefs.getBoolean("favorite", false),
        pinned = prefs.getBoolean("pinned", false),
        reminderDate = prefs.getString("reminderDate", null) ?: "",
        reminderTime = prefs.getString("reminderTime", null) ?: "",
        notificationsEnabled = prefs.getBoolean("notificationsEnabled", true),
        overrideTime = prefs.getString("overrideTime", null) ?: "",
        recurring = prefs.getBoolean("recurring", false),
        showAdvanced = prefs.getBoolean("showAdvanced", false),
        showReminderOptions = prefs.getBoolean("showReminderOptions", false),
    )
}

private fun clearDraftFromPrefs(context: Context) {
    context.getSharedPreferences(DRAFT_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLifeApp(viewModel: DailyLifeViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var showPreferences by rememberSaveable { mutableStateOf(false) }
    var showS3BackupSettings by rememberSaveable { mutableStateOf(false) }
    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedTabName by rememberSaveable { mutableStateOf(HomeTab.Photos.name) }
    val selectedTab = HomeTab.entries.firstOrNull { it.name == selectedTabName } ?: HomeTab.Photos
    val selectedItem = state.items.firstOrNull { it.id == selectedItemId }
    val s3Settings by viewModel.s3BackupSettings.collectAsStateWithLifecycle()
    val lastBackupResult by viewModel.lastBackupResult.collectAsStateWithLifecycle()
    var quickAddDraft by rememberSaveable(stateSaver = QuickAddDraftSaver) {
        mutableStateOf(loadDraftFromPrefs(context))
    }
    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var editItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editDraft by rememberSaveable(stateSaver = QuickAddDraftSaver) {
        mutableStateOf(QuickAddDraft())
    }
    var completionHistoryItemId by rememberSaveable { mutableStateOf<Long?>(null) }

    val screen = when {
        completionHistoryItemId != null -> Screen.CompletionHistory(completionHistoryItemId!!)
        selectedItemId != null -> Screen.Detail(selectedItemId!!)
        else -> Screen.Main(selectedTab)
    }

    BackHandler(enabled = completionHistoryItemId != null || selectedItemId != null) {
        if (completionHistoryItemId != null) {
            completionHistoryItemId = null
        } else {
            selectedItemId = null
        }
    }

    LaunchedEffect(quickAddDraft) {
        saveDraftToPrefs(context, quickAddDraft)
    }

    var quickAddLocationCallback by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }
    var editLocationCallback by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            MediaEncryptionManager(context).clearDecryptedCache()
        }
    }

    val mediaLauncher = rememberMediaCaptureLauncher(
        context = context,
        onPhotoCaptured = { uri ->
            quickAddDraft = quickAddDraft.copy(
                typeName = LifeItemType.Photo.name,
                body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri",
            )
            showQuickAdd = true
        },
        onVideoCaptured = { uri ->
            quickAddDraft = quickAddDraft.copy(
                typeName = LifeItemType.Video.name,
                body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri",
            )
            showQuickAdd = true
        },
        onPhotoPicked = { uri ->
            quickAddDraft = quickAddDraft.copy(
                typeName = LifeItemType.Photo.name,
                body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri",
            )
            showQuickAdd = true
        },
        onVideoPicked = { uri ->
            quickAddDraft = quickAddDraft.copy(
                typeName = LifeItemType.Video.name,
                body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri",
            )
            showQuickAdd = true
        },
        onFilePicked = { uri ->
            quickAddDraft = quickAddDraft.copy(body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri")
            showQuickAdd = true
        },
    )

    val editMediaLauncher = rememberMediaCaptureLauncher(
        context = context,
        onPhotoCaptured = { uri ->
            editDraft = editDraft.copy(
                typeName = LifeItemType.Photo.name,
                body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri",
            )
        },
        onVideoCaptured = { uri ->
            editDraft = editDraft.copy(
                typeName = LifeItemType.Video.name,
                body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri",
            )
        },
        onPhotoPicked = { uri ->
            editDraft = editDraft.copy(
                typeName = LifeItemType.Photo.name,
                body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri",
            )
        },
        onVideoPicked = { uri ->
            editDraft = editDraft.copy(
                typeName = LifeItemType.Video.name,
                body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri",
            )
        },
        onFilePicked = { uri ->
            editDraft = editDraft.copy(body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri")
        },
    )

    val visibleItemIds = remember(state.visibleItems) { state.visibleItems.map { it.id } }
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = screen,
            transitionSpec = {
                val initial = initialState
                val target = targetState
                val enter: EnterTransition
                val exit: ExitTransition
                when {
                    initial is Screen.CompletionHistory && target is Screen.Detail -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                    }
                    target is Screen.CompletionHistory -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                    }
                    initial is Screen.Detail && target is Screen.Detail -> {
                        val initialIndex = visibleItemIds.indexOf(initial.itemId)
                        val targetIndex = visibleItemIds.indexOf(target.itemId)
                        val isNext = if (initialIndex != -1 && targetIndex != -1) {
                            targetIndex > initialIndex
                        } else {
                            target.itemId > initial.itemId
                        }
                        if (isNext) {
                            enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                            exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                        } else {
                            enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { -it / 6 }
                            exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { it / 8 }
                        }
                    }
                    target is Screen.Detail -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                    }
                    else -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                    }
                }
                ContentTransform(enter, exit, targetContentZIndex = 1f, sizeTransform = null)
            },
            label = "screenTransition"
        ) { currentScreen ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalAnimatedVisibilityScope provides this@AnimatedContent,
            ) {
                when (currentScreen) {
                    is Screen.Main -> MainScaffold(
                        state = state,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTabName = it.name },
                        onItemSelected = { selectedItemId = it },
                        onStorageErrorDismissed = viewModel::clearStorageError,
                        onSearchChanged = viewModel::updateSearchQuery,
                        onTypeSelected = viewModel::selectType,
                        onTagSelected = viewModel::selectTag,
                        onDateRangeChanged = viewModel::updateDateRange,
                        onFavoritesOnlyToggled = viewModel::toggleFavoritesOnly,
                        onClearFilters = viewModel::clearFilters,
                        onFavoriteToggled = viewModel::toggleFavorite,
                        onPinnedToggled = viewModel::togglePinned,
                        onTaskStatusChanged = viewModel::updateTaskStatus,
                        onCompleted = viewModel::markOccurrenceCompleted,
                        onCollectionSelected = { items ->
                            val first = items.firstOrNull() ?: return@MainScaffold
                            selectedItemId = first.id
                        },
                        onShowQuickAdd = { showQuickAdd = true },
                        onShowPreferences = { showPreferences = true },
                        onShowS3Backup = { showS3BackupSettings = true },
                        contentPadding = PaddingValues(),
                    )

                    is Screen.Detail -> selectedItem?.let { item ->
                        val navigableItemIds = state.visibleItems.map { it.id }
                        ItemDetailScreen(
                            item = item,
                            globalSettings = state.notificationSettings,
                            navigableItemIds = navigableItemIds,
                            onBack = { selectedItemId = null },
                            onFavoriteToggled = { viewModel.toggleFavorite(item.id) },
                            onPinnedToggled = { viewModel.togglePinned(item.id) },
                            onCompleted = { viewModel.markOccurrenceCompleted(item.id) },
                            onNotificationsChanged = { viewModel.updateItemNotifications(item.id, it) },
                            onEdit = {
                                editItemId = item.id
                                editDraft = QuickAddDraft(
                                    typeName = item.type.name,
                                    title = item.title,
                                    body = item.body,
                                    tags = item.tags.joinToString(", "),
                                    favorite = item.isFavorite,
                                    pinned = item.isPinned,
                                    reminderDate = item.reminderAt?.toLocalDate()?.toString() ?: "",
                                    reminderTime = item.reminderAt?.toLocalTime()?.format(TimeFormatter) ?: "",
                                    notificationsEnabled = item.notificationSettings.enabled,
                                    overrideTime = item.notificationSettings.timeOverride?.format(TimeFormatter) ?: "",
                                    recurring = item.isRecurring,
                                )
                                showEditSheet = true
                            },
                            onDelete = {
                                viewModel.deleteItem(item.id)
                                selectedItemId = null
                            },
                            onNavigateToItem = { selectedItemId = it },
                            onViewHistory = { completionHistoryItemId = item.id },
                        )
                    } ?: run {
                        selectedItemId = null
                    }

                    is Screen.CompletionHistory -> {
                        val historyItem = state.items.firstOrNull { it.id == currentScreen.itemId }
                        if (historyItem != null) {
                            com.raulshma.dailylife.ui.detail.CompletionHistoryScreen(
                                item = historyItem,
                                onBack = { completionHistoryItemId = null },
                                onUpdateRecord = { itemId, record ->
                                    viewModel.updateCompletionRecord(itemId, record)
                                },
                                onDeleteRecord = { itemId, occurrenceDate, completedAt ->
                                    viewModel.deleteCompletionRecord(itemId, occurrenceDate, completedAt)
                                },
                            )
                        } else {
                            completionHistoryItemId = null
                        }
                    }
                }
            }
        }
    }

    val quickAddSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showQuickAdd) {
        ModalBottomSheet(
            onDismissRequest = { showQuickAdd = false },
            sheetState = quickAddSheetState,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxSize()
        ) {
            QuickAddScreen(
                draft = quickAddDraft,
                onDraftChanged = { quickAddDraft = it },
                onAdd = { draft ->
                    viewModel.addItem(draft)
                    showQuickAdd = false
                    quickAddDraft = QuickAddDraft()
                    clearDraftFromPrefs(context)
                },
                onAddAndContinue = { draft ->
                    viewModel.addItem(draft)
                    quickAddDraft = QuickAddDraft()
                    clearDraftFromPrefs(context)
                },
                onDismiss = {
                    showQuickAdd = false
                },
                onDiscardDraft = {
                    showQuickAdd = false
                    quickAddDraft = QuickAddDraft()
                    clearDraftFromPrefs(context)
                },
                mediaLauncher = mediaLauncher,
                onShowLocationPicker = { onLocationSelected ->
                    quickAddLocationCallback = onLocationSelected
                    showLocationPicker = true
                },
                allTags = state.allTags,
            )
        }
    }

    if (showLocationPicker) {
        LocationPickerSheet(
            onLocationSelected = { lat, lon ->
                quickAddLocationCallback?.invoke(lat, lon)
                editLocationCallback?.invoke(lat, lon)
                quickAddLocationCallback = null
                editLocationCallback = null
                showLocationPicker = false
            },
            onDismiss = {
                quickAddLocationCallback = null
                editLocationCallback = null
                showLocationPicker = false
            },
        )
    }

    if (showEditSheet && editItemId != null) {
        val editSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
                editItemId = null
            },
            sheetState = editSheetState,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxSize()
        ) {
            QuickAddScreen(
                draft = editDraft,
                onDraftChanged = { editDraft = it },
                onAdd = { draft ->
                    editItemId?.let { id ->
                        viewModel.updateItem(id, draft)
                    }
                    showEditSheet = false
                    editItemId = null
                },
                onAddAndContinue = { _ -> },
                onDismiss = {
                    showEditSheet = false
                    editItemId = null
                },
                onDiscardDraft = {
                    showEditSheet = false
                    editItemId = null
                },
                mediaLauncher = editMediaLauncher,
                onShowLocationPicker = { onLocationSelected ->
                    editLocationCallback = onLocationSelected
                    showLocationPicker = true
                },
                allTags = state.allTags,
                isEditMode = true,
            )
        }
    }

    if (showPreferences) {
        val prefsSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPreferences = false },
            sheetState = prefsSheetState
        ) {
            NotificationPreferencesSheet(
                settings = state.notificationSettings,
                onSave = {
                    viewModel.updateNotificationSettings(it)
                    showPreferences = false
                },
                onDismiss = { showPreferences = false },
            )
        }
    }

    if (showS3BackupSettings) {
        val s3SheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showS3BackupSettings = false },
            sheetState = s3SheetState
        ) {
            S3BackupSettingsSheet(
                settings = s3Settings,
                lastResult = lastBackupResult,
                onSave = {
                    viewModel.updateS3BackupSettings(it)
                },
                onBackup = { viewModel.performS3Backup() },
                onClearResult = { viewModel.clearBackupResult() },
                onDismiss = { showS3BackupSettings = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    state: DailyLifeState,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onItemSelected: (Long) -> Unit,
    onStorageErrorDismissed: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onTypeSelected: (LifeItemType?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onFavoritesOnlyToggled: () -> Unit,
    onClearFilters: () -> Unit,
    onFavoriteToggled: (Long) -> Unit,
    onPinnedToggled: (Long) -> Unit,
    onTaskStatusChanged: (Long, TaskStatus) -> Unit,
    onCompleted: (Long) -> Unit,
    onCollectionSelected: (List<LifeItem>) -> Unit,
    onShowQuickAdd: () -> Unit,
    onShowPreferences: () -> Unit,
    onShowS3Backup: () -> Unit,
    contentPadding: PaddingValues,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DailyLife",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                },
                actions = {
                    IconButton(onClick = onShowS3Backup) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = "Cloud backup settings",
                        )
                    }
                    IconButton(
                        onClick = onShowPreferences,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.scaleIn(
                    initialScale = 0.7f,
                    animationSpec = DailyLifeTween.emphasized<Float>(),
                ) + fadeIn(DailyLifeTween.fade<Float>()),
                exit = fadeOut(DailyLifeTween.fade<Float>()),
            ) {
                ExtendedFloatingActionButton(
                    onClick = onShowQuickAdd,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add") },
                )
            }
        },
    ) { paddingValues ->
        androidx.compose.animation.Crossfade(
            targetState = selectedTab,
            animationSpec = DailyLifeTween.content(),
            label = "tabTransition",
        ) { currentTab ->
            when (currentTab) {
                HomeTab.Photos -> {
                    PhotosMosaicScreen(
                        state = state,
                        contentPadding = paddingValues,
                        onItemSelected = onItemSelected,
                        onStorageErrorDismissed = onStorageErrorDismissed,
                    )
                }

                HomeTab.Search -> {
                    TimelineScreen(
                        state = state,
                        contentPadding = paddingValues,
                        onSearchChanged = onSearchChanged,
                        onTypeSelected = onTypeSelected,
                        onTagSelected = onTagSelected,
                        onDateRangeChanged = onDateRangeChanged,
                        onFavoritesOnlyToggled = onFavoritesOnlyToggled,
                        onClearFilters = onClearFilters,
                        onItemSelected = onItemSelected,
                        onFavoriteToggled = onFavoriteToggled,
                        onPinnedToggled = onPinnedToggled,
                        onTaskStatusChanged = onTaskStatusChanged,
                        onCompleted = onCompleted,
                        onStorageErrorDismissed = onStorageErrorDismissed,
                    )
                }

                HomeTab.Collections -> {
                    CollectionsScreen(
                        state = state,
                        contentPadding = paddingValues,
                        onCollectionSelected = onCollectionSelected,
                    )
                }

                HomeTab.Graph -> {
                    GraphViewScreen(
                        items = state.visibleItems,
                        contentPadding = paddingValues,
                        onItemSelected = onItemSelected,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotosMosaicScreen(
    state: DailyLifeState,
    contentPadding: PaddingValues,
    onItemSelected: (Long) -> Unit,
    onStorageErrorDismissed: () -> Unit,
) {
    val groupedItems = remember(state.visibleItems) {
        state.visibleItems.groupBy { it.createdAt.toLocalDate() }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 132.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 10.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            end = 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 92.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        state.storageError?.let { storageError ->
            item(key = "storage-error", span = StaggeredGridItemSpan.FullLine) {
                StorageWarningCard(
                    error = storageError,
                    onDismiss = onStorageErrorDismissed,
                )
            }
        }

        if (groupedItems.isEmpty()) {
            item(key = "empty-state", span = StaggeredGridItemSpan.FullLine) {
                EmptyPhotosScreen()
            }
        } else {
            var globalIndex = 0
            groupedItems.forEach { (date, itemsForDate) ->
                val dateIdx = globalIndex
                globalIndex++
                item(key = "date-$date", span = StaggeredGridItemSpan.FullLine) {
                    val dateVisible = rememberStaggeredVisibility(dateIdx, baseDelayMs = 40, maxDelayMs = 300)
                    AnimatedVisibility(
                        visibleState = dateVisible,
                        enter = StaggeredEnter,
                    ) {
                        DateHeader(date = date)
                    }
                }
                staggeredItems(itemsForDate, key = { it.id }) { item ->
                    val itemIdx = globalIndex
                    globalIndex++
                    val tileVisible = rememberStaggeredVisibility(itemIdx, baseDelayMs = 45, maxDelayMs = 450)
                    AnimatedVisibility(
                        visibleState = tileVisible,
                        enter = StaggeredEnter,
                    ) {
                        MediaMosaicTile(
                            item = item,
                            onClick = { onItemSelected(item.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionsScreen(
    state: DailyLifeState,
    contentPadding: PaddingValues,
    onCollectionSelected: (List<LifeItem>) -> Unit,
) {
    val favoriteItems = remember(state.visibleItems) { state.visibleItems.filter { it.isFavorite } }
    val videoItems = remember(state.visibleItems) {
        state.visibleItems.filter { it.type == LifeItemType.Video }
    }
    val placeItems = remember(state.visibleItems) {
        state.visibleItems.filter { it.type == LifeItemType.Location || it.inferLocationPreview() != null }
    }
    val notes = remember(state.visibleItems) {
        state.visibleItems.filter {
            it.type == LifeItemType.Note ||
                it.type == LifeItemType.Thought ||
                it.type == LifeItemType.Task ||
                it.type == LifeItemType.Reminder
        }
    }

    val collectionsData = remember(favoriteItems.size, videoItems.size, placeItems.size, notes.size) {
        listOf(
            Triple("Favorites", "Pinned and loved memories", Icons.Filled.Star) to favoriteItems,
            Triple("Videos", "Tap to open playback items", Icons.Filled.Videocam) to videoItems,
            Triple("Places", "Items with map context", Icons.Filled.LocationOn) to placeItems,
            Triple("Notes & Thoughts", "Text-first memories and reminders", Icons.Filled.EditNote) to notes,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 100.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            val headerVisible = rememberStaggeredVisibility(0, baseDelayMs = 30, maxDelayMs = 150)
            AnimatedVisibility(visibleState = headerVisible, enter = StaggeredEnter) {
                Text(
                    text = "Collections",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        itemsIndexed(collectionsData) { index, (meta, items) ->
            val cardVisible = rememberStaggeredVisibility(index + 1, baseDelayMs = 70, maxDelayMs = 500)
            AnimatedVisibility(visibleState = cardVisible, enter = StaggeredEnter) {
                CollectionCard(
                    title = meta.first,
                    subtitle = meta.second,
                    count = items.size,
                    icon = meta.third,
                    onClick = { onCollectionSelected(items) },
                )
            }
        }
    }
}

@Composable
private fun CollectionCard(
    title: String,
    subtitle: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            AnimatedCounter(
                value = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MediaMosaicTile(
    item: LifeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

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

    PressableCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(item.inferMosaicHeight()),
        shape = RectangleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.then(mediaSharedModifier)) {
                ItemPreview(item = item)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.65f),
                            ),
                        ),
                    )
                    .padding(10.dp),
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun ItemPreview(item: LifeItem, autoplayVideo: Boolean = false) {
    when (item.type) {
        LifeItemType.Photo -> ImagePreview(item = item)
        LifeItemType.Video -> VideoPreview(item = item, autoplay = autoplayVideo)
        LifeItemType.Audio -> AudioPreview(item = item)
        LifeItemType.Location -> LocationPreview(item = item)
        LifeItemType.Mixed -> {
            when {
                item.inferImagePreviewUrl() != null -> ImagePreview(item = item)
                item.inferLocationPreview() != null -> LocationPreview(item = item)
                item.inferVideoPlaybackUrl() != null -> VideoPreview(item = item, autoplay = autoplayVideo)
                item.inferAudioUrl() != null -> AudioPreview(item = item)
                else -> TextPreview(item = item)
            }
        }

        else -> {
            when {
                item.inferImagePreviewUrl() != null -> ImagePreview(item = item)
                item.inferVideoPlaybackUrl() != null -> VideoPreview(item = item, autoplay = autoplayVideo)
                item.inferAudioUrl() != null -> AudioPreview(item = item)
                item.inferLocationPreview() != null -> LocationPreview(item = item)
                else -> TextPreview(item = item)
            }
        }
    }
}

@Composable
internal fun TextPreview(item: LifeItem) {
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
internal fun ImagePreview(item: LifeItem) {
    val imageUrl = rememberDecryptedMediaUri(item.inferImagePreviewUrl())
    if (imageUrl != null) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Image preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            },
            error = {
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
internal fun VideoPreview(item: LifeItem, autoplay: Boolean = false) {
    val videoUrl = rememberDecryptedMediaUri(item.inferVideoPlaybackUrl())

    if (autoplay && videoUrl != null) {
        val context = LocalContext.current
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
        val imageUrl = rememberDecryptedMediaUri(item.inferImagePreviewUrl())
        val thumbUrl = rememberVideoThumbnail(item)
        val displayUrl = imageUrl ?: thumbUrl
        Box(modifier = Modifier.fillMaxSize()) {
            if (displayUrl != null) {
                AsyncImage(
                    model = displayUrl,
                    contentDescription = "Video preview",
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
internal fun AudioPreview(item: LifeItem) {
    val waveform = rememberAudioWaveform(item)
    val targetHeights = if (waveform.isNotEmpty()) {
        waveform.map { (16.dp + (it * 24).dp).coerceAtLeast(4.dp) }
    } else {
        remember(item.id) {
            listOf(8.dp, 16.dp, 10.dp, 22.dp, 14.dp, 20.dp, 12.dp, 18.dp)
        }
    }
    val animatedHeights = targetHeights.mapIndexed { index, target ->
        animateFloatAsState(
            targetValue = target.value,
            animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Gentle,
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
internal fun LocationPreview(item: LifeItem) {
    val location = item.inferLocationPreview()
    if (location != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            OpenStreetMapPreview(
                latitude = location.first,
                longitude = location.second,
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
internal fun OpenStreetMapPreview(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                isTilesScaledToDpi = true
                minZoomLevel = 2.0
                maxZoomLevel = 19.5
            }
        },
        update = { mapView ->
            val point = GeoPoint(latitude, longitude)
            mapView.controller.setZoom(14.5)
            mapView.controller.setCenter(point)
            mapView.overlays.removeAll { overlay -> overlay is Marker }
            mapView.overlays.add(
                Marker(mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = null
                    title = "Saved location"
                },
            )
            mapView.invalidate()
        },
    )
}

@Composable
internal fun StorageWarningCard(
    error: StorageError,
    onDismiss: () -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.padding(top = 2.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Local storage needs attention",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss storage warning")
            }
        }
    }
}

@Composable
internal fun SnapshotRow(state: DailyLifeState) {
    val today = LocalDate.now()
    val completionCount = state.items.sumOf { it.occurrenceStats(today).completedCount }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AnimatedSnapshotPill(
            label = "Items",
            value = state.items.size,
            modifier = Modifier.weight(1f),
        )
        AnimatedSnapshotPill(
            label = "Tags",
            value = state.allTags.size,
            modifier = Modifier.weight(1f),
        )
        AnimatedSnapshotPill(
            label = "Done",
            value = completionCount,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun AnimatedSnapshotPill(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            AnimatedCounter(
                value = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun DateHeader(date: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = date.format(DateFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = DividerDefaults.color.copy(alpha = 0.6f),
        )
    }
}

@Composable
internal fun TypeBadge(
    type: LifeItemType,
    modifier: Modifier = Modifier,
    boxSize: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .size(boxSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = type.icon(),
            contentDescription = type.label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(boxSize * 0.45f),
        )
    }
}

@Composable
internal fun ReminderDateTimeRow(
    reminderDate: LocalDate?,
    reminderTime: LocalTime?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onDateClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Select reminder date",
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = reminderDate?.format(FilterDateFormatter) ?: "Date",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedButton(
            onClick = onTimeClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = "Select reminder time",
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = reminderTime?.format(TimeFormatter) ?: "Time",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (reminderDate != null || reminderTime != null) {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = "Clear reminder")
            }
        }
    }
}

internal fun inferTypeFromBody(body: String): LifeItemType? {
    val trimmed = body.trim().lowercase()
    return when {
        trimmed.startsWith("geo:") -> LifeItemType.Location
        trimmed.startsWith("http") && Regex("\\.(png|jpe?g|webp|gif|bmp|avif)(\\?\\S*)?$", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) -> LifeItemType.Photo
        trimmed.startsWith("http") && Regex("\\.(mp4|m4v|webm|mkv|mov|m3u8)(\\?\\S*)?$", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) -> LifeItemType.Video
        trimmed.startsWith("content://") || trimmed.startsWith("file://") -> {
            when {
                Regex("\\.(png|jpe?g|webp|gif|bmp|avif)(\\.enc)?$", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) -> LifeItemType.Photo
                Regex("\\.(mp4|m4v|webm|mkv|mov)(\\.enc)?$", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) -> LifeItemType.Video
                Regex("\\.(mp3|aac|wav|ogg|m4a|flac)(\\.enc)?$", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) -> LifeItemType.Audio
                else -> null
            }
        }
        else -> null
    }
}

@Composable
private fun NotificationPreferencesSheet(
    settings: NotificationSettings,
    onSave: (NotificationSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var globalEnabled by rememberSaveable(settings) { mutableStateOf(settings.globalEnabled) }
    var preferredTime by rememberSaveable(settings) {
        mutableStateOf(settings.preferredTime.format(TimeFormatter))
    }
    var flexibleWindow by rememberSaveable(settings) {
        mutableStateOf(settings.flexibleWindowMinutes.toString())
    }
    var snooze by rememberSaveable(settings) {
        mutableStateOf(settings.defaultSnoozeMinutes.toString())
    }
    var batchNotifications by rememberSaveable(settings) { mutableStateOf(settings.batchNotifications) }
    var respectDnd by rememberSaveable(settings) { mutableStateOf(settings.respectDoNotDisturb) }
    var canScheduleExactAlarms by remember { mutableStateOf(context.canScheduleExactAlarms()) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canScheduleExactAlarms = context.canScheduleExactAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Notification preferences",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        ToggleRow(
            icon = if (globalEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
            label = "Global notifications",
            checked = globalEnabled,
            onCheckedChange = { globalEnabled = it },
        )
        OutlinedTextField(
            value = preferredTime,
            onValueChange = { preferredTime = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Preferred time, HH:mm") },
            leadingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = flexibleWindow,
                onValueChange = { flexibleWindow = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Window min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = snooze,
                onValueChange = { snooze = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Snooze min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        ToggleRow(
            icon = Icons.Filled.Category,
            label = "Batch reminders",
            checked = batchNotifications,
            onCheckedChange = { batchNotifications = it },
        )
        ToggleRow(
            icon = Icons.Filled.CheckCircle,
            label = "Respect Do Not Disturb",
            checked = respectDnd,
            onCheckedChange = { respectDnd = it },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Exact alarm access",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (canScheduleExactAlarms) {
                            "Enabled. Reminders can run at exact times when no flexible window is used."
                        } else {
                            "Not enabled. DailyLife will still schedule reminders, but the system may delay delivery."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = { context.openExactAlarmSettings() }) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage exact alarm access")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    onSave(
                        NotificationSettings(
                            globalEnabled = globalEnabled,
                            preferredTime = parseTimeOrNull(preferredTime) ?: settings.preferredTime,
                            flexibleWindowMinutes = flexibleWindow.toIntOrNull()?.coerceAtLeast(0)
                                ?: settings.flexibleWindowMinutes,
                            defaultSnoozeMinutes = snooze.toIntOrNull()?.coerceAtLeast(1)
                                ?: settings.defaultSnoozeMinutes,
                            batchNotifications = batchNotifications,
                            respectDoNotDisturb = respectDnd,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
internal fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun EmptyTimeline() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "emptyFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeRepeat.float<Float>(duration = 2200),
        label = "float"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeRepeat.breathe<Float>(duration = 3000),
        label = "scalePulse"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "No results",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer {
                        translationY = floatOffset
                        scaleX = scalePulse
                        scaleY = scalePulse
                    },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No items match these filters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun EmptyPhotosScreen() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "emptyFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeRepeat.float<Float>(),
        label = "float"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeRepeat.breathe<Float>(duration = 3500),
        label = "scalePulse"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        translationY = floatOffset
                        scaleX = scalePulse
                        scaleY = scalePulse
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = "No memories",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp),
                )
            }

            Text(
                text = "No memories yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Capture photos, videos, and moments to see them beautifully arranged here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Tap the + Add button to get started",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

internal fun LifeItemType.icon(): ImageVector = when (this) {
    LifeItemType.Thought -> Icons.Filled.Lightbulb
    LifeItemType.Note -> Icons.Filled.EditNote
    LifeItemType.Task -> Icons.Filled.Checklist
    LifeItemType.Reminder -> Icons.Filled.Alarm
    LifeItemType.Photo -> Icons.Filled.PhotoCamera
    LifeItemType.Video -> Icons.Filled.Videocam
    LifeItemType.Audio -> Icons.Filled.Mic
    LifeItemType.Location -> Icons.Filled.LocationOn
    LifeItemType.Mixed -> Icons.Filled.Category
}

internal fun LifeItemType.isMediaLike(): Boolean =
    this == LifeItemType.Photo ||
        this == LifeItemType.Video ||
        this == LifeItemType.Audio ||
        this == LifeItemType.Location ||
        this == LifeItemType.Mixed

private val GeoPattern =
    Regex("""geo:\s*([-+]?\d{1,2}(?:\.\d+)?),\s*([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
private val LatLonPattern =
    Regex("""([-+]?\d{1,2}(?:\.\d+)?)\s*[, ]\s*([-+]?\d{1,3}(?:\.\d+)?)""")
private val OsmMlatPattern =
    Regex("""[?&]mlat=([-+]?\d{1,2}(?:\.\d+)?).*?[?&]mlon=([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

@Composable
internal fun rememberDecryptedMediaUri(uriString: String?): String? {
    val context = LocalContext.current
    return remember(uriString) {
        if (uriString == null) return@remember null
        if (uriString.endsWith(".enc")) {
            val manager = MediaEncryptionManager(context)
            manager.decryptToCache(android.net.Uri.parse(uriString), context)?.toString()
        } else {
            uriString
        }
    }
}

@Composable
internal fun rememberVideoThumbnail(item: LifeItem): String? {
    val context = LocalContext.current
    val videoUrl = rememberDecryptedMediaUri(item.inferVideoPlaybackUrl())
    return remember(item.id, item.body, videoUrl) {
        if (videoUrl == null) return@remember null
        val generator = MediaThumbnailGenerator(context)
        generator.generateVideoThumbnail(android.net.Uri.parse(videoUrl), context)?.toString()
    }
}

@Composable
internal fun rememberAudioWaveform(item: LifeItem): List<Float> {
    val context = LocalContext.current
    val rawAudioUrl = item.inferAudioUrl()
        ?: item.body.split("\\s+".toRegex()).firstOrNull { it.startsWith("content://") || it.startsWith("file://") }
    val decryptedUrl = rememberDecryptedMediaUri(rawAudioUrl)
    return remember(item.id, decryptedUrl) {
        if (decryptedUrl == null) return@remember emptyList()
        val generator = AudioWaveformGenerator()
        generator.generateWaveform(context, android.net.Uri.parse(decryptedUrl), barCount = 8) ?: emptyList()
    }
}

private fun LifeItem.inferMosaicHeight(): Dp {
    val bucket = ((id % 7L) + 7L) % 7L
    return when (type) {
        LifeItemType.Photo -> if (bucket % 3L == 0L) 222.dp else 164.dp
        LifeItemType.Video -> if (bucket % 2L == 0L) 214.dp else 168.dp
        LifeItemType.Location -> 198.dp
        LifeItemType.Audio -> 156.dp
        LifeItemType.Mixed -> if (bucket % 2L == 0L) 228.dp else 172.dp
        else -> {
            when {
                inferImagePreviewUrl() != null -> if (bucket % 3L == 0L) 222.dp else 164.dp
                inferVideoPlaybackUrl() != null -> if (bucket % 2L == 0L) 214.dp else 168.dp
                inferAudioUrl() != null -> 156.dp
                inferLocationPreview() != null -> 198.dp
                body.length > 120 -> 198.dp
                else -> 152.dp
            }
        }
    }
}

internal fun LifeItem.inferLocationPreview(): Pair<Double, Double>? {
    val source = listOf(title, body).joinToString(" ")

    val geoMatch = GeoPattern.find(source)
    if (geoMatch != null) {
        return geoMatch.groupValues[1].toDoubleOrNull()
            ?.let { lat ->
                geoMatch.groupValues[2].toDoubleOrNull()?.let { lon -> lat to lon }
            }
            ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
    }

    val osmMatch = OsmMlatPattern.find(source)
    if (osmMatch != null) {
        return osmMatch.groupValues[1].toDoubleOrNull()
            ?.let { lat ->
                osmMatch.groupValues[2].toDoubleOrNull()?.let { lon -> lat to lon }
            }
            ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
    }

    return LatLonPattern.find(source)
        ?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull() ?: return@let null
            val lon = match.groupValues[2].toDoubleOrNull() ?: return@let null
            lat to lon
        }
        ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
}

internal fun parseTags(input: String): Set<String> =
    input.split(",")
        .map { it.trim().removePrefix("#") }
        .filter { it.isNotBlank() }
        .toSet()

internal fun parseDateOrNull(input: String): LocalDate? =
    runCatching { LocalDate.parse(input.trim()) }.getOrNull()

internal fun parseTimeOrNull(input: String): LocalTime? =
    runCatching { LocalTime.parse(input.trim(), TimeFormatter) }.getOrNull()

internal fun parseReminderDateTime(dateInput: String, timeInput: String): LocalDateTime? {
    val date = parseDateOrNull(dateInput) ?: return null
    val time = parseTimeOrNull(timeInput) ?: DefaultReminderTime
    return LocalDateTime.of(date, time)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun S3BackupSettingsSheet(
    settings: S3BackupSettings,
    lastResult: com.raulshma.dailylife.domain.BackupResult?,
    onSave: (S3BackupSettings) -> Unit,
    onBackup: () -> Unit,
    onClearResult: () -> Unit,
    onDismiss: () -> Unit,
) {
    var enabled by rememberSaveable(settings) { mutableStateOf(settings.enabled) }
    var endpoint by rememberSaveable(settings) { mutableStateOf(settings.endpoint) }
    var bucketName by rememberSaveable(settings) { mutableStateOf(settings.bucketName) }
    var region by rememberSaveable(settings) { mutableStateOf(settings.region) }
    var accessKeyId by rememberSaveable(settings) { mutableStateOf(settings.accessKeyId) }
    var secretAccessKey by rememberSaveable(settings) { mutableStateOf(settings.secretAccessKey) }
    var pathPrefix by rememberSaveable(settings) { mutableStateOf(settings.pathPrefix) }
    var autoBackup by rememberSaveable(settings) { mutableStateOf(settings.autoBackup) }
    var backupFrequencyHours by rememberSaveable(settings) {
        mutableStateOf(settings.backupFrequencyHours.toString())
    }
    var encryptBackups by rememberSaveable(settings) { mutableStateOf(settings.encryptBackups) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Cloud backup (BYOK S3)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        ToggleRow(
            icon = Icons.Filled.CloudUpload,
            label = "Enable S3 backup",
            checked = enabled,
            onCheckedChange = { enabled = it },
        )

        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("S3 endpoint URL") },
            placeholder = { Text("https://s3.amazonaws.com") },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = bucketName,
                onValueChange = { bucketName = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Bucket") },
            )
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Region") },
            )
        }

        OutlinedTextField(
            value = accessKeyId,
            onValueChange = { accessKeyId = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Access key ID") },
        )

        OutlinedTextField(
            value = secretAccessKey,
            onValueChange = { secretAccessKey = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Secret access key") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        )

        OutlinedTextField(
            value = pathPrefix,
            onValueChange = { pathPrefix = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Path prefix") },
        )

        ToggleRow(
            icon = Icons.Filled.EventRepeat,
            label = "Auto-backup",
            checked = autoBackup,
            onCheckedChange = { autoBackup = it },
        )

        if (autoBackup) {
            OutlinedTextField(
                value = backupFrequencyHours,
                onValueChange = { backupFrequencyHours = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Frequency (hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        ToggleRow(
            icon = Icons.Filled.CheckCircle,
            label = "Encrypt backups",
            checked = encryptBackups,
            onCheckedChange = { encryptBackups = it },
        )

        lastResult?.let { result ->
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = when (result) {
                        is com.raulshma.dailylife.domain.BackupResult.Success ->
                            MaterialTheme.colorScheme.primaryContainer
                        is com.raulshma.dailylife.domain.BackupResult.Failure ->
                            MaterialTheme.colorScheme.errorContainer
                    },
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = when (result) {
                            is com.raulshma.dailylife.domain.BackupResult.Success -> "Backup started"
                            is com.raulshma.dailylife.domain.BackupResult.Failure -> "Backup failed"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when (result) {
                            is com.raulshma.dailylife.domain.BackupResult.Success ->
                                "${result.itemsBackedUp} items, ${result.mediaFilesBackedUp} media files queued."
                            is com.raulshma.dailylife.domain.BackupResult.Failure -> result.reason
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = onClearResult) {
                        Text("Dismiss")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
            Button(
                onClick = {
                    onSave(
                        S3BackupSettings(
                            enabled = enabled,
                            endpoint = endpoint,
                            bucketName = bucketName,
                            region = region,
                            accessKeyId = accessKeyId,
                            secretAccessKey = secretAccessKey,
                            pathPrefix = pathPrefix.ifBlank { "dailylife" },
                            autoBackup = autoBackup,
                            backupFrequencyHours = backupFrequencyHours.toIntOrNull()?.coerceAtLeast(1)
                                ?: settings.backupFrequencyHours,
                            encryptBackups = encryptBackups,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
            Button(
                onClick = onBackup,
                enabled = enabled && endpoint.isNotBlank() && bucketName.isNotBlank() &&
                    accessKeyId.isNotBlank() && secretAccessKey.isNotBlank(),
            ) {
                Text("Backup now")
            }
        }
    }
}

internal fun showDatePicker(
    context: Context,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth,
    ).show()
}

internal fun showTimePicker(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        initialTime.hour,
        initialTime.minute,
        true,
    ).show()
}

private fun Context.canScheduleExactAlarms(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = getSystemService(android.app.AlarmManager::class.java)
    return alarmManager?.canScheduleExactAlarms() ?: false
}

private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val requestIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching { startActivity(requestIntent) }
        .recoverCatching {
            if (it is ActivityNotFoundException) startActivity(appDetailsIntent) else throw it
        }
}
