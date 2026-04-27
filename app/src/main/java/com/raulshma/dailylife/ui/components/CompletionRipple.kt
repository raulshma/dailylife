package com.raulshma.dailylife.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.ui.theme.DailyLifeDuration

@Composable
fun CompletionRipple(
    triggered: Boolean,
    color: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier,
) {
    val rippleScale = remember { Animatable(0.4f) }
    val rippleAlpha = remember { Animatable(0f) }

    LaunchedEffect(triggered) {
        if (triggered) {
            rippleAlpha.snapTo(0.5f)
            rippleScale.snapTo(0.4f)
            rippleScale.animateTo(
                targetValue = 2.2f,
                animationSpec = tween(durationMillis = DailyLifeDuration.LONG, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            )
            rippleAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = DailyLifeDuration.MEDIUM),
            )
        }
    }

    Canvas(modifier = modifier.size(48.dp)) {
        val currentScale = rippleScale.value
        val currentAlpha = rippleAlpha.value
        if (currentAlpha > 0.01f) {
            val radius = (size.minDimension / 2f) * currentScale
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(size.width / 2f, size.height / 2f),
                alpha = currentAlpha,
            )
        }
    }
}
