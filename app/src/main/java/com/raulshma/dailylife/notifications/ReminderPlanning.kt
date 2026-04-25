package com.raulshma.dailylife.notifications

import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val DefaultWindowMinutes = 0

data class ReminderScheduleRequest(
    val itemId: Long,
    val title: String,
    val body: String,
    val triggerAt: LocalDateTime,
    val dueAt: LocalDateTime,
    val windowMinutes: Int,
    val snoozeMinutes: Int,
    val batchNotifications: Boolean,
    val respectDoNotDisturb: Boolean,
)

fun LifeItem.nextReminderRequest(
    globalSettings: NotificationSettings,
    now: LocalDateTime = LocalDateTime.now(),
): ReminderScheduleRequest? {
    if (!globalSettings.globalEnabled || !notificationSettings.enabled) return null

    val dueAt = nextDueAt(globalSettings, now) ?: return null
    val windowMinutes = notificationSettings.flexibleWindowMinutes
        ?: globalSettings.flexibleWindowMinutes
    val safeWindowMinutes = windowMinutes.coerceAtLeast(DefaultWindowMinutes)
    val triggerAt = if (safeWindowMinutes > 0) {
        dueAt.minusMinutes(safeWindowMinutes.toLong())
    } else {
        dueAt
    }

    return ReminderScheduleRequest(
        itemId = id,
        title = title.ifBlank { type.label },
        body = body,
        triggerAt = triggerAt.takeIf { it.isAfter(now) } ?: dueAt,
        dueAt = dueAt,
        windowMinutes = safeWindowMinutes,
        snoozeMinutes = (notificationSettings.snoozeMinutes ?: globalSettings.defaultSnoozeMinutes)
            .coerceAtLeast(1),
        batchNotifications = globalSettings.batchNotifications,
        respectDoNotDisturb = globalSettings.respectDoNotDisturb,
    )
}

private fun LifeItem.nextDueAt(
    globalSettings: NotificationSettings,
    now: LocalDateTime,
): LocalDateTime? {
    val effectiveTime = effectiveReminderTime(globalSettings)
    val completedDates = completionHistory
        .filterNot { record -> record.missed }
        .map { record -> record.occurrenceDate }
        .toSet()
    if (!isRecurring) {
        if (taskStatus == TaskStatus.Done || completedDates.isNotEmpty()) return null
        return reminderAt?.takeIf { it.isAfter(now) }
    }

    val startDate = reminderAt?.toLocalDate() ?: createdAt.toLocalDate()
    val firstCandidate = LocalDateTime.of(startDate, effectiveTime)
    if (firstCandidate.isAfter(now) && startDate !in completedDates) return firstCandidate

    val stepDays = recurrenceStepDays() ?: return null
    var candidateDate = startDate.plusDays(
        daysUntilNextCandidate(
            startDate = startDate,
            nowDate = now.toLocalDate(),
            stepDays = stepDays,
        ),
    )
    var candidate = LocalDateTime.of(candidateDate, effectiveTime)
    while (!candidate.isAfter(now) || candidateDate in completedDates) {
        candidateDate = candidateDate.plusDays(stepDays)
        candidate = LocalDateTime.of(candidateDate, effectiveTime)
    }
    return candidate
}

private fun LifeItem.effectiveReminderTime(globalSettings: NotificationSettings): LocalTime =
    notificationSettings.timeOverride
        ?: reminderAt?.toLocalTime()
        ?: globalSettings.preferredTime

private fun LifeItem.recurrenceStepDays(): Long? {
    val safeInterval = recurrenceRule.interval.coerceAtLeast(1).toLong()
    return when (recurrenceRule.frequency) {
        RecurrenceFrequency.None -> null
        RecurrenceFrequency.Daily -> safeInterval
        RecurrenceFrequency.Weekly -> safeInterval * 7L
        RecurrenceFrequency.Custom -> safeInterval
    }
}

private fun daysUntilNextCandidate(
    startDate: LocalDate,
    nowDate: LocalDate,
    stepDays: Long,
): Long {
    val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, nowDate)
    if (elapsedDays <= 0L) return 0L

    val elapsedIntervals = elapsedDays / stepDays
    return elapsedIntervals * stepDays
}
