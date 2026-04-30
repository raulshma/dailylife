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
import android.util.Base64
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
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.security.MediaDecryptCoordinator
import com.raulshma.dailylife.domain.DailyLifeFilters
import com.raulshma.dailylife.domain.DailyLifeState
import com.raulshma.dailylife.domain.EngineState
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
import com.raulshma.dailylife.domain.WritingTone
import com.raulshma.dailylife.domain.displayBody
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferPdfUrl
import com.raulshma.dailylife.domain.inferTypeFromText
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.data.CollectionCounts
import com.raulshma.dailylife.domain.SnapshotStats
import com.raulshma.dailylife.domain.supports
import com.raulshma.dailylife.ui.TimelineEntry
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.raulshma.dailylife.ui.capture.AudioRecorder
import com.raulshma.dailylife.ui.capture.LocationPickerSheet
import com.raulshma.dailylife.ui.capture.SpeechTranscriber
import com.raulshma.dailylife.ui.capture.hasAudioPermission
import com.raulshma.dailylife.ui.capture.hasCameraPermission
import com.raulshma.dailylife.ui.capture.rememberMediaCaptureLauncher
import com.raulshma.dailylife.ui.components.AnimatedCounter
import com.raulshma.dailylife.ui.components.PressableCard
import com.raulshma.dailylife.ui.components.ShimmerBox
import com.raulshma.dailylife.ui.components.SkeletonMosaicTile
import com.raulshma.dailylife.ui.components.StaggeredEnter
import com.raulshma.dailylife.ui.components.rememberStaggeredVisibility
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.detail.ItemDetailScreen
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.isSystemInDarkTheme
import com.raulshma.dailylife.ui.capture.CartoDark
import com.raulshma.dailylife.ui.capture.CartoLight
import com.raulshma.dailylife.ui.capture.EsriSatellite
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
    data object Main : Screen()
    data class Detail(val itemId: Long) : Screen()
    data class CompletionHistory(val itemId: Long) : Screen()
    data class FocusTimer(val itemId: Long) : Screen()
    data object Settings : Screen()
    data object ModelManager : Screen()
    data object AIChatList : Screen()
    data class AIChat(val conversationId: Long? = null) : Screen()
    data object AIMetrics : Screen()
    data object AIReflections : Screen()
    data object AIEnrichment : Screen()
}

internal val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
internal val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
internal val LocalDecryptCoordinator = staticCompositionLocalOf<MediaDecryptCoordinator> {
    error("No MediaDecryptCoordinator provided")
}

data class QuickAddDraft(
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
    val recurrenceFrequency: String = RecurrenceFrequency.None.name,
    val recurrenceDaysOfWeek: String = "",
    val recurrenceDayOfWeek: String = "",
    val recurrenceWeekOfMonth: String = "",
    val geofenceLatitude: String = "",
    val geofenceLongitude: String = "",
    val geofenceTrigger: String = "Arrival",
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
            "recurrenceFrequency" to it.recurrenceFrequency,
            "recurrenceDaysOfWeek" to it.recurrenceDaysOfWeek,
            "recurrenceDayOfWeek" to it.recurrenceDayOfWeek,
            "recurrenceWeekOfMonth" to it.recurrenceWeekOfMonth,
            "geofenceLatitude" to it.geofenceLatitude,
            "geofenceLongitude" to it.geofenceLongitude,
            "geofenceTrigger" to it.geofenceTrigger,
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
            recurrenceFrequency = it["recurrenceFrequency"] as? String ?: RecurrenceFrequency.None.name,
            recurrenceDaysOfWeek = it["recurrenceDaysOfWeek"] as? String ?: "",
            recurrenceDayOfWeek = it["recurrenceDayOfWeek"] as? String ?: "",
            recurrenceWeekOfMonth = it["recurrenceWeekOfMonth"] as? String ?: "",
            geofenceLatitude = it["geofenceLatitude"] as? String ?: "",
            geofenceLongitude = it["geofenceLongitude"] as? String ?: "",
            geofenceTrigger = it["geofenceTrigger"] as? String ?: "Arrival",
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
        putString("recurrenceFrequency", draft.recurrenceFrequency)
        putString("recurrenceDaysOfWeek", draft.recurrenceDaysOfWeek)
        putString("recurrenceDayOfWeek", draft.recurrenceDayOfWeek)
        putString("recurrenceWeekOfMonth", draft.recurrenceWeekOfMonth)
        putString("geofenceLatitude", draft.geofenceLatitude)
        putString("geofenceLongitude", draft.geofenceLongitude)
        putString("geofenceTrigger", draft.geofenceTrigger)
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
        recurrenceFrequency = prefs.getString("recurrenceFrequency", null) ?: RecurrenceFrequency.None.name,
        recurrenceDaysOfWeek = prefs.getString("recurrenceDaysOfWeek", null) ?: "",
        recurrenceDayOfWeek = prefs.getString("recurrenceDayOfWeek", null) ?: "",
        recurrenceWeekOfMonth = prefs.getString("recurrenceWeekOfMonth", null) ?: "",
        geofenceLatitude = prefs.getString("geofenceLatitude", null) ?: "",
        geofenceLongitude = prefs.getString("geofenceLongitude", null) ?: "",
        geofenceTrigger = prefs.getString("geofenceTrigger", null) ?: "Arrival",
        showAdvanced = prefs.getBoolean("showAdvanced", false),
        showReminderOptions = prefs.getBoolean("showReminderOptions", false),
    )
}

