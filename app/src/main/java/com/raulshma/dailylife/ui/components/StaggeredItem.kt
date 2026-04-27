package com.raulshma.dailylife.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.staggerDelay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberStaggeredVisibility(
    index: Int,
    baseDelayMs: Int = 50,
    maxDelayMs: Int = 400,
): MutableTransitionState<Boolean> {
    val state = remember { MutableTransitionState(false) }
    val delay = staggerDelay(index, baseDelayMs, maxDelayMs)
    LaunchedEffect(delay) {
        kotlinx.coroutines.delay(delay.toLong())
        state.targetState = true
    }
    return state
}

val StaggeredEnter: EnterTransition
    get() = fadeIn(
        animationSpec = DailyLifeTween.content<Float>()
    ) + slideInVertically(
        animationSpec = DailyLifeTween.content<androidx.compose.ui.unit.IntOffset>(),
        initialOffsetY = { it / 12 },
    )

val StaggeredExit: ExitTransition
    get() = fadeOut(
        animationSpec = DailyLifeTween.fade<Float>()
    ) + slideOutVertically(
        animationSpec = DailyLifeTween.fade<androidx.compose.ui.unit.IntOffset>(),
        targetOffsetY = { it / 12 },
    )
