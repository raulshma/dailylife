package com.raulshma.dailylife.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val FilterDateFormatter = DateTimeFormatter.ofPattern("MMM d")
private val TimestampFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
private val DefaultReminderTime = LocalTime.of(9, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLifeApp(viewModel: DailyLifeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var showPreferences by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedItem = state.items.firstOrNull { it.id == selectedItemId }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DailyLife",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${state.items.size} items saved locally",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showPreferences = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Notification preferences",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
            )
        },
    ) { paddingValues ->
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

    if (showQuickAdd) {
        ModalBottomSheet(onDismissRequest = { showQuickAdd = false }) {
            QuickAddSheet(
                onAdd = { draft ->
                    viewModel.addItem(draft)
                    showQuickAdd = false
                },
                onDismiss = { showQuickAdd = false },
            )
        }
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
            contentDescription = null,
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
    onAdd: (LifeItemDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedType by rememberSaveable { mutableStateOf(LifeItemType.Thought) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var pinned by rememberSaveable { mutableStateOf(false) }
    var reminderDate by rememberSaveable { mutableStateOf("") }
    var reminderTime by rememberSaveable { mutableStateOf("") }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var overrideTime by rememberSaveable { mutableStateOf("") }
    var recurring by rememberSaveable { mutableStateOf(false) }

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
            onValueChange = { body = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            label = { Text("Details") },
        )
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
                contentDescription = null,
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
