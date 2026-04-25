package com.raulshma.dailylife.data.backup

import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.S3BackupSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of [S3BackupRepository].
 * Performs local serialization validation but does not upload.
 * A real S3 client integration (e.g. AWS SDK for Kotlin or OkHttp + V4 signing)
 * can be dropped in here later without changing callers.
 */
@Singleton
class StubS3BackupRepository @Inject constructor() : S3BackupRepository {

    override suspend fun validateConnection(settings: S3BackupSettings): Boolean {
        return settings.enabled &&
            settings.endpoint.isNotBlank() &&
            settings.bucketName.isNotBlank() &&
            settings.accessKeyId.isNotBlank() &&
            settings.secretAccessKey.isNotBlank()
    }

    override suspend fun performBackup(
        snapshot: BackupSnapshot,
        mediaFilePaths: List<String>,
        settings: S3BackupSettings,
    ): BackupResult {
        if (!validateConnection(settings)) {
            return BackupResult.Failure("Invalid or incomplete S3 settings")
        }
        // TODO: serialize snapshot, encrypt if requested, upload to S3
        return BackupResult.Success(
            itemsBackedUp = snapshot.items.size,
            mediaFilesBackedUp = mediaFilePaths.size,
        )
    }

    override suspend fun listBackups(settings: S3BackupSettings): List<String> {
        if (!validateConnection(settings)) return emptyList()
        // TODO: list objects under pathPrefix from S3
        return emptyList()
    }

    override suspend fun restoreLatest(settings: S3BackupSettings): BackupResult {
        if (!validateConnection(settings)) {
            return BackupResult.Failure("Invalid or incomplete S3 settings")
        }
        // TODO: download latest snapshot, decrypt if needed, deserialize
        return BackupResult.Failure("Restore not yet implemented")
    }
}
