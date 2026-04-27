package com.raulshma.dailylife.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS s3_backup_settings (
                id INTEGER PRIMARY KEY NOT NULL DEFAULT 0,
                enabled INTEGER NOT NULL DEFAULT 0,
                endpoint TEXT NOT NULL DEFAULT '',
                bucketName TEXT NOT NULL DEFAULT '',
                region TEXT NOT NULL DEFAULT 'us-east-1',
                accessKeyId TEXT NOT NULL DEFAULT '',
                secretAccessKey TEXT NOT NULL DEFAULT '',
                pathPrefix TEXT NOT NULL DEFAULT 'dailylife',
                autoBackup INTEGER NOT NULL DEFAULT 0,
                backupFrequencyHours INTEGER NOT NULL DEFAULT 24,
                encryptBackups INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE completion_records ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE completion_records ADD COLUMN longitude REAL")
        db.execSQL("ALTER TABLE completion_records ADD COLUMN batteryLevel INTEGER")
        db.execSQL("ALTER TABLE completion_records ADD COLUMN appVersion TEXT")
        db.execSQL("ALTER TABLE completion_records ADD COLUMN note TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_life_items_createdAt ON life_items(createdAt)")
        db.execSQL(
            """
            CREATE TABLE completion_records_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                itemId INTEGER NOT NULL,
                occurrenceDate TEXT NOT NULL,
                completedAt TEXT NOT NULL,
                missed INTEGER NOT NULL,
                latitude REAL,
                longitude REAL,
                batteryLevel INTEGER,
                appVersion TEXT,
                note TEXT,
                FOREIGN KEY(itemId) REFERENCES life_items(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("INSERT INTO completion_records_new SELECT * FROM completion_records")
        db.execSQL("DROP TABLE completion_records")
        db.execSQL("ALTER TABLE completion_records_new RENAME TO completion_records")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_completion_records_itemId ON completion_records(itemId)")
    }
}

val ALL_MIGRATIONS = listOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
