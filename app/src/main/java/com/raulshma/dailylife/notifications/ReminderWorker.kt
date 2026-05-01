package com.raulshma.dailylife.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val itemId = inputData.getLong(KeyItemId, -1L)
        if (itemId <= 0L) return Result.failure()

        val intent = android.content.Intent(applicationContext, DailyLifeReminderReceiver::class.java).apply {
            action = ActionShowReminderWorker
            putExtra(ExtraItemId, itemId)
            putExtra(ExtraTitle, inputData.getString(KeyTitle).orEmpty())
            putExtra(ExtraBody, inputData.getString(KeyBody).orEmpty())
            putExtra(ExtraDueAt, inputData.getString(KeyDueAt).orEmpty())
            putExtra(ExtraSnoozeMinutes, inputData.getInt(KeySnoozeMinutes, 10))
            putExtra(ExtraBatchNotifications, inputData.getBoolean(KeyBatchNotifications, false))
            putExtra(ExtraRespectDoNotDisturb, inputData.getBoolean(KeyRespectDnd, true))
            putExtra(ExtraGracePeriodMinutes, inputData.getInt(KeyGracePeriodMinutes, 30))
            putExtra(ExtraNotificationSoundUri, inputData.getString(KeySoundUri))
            putExtra(ExtraVibrationEnabled, inputData.getBoolean(KeyVibrationEnabled, true))
        }
        applicationContext.sendBroadcast(intent)
        return Result.success()
    }

    companion object {
        private const val KeyItemId = "worker_item_id"
        private const val KeyTitle = "worker_title"
        private const val KeyBody = "worker_body"
        private const val KeyDueAt = "worker_due_at"
        private const val KeySnoozeMinutes = "worker_snooze_minutes"
        private const val KeyBatchNotifications = "worker_batch"
        private const val KeyRespectDnd = "worker_respect_dnd"
        private const val KeyGracePeriodMinutes = "worker_grace_period"
        private const val KeySoundUri = "worker_sound_uri"
        private const val KeyVibrationEnabled = "worker_vibration"

        fun scheduleWorkReminder(
            context: Context,
            request: ReminderScheduleRequest,
        ) {
            val now = LocalDateTime.now()
            val delay = Duration.between(now, request.triggerAt)
            if (delay.isNegative || delay.isZero) return

            val tag = "reminder_${request.itemId}"
            WorkManager.getInstance(context).cancelAllWorkByTag(tag)

            val data = Data.Builder()
                .putLong(KeyItemId, request.itemId)
                .putString(KeyTitle, request.title)
                .putString(KeyBody, request.body)
                .putString(KeyDueAt, request.dueAt.toString())
                .putInt(KeySnoozeMinutes, request.snoozeMinutes)
                .putBoolean(KeyBatchNotifications, request.batchNotifications)
                .putBoolean(KeyRespectDnd, request.respectDoNotDisturb)
                .putInt(KeyGracePeriodMinutes, request.gracePeriodMinutes)
                .putString(KeySoundUri, request.notificationSoundUri)
                .putBoolean(KeyVibrationEnabled, request.vibrationEnabled)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(data)
                .setInitialDelay(delay)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun cancelWorkReminder(context: Context, itemId: Long) {
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$itemId")
        }
    }
}

private const val ActionShowReminderWorker = "com.raulshma.dailylife.action.SHOW_REMINDER_WORKER"
