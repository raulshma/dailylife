package com.raulshma.dailylife.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        LifeItemEntity::class,
        CompletionRecordEntity::class,
        NotificationSettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class DailyLifeDatabase : RoomDatabase() {
    abstract fun dailyLifeDao(): DailyLifeDao
}