private fun clearDraftFromPrefs(context: Context) {
    context.getSharedPreferences(DRAFT_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLifeApp(
    viewModel: DailyLifeViewModel,
    shareDraft: QuickAddDraft? = null,
    onShareDraftConsumed: () -> Unit = {},
    onPaletteChanged: () -> Unit = {},
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingItems.collectAsLazyPagingItems()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle(initialValue = emptyList())
    val snapshotStats by viewModel.snapshotStats.collectAsStateWithLifecycle(initialValue = SnapshotStats())
    val collectionCounts by viewModel.collectionCounts.collectAsStateWithLifecycle(initialValue = CollectionCounts())
    val graphItems by viewModel.taggedItemsForGraph.collectAsStateWithLifecycle(initialValue = emptyList())
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedTabName by rememberSaveable { mutableStateOf(HomeTab.Photos.name) }
    val selectedTab = HomeTab.entries.firstOrNull { it.name == selectedTabName } ?: HomeTab.Photos
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val s3Settings by viewModel.s3BackupSettings.collectAsStateWithLifecycle()
    val lastBackupResult by viewModel.lastBackupResult.collectAsStateWithLifecycle()
    val encryptionProgress by viewModel.encryptionProgress.collectAsStateWithLifecycle()
    val aiSmartTitle by viewModel.aiSmartTitle.collectAsStateWithLifecycle()
    val aiSummary by viewModel.aiSummary.collectAsStateWithLifecycle()
    val aiTagSuggestions by viewModel.aiTagSuggestions.collectAsStateWithLifecycle()
    val aiMood by viewModel.aiMood.collectAsStateWithLifecycle()
    val aiPhotoDescription by viewModel.aiPhotoDescription.collectAsStateWithLifecycle()
    val aiAudioSummary by viewModel.aiAudioSummary.collectAsStateWithLifecycle()
    val aiRewrittenText by viewModel.aiRewrittenText.collectAsStateWithLifecycle()
    val aiInferredType by viewModel.aiInferredType.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()
    val aiSearchFilters by viewModel.aiSearchFilters.collectAsStateWithLifecycle()
    val isAiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle(initialValue = true)
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle(initialValue = emptyList())
    var quickAddDraft by rememberSaveable(stateSaver = QuickAddDraftSaver) {
        mutableStateOf(QuickAddDraft())
    }
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { loadDraftFromPrefs(context) }
        quickAddDraft = loaded
    }
    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var editItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editDraft by rememberSaveable(stateSaver = QuickAddDraftSaver) {
        mutableStateOf(QuickAddDraft())
    }
    var completionHistoryItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var focusTimerItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showAIModelManager by rememberSaveable { mutableStateOf(false) }
    var showAIChatList by rememberSaveable { mutableStateOf(false) }
    var showAIChat by rememberSaveable { mutableStateOf(false) }
    var activeChatConversationId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showAIMetrics by rememberSaveable { mutableStateOf(false) }
    var showAIReflections by rememberSaveable { mutableStateOf(false) }
    var showAIEnrichment by rememberSaveable { mutableStateOf(false) }
    var isAiSearchActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isAiEnabled) {
        if (!isAiEnabled) {
            showAIChatList = false
            showAIChat = false
            showAIMetrics = false
            showAIReflections = false
            isAiSearchActive = false
        }
    }
    var pendingQuickAddSave by remember { mutableStateOf(false) }
    var pendingEditSave by remember { mutableStateOf(false) }
    var preloadedDetailItem by remember { mutableStateOf<LifeItem?>(null) }

    val screen = when {
        isAiEnabled && showAIMetrics -> Screen.AIMetrics
        isAiEnabled && showAIChat -> Screen.AIChat(activeChatConversationId)
        isAiEnabled && showAIChatList -> Screen.AIChatList
        isAiEnabled && showAIReflections -> Screen.AIReflections
        isAiEnabled && showAIEnrichment -> Screen.AIEnrichment
        isAiEnabled && showAIModelManager -> Screen.ModelManager
        focusTimerItemId != null -> Screen.FocusTimer(focusTimerItemId!!)
        completionHistoryItemId != null -> Screen.CompletionHistory(completionHistoryItemId!!)
        selectedItemId != null -> Screen.Detail(selectedItemId!!)
        showSettings -> Screen.Settings
        else -> Screen.Main
    }

    var skipStaggerAnimation by remember { mutableStateOf(false) }

    BackHandler(enabled = showAIChatList || showAIChat || showAIMetrics || showAIReflections || showAIEnrichment || showAIModelManager || focusTimerItemId != null || completionHistoryItemId != null || selectedItemId != null || showSettings) {
        when {
            showAIChat -> {
                showAIChat = false
                activeChatConversationId = null
                showAIChatList = true
            }
            showAIMetrics -> showAIMetrics = false
            showAIChatList -> showAIChatList = false
            showAIReflections -> showAIReflections = false
            showAIEnrichment -> showAIEnrichment = false
            showAIModelManager -> showAIModelManager = false
            focusTimerItemId != null -> focusTimerItemId = null
            completionHistoryItemId != null -> completionHistoryItemId = null
            selectedItemId != null -> {
                selectedItemId = null
                viewModel.clearSelectedItem()
            }
            showSettings -> showSettings = false
        }
    }

    LaunchedEffect(quickAddDraft) {
        withContext(Dispatchers.IO) { saveDraftToPrefs(context, quickAddDraft) }
    }

    LaunchedEffect(shareDraft) {
        shareDraft?.let { draft ->
            quickAddDraft = draft
            showQuickAdd = true
            onShareDraftConsumed()
        }
    }

    var quickAddLocationCallback by remember { mutableStateOf<((Double, Double, String) -> Unit)?>(null) }
    var editLocationCallback by remember { mutableStateOf<((Double, Double, String) -> Unit)?>(null) }
    var locationPickerInitialLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var locationPickerInitialLon by rememberSaveable { mutableStateOf<Double?>(null) }
    var locationPickerInitialTile by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.decryptCoordinator.clearCache()
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
            val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
            val typeName = when {
                mimeType == "application/pdf" -> LifeItemType.Pdf.name
                mimeType?.startsWith("image/") == true -> LifeItemType.Photo.name
                mimeType?.startsWith("video/") == true -> LifeItemType.Video.name
                mimeType?.startsWith("audio/") == true -> LifeItemType.Audio.name
                else -> quickAddDraft.typeName
            }
            quickAddDraft = quickAddDraft.copy(
                typeName = typeName,
                body = if (quickAddDraft.body.isBlank()) uri.toString() else "${quickAddDraft.body}\n$uri",
            )
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
            val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
            val typeName = when {
                mimeType == "application/pdf" -> LifeItemType.Pdf.name
                mimeType?.startsWith("image/") == true -> LifeItemType.Photo.name
                mimeType?.startsWith("video/") == true -> LifeItemType.Video.name
                mimeType?.startsWith("audio/") == true -> LifeItemType.Audio.name
                else -> editDraft.typeName
            }
            editDraft = editDraft.copy(
                typeName = typeName,
                body = if (editDraft.body.isBlank()) uri.toString() else "${editDraft.body}\n$uri",
            )
        },
    )

    val visibleItemIds by viewModel.allItemIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val photosGridState = rememberLazyStaggeredGridState()
    val timelineListState = rememberLazyListState()

    CompositionLocalProvider(LocalDecryptCoordinator provides viewModel.decryptCoordinator) {
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
                    initial is Screen.Main && target is Screen.Detail -> {
                        enter = fadeIn(DailyLifeTween.content<Float>())
                        exit = fadeOut(DailyLifeTween.fade<Float>())
                    }
                    initial is Screen.Detail && target is Screen.Main -> {
                        enter = fadeIn(DailyLifeTween.content<Float>())
                        exit = fadeOut(DailyLifeTween.fade<Float>())
                    }
                    initial is Screen.Main && target is Screen.Settings -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                    }
                    initial is Screen.Settings && target is Screen.Main -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                    }
                    target is Screen.Detail -> {
                        enter = fadeIn(DailyLifeTween.content<Float>()) + slideInHorizontally(DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>()) { it / 6 }
                        exit = fadeOut(DailyLifeTween.fade<Float>()) + slideOutHorizontally(DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>()) { -it / 8 }
                    }
                    target is Screen.Settings -> {
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
                        pagingItems = pagingItems,
                        snapshotStats = snapshotStats,
                        collectionCounts = collectionCounts,
                        graphItems = graphItems,
                        allTags = allTags,
                        selectedTab = selectedTab,
                        skipStaggerAnimation = skipStaggerAnimation,
                        photosGridState = photosGridState,
                        timelineListState = timelineListState,
                        onTabSelected = {
                            if (it == HomeTab.Photos) viewModel.clearFilters()
                            selectedTabName = it.name
                        },
                        onItemSelected = { id, item ->
                            skipStaggerAnimation = true
                            preloadedDetailItem = item
                            selectedItemId = id
                            viewModel.selectItem(id, item)
                        },
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
                        onCollectionSelected = { collection ->
                            viewModel.clearFilters()
                            when (collection) {
                                "favorites" -> {
                                    viewModel.toggleFavoritesOnly()
                                }
                                "videos" -> {
                                    viewModel.selectTypes(setOf(LifeItemType.Video))
                                }
                                "pdfs" -> {
                                    viewModel.selectType(LifeItemType.Pdf)
                                }
                                "places" -> {
                                    viewModel.selectType(LifeItemType.Location)
                                }
                                "notes" -> {
                                    viewModel.selectTypes(setOf(LifeItemType.Note, LifeItemType.Thought, LifeItemType.Task, LifeItemType.Reminder))
                                }
                            }
                            selectedTabName = HomeTab.Search.name
                        },
                        onShowQuickAdd = { showQuickAdd = true },
                        onShowSettings = { showSettings = true },
                        onShowAIChat = if (isAiEnabled) { { showAIChatList = true } } else null,
                        onShowAIReflections = if (isAiEnabled) { { showAIReflections = true } } else null,
                        onShowAIEnrichment = if (isAiEnabled) { { showAIEnrichment = true } } else null,
                        isAiSearchActive = if (isAiEnabled) isAiSearchActive else false,
                        isAiGenerating = isAiGenerating,
                        engineState = engineState,
                        onToggleAiSearch = if (isAiEnabled) { { isAiSearchActive = !isAiSearchActive } } else null,
                        onAiSearchQuery = { viewModel.naturalLanguageSearch(it) },
                        contentPadding = PaddingValues(),
                    )

                    is Screen.Detail -> (selectedItem ?: preloadedDetailItem)?.let { item ->
                        ItemDetailScreen(
                            item = item,
                            globalSettings = state.notificationSettings,
                            navigableItemIds = visibleItemIds,
                            onBack = {
                                selectedItemId = null
                                viewModel.clearSelectedItem()
                            },
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
                                    recurrenceFrequency = item.recurrenceRule.frequency.name,
                                    recurrenceDaysOfWeek = item.recurrenceRule.daysOfWeek.joinToString(",") { it.name },
                                    recurrenceDayOfWeek = item.recurrenceRule.dayOfWeek?.name ?: "",
                                    recurrenceWeekOfMonth = item.recurrenceRule.weekOfMonth?.name ?: "",
                                    geofenceLatitude = item.notificationSettings.geofenceLatitude?.toString() ?: "",
                                    geofenceLongitude = item.notificationSettings.geofenceLongitude?.toString() ?: "",
                                    geofenceTrigger = item.notificationSettings.geofenceTrigger.name,
                                )
                                showEditSheet = true
                            },
                            onDelete = {
                                viewModel.deleteItem(item.id)
                                selectedItemId = null
                                preloadedDetailItem = null
                                viewModel.clearSelectedItem()
                            },
                            onNavigateToItem = {
                                preloadedDetailItem = null
                                selectedItemId = it
                                viewModel.selectItem(it)
                            },
                            onViewHistory = { completionHistoryItemId = item.id },
                            onStartFocusTimer = { focusTimerItemId = item.id },
                            isAiEnabled = isAiEnabled,
                            isFeatureAvailable = { feature -> viewModel.isFeatureAvailable(feature) },
                            aiGeneratedTitle = if (isAiEnabled) aiSmartTitle else "",
                            aiGeneratedDescription = if (isAiEnabled) aiSummary else "",
                            aiGeneratedTags = if (isAiEnabled) aiTagSuggestions else emptyList(),
                            aiMood = if (isAiEnabled) aiMood else null,
                            aiPhotoDescription = if (isAiEnabled) aiPhotoDescription else "",
                            aiAudioSummary = if (isAiEnabled) aiAudioSummary else "",
                            isAiGenerating = if (isAiEnabled) isAiGenerating else false,
                            aiError = if (isAiEnabled) aiError else null,
                            onGenerateTitle = { body -> viewModel.generateSmartTitle(body) },
                            onGenerateDescription = { title, body -> viewModel.summarizeEntry(title, body) },
                            onSuggestTags = { title, body -> viewModel.suggestTags(title, body) },
                            onAnalyzeMood = { title, body -> viewModel.analyzeMood(title, body) },
                            onDescribePhoto = { uri -> viewModel.describePhotoFromUri(uri) },
                            onSummarizeAudio = { uri -> viewModel.summarizeAudioFromUri(uri) },
                            onApplyTitle = { id, title -> viewModel.applyAiTitle(id, title) },
                            onApplyTags = { id, tags -> viewModel.applyAiTags(id, tags) },
                            onApplyDescription = { id, desc -> viewModel.applyAiSummary(id, desc) },
                            onClearAiError = { viewModel.clearAiError() },
                            onCancelAi = { viewModel.cancelAiGeneration() },
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    is Screen.CompletionHistory -> {
                        LaunchedEffect(currentScreen.itemId) {
                            viewModel.selectItem(currentScreen.itemId)
                        }
                        val historyItem by viewModel.selectedItem.collectAsStateWithLifecycle()
                        if (historyItem != null) {
                            com.raulshma.dailylife.ui.detail.CompletionHistoryScreen(
                                item = historyItem!!,
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

                    is Screen.FocusTimer -> {
                        LaunchedEffect(currentScreen.itemId) {
                            viewModel.selectItem(currentScreen.itemId)
                        }
                        val focusItem by viewModel.selectedItem.collectAsStateWithLifecycle()
                        if (focusItem != null) {
                            com.raulshma.dailylife.ui.focus.FocusTimerScreen(
                                itemTitle = focusItem!!.title.ifBlank { focusItem!!.type.label },
                                onBack = { focusTimerItemId = null },
                                onSessionComplete = { sessionCount ->
                                    focusItem?.let { viewModel.markOccurrenceCompleted(it.id) }
                                },
                            )
                        } else {
                            focusTimerItemId = null
                        }
                    }

                    is Screen.Settings -> {
                        SettingsScreen(
                            notificationSettings = state.notificationSettings,
                            s3Settings = s3Settings,
                            lastBackupResult = lastBackupResult,
                            onSaveNotifications = {
                                viewModel.updateNotificationSettings(it)
                            },
                            onSaveS3 = {
                                viewModel.updateS3BackupSettings(it)
                            },
                            onBackupNow = { viewModel.performS3Backup() },
                            onClearResult = { viewModel.clearBackupResult() },
                            onNavigateToModelManager = { showAIModelManager = true },
                            aiStorageUsed = viewModel.modelManager.getStorageUsage(),
                            defaultModelName = viewModel.modelManager.getDefaultModel()?.name,
                            isAiEnabled = isAiEnabled,
                            onAiEnabledChanged = { viewModel.setAiEnabled(it) },
                            engineState = engineState,
                            isEnrichmentEnabled = viewModel.enrichmentSettings.collectAsStateWithLifecycle().value.enabled,
                            onNavigateToEnrichment = { showSettings = false; showAIEnrichment = true },
                            onPaletteChanged = onPaletteChanged,
                            onBack = { showSettings = false },
                        )
                    }

                    is Screen.ModelManager -> {
                        com.raulshma.dailylife.ui.ai.ModelManagerScreen(
                            modelManager = viewModel.modelManager,
                            engineService = viewModel.engineService,
                            onBack = { showAIModelManager = false },
                        )
                    }

                    is Screen.AIChatList -> {
                        com.raulshma.dailylife.ui.ai.AIConversationListScreen(
                            chatRepository = viewModel.aiChatRepository,
                            onBack = { showAIChatList = false },
                            onOpenConversation = { conversationId ->
                                showAIChatList = false
                                activeChatConversationId = conversationId
                                showAIChat = true
                            },
                            onNewChat = {
                                showAIChatList = false
                                activeChatConversationId = null
                                showAIChat = true
                            },
                            onViewMetrics = {
                                showAIChatList = false
                                showAIMetrics = true
                            },
                        )
                    }

                    is Screen.AIChat -> {
                        val chatConversationId = (currentScreen as Screen.AIChat).conversationId
                        com.raulshma.dailylife.ui.ai.AIChatScreen(
                            aiExecutor = viewModel.aiExecutor,
                            engineService = viewModel.engineService,
                            chatRepository = viewModel.aiChatRepository,
                            conversationId = chatConversationId,
                            onBack = {
                                showAIChat = false
                                activeChatConversationId = null
                                showAIChatList = true
                            },
                            onNavigateToModelManager = {
                                showAIChat = false
                                showAIModelManager = true
                            },
                        )
                    }

                    is Screen.AIMetrics -> {
                        com.raulshma.dailylife.ui.ai.AIMetricsScreen(
                            chatRepository = viewModel.aiChatRepository,
                            onBack = { showAIMetrics = false },
                        )
                    }

                    is Screen.AIReflections -> {
                        com.raulshma.dailylife.ui.ai.AIReflectionsScreen(
                            aiExecutor = viewModel.aiExecutor,
                            engineService = viewModel.engineService,
                            recentEntries = recentEntries,
                            onBack = { showAIReflections = false },
                            onNavigateToModelManager = { showAIReflections = false; showAIModelManager = true },
                        )
                    }
                    is Screen.AIEnrichment -> {
                        com.raulshma.dailylife.ui.ai.AIEnrichmentScreen(
                            progress = viewModel.enrichmentProgress.collectAsStateWithLifecycle().value,
                            settings = viewModel.enrichmentSettings.collectAsStateWithLifecycle().value,
                            history = viewModel.enrichmentHistory.collectAsStateWithLifecycle().value,
                            modelCapabilities = viewModel.modelManager.getDefaultModel()?.capabilities
                                ?: emptySet(),
                            defaultModelName = viewModel.modelManager.getDefaultModel()?.name,
                            onSettingsChanged = viewModel::updateEnrichmentSettings,
                            onStartBatch = viewModel::startEnrichmentBatch,
                            onPause = viewModel::pauseEnrichment,
                            onResume = viewModel::resumeEnrichment,
                            onCancel = {
                                viewModel.cancelEnrichment()
                                viewModel.resetEnrichmentProgress()
                            },
                            onClearHistory = viewModel::clearEnrichmentHistory,
                            onNavigateBack = { showAIEnrichment = false },
                            onLoadHistory = viewModel::loadEnrichmentHistory,
                        )
                    }
                }
            }
        }
    }

    val quickAddSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(encryptionProgress) {
        if (encryptionProgress == null && pendingQuickAddSave) {
            pendingQuickAddSave = false
            showQuickAdd = false
            quickAddDraft = QuickAddDraft()
            clearDraftFromPrefs(context)
            viewModel.clearAiState()
        }
        if (encryptionProgress == null && pendingEditSave) {
            pendingEditSave = false
            showEditSheet = false
            editItemId = null
            viewModel.clearAiState()
        }
    }

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
                    pendingQuickAddSave = true
                },
                onAddAndContinue = { draft ->
                    viewModel.addItem(draft)
                    viewModel.clearAiState()
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
                    viewModel.clearAiState()
                },
                mediaLauncher = mediaLauncher,
                onShowLocationPicker = { lat, lon, tile, onLocationSelected ->
                    quickAddLocationCallback = onLocationSelected
                    locationPickerInitialLat = lat
                    locationPickerInitialLon = lon
                    locationPickerInitialTile = tile
                    showLocationPicker = true
                },
                allTags = allTags,
                encryptionProgress = encryptionProgress,
                aiSmartTitle = if (isAiEnabled) aiSmartTitle else "",
                aiTagSuggestions = if (isAiEnabled) aiTagSuggestions else emptyList(),
                aiRewrittenText = if (isAiEnabled) aiRewrittenText else "",
                isAiGenerating = if (isAiEnabled) isAiGenerating else false,
                engineState = if (isAiEnabled) engineState else EngineState.Idle,
                onGenerateSmartTitle = if (isAiEnabled) { { viewModel.generateSmartTitle(it) } } else null,
                onSuggestTags = if (isAiEnabled) { t, b -> viewModel.suggestTags(t, b) } else null,
                onRewriteText = if (isAiEnabled) { text, tone -> viewModel.rewriteText(text, tone) } else null,
                onApplyAiTitle = { quickAddDraft = quickAddDraft.copy(title = it) },
                onApplyAiTags = { tags ->
                    val currentSet = quickAddDraft.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    val newTags = (currentSet + tags).joinToString(", ")
                    quickAddDraft = quickAddDraft.copy(tags = newTags)
                },
                onApplyAiRewrite = { quickAddDraft = quickAddDraft.copy(body = it) },
                onClearAiState = { viewModel.clearAiState() },
                aiInferredType = aiInferredType,
                onInferTypeWithAI = if (isAiEnabled) { t, b -> viewModel.inferTypeWithAI(t, b) } else null,
            )
        }
    }

    if (showLocationPicker) {
        LocationPickerSheet(
            initialLatitude = locationPickerInitialLat,
            initialLongitude = locationPickerInitialLon,
            initialTile = locationPickerInitialTile,
            onLocationSelected = { lat, lon, tile ->
                quickAddLocationCallback?.invoke(lat, lon, tile)
                editLocationCallback?.invoke(lat, lon, tile)
                quickAddLocationCallback = null
                editLocationCallback = null
                locationPickerInitialLat = null
                locationPickerInitialLon = null
                locationPickerInitialTile = null
                showLocationPicker = false
            },
            onDismiss = {
                quickAddLocationCallback = null
                editLocationCallback = null
                locationPickerInitialLat = null
                locationPickerInitialLon = null
                locationPickerInitialTile = null
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
                        pendingEditSave = true
                    }
                },
                onAddAndContinue = { _ -> },
                onDismiss = {
                    showEditSheet = false
                    editItemId = null
                },
                onDiscardDraft = {
                    showEditSheet = false
                    editItemId = null
                    viewModel.clearAiState()
                },
                mediaLauncher = editMediaLauncher,
                onShowLocationPicker = { lat, lon, tile, onLocationSelected ->
                    editLocationCallback = onLocationSelected
                    locationPickerInitialLat = lat
                    locationPickerInitialLon = lon
                    locationPickerInitialTile = tile
                    showLocationPicker = true
                },
                allTags = allTags,
                isEditMode = true,
                encryptionProgress = encryptionProgress,
                aiSmartTitle = if (isAiEnabled) aiSmartTitle else "",
                aiTagSuggestions = if (isAiEnabled) aiTagSuggestions else emptyList(),
                aiRewrittenText = if (isAiEnabled) aiRewrittenText else "",
                isAiGenerating = if (isAiEnabled) isAiGenerating else false,
                engineState = if (isAiEnabled) engineState else EngineState.Idle,
                onGenerateSmartTitle = if (isAiEnabled) { { viewModel.generateSmartTitle(it) } } else null,
                onSuggestTags = if (isAiEnabled) { t, b -> viewModel.suggestTags(t, b) } else null,
                onRewriteText = if (isAiEnabled) { text, tone -> viewModel.rewriteText(text, tone) } else null,
                onApplyAiTitle = { editDraft = editDraft.copy(title = it) },
                onApplyAiTags = { tags ->
                    val currentSet = editDraft.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    val newTags = (currentSet + tags).joinToString(", ")
                    editDraft = editDraft.copy(tags = newTags)
                },
                onApplyAiRewrite = { editDraft = editDraft.copy(body = it) },
                onClearAiState = { viewModel.clearAiState() },
                aiInferredType = aiInferredType,
                onInferTypeWithAI = if (isAiEnabled) { t, b -> viewModel.inferTypeWithAI(t, b) } else null,
            )
        }
    }
}
}

