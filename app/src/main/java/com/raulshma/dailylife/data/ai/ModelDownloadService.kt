package com.raulshma.dailylife.data.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.raulshma.dailylife.MainActivity
import com.raulshma.dailylife.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "model_download"
private const val NOTIFICATION_ID = 1
private const val ACTION_CANCEL = "com.raulshma.dailylife.action.CANCEL_DOWNLOAD"
private const val EXTRA_MODEL_ID = "model_id"
private const val EXTRA_MODEL_NAME = "model_name"

class ModelDownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var currentModelId: String? = null
    private var currentModelName: String = ""
    private var progressJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                val modelId = intent.getStringExtra(EXTRA_MODEL_ID)
                if (modelId != null) {
                    ModelDownloadServiceBridge.cancelDownload(modelId)
                }
                maybeStopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val modelId = intent?.getStringExtra(EXTRA_MODEL_ID) ?: return START_NOT_STICKY
                val modelName = intent.getStringExtra(EXTRA_MODEL_NAME) ?: modelId
                currentModelId = modelId
                currentModelName = modelName
                startForeground(NOTIFICATION_ID, buildNotification(0f, modelName))
                acquireLocks()
                startProgressTracking(modelId, modelName)
            }
        }
        return START_NOT_STICKY
    }

    private fun startProgressTracking(modelId: String, modelName: String) {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (true) {
                val state = ModelDownloadServiceBridge.getDownloadState(modelId)
                val progress = ModelDownloadServiceBridge.getDownloadProgress(modelId)
                when (state) {
                    is com.raulshma.dailylife.domain.ModelDownloadState.Downloaded -> {
                        updateNotification(1f, modelName, done = true)
                        releaseLocks()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        return@launch
                    }
                    is com.raulshma.dailylife.domain.ModelDownloadState.DownloadFailed -> {
                        updateNotification(progress, modelName, error = true)
                        releaseLocks()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        return@launch
                    }
                    is com.raulshma.dailylife.domain.ModelDownloadState.NotDownloaded -> {
                        releaseLocks()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        return@launch
                    }
                    else -> {
                        updateNotification(progress, modelName)
                    }
                }
                delay(500)
            }
        }
    }

    private fun maybeStopSelf() {
        if (progressJob?.isActive != true) {
            releaseLocks()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun acquireLocks() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DailyLife:ModelDownloadWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(30 * 60 * 1000L) // 30 minutes max
        }

        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(
            WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
            "DailyLife:ModelDownloadWifiLock"
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun releaseLocks() {
        runCatching {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null
        }
        runCatching {
            wifiLock?.let { if (it.isHeld) it.release() }
            wifiLock = null
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AI Model Downloads",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows progress for on-device AI model downloads"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        progress: Float,
        modelName: String,
        done: Boolean = false,
        error: Boolean = false,
    ): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val cancelIntent = Intent(this, ModelDownloadService::class.java).apply {
            action = ACTION_CANCEL
            putExtra(EXTRA_MODEL_ID, currentModelId)
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = when {
            done -> "Download complete"
            error -> "Download failed"
            progress > 0f && progress < 1f -> "Downloading $modelName"
            else -> "Preparing download"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (done) "$modelName is ready" else if (error) "Tap to retry" else "${(progress * 100).toInt()}%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(!done && !error)
            .setContentIntent(contentIntent)
            .setProgress(100, (progress * 100).toInt(), progress <= 0f)
            .setOnlyAlertOnce(true)

        if (!done && !error) {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Cancel",
                cancelPendingIntent,
            )
        }

        return builder.build()
    }

    private fun updateNotification(
        progress: Float,
        modelName: String,
        done: Boolean = false,
        error: Boolean = false,
    ) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(progress, modelName, done, error))
    }

    override fun onDestroy() {
        serviceScope.cancel()
        releaseLocks()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context, modelId: String, modelName: String) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                putExtra(EXTRA_MODEL_ID, modelId)
                putExtra(EXTRA_MODEL_NAME, modelName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
