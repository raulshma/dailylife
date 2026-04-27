package com.raulshma.dailylife.data.security

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

/**
 * Encrypts and decrypts media files using AES/GCM with a key stored in Android Keystore.
 * Encrypted files are stored with a .enc extension. Temporary decrypted copies are placed
 * in a cache directory and should be cleaned up by callers when no longer needed.
 */
class MediaEncryptionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val encryptedDir: File = File(context.filesDir, "media/encrypted").apply { mkdirs() }
    private val cacheManager: MediaCacheManager = MediaCacheManager(context)

    /**
     * Encrypts a plaintext media file and returns the URI of the encrypted file.
     * The original file is deleted after successful encryption.
     */
    fun encryptFile(sourceUri: Uri, context: Context): Uri? {
        return try {
            val sourceFile = sourceUri.toFile(context) ?: return null
            val encryptedFile = File(encryptedDir, "${sourceFile.name}.enc")
            val key = getOrCreateMediaKey()
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, key)
            }
            FileInputStream(sourceFile).use { fis ->
                FileOutputStream(encryptedFile).use { fos ->
                    fos.write(cipher.iv)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cipher.update(buffer, 0, bytesRead)?.let { output ->
                            if (output.isNotEmpty()) fos.write(output)
                        }
                    }
                    cipher.doFinal()?.let { finalBytes ->
                        if (finalBytes.isNotEmpty()) fos.write(finalBytes)
                    }
                }
            }
            sourceFile.delete()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", encryptedFile)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypts an encrypted media file to a temporary cache file and returns its URI.
     * The cache enforces an LRU size limit automatically.
     */
    fun decryptToCache(encryptedUri: Uri, context: Context): Uri? {
        return try {
            val encryptedFile = encryptedUri.toFile(context) ?: return null
            if (!encryptedFile.name.endsWith(".enc")) {
                // Not encrypted; return original URI
                return encryptedUri
            }
            val decryptedName = encryptedFile.name.removeSuffix(".enc")
            val cacheKey = "${encryptedFile.name}_${encryptedFile.length()}"
            val decryptedFile = cacheManager.obtainCacheFile(cacheKey, decryptedName)

            if (decryptedFile.exists() && decryptedFile.length() > 0) {
                return FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    decryptedFile,
                )
            }

            val key = getOrCreateMediaKey()

            FileInputStream(encryptedFile).use { fis ->
                val iv = ByteArray(IV_LENGTH)
                fis.read(iv)
                val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
                }
                FileOutputStream(decryptedFile).use { fos ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cipher.update(buffer, 0, bytesRead)?.let { output ->
                            if (output.isNotEmpty()) fos.write(output)
                        }
                    }
                    cipher.doFinal()?.let { finalBytes ->
                        if (finalBytes.isNotEmpty()) fos.write(finalBytes)
                    }
                }
            }
            cacheManager.enforceSizeLimit()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", decryptedFile)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Scans [text] for local media URIs, encrypts the referenced files,
     * and returns updated text with encrypted file URIs.
     */
    fun encryptMediaInText(text: String, context: Context): String {
        val uriPattern = Regex("""(?:content|file)://[^\s\"'<>]+""")
        return uriPattern.replace(text) { matchResult ->
            val uriString = matchResult.value
            val uri = Uri.parse(uriString)
            val encryptedUri = encryptFile(uri, context)
            encryptedUri?.toString() ?: uriString
        }
    }

    /**
     * Clears all temporary decrypted files from the cache.
     */
    fun clearDecryptedCache() {
        cacheManager.clearCache()
    }

    private fun getOrCreateMediaKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setKeySize(KEY_SIZE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun Uri.toFile(context: Context): File? {
        return when (scheme) {
            "file" -> path?.let { File(it) }
            "content" -> {
                // Attempt to resolve content URI to a direct file path only if the provider supports it.
                // Modern photo picker URIs often do not expose DATA and may throw, so query failures
                // must not abort the fallback copy path.
                runCatching {
                    val projection = arrayOf(android.provider.MediaStore.MediaColumns.DATA)
                    context.contentResolver.query(this, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val path = cursor.getString(0)
                            if (path != null) {
                                val f = File(path)
                                if (f.exists()) return f
                            }
                        }
                    }
                }
                // Fallback: try to find by path segments for FileProvider URIs
                val segments = pathSegments
                if (segments.isNotEmpty()) {
                    val possiblePath = segments.joinToString("/")
                    val file = File(context.filesDir, possiblePath)
                    if (file.exists()) return file
                }
                // Last resort: copy content URI to internal storage (handles photo picker URIs, etc.)
                copyContentUriToInternal(this, context)
            }
            else -> null
        }
    }

    /**
     * Copies a content URI's data to internal storage. This is necessary for URIs from
     * the photo picker and other providers that grant only temporary read permission.
     * The file is saved with an appropriate extension based on its MIME type.
     */
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
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "dailylife_media_encryption"
        private const val KEY_ALIAS = "dailylife_media_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 16
        private const val IV_LENGTH = 12
        private const val BUFFER_SIZE = 8192
    }
}
