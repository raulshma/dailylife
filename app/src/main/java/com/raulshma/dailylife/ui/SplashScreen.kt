package com.raulshma.dailylife.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.R
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeSpring

@Composable
fun SplashScreen(
    visible: Boolean,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = DailyLifeSpring.Gentle,
        label = "splashIconScale",
    )

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = DailyLifeDuration.MEDIUM,
                easing = DailyLifeEasing.Exit,
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2F5D8C),
                            Color(0xFF4F6F52),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_splash_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .scale(iconScale),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DailyLife",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.scale(iconScale),
                )
            }
        }
    }
}
