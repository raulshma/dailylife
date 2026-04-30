package com.raulshma.dailylife.ui.ai.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing

@Composable
fun AIStatusChip(
    engineState: EngineState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val (dotColor, label) = when (engineState) {
        is EngineState.Ready -> MaterialTheme.colorScheme.primary to engineState.modelName
        is EngineState.LoadingModel -> MaterialTheme.colorScheme.tertiary to "Loading..."
        is EngineState.Initializing -> MaterialTheme.colorScheme.tertiary to "Initializing..."
        is EngineState.Error -> MaterialTheme.colorScheme.error to "Error"
        EngineState.Idle -> MaterialTheme.colorScheme.outline to "No model"
    }

    val isLoading = engineState is EngineState.LoadingModel || engineState is EngineState.Initializing

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isLoading) {
            PulsingDot(color = dotColor)
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
fun PulsingDot(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotPulse",
    )
    Box(
        modifier = modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = pulse)),
    )
}

@Composable
fun AIModelLoadingOverlay(
    engineState: EngineState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(
            targetState = engineState,
            transitionSpec = {
                fadeIn(tween(DailyLifeDuration.SHORT)) togetherWith
                    fadeOut(tween(DailyLifeDuration.SHORT))
            },
            label = "loadingPhase",
        ) { state ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AISparkleIcon(
                    size = 48.dp,
                    tint = when (state) {
                        is EngineState.LoadingModel, is EngineState.Initializing ->
                            MaterialTheme.colorScheme.tertiary
                        is EngineState.Ready -> MaterialTheme.colorScheme.primary
                        is EngineState.Error -> MaterialTheme.colorScheme.error
                        EngineState.Idle -> MaterialTheme.colorScheme.outline
                    },
                )
                when (state) {
                    is EngineState.LoadingModel -> {
                        Text(
                            text = "Loading model...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    is EngineState.Initializing -> {
                        Text(
                            text = "Initializing engine...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    is EngineState.Ready -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    is EngineState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    EngineState.Idle -> {}
                }
            }
        }
    }
}

@Composable
fun AINoModelCard(
    onNavigateToModelManager: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "No AI model installed",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Download a model to enable AI features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onNavigateToModelManager,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go to Model Manager")
            }
        }
    }
}

@Composable
fun AIGradientAccent(
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primary.copy(alpha = 0.3f),
                        tertiary.copy(alpha = 0.2f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

@Composable
fun AISparkleIcon(
    size: androidx.compose.ui.unit.Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkleScale",
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkleRotation",
    )
    Icon(
        Icons.Filled.SmartToy,
        contentDescription = null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            },
        tint = tint,
    )
}

@Composable
fun AITypingIndicator(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = index * 120, easing = DailyLifeEasing.Enter),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = bounce)),
            )
        }
    }
}

@Composable
fun AIGradientBorderCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val infiniteTransition = rememberInfiniteTransition(label = "gradientBorder")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(DailyLifeDuration.LONG * 4),
            repeatMode = RepeatMode.Restart,
        ),
        label = "gradientOffset",
    )
    ElevatedCard(
        modifier = modifier
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(primary, tertiary, primary),
                    start = Offset(offset - 400f, 0f),
                    end = Offset(offset, 200f),
                ),
                shape = MaterialTheme.shapes.large,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        content()
    }
}

@Composable
fun AIStreamingText(
    text: String,
    modifier: Modifier = Modifier,
) {
    if (text.isNotBlank()) {
        Column(modifier = modifier) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            AITypingIndicator()
        }
    }
}
