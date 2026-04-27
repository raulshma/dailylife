package com.raulshma.dailylife.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimelineScreen(
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
    
    var showAdvancedFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Top Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Search Bar
            SearchBarRow(
                query = state.filters.query,
                onSearchChanged = onSearchChanged,
                onOpenFilters = { showAdvancedFilters = true }
            )

            // Quick Filters Carousel
            QuickFiltersCarousel(
                state = state,
                onFavoritesOnlyToggled = onFavoritesOnlyToggled,
                onTypeSelected = onTypeSelected,
                onOpenFilters = { showAdvancedFilters = true }
            )
        }

        // Timeline Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            if (groupedItems.isEmpty()) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = androidx.compose.animation.fadeIn(
                            com.raulshma.dailylife.ui.theme.DailyLifeTween.content<Float>()
                        ) + androidx.compose.animation.slideInVertically(
                            com.raulshma.dailylife.ui.theme.DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
                            initialOffsetY = { it / 4 }
                        ),
                    ) {
                        EmptyTimeline()
                    }
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
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (showAdvancedFilters) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAdvancedFilters = false },
            sheetState = sheetState
        ) {
            AdvancedFiltersSheet(
                state = state,
                onTypeSelected = onTypeSelected,
                onTagSelected = onTagSelected,
                onDateRangeChanged = onDateRangeChanged,
                onClearFilters = onClearFilters,
                onDismiss = { showAdvancedFilters = false }
            )
        }
    }
}

@Composable
private fun SearchBarRow(
    query: String,
    onSearchChanged: (String) -> Unit,
    onOpenFilters: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        TextField(
            value = query,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search your timeline") },
            leadingIcon = {
                IconButton(onClick = { /* Menu or search action */ }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    } else {
                        IconButton(onClick = onOpenFilters) {
                            Icon(Icons.Filled.Tune, contentDescription = "Filters")
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
    }
}

@Composable
private fun QuickFiltersCarousel(
    state: DailyLifeState,
    onFavoritesOnlyToggled: () -> Unit,
    onTypeSelected: (LifeItemType?) -> Unit,
    onOpenFilters: () -> Unit
) {
    val filters = state.filters
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = filters.favoritesOnly,
                onClick = onFavoritesOnlyToggled,
                label = { Text("Favorites") },
                leadingIcon = {
                    Icon(
                        imageVector = if (filters.favoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        val quickTypes = listOf(LifeItemType.Note, LifeItemType.Task, LifeItemType.Photo, LifeItemType.Location)
        items(quickTypes) { type ->
            FilterChip(
                selected = filters.selectedType == type,
                onClick = { onTypeSelected(if (filters.selectedType == type) null else type) },
                label = { Text(type.label) },
                leadingIcon = {
                    Icon(
                        imageVector = type.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        
        item {
            // Also show an active indicator if tags or dates are filtered
            if (filters.selectedTag != null || filters.dateRangeStart != null || filters.dateRangeEnd != null) {
                FilterChip(
                    selected = true,
                    onClick = onOpenFilters,
                    label = { Text("More Active Filters") },
                    leadingIcon = {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFiltersSheet(
    state: DailyLifeState,
    onTypeSelected: (LifeItemType?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val filters = state.filters

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (filters != DailyLifeFilters()) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear all")
                }
            }
        }

        // Date Range
        Text("Date Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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

        // Types
        Text("Item Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LifeItemType.entries.forEach { type ->
                FilterChip(
                    selected = filters.selectedType == type,
                    onClick = { onTypeSelected(if (filters.selectedType == type) null else type) },
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

        // Tags
        if (state.allTags.isNotEmpty()) {
            Text("Tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.allTags.forEach { tag ->
                    FilterChip(
                        selected = filters.selectedTag == tag,
                        onClick = { onTagSelected(if (filters.selectedTag == tag) null else tag) },
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
        
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show results")
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifeItemCard(
    item: LifeItem,
    onClick: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onTaskStatusChanged: (TaskStatus) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val occurrenceStats = item.occurrenceStats()

    com.raulshma.dailylife.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (item.isFavorite) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            val hasMediaContent = item.type.isMediaLike() ||
                item.inferImagePreviewUrl() != null ||
                item.inferVideoPlaybackUrl() != null ||
                item.inferAudioUrl() != null ||
                item.inferLocationPreview() != null
            if (hasMediaContent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    ItemPreview(item = item)
                }
            }

            if (item.displayBody().isNotBlank()) {
                Text(
                    text = item.displayBody(),
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
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


