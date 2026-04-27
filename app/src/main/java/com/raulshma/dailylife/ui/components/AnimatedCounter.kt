package com.raulshma.dailylife.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.raulshma.dailylife.ui.theme.DailyLifeDuration

/**
 * Animates a numeric value change with a smooth count-up/count-down effect.
 * Uses Calm & Fluid specs: medium duration, gentle easing.
 */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight? = FontWeight.SemiBold,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val animatedValue = remember(value) { Animatable(value.toFloat()) }

    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(
                durationMillis = DailyLifeDuration.MEDIUM,
                easing = FastOutSlowInEasing
            )
        )
    }

    Text(
        text = "${animatedValue.value.toInt()}",
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/**
 * AnimatedCounter for String values that represent numbers (e.g., formatted counts).
 * Falls back to immediate display if parsing fails.
 */
@Composable
fun AnimatedCounter(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight? = FontWeight.SemiBold,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val intValue = value.toIntOrNull() ?: 0
    AnimatedCounter(
        value = intValue,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
    )
}
