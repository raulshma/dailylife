package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "s3_backup_settings")
data class S3BackupSettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val enabled: Boolean,
    val endpoint: String,
    val bucketName: String,
    val region: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val pathPrefix: String,
    val autoBackup: Boolean,
    val backupFrequencyHours: Int,
    val encryptBackups: Boolean,
)
