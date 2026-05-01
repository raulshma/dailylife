package com.raulshma.dailylife.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

// ═══════════════════════════════════════════════════════════════
// DailyLife Animation Design System
// Style: Calm & Fluid
// ═══════════════════════════════════════════════════════════════

object DailyLifeDuration {
    const val INSTANT = 100
    const val SHORT = 200
    const val MEDIUM = 350
    const val LONG = 500
    const val EMPHASIZED = 450
}

object DailyLifeEasing {
    /** Smooth deceleration for entering elements */
    val Enter = FastOutSlowInEasing

    /** Gentle exit with slight linear start */
    val Exit = LinearOutSlowInEasing

    /** Material 3 emphasized — primary UI motion */
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Extra gentle for subtle ambient motion */
    val Ambient = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
}

object DailyLifeSpring {
    /** Low stiffness, slight bounce — for delightful micro-interactions */
    val Bouncy: AnimationSpec<Float> = spring(
        stiffness = 380f,
        dampingRatio = 0.8f,
        visibilityThreshold = 0.01f
    )

    /** Gentle, no overshoot — for layout changes and calm motion */
    val Gentle: AnimationSpec<Float> = spring(
        stiffness = 300f,
        dampingRatio = 1.0f,
        visibilityThreshold = 0.01f
    )

    /** Stiff but still fluid — for quick snap-backs */
    val Snappy: AnimationSpec<Float> = spring(
        stiffness = 500f,
        dampingRatio = 0.9f,
        visibilityThreshold = 0.01f
    )

    /** Layout-specific spring for content size changes */
    val Layout: AnimationSpec<androidx.compose.ui.unit.IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )
}

object DailyLifeTween {
    /** Fade transitions — quick and unobtrusive */
    fun <T> fade(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.SHORT,
        easing = DailyLifeEasing.Ambient
    )

    /** Standard content replacement */
    fun <T> content(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.MEDIUM,
        easing = DailyLifeEasing.Enter
    )

    /** Emphasized — for primary actions and screen transitions */
    fun <T> emphasized(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.EMPHASIZED,
        easing = DailyLifeEasing.Emphasized
    )

    /** Slow ambient — for empty states and decorative motion */
    fun <T> ambient(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.LONG,
        easing = DailyLifeEasing.Ambient
    )

    /** Tab switch — snappy crossfade for bottom nav */
    fun <T> tab(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.INSTANT,
        easing = DailyLifeEasing.Enter
    )

    /** Depth navigation — smooth screen-to-screen with spatial motion */
    fun <T> depth(): FiniteAnimationSpec<T> = tween(
        durationMillis = DailyLifeDuration.EMPHASIZED,
        easing = DailyLifeEasing.Emphasized
    )
}

object DailyLifeRepeat {
    /** Calm breathing animation for recording indicators */
    fun <T> breathe(duration: Int = 1200): InfiniteRepeatableSpec<T> = infiniteRepeatable(
        animation = tween(duration / 2, easing = DailyLifeEasing.Ambient),
        repeatMode = RepeatMode.Reverse
    )

    /** Gentle float for empty state icons */
    fun <T> float(duration: Int = 2500): InfiniteRepeatableSpec<T> = infiniteRepeatable(
        animation = tween(duration, easing = DailyLifeEasing.Ambient),
        repeatMode = RepeatMode.Reverse
    )
}

/** Stagger delay calculator for lists and grids */
fun staggerDelay(index: Int, baseDelayMs: Int = 50, maxDelayMs: Int = 400): Int {
    return (index * baseDelayMs).coerceAtMost(maxDelayMs)
}

/** Grid stagger delay based on column position */
fun gridStaggerDelay(row: Int, column: Int, baseDelayMs: Int = 60): Int {
    return ((row + column) * baseDelayMs).coerceAtMost(500)
}
