package com.raulshma.dailylife.data.security

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.security.keystore.KeyProperties
import androidx.core.content.FileProvider
import com.raulshma.dailylife.data.media.UriFileResolver
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
            if (uriString.endsWith(".enc", ignoreCase = true)) {
                return@replace uriString
            }
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

    private fun getOrCreateMediaKey(): SecretKey =
        KeystoreHelper.getOrCreateKey(KEY_ALIAS)

    private fun Uri.toFile(context: Context): File? = UriFileResolver.resolveToFile(this, context)

    companion object {
        private const val PREFS_NAME = "dailylife_media_encryption"
        private const val KEY_ALIAS = "dailylife_media_key"
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
