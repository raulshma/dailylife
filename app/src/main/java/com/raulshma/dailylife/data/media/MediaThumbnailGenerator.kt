package com.raulshma.dailylife.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
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

            val targetWidth = 320
            val targetHeight = 180

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(
                    1_000_000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    targetWidth,
                    targetHeight,
                ) ?: retriever.frameAtTime
            } else {
                retriever.frameAtTime
                    ?: retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }

            val scaled = bitmap?.let { frame ->
                if (frame.width > targetWidth) {
                    val ratio = targetWidth.toFloat() / frame.width
                    val result = Bitmap.createScaledBitmap(frame, targetWidth, (frame.height * ratio).toInt(), true)
                    if (result !== frame) frame.recycle()
                    result
                } else {
                    frame
                }
            }

            scaled?.let {
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

    private fun Uri.toFile(context: Context): File? = UriFileResolver.resolveToFile(this, context)

    companion object {
        private const val ThumbCacheMaxSize = 64L * 1024 * 1024 // 64 MB
    }
}
