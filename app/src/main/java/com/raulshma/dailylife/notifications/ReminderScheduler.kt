package com.raulshma.dailylife.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.raulshma.dailylife.MainActivity
import com.raulshma.dailylife.R
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.db.ALL_MIGRATIONS
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.DatabasePassphraseManager
import com.raulshma.dailylife.data.db.toEntity
import com.raulshma.dailylife.data.db.toLifeItem
import com.raulshma.dailylife.data.db.toNotificationSettings
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

interface ReminderScheduler {
    fun sync(
        items: List<LifeItem>,
        settings: NotificationSettings,
        now: LocalDateTime = LocalDateTime.now(),
    )

    fun cancel(itemId: Long)
}

object NoopReminderScheduler : ReminderScheduler {
    override fun sync(
        items: List<LifeItem>,
        settings: NotificationSettings,
        now: LocalDateTime,
    ) = Unit

    override fun cancel(itemId: Long) = Unit
}

class AndroidReminderScheduler(
    context: Context,
) : ReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun sync(
        items: List<LifeItem>,
        settings: NotificationSettings,
        now: LocalDateTime,
    ) {
        val scheduledIds = mutableSetOf<Long>()
        val requests = items.mapNotNull { item ->
            item.nextReminderRequest(settings, now)?.also { scheduledIds.add(item.id) }
        }
        items.forEach { item ->
            if (item.id !in scheduledIds) cancel(item.id)
        }
        requests.forEach { request -> schedule(request) }
    }

    override fun cancel(itemId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            itemId.requestCode(),
            Intent(appContext, DailyLifeReminderReceiver::class.java).apply {
                action = ActionShowReminder
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
        val missedPending = PendingIntent.getBroadcast(
            appContext,
            itemId.missedCheckRequestCode(),
            Intent(appContext, DailyLifeReminderReceiver::class.java).apply {
                action = ActionCheckMissed
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        missedPending?.let { alarmManager.cancel(it) }
        ReminderWorker.cancelWorkReminder(appContext, itemId)
    }

    fun schedule(request: ReminderScheduleRequest) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            request.itemId.requestCode(),
            request.toIntent(appContext),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerMillis = request.triggerAt.toEpochMillis()

        if (request.windowMinutes > 0) {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                max(request.windowMinutes.minutesToMillis(), MinimumWindowMillis),
                pendingIntent,
            )
        } else {
            if (canUseExactAlarms()) {
                runCatching {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent,
                    )
                }.getOrElse {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent,
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent,
                )
            }
        }

        if (!canUseExactAlarms()) {
            ReminderWorker.scheduleWorkReminder(appContext, request)
        } else {
            ReminderWorker.cancelWorkReminder(appContext, request.itemId)
        }
    }

    fun scheduleMissedCheck(request: ReminderScheduleRequest) {
        val gracePeriodMinutes = request.gracePeriodMinutes.coerceAtLeast(1)
        val triggerAt = request.dueAt.plusMinutes(gracePeriodMinutes.toLong())
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            request.itemId.missedCheckRequestCode(),
            Intent(appContext, DailyLifeReminderReceiver::class.java).apply {
                action = ActionCheckMissed
                putExtra(ExtraItemId, request.itemId)
                putExtra(ExtraDueAt, request.dueAt.toString())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toEpochMillis(),
            pendingIntent,
        )
    }

    fun cancelMissedCheck(itemId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            itemId.missedCheckRequestCode(),
            Intent(appContext, DailyLifeReminderReceiver::class.java).apply {
                action = ActionCheckMissed
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }

    private fun canUseExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }
}

class DailyLifeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val pendingResult = goAsync()
                rescheduleStoredRemindersAsync(context, pendingResult)
            }
            ActionShowReminder -> showReminder(context, intent)
            ActionSnoozeReminder -> snoozeReminder(context, intent)
            ActionMarkComplete -> markComplete(context, intent)
            ActionDismiss -> dismissReminder(context, intent)
            ActionCheckMissed -> checkMissedReminder(context, intent)
            "com.raulshma.dailylife.action.SHOW_REMINDER_WORKER" -> showReminder(context, intent)
        }
    }

    private fun showReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        val dueAt = intent.getStringExtra(ExtraDueAt)
            ?.let { raw -> runCatching { LocalDateTime.parse(raw) }.getOrNull() }
            ?: LocalDateTime.now()
        createReminderChannel(context)

        val respectDoNotDisturb = intent.getBooleanExtra(ExtraRespectDoNotDisturb, true)
        if (respectDoNotDisturb && isDoNotDisturbActive(context)) {
            AndroidReminderScheduler(context).schedule(
                ReminderScheduleRequest(
                    itemId = itemId,
                    title = intent.getStringExtra(ExtraTitle).orEmpty().ifBlank { "DailyLife reminder" },
                    body = intent.getStringExtra(ExtraBody).orEmpty(),
                    triggerAt = dueAt.plusMinutes(15),
                    dueAt = dueAt.plusMinutes(15),
                    windowMinutes = 0,
                    snoozeMinutes = intent.getIntExtra(ExtraSnoozeMinutes, DefaultSnoozeMinutes)
                        .coerceAtLeast(1),
                    batchNotifications = intent.getBooleanExtra(ExtraBatchNotifications, false),
                    respectDoNotDisturb = respectDoNotDisturb,
                    gracePeriodMinutes = intent.getIntExtra(ExtraGracePeriodMinutes, DefaultGracePeriodMinutes),
                    notificationSoundUri = intent.getStringExtra(ExtraNotificationSoundUri),
                    vibrationEnabled = intent.getBooleanExtra(ExtraVibrationEnabled, true),
                ),
            )
            return
        }

        if (canPostNotifications(context)) {
            val title = intent.getStringExtra(ExtraTitle).orEmpty().ifBlank { "DailyLife reminder" }
            val body = intent.getStringExtra(ExtraBody).orEmpty()
            val snoozeMinutes = intent.getIntExtra(ExtraSnoozeMinutes, DefaultSnoozeMinutes)
                .coerceAtLeast(1)
            val batchNotifications = intent.getBooleanExtra(ExtraBatchNotifications, false)
            val soundUri = intent.getStringExtra(ExtraNotificationSoundUri)
            val vibrationEnabled = intent.getBooleanExtra(ExtraVibrationEnabled, true)
            val dueText = dueAt.format(ReminderTimeFormatter)
            val contentText = body.ifBlank { "Due $dueText" }
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    action = IntentActionViewDetail
                    putExtra(ExtraItemId, itemId)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val completeIntent = Intent(context, DailyLifeReminderReceiver::class.java).apply {
                action = ActionMarkComplete
                putExtra(ExtraItemId, itemId)
                putExtra(ExtraDueAt, dueAt.toString())
            }
            val completePendingIntent = PendingIntent.getBroadcast(
                context,
                itemId.completeRequestCode(),
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val snoozeIntent = Intent(context, DailyLifeReminderReceiver::class.java).apply {
                action = ActionSnoozeReminder
                putExtra(ExtraItemId, itemId)
                putExtra(ExtraTitle, title)
                putExtra(ExtraBody, body)
                putExtra(ExtraSnoozeMinutes, snoozeMinutes)
                putExtra(ExtraBatchNotifications, batchNotifications)
                putExtra(ExtraRespectDoNotDisturb, respectDoNotDisturb)
                putExtra(ExtraGracePeriodMinutes, intent.getIntExtra(ExtraGracePeriodMinutes, DefaultGracePeriodMinutes))
                putExtra(ExtraNotificationSoundUri, soundUri)
                putExtra(ExtraVibrationEnabled, vibrationEnabled)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                itemId.snoozeRequestCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val dismissIntent = Intent(context, DailyLifeReminderReceiver::class.java).apply {
                action = ActionDismiss
                putExtra(ExtraItemId, itemId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                itemId.dismissRequestCode(),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notificationBuilder = NotificationCompat.Builder(context, ReminderChannelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Complete",
                    completePendingIntent,
                )
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Snooze",
                    snoozePendingIntent,
                )
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Dismiss",
                    dismissPendingIntent,
                )
                .apply {
                    if (batchNotifications) {
                        setGroup(ReminderGroupKey)
                    }
                }

            if (!soundUri.isNullOrBlank()) {
                notificationBuilder.setSound(android.net.Uri.parse(soundUri))
            }

            if (vibrationEnabled) {
                notificationBuilder.setVibrate(longArrayOf(0L, 300L, 200L, 300L))
            } else {
                notificationBuilder.setVibrate(longArrayOf(0L))
            }

            val notification = notificationBuilder.build()
            NotificationManagerCompat.from(context).notify(itemId.toNotificationId(), notification)

            if (batchNotifications) {
                postBatchSummary(context, contentIntent)
            }
        }

        val gracePeriod = intent.getIntExtra(ExtraGracePeriodMinutes, DefaultGracePeriodMinutes)
        if (gracePeriod > 0) {
            AndroidReminderScheduler(context).scheduleMissedCheck(
                ReminderScheduleRequest(
                    itemId = itemId,
                    title = "",
                    body = "",
                    triggerAt = dueAt,
                    dueAt = dueAt,
                    windowMinutes = 0,
                    snoozeMinutes = 0,
                    batchNotifications = false,
                    respectDoNotDisturb = false,
                    gracePeriodMinutes = gracePeriod,
                ),
            )
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            rescheduleStoredReminders(context, dueAt.plusSeconds(1))
        }
    }

    private fun postBatchSummary(context: Context, contentIntent: PendingIntent) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val activeNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.filter { it.groupKey.contains(ReminderGroupKey) }
        } else {
            emptyList()
        }
        val count = activeNotifications.size.coerceAtLeast(1)
        val titles = activeNotifications.take(5).map { it.notification.extras.getString(NotificationCompat.EXTRA_TITLE).orEmpty() }
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$count DailyLife reminders")
        titles.forEach { inboxStyle.addLine(it) }
        if (activeNotifications.size > 5) {
            inboxStyle.addLine("+${activeNotifications.size - 5} more")
        }

        val summary = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DailyLife reminders")
            .setContentText("$count reminders")
            .setStyle(inboxStyle)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(ReminderGroupKey)
            .setGroupSummary(true)
            .build()

        NotificationManagerCompat.from(context).notify(ReminderSummaryId, summary)
    }

    private fun snoozeReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        NotificationManagerCompat.from(context).cancel(itemId.toNotificationId())
        AndroidReminderScheduler(context).cancelMissedCheck(itemId)

        val snoozeMinutes = intent.getIntExtra(ExtraSnoozeMinutes, DefaultSnoozeMinutes)
            .coerceAtLeast(1)
        val triggerAt = LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())
        AndroidReminderScheduler(context).schedule(
            ReminderScheduleRequest(
                itemId = itemId,
                title = intent.getStringExtra(ExtraTitle).orEmpty().ifBlank { "DailyLife reminder" },
                body = intent.getStringExtra(ExtraBody).orEmpty(),
                triggerAt = triggerAt,
                dueAt = triggerAt,
                windowMinutes = 0,
                snoozeMinutes = snoozeMinutes,
                batchNotifications = intent.getBooleanExtra(ExtraBatchNotifications, false),
                respectDoNotDisturb = intent.getBooleanExtra(ExtraRespectDoNotDisturb, true),
                gracePeriodMinutes = intent.getIntExtra(ExtraGracePeriodMinutes, DefaultGracePeriodMinutes),
                notificationSoundUri = intent.getStringExtra(ExtraNotificationSoundUri),
                vibrationEnabled = intent.getBooleanExtra(ExtraVibrationEnabled, true),
            ),
        )
    }

    private fun markComplete(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        NotificationManagerCompat.from(context).cancel(itemId.toNotificationId())
        AndroidReminderScheduler(context).cancelMissedCheck(itemId)

        val dueAt = parseDueAt(intent)
        recordCompletionAndResync(context, itemId, dueAt, missed = false)
    }

    private fun dismissReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        NotificationManagerCompat.from(context).cancel(itemId.toNotificationId())
        AndroidReminderScheduler(context).cancelMissedCheck(itemId)
    }

    private fun checkMissedReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val isStillActive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.any { it.id == itemId.toNotificationId() }
        } else {
            false
        }

        if (!isStillActive) return

        NotificationManagerCompat.from(context).cancel(itemId.toNotificationId())

        val dueAt = parseDueAt(intent)
        recordCompletionAndResync(context, itemId, dueAt, missed = true)
    }

    private fun recordCompletionAndResync(
        context: Context,
        itemId: Long,
        dueAt: LocalDateTime,
        missed: Boolean,
    ) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                val database = openDatabase(context)
                try {
                    val dao = database.dailyLifeDao()
                    val itemWithCompletions = dao.getItemById(itemId) ?: return@runCatching
                    val item = itemWithCompletions.toLifeItem()
                    val settingsEntity = dao.getNotificationSettings()
                    val settings = settingsEntity?.toNotificationSettings() ?: return@runCatching

                    val record = CompletionRecord(
                        itemId = itemId,
                        occurrenceDate = dueAt.toLocalDate(),
                        completedAt = LocalDateTime.now(),
                        missed = missed,
                    )
                    val updatedItem = item.copy(
                        completionHistory = item.completionHistory + record,
                    )
                    dao.insertItem(updatedItem.toEntity())
                    dao.insertCompletionRecords(listOf(record.toEntity()))

                    AndroidReminderScheduler(context).cancel(itemId)
                    val request = updatedItem.nextReminderRequest(settings, LocalDateTime.now())
                    if (request != null) {
                        AndroidReminderScheduler(context).schedule(request)
                    }
                } finally {
                    database.close()
                }
            }
        }
    }

    private fun parseDueAt(intent: Intent): LocalDateTime =
        intent.getStringExtra(ExtraDueAt)
            ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            ?: LocalDateTime.now()
}

