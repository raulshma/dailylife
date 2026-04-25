package com.raulshma.dailylife.data.backup

import com.raulshma.dailylife.data.security.BackupEncryptionManager
import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.BackupSnapshot
import com.raulshma.dailylife.domain.S3BackupSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * S3-compatible backup repository using OkHttp with AWS Signature Version 4.
 */
@Singleton
class OkHttpS3BackupRepository @Inject constructor(
    private val encryptionManager: BackupEncryptionManager,
) : S3BackupRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun validateConnection(settings: S3BackupSettings): Boolean {
        if (!hasRequiredSettings(settings)) return false
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = buildBaseUrl(settings)
                val request = Request.Builder()
                    .url("$url?location=")
                    .head()
                    .build()
                val signedRequest = signerFor(settings).sign(request)
                client.newCall(signedRequest).execute().use { response ->
                    response.isSuccessful
                }
            }.getOrDefault(false)
        }
    }

    override suspend fun performBackup(
        snapshot: BackupSnapshot,
        mediaFilePaths: List<String>,
        settings: S3BackupSettings,
    ): BackupResult {
        if (!hasRequiredSettings(settings)) {
            return BackupResult.Failure("Invalid or incomplete S3 settings")
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                val baseUrl = buildBaseUrl(settings)
                val prefix = settings.pathPrefix.trim('/')
                val snapshotKey = "$prefix/snapshots/snapshot_${snapshot.exportedAt.toEpochMilli()}.json"
                val snapshotJson = serializeSnapshot(snapshot)
                val snapshotBytes = if (settings.encryptBackups) {
                    encryptionManager.encrypt(snapshotJson.toByteArray(Charsets.UTF_8))
                } else {
                    snapshotJson.toByteArray(Charsets.UTF_8)
                }

                val uploadResult = uploadObject(baseUrl, snapshotKey, snapshotBytes, settings)
                if (!uploadResult) {
                    return@runCatching BackupResult.Failure("Failed to upload snapshot")
                }

                var mediaUploaded = 0
                for (path in mediaFilePaths) {
                    val fileName = path.substringAfterLast('/')
                    val mediaKey = "$prefix/media/$fileName"
                    val fileBytes = java.io.File(path.replace("file://", "")).readBytes()
                    val bytesToUpload = if (settings.encryptBackups) {
                        encryptionManager.encrypt(fileBytes)
                    } else {
                        fileBytes
                    }
                    if (uploadObject(baseUrl, mediaKey, bytesToUpload, settings)) {
                        mediaUploaded++
                    }
                }

                BackupResult.Success(
                    itemsBackedUp = snapshot.items.size,
                    mediaFilesBackedUp = mediaUploaded,
                )
            }.getOrElse { error ->
                BackupResult.Failure(error.message ?: "Unknown backup error")
            }
        }
    }

    override suspend fun listBackups(settings: S3BackupSettings): List<String> {
        if (!hasRequiredSettings(settings)) return emptyList()
        return withContext(Dispatchers.IO) {
            runCatching {
                val baseUrl = buildBaseUrl(settings)
                val prefix = settings.pathPrefix.trim('/')
                val url = "$baseUrl?list-type=2&prefix=${prefix}/snapshots/"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                val signedRequest = signerFor(settings).sign(request)
                client.newCall(signedRequest).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList()
                    val xml = response.body?.string() ?: return@use emptyList()
                    parseObjectKeys(xml)
                }
            }.getOrDefault(emptyList())
        }
    }

    override suspend fun restoreLatest(settings: S3BackupSettings): BackupResult {
        if (!hasRequiredSettings(settings)) {
            return BackupResult.Failure("Invalid or incomplete S3 settings")
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                val keys = listBackups(settings)
                val latestKey = keys.maxOrNull()
                    ?: return@runCatching BackupResult.Failure("No backups found")
                val baseUrl = buildBaseUrl(settings)
                val request = Request.Builder()
                    .url("$baseUrl/$latestKey")
                    .get()
                    .build()
                val signedRequest = signerFor(settings).sign(request)
                client.newCall(signedRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@runCatching BackupResult.Failure("Download failed: ${response.code}")
                    }
                    val bytes = response.body?.bytes()
                        ?: return@runCatching BackupResult.Failure("Empty response")
                    val decryptedBytes = if (settings.encryptBackups) {
                        runCatching { encryptionManager.decrypt(bytes) }.getOrElse {
                            return@runCatching BackupResult.Failure("Decryption failed: ${it.message}")
                        }
                    } else {
                        bytes
                    }
                    val json = String(decryptedBytes, Charsets.UTF_8)
                    // TODO: deserialize snapshot and merge into local store
                    BackupResult.Success(itemsBackedUp = 0, mediaFilesBackedUp = 0)
                }
            }.getOrElse { error ->
                BackupResult.Failure(error.message ?: "Unknown restore error")
            }
        }
    }

    private fun uploadObject(baseUrl: String, key: String, data: ByteArray, settings: S3BackupSettings): Boolean {
        val url = "$baseUrl/$key"
        val body = data.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()
        val signedRequest = signerFor(settings).sign(request, data)
        return client.newCall(signedRequest).execute().use { it.isSuccessful }
    }

    private fun buildBaseUrl(settings: S3BackupSettings): String {
        val endpoint = settings.endpoint.trimEnd('/')
        return if (endpoint.contains("://")) {
            "$endpoint/${settings.bucketName}"
        } else {
            "https://$endpoint/${settings.bucketName}"
        }
    }

    private fun signerFor(settings: S3BackupSettings): AwsV4Signer {
        return AwsV4Signer(
            accessKeyId = settings.accessKeyId,
            secretAccessKey = settings.secretAccessKey,
            region = settings.region,
        )
    }

    private fun hasRequiredSettings(settings: S3BackupSettings): Boolean {
        return settings.enabled &&
            settings.endpoint.isNotBlank() &&
            settings.bucketName.isNotBlank() &&
            settings.accessKeyId.isNotBlank() &&
            settings.secretAccessKey.isNotBlank()
    }

    private fun serializeSnapshot(snapshot: BackupSnapshot): String {
        // Minimal JSON serialization for the snapshot
        val itemsJson = snapshot.items.joinToString(",\n") { item ->
            """
            {
              "id": ${item.id},
              "type": "${item.type.name}",
              "title": ${org.json.JSONObject.quote(item.title)},
              "body": ${org.json.JSONObject.quote(item.body)},
              "createdAt": "${item.createdAt}",
              "tags": [${item.tags.joinToString(",") { org.json.JSONObject.quote(it) }}],
              "isFavorite": ${item.isFavorite},
              "isPinned": ${item.isPinned},
              "taskStatus": ${item.taskStatus?.name?.let { "\"$it\"" } ?: "null"},
              "reminderAt": ${item.reminderAt?.let { "\"$it\"" } ?: "null"},
              "recurrenceFrequency": "${item.recurrenceRule.frequency.name}",
              "recurrenceInterval": ${item.recurrenceRule.interval},
              "notificationEnabled": ${item.notificationSettings.enabled},
              "notificationTimeOverride": ${item.notificationSettings.timeOverride?.let { "\"$it\"" } ?: "null"},
              "completionHistory": [${item.completionHistory.joinToString(",") { record ->
                """
                {"itemId": ${record.itemId}, "occurrenceDate": "${record.occurrenceDate}", "completedAt": "${record.completedAt}", "missed": ${record.missed}}
                """.trimIndent()
              }}]
            }
            """.trimIndent()
        }
        return """
        {
          "version": ${snapshot.version},
          "exportedAt": "${snapshot.exportedAt}",
          "notificationSettings": {
            "globalEnabled": ${snapshot.notificationSettings.globalEnabled},
            "preferredTime": "${snapshot.notificationSettings.preferredTime}",
            "flexibleWindowMinutes": ${snapshot.notificationSettings.flexibleWindowMinutes},
            "defaultSnoozeMinutes": ${snapshot.notificationSettings.defaultSnoozeMinutes},
            "batchNotifications": ${snapshot.notificationSettings.batchNotifications},
            "respectDoNotDisturb": ${snapshot.notificationSettings.respectDoNotDisturb}
          },
          "items": [$itemsJson]
        }
        """.trimIndent()
    }

    private fun parseObjectKeys(xml: String): List<String> {
        val keys = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "Key") {
                parser.next()
                if (parser.eventType == XmlPullParser.TEXT) {
                    keys.add(parser.text)
                }
            }
            eventType = parser.next()
        }
        return keys
    }
}
