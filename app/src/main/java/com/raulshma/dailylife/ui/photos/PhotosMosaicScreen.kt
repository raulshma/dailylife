package com.raulshma.dailylife.ui.photos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.StorageError
import com.raulshma.dailylife.ui.TimelineEntry
import com.raulshma.dailylife.ui.components.DateHeader
import com.raulshma.dailylife.ui.components.StorageWarningCard
import com.raulshma.dailylife.ui.components.SkeletonMosaicTile
import com.raulshma.dailylife.ui.components.rememberStaggeredVisibility
import com.raulshma.dailylife.ui.components.StaggeredEnter
import com.raulshma.dailylife.ui.theme.DailyLifeRepeat
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotosMosaicScreen(
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
        val seenDates = mutableSetOf<LocalDate>()
        var headerIndex = 0
        for (i in snapshot.indices) {
            val item = snapshot[i] ?: continue
            val date = item.createdAt.toLocalDate()
            if (date !in seenDates) {
                result.add(TimelineEntry.DateHeader(date, headerIndex++))
                seenDates.add(date)
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
internal fun EmptyPhotosScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = DailyLifeRepeat.float<Float>(),
        label = "float"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = DailyLifeRepeat.breathe<Float>(duration = 3500),
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
