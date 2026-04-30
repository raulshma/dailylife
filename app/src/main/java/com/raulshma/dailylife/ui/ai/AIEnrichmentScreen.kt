package com.raulshma.dailylife.ui.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.EnrichmentFeature
import com.raulshma.dailylife.domain.EnrichmentProcessorStatus
import com.raulshma.dailylife.domain.EnrichmentProgress
import com.raulshma.dailylife.domain.EnrichmentSettings
import com.raulshma.dailylife.domain.EnrichmentTask
import com.raulshma.dailylife.domain.EnrichmentTaskStatus
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.requiredCapabilities
import com.raulshma.dailylife.ui.ai.components.AIGradientAccent
import com.raulshma.dailylife.ui.ai.components.AIMetadataBadge
import com.raulshma.dailylife.ui.ai.components.AISparkleIcon
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeSpring
import androidx.compose.animation.core.FastOutSlowInEasing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIEnrichmentScreen(
    progress: EnrichmentProgress,
    settings: EnrichmentSettings,
    history: List<EnrichmentTask>,
    modelCapabilities: Set<com.raulshma.dailylife.domain.AIModelCapability>,
    defaultModelName: String?,
    onSettingsChanged: (EnrichmentSettings) -> Unit,
    onStartBatch: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onClearHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    onLoadHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        onLoadHistory()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "AI Enrichment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    windowInsets = WindowInsets.safeDrawing,
                )
                AIGradientAccent()
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                EnrichmentSettingsSection(
                    settings = settings,
                    onSettingsChanged = onSettingsChanged,
                    isProcessing = progress.isActive,
                    modelCapabilities = modelCapabilities,
                    defaultModelName = defaultModelName,
                )
            }

            item {
                EnrichmentProcessingSection(
                    progress = progress,
                    onStartBatch = onStartBatch,
                    onPause = onPause,
                    onResume = onResume,
                    onCancel = onCancel,
                    settings = settings,
                )
            }

            if (history.isEmpty() && !progress.isActive) {
                item {
                    EmptyStateCard()
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HistorySectionHeader(onClearHistory = onClearHistory)
                }

                items(history, key = { it.id }) { task ->
                    EnrichmentTaskCard(task = task)
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EnrichmentSettingsSection(
    settings: EnrichmentSettings,
    onSettingsChanged: (EnrichmentSettings) -> Unit,
    isProcessing: Boolean,
    modelCapabilities: Set<com.raulshma.dailylife.domain.AIModelCapability>,
    defaultModelName: String?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val hasTextGen = com.raulshma.dailylife.domain.AIModelCapability.TEXT_GENERATION in modelCapabilities
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (settings.enabled && hasTextGen) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                ) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = if (settings.enabled && hasTextGen) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Enrichment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            modelCapabilities.isEmpty() -> "No model installed"
                            !hasTextGen -> "Model missing text generation"
                            settings.enabled -> "Enriches new items automatically"
                            else -> "Disabled"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!hasTextGen) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(enabled = it)) },
                    enabled = !isProcessing && hasTextGen,
                )
            }

            if (modelCapabilities.isEmpty() ||
                com.raulshma.dailylife.domain.AIModelCapability.TEXT_GENERATION !in modelCapabilities
            ) {
                ModelIncompatibleBanner(defaultModelName = defaultModelName)
            }

            SectionLabel(text = "Features")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EnrichmentFeature.entries.forEach { feature ->
                    val isSelected = feature in settings.features
                    val featureSupported = feature.requiredCapabilities().all { it in modelCapabilities }
                    val (icon, title, description) = featureInfo(feature)
                    FeatureToggleChip(
                        icon = icon,
                        title = title,
                        description = description,
                        isSelected = isSelected,
                        enabled = !isProcessing && featureSupported,
                        onClick = {
                            val newFeatures = if (isSelected) {
                                settings.features - feature
                            } else {
                                settings.features + feature
                            }
                            onSettingsChanged(settings.copy(features = newFeatures))
                        },
                    )
                }
            }

            SectionLabel(text = "Item Types")

            val eligibleTypes = settings.eligibleTypes
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LifeItemType.entries.forEach { type ->
                    val isSelected = type in eligibleTypes
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTypes = if (isSelected) {
                                eligibleTypes - type
                            } else {
                                eligibleTypes + type
                            }
                            onSettingsChanged(settings.copy(eligibleTypes = newTypes))
                        },
                        enabled = !isProcessing && modelCapabilities.isNotEmpty(),
                        label = {
                            Text(
                                type.name,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        modifier = Modifier.height(32.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        Icons.Filled.Archive,
                        contentDescription = null,
                        modifier = Modifier.padding(5.dp).size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Text(
                    text = "Skip archived items",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = settings.skipArchived,
                    onCheckedChange = { onSettingsChanged(settings.copy(skipArchived = it)) },
                    enabled = !isProcessing,
                )
            }
        }
    }
}

