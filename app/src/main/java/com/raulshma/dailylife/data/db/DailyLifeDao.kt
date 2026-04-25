package com.raulshma.dailylife.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DailyLifeDao {
    @Query("SELECT * FROM life_items ORDER BY createdAt DESC")
    suspend fun getAllItems(): List<LifeItemEntity>

    @Query("SELECT * FROM completion_records ORDER BY occurrenceDate DESC")
    suspend fun getAllCompletionRecords(): List<CompletionRecordEntity>

    @Query("SELECT * FROM notification_settings WHERE id = 0")
    suspend fun getNotificationSettings(): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LifeItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LifeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionRecords(records: List<CompletionRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: NotificationSettingsEntity)

    @Query("DELETE FROM life_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM completion_records")
    suspend fun deleteAllCompletionRecords()

    @Query("DELETE FROM notification_settings")
    suspend fun deleteAllNotificationSettings()

    @Transaction
    suspend fun replaceAll(
        items: List<LifeItemEntity>,
        completionRecords: List<CompletionRecordEntity>,
        settings: NotificationSettingsEntity,
    ) {
        deleteAllItems()
        deleteAllCompletionRecords()
        deleteAllNotificationSettings()
        insertItems(items)
        insertCompletionRecords(completionRecords)
        insertNotificationSettings(settings)
    }
}
