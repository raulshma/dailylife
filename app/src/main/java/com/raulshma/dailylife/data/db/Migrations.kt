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
