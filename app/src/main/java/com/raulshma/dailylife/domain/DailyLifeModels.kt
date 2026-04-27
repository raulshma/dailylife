package com.raulshma.dailylife.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class LifeItemType(val label: String) {
    Thought("Thought"),
    Note("Note"),
    Task("Task"),
    Reminder("Reminder"),
    Photo("Photo"),
    Video("Video"),
    Audio("Audio"),
    Location("Location"),
    Pdf("PDF"),
    Mixed("Mixed"),
}

enum class TaskStatus(val label: String) {
    Open("Open"),
    InProgress("In progress"),
    Done("Done"),
}

enum class RecurrenceFrequency(val label: String) {
    None("None"),
    Daily("Daily"),
    Weekly("Weekly"),
    Custom("Custom"),
}

data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.None,
    val interval: Int = 1,
)

fun RecurrenceRule.stepDays(): Long {
    val safeInterval = interval.coerceAtLeast(1).toLong()
    return when (frequency) {
        RecurrenceFrequency.None -> Long.MAX_VALUE
        RecurrenceFrequency.Daily -> safeInterval
        RecurrenceFrequency.Weekly -> safeInterval * 7L
        RecurrenceFrequency.Custom -> safeInterval
    }
}

data class NotificationSettings(
    val globalEnabled: Boolean = true,
    val preferredTime: LocalTime = LocalTime.of(9, 0),
    val flexibleWindowMinutes: Int = 0,
    val defaultSnoozeMinutes: Int = 10,
    val batchNotifications: Boolean = false,
    val respectDoNotDisturb: Boolean = true,
)

data class ItemNotificationSettings(
    val enabled: Boolean = true,
    val timeOverride: LocalTime? = null,
    val flexibleWindowMinutes: Int? = null,
    val snoozeMinutes: Int? = null,
)

data class CompletionRecord(
    val itemId: Long,
    val occurrenceDate: LocalDate,
    val completedAt: LocalDateTime,
    val missed: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val batteryLevel: Int? = null,
    val appVersion: String? = null,
    val note: String? = null,
)

data class OccurrenceStats(
    val completedCount: Int,
    val missedCount: Int,
    val currentStreak: Int,
)

