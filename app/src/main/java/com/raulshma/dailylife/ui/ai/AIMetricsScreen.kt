package com.raulshma.dailylife.ui.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.AIChatRepository
import com.raulshma.dailylife.data.db.DailyMetricsSummary
import com.raulshma.dailylife.data.db.FeatureMetricsSummary
import com.raulshma.dailylife.data.db.ModelMetricsSummary
import com.raulshma.dailylife.domain.AIMetric
import com.raulshma.dailylife.ui.ai.components.AIGradientAccent
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import kotlinx.coroutines.delay

private enum class MetricsTab(val label: String) {
    Overview("Overview"),
    Individual("Individual"),
    Daily("Daily"),
}

private fun featureIcon(feature: String): ImageVector = when (feature.uppercase()) {
    "SMART_TITLE" -> Icons.Filled.ShortText
    "TAG_SUGGESTION" -> Icons.Filled.Label
    "SUMMARIZE" -> Icons.Filled.MenuBook
    "REFLECTION" -> Icons.Filled.AutoFixHigh
    "MOOD_ANALYSIS" -> Icons.Filled.Mood
    "NL_SEARCH" -> Icons.Filled.Search
    "PHOTO_DESCRIPTION" -> Icons.Filled.CameraAlt
    "AUDIO_SUMMARY" -> Icons.Filled.Audiotrack
    "CHAT" -> Icons.AutoMirrored.Filled.Chat
    "WRITING_ASSISTANT" -> Icons.Filled.EditNote
    else -> Icons.Filled.SmartToy
}

private fun featureDisplayName(feature: String): String =
    feature.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

private fun formatTimestamp(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}

private fun formatDuration(ms: Long): String = when {
    ms < 1000 -> "${ms}ms"
    ms < 60_000 -> "%.1fs".format(ms / 1000.0)
    else -> "%.1fm".format(ms / 60_000.0)
}

private fun formatNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}

private fun formatThroughput(charsPerSec: Double?): String =
    if (charsPerSec != null && charsPerSec > 0) "%.0f".format(charsPerSec) else "N/A"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMetricsScreen(
    chatRepository: AIChatRepository,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val weekMs = 7L * 24 * 60 * 60 * 1000
    val weekAgo = System.currentTimeMillis() - weekMs

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    var totalRequests by remember { mutableStateOf(0) }
    var totalInputChars by remember { mutableStateOf(0L) }
    var totalOutputChars by remember { mutableStateOf(0L) }
    var avgLatency by remember { mutableStateOf<Double?>(null) }
    var avgTtft by remember { mutableStateOf<Double?>(null) }
    var avgThroughput by remember { mutableStateOf<Double?>(null) }
    var errorCount by remember { mutableStateOf(0) }
    var featureMetrics by remember { mutableStateOf<List<FeatureMetricsSummary>>(emptyList()) }
    var modelMetrics by remember { mutableStateOf<List<ModelMetricsSummary>>(emptyList()) }
    var dailyMetrics by remember { mutableStateOf<List<DailyMetricsSummary>>(emptyList()) }
    var individualMetrics by remember { mutableStateOf<List<AIMetric>>(emptyList()) }
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var isFetchingMore by remember { mutableStateOf(false) }
    var allLoaded by remember { mutableStateOf(false) }

    val loadAggregated: suspend () -> Unit = {
        totalRequests = chatRepository.getTotalRequestsSince(weekAgo)
        totalInputChars = chatRepository.getTotalInputCharsSince(weekAgo)
        totalOutputChars = chatRepository.getTotalOutputCharsSince(weekAgo)
        avgLatency = chatRepository.getAvgLatencySince(weekAgo)
        avgTtft = chatRepository.getAvgTtftSince(weekAgo)
        avgThroughput = chatRepository.getAvgTokensPerSecSince(weekAgo)
        errorCount = chatRepository.getErrorCountSince(weekAgo)
        featureMetrics = chatRepository.getMetricsByFeature(weekAgo)
        modelMetrics = chatRepository.getMetricsByModel(weekAgo)
        dailyMetrics = chatRepository.getDailyMetrics(weekAgo)
    }

    val loadIndividual: suspend (Int) -> Unit = { offset ->
        val batch = chatRepository.getPaginatedMetrics(20, offset)
        if (offset == 0) individualMetrics = batch else individualMetrics += batch
        allLoaded = batch.size < 20
    }

    LaunchedEffect(Unit) {
        delay(50)
        loadAggregated()
        loadIndividual(0)
        isLoading = false
    }

    LaunchedEffect(selectedFeature) {
        if (selectedTab == 2) {
            allLoaded = false
            loadIndividual(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "AI Usage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    windowInsets = WindowInsets.safeDrawing,
                )
                AIGradientAccent()
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text(
                    "Last 7 days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    divider = {},
                ) {
                    MetricsTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                when (selectedTab) {
                    0 -> OverviewTab(
                        totalRequests = totalRequests,
                        totalInputChars = totalInputChars,
                        totalOutputChars = totalOutputChars,
                        avgLatency = avgLatency,
                        avgTtft = avgTtft,
                        avgThroughput = avgThroughput,
                        errorCount = errorCount,
                        featureMetrics = featureMetrics,
                        modelMetrics = modelMetrics,
                    )
                    1 -> IndividualTab(
                        metrics = individualMetrics,
                        featureMetrics = featureMetrics,
                        selectedFeature = selectedFeature,
                        onFeatureSelected = { selectedFeature = it },
                        isLoadingMore = isFetchingMore,
                        allLoaded = allLoaded,
                        onLoadMore = {
                            if (!isFetchingMore && !allLoaded) {
                                isFetchingMore = true
                            }
                        },
                    )
                    2 -> DailyTab(dailyMetrics = dailyMetrics)
                }
            }
        }
    }

    LaunchedEffect(isFetchingMore) {
        if (isFetchingMore) {
            loadIndividual(individualMetrics.size)
            isFetchingMore = false
        }
    }
}

