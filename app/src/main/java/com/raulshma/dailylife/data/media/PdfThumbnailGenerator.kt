package com.raulshma.dailylife.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.raulshma.dailylife.data.security.MediaCacheManager
import java.io.File
import java.io.FileOutputStream

/**
 * Generates PDF page thumbnails using [PdfRenderer] and caches them
 * via [MediaCacheManager]. Thumbnails are saved as JPEG files.
 */
class PdfThumbnailGenerator(context: Context) {

    private val cacheManager = MediaCacheManager(context)
    private val packageName = context.packageName

    /**
     * Returns a [Uri] pointing to a JPEG thumbnail of the first page of the PDF at [pdfUri],
     * or null if generation fails. Generated thumbnails are cached and reused.
     */
    fun generatePdfThumbnail(pdfUri: Uri, context: Context): Uri? {
        val pdfFile = pdfUri.toFile(context)
        val pfd = if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 0L) {
            runCatching {
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }.getOrNull()
        } else {
            runCatching {
                context.contentResolver.openFileDescriptor(pdfUri, "r")
            }.getOrNull()
        }
        if (pfd == null) return null

        val statSize = runCatching { pfd.statSize }.getOrNull() ?: 0L
        val cacheKey = if (pdfFile != null) {
            "pdf_thumb_${pdfFile.name}_${statSize}"
        } else {
            "pdf_thumb_${pdfUri.hashCode()}_${statSize}"
        }
        val thumbFile = cacheManager.obtainCacheFile(cacheKey, "jpg")

        if (thumbFile.exists() && thumbFile.length() > 0) {
            runCatching { pfd.close() }
            return FileProvider.getUriForFile(context, "$packageName.fileprovider", thumbFile)
        }

        return try {
            val renderer = PdfRenderer(pfd)
            val page = renderer.openPage(0)

            val targetWidth = 320
            val targetHeight = (page.height.toFloat() / page.width * targetWidth).toInt()
            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val matrix = Matrix()
            matrix.postScale(
                targetWidth.toFloat() / page.width,
                targetHeight.toFloat() / page.height,
            )
            canvas.concat(matrix)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            FileOutputStream(thumbFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }

            bitmap.recycle()

            cacheManager.enforceSizeLimit(maxSizeBytes = ThumbCacheMaxSize)
            FileProvider.getUriForFile(context, "$packageName.fileprovider", thumbFile)
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { pfd.close() }
        }
    }

    private fun Uri.toFile(context: Context): File? = UriFileResolver.resolveToFile(this, context)

    companion object {
        private const val ThumbCacheMaxSize = 64L * 1024 * 1024 // 64 MB
    }
}
