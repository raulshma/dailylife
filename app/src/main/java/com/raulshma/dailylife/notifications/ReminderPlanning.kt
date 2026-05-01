package com.raulshma.dailylife.notifications

import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.WeekOfMonth
import java.time.DayOfWeek as JavaDayOfWeek
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
    val gracePeriodMinutes: Int = 30,
    val notificationSoundUri: String? = null,
    val vibrationEnabled: Boolean = true,
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

    val effectiveSoundUri = notificationSettings.notificationSoundUri
        ?: globalSettings.notificationSoundUri
    val effectiveVibration = notificationSettings.vibrationEnabled
        ?: globalSettings.vibrationEnabled

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
        gracePeriodMinutes = globalSettings.missedGracePeriodMinutes.coerceAtLeast(0),
        notificationSoundUri = effectiveSoundUri,
        vibrationEnabled = effectiveVibration,
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
    val rule = recurrenceRule

    when (rule.frequency) {
        RecurrenceFrequency.WeeklyDays -> {
            val selectedDays = rule.daysOfWeek.map { it.value }.toSet()
            if (selectedDays.isEmpty()) return null
            var cursor = maxOf(startDate, now.toLocalDate())
            while (true) {
                if (cursor.dayOfWeek in selectedDays && cursor !in completedDates) {
                    val candidate = LocalDateTime.of(cursor, effectiveTime)
                    if (candidate.isAfter(now)) return candidate
                }
                cursor = cursor.plusDays(1)
                if (cursor.isAfter(now.toLocalDate().plusYears(2))) return null
            }
        }
        RecurrenceFrequency.MonthlyDay -> {
            val dayOfMonth = startDate.dayOfMonth
            var cursor = startDate
            while (true) {
                val maxDay = cursor.lengthOfMonth()
                val effectiveDay = dayOfMonth.coerceAtMost(maxDay)
                val candidateDate = cursor.withDayOfMonth(effectiveDay)
                if (!candidateDate.isBefore(now.toLocalDate()) && candidateDate !in completedDates) {
                    val candidate = LocalDateTime.of(candidateDate, effectiveTime)
                    if (candidate.isAfter(now)) return candidate
                }
                cursor = cursor.plusMonths(rule.interval.coerceAtLeast(1).toLong())
                if (cursor.isAfter(now.toLocalDate().plusYears(2))) return null
            }
        }
        RecurrenceFrequency.MonthlyNthDay -> {
            val targetDow = rule.dayOfWeek?.value ?: startDate.dayOfWeek
            var cursor = startDate.withDayOfMonth(1)
            while (!cursor.isAfter(now.toLocalDate().plusYears(2))) {
                val candidateDate = nthDayOfWeekInMonth(cursor.year, cursor.month, rule.weekOfMonth, targetDow)
                if (candidateDate != null && !candidateDate.isBefore(now.toLocalDate()) && candidateDate !in completedDates && !candidateDate.isBefore(startDate)) {
                    val candidate = LocalDateTime.of(candidateDate, effectiveTime)
                    if (candidate.isAfter(now)) return candidate
                }
                cursor = cursor.plusMonths(rule.interval.coerceAtLeast(1).toLong())
            }
            return null
        }
        else -> {
            val stepDays = recurrenceStepDays() ?: return null
            val firstCandidate = LocalDateTime.of(startDate, effectiveTime)
            if (firstCandidate.isAfter(now) && startDate !in completedDates) return firstCandidate

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
    }
}

private fun nthDayOfWeekInMonth(year: Int, month: java.time.Month, weekOfMonth: WeekOfMonth?, targetDow: JavaDayOfWeek): LocalDate? {
    val firstOfMonth = LocalDate.of(year, month, 1)
    val lastOfMonth = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth())
    if (weekOfMonth == WeekOfMonth.Last) {
        var cursor = lastOfMonth
        while (cursor.month == month) {
            if (cursor.dayOfWeek == targetDow) return cursor
            cursor = cursor.minusDays(1)
        }
        return null
    }
    val targetOrdinal = weekOfMonth?.weekNumber ?: 1
    var cursor = firstOfMonth
    var occurrence = 0
    while (cursor.month == month) {
        if (cursor.dayOfWeek == targetDow) {
            occurrence++
            if (occurrence == targetOrdinal) return cursor
        }
        cursor = cursor.plusDays(1)
    }
    return null
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
        RecurrenceFrequency.WeeklyDays -> 1L
        RecurrenceFrequency.MonthlyDay -> 1L
        RecurrenceFrequency.MonthlyNthDay -> 1L
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

fun LifeItem.upcomingOccurrences(
    globalSettings: NotificationSettings,
    from: LocalDateTime = LocalDateTime.now(),
    count: Int = 10,
): List<LocalDateTime> {
    if (!globalSettings.globalEnabled || !notificationSettings.enabled) return emptyList()
    if (!isRecurring && taskStatus == com.raulshma.dailylife.domain.TaskStatus.Done) return emptyList()

    val results = mutableListOf<LocalDateTime>()
    var cursor = from
    repeat(count) {
        val next = nextDueAt(globalSettings, cursor) ?: return results
        results.add(next)
        cursor = next.plusMinutes(1)
    }
    return results
}
