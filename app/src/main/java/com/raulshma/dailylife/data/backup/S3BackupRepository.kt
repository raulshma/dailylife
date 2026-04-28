package com.raulshma.dailylife.data.backup

import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.S3BackupSettings

interface S3BackupRepository {
    suspend fun validateConnection(settings: S3BackupSettings): Boolean

    suspend fun performBackup(
        snapshot: BackupSnapshot,
        mediaFilePaths: List<String>,
        settings: S3BackupSettings,
    ): BackupResult

    suspend fun listBackups(settings: S3BackupSettings): List<String>

    suspend fun restoreLatest(settings: S3BackupSettings): BackupResult

    suspend fun restoreLatestSnapshot(settings: S3BackupSettings): BackupSnapshot?
}
