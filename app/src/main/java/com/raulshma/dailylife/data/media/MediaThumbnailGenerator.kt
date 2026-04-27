package com.raulshma.dailylife.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.content.FileProvider
import com.raulshma.dailylife.data.security.MediaCacheManager
import java.io.File
import java.io.FileOutputStream

/**
 * Generates video thumbnails using [MediaMetadataRetriever] and caches them
 * via [MediaCacheManager]. Thumbnails are saved as JPEG files.
 */
class MediaThumbnailGenerator(context: Context) {

    private val cacheManager = MediaCacheManager(context)
    private val packageName = context.packageName

    /**
     * Returns a [Uri] pointing to a JPEG thumbnail for the video at [videoUri],
     * or null if generation fails. Generated thumbnails are cached and reused.
     */
    fun generateVideoThumbnail(videoUri: Uri, context: Context): Uri? {
        val videoFile = videoUri.toFile(context) ?: return null
        if (!videoFile.exists() || videoFile.length() == 0L) return null
        val cacheKey = "thumb_${videoFile.name}_${videoFile.length()}"
        val thumbFile = cacheManager.obtainCacheFile(cacheKey, "jpg")

        if (thumbFile.exists() && thumbFile.length() > 0) {
            return FileProvider.getUriForFile(context, "$packageName.fileprovider", thumbFile)
        }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val bitmap = retriever.frameAtTime
                ?: retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            bitmap?.let {
                FileOutputStream(thumbFile).use { fos ->
                    it.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                }
                it.recycle()
            }
            cacheManager.enforceSizeLimit(maxSizeBytes = ThumbCacheMaxSize)
            FileProvider.getUriForFile(context, "$packageName.fileprovider", thumbFile)
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun Uri.toFile(context: Context): File? {
        return when (scheme) {
            "file" -> path?.let { File(it) }
            "content" -> {
                runCatching {
                    val projection = arrayOf(android.provider.MediaStore.MediaColumns.DATA)
                    context.contentResolver.query(this, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val path = cursor.getString(0)
                            if (path != null) {
                                val file = File(path)
                                if (file.exists()) return file
                            }
                        }
                    }
                }
                val segments = pathSegments
                if (segments.isNotEmpty()) {
                    val possiblePath = segments.joinToString("/")
                    val cacheFile = File(context.cacheDir, possiblePath)
                    if (cacheFile.exists()) return cacheFile

                    val file = File(context.filesDir, possiblePath)
                    if (file.exists()) return file
                }
                copyContentUriToInternal(this, context)
            }
            else -> null
        }
    }

    private fun copyContentUriToInternal(uri: Uri, context: Context): File? {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            val extension = mimeType?.let { mime ->
                when {
                    mime.startsWith("video/") -> mime.substringAfter("video/").let {
                        when (it) { "mp4" -> "mp4"; "webm" -> "webm"; "3gpp" -> "3gp"; else -> "mp4" }
                    }
                    mime.startsWith("image/") -> mime.substringAfter("image/").let {
                        when (it) { "jpeg" -> "jpg"; "png" -> "png"; "webp" -> "webp"; "gif" -> "gif"; else -> "jpg" }
                    }
                    else -> "bin"
                }
            } ?: "bin"

            val importDir = File(context.filesDir, "media/imported").apply { mkdirs() }
            val destFile = File(importDir, "${System.currentTimeMillis()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output, 8192)
                }
            } ?: return null

            if (destFile.length() > 0) destFile else { destFile.delete(); null }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val ThumbCacheMaxSize = 64L * 1024 * 1024 // 64 MB
    }
}
