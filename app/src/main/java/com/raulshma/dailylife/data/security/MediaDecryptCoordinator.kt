package com.raulshma.dailylife.data.security

import android.content.Context
import android.net.Uri
import com.raulshma.dailylife.data.media.MediaThumbnailGenerator
import com.raulshma.dailylife.data.media.PdfThumbnailGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

@Singleton
class MediaDecryptCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: MediaEncryptionManager,
    private val thumbnailGenerator: MediaThumbnailGenerator,
) {
    private val decryptSemaphore = Semaphore(MAX_CONCURRENT_DECRYPTIONS)
    private val thumbnailSemaphore = Semaphore(MAX_CONCURRENT_THUMBNAILS)

    private val inFlightDecrypt = mutableMapOf<String, Deferred<String?>>()
    private val inFlightVideoThumb = mutableMapOf<String, Deferred<String?>>()
    private val inFlightPdfThumb = mutableMapOf<String, Deferred<String?>>()

    suspend fun decrypt(uriString: String): String? {
        if (!uriString.endsWith(".enc")) return uriString

        return withContext(Dispatchers.IO) {
            val existing = synchronized(inFlightDecrypt) { inFlightDecrypt[uriString] }
            if (existing != null) {
                return@withContext existing.await()
            }

            val deferred = coroutineScope {
                val d = async {
                    decryptSemaphore.withPermit {
                        encryptionManager.decryptToCache(Uri.parse(uriString), context)?.toString()
                    }
                }
                synchronized(inFlightDecrypt) { inFlightDecrypt[uriString] = d }
                d
            }
            try {
                deferred.await()
            } finally {
                synchronized(inFlightDecrypt) { inFlightDecrypt.remove(uriString) }
            }
        }
    }

    suspend fun generateVideoThumbnail(decryptedVideoUri: String): String? {
        return withContext(Dispatchers.IO) {
            val existing = synchronized(inFlightVideoThumb) { inFlightVideoThumb[decryptedVideoUri] }
            if (existing != null) {
                return@withContext existing.await()
            }

            val deferred = coroutineScope {
                val d = async {
                    thumbnailSemaphore.withPermit {
                        thumbnailGenerator.generateVideoThumbnail(Uri.parse(decryptedVideoUri), context)?.toString()
                    }
                }
                synchronized(inFlightVideoThumb) { inFlightVideoThumb[decryptedVideoUri] = d }
                d
            }
            try {
                deferred.await()
            } finally {
                synchronized(inFlightVideoThumb) { inFlightVideoThumb.remove(decryptedVideoUri) }
            }
        }
    }

    suspend fun generatePdfThumbnail(decryptedPdfUri: String): String? {
        return withContext(Dispatchers.IO) {
            val existing = synchronized(inFlightPdfThumb) { inFlightPdfThumb[decryptedPdfUri] }
            if (existing != null) {
                return@withContext existing.await()
            }

            val generator = PdfThumbnailGenerator(context)
            val deferred = coroutineScope {
                val d = async {
                    thumbnailSemaphore.withPermit {
                        generator.generatePdfThumbnail(Uri.parse(decryptedPdfUri), context)?.toString()
                    }
                }
                synchronized(inFlightPdfThumb) { inFlightPdfThumb[decryptedPdfUri] = d }
                d
            }
            try {
                deferred.await()
            } finally {
                synchronized(inFlightPdfThumb) { inFlightPdfThumb.remove(decryptedPdfUri) }
            }
        }
    }

    fun clearCache() {
        encryptionManager.clearDecryptedCache()
    }

    companion object {
        private const val MAX_CONCURRENT_DECRYPTIONS = 2
        private const val MAX_CONCURRENT_THUMBNAILS = 2
    }
}