data class LifeItem(
    val id: Long,
    val type: LifeItemType,
    val title: String,
    val body: String,
    val createdAt: LocalDateTime,
    val tags: Set<String> = emptySet(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val taskStatus: TaskStatus? = null,
    val reminderAt: LocalDateTime? = null,
    val recurrenceRule: RecurrenceRule = RecurrenceRule(),
    val notificationSettings: ItemNotificationSettings = ItemNotificationSettings(),
    val completionHistory: List<CompletionRecord> = emptyList(),
) {
    val isRecurring: Boolean
        get() = recurrenceRule.frequency != RecurrenceFrequency.None

    fun occurrenceStats(referenceDate: LocalDate = LocalDate.now()): OccurrenceStats {
        val completedDates = completionHistory
            .filterNot { it.missed }
            .map { it.occurrenceDate }
            .toSet()
        val recordedMissedDates = completionHistory
            .filter { it.missed }
            .map { it.occurrenceDate }
            .toSet()
        val expectedDates = expectedOccurrenceDates(referenceDate)
        val computedMissedDates = expectedDates
            .filter { occurrenceDate ->
                occurrenceDate.isBefore(referenceDate) && occurrenceDate !in completedDates
            }
            .toSet()
        val missedDates = (recordedMissedDates + computedMissedDates) - completedDates

        return OccurrenceStats(
            completedCount = completedDates.size,
            missedCount = missedDates.size,
            currentStreak = expectedDates.currentStreak(
                referenceDate = referenceDate,
                completedDates = completedDates,
            ),
        )
    }

    private fun expectedOccurrenceDates(referenceDate: LocalDate): List<LocalDate> {
        if (!isRecurring) return emptyList()

        val startDate = reminderAt?.toLocalDate() ?: createdAt.toLocalDate()
        if (startDate.isAfter(referenceDate)) return emptyList()

        val stepDays = recurrenceRule.stepDays()
        val dates = mutableListOf<LocalDate>()
        var occurrenceDate = startDate
        while (!occurrenceDate.isAfter(referenceDate)) {
            dates += occurrenceDate
            occurrenceDate = occurrenceDate.plusDays(stepDays)
        }
        return dates
    }

    private fun List<LocalDate>.currentStreak(
        referenceDate: LocalDate,
        completedDates: Set<LocalDate>,
    ): Int {
        val streakStartIndex = indexOfLast { occurrenceDate ->
            occurrenceDate.isBefore(referenceDate) || occurrenceDate in completedDates
        }
        if (streakStartIndex == -1) return 0

        var streak = 0
        for (index in streakStartIndex downTo 0) {
            if (this[index] !in completedDates) break
            streak += 1
        }
        return streak
    }
}

data class LifeItemDraft(
    val type: LifeItemType = LifeItemType.Thought,
    val title: String = "",
    val body: String = "",
    val tags: Set<String> = emptySet(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val taskStatus: TaskStatus? = null,
    val reminderAt: LocalDateTime? = null,
    val recurrenceRule: RecurrenceRule = RecurrenceRule(),
    val notificationSettings: ItemNotificationSettings = ItemNotificationSettings(),
)

enum class StorageOperation {
    Load,
    Save,
}

data class StorageError(
    val operation: StorageOperation,
    val message: String,
)

private val ImageUrlPattern =
    Regex("""https?://\S+\.(?:png|jpe?g|webp|gif|bmp|avif)(?:\?\S*)?""", RegexOption.IGNORE_CASE)
private val ContentImagePattern =
    Regex("""(?:content|file)://\S+\.(?:png|jpe?g|webp|gif|bmp|avif)(?:\.enc)?""", RegexOption.IGNORE_CASE)
private val VideoUrlPattern =
    Regex("""https?://\S+\.(?:mp4|m4v|webm|mkv|mov|m3u8)(?:\?\S*)?""", RegexOption.IGNORE_CASE)
private val ContentVideoPattern =
    Regex("""(?:content|file)://\S+\.(?:mp4|m4v|webm|mkv|mov)(?:\.enc)?""", RegexOption.IGNORE_CASE)
private val AudioUrlPattern =
    Regex("""https?://\S+\.(?:mp3|aac|wav|ogg|m4a|flac)(?:\?\S*)?""", RegexOption.IGNORE_CASE)
private val ContentAudioPattern =
    Regex("""(?:content|file)://\S+\.(?:mp3|aac|wav|ogg|m4a|flac)(?:\.enc)?""", RegexOption.IGNORE_CASE)
private val PdfUrlPattern =
    Regex("""https?://\S+\.(?:pdf)(?:\?\S*)?""", RegexOption.IGNORE_CASE)
private val ContentPdfPattern =
    Regex("""(?:content|file)://\S+\.(?:pdf)(?:\.enc)?""", RegexOption.IGNORE_CASE)
private val AnyContentUriPattern =
    Regex("""(?:content|file)://\S+""")

private val AllMediaPatterns by lazy {
    listOf(
        ImageUrlPattern, ContentImagePattern,
        VideoUrlPattern, ContentVideoPattern,
        AudioUrlPattern, ContentAudioPattern,
        PdfUrlPattern, ContentPdfPattern,
        AnyContentUriPattern,
        GeoPattern,
        Regex("""https?://\S+\.(?:png|jpe?g|webp|gif|bmp|avif|mp4|m4v|webm|mkv|mov|mp3|aac|wav|ogg|m4a|flac|pdf)\S*""", RegexOption.IGNORE_CASE),
    )
}

private val GeoPattern =
    Regex("""geo:\s*[-+]?\d{1,2}(?:\.\d+)?,\s*[-+]?\d{1,3}(?:\.\d+)?""", RegexOption.IGNORE_CASE)

fun LifeItem.inferImagePreviewUrl(): String? {
    val source = listOf(title, body).joinToString(" ")
    return firstInferredUriByType(source, LifeItemType.Photo)
        ?: Regex("""https?://\S+""").find(source)?.value?.takeIf { url ->
            url.contains("picsum", ignoreCase = true) ||
                url.contains("unsplash", ignoreCase = true) ||
                url.contains("images", ignoreCase = true)
        }
        ?: if (type == LifeItemType.Photo || type == LifeItemType.Mixed) {
            firstInferredUri(source)
        } else {
            null
        }
}

fun LifeItem.inferVideoPlaybackUrl(): String? {
    val source = listOf(title, body).joinToString(" ")
    return firstInferredUriByType(source, LifeItemType.Video)
        ?: if (type == LifeItemType.Video) {
            firstInferredUri(source)
        } else {
            null
        }
}

fun LifeItem.inferAudioUrl(): String? {
    val source = listOf(title, body).joinToString(" ")
    return firstInferredUriByType(source, LifeItemType.Audio)
        ?: if (type == LifeItemType.Audio) {
            firstInferredUri(source)
        } else {
            null
        }
}

fun LifeItem.inferPdfUrl(): String? {
    val source = listOf(title, body).joinToString(" ")
    return firstInferredUriByType(source, LifeItemType.Pdf)
        ?: if (type == LifeItemType.Pdf) {
            firstInferredUri(source)
        } else {
            null
        }
}

fun LifeItem.displayBody(): String {
    var cleaned = body
    for (pattern in AllMediaPatterns) {
        cleaned = pattern.replace(cleaned, "")
    }
    return cleaned.trim()
}

data class DailyLifeFilters(
    val query: String = "",
    val selectedType: LifeItemType? = null,
    val selectedTag: String? = null,
    val dateRangeStart: LocalDate? = null,
    val dateRangeEnd: LocalDate? = null,
    val favoritesOnly: Boolean = false,
)

data class DailyLifeState(
    val items: List<LifeItem> = emptyList(),
    val filters: DailyLifeFilters = DailyLifeFilters(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val storageError: StorageError? = null,
) {
    val allTags: List<String>
        get() = items.flatMap { it.tags }.distinct().sorted()

    val visibleItems: List<LifeItem>
        get() = items
            .filter { item -> item.matches(filters) }
            .sortedWith(
                compareByDescending<LifeItem> { it.isPinned }
                    .thenByDescending { it.createdAt },
            )
}

private fun LifeItem.matches(filters: DailyLifeFilters): Boolean {
    val normalizedQuery = filters.query.trim().lowercase()
    val createdDate = createdAt.toLocalDate()
    val startDate = listOfNotNull(filters.dateRangeStart, filters.dateRangeEnd).minOrNull()
    val endDate = listOfNotNull(filters.dateRangeStart, filters.dateRangeEnd).maxOrNull()
    val matchesQuery = normalizedQuery.isEmpty() ||
        title.lowercase().contains(normalizedQuery) ||
        body.lowercase().contains(normalizedQuery) ||
        type.label.lowercase().contains(normalizedQuery) ||
        tags.any { tag -> tag.lowercase().contains(normalizedQuery) }
    val matchesDateRange = (startDate == null || !createdDate.isBefore(startDate)) &&
        (endDate == null || !createdDate.isAfter(endDate))

    return matchesQuery &&
        (filters.selectedType == null || type == filters.selectedType) &&
        (filters.selectedTag == null || tags.contains(filters.selectedTag)) &&
        matchesDateRange &&
        (!filters.favoritesOnly || isFavorite)
}
