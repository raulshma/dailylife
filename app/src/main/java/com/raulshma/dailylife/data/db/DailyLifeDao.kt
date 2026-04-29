package com.raulshma.dailylife.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

data class LifeItemWithCompletions(
    @Embedded val item: LifeItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId",
    )
    val completions: List<CompletionRecordEntity>,
)

@Dao
interface DailyLifeDao {
    @Transaction
    @Query("""
        SELECT * FROM life_items
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        AND (:type IS NULL OR type = :type)
        AND (:typesCsv IS NULL OR ',' || :typesCsv || ',' LIKE '%,' || type || ',%')
        AND (:tag IS NULL OR ',' || tags || ',' LIKE '%,' || :tag || ',%')
        AND (:start IS NULL OR createdAt >= :start)
        AND (:end IS NULL OR createdAt <= :end)
        AND (:favoritesOnly = 0 OR isFavorite = 1)
        AND (:showArchived = 1 OR isArchived = 0)
        AND (:itemIdsCsv IS NULL OR ',' || :itemIdsCsv || ',' LIKE '%,' || CAST(id AS TEXT) || ',%')
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun filteredPagingSource(
        query: String,
        type: String?,
        typesCsv: String?,
        tag: String?,
        start: String?,
        end: String?,
        favoritesOnly: Boolean,
        showArchived: Boolean,
        itemIdsCsv: String?,
    ): PagingSource<Int, LifeItemWithCompletions>

    @Transaction
    @Query("SELECT * FROM life_items WHERE id = :id")
    suspend fun getItemById(id: Long): LifeItemWithCompletions?

    @Transaction
    @Query("SELECT * FROM life_items ORDER BY createdAt DESC")
    suspend fun getItemsWithCompletions(): List<LifeItemWithCompletions>

    @Transaction
    @Query("SELECT * FROM life_items ORDER BY createdAt DESC")
    suspend fun getAllItemsWithCompletions(): List<LifeItemWithCompletions>

    @Transaction
    @Query("SELECT * FROM life_items WHERE recurrenceFrequency != 'None'")
    suspend fun getRecurringItemsWithCompletions(): List<LifeItemWithCompletions>

    @Transaction
    @Query("SELECT * FROM life_items WHERE tags != '' ORDER BY createdAt DESC LIMIT 500")
    fun taggedItemsForGraph(): Flow<List<LifeItemWithCompletions>>

    @Query("SELECT id FROM life_items ORDER BY isPinned DESC, createdAt DESC")
    fun allItemIds(): Flow<List<Long>>

    @Query("SELECT tags FROM life_items WHERE isArchived = 0")
    fun allTagStrings(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM life_items")
    fun itemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM completion_records WHERE missed = 0")
    fun completedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM life_items WHERE isFavorite = 1")
    fun favoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM life_items WHERE type = 'Video'")
    fun videoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM life_items WHERE type = 'Pdf' OR (type != 'Pdf' AND body LIKE '%.pdf%')")
    fun pdfCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM life_items WHERE type = 'Location' OR (type != 'Location' AND (body LIKE '%geo:%' OR body LIKE '%latitude%'))")
    fun placeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM life_items WHERE type IN ('Note', 'Thought', 'Task', 'Reminder')")
    fun notesCount(): Flow<Int>

    @Query("SELECT * FROM life_items ORDER BY createdAt DESC")
    suspend fun getAllItems(): List<LifeItemEntity>

    @Query("SELECT * FROM completion_records ORDER BY occurrenceDate DESC")
    suspend fun getAllCompletionRecords(): List<CompletionRecordEntity>

    @Query("SELECT * FROM notification_settings WHERE id = 0")
    suspend fun getNotificationSettings(): NotificationSettingsEntity?

    @Query("SELECT * FROM notification_settings WHERE id = 0")
    fun notificationSettingsFlow(): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM s3_backup_settings WHERE id = 0")
    suspend fun getS3BackupSettings(): S3BackupSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LifeItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LifeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionRecords(records: List<CompletionRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: NotificationSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertS3BackupSettings(settings: S3BackupSettingsEntity)

    @Query("DELETE FROM life_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM completion_records")
    suspend fun deleteAllCompletionRecords()

    @Query("DELETE FROM notification_settings")
    suspend fun deleteAllNotificationSettings()

    @Query("DELETE FROM s3_backup_settings")
    suspend fun deleteAllS3BackupSettings()

    @Query("DELETE FROM life_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("DELETE FROM completion_records WHERE itemId = :itemId")
    suspend fun deleteCompletionRecordsByItemId(itemId: Long)

    @Query("DELETE FROM completion_records WHERE itemId = :itemId AND occurrenceDate = :occurrenceDate AND completedAt = :completedAt")
    suspend fun deleteCompletionRecordByKey(itemId: Long, occurrenceDate: String, completedAt: String)

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

    @Transaction
    suspend fun deleteItemCascade(itemId: Long) {
        deleteCompletionRecordsByItemId(itemId)
        deleteItemById(itemId)
    }
}
