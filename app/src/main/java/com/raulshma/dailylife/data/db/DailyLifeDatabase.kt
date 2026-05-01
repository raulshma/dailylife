package com.raulshma.dailylife.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        LifeItemEntity::class,
        CompletionRecordEntity::class,
        NotificationSettingsEntity::class,
        S3BackupSettingsEntity::class,
        AIConversationEntity::class,
        AIChatMessageEntity::class,
        AIMetricsEntity::class,
        AIEnrichmentTaskEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class DailyLifeDatabase : RoomDatabase() {
    abstract fun dailyLifeDao(): DailyLifeDao
    abstract fun aiConversationDao(): AIConversationDao
}