private fun ReminderScheduleRequest.toIntent(context: Context): Intent =
    Intent(context, DailyLifeReminderReceiver::class.java).apply {
        action = ActionShowReminder
        putExtra(ExtraItemId, itemId)
        putExtra(ExtraTitle, title)
        putExtra(ExtraBody, body)
        putExtra(ExtraDueAt, dueAt.toString())
        putExtra(ExtraSnoozeMinutes, snoozeMinutes)
        putExtra(ExtraBatchNotifications, batchNotifications)
        putExtra(ExtraRespectDoNotDisturb, respectDoNotDisturb)
        putExtra(ExtraGracePeriodMinutes, gracePeriodMinutes)
        putExtra(ExtraNotificationSoundUri, notificationSoundUri)
        putExtra(ExtraVibrationEnabled, vibrationEnabled)
    }

private fun rescheduleStoredRemindersAsync(
    context: Context,
    pendingResult: BroadcastReceiver.PendingResult,
    now: LocalDateTime = LocalDateTime.now(),
) {
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
            rescheduleStoredReminders(context, now)
        } finally {
            pendingResult.finish()
        }
    }
}

private suspend fun rescheduleStoredReminders(
    context: Context,
    now: LocalDateTime = LocalDateTime.now(),
) {
    runCatching {
        val database = openDatabase(context)
        try {
            val snapshot = RoomDailyLifeStore(database).load() ?: return
            AndroidReminderScheduler(context).sync(
                items = snapshot.items,
                settings = snapshot.notificationSettings,
                now = now,
            )
        } finally {
            database.close()
        }
    }
}