@Composable
private fun LazyStaggeredGridState.isScrollingUp(): Boolean {
    var previousIndex by remember { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                firstVisibleItemIndex < previousIndex
            } else {
                firstVisibleItemScrollOffset < previousScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                firstVisibleItemIndex < previousIndex
            } else {
                firstVisibleItemScrollOffset < previousScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    state: DailyLifeState,
    pagingItems: LazyPagingItems<LifeItem>,
    snapshotStats: SnapshotStats,
    collectionCounts: CollectionCounts,
    graphItems: List<LifeItem>,
    allTags: List<String>,
    selectedTab: HomeTab,
    skipStaggerAnimation: Boolean,
    photosGridState: LazyStaggeredGridState,
    timelineListState: LazyListState,
    onTabSelected: (HomeTab) -> Unit,
    onItemSelected: (Long, LifeItem?) -> Unit,
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
    onCollectionSelected: (String) -> Unit,
    onShowQuickAdd: () -> Unit,
    onShowSettings: () -> Unit,
    onShowAIChat: (() -> Unit)? = null,
    onShowAIReflections: (() -> Unit)? = null,
    onShowAIEnrichment: (() -> Unit)? = null,
    isAiSearchActive: Boolean = false,
    isAiGenerating: Boolean = false,
    engineState: EngineState = EngineState.Idle,
    onToggleAiSearch: (() -> Unit)? = null,
    onAiSearchQuery: (String) -> Unit = {},
    contentPadding: PaddingValues,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val statusBarHeight = WindowInsets.safeDrawing.getTop(density)
    val topBarHeightPx = with(density) { 64.dp.roundToPx() }
    val totalTopPx = topBarHeightPx + statusBarHeight

    val isScrollingUp = when (selectedTab) {
        HomeTab.Photos -> photosGridState.isScrollingUp()
        HomeTab.Search -> timelineListState.isScrollingUp()
        else -> true
    }
    val isAtStart = when (selectedTab) {
        HomeTab.Photos -> photosGridState.firstVisibleItemIndex == 0 &&
            photosGridState.firstVisibleItemScrollOffset == 0
        HomeTab.Search -> timelineListState.firstVisibleItemIndex == 0 &&
            timelineListState.firstVisibleItemScrollOffset == 0
        else -> true
    }
    val isCollapsed = !isScrollingUp && !isAtStart

    val topBarOffset by animateFloatAsState(
        targetValue = if (isCollapsed) -totalTopPx.toFloat() else 0f,
        animationSpec = DailyLifeTween.content(),
        label = "topBarOffset",
    )

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
                    val showDropdown = rememberSaveable { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showDropdown.value = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                            )
                        }
                        DropdownMenu(
                            expanded = showDropdown.value,
                            onDismissRequest = { showDropdown.value = false },
                        ) {
                            if (onShowAIChat != null) {
                                DropdownMenuItem(
                                    text = { Text("AI Chat") },
                                    onClick = {
                                        showDropdown.value = false
                                        onShowAIChat()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.SmartToy, contentDescription = null)
                                    },
                                )
                            }
                            if (onShowAIReflections != null) {
                                DropdownMenuItem(
                                    text = { Text("AI Reflection") },
                                    onClick = {
                                        showDropdown.value = false
                                        onShowAIReflections()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                    },
                                )
                            }
                            if (onShowAIEnrichment != null) {
                                DropdownMenuItem(
                                    text = { Text("AI Enrichment") },
                                    onClick = {
                                        showDropdown.value = false
                                        onShowAIEnrichment()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.AutoFixHigh, contentDescription = null)
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showDropdown.value = false
                                    onShowSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.graphicsLayer {
                    translationY = topBarOffset
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
        val adjustedPadding = PaddingValues(
            top = with(density) { (paddingValues.calculateTopPadding() + topBarOffset.toDp()).coerceAtLeast(0.dp) },
            bottom = paddingValues.calculateBottomPadding(),
        )
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(DailyLifeTween.tab<Float>()) togetherWith fadeOut(DailyLifeTween.tab<Float>())
            },
            label = "tabTransition",
        ) { currentTab ->
            when (currentTab) {
                HomeTab.Photos -> {
                    PhotosMosaicScreen(
                        pagingItems = pagingItems,
                        storageError = state.storageError,
                        contentPadding = adjustedPadding,
                        skipStaggerAnimation = skipStaggerAnimation,
                        gridState = photosGridState,
                        onItemSelected = onItemSelected,
                        onStorageErrorDismissed = onStorageErrorDismissed,
                    )
                }

                HomeTab.Search -> {
                    TimelineScreen(
                        state = state,
                        pagingItems = pagingItems,
                        snapshotStats = snapshotStats,
                        allTags = allTags,
                        contentPadding = adjustedPadding,
                        skipStaggerAnimation = skipStaggerAnimation,
                        listState = timelineListState,
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
                        isAiSearchActive = isAiSearchActive,
                        isAiGenerating = isAiGenerating,
                        engineState = engineState,
                        onToggleAiSearch = onToggleAiSearch,
                        onAiSearchQuery = onAiSearchQuery,
                    )
                }

                HomeTab.Collections -> {
                    CollectionsScreen(
                        collectionCounts = collectionCounts,
                        contentPadding = adjustedPadding,
                        onCollectionSelected = onCollectionSelected,
                    )
                }

                HomeTab.Graph -> {
                    GraphViewScreen(
                        items = graphItems,
                        contentPadding = adjustedPadding,
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
    pagingItems: LazyPagingItems<LifeItem>,
    storageError: StorageError?,
    contentPadding: PaddingValues,
    skipStaggerAnimation: Boolean,
    gridState: LazyStaggeredGridState,
    onItemSelected: (Long, LifeItem?) -> Unit,
    onStorageErrorDismissed: () -> Unit,
) {
    val entries = remember(pagingItems.itemSnapshotList) {
        val snapshot = pagingItems.itemSnapshotList
        val result = mutableListOf<TimelineEntry>()
        var lastDate: LocalDate? = null
        for (i in snapshot.indices) {
            val item = snapshot[i] ?: continue
            val date = item.createdAt.toLocalDate()
            if (date != lastDate) {
                result.add(TimelineEntry.DateHeader(date))
                lastDate = date
            }
            result.add(TimelineEntry.Item(i, item.id))
        }
        result
    }
    val isEmpty = entries.isEmpty() && pagingItems.loadState.refresh !is LoadState.Loading

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 132.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 2.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            end = 2.dp,
            bottom = contentPadding.calculateBottomPadding() + 92.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalItemSpacing = 2.dp,
    ) {
        if (pagingItems.loadState.refresh is LoadState.Loading && !isEmpty) {
            items(count = 6, key = { "refresh-skeleton-$it" }) {
                SkeletonMosaicTile(
                    height = listOf(222.dp, 164.dp, 198.dp, 156.dp, 190.dp, 172.dp)[it % 6],
                    modifier = Modifier.animateItem(),
                )
            }
        }

        storageError?.let { storageError ->
            item(key = "storage-error", span = StaggeredGridItemSpan.FullLine) {
                StorageWarningCard(
                    error = storageError,
                    onDismiss = onStorageErrorDismissed,
                )
            }
        }

        if (isEmpty) {
            if (pagingItems.loadState.refresh is LoadState.Error) {
                val error = (pagingItems.loadState.refresh as LoadState.Error).error
                item(key = "refresh-error", span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = error.localizedMessage ?: "Couldn't load items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Button(onClick = { pagingItems.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                item(key = "empty-state", span = StaggeredGridItemSpan.FullLine) {
                    EmptyPhotosScreen()
                }
            }
        } else {
            var globalIndex = 0
            entries.forEach { entry ->
                when (entry) {
                    is TimelineEntry.DateHeader -> {
                        val dateIdx = globalIndex
                        globalIndex++
                        item(key = "date-${entry.date}", span = StaggeredGridItemSpan.FullLine) {
                            if (skipStaggerAnimation) {
                                DateHeader(date = entry.date)
                            } else {
                                val dateVisible = rememberStaggeredVisibility(dateIdx, baseDelayMs = 40, maxDelayMs = 300)
                                AnimatedVisibility(
                                    visibleState = dateVisible,
                                    enter = StaggeredEnter,
                                ) {
                                    DateHeader(date = entry.date)
                                }
                            }
                        }
                    }
                    is TimelineEntry.Item -> {
                        val itemIdx = globalIndex
                        globalIndex++
                        item(
                            key = entry.id,
                        ) {
                            val item = if (entry.index < pagingItems.itemCount) pagingItems[entry.index] else null
                            if (item != null) {
                                if (skipStaggerAnimation) {
                                    MediaMosaicTile(
                                        item = item,
                                        onClick = { onItemSelected(item.id, item) },
                                        modifier = Modifier.animateItem(),
                                    )
                                } else {
                                    val tileVisible = rememberStaggeredVisibility(itemIdx, baseDelayMs = 45, maxDelayMs = 450)
                                    AnimatedVisibility(
                                        visibleState = tileVisible,
                                        enter = StaggeredEnter,
                                    ) {
                                        MediaMosaicTile(
                                            item = item,
                                            onClick = { onItemSelected(item.id, item) },
                                            modifier = Modifier.animateItem(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            pagingItems.apply {
                when (loadState.append) {
                    is LoadState.Loading -> {
                        items(count = 3, key = { "append-skeleton-$it" }) {
                            SkeletonMosaicTile(
                                height = listOf(222.dp, 164.dp, 198.dp)[it % 3],
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                    is LoadState.Error -> {
                        val error = (loadState.append as LoadState.Error).error
                        item(key = "append-error", span = StaggeredGridItemSpan.FullLine) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = error.localizedMessage ?: "Couldn't load more items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                TextButton(onClick = { retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun CollectionsScreen(
    collectionCounts: CollectionCounts,
    contentPadding: PaddingValues,
    onCollectionSelected: (String) -> Unit,
) {
    val collectionsData = listOf(
        Triple("Favorites", "Pinned and loved memories", Icons.Filled.Star) to "favorites",
        Triple("Videos", "Tap to open playback items", Icons.Filled.Videocam) to "videos",
        Triple("PDFs", "Documents and scanned pages", Icons.Filled.PictureAsPdf) to "pdfs",
        Triple("Places", "Items with map context", Icons.Filled.LocationOn) to "places",
        Triple("Notes & Thoughts", "Text-first memories and reminders", Icons.Filled.EditNote) to "notes",
    )

    val counts = listOf(
        collectionCounts.favorites,
        collectionCounts.videos,
        collectionCounts.pdfs,
        collectionCounts.places,
        collectionCounts.notes,
    )

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
        itemsIndexed(collectionsData) { index, (meta, _) ->
            val cardVisible = rememberStaggeredVisibility(index + 1, baseDelayMs = 70, maxDelayMs = 500)
            AnimatedVisibility(visibleState = cardVisible, enter = StaggeredEnter) {
                CollectionCard(
                    title = meta.first,
                    subtitle = meta.second,
                    count = counts[index],
                    icon = meta.third,
                    onClick = { onCollectionSelected(collectionsData[index].second) },
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TypeBadge(type = item.type, modifier = badgeSharedModifier, boxSize = 28.dp)
                        Text(
                            text = item.title,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = titleSharedModifier,
                        )
                    }
                }
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
internal fun VideoPreview(item: LifeItem, autoplay: Boolean = false) {
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

private data class ThumbhashPreviewPalette(
    val colors: List<Color>,
)

@Composable
private fun ThumbhashShimmerPlaceholder(
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

private fun LifeItem.thumbhashPreviewPalette(): ThumbhashPreviewPalette? {
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
internal fun PdfPreview(item: LifeItem) {
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

@Composable
internal fun OpenStreetMapPreview(
    latitude: Double,
    longitude: Double,
    mapTile: String? = null,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                val tileSource = when (mapTile) {
                    "Satellite" -> EsriSatellite
                    "OSM" -> TileSourceFactory.MAPNIK
                    "Auto" -> if (isDarkTheme) CartoDark else CartoLight
                    else -> TileSourceFactory.MAPNIK
                }
                setTileSource(tileSource)
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
            val newSource = when (mapTile) {
                "Satellite" -> EsriSatellite
                "OSM" -> TileSourceFactory.MAPNIK
                "Auto" -> if (isDarkTheme) CartoDark else CartoLight
                else -> TileSourceFactory.MAPNIK
            }
            if (mapView.tileProvider.tileSource != newSource) {
                mapView.setTileSource(newSource)
            }
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
internal fun SnapshotRow(snapshotStats: SnapshotStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AnimatedSnapshotPill(
            label = "Items",
            value = snapshotStats.itemCount,
            modifier = Modifier.weight(1f),
        )
        AnimatedSnapshotPill(
            label = "Tags",
            value = snapshotStats.tagCount,
            modifier = Modifier.weight(1f),
        )
        AnimatedSnapshotPill(
            label = "Done",
            value = snapshotStats.completedCount,
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
    return inferTypeFromText(body)
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
    LifeItemType.Pdf -> Icons.Filled.PictureAsPdf
    LifeItemType.Mixed -> Icons.Filled.Category
}

internal fun LifeItemType.isMediaLike(): Boolean =
    this == LifeItemType.Photo ||
        this == LifeItemType.Video ||
        this == LifeItemType.Audio ||
        this == LifeItemType.Location ||
        this == LifeItemType.Pdf ||
        this == LifeItemType.Mixed

private val GeoPattern =
    Regex("""geo:\s*([-+]?\d{1,2}(?:\.\d+)?),\s*([-+]?\d{1,3}(?:\.\d+)?)(?:\?[^\s]*)?""", RegexOption.IGNORE_CASE)
private val LatLonPattern =
    Regex("""([-+]?\d{1,2}(?:\.\d+)?)\s*[, ]\s*([-+]?\d{1,3}(?:\.\d+)?)""")
private val OsmMlatPattern =
    Regex("""[?&]mlat=([-+]?\d{1,2}(?:\.\d+)?).*?[?&]mlon=([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
private val MapTilePattern =
    Regex("""[?&]mapTile=([^&\s]+)""", RegexOption.IGNORE_CASE)

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
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val generator = AudioWaveformGenerator()
                generator.generateWaveform(context, android.net.Uri.parse(decryptedUrl), barCount = 8)?.toList() ?: emptyList()
            }
        }
    }.value
}

private fun LifeItem.inferMosaicHeight(): Dp {
    val bucket = ((id % 7L) + 7L) % 7L
    return when (type) {
        LifeItemType.Photo -> if (bucket % 3L == 0L) 222.dp else 164.dp
        LifeItemType.Video -> if (bucket % 2L == 0L) 214.dp else 168.dp
        LifeItemType.Location -> 198.dp
        LifeItemType.Audio -> 156.dp
        LifeItemType.Pdf -> 190.dp
        LifeItemType.Mixed -> if (bucket % 2L == 0L) 228.dp else 172.dp
        else -> {
            when {
                inferImagePreviewUrl() != null -> if (bucket % 3L == 0L) 222.dp else 164.dp
                inferVideoPlaybackUrl() != null -> if (bucket % 2L == 0L) 214.dp else 168.dp
                inferAudioUrl() != null -> 156.dp
                inferPdfUrl() != null -> 190.dp
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

internal fun LifeItem.inferLocationMapTile(): String? {
    val source = listOf(title, body).joinToString(" ")
    return MapTilePattern.find(source)?.groupValues?.get(1)
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

internal fun Context.canScheduleExactAlarms(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = getSystemService(android.app.AlarmManager::class.java)
    return alarmManager?.canScheduleExactAlarms() ?: false
}

internal fun Context.openExactAlarmSettings() {
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
