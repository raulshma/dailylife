package com.raulshma.dailylife.data.security

import android.content.Context
import java.io.File

/**
 * Manages an LRU cache for decrypted media files with a configurable max size.
 * When the cache exceeds its limit, least-recently accessed files are evicted.
 */
class MediaCacheManager(context: Context) {

    private val cacheDir: File = File(context.cacheDir, "media_decrypted").apply { mkdirs() }

    /**
     * Returns a cache file for the given [key]. If it already exists, its last-modified
     * time is updated so it is considered most-recently used.
     */
    fun obtainCacheFile(key: String, extension: String = "tmp"): File {
        val safeKey = key.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(cacheDir, "${safeKey}_$extension")
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis())
        }
        return file
    }

    /**
     * Call after writing a new file to the cache to enforce the size limit.
     */
    fun enforceSizeLimit(maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES) {
        val files = cacheDir.listFiles()?.filter { it.isFile } ?: return
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= maxSizeBytes) return

        val sortedByAccess = files.sortedBy { it.lastModified() }
        for (file in sortedByAccess) {
            if (totalSize <= maxSizeBytes) break
            val length = file.length()
            if (file.delete()) {
                totalSize -= length
            }
        }
    }

    /**
     * Clears all cached files.
     */
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    companion object {
        private const val DEFAULT_MAX_SIZE_BYTES = 256L * 1024 * 1024 // 256 MB
    }
}
