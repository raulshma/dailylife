package com.raulshma.dailylife.data

import com.raulshma.dailylife.data.db.CompletionRecordEntity
import com.raulshma.dailylife.data.db.DailyLifeDao
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.LifeItemEntity
import com.raulshma.dailylife.data.db.LifeItemWithCompletions
import com.raulshma.dailylife.data.db.NotificationSettingsEntity
import com.raulshma.dailylife.data.db.S3BackupSettingsEntity
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.S3BackupSettings
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.DayOfWeek as RecurrenceDayOfWeek
import com.raulshma.dailylife.domain.GeofenceTrigger
import com.raulshma.dailylife.domain.WeekOfMonth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RoomDailyLifeStore(
    database: DailyLifeDatabase,
) : DailyLifeStore {
    private val dao = database.dailyLifeDao()

    override suspend fun load(): PersistedDailyLifeState? {
        val itemsWithCompletions = dao.getItemsWithCompletions()
        val settingsEntity = dao.getNotificationSettings()

        if (itemsWithCompletions.isEmpty() && settingsEntity == null) {
            return null
        }

        val lifeItems = itemsWithCompletions.map { it.item.toLifeItem(it.completions) }

        val fallbackNextId = (lifeItems.maxOfOrNull { it.id } ?: 0L) + 1L

        return PersistedDailyLifeState(
            items = lifeItems,
            notificationSettings = settingsEntity?.toNotificationSettings() ?: NotificationSettings(),
            nextId = fallbackNextId,
        )
    }

    override suspend fun save(snapshot: PersistedDailyLifeState) {
        val itemEntities = snapshot.items.map { it.toEntity() }
        val completionEntities = snapshot.items.flatMap { item ->
            item.completionHistory.map { it.toEntity() }
        }
        val settingsEntity = snapshot.notificationSettings.toEntity()

        dao.replaceAll(itemEntities, completionEntities, settingsEntity)
    }

    suspend fun loadS3BackupSettings(): S3BackupSettings {
        return dao.getS3BackupSettings()?.toS3BackupSettings() ?: S3BackupSettings()
    }

    suspend fun saveS3BackupSettings(settings: S3BackupSettings) {
        dao.insertS3BackupSettings(settings.toEntity())
    }

    private fun LifeItemEntity.toLifeItem(
        completionEntities: List<CompletionRecordEntity>,
    ): LifeItem = LifeItem(
        id = id,
        type = LifeItemType.entries.firstOrNull { it.name == type } ?: LifeItemType.Thought,
        title = title,
        body = body,
        createdAt = runCatching { LocalDateTime.parse(createdAt) }.getOrNull() ?: LocalDateTime.now(),
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        isFavorite = isFavorite,
        isPinned = isPinned,
        taskStatus = taskStatus?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() },
        reminderAt = reminderAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
        recurrenceRule = RecurrenceRule(
            frequency = RecurrenceFrequency.entries.firstOrNull { it.name == recurrenceFrequency }
                ?: RecurrenceFrequency.None,
            interval = recurrenceInterval.coerceAtLeast(1),
            daysOfWeek = recurrenceDaysOfWeek.split(",").mapNotNull { s ->
                RecurrenceDayOfWeek.entries.firstOrNull { it.name.equals(s, ignoreCase = true) }
            }.toSet(),
            dayOfWeek = recurrenceDayOfWeek?.let {
                RecurrenceDayOfWeek.entries.firstOrNull { d -> d.name.equals(it, ignoreCase = true) }
            },
            weekOfMonth = recurrenceWeekOfMonth?.let {
                WeekOfMonth.entries.firstOrNull { w -> w.name.equals(it, ignoreCase = true) }
            },
        ),
        notificationSettings = ItemNotificationSettings(
            enabled = notificationEnabled,
            timeOverride = notificationTimeOverride?.let {
                runCatching { LocalTime.parse(it) }.getOrNull()
            },
            flexibleWindowMinutes = notificationFlexibleWindow?.coerceAtLeast(0),
            snoozeMinutes = notificationSnoozeMinutes?.coerceAtLeast(1),
            geofenceLatitude = geofenceLatitude,
            geofenceLongitude = geofenceLongitude,
            geofenceRadiusMeters = geofenceRadiusMeters,
            geofenceTrigger = GeofenceTrigger.entries.firstOrNull { it.name == geofenceTrigger }
                ?: GeofenceTrigger.Arrival,
        ),
        completionHistory = completionEntities.map { it.toCompletionRecord() },
        isArchived = isArchived,
    )

    private fun CompletionRecordEntity.toCompletionRecord(): CompletionRecord = CompletionRecord(
        itemId = itemId,
        occurrenceDate = runCatching { LocalDate.parse(occurrenceDate) }.getOrNull()
            ?: LocalDate.now(),
        completedAt = runCatching { LocalDateTime.parse(completedAt) }.getOrNull()
            ?: LocalDateTime.now(),
        missed = missed,
        latitude = latitude,
        longitude = longitude,
        batteryLevel = batteryLevel,
        appVersion = appVersion,
        note = note,
    )

    private fun LifeItem.toEntity(): LifeItemEntity = LifeItemEntity(
        id = id,
        type = type.name,
        title = title,
        body = body,
        createdAt = createdAt.toString(),
        tags = tags.sorted().joinToString(","),
        isFavorite = isFavorite,
        isPinned = isPinned,
        taskStatus = taskStatus?.name,
        reminderAt = reminderAt?.toString(),
        recurrenceFrequency = recurrenceRule.frequency.name,
        recurrenceInterval = recurrenceRule.interval.coerceAtLeast(1),
        recurrenceDaysOfWeek = recurrenceRule.daysOfWeek.joinToString(",") { it.name },
        recurrenceDayOfWeek = recurrenceRule.dayOfWeek?.name,
        recurrenceWeekOfMonth = recurrenceRule.weekOfMonth?.name,
        notificationEnabled = notificationSettings.enabled,
        notificationTimeOverride = notificationSettings.timeOverride?.toString(),
        notificationFlexibleWindow = notificationSettings.flexibleWindowMinutes,
        notificationSnoozeMinutes = notificationSettings.snoozeMinutes,
        geofenceLatitude = notificationSettings.geofenceLatitude,
        geofenceLongitude = notificationSettings.geofenceLongitude,
        geofenceRadiusMeters = notificationSettings.geofenceRadiusMeters,
        geofenceTrigger = notificationSettings.geofenceTrigger.name,
        isArchived = isArchived,
    )

    private fun CompletionRecord.toEntity(): CompletionRecordEntity = CompletionRecordEntity(
        itemId = itemId,
        occurrenceDate = occurrenceDate.toString(),
        completedAt = completedAt.toString(),
        missed = missed,
        latitude = latitude,
        longitude = longitude,
        batteryLevel = batteryLevel,
        appVersion = appVersion,
        note = note,
    )

    private fun NotificationSettings.toEntity(): NotificationSettingsEntity =
        NotificationSettingsEntity(
            id = 0,
            globalEnabled = globalEnabled,
            preferredTime = preferredTime.toString(),
            flexibleWindowMinutes = flexibleWindowMinutes.coerceAtLeast(0),
            defaultSnoozeMinutes = defaultSnoozeMinutes.coerceAtLeast(1),
            batchNotifications = batchNotifications,
            respectDoNotDisturb = respectDoNotDisturb,
        )

    private fun NotificationSettingsEntity.toNotificationSettings(): NotificationSettings =
        NotificationSettings(
            globalEnabled = globalEnabled,
            preferredTime = runCatching { LocalTime.parse(preferredTime) }.getOrNull()
                ?: LocalTime.of(9, 0),
            flexibleWindowMinutes = flexibleWindowMinutes.coerceAtLeast(0),
            defaultSnoozeMinutes = defaultSnoozeMinutes.coerceAtLeast(1),
            batchNotifications = batchNotifications,
            respectDoNotDisturb = respectDoNotDisturb,
        )

    private fun S3BackupSettings.toEntity(): S3BackupSettingsEntity = S3BackupSettingsEntity(
        id = 0,
        enabled = enabled,
        endpoint = endpoint,
        bucketName = bucketName,
        region = region,
        accessKeyId = accessKeyId,
        secretAccessKey = secretAccessKey,
        pathPrefix = pathPrefix,
        autoBackup = autoBackup,
        backupFrequencyHours = backupFrequencyHours.coerceAtLeast(1),
        encryptBackups = encryptBackups,
    )

    private fun S3BackupSettingsEntity.toS3BackupSettings(): S3BackupSettings = S3BackupSettings(
        enabled = enabled,
        endpoint = endpoint,
        bucketName = bucketName,
        region = region,
        accessKeyId = accessKeyId,
        secretAccessKey = secretAccessKey,
        pathPrefix = pathPrefix,
        autoBackup = autoBackup,
        backupFrequencyHours = backupFrequencyHours.coerceAtLeast(1),
        encryptBackups = encryptBackups,
    )
}
