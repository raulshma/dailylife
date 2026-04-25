package com.raulshma.dailylife.domain

/**
 * User-provided S3-compatible storage configuration for BYOK cloud backup.
 */
data class S3BackupSettings(
    val enabled: Boolean = false,
    val endpoint: String = "",
    val bucketName: String = "",
    val region: String = "us-east-1",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
    val pathPrefix: String = "dailylife",
    val autoBackup: Boolean = false,
    val backupFrequencyHours: Int = 24,
    val encryptBackups: Boolean = true,
)

/**
 * Represents the result of a backup or restore operation.
 */
sealed class BackupResult {
    data class Success(val itemsBackedUp: Int, val mediaFilesBackedUp: Int) : BackupResult()
    data class Failure(val reason: String) : BackupResult()
}

/**
 * Snapshot of local data for backup serialization.
 */
data class BackupSnapshot(
    val items: List<LifeItem>,
    val notificationSettings: NotificationSettings,
    val exportedAt: java.time.Instant,
    val version: Int = 1,
)