@Composable
private fun OverviewTab(
    totalRequests: Int,
    totalInputChars: Long,
    totalOutputChars: Long,
    avgLatency: Double?,
    avgTtft: Double?,
    avgThroughput: Double?,
    errorCount: Int,
    featureMetrics: List<FeatureMetricsSummary>,
    modelMetrics: List<ModelMetricsSummary>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedMetricCard(
                    title = "Requests",
                    value = totalRequests.toString(),
                    modifier = Modifier.weight(1f),
                )
                AnimatedMetricCard(
                    title = "Output",
                    value = formatNumber(totalOutputChars),
                    subtitle = "chars",
                    modifier = Modifier.weight(1f),
                )
                AnimatedMetricCard(
                    title = "Input",
                    value = formatNumber(totalInputChars),
                    subtitle = "chars",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                MetricCard(
                    title = "Avg Latency",
                    value = if (avgLatency != null) formatDuration(avgLatency.toLong()) else "N/A",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Avg TTFT",
                    value = if (avgTtft != null) formatDuration(avgTtft.toLong()) else "N/A",
                    subtitle = "First token",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                MetricCard(
                    title = "Throughput",
                    value = "${formatThroughput(avgThroughput)} c/s",
                    subtitle = "Chars/sec",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Errors",
                    value = errorCount.toString(),
                    modifier = Modifier.weight(1f),
                    isError = errorCount > 0,
                )
            }
        }

        if (errorCount > 0 && totalRequests > 0) {
            item {
                val errorRate = errorCount.toFloat() / totalRequests
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                "Error Rate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        Text(
                            "%.1f%%".format(errorRate * 100),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        if (featureMetrics.isNotEmpty()) {
            item {
                Text(
                    "Usage by Feature",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            val maxCount = featureMetrics.maxOf { it.count }
            items(featureMetrics) { fm ->
                FeatureMetricCard(fm, maxCount)
            }
        }

        if (modelMetrics.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Usage by Model",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            items(modelMetrics) { mm ->
                ModelMetricCard(mm)
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun IndividualTab(
    metrics: List<AIMetric>,
    featureMetrics: List<FeatureMetricsSummary>,
    selectedFeature: String?,
    onFeatureSelected: (String?) -> Unit,
    isLoadingMore: Boolean,
    allLoaded: Boolean,
    onLoadMore: () -> Unit,
) {
    val filteredMetrics = if (selectedFeature != null) {
        metrics.filter { it.feature.uppercase() == selectedFeature.uppercase() }
    } else metrics

    val listState = rememberLazyListState()

    LaunchedEffect(listState.canScrollForward, listState.firstVisibleItemIndex) {
        if (!listState.canScrollForward && !allLoaded && !isLoadingMore) {
            onLoadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (featureMetrics.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = selectedFeature == null,
                    onClick = { onFeatureSelected(null) },
                    label = { Text("All") },
                    modifier = Modifier.height(32.dp),
                )
                featureMetrics
                    .sortedByDescending { it.count }
                    .take(5)
                    .forEach { fm ->
                        FilterChip(
                            selected = selectedFeature?.uppercase() == fm.feature.uppercase(),
                            onClick = {
                                onFeatureSelected(
                                    if (selectedFeature?.uppercase() == fm.feature.uppercase()) null
                                    else fm.feature,
                                )
                            },
                            label = {
                                Text(
                                    featureDisplayName(fm.feature),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            modifier = Modifier.height(32.dp),
                        )
                    }
            }
        }

        if (filteredMetrics.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No usage data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(filteredMetrics, key = { it.id }) { metric ->
                    IndividualMetricCard(metric)
                }

                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DailyTab(dailyMetrics: List<DailyMetricsSummary>) {
    if (dailyMetrics.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No daily data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        val maxDailyCount = dailyMetrics.maxOf { it.count }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Daily Activity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${dailyMetrics.sumOf { it.count }} total requests",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(dailyMetrics) { day ->
                DailyMetricCard(day, maxDailyCount)
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AnimatedMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val numericValue = value.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
    val animatedValue = remember(numericValue) { Animatable(0f) }

    LaunchedEffect(numericValue) {
        animatedValue.animateTo(
            targetValue = numericValue.toFloat(),
            animationSpec = tween(
                durationMillis = DailyLifeDuration.EMPHASIZED,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    val displayValue = when {
        numericValue >= 1_000_000 -> "%.1fM".format(animatedValue.value / 1_000_000.0)
        numericValue >= 1_000 -> "%.1fK".format(animatedValue.value / 1_000.0)
        else -> "%.0f".format(animatedValue.value)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = displayValue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isError: Boolean = false,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun FeatureMetricCard(
    fm: FeatureMetricsSummary,
    maxCount: Int,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxCount > 0) fm.count.toFloat() / maxCount else 0f,
        animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
        label = "featureProgress",
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        featureIcon(fm.feature),
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = featureDisplayName(fm.feature),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${fm.count}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = " req",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricLabel("Latency", if (fm.avgLatencyMs != null) formatDuration(fm.avgLatencyMs.toLong()) else "N/A")
                MetricLabel("TTFT", if (fm.avgTtftMs != null) formatDuration(fm.avgTtftMs.toLong()) else "N/A")
                MetricLabel("Output", formatNumber(fm.totalOutputChars ?: 0))
            }

            if (fm.errorCount > 0) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "${fm.errorCount} error${if (fm.errorCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelMetricCard(mm: ModelMetricsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp).size(16.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mm.modelId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${mm.count} requests",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (mm.avgLatencyMs != null) formatDuration(mm.avgLatencyMs.toLong()) else "N/A",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "avg latency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun IndividualMetricCard(metric: AIMetric) {
    val containerColor by animateColorAsState(
        targetValue = if (metric.isError)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "metricColor",
    )

    val throughput = if (metric.totalGenerationMs > 0) {
        metric.outputCharCount * 1000.0 / metric.totalGenerationMs
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        featureIcon(metric.feature),
                        contentDescription = null,
                        modifier = Modifier.padding(5.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = featureDisplayName(metric.feature),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = formatTimestamp(metric.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (metric.isError) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = formatDuration(metric.totalGenerationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            if (metric.isError && metric.errorMessage != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = metric.errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetricLabel("TTFT", if (metric.timeToFirstTokenMs != null) formatDuration(metric.timeToFirstTokenMs) else "N/A")
                MetricLabel("Input", "${metric.inputCharCount} c")
                MetricLabel("Output", "${metric.outputCharCount} c")
                if (throughput != null) {
                    MetricLabel("Speed", "%.0f c/s".format(throughput))
                }
                if (metric.modelId != null) {
                    MetricLabel("Model", metric.modelId)
                }
            }
        }
    }
}

@Composable
private fun DailyMetricCard(
    day: DailyMetricsSummary,
    maxCount: Int,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxCount > 0) day.count.toFloat() / maxCount else 0f,
        animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
        label = "dailyProgress",
    )

    val isToday = try {
        val today = java.time.LocalDate.now()
        val parsed = java.time.LocalDate.parse(day.day)
        today == parsed
    } catch (_: Exception) { false }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = formatDayLabel(day.day),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        if (isToday) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${day.count} requests",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (day.totalChars != null && day.totalChars > 0) {
                        Text(
                            text = formatNumber(day.totalChars) + " chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            androidx.compose.material3.LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (day.avgLatencyMs != null) {
                    MetricLabel("Avg latency", formatDuration(day.avgLatencyMs.toLong()))
                }
                if (day.errorCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "${day.errorCount} error${if (day.errorCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLabel(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatDayLabel(day: String): String {
    return try {
        val date = java.time.LocalDate.parse(day)
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d"))
        }
    } catch (_: Exception) {
        day
    }
}


