package com.raulshma.dailylife.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.components.CompletionRipple
import com.raulshma.dailylife.ui.components.TimelineSkeletonItem
import com.raulshma.dailylife.domain.*
import java.time.LocalDate

internal sealed class TimelineEntry {
    data class DateHeader(val date: LocalDate) : TimelineEntry()
    data class Item(val index: Int, val id: Long) : TimelineEntry()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimelineScreen(
    state: DailyLifeState,
    pagingItems: LazyPagingItems<LifeItem>,
    snapshotStats: SnapshotStats,
    allTags: List<String>,
    contentPadding: PaddingValues,
    skipStaggerAnimation: Boolean,
    listState: LazyListState,
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

    var showAdvancedFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SearchBarRow(
                query = state.filters.query,
                onSearchChanged = onSearchChanged,
                onOpenFilters = { showAdvancedFilters = true }
            )

            QuickFiltersCarousel(
                filters = state.filters,
                onFavoritesOnlyToggled = onFavoritesOnlyToggled,
                onTypeSelected = onTypeSelected,
                onClearFilters = onClearFilters,
                onOpenFilters = { showAdvancedFilters = true }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pagingItems.loadState.refresh is LoadState.Loading && !isEmpty) {
                items(count = 3, key = { "refresh-skeleton-$it" }) {
                    TimelineSkeletonItem(modifier = Modifier.animateItem())
                }
            }

            item {
                SnapshotRow(snapshotStats = snapshotStats)
            }

            state.storageError?.let { storageError ->
                item(key = "storage-error") {
                    StorageWarningCard(
                        error = storageError,
                        onDismiss = onStorageErrorDismissed,
                    )
                }
            }

            if (isEmpty) {
                if (pagingItems.loadState.refresh is LoadState.Error) {
                    val error = (pagingItems.loadState.refresh as LoadState.Error).error
                    item {
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
                }
            } else {
                items(
                    count = entries.size,
                    key = { index ->
                        when (val entry = entries[index]) {
                            is TimelineEntry.DateHeader -> "date-${entry.date}"
                            is TimelineEntry.Item -> "item-${entry.id}"
                        }
                    },
                ) { index ->
                    when (val entry = entries[index]) {
                        is TimelineEntry.DateHeader -> {
                            DateHeader(date = entry.date)
                        }
                        is TimelineEntry.Item -> {
                            val item = if (entry.index < pagingItems.itemCount) pagingItems[entry.index] else null
                            if (item != null) {
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

                pagingItems.apply {
                    when (loadState.append) {
                        is LoadState.Loading -> {
                            item(key = "append-loading") {
                                TimelineSkeletonItem(modifier = Modifier.animateItem())
                            }
                        }
                        is LoadState.Error -> {
                            val error = (loadState.append as LoadState.Error).error
                            item(key = "append-error") {
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

    if (showAdvancedFilters) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAdvancedFilters = false },
            sheetState = sheetState
        ) {
            AdvancedFiltersSheet(
                filters = state.filters,
                allTags = allTags,
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
    var isFocused by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 2.dp,
        animationSpec = tween(durationMillis = com.raulshma.dailylife.ui.theme.DailyLifeDuration.SHORT),
        label = "searchElevation",
    )
    val animatedColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = com.raulshma.dailylife.ui.theme.DailyLifeDuration.SHORT),
        label = "searchColor",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = CircleShape,
        color = animatedColor,
        tonalElevation = animatedElevation,
    ) {
        TextField(
            value = query,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> isFocused = focusState.isFocused },
            placeholder = { Text("Search your timeline") },
            leadingIcon = {
                IconButton(onClick = { }) {
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
    filters: DailyLifeFilters,
    onFavoritesOnlyToggled: () -> Unit,
    onTypeSelected: (LifeItemType?) -> Unit,
    onClearFilters: () -> Unit,
    onOpenFilters: () -> Unit
) {
    val hasActiveFilters = filters != DailyLifeFilters()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
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

        val quickTypes = listOf(LifeItemType.Video, LifeItemType.Note, LifeItemType.Task, LifeItemType.Photo, LifeItemType.Pdf, LifeItemType.Location)
        items(quickTypes) { type ->
            val isSelected = filters.selectedType == type || filters.selectedTypes?.contains(type) == true
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(if (isSelected) null else type) },
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

        item {
            if (hasActiveFilters) {
                FilterChip(
                    selected = false,
                    onClick = onClearFilters,
                    label = { Text("Clear") },
                    leadingIcon = {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFiltersSheet(
    filters: DailyLifeFilters,
    allTags: List<String>,
    onTypeSelected: (LifeItemType?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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

        if (allTags.isNotEmpty()) {
            Text("Tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allTags.forEach { tag ->
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
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val haptic = LocalHapticFeedback.current
    var justCompleted by remember { mutableStateOf(false) }

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

    com.raulshma.dailylife.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TypeBadge(type = item.type, modifier = badgeSharedModifier, boxSize = 32.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        modifier = titleSharedModifier,
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
                IconButton(
                    onClick = onPinnedToggled,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (item.isPinned) "Unpin item" else "Pin item",
                        tint = if (item.isPinned) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onFavoriteToggled,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (item.isFavorite) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            val hasMediaContent = item.type.isMediaLike() ||
                item.inferImagePreviewUrl() != null ||
                item.inferVideoPlaybackUrl() != null ||
                item.inferAudioUrl() != null ||
                item.inferPdfUrl() != null ||
                item.inferLocationPreview() != null
            if (hasMediaContent) {
                Box(
                    modifier = Modifier
                        .then(mediaSharedModifier)
                        .fillMaxWidth()
                        .height(120.dp)
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            CompactMetadataStrip(item = item, onClick = onClick)

            if (item.isRecurring || occurrenceStats.completedCount > 0 || occurrenceStats.missedCount > 0) {
                Text(
                    text = "Done ${occurrenceStats.completedCount} · Missed ${occurrenceStats.missedCount} · Streak ${occurrenceStats.currentStreak}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (item.type == LifeItemType.Task) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TaskStatus.entries.forEach { status ->
                            val selected = item.taskStatus == status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onTaskStatusChanged(status) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = status.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            onCompleted()
                            justCompleted = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        val checkScale by animateFloatAsState(
                            targetValue = if (justCompleted) 1.5f else 1f,
                            animationSpec = com.raulshma.dailylife.ui.theme.DailyLifeSpring.Bouncy,
                            label = "completeScale",
                        )
                        val checkTint by animateColorAsState(
                            targetValue = if (justCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            animationSpec = tween(durationMillis = 300),
                            label = "completeTint",
                        )
                        Box(contentAlignment = Alignment.Center) {
                            CompletionRipple(triggered = justCompleted, modifier = Modifier.size(36.dp))
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Complete",
                                tint = checkTint,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = checkScale
                                        scaleY = checkScale
                                    }
                                    .size(18.dp),
                            )
                        }
                    }

                    LaunchedEffect(justCompleted) {
                        if (justCompleted) {
                            kotlinx.coroutines.delay(800L)
                            justCompleted = false
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactMetadataStrip(
    item: LifeItem,
    onClick: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item.tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "#$tag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        if (item.isRecurring) {
            CompactMetaPill(
                icon = Icons.Filled.EventRepeat,
                label = item.recurrenceRule.frequency.label,
            )
        }
        item.reminderAt?.let { reminderAt ->
            CompactMetaPill(
                icon = Icons.Filled.AccessTime,
                label = reminderAt.format(TimestampFormatter),
            )
        }
        if (item.notificationSettings.enabled) {
            CompactMetaPill(
                icon = Icons.Filled.Notifications,
                label = "Notify",
            )
        }
    }
}

@Composable
private fun CompactMetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
