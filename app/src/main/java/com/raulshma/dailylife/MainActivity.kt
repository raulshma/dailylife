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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.ui.DailyLifeApp
import com.raulshma.dailylife.ui.DailyLifeViewModel
import com.raulshma.dailylife.ui.LockScreen
import com.raulshma.dailylife.ui.OnboardingScreen
import com.raulshma.dailylife.ui.QuickAddDraft
import com.raulshma.dailylife.ui.theme.DailyLifeTheme
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

        setContent {
            DailyLifeTheme {
                when {
                    showOnboarding -> {
                        OnboardingScreen(
                            onComplete = {
                                prefs.edit().putBoolean("onboarding_complete", true).apply()
                                showOnboarding = false
                                if (lockEnabled) isLocked = true else isLocked = false
                            },
                        )
                    }
                    isLocked -> {
                        LockScreen(onUnlocked = { isLocked = false })
                    }
                    else -> {
                        val viewModel: DailyLifeViewModel = hiltViewModel()
                        DailyLifeApp(
                            viewModel = viewModel,
                            shareDraft = shareDraft,
                            onShareDraftConsumed = { shareDraft = null },
                        )
                    }
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
