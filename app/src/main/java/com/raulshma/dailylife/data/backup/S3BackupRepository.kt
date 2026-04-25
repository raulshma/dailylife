package com.raulshma.dailylife.data.backup

import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.S3BackupSettings

/**
 * Contract for BYOK S3-compatible backup operations.
 */
interface S3BackupRepository {
    /**
     * Validates that the configured S3 settings can connect and access the bucket.
     */
    suspend fun validateConnection(settings: S3BackupSettings): Boolean

    /**
     * Exports a [BackupSnapshot] and referenced media files to S3.
     */
    suspend fun performBackup(
        snapshot: BackupSnapshot,
        mediaFilePaths: List<String>,
        settings: S3BackupSettings,
    ): BackupResult

    /**
     * Lists available backup snapshots on S3.
     */
    suspend fun listBackups(settings: S3BackupSettings): List<String>

    /**
     * Restores the most recent backup snapshot from S3.
     */
    suspend fun restoreLatest(settings: S3BackupSettings): BackupResult
}
