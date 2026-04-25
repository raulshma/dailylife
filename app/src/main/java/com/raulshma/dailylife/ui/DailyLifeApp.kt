package com.raulshma.dailylife.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
    var quickAddBody by rememberSaveable { mutableStateOf("") }
    var quickAddLocationCallback by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            MediaEncryptionManager(context).clearDecryptedCache()
        }
    }

    val mediaLauncher = rememberMediaCaptureLauncher(
        context = context,
        onPhotoCaptured = { uri ->
            quickAddBody = uri.toString()
            showQuickAdd = true
        },
        onVideoCaptured = { uri ->
            quickAddBody = uri.toString()
            showQuickAdd = true
        },
        onPhotoPicked = { uri ->
            quickAddBody = uri.toString()
            showQuickAdd = true
        },
        onVideoPicked = { uri ->
            quickAddBody = uri.toString()
            showQuickAdd = true
        },
        onFilePicked = { uri ->
            quickAddBody = uri.toString()
            showQuickAdd = true
        },
    )

    Scaffold(
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
        ModalBottomSheet(onDismissRequest = {
            showQuickAdd = false
            quickAddBody = ""
        }) {
            QuickAddSheet(
                initialBody = quickAddBody,
                onBodyChanged = { quickAddBody = it },
                onAdd = { draft ->
                    viewModel.addItem(draft)
                    showQuickAdd = false
                    quickAddBody = ""
                },
                onDismiss = {
                    showQuickAdd = false
                    quickAddBody = ""
                },
                mediaLauncher = mediaLauncher,
                onShowLocationPicker = { onLocationSelected ->
                    quickAddLocationCallback = onLocationSelected
                    showLocationPicker = true
                },
            )
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddSheet(
    initialBody: String,
    onBodyChanged: (String) -> Unit,
    onAdd: (LifeItemDraft) -> Unit,
    onDismiss: () -> Unit,
    mediaLauncher: com.raulshma.dailylife.ui.capture.MediaCaptureLauncher,
    onShowLocationPicker: ((Double, Double) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    var selectedType by rememberSaveable { mutableStateOf(LifeItemType.Thought) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf(initialBody) }
    var tags by rememberSaveable { mutableStateOf("") }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var pinned by rememberSaveable { mutableStateOf(false) }
    var reminderDate by rememberSaveable { mutableStateOf("") }
    var reminderTime by rememberSaveable { mutableStateOf("") }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var overrideTime by rememberSaveable { mutableStateOf("") }
    var recurring by rememberSaveable { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorder(context) }

    if (initialBody.isNotBlank() && body != initialBody) {
        body = initialBody
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.isRecording) {
                audioRecorder.cancelRecording()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Quick add",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LifeItemType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.label) },
                    leadingIcon = {
                        Icon(type.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Title") },
        )
        OutlinedTextField(
            value = body,
            onValueChange = {
                body = it
                onBodyChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            label = { Text("Details") },
            placeholder = {
                Text("Tip: add image/video URL, or geo:lat,lon for map previews")
            },
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = {
                    if (hasCameraPermission(context)) {
                        mediaLauncher.launchCamera()
                    } else {
                        mediaLauncher.requestCameraPermissionIfNeeded()
                    }
                },
                label = { Text("Camera") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = { mediaLauncher.launchPhotoPicker() },
                label = { Text("Photos") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = {
                    if (hasCameraPermission(context)) {
                        mediaLauncher.launchVideoCamera()
                    } else {
                        mediaLauncher.requestCameraPermissionIfNeeded()
                    }
                },
                label = { Text("Video") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = { mediaLauncher.launchVideoPicker() },
                label = { Text("Pick video") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = {
                    if (isRecordingAudio) {
                        audioRecorder.stopRecording()?.let { uri ->
                            body = uri.toString()
                            onBodyChanged(uri.toString())
                        }
                        isRecordingAudio = false
                    } else {
                        if (hasAudioPermission(context)) {
                            audioRecorder.startRecording()?.let { uri ->
                                body = uri.toString()
                                onBodyChanged(uri.toString())
                            }
                            isRecordingAudio = true
                        } else {
                            mediaLauncher.requestAudioPermissionIfNeeded()
                        }
                    }
                },
                label = { Text(if (isRecordingAudio) "Stop recording" else "Audio") },
                leadingIcon = {
                    Icon(
                        if (isRecordingAudio) Icons.Filled.Done else Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = { mediaLauncher.launchFilePicker() },
                label = { Text("File") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            AssistChip(
                onClick = {
                    onShowLocationPicker { lat, lon ->
                        body = "geo:$lat,$lon"
                        onBodyChanged("geo:$lat,$lon")
                    }
                },
                label = { Text("Location") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Tags, comma separated") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
        )

        ToggleRow(
            icon = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
            label = "Favorite",
            checked = favorite,
            onCheckedChange = { favorite = it },
        )
        ToggleRow(
            icon = Icons.Filled.PushPin,
            label = "Pinned",
            checked = pinned,
            onCheckedChange = { pinned = it },
        )
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
        ToggleRow(
            icon = if (notificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
            label = "Notifications",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it },
        )
        if (notificationsEnabled) {
            OutlinedTextField(
                value = overrideTime,
                onValueChange = { overrideTime = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Time override, HH:mm") },
                leadingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
            )
        }
        ToggleRow(
            icon = Icons.Filled.EventRepeat,
            label = "Daily recurrence",
            checked = recurring,
            onCheckedChange = { recurring = it },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    onAdd(
                        LifeItemDraft(
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
                        ),
                    )
                },
                enabled = title.isNotBlank() || body.isNotBlank(),
            ) {
                Text("Save")
            }
        }
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
