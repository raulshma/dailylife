package com.raulshma.dailylife.ui.ai

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.ui.ai.components.AIEmptyState
import com.raulshma.dailylife.ui.ai.components.AIEntryPreviewChip
import com.raulshma.dailylife.ui.ai.components.AIGradientAccent
import com.raulshma.dailylife.ui.ai.components.AIGradientBorderCard
import com.raulshma.dailylife.ui.ai.components.AIModelLoadingOverlay
import com.raulshma.dailylife.ui.ai.components.AINoModelCard
import com.raulshma.dailylife.ui.ai.components.AIShimmerLoader
import com.raulshma.dailylife.ui.ai.components.AIStatusChip
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import kotlinx.coroutines.launch
import java.time.LocalDate

private fun LifeItemType.emoji(): String = when (this) {
    LifeItemType.Thought -> "\uD83D\uDCAD"
    LifeItemType.Note -> "\uD83D\uDCDD"
    LifeItemType.Task -> "\u2705"
    LifeItemType.Reminder -> "\u23F0"
    LifeItemType.Photo -> "\uD83D\uDCF7"
    LifeItemType.Video -> "\uD83C\uDFA5"
    LifeItemType.Audio -> "\uD83C\uDF99\uFE0F"
    LifeItemType.Location -> "\uD83D\uDCCD"
    LifeItemType.Pdf -> "\uD83D\uDCC4"
    LifeItemType.Mixed -> "\uD83D\uDDC2\uFE0F"
}

data class ReflectionDateRange(
    val label: String,
    val start: LocalDate,
    val end: LocalDate,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIReflectionsScreen(
    aiExecutor: AIFeatureExecutor,
    engineService: LiteRTEngineService,
    recentEntries: List<LifeItem>,
    onBack: () -> Unit,
    onNavigateToModelManager: () -> Unit = {},
) {
    val today = LocalDate.now()
    val dateRanges = remember {
        listOf(
            ReflectionDateRange("Today", today, today),
            ReflectionDateRange("This Week", today.minusDays(7), today),
            ReflectionDateRange("This Month", today.minusMonths(1), today),
            ReflectionDateRange("Last 3 Months", today.minusMonths(3), today),
        )
    }
    var selectedRange by remember { mutableStateOf(dateRanges[1]) }
    var reflectionText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var modelLoadInitiated by remember { mutableStateOf(false) }
    val engineState by engineService.engineState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        modelLoadInitiated = true
        aiExecutor.ensureModelForFeature(com.raulshma.dailylife.domain.AIFeature.REFLECTION)
    }

    val filteredEntries = remember(selectedRange, recentEntries) {
        recentEntries.filter { entry ->
            val date = entry.createdAt.toLocalDate()
            !date.isBefore(selectedRange.start) && !date.isAfter(selectedRange.end)
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
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "AI Reflection",
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
                    actions = {
                        AIStatusChip(
                            engineState = engineState,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        if (isGenerating) {
                            IconButton(onClick = {
                                engineService.cancelGeneration()
                                isGenerating = false
                            }) {
                                Icon(Icons.Filled.Stop, contentDescription = "Stop")
                            }
                        } else if (reflectionText.isNotEmpty()) {
                            IconButton(onClick = {
                                reflectionText = ""
                            }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                            }
                        }
                    },
                    windowInsets = WindowInsets.safeDrawing,
                )
                AIGradientAccent()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            val isModelLoading = engineState is EngineState.LoadingModel ||
                engineState is EngineState.Initializing ||
                (modelLoadInitiated && engineState is EngineState.Idle)

            if (isModelLoading) {
                AIModelLoadingOverlay(
                    engineState = engineState,
                    modifier = Modifier.weight(1f),
                )
            } else if (engineState is EngineState.Error || (engineState is EngineState.Idle && modelLoadInitiated)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    AINoModelCard(onNavigateToModelManager = onNavigateToModelManager)
                }
            } else {
                Spacer(Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(dateRanges) { range ->
                        DateRangePill(
                            label = range.label,
                            selected = selectedRange == range,
                            onClick = { selectedRange = range },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${filteredEntries.size} entries in range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (filteredEntries.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        items(filteredEntries.take(12)) { entry ->
                            AIEntryPreviewChip(
                                title = entry.title.ifBlank { entry.type.label },
                                typeEmoji = entry.type.emoji(),
                            )
                        }
                        if (filteredEntries.size > 12) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+${filteredEntries.size - 12} more",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (isGenerating || reflectionText.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                    ) {
                        AIGradientBorderCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (isGenerating && reflectionText.isEmpty()) {
                                    AIShimmerLoader(lineCount = 4)
                                } else {
                                    Text(
                                        text = reflectionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                if (!isGenerating && reflectionText.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(reflectionText))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Copied to clipboard")
                                                }
                                            },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                Icons.Filled.ContentCopy,
                                                contentDescription = "Copy",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, reflectionText)
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Share reflection")
                                                context.startActivity(shareIntent)
                                            },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                Icons.Filled.Share,
                                                contentDescription = "Share",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (filteredEntries.isEmpty()) {
                            AIEmptyState(
                                title = "No entries to reflect on",
                                subtitle = "Add some journal entries for this time range first",
                                icon = {
                                    Icon(
                                        Icons.Filled.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    )
                                },
                            )
                        } else {
                            AIEmptyState(
                                title = "Reflect on your entries",
                                subtitle = "Select a time range and generate an AI reflection",
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        reflectionText = ""
                        isGenerating = true
                        scope.launch {
                            try {
                                aiExecutor.generateReflection(
                                    entries = filteredEntries,
                                    startDate = selectedRange.start,
                                    endDate = selectedRange.end,
                                ).collect { chunk ->
                                    reflectionText = chunk
                                }
                            } catch (e: Exception) {
                                reflectionText = "Error generating reflection: ${e.message}"
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    enabled = engineState is EngineState.Ready && filteredEntries.isNotEmpty() && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors().let {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Reflection")
                }
            }
        }
    }
}

@Composable
private fun DateRangePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "pillBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "pillText",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}
