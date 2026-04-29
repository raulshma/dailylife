package com.raulshma.dailylife.data

import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Properties

data class PersistedDailyLifeState(
    val items: List<LifeItem>,
    val notificationSettings: NotificationSettings,
    val nextId: Long,
)

interface DailyLifeStore {
    suspend fun load(): PersistedDailyLifeState?

    suspend fun save(snapshot: PersistedDailyLifeState)
}

class FileDailyLifeStore(
    private val file: File,
) : DailyLifeStore {
    override suspend fun load(): PersistedDailyLifeState? {
        if (!file.exists()) return null

        val properties = Properties()
        FileInputStream(file).use { input -> properties.load(input) }
        val items = properties.readItems()
        val fallbackNextId = (items.maxOfOrNull { it.id } ?: 0L) + 1L

        return PersistedDailyLifeState(
            items = items,
            notificationSettings = properties.readNotificationSettings(),
            nextId = properties.long("nextId", fallbackNextId).coerceAtLeast(fallbackNextId),
        )
    }

    override suspend fun save(snapshot: PersistedDailyLifeState) {
        file.parentFile?.mkdirs()

        val properties = Properties().apply {
            setProperty("version", "1")
            setProperty("nextId", snapshot.nextId.toString())
            writeNotificationSettings("notifications.", snapshot.notificationSettings)
            writeItems(snapshot.items)
        }

        val tempFile = File("${file.path}.tmp")
        FileOutputStream(tempFile).use { output ->
            properties.store(output, "DailyLife local data")
        }

        runCatching {
            Files.move(tempFile.toPath(), file.toPath(), REPLACE_EXISTING, ATOMIC_MOVE)
        }.recoverCatching { error ->
            if (error is AtomicMoveNotSupportedException) {
                Files.move(tempFile.toPath(), file.toPath(), REPLACE_EXISTING)
            } else {
                throw error
            }
        }.getOrThrow()
    }
}

@Suppress("unused")
class FileBackedDailyLifeRepository(
    file: File,
) {
    private val delegate = InMemoryDailyLifeRepository(store = FileDailyLifeStore(file))
}

private fun Properties.writeItems(items: List<LifeItem>) {
    setProperty("items.count", items.size.toString())
    items.forEachIndexed { index, item ->
        val prefix = "items.$index."
        setProperty("${prefix}id", item.id.toString())
        setProperty("${prefix}type", item.type.name)
        setProperty("${prefix}title", item.title)
        setProperty("${prefix}body", item.body)
        setProperty("${prefix}createdAt", item.createdAt.toString())
        setProperty("${prefix}isFavorite", item.isFavorite.toString())
        setProperty("${prefix}isPinned", item.isPinned.toString())
        item.taskStatus?.let { setProperty("${prefix}taskStatus", it.name) }
        item.reminderAt?.let { setProperty("${prefix}reminderAt", it.toString()) }

        setProperty("${prefix}tags.count", item.tags.size.toString())
        item.tags.sorted().forEachIndexed { tagIndex, tag ->
            setProperty("${prefix}tags.$tagIndex", tag)
        }

        setProperty("${prefix}recurrence.frequency", item.recurrenceRule.frequency.name)
        setProperty("${prefix}recurrence.interval", item.recurrenceRule.interval.toString())
        writeItemNotificationSettings("${prefix}notification.", item.notificationSettings)

        setProperty("${prefix}completions.count", item.completionHistory.size.toString())
        item.completionHistory.forEachIndexed { completionIndex, record ->
            val completionPrefix = "${prefix}completions.$completionIndex."
            setProperty("${completionPrefix}itemId", record.itemId.toString())
            setProperty("${completionPrefix}occurrenceDate", record.occurrenceDate.toString())
            setProperty("${completionPrefix}completedAt", record.completedAt.toString())
            setProperty("${completionPrefix}missed", record.missed.toString())
        }
    }
}

private fun Properties.readItems(): List<LifeItem> {
    val itemCount = int("items.count", 0).coerceAtLeast(0)
    return (0 until itemCount).mapNotNull { index ->
        val prefix = "items.$index."
        val id = long("${prefix}id", -1L)
        if (id <= 0L) return@mapNotNull null

        LifeItem(
            id = id,
            type = enum("${prefix}type", LifeItemType.Thought),
            title = getProperty("${prefix}title", ""),
            body = getProperty("${prefix}body", ""),
            createdAt = localDateTime("${prefix}createdAt") ?: LocalDateTime.now(),
            tags = readTags(prefix),
            isFavorite = boolean("${prefix}isFavorite", false),
            isPinned = boolean("${prefix}isPinned", false),
            taskStatus = optionalEnum<TaskStatus>("${prefix}taskStatus"),
            reminderAt = localDateTime("${prefix}reminderAt"),
            recurrenceRule = RecurrenceRule(
                frequency = enum("${prefix}recurrence.frequency", RecurrenceFrequency.None),
                interval = int("${prefix}recurrence.interval", 1).coerceAtLeast(1),
            ),
            notificationSettings = readItemNotificationSettings("${prefix}notification."),
            completionHistory = readCompletionRecords(prefix, id),
        )
    }
}

