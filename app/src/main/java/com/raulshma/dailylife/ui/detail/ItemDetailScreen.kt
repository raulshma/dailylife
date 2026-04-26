package com.raulshma.dailylife.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.OccurrenceStats
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.ui.DateFormatter
import com.raulshma.dailylife.ui.inferLocationPreview
import com.raulshma.dailylife.ui.isMediaLike
import com.raulshma.dailylife.ui.ItemPreview
import com.raulshma.dailylife.ui.LocalAnimatedVisibilityScope
import com.raulshma.dailylife.ui.LocalSharedTransitionScope
import com.raulshma.dailylife.ui.TimestampFormatter
import com.raulshma.dailylife.ui.TypeBadge
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeSpring
import com.raulshma.dailylife.ui.theme.staggerDelay
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    item: LifeItem,
    globalSettings: NotificationSettings,
    onBack: () -> Unit,
    onFavoriteToggled: () -> Unit,
    onPinnedToggled: () -> Unit,
    onCompleted: () -> Unit,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val haptic = LocalHapticFeedback.current
    val occurrenceStats = item.occurrenceStats()

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(DailyLifeDuration.SHORT.toLong())
        contentVisible = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LargeTopAppBar(
            title = {
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                AnimatedActionButton(
                    icon = Icons.Filled.PushPin,
                    contentDescription = if (item.isPinned) "Unpin" else "Pin",
                    tint = if (item.isPinned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onPinnedToggled,
                )
                AnimatedActionButton(
                    icon = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (item.isFavorite) "Remove favorite" else "Add favorite",
                    tint = if (item.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFavoriteToggled()
                    },
                    animate = item.isFavorite,
                )
                IconButton(onClick = onCompleted) {
                    Icon(Icons.Filled.Done, contentDescription = "Mark complete")
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ),
            modifier = Modifier.statusBarsPadding(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            // Media Hero
            if (item.type.isMediaLike() || item.inferImagePreviewUrl() != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    ItemPreview(item = item)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Content stagger reveal
            DetailContentSection(
                item = item,
                occurrenceStats = occurrenceStats,
                globalSettings = globalSettings,
                contentVisible = contentVisible,
                onNotificationsChanged = onNotificationsChanged,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContentSection(
    item: LifeItem,
    occurrenceStats: OccurrenceStats,
    globalSettings: NotificationSettings,
    contentVisible: Boolean,
    onNotificationsChanged: (ItemNotificationSettings) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Type + Date row
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(DailyLifeTween.fade<Float>()) + slideInVertically(
                DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
                initialOffsetY = { it / 3 }
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TypeBadge(type = item.type)
                Column {
                    Text(
                        text = item.type.label,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = item.createdAt.format(TimestampFormatter),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Body text
        if (item.body.isNotBlank()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // Tags
        if (item.tags.isNotEmpty()) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(DailyLifeTween.fade()) + slideInVertically(
                    DailyLifeTween.content(),
                    initialOffsetY = { it / 3 }
                ),
            ) {
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
            }
        }

        HorizontalDivider()

        // Metadata rows with stagger
        val metadataItems = buildList {
            add("Favorite" to if (item.isFavorite) "Yes" else "No")
            add("Pinned" to if (item.isPinned) "Yes" else "No")
            item.taskStatus?.let { add("Task status" to it.label) }
            item.reminderAt?.let { add("Reminder" to it.format(TimestampFormatter)) }
            if (item.isRecurring) {
                add("Recurrence" to item.recurrenceRule.frequency.label)
            }
            add("Completions" to occurrenceStats.completedCount.toString())
            if (item.isRecurring || occurrenceStats.missedCount > 0) {
                add("Missed" to occurrenceStats.missedCount.toString())
                add("Current streak" to occurrenceStats.currentStreak.toString())
            }
        }

        metadataItems.forEachIndexed { index, (label, value) ->
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(
                    tween(
                        durationMillis = DailyLifeDuration.SHORT,
                        delayMillis = staggerDelay(index, baseDelayMs = 40),
                        easing = DailyLifeEasing.Enter,
                    )
                ) + slideInVertically(
                    tween(
                        durationMillis = DailyLifeDuration.MEDIUM,
                        delayMillis = staggerDelay(index, baseDelayMs = 40),
                        easing = DailyLifeEasing.Enter,
                    ),
                    initialOffsetY = { it / 4 }
                ),
            ) {
                DetailLine(label = label, value = value)
            }
        }

        // Notification toggle
        val effectiveTime = item.notificationSettings.timeOverride
            ?: globalSettings.preferredTime
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(
                tween(
                    durationMillis = DailyLifeDuration.SHORT,
                    delayMillis = staggerDelay(metadataItems.size, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                )
            ) + slideInVertically(
                tween(
                    durationMillis = DailyLifeDuration.MEDIUM,
                    delayMillis = staggerDelay(metadataItems.size, baseDelayMs = 40),
                    easing = DailyLifeEasing.Enter,
                ),
                initialOffsetY = { it / 4 }
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (item.notificationSettings.enabled) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Item notifications at ${effectiveTime.format(com.raulshma.dailylife.ui.TimeFormatter)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = item.notificationSettings.enabled,
                    onCheckedChange = {
                        onNotificationsChanged(item.notificationSettings.copy(enabled = it))
                    }
                )
            }
        }
    }
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
private fun AnimatedActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    animate: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (animate) 1.2f else 1f,
        animationSpec = DailyLifeSpring.Bouncy,
        label = "actionScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (animate) 0f else -15f,
        animationSpec = DailyLifeSpring.Bouncy,
        label = "actionRotation"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
                .size(24.dp)
        )
    }
}
