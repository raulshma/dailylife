package com.raulshma.dailylife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulshma.dailylife.ui.SplashScreen
import kotlinx.coroutines.delay
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.ui.DailyLifeApp
import com.raulshma.dailylife.ui.DailyLifeViewModel
import com.raulshma.dailylife.ui.LockScreen
import com.raulshma.dailylife.ui.OnboardingScreen
import com.raulshma.dailylife.ui.QuickAddDraft
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeTheme
import com.raulshma.dailylife.ui.theme.DailyLifeTween
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    var shareDraft by mutableStateOf<QuickAddDraft?>(null)
        private set

    private var isLocked by mutableStateOf(true)
    private var showOnboarding by mutableStateOf(false)
    private var stoppedAtMillis = 0L
    private var paletteKey by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        handleShareIntent(intent)

        val prefs = getSharedPreferences("dailylife_prefs", MODE_PRIVATE)
        val onboardingComplete = prefs.getBoolean("onboarding_complete", false)
        val lockEnabled = prefs.getBoolean("lock_enabled", false)

        if (!onboardingComplete) {
            showOnboarding = true
        } else if (lockEnabled) {
            isLocked = true
        } else {
            isLocked = false
        }

        val isColdStart = savedInstanceState == null

        setContent {
            var isSplashVisible by remember { mutableStateOf(isColdStart) }

            LaunchedEffect(Unit) {
                if (isSplashVisible) {
                    // Splash initialization: preload data, warm caches, etc.
                    // Minimum hold duration ensures the splash never flickers
                    delay(1_500L)
                    isSplashVisible = false
                }
            }

            DailyLifeTheme(paletteKey = paletteKey) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        modifier = Modifier.fillMaxSize(),
                        targetState = when {
                            showOnboarding -> 0
                            isLocked -> 1
                            else -> 2
                        },
                        transitionSpec = {
                            (fadeIn(
                                animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
                            ) + scaleIn(
                                animationSpec = tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter),
                                initialScale = 0.96f,
                            )).togetherWith(
                                fadeOut(
                                    animationSpec = tween(DailyLifeDuration.SHORT),
                                ) + scaleOut(
                                    animationSpec = tween(DailyLifeDuration.SHORT),
                                    targetScale = 1.02f,
                                ),
                            )
                        },
                        label = "screenTransition",
                    ) { screenState ->
                        when (screenState) {
                            0 -> {
                                OnboardingScreen(
                                    onComplete = {
                                        prefs.edit().putBoolean("onboarding_complete", true).apply()
                                        showOnboarding = false
                                        if (lockEnabled) isLocked = true else isLocked = false
                                    },
                                )
                            }
                            1 -> {
                                Box(contentAlignment = Alignment.Center) {
                                    LockScreen(onUnlocked = { isLocked = false })
                                }
                            }
                            else -> {
                                val viewModel: DailyLifeViewModel = hiltViewModel()
                                DailyLifeApp(
                                    viewModel = viewModel,
                                    shareDraft = shareDraft,
                                    onShareDraftConsumed = { shareDraft = null },
                                    onPaletteChanged = { paletteKey++ },
                                )
                            }
                        }
                    }

                    SplashScreen(
                        visible = isSplashVisible,
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stoppedAtMillis = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("dailylife_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("lock_enabled", false) && !showOnboarding && stoppedAtMillis > 0L) {
            if (System.currentTimeMillis() - stoppedAtMillis > LOCK_GRACE_PERIOD_MS) {
                isLocked = true
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) return

        val mimeType = intent.type ?: ""
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        var body = ""
        val uris = mutableListOf<Uri>()

        if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val clipData = intent.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { uris.add(it) }
                }
            }
            val extraUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            if (extraUris != null) uris.addAll(extraUris)
        } else {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris.add(it) }
        }

        val copiedUris = uris.mapNotNull { copyToInternal(it) }
        body = (copiedUris.map { it.toString() } + listOfNotNull(sharedText.ifBlank { null }))
            .joinToString("\n")

        val inferredType = when {
            mimeType.startsWith("image/") -> LifeItemType.Photo
            mimeType.startsWith("video/") -> LifeItemType.Video
            mimeType.startsWith("audio/") -> LifeItemType.Audio
            mimeType == "application/pdf" -> LifeItemType.Pdf
            else -> LifeItemType.Note
        }

        shareDraft = QuickAddDraft(
            typeName = inferredType.name,
            body = body,
        )
    }

    private fun copyToInternal(uri: Uri): Uri? {
        return runCatching {
            val ext = contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "dat"
            val destFile = File(cacheDir, "shared_${System.currentTimeMillis()}.$ext")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        }.getOrNull()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), NotificationPermissionRequestCode)
        }
    }
}

private const val NotificationPermissionRequestCode = 42
private const val LOCK_GRACE_PERIOD_MS = 5_000L