@Composable
private fun ModelIncompatibleBanner(defaultModelName: String?) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (defaultModelName != null) "Model capabilities limited" else "No model installed",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = if (defaultModelName != null) {
                        "$defaultModelName only supports some enrichment features. Install models with VISION or AUDIO capabilities to unlock photo and audio enrichment."
                    } else {
                        "Download a model to use enrichment. Models with TEXT_GENERATION handle titles/tags/descriptions. VISION models describe photos. AUDIO models summarize recordings."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FeatureToggleChip(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "featureChipBg",
    )
    val iconColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        tonalElevation = if (isSelected) 1.dp else 0.dp,
        modifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp).size(18.dp),
                    tint = iconColor,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnrichmentProcessingSection(
    progress: EnrichmentProgress,
    onStartBatch: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    settings: EnrichmentSettings,
) {
    AnimatedContent(
        targetState = progress.status,
        transitionSpec = {
            fadeIn(tween(DailyLifeDuration.SHORT)) togetherWith
                fadeOut(tween(DailyLifeDuration.SHORT))
        },
        label = "processingState",
    ) { status ->
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (status) {
                    EnrichmentProcessorStatus.IDLE -> IdleProcessingState(
                        settings = settings,
                        onStartBatch = onStartBatch,
                    )
                    EnrichmentProcessorStatus.PROCESSING -> ActiveProcessingState(
                        progress = progress,
                        onPause = onPause,
                        onCancel = onCancel,
                    )
                    EnrichmentProcessorStatus.PAUSED -> PausedProcessingState(
                        progress = progress,
                        onResume = onResume,
                        onCancel = onCancel,
                    )
                    EnrichmentProcessorStatus.DONE -> CompletedProcessingState(
                        progress = progress,
                        onDismiss = onCancel,
                    )
                    EnrichmentProcessorStatus.CANCELLED -> CompletedProcessingState(
                        progress = progress,
                        isCancelled = true,
                        onDismiss = onCancel,
                    )
                    EnrichmentProcessorStatus.ERROR -> ErrorProcessingState(
                        progress = progress,
                        onRetry = onStartBatch,
                        onDismiss = onCancel,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleProcessingState(
    settings: EnrichmentSettings,
    onStartBatch: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                AISparkleIcon(
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(6.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ready to enrich",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Generate titles, tags, and descriptions for your items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (settings.features.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${settings.features.size} feature${if (settings.features.size > 1) "s" else ""} selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "\u00B7",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "${settings.eligibleTypes.size} type${if (settings.eligibleTypes.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Button(
            onClick = onStartBatch,
            enabled = settings.features.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Start Processing")
        }
    }
}

@Composable
private fun ActiveProcessingState(
    progress: EnrichmentProgress,
    onPause: () -> Unit,
    onCancel: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressFraction,
        animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
        label = "enrichmentProgress",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PulsingIndicator()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enriching items...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (progress.currentFeature != null) {
                    val (_, title, _) = featureInfo(progress.currentFeature)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "${progress.processedItems}/${progress.totalItems}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        if (progress.failedItems > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "${progress.failedItems} failed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = onPause,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Pause")
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun PausedProcessingState(
    progress: EnrichmentProgress,
    onResume: () -> Unit,
    onCancel: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressFraction,
        animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
        label = "enrichmentProgress",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Icon(
                    Icons.Filled.PauseCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${progress.processedItems} of ${progress.totalItems} items processed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.outline,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onResume,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Resume")
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun CompletedProcessingState(
    progress: EnrichmentProgress,
    isCancelled: Boolean = false,
    onDismiss: () -> Unit,
) {
    val durationMs = progress.endTimeMs?.let { end ->
        progress.startTimeMs?.let { start -> end - start }
    }
    val durationText = durationMs?.let { ms ->
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        if (minutes > 0) "${minutes}m ${remainingSeconds}s" else "${seconds}s"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val iconTint = if (isCancelled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
            val iconBg = if (isCancelled) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBg,
            ) {
                Icon(
                    if (isCancelled) Icons.Filled.Close else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                    tint = iconTint,
                )
            }
            Column {
                Text(
                    text = if (isCancelled) "Processing cancelled" else "Enrichment complete",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${progress.processedItems} items processed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedMetricCard(
                title = "Processed",
                value = "${progress.processedItems}",
                modifier = Modifier.weight(1f),
            )
            AnimatedMetricCard(
                title = "Failed",
                value = "${progress.failedItems}",
                modifier = Modifier.weight(1f),
                isError = progress.failedItems > 0,
            )
            if (durationText != null) {
                AnimatedMetricCard(
                    title = "Duration",
                    value = durationText,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(if (isCancelled) "Dismiss" else "Done")
        }
    }
}

@Composable
private fun ErrorProcessingState(
    progress: EnrichmentProgress,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Column {
                Text(
                    text = "Processing error",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Something went wrong. ${progress.processedItems} items were processed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Retry")
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun PulsingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "processingPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.AutoFixHigh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
            )
        }
    }
}

@Composable
private fun AnimatedMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
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
        }
    }
}

@Composable
private fun EmptyStateCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AISparkleIcon(
                size = 48.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Text(
                text = "No enrichment history yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Start processing to enrich your items with AI-generated titles, tags, and descriptions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistorySectionHeader(onClearHistory: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent History",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onClearHistory) {
            Icon(
                Icons.Filled.DeleteSweep,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text("Clear")
        }
    }
}

@Composable
private fun EnrichmentTaskCard(task: EnrichmentTask) {
    val (icon, title, _) = featureInfo(task.feature)
    val containerColor by animateColorAsState(
        targetValue = when (task.status) {
            EnrichmentTaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceContainerLow
            EnrichmentTaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            EnrichmentTaskStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "taskCardBg",
    )
    val statusIcon: ImageVector = when (task.status) {
        EnrichmentTaskStatus.COMPLETED -> Icons.Filled.CheckCircle
        EnrichmentTaskStatus.FAILED -> Icons.Filled.ErrorOutline
        EnrichmentTaskStatus.SKIPPED -> Icons.Filled.SkipNext
    }
    val statusColor = when (task.status) {
        EnrichmentTaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        EnrichmentTaskStatus.FAILED -> MaterialTheme.colorScheme.error
        EnrichmentTaskStatus.SKIPPED -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (task.status) {
                    EnrichmentTaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                    EnrichmentTaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    EnrichmentTaskStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(5.dp).size(16.dp),
                    tint = when (task.status) {
                        EnrichmentTaskStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
                        EnrichmentTaskStatus.FAILED -> MaterialTheme.colorScheme.error
                        EnrichmentTaskStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Item #${task.itemId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (task.modelId != null) {
                        Text(
                            text = task.modelId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (task.status) {
                    EnrichmentTaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                    EnrichmentTaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    EnrichmentTaskStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = task.status.name,
                        modifier = Modifier.size(12.dp),
                        tint = statusColor,
                    )
                    if (task.processingTimeMs != null) {
                        Text(
                            text = formatDuration(task.processingTimeMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

private fun featureInfo(feature: EnrichmentFeature): Triple<ImageVector, String, String> =
    when (feature) {
        EnrichmentFeature.SMART_TITLE -> Triple(
            Icons.Filled.ShortText,
            "Smart Title",
            "Generate concise titles for items",
        )
        EnrichmentFeature.TAGS -> Triple(
            Icons.Filled.Label,
            "Tags",
            "Suggest relevant tags based on content",
        )
        EnrichmentFeature.DESCRIPTION -> Triple(
            Icons.Filled.Description,
            "Description",
            "Generate AI summaries and descriptions",
        )
        EnrichmentFeature.PHOTO_DESCRIPTION -> Triple(
            Icons.Filled.CameraAlt,
            "Photo Description",
            "Describe photos using vision AI",
        )
        EnrichmentFeature.AUDIO_SUMMARY -> Triple(
            Icons.Filled.Audiotrack,
            "Audio Summary",
            "Summarize audio recordings",
        )
    }

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    return if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"
}