private suspend fun openDatabase(context: Context): DailyLifeDatabase {
    val passphrase = DatabasePassphraseManager(context.applicationContext).getPassphrase()
    val openHelperFactory = SupportOpenHelperFactory(passphrase, null, false)
    return Room.databaseBuilder(
        context.applicationContext,
        DailyLifeDatabase::class.java,
        "dailylife.db",
    )
        .openHelperFactory(openHelperFactory)
        .addMigrations(*ALL_MIGRATIONS.toTypedArray())
        .build()
}

private fun createReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(
        ReminderChannelId,
        "DailyLife reminders",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "Reminders and snooze alerts for DailyLife items"
        setBypassDnd(false)
        enableVibration(true)
        vibrationPattern = longArrayOf(0L, 300L, 200L, 300L)
    }
    notificationManager.createNotificationChannel(channel)
}

private fun isDoNotDisturbActive(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    return notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
}

private fun canPostNotifications(context: Context): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Int.minutesToMillis(): Long = this * 60_000L

private fun Long.requestCode(): Int =
    (this % Int.MAX_VALUE).toInt()

private fun Long.snoozeRequestCode(): Int =
    requestCode() xor SnoozeRequestMask

private fun Long.missedCheckRequestCode(): Int =
    requestCode() xor MissedCheckMask

private fun Long.completeRequestCode(): Int =
    requestCode() xor CompleteActionMask

private fun Long.dismissRequestCode(): Int =
    requestCode() xor DismissActionMask

private fun Long.toNotificationId(): Int =
    requestCode().coerceAtLeast(1)

private val ReminderTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

private const val ActionShowReminder = "com.raulshma.dailylife.action.SHOW_REMINDER"
private const val ActionSnoozeReminder = "com.raulshma.dailylife.action.SNOOZE_REMINDER"
private const val ActionMarkComplete = "com.raulshma.dailylife.action.MARK_COMPLETE"
private const val ActionDismiss = "com.raulshma.dailylife.action.DISMISS"
private const val ActionCheckMissed = "com.raulshma.dailylife.action.CHECK_MISSED"
internal const val IntentActionViewDetail = "com.raulshma.dailylife.action.VIEW_DETAIL"
internal const val ExtraItemId = "extra_item_id"
internal const val ExtraTitle = "extra_title"
internal const val ExtraBody = "extra_body"
internal const val ExtraDueAt = "extra_due_at"
internal const val ExtraSnoozeMinutes = "extra_snooze_minutes"
internal const val ExtraBatchNotifications = "extra_batch_notifications"
internal const val ExtraRespectDoNotDisturb = "extra_respect_dnd"
internal const val ExtraGracePeriodMinutes = "extra_grace_period_minutes"
internal const val ExtraNotificationSoundUri = "extra_notification_sound_uri"
internal const val ExtraVibrationEnabled = "extra_vibration_enabled"
private const val ReminderChannelId = "daily_life_reminders"
private const val ReminderGroupKey = "daily_life_reminders"
private const val ReminderSummaryId = 0
private const val DefaultSnoozeMinutes = 10
private const val DefaultGracePeriodMinutes = 30
private const val MinimumWindowMillis = 10 * 60_000L
private const val SnoozeRequestMask = 0x40000000
private const val MissedCheckMask = 0x50000000
private const val CompleteActionMask = 0x60000000
private const val DismissActionMask = 0x70000000
