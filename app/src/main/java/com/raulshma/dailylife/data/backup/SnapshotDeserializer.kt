package com.raulshma.dailylife.data.backup

import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object SnapshotDeserializer {
    fun deserialize(json: String): BackupSnapshot {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        val exportedAt = Instant.parse(root.getString("exportedAt"))

        val nsObj = root.getJSONObject("notificationSettings")
        val notificationSettings = NotificationSettings(
            globalEnabled = nsObj.optBoolean("globalEnabled", true),
            preferredTime = nsObj.optString("preferredTime").let {
                runCatching { LocalTime.parse(it) }.getOrDefault(LocalTime.of(9, 0))
            },
            flexibleWindowMinutes = nsObj.optInt("flexibleWindowMinutes", 0),
            defaultSnoozeMinutes = nsObj.optInt("defaultSnoozeMinutes", 10),
            batchNotifications = nsObj.optBoolean("batchNotifications", false),
            respectDoNotDisturb = nsObj.optBoolean("respectDoNotDisturb", true),
        )

        val itemsArray = root.getJSONArray("items")
        val items = mutableListOf<LifeItem>()
        for (i in 0 until itemsArray.length()) {
            items.add(parseItem(itemsArray.getJSONObject(i)))
        }

        return BackupSnapshot(
            items = items,
            notificationSettings = notificationSettings,
            exportedAt = exportedAt,
            version = version,
        )
    }

    private fun parseItem(obj: JSONObject): LifeItem {
        val tagsArray = obj.optJSONArray("tags")
        val tags = if (tagsArray != null) {
            (0 until tagsArray.length()).map { tagsArray.getString(it) }.toSet()
        } else emptySet()

        val historyArray = obj.optJSONArray("completionHistory")
        val completionHistory = if (historyArray != null) {
            (0 until historyArray.length()).map { parseCompletionRecord(historyArray.getJSONObject(it)) }
        } else emptyList()

        return LifeItem(
            id = obj.getLong("id"),
            type = LifeItemType.entries.firstOrNull { it.name == obj.optString("type", "Thought") }
                ?: LifeItemType.Thought,
            title = obj.optString("title", ""),
            body = obj.optString("body", ""),
            createdAt = obj.optString("createdAt").let {
                runCatching { LocalDateTime.parse(it) }.getOrDefault(LocalDateTime.now())
            },
            tags = tags,
            isFavorite = obj.optBoolean("isFavorite", false),
            isPinned = obj.optBoolean("isPinned", false),
            taskStatus = obj.optString("taskStatus", null)?.let {
                runCatching { TaskStatus.valueOf(it) }.getOrNull()
            },
            reminderAt = obj.optString("reminderAt", null)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            },
            recurrenceRule = RecurrenceRule(
                frequency = RecurrenceFrequency.entries.firstOrNull {
                    it.name == obj.optString("recurrenceFrequency", "None")
                } ?: RecurrenceFrequency.None,
                interval = obj.optInt("recurrenceInterval", 1).coerceAtLeast(1),
            ),
            notificationSettings = ItemNotificationSettings(
                enabled = obj.optBoolean("notificationEnabled", true),
                timeOverride = obj.optString("notificationTimeOverride", null)?.let {
                    runCatching { LocalTime.parse(it) }.getOrNull()
                },
            ),
            completionHistory = completionHistory,
        )
    }

    private fun parseCompletionRecord(obj: JSONObject): CompletionRecord {
        return CompletionRecord(
            itemId = obj.optLong("itemId", 0),
            occurrenceDate = obj.optString("occurrenceDate").let {
                runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.now())
            },
            completedAt = obj.optString("completedAt").let {
                runCatching { LocalDateTime.parse(it) }.getOrDefault(LocalDateTime.now())
            },
            missed = obj.optBoolean("missed", false),
        )
    }
}
