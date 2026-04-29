package com.raulshma.dailylife.data

import com.raulshma.dailylife.data.db.DailyLifeDao
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.toEntity
import com.raulshma.dailylife.data.db.toLifeItem
import com.raulshma.dailylife.data.db.toNotificationSettings
import com.raulshma.dailylife.data.db.toS3BackupSettings
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.S3BackupSettings

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

        val lifeItems = itemsWithCompletions.map { it.toLifeItem() }
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
}
