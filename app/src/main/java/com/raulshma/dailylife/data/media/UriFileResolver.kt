package com.raulshma.dailylife.data.media

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object UriFileResolver {

    fun resolveToFile(uri: Uri, context: Context): File? {
        return when (uri.scheme) {
            "file" -> uri.path?.let { File(it) }
            "content" -> {
                runCatching {
                    val projection = arrayOf(android.provider.MediaStore.MediaColumns.DATA)
                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val path = cursor.getString(0)
                            if (path != null) {
                                val file = File(path)
                                if (file.exists()) return file
                            }
                        }
                    }
                }
                val segments = uri.pathSegments
                if (segments.isNotEmpty()) {
                    val possiblePath = segments.joinToString("/")
                    val cacheFile = File(context.cacheDir, possiblePath)
                    if (cacheFile.exists()) return cacheFile

                    if (segments.first() == "cache_media") {
                        val relative = segments.drop(1).joinToString("/")
                        val mappedCacheFile = File(context.cacheDir, "media/$relative")
                        if (mappedCacheFile.exists()) return mappedCacheFile
                    }

                    val file = File(context.filesDir, possiblePath)
                    if (file.exists()) return file
                }
                copyContentUriToInternal(uri, context)
            }
            else -> null
        }
    }

    private fun copyContentUriToInternal(uri: Uri, context: Context): File? {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            val extension = mimeType?.let { mime ->
                when {
                    mime.startsWith("image/") -> mime.substringAfter("image/").let {
                        when (it) { "jpeg" -> "jpg"; "png" -> "png"; "webp" -> "webp"; "gif" -> "gif"; else -> "jpg" }
                    }
                    mime.startsWith("video/") -> mime.substringAfter("video/").let {
                        when (it) { "mp4" -> "mp4"; "webm" -> "webm"; "3gpp" -> "3gp"; else -> "mp4" }
                    }
                    mime.startsWith("audio/") -> mime.substringAfter("audio/").let {
                        when (it) { "mpeg" -> "mp3"; "mp4" -> "m4a"; "wav" -> "wav"; "ogg" -> "ogg"; else -> "m4a" }
                    }
                    else -> "bin"
                }
            } ?: "bin"

            val importDir = File(context.filesDir, "media/imported").apply { mkdirs() }
            val destFile = File(importDir, "${System.currentTimeMillis()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            } ?: return null

            if (destFile.length() > 0) destFile else { destFile.delete(); null }
        } catch (_: Exception) {
            null
        }
    }

    private const val BUFFER_SIZE = 8192
}
