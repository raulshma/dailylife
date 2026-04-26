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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.runtime.remember
import coil.compose.AsyncImage
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
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.capture.AudioRecorder
import com.raulshma.dailylife.ui.capture.LocationPickerSheet
import com.raulshma.dailylife.ui.capture.SpeechTranscriber
import com.raulshma.dailylife.ui.capture.hasAudioPermission
import com.raulshma.dailylife.ui.capture.hasCameraPermission
import com.raulshma.dailylife.ui.capture.rememberMediaCaptureLauncher
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val FilterDateFormatter = DateTimeFormatter.ofPattern("MMM d")
private val TimestampFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
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

private data class QuickAddDraft(
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

private val QuickAddDraftSaver = mapSaver(
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
        mutableStateOf(QuickAddDraft())
    }
    var quickAddLocationCallback by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            MediaEncryptionManager(context).clearDecryptedCache()
        }
    }

    val mediaLauncher = rememberMediaCaptureLauncher(
        context = context,
        onPhotoCaptured = { uri ->
            quickAddDraft = quickAddDraft.copy(body = uri.toString())
            showQuickAdd = true
        },
        onVideoCaptured = { uri ->
            quickAddDraft = quickAddDraft.copy(body = uri.toString())
            showQuickAdd = true
        },
        onPhotoPicked = { uri ->
            quickAddDraft = quickAddDraft.copy(body = uri.toString())
            showQuickAdd = true
        },
        onVideoPicked = { uri ->
            quickAddDraft = quickAddDraft.copy(body = uri.toString())
            showQuickAdd = true
        },
        onFilePicked = { uri ->
            quickAddDraft = quickAddDraft.copy(body = uri.toString())
            showQuickAdd = true
        },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DailyLife Photos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${state.visibleItems.size} of ${state.items.size} items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { selectedTabName = HomeTab.Search.name }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Open search",
                        )
                    }
                    IconButton(onClick = { showS3BackupSettings = true }) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = "Cloud backup settings",
                        )
                    }
                    IconButton(onClick = { showPreferences = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Notification preferences",
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTabName = tab.name },
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
            ExtendedFloatingActionButton(
                onClick = { showQuickAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
            )
        },
    ) { paddingValues ->
        when (selectedTab) {
            HomeTab.Photos -> {
                PhotosMosaicScreen(
                    state = state,
                    contentPadding = paddingValues,
                    onItemSelected = { selectedItemId = it },
                    onStorageErrorDismissed = viewModel::clearStorageError,
                )
            }

            HomeTab.Search -> {
                TimelineScreen(
                    state = state,
                    contentPadding = paddingValues,
                    onSearchChanged = viewModel::updateSearchQuery,
                    onTypeSelected = viewModel::selectType,
                    onTagSelected = viewModel::selectTag,
                    onDateRangeChanged = viewModel::updateDateRange,
                    onFavoritesOnlyToggled = viewModel::toggleFavoritesOnly,
                    onClearFilters = viewModel::clearFilters,
                    onItemSelected = { selectedItemId = it },
                    onFavoriteToggled = viewModel::toggleFavorite,
                    onPinnedToggled = viewModel::togglePinned,
                    onTaskStatusChanged = viewModel::updateTaskStatus,
                    onCompleted = viewModel::markOccurrenceCompleted,
                    onStorageErrorDismissed = viewModel::clearStorageError,
                )
            }

            HomeTab.Collections -> {
                CollectionsScreen(
                    state = state,
                    contentPadding = paddingValues,
                    onCollectionSelected = { items ->
                        selectedTabName = HomeTab.Search.name
                        val first = items.firstOrNull() ?: return@CollectionsScreen
                        selectedItemId = first.id
                    },
                )
            }

            HomeTab.Graph -> {
                GraphViewScreen(
                    items = state.visibleItems,
                    contentPadding = paddingValues,
                    onItemSelected = { selectedItemId = it },
                )
            }
        }
    }

    if (showQuickAdd) {
        QuickAddComposerScreen(
            draft = quickAddDraft,
            onDraftChanged = { quickAddDraft = it },
            onAdd = { draft ->
                viewModel.addItem(draft)
                showQuickAdd = false
                quickAddDraft = QuickAddDraft()
            },
            onAddAndContinue = { draft ->
                viewModel.addItem(draft)
                quickAddDraft = QuickAddDraft()
            },
            onDismiss = {
                showQuickAdd = false
            },
            onDiscardDraft = {
                showQuickAdd = false
                quickAddDraft = QuickAddDraft()
            },
            mediaLauncher = mediaLauncher,
            onShowLocationPicker = { onLocationSelected ->
                quickAddLocationCallback = onLocationSelected
                showLocationPicker = true
            },
            allTags = state.allTags,
        )
    }

    if (showLocationPicker) {
        LocationPickerSheet(
            onLocationSelected = { lat, lon ->
                quickAddLocationCallback?.invoke(lat, lon)
                quickAddLocationCallback = null
                showLocationPicker = false
            },
            onDismiss = {
                quickAddLocationCallback = null
                showLocationPicker = false
            },
        )
    }

    if (showPreferences) {
        ModalBottomSheet(onDismissRequest = { showPreferences = false }) {
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
        ModalBottomSheet(onDismissRequest = { showS3BackupSettings = false }) {
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

    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            globalSettings = state.notificationSettings,
            onDismiss = { selectedItemId = null },
            onFavoriteToggled = { viewModel.toggleFavorite(item.id) },
            onPinnedToggled = { viewModel.togglePinned(item.id) },
            onCompleted = { viewModel.markOccurrenceCompleted(item.id) },
            onNotificationsChanged = { viewModel.updateItemNotifications(item.id, it) },
        )
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
        item(key = "mosaic-header", span = StaggeredGridItemSpan.FullLine) {
            SnapshotRow(state = state)
        }
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
                EmptyTimeline()
            }
        } else {
            groupedItems.forEach { (date, itemsForDate) ->
                item(key = "date-$date", span = StaggeredGridItemSpan.FullLine) {
                    DateHeader(date = date)
                }
                staggeredItems(itemsForDate, key = { it.id }) { item ->
                    MediaMosaicTile(
                        item = item,
                        onClick = { onItemSelected(item.id) },
                    )
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
            Text(
                text = "Collections",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            CollectionCard(
                title = "Favorites",
                subtitle = "Pinned and loved memories",
                count = favoriteItems.size,
                icon = Icons.Filled.Star,
                onClick = { onCollectionSelected(favoriteItems) },
            )
        }
        item {
            CollectionCard(
                title = "Videos",
                subtitle = "Tap to open playback items",
                count = videoItems.size,
                icon = Icons.Filled.Videocam,
                onClick = { onCollectionSelected(videoItems) },
            )
        }
        item {
            CollectionCard(
                title = "Places",
                subtitle = "Items with map context",
                count = placeItems.size,
                icon = Icons.Filled.LocationOn,
                onClick = { onCollectionSelected(placeItems) },
            )
        }
        item {
            CollectionCard(
                title = "Notes & Thoughts",
                subtitle = "Text-first memories and reminders",
                count = notes.size,
                icon = Icons.Filled.EditNote,
                onClick = { onCollectionSelected(notes) },
            )
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MediaMosaicTile(
    item: LifeItem,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(item.inferMosaicHeight())
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ItemPreview(item = item)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
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
private fun ItemPreview(item: LifeItem) {
    when (item.type) {
        LifeItemType.Photo -> ImagePreview(item = item)
        LifeItemType.Video -> VideoPreview(item = item)
        LifeItemType.Audio -> AudioPreview(item = item)
        LifeItemType.Location -> LocationPreview(item = item)
        LifeItemType.Mixed -> {
            when {
                item.inferImagePreviewUrl() != null -> ImagePreview(item = item)
                item.inferLocationPreview() != null -> LocationPreview(item = item)
                item.inferVideoPlaybackUrl() != null -> VideoPreview(item = item)
                else -> TextPreview(item = item)
            }
        }

        else -> TextPreview(item = item)
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
            text = item.body.ifBlank { "No notes yet" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ImagePreview(item: LifeItem) {
    val imageUrl = rememberDecryptedMediaUri(item.inferImagePreviewUrl())
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Image preview",
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
private fun VideoPreview(item: LifeItem) {
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
                .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(14.dp))
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

@Composable
private fun AudioPreview(item: LifeItem) {
    val waveform = rememberAudioWaveform(item)
    val barHeights = if (waveform.isNotEmpty()) {
        waveform.map { (16.dp + (it * 24).dp).coerceAtLeast(4.dp) }
    } else {
        remember(item.id) {
            listOf(8.dp, 16.dp, 10.dp, 22.dp, 14.dp, 20.dp, 12.dp, 18.dp)
        }
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
            barHeights.forEach { height ->
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(height)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
        if (item.body.isNotBlank()) {
            Text(
                text = item.body,
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
                    .background(Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(12.dp))
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
private fun OpenStreetMapPreview(
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
private fun TimelineScreen(
    state: DailyLifeState,
    contentPadding: PaddingValues,
    onSearchChanged: (String) -> Unit,
    onTypeSelected: (LifeItemType?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onFavoritesOnlyToggled: () -> Unit,
    onClearFilters: () -> Unit,
    onItemSelected: (Long) -> Unit,
    onFavoriteToggled: (Long) -> Unit,
    onPinnedToggled: (Long) -> Unit,
    onTaskStatusChanged: (Long, TaskStatus) -> Unit,
    onCompleted: (Long) -> Unit,
    onStorageErrorDismissed: () -> Unit,
) {
    val groupedItems = remember(state.visibleItems) {
        state.visibleItems.groupBy { it.createdAt.toLocalDate() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SnapshotRow(state = state)
        }
        state.storageError?.let { storageError ->
            item(key = "storage-error") {
                StorageWarningCard(
                    error = storageError,
                    onDismiss = onStorageErrorDismissed,
                )
            }
        }
        item {
            TimelineFilters(
                state = state,
                onSearchChanged = onSearchChanged,
                onTypeSelected = onTypeSelected,
                onTagSelected = onTagSelected,
                onDateRangeChanged = onDateRangeChanged,
                onFavoritesOnlyToggled = onFavoritesOnlyToggled,
                onClearFilters = onClearFilters,
            )
        }
        if (groupedItems.isEmpty()) {
            item {
                EmptyTimeline()
            }
        } else {
            groupedItems.forEach { (date, itemsForDate) ->
                item(key = "date-$date") {
                    DateHeader(date = date)
                }
                items(itemsForDate, key = { it.id }) { item ->
                    LifeItemCard(
                        item = item,
                        onClick = { onItemSelected(item.id) },
                        onFavoriteToggled = { onFavoriteToggled(item.id) },
                        onPinnedToggled = { onPinnedToggled(item.id) },
                        onTaskStatusChanged = { status -> onTaskStatusChanged(item.id, status) },
                        onCompleted = { onCompleted(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageWarningCard(
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
private fun SnapshotRow(state: DailyLifeState) {
    val today = LocalDate.now()
    val completionCount = state.items.sumOf { it.occurrenceStats(today).completedCount }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SnapshotPill(
            label = "Items",
            value = state.items.size.toString(),
            modifier = Modifier.weight(1f),
        )
        SnapshotPill(
            label = "Tags",
            value = state.allTags.size.toString(),
            modifier = Modifier.weight(1f),
        )
        SnapshotPill(
            label = "Done",
            value = completionCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SnapshotPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineFilters(
    state: DailyLifeState,
    onSearchChanged: (String) -> Unit,
    onTypeSelected: (LifeItemType?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onFavoritesOnlyToggled: () -> Unit,
    onClearFilters: () -> Unit,
) {
    val context = LocalContext.current
    val filters = state.filters

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = filters.query,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (filters.query.isNotBlank()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search timeline") },
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filters.favoritesOnly,
                onClick = onFavoritesOnlyToggled,
                label = { Text("Favorites") },
                leadingIcon = {
                    Icon(
                        imageVector = if (filters.favoritesOnly) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            LifeItemType.entries.forEach { type ->
                FilterChip(
                    selected = filters.selectedType == type,
                    onClick = {
                        onTypeSelected(if (filters.selectedType == type) null else type)
                    },
                    label = { Text(type.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = type.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateRangeButton(
                label = filters.dateRangeStart?.format(FilterDateFormatter) ?: "From",
                contentDescription = "Select start date",
                modifier = Modifier.weight(1f),
                onClick = {
                    showDatePicker(
                        context = context,
                        initialDate = filters.dateRangeStart ?: filters.dateRangeEnd ?: LocalDate.now(),
                        onDateSelected = { selected ->
                            onDateRangeChanged(selected, filters.dateRangeEnd)
                        },
                    )
                },
            )
            DateRangeButton(
                label = filters.dateRangeEnd?.format(FilterDateFormatter) ?: "To",
                contentDescription = "Select end date",
                modifier = Modifier.weight(1f),
                onClick = {
                    showDatePicker(
                        context = context,
                        initialDate = filters.dateRangeEnd ?: filters.dateRangeStart ?: LocalDate.now(),
                        onDateSelected = { selected ->
                            onDateRangeChanged(filters.dateRangeStart, selected)
                        },
                    )
                },
            )
            if (filters.dateRangeStart != null || filters.dateRangeEnd != null) {
                IconButton(onClick = { onDateRangeChanged(null, null) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear date range")
                }
            }
        }

        if (state.allTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.allTags.forEach { tag ->
                    FilterChip(
                        selected = filters.selectedTag == tag,
                        onClick = {
                            onTagSelected(if (filters.selectedTag == tag) null else tag)
                        },
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

        if (filters != DailyLifeFilters()) {
            TextButton(onClick = onClearFilters) {
                Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear filters")
            }
        }
    }
}

@Composable
private fun DateRangeButton(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifeItemCard(
    item: LifeItem,
    onClick: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onTaskStatusChanged: (TaskStatus) -> Unit,
    onCompleted: () -> Unit,
) {
    val occurrenceStats = item.occurrenceStats()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TypeBadge(type = item.type)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.createdAt.format(TimestampFormatter),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onPinnedToggled) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (item.isPinned) "Unpin item" else "Pin item",
                        tint = if (item.isPinned) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(onClick = onFavoriteToggled) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (item.isFavorite) {
                            "Remove favorite"
                        } else {
                            "Add favorite"
                        },
                        tint = if (item.isFavorite) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            if (item.type.isMediaLike()) {
                MediaPreview(item = item)
            }

            if (item.body.isNotBlank()) {
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.tags.forEach { tag ->
                    AssistChip(
                        onClick = onClick,
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
                if (item.isRecurring) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text(item.recurrenceRule.frequency.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.EventRepeat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
                item.reminderAt?.let { reminderAt ->
                    AssistChip(
                        onClick = onClick,
                        label = { Text(reminderAt.format(TimestampFormatter)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
                if (item.notificationSettings.enabled) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Notify") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }

            if (item.isRecurring || occurrenceStats.completedCount > 0 || occurrenceStats.missedCount > 0) {
                OccurrenceStatsRow(stats = occurrenceStats)
            }

            if (item.type == LifeItemType.Task) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TaskStatus.entries.forEach { status ->
                        FilterChip(
                            selected = item.taskStatus == status,
                            onClick = { onTaskStatusChanged(status) },
                            label = { Text(status.label) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCompleted) {
                    Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
private fun OccurrenceStatsRow(stats: OccurrenceStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OccurrenceMetric(
            label = "Done",
            value = stats.completedCount.toString(),
            modifier = Modifier.weight(1f),
        )
        OccurrenceMetric(
            label = "Missed",
            value = stats.missedCount.toString(),
            modifier = Modifier.weight(1f),
        )
        OccurrenceMetric(
            label = "Streak",
            value = stats.currentStreak.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OccurrenceMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TypeBadge(type: LifeItemType) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = type.icon(),
            contentDescription = type.label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun MediaPreview(item: LifeItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = item.type.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.type.label} placeholder",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ReminderDateTimeRow(
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddComposerScreen(
    draft: QuickAddDraft,
    onDraftChanged: (QuickAddDraft) -> Unit,
    onAdd: (LifeItemDraft) -> Unit,
    onAddAndContinue: (LifeItemDraft) -> Unit,
    onDismiss: () -> Unit,
    onDiscardDraft: () -> Unit,
    mediaLauncher: com.raulshma.dailylife.ui.capture.MediaCaptureLauncher,
    onShowLocationPicker: ((Double, Double) -> Unit) -> Unit,
    allTags: List<String> = emptyList(),
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Quick add",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Compose full screen",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close quick add",
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onDiscardDraft) {
                        Text("Discard")
                    }
                },
            )
        },
    ) { paddingValues ->
        QuickAddSheet(
            modifier = Modifier.padding(paddingValues),
            initialDraft = draft,
            onDraftChanged = onDraftChanged,
            onAdd = onAdd,
            onAddAndContinue = onAddAndContinue,
            onDismiss = onDismiss,
            mediaLauncher = mediaLauncher,
            onShowLocationPicker = onShowLocationPicker,
            allTags = allTags,
            showHeader = false,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddSheet(
    modifier: Modifier = Modifier,
    initialDraft: QuickAddDraft,
    onDraftChanged: (QuickAddDraft) -> Unit,
    onAdd: (LifeItemDraft) -> Unit,
    onAddAndContinue: (LifeItemDraft) -> Unit,
    onDismiss: () -> Unit,
    mediaLauncher: com.raulshma.dailylife.ui.capture.MediaCaptureLauncher,
    onShowLocationPicker: ((Double, Double) -> Unit) -> Unit,
    allTags: List<String> = emptyList(),
    showHeader: Boolean = true,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val initialType = remember(initialDraft.typeName) {
        LifeItemType.entries.firstOrNull { it.name == initialDraft.typeName } ?: LifeItemType.Thought
    }
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    var title by rememberSaveable { mutableStateOf(initialDraft.title) }
    var body by rememberSaveable { mutableStateOf(initialDraft.body) }
    var tags by rememberSaveable { mutableStateOf(initialDraft.tags) }
    var favorite by rememberSaveable { mutableStateOf(initialDraft.favorite) }
    var pinned by rememberSaveable { mutableStateOf(initialDraft.pinned) }
    var reminderDate by rememberSaveable { mutableStateOf(initialDraft.reminderDate) }
    var reminderTime by rememberSaveable { mutableStateOf(initialDraft.reminderTime) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(initialDraft.notificationsEnabled) }
    var overrideTime by rememberSaveable { mutableStateOf(initialDraft.overrideTime) }
    var recurring by rememberSaveable { mutableStateOf(initialDraft.recurring) }
    var showAdvanced by rememberSaveable { mutableStateOf(initialDraft.showAdvanced) }
    var showReminderOptions by rememberSaveable { mutableStateOf(initialDraft.showReminderOptions) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var activeRecordingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAudioUri by remember { mutableStateOf<Uri?>(null) }
    var liveTranscription by rememberSaveable { mutableStateOf("") }
    val audioRecorder = remember { AudioRecorder(context) }
    val speechTranscriber = remember { SpeechTranscriber(context) }
    val titleFocusRequester = remember { FocusRequester() }

    fun currentDraftSnapshot(): QuickAddDraft = QuickAddDraft(
        typeName = selectedType.name,
        title = title,
        body = body,
        tags = tags,
        favorite = favorite,
        pinned = pinned,
        reminderDate = reminderDate,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        overrideTime = overrideTime,
        recurring = recurring,
        showAdvanced = showAdvanced,
        showReminderOptions = showReminderOptions,
    )

    fun buildDraftPayload(): LifeItemDraft = LifeItemDraft(
        type = selectedType,
        title = title,
        body = body,
        tags = parseTags(tags),
        isFavorite = favorite,
        isPinned = pinned,
        taskStatus = if (selectedType == LifeItemType.Task) {
            TaskStatus.Open
        } else {
            null
        },
        reminderAt = parseReminderDateTime(reminderDate, reminderTime),
        recurrenceRule = if (recurring) {
            RecurrenceRule(RecurrenceFrequency.Daily)
        } else {
            RecurrenceRule()
        },
        notificationSettings = ItemNotificationSettings(
            enabled = notificationsEnabled,
            timeOverride = parseTimeOrNull(overrideTime),
        ),
    )

    fun resetLocalDraft() {
        selectedType = LifeItemType.Thought
        title = ""
        body = ""
        tags = ""
        favorite = false
        pinned = false
        reminderDate = ""
        reminderTime = ""
        notificationsEnabled = true
        overrideTime = ""
        recurring = false
        showAdvanced = false
        showReminderOptions = false
    }

    // Smart type detection from body content
    val inferredType = remember(body) { inferTypeFromBody(body) }
    val canSave = title.isNotBlank() || body.isNotBlank()

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    LaunchedEffect(
        selectedType,
        title,
        body,
        tags,
        favorite,
        pinned,
        reminderDate,
        reminderTime,
        notificationsEnabled,
        overrideTime,
        recurring,
        showAdvanced,
        showReminderOptions,
    ) {
        onDraftChanged(currentDraftSnapshot())
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.isRecording) {
                audioRecorder.cancelRecording()
            }
            speechTranscriber.destroy()
        }
    }

    fun startAudioCapture() {
        pendingAudioUri = null
        liveTranscription = ""
        isRecordingAudio = true
        val transcriberStarted = speechTranscriber.start(
            onTranscriptChanged = { transcript ->
                liveTranscription = transcript
            },
            onError = { errorMsg ->
                // Show speech errors in transcription area if no text yet
                if (liveTranscription.isBlank()) {
                    liveTranscription = errorMsg
                }
            },
        )
        if (!transcriberStarted) {
            liveTranscription = "Speech recognition not available on this device."
        }
        // Delay audio recording start to let SpeechRecognizer claim the mic first
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isRecordingAudio) {
                val uri = audioRecorder.startRecording()
                if (uri != null) {
                    activeRecordingUri = uri
                }
            }
        }, 500L)
    }

    fun stopAudioCapture() {
        if (!isRecordingAudio) return
        val stoppedUri = if (audioRecorder.isRecording) audioRecorder.stopRecording() else null
        val finalTranscript = speechTranscriber.stop()
        isRecordingAudio = false
        pendingAudioUri = stoppedUri ?: activeRecordingUri
        activeRecordingUri = null
        if (finalTranscript.isNotBlank()) {
            liveTranscription = finalTranscript
        }
    }

    fun discardCapturedAudio() {
        pendingAudioUri = null
        activeRecordingUri = null
        liveTranscription = ""
        speechTranscriber.cancel()
        if (audioRecorder.isRecording) {
            audioRecorder.cancelRecording()
        } else {
            audioRecorder.discardLastRecording()
        }
    }

    fun reRecordAudio() {
        discardCapturedAudio()
        startAudioCapture()
    }

    fun addCapturedAudioToDraft() {
        val uri = pendingAudioUri
        val transcript = liveTranscription.trim()
        if (uri != null) {
            body = buildString {
                append(uri.toString())
                if (transcript.isNotBlank()) {
                    append("\n\n")
                    append(transcript)
                }
            }
            selectedType = LifeItemType.Audio
        } else if (transcript.isNotBlank()) {
            body = transcript
        }
        pendingAudioUri = null
        liveTranscription = ""
        audioRecorder.clearLastRecordingReference()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showHeader) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Quick add",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Type selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LifeItemType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        val containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        val contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .clickable {
                                    if (selectedType != type) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    selectedType = type
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = type.icon(),
                                    contentDescription = type.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor,
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = inferredType != null && inferredType != selectedType,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Detected ${inferredType?.label ?: ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        TextButton(
                            onClick = {
                                inferredType?.let {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedType = it
                                }
                            },
                        ) {
                            Text("Switch")
                        }
                    }
                }
            }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 120) title = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester),
            singleLine = true,
            label = { Text("Title") },
            supportingText = {
                Text(
                    text = "${title.length}/120",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (title.length >= 100) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
        )

        // Body
        OutlinedTextField(
            value = body,
            onValueChange = {
                if (it.length <= 2000) {
                    body = it
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            label = { Text("Details") },
            placeholder = {
                Text("Tip: add image/video URL, or geo:lat,lon for map previews")
            },
            supportingText = {
                Text(
                    text = "${body.length}/2000",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (body.length >= 1800) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
        )

        // Attachments toolbar
        QuickAddAttachmentToolbar(
            isRecordingAudio = isRecordingAudio,
            onCamera = {
                if (hasCameraPermission(context)) {
                    mediaLauncher.launchCamera()
                } else {
                    mediaLauncher.requestCameraPermissionIfNeeded()
                }
            },
            onPhotos = { mediaLauncher.launchPhotoPicker() },
            onVideo = {
                if (hasCameraPermission(context)) {
                    mediaLauncher.launchVideoCamera()
                } else {
                    mediaLauncher.requestCameraPermissionIfNeeded()
                }
            },
            onPickVideo = { mediaLauncher.launchVideoPicker() },
            onAudio = {
                if (isRecordingAudio) {
                    stopAudioCapture()
                } else {
                    if (hasAudioPermission(context)) {
                        startAudioCapture()
                    } else {
                        mediaLauncher.requestAudioPermissionIfNeeded()
                    }
                }
            },
            onFile = { mediaLauncher.launchFilePicker() },
            onLocation = {
                onShowLocationPicker { lat, lon ->
                    body = "geo:$lat,$lon"
                }
            },
        )

        AnimatedVisibility(
            visible = isRecordingAudio || pendingAudioUri != null || liveTranscription.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            AudioRecordingCard(
                isRecordingAudio = isRecordingAudio,
                pendingAudioUri = pendingAudioUri,
                liveTranscription = liveTranscription,
                audioRecorder = audioRecorder,
                onStop = ::stopAudioCapture,
                onDiscard = ::discardCapturedAudio,
                onReRecord = ::reRecordAudio,
                onAddRecording = ::addCapturedAudioToDraft,
            )
        }

        // Attached content preview
        val attachedUri = remember(body) {
            body.takeIf {
                it.startsWith("content://") ||
                    it.startsWith("file://") ||
                    it.startsWith("geo:") ||
                    it.startsWith("http")
            }
        }
        AnimatedVisibility(
            visible = attachedUri != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when {
                            body.startsWith("geo:") -> Icons.Filled.LocationOn
                            body.startsWith("http") -> Icons.Filled.CloudUpload
                            else -> Icons.Filled.EditNote
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = attachedUri?.take(60) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            body = ""
                        },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear attachment",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Tags
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Tags, comma separated") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
        )

        // Tag suggestions
        val currentTagSet = parseTags(tags)
        val suggestions = remember(allTags, currentTagSet) {
            allTags.filter { it !in currentTagSet }.take(8)
        }
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                suggestions.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = {
                            tags = if (tags.isBlank()) tag else "$tags, $tag"
                        },
                        label = { Text("+$tag") },
                        modifier = Modifier.height(28.dp),
                    )
                }
            }
        }

            // Quick properties row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        favorite = !favorite
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (favorite) "Favorited" else "Favorite",
                        tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        pinned = !pinned
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (pinned) "Pinned" else "Pin",
                        tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(if (showAdvanced) "Less options" else "More options")
                }
            }

            // Reminder card
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        QuickAddSectionTitle(
                            icon = Icons.Filled.Alarm,
                            title = "Reminder",
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showReminderOptions = !showReminderOptions }) {
                            Text(if (showReminderOptions) "Hide" else "Add reminder")
                        }
                    }
                    AnimatedVisibility(
                        visible = showReminderOptions,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ReminderDateTimeRow(
                                reminderDate = parseDateOrNull(reminderDate),
                                reminderTime = parseTimeOrNull(reminderTime),
                                onDateClick = {
                                    showDatePicker(
                                        context = context,
                                        initialDate = parseDateOrNull(reminderDate) ?: LocalDate.now(),
                                        onDateSelected = { selected ->
                                            reminderDate = selected.toString()
                                            if (reminderTime.isBlank()) {
                                                reminderTime = DefaultReminderTime.format(TimeFormatter)
                                            }
                                        },
                                    )
                                },
                                onTimeClick = {
                                    showTimePicker(
                                        context = context,
                                        initialTime = parseTimeOrNull(reminderTime)
                                            ?: parseTimeOrNull(overrideTime)
                                            ?: DefaultReminderTime,
                                        onTimeSelected = { selected ->
                                            if (reminderDate.isBlank()) {
                                                reminderDate = LocalDate.now().toString()
                                            }
                                            reminderTime = selected.format(TimeFormatter)
                                        },
                                    )
                                },
                                onClear = {
                                    reminderDate = ""
                                    reminderTime = ""
                                },
                            )
                            QuickReminderPresets(
                                onToday = {
                                    reminderDate = LocalDate.now().toString()
                                    if (reminderTime.isBlank()) {
                                        reminderTime = DefaultReminderTime.format(TimeFormatter)
                                    }
                                },
                                onTomorrow = {
                                    reminderDate = LocalDate.now().plusDays(1).toString()
                                    if (reminderTime.isBlank()) {
                                        reminderTime = DefaultReminderTime.format(TimeFormatter)
                                    }
                                },
                                onNextWeek = {
                                    reminderDate = LocalDate.now().plusWeeks(1).toString()
                                    if (reminderTime.isBlank()) {
                                        reminderTime = DefaultReminderTime.format(TimeFormatter)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            // Advanced options
            AnimatedVisibility(
                visible = showAdvanced,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        QuickAddSectionTitle(
                            icon = Icons.Filled.Tune,
                            title = "Advanced",
                        )
                        ToggleRow(
                            icon = if (notificationsEnabled) {
                                Icons.Filled.Notifications
                            } else {
                                Icons.Filled.NotificationsOff
                            },
                            label = "Notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                        )
                        AnimatedVisibility(
                            visible = notificationsEnabled,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            OutlinedTextField(
                                value = overrideTime,
                                onValueChange = { overrideTime = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Time override, HH:mm") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.AccessTime,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                        ToggleRow(
                            icon = Icons.Filled.EventRepeat,
                            label = "Daily recurrence",
                            checked = recurring,
                            onCheckedChange = { recurring = it },
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAdd(buildDraftPayload())
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }

        TextButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddAndContinue(buildDraftPayload())
                resetLocalDraft()
                titleFocusRequester.requestFocus()
            },
            enabled = canSave,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Save + new")
        }
    }
}

@Composable
private fun QuickAddSectionTitle(
    icon: ImageVector,
    title: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun QuickAddAttachmentToolbar(
    isRecordingAudio: Boolean,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onVideo: () -> Unit,
    onPickVideo: () -> Unit,
    onAudio: () -> Unit,
    onFile: () -> Unit,
    onLocation: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val recordingPulse = rememberInfiniteTransition(label = "recordingPulse")
        val recordingScale by recordingPulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "recordingScale",
        )

        listOf(
            Triple(Icons.Filled.PhotoCamera, "Camera", onCamera),
            Triple(Icons.Filled.PhotoLibrary, "Photos", onPhotos),
            Triple(Icons.Filled.Videocam, "Video", onVideo),
            Triple(Icons.Filled.PlayArrow, "Pick video", onPickVideo),
            Triple(Icons.Filled.EditNote, "File", onFile),
            Triple(Icons.Filled.LocationOn, "Location", onLocation),
        ).forEach { (icon, desc, action) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(
                    onClick = action,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = desc,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FilledTonalIconButton(
                onClick = onAudio,
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer {
                        val scale = if (isRecordingAudio) recordingScale else 1f
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                Icon(
                    imageVector = if (isRecordingAudio) Icons.Filled.Done else Icons.Filled.Mic,
                    contentDescription = if (isRecordingAudio) "Stop" else "Audio",
                    modifier = Modifier.size(20.dp),
                    tint = if (isRecordingAudio) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalContentColor.current
                    },
                )
            }
            Text(
                text = if (isRecordingAudio) "Stop" else "Audio",
                style = MaterialTheme.typography.labelSmall,
                color = if (isRecordingAudio) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickReminderPresets(
    onToday: () -> Unit,
    onTomorrow: () -> Unit,
    onNextWeek: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = onToday,
            label = { Text("Today") },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
        AssistChip(
            onClick = onTomorrow,
            label = { Text("Tomorrow") },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
        AssistChip(
            onClick = onNextWeek,
            label = { Text("Next week") },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun AudioRecordingCard(
    isRecordingAudio: Boolean,
    pendingAudioUri: Uri?,
    liveTranscription: String,
    audioRecorder: AudioRecorder,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onReRecord: () -> Unit,
    onAddRecording: () -> Unit,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val transcriptionScrollState = rememberScrollState()

    // Seekbar state
    var playbackPosition by remember { mutableStateOf(0f) }
    var playbackDuration by remember { mutableStateOf(0) }
    var isSeeking by remember { mutableStateOf(false) }

    // Live amplitude samples for waveform (rolling buffer)
    var amplitudeSamples by remember { mutableStateOf(listOf<Float>()) }
    var displayedElapsedMs by remember { mutableStateOf(0L) }

    // Poll the recorder for live amplitude and elapsed time while recording
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            amplitudeSamples = emptyList()
            while (isRecordingAudio) {
                val amp = audioRecorder.currentAmplitude
                displayedElapsedMs = audioRecorder.elapsedMs
                amplitudeSamples = (amplitudeSamples + amp).takeLast(48)
                delay(80L)
            }
        }
    }

    // Poll media player position during playback
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val mp = mediaPlayer
            if (mp != null && mp.isPlaying && !isSeeking) {
                playbackPosition = mp.currentPosition.toFloat()
                playbackDuration = mp.duration
            }
            delay(200L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        playbackPosition = 0f
    }

    fun startPlayback(uri: Uri) {
        stopPlayback()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setOnCompletionListener {
                isPlaying = false
                playbackPosition = 0f
            }
            prepare()
            playbackDuration = duration
            start()
        }
        isPlaying = true
    }

    fun togglePlayback() {
        val uri = pendingAudioUri ?: return
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        } else {
            val mp = mediaPlayer
            if (mp != null) {
                mp.start()
                isPlaying = true
            } else {
                startPlayback(uri)
            }
        }
    }

    LaunchedEffect(liveTranscription) {
        if (liveTranscription.isNotBlank()) {
            transcriptionScrollState.animateScrollTo(transcriptionScrollState.maxValue)
        }
    }

    val recordingPulseAlpha = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by recordingPulseAlpha.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header row with status + timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isRecordingAudio) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
                                shape = CircleShape,
                            ),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = when {
                        isRecordingAudio -> "Recording & transcribing"
                        isPlaying -> "Playing recording"
                        pendingAudioUri != null -> "Recording ready"
                        else -> "Transcription ready"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
                if (isRecordingAudio) {
                    Text(
                        text = formatTime(displayedElapsedMs),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Live waveform during recording
            if (isRecordingAudio) {
                val waveColor = MaterialTheme.colorScheme.primary
                val waveTrackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                ) {
                    val barCount = amplitudeSamples.size.coerceAtLeast(1)
                    val barWidth = (size.width / 48f) * 0.65f
                    val gap = (size.width / 48f) * 0.35f
                    val startX = size.width - (barCount * (barWidth + gap))
                    amplitudeSamples.forEachIndexed { index, amp ->
                        val barHeight = (amp * size.height * 0.85f).coerceAtLeast(4f)
                        val x = startX + index * (barWidth + gap)
                        val y = (size.height - barHeight) / 2f
                        drawRoundRect(
                            color = waveColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f),
                        )
                    }
                    // Fill remaining bars as dim track
                    val emptyBars = 48 - barCount
                    for (i in 0 until emptyBars) {
                        val x = i * (barWidth + gap)
                        val barH = 4f
                        val y = (size.height - barH) / 2f
                        drawRoundRect(
                            color = waveTrackColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(barWidth, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f),
                        )
                    }
                }
            }

            // Stop button while recording
            if (isRecordingAudio) {
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop recording",
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop recording")
                }
            }

            // Live transcription panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isRecordingAudio) 100.dp else 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    )
                    .verticalScroll(transcriptionScrollState)
                    .padding(10.dp),
            ) {
                Text(
                    text = liveTranscription.ifBlank {
                        if (isRecordingAudio) "Listening\u2026" else "No transcription available."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (liveTranscription.isBlank()) {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                )
            }

            // Playback controls (after recording stopped)
            if (!isRecordingAudio && pendingAudioUri != null) {
                // Seek slider + play/pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = { togglePlayback() },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        text = formatTime(playbackPosition.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Slider(
                        value = playbackPosition,
                        onValueChange = { newVal ->
                            isSeeking = true
                            playbackPosition = newVal
                        },
                        onValueChangeFinished = {
                            mediaPlayer?.seekTo(playbackPosition.toInt())
                            isSeeking = false
                        },
                        valueRange = 0f..playbackDuration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatTime(playbackDuration.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = {
                        stopPlayback()
                        onDiscard()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discard")
                    }
                    OutlinedButton(onClick = {
                        stopPlayback()
                        onReRecord()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-record")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        stopPlayback()
                        onAddRecording()
                    }) {
                        Text("Add")
                    }
                }
            }

            // Discard button if only transcription with no recording
            if (!isRecordingAudio && pendingAudioUri == null && liveTranscription.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(onClick = onDiscard) {
                        Text("Discard")
                    }
                    Button(onClick = onAddRecording) {
                        Text("Add text")
                    }
                }
            }
        }
    }
}

private fun inferTypeFromBody(body: String): LifeItemType? {
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
                else -> LifeItemType.Mixed
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemDetailDialog(
    item: LifeItem,
    globalSettings: NotificationSettings,
    onDismiss: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onCompleted: () -> Unit,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
) {
    val occurrenceStats = item.occurrenceStats()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = item.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeBadge(type = item.type)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.type.label, fontWeight = FontWeight.SemiBold)
                        Text(
                            item.createdAt.format(TimestampFormatter),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                if (item.body.isNotBlank()) {
                    Text(item.body)
                }

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

                HorizontalDivider()

                DetailLine("Favorite", if (item.isFavorite) "Yes" else "No")
                DetailLine("Pinned", if (item.isPinned) "Yes" else "No")
                item.taskStatus?.let { DetailLine("Task status", it.label) }
                item.reminderAt?.let { DetailLine("Reminder", it.format(TimestampFormatter)) }
                if (item.isRecurring) {
                    DetailLine("Recurrence", item.recurrenceRule.frequency.label)
                }
                DetailLine("Completions", occurrenceStats.completedCount.toString())
                if (item.isRecurring || occurrenceStats.missedCount > 0) {
                    DetailLine("Missed", occurrenceStats.missedCount.toString())
                    DetailLine("Current streak", occurrenceStats.currentStreak.toString())
                }

                val effectiveTime = item.notificationSettings.timeOverride
                    ?: globalSettings.preferredTime
                ToggleRow(
                    icon = if (item.notificationSettings.enabled) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    label = "Item notifications at ${effectiveTime.format(TimeFormatter)}",
                    checked = item.notificationSettings.enabled,
                    onCheckedChange = {
                        onNotificationsChanged(item.notificationSettings.copy(enabled = it))
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            Row {
                IconButton(onClick = onPinnedToggled) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (item.isPinned) "Unpin" else "Pin",
                    )
                }
                IconButton(onClick = onFavoriteToggled) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (item.isFavorite) {
                            "Remove favorite"
                        } else {
                            "Add favorite"
                        },
                    )
                }
                IconButton(onClick = onCompleted) {
                    Icon(Icons.Filled.Done, contentDescription = "Mark complete")
                }
            }
        },
    )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ToggleRow(
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
private fun EmptyTimeline() {
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
                modifier = Modifier.size(42.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No items match these filters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LifeItemType.icon(): ImageVector = when (this) {
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

private fun LifeItemType.isMediaLike(): Boolean =
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
private fun rememberDecryptedMediaUri(uriString: String?): String? {
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
private fun rememberVideoThumbnail(item: LifeItem): String? {
    val context = LocalContext.current
    return remember(item.id, item.body) {
        val videoUrl = item.inferVideoPlaybackUrl()
        if (videoUrl == null) return@remember null
        val generator = MediaThumbnailGenerator(context)
        generator.generateVideoThumbnail(android.net.Uri.parse(videoUrl), context)?.toString()
    }
}

@Composable
private fun rememberAudioWaveform(item: LifeItem): List<Float> {
    return remember(item.id, item.body) {
        val audioUrl = item.inferVideoPlaybackUrl()
            ?: item.body.split(" ").firstOrNull { it.startsWith("content://") || it.startsWith("file://") }
        if (audioUrl == null) return@remember emptyList()
        val generator = AudioWaveformGenerator()
        generator.generateWaveform(android.net.Uri.parse(audioUrl), barCount = 8) ?: emptyList()
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
        else -> if (body.length > 120) 198.dp else 152.dp
    }
}

private fun LifeItem.inferLocationPreview(): Pair<Double, Double>? {
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

private fun parseTags(input: String): Set<String> =
    input.split(",")
        .map { it.trim().removePrefix("#") }
        .filter { it.isNotBlank() }
        .toSet()

private fun parseDateOrNull(input: String): LocalDate? =
    runCatching { LocalDate.parse(input.trim()) }.getOrNull()

private fun parseTimeOrNull(input: String): LocalTime? =
    runCatching { LocalTime.parse(input.trim(), TimeFormatter) }.getOrNull()

private fun parseReminderDateTime(dateInput: String, timeInput: String): LocalDateTime? {
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

private fun showDatePicker(
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

private fun showTimePicker(
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