private fun Properties.writeNotificationSettings(prefix: String, settings: NotificationSettings) {
    setProperty("${prefix}globalEnabled", settings.globalEnabled.toString())
    setProperty("${prefix}preferredTime", settings.preferredTime.toString())
    setProperty("${prefix}flexibleWindowMinutes", settings.flexibleWindowMinutes.toString())
    setProperty("${prefix}defaultSnoozeMinutes", settings.defaultSnoozeMinutes.toString())
    setProperty("${prefix}batchNotifications", settings.batchNotifications.toString())
    setProperty("${prefix}respectDoNotDisturb", settings.respectDoNotDisturb.toString())
}

private fun Properties.readNotificationSettings(): NotificationSettings {
    val defaults = NotificationSettings()
    return NotificationSettings(
        globalEnabled = boolean("notifications.globalEnabled", defaults.globalEnabled),
        preferredTime = localTime("notifications.preferredTime") ?: defaults.preferredTime,
        flexibleWindowMinutes = int(
            "notifications.flexibleWindowMinutes",
            defaults.flexibleWindowMinutes,
        ).coerceAtLeast(0),
        defaultSnoozeMinutes = int(
            "notifications.defaultSnoozeMinutes",
            defaults.defaultSnoozeMinutes,
        ).coerceAtLeast(1),
        batchNotifications = boolean("notifications.batchNotifications", defaults.batchNotifications),
        respectDoNotDisturb = boolean(
            "notifications.respectDoNotDisturb",
            defaults.respectDoNotDisturb,
        ),
    )
}

private fun Properties.writeItemNotificationSettings(
    prefix: String,
    settings: ItemNotificationSettings,
) {
    setProperty("${prefix}enabled", settings.enabled.toString())
    settings.timeOverride?.let { setProperty("${prefix}timeOverride", it.toString()) }
    settings.flexibleWindowMinutes?.let {
        setProperty("${prefix}flexibleWindowMinutes", it.toString())
    }
    settings.snoozeMinutes?.let { setProperty("${prefix}snoozeMinutes", it.toString()) }
}

private fun Properties.readItemNotificationSettings(prefix: String): ItemNotificationSettings {
    val defaults = ItemNotificationSettings()
    return ItemNotificationSettings(
        enabled = boolean("${prefix}enabled", defaults.enabled),
        timeOverride = localTime("${prefix}timeOverride"),
        flexibleWindowMinutes = getProperty("${prefix}flexibleWindowMinutes")
            ?.toIntOrNull()
            ?.coerceAtLeast(0),
        snoozeMinutes = getProperty("${prefix}snoozeMinutes")
            ?.toIntOrNull()
            ?.coerceAtLeast(1),
    )
}

private fun Properties.readTags(prefix: String): Set<String> {
    val tagCount = int("${prefix}tags.count", 0).coerceAtLeast(0)
    return (0 until tagCount)
        .mapNotNull { tagIndex -> getProperty("${prefix}tags.$tagIndex") }
        .map { tag -> tag.trim().removePrefix("#").lowercase() }
        .filter { tag -> tag.isNotEmpty() }
        .toSet()
}

private fun Properties.readCompletionRecords(prefix: String, itemId: Long): List<CompletionRecord> {
    val completionCount = int("${prefix}completions.count", 0).coerceAtLeast(0)
    return (0 until completionCount).mapNotNull { completionIndex ->
        val completionPrefix = "${prefix}completions.$completionIndex."
        val occurrenceDate = localDate("${completionPrefix}occurrenceDate") ?: return@mapNotNull null
        val completedAt = localDateTime("${completionPrefix}completedAt") ?: return@mapNotNull null

        CompletionRecord(
            itemId = long("${completionPrefix}itemId", itemId),
            occurrenceDate = occurrenceDate,
            completedAt = completedAt,
            missed = boolean("${completionPrefix}missed", false),
        )
    }
}

private fun Properties.int(key: String, default: Int): Int =
    getProperty(key)?.toIntOrNull() ?: default

private fun Properties.long(key: String, default: Long): Long =
    getProperty(key)?.toLongOrNull() ?: default

private fun Properties.boolean(key: String, default: Boolean): Boolean =
    when (getProperty(key)?.lowercase()) {
        "true" -> true
        "false" -> false
        else -> default
    }

private inline fun <reified T : Enum<T>> Properties.enum(key: String, default: T): T =
    optionalEnum(key) ?: default

private inline fun <reified T : Enum<T>> Properties.optionalEnum(key: String): T? =
    getProperty(key)?.let { raw -> runCatching { enumValueOf<T>(raw) }.getOrNull() }

private fun Properties.localDate(key: String): LocalDate? =
    getProperty(key)?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }

private fun Properties.localDateTime(key: String): LocalDateTime? =
    getProperty(key)?.let { raw -> runCatching { LocalDateTime.parse(raw) }.getOrNull() }

private fun Properties.localTime(key: String): LocalTime? =
    getProperty(key)?.let { raw -> runCatching { LocalTime.parse(raw) }.getOrNull() }
