package com.raulshma.dailylife.data

import com.raulshma.dailylife.data.db.CompletionRecordEntity
import com.raulshma.dailylife.data.db.DailyLifeDao
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.LifeItemEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RoomDailyLifeStore(
    database: DailyLifeDatabase,
) : DailyLifeStore {
    private val dao = database.dailyLifeDao()

    override fun load(): PersistedDailyLifeState? = runBlocking(Dispatchers.IO) {
        val items = dao.getAllItems()
        val completionRecords = dao.getAllCompletionRecords()
        val settingsEntity = dao.getNotificationSettings()

        if (items.isEmpty() && settingsEntity == null) {
            return@runBlocking null
        }

        val completionsByItemId = completionRecords.groupBy { it.itemId }
        val lifeItems = items.map { entity ->
            entity.toLifeItem(completionsByItemId[entity.id].orEmpty())
        }

        val fallbackNextId = (lifeItems.maxOfOrNull { it.id } ?: 0L) + 1L

        PersistedDailyLifeState(
            items = lifeItems,
            notificationSettings = settingsEntity?.toNotificationSettings() ?: NotificationSettings(),
            nextId = fallbackNextId,
        )
    }

    override fun save(snapshot: PersistedDailyLifeState) = runBlocking(Dispatchers.IO) {
        val itemEntities = snapshot.items.map { it.toEntity() }
        val completionEntities = snapshot.items.flatMap { item ->
            item.completionHistory.map { it.toEntity() }
        }
        val settingsEntity = snapshot.notificationSettings.toEntity()

        dao.replaceAll(itemEntities, completionEntities, settingsEntity)
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
        ),
        notificationSettings = ItemNotificationSettings(
            enabled = notificationEnabled,
            timeOverride = notificationTimeOverride?.let {
                runCatching { LocalTime.parse(it) }.getOrNull()
            },
            flexibleWindowMinutes = notificationFlexibleWindow?.coerceAtLeast(0),
            snoozeMinutes = notificationSnoozeMinutes?.coerceAtLeast(1),
        ),
        completionHistory = completionEntities.map { it.toCompletionRecord() },
    )

    private fun CompletionRecordEntity.toCompletionRecord(): CompletionRecord = CompletionRecord(
        itemId = itemId,
        occurrenceDate = runCatching { LocalDate.parse(occurrenceDate) }.getOrNull()
            ?: LocalDate.now(),
        completedAt = runCatching { LocalDateTime.parse(completedAt) }.getOrNull()
            ?: LocalDateTime.now(),
        missed = missed,
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
        notificationEnabled = notificationSettings.enabled,
        notificationTimeOverride = notificationSettings.timeOverride?.toString(),
        notificationFlexibleWindow = notificationSettings.flexibleWindowMinutes,
        notificationSnoozeMinutes = notificationSettings.snoozeMinutes,
    )

    private fun CompletionRecord.toEntity(): CompletionRecordEntity = CompletionRecordEntity(
        itemId = itemId,
        occurrenceDate = occurrenceDate.toString(),
        completedAt = completedAt.toString(),
        missed = missed,
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

    fun loadS3BackupSettings(): S3BackupSettings = runBlocking(Dispatchers.IO) {
        dao.getS3BackupSettings()?.toS3BackupSettings() ?: S3BackupSettings()
    }

    fun saveS3BackupSettings(settings: S3BackupSettings) = runBlocking(Dispatchers.IO) {
        dao.insertS3BackupSettings(settings.toEntity())
    }

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
