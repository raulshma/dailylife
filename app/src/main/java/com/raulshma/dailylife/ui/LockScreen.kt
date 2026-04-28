package com.raulshma.dailylife.ui

import android.app.KeyguardManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import com.raulshma.dailylife.ui.theme.DailyLifeRepeat
import com.raulshma.dailylife.ui.theme.staggerDelay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return
    val scope = rememberCoroutineScope()
    val biometricManager = remember { BiometricManager.from(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }

    val canAuthenticate = remember {
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val km = context.getSystemService(KeyguardManager::class.java)
                    km?.isDeviceSecure == true
                } else false
            }
        }
    }

    var authTrigger by remember { mutableIntStateOf(0) }
    var isAuthenticating by remember { mutableStateOf(false) }
    val unlockScale = remember { Animatable(1f) }

    LaunchedEffect(authTrigger) {
        if (!canAuthenticate) {
            delay(300)
            unlockScale.animateTo(0.9f, tween(150))
            unlockScale.animateTo(1.05f, tween(100))
            delay(50)
            onUnlocked()
            return@LaunchedEffect
        }

        while (lifecycleOwner.lifecycle.currentState < Lifecycle.State.RESUMED) {
            delay(100)
        }

        isAuthenticating = true

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock DailyLife")
            .setDescription("Authenticate to access your journal")
            .setAllowedAuthenticators(authenticators)
            .build()

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isAuthenticating = false
                    scope.launch {
                        unlockScale.animateTo(0.9f, tween(150))
                        unlockScale.animateTo(1.08f, tween(120))
                        delay(80)
                        onUnlocked()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    isAuthenticating = false
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        authTrigger++
                    }
                }

                override fun onAuthenticationFailed() {}
            },
        )

        delay(600)
        prompt.authenticate(promptInfo)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lockIconFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = DailyLifeRepeat.float(duration = 2500),
        label = "floatOffset",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .scale(unlockScale.value),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = DailyLifeTween.content(),
                    initialAlpha = 0f,
                ) + scaleIn(
                    animationSpec = tween(DailyLifeDuration.EMPHASIZED, easing = DailyLifeEasing.Emphasized),
                    initialScale = 0.6f,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = glowAlpha),
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .offset(y = floatOffset.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Lock",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = DailyLifeTween.fade(),
                    initialAlpha = 0f,
                ) + slideInVertically(
                    animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
                    initialOffsetY = { it / 4 },
                ),
            ) {
                Text(
                    text = "DailyLife",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(
                        DailyLifeDuration.MEDIUM + staggerDelay(1, 80),
                        easing = DailyLifeEasing.Ambient,
                    ),
                    initialAlpha = 0f,
                ) + slideInVertically(
                    animationSpec = tween(
                        DailyLifeDuration.MEDIUM,
                        delayMillis = staggerDelay(1, 80),
                        easing = DailyLifeEasing.Enter,
                    ),
                    initialOffsetY = { it / 4 },
                ),
            ) {
                Text(
                    text = if (isAuthenticating) "Verifying identity..." else if (canAuthenticate) "Tap below to unlock" else "App locked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = !isAuthenticating,
                enter = fadeIn(animationSpec = DailyLifeTween.fade()) + scaleIn(
                    animationSpec = DailyLifeTween.content(),
                    initialScale = 0.8f,
                ),
                exit = fadeOut(animationSpec = tween(DailyLifeDuration.SHORT)) + scaleOut(
                    animationSpec = tween(DailyLifeDuration.SHORT),
                    targetScale = 0.8f,
                ),
            ) {
                if (canAuthenticate) {
                    Button(
                        onClick = { authTrigger++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text("Unlock")
                    }
                } else {
                    TextButton(onClick = onUnlocked) {
                        Text("Skip (no lock available)")
                    }
                }
            }

            AnimatedVisibility(
                visible = isAuthenticating,
                enter = fadeIn(animationSpec = DailyLifeTween.fade()) + scaleIn(
                    animationSpec = DailyLifeTween.content(),
                    initialScale = 0.5f,
                ),
                exit = fadeOut(animationSpec = tween(DailyLifeDuration.SHORT)) + scaleOut(
                    animationSpec = tween(DailyLifeDuration.SHORT),
                    targetScale = 0.5f,
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
