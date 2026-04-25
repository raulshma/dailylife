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
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.raulshma.dailylife.MainActivity
import com.raulshma.dailylife.R
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

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
        val requests = items.mapNotNull { item -> item.nextReminderRequest(settings, now) }
        items.forEach { item -> cancel(item.id) }
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
        ) ?: return
        alarmManager.cancel(pendingIntent)
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
    }

    private fun canUseExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }
}

class DailyLifeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> rescheduleStoredReminders(context)
            ActionShowReminder -> showReminder(context, intent)
            ActionSnoozeReminder -> snoozeReminder(context, intent)
        }
    }

    private fun showReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        val dueAt = intent.getStringExtra(ExtraDueAt)
            ?.let { raw -> runCatching { LocalDateTime.parse(raw) }.getOrNull() }
            ?: LocalDateTime.now()
        createReminderChannel(context)

        if (canPostNotifications(context)) {
            val title = intent.getStringExtra(ExtraTitle).orEmpty().ifBlank { "DailyLife reminder" }
            val body = intent.getStringExtra(ExtraBody).orEmpty()
            val snoozeMinutes = intent.getIntExtra(ExtraSnoozeMinutes, DefaultSnoozeMinutes)
                .coerceAtLeast(1)
            val batchNotifications = intent.getBooleanExtra(ExtraBatchNotifications, false)
            val dueText = dueAt.format(ReminderTimeFormatter)
            val contentText = body.ifBlank { "Due $dueText" }
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val snoozeIntent = Intent(context, DailyLifeReminderReceiver::class.java).apply {
                action = ActionSnoozeReminder
                putExtra(ExtraItemId, itemId)
                putExtra(ExtraTitle, title)
                putExtra(ExtraBody, body)
                putExtra(ExtraSnoozeMinutes, snoozeMinutes)
                putExtra(ExtraBatchNotifications, batchNotifications)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                itemId.snoozeRequestCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, ReminderChannelId)
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
                    "Snooze",
                    snoozePendingIntent,
                )
                .apply {
                    if (batchNotifications) {
                        setGroup(ReminderGroupKey)
                    }
                }
                .build()

            NotificationManagerCompat.from(context).notify(itemId.toNotificationId(), notification)
        }

        rescheduleStoredReminders(context, dueAt.plusSeconds(1))
    }

    private fun snoozeReminder(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(ExtraItemId, -1L)
        if (itemId <= 0L) return

        NotificationManagerCompat.from(context).cancel(itemId.toNotificationId())

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
            ),
        )
    }
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
    }

private fun rescheduleStoredReminders(
    context: Context,
    now: LocalDateTime = LocalDateTime.now(),
) {
    runCatching {
        val database = Room.databaseBuilder(
            context.applicationContext,
            DailyLifeDatabase::class.java,
            "dailylife.db",
        ).build()
        val snapshot = RoomDailyLifeStore(database).load() ?: return
        AndroidReminderScheduler(context).sync(
            items = snapshot.items,
            settings = snapshot.notificationSettings,
            now = now,
        )
    }
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
    }
    notificationManager.createNotificationChannel(channel)
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

private fun Long.toNotificationId(): Int =
    requestCode().coerceAtLeast(1)

private val ReminderTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

private const val ActionShowReminder = "com.raulshma.dailylife.action.SHOW_REMINDER"
private const val ActionSnoozeReminder = "com.raulshma.dailylife.action.SNOOZE_REMINDER"
private const val ExtraItemId = "extra_item_id"
private const val ExtraTitle = "extra_title"
private const val ExtraBody = "extra_body"
private const val ExtraDueAt = "extra_due_at"
private const val ExtraSnoozeMinutes = "extra_snooze_minutes"
private const val ExtraBatchNotifications = "extra_batch_notifications"
private const val ReminderChannelId = "daily_life_reminders"
private const val ReminderGroupKey = "daily_life_reminders"
private const val DefaultSnoozeMinutes = 10
private const val MinimumWindowMillis = 10 * 60_000L
private const val SnoozeRequestMask = 0x40000000
