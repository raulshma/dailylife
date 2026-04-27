package com.raulshma.dailylife.data.db

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DatabasePassphraseManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var cachedPassphrase: ByteArray? = null

    fun getPassphrase(): ByteArray {
        cachedPassphrase?.let { return it }

        val encryptedPassphrase = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
            ?.let { hexToBytes(it) }
        val iv = prefs.getString(KEY_PASSPHRASE_IV, null)
            ?.let { hexToBytes(it) }

        if (encryptedPassphrase != null && iv != null) {
            try {
                val passphrase = decryptWithKeystore(encryptedPassphrase, iv)
                ensureBackupExists(passphrase)
                return passphrase.also { cachedPassphrase = it }
            } catch (_: Exception) {
                // Keystore key lost (e.g. app reinstalled), try backup
            }
        }

        val backupEncrypted = prefs.getString(KEY_BACKUP_ENCRYPTED, null)
            ?.let { hexToBytes(it) }
        val backupIv = prefs.getString(KEY_BACKUP_IV, null)
            ?.let { hexToBytes(it) }

        if (backupEncrypted != null && backupIv != null) {
            try {
                val passphrase = decryptWithBackup(backupEncrypted, backupIv)
                storePassphrase(passphrase)
                return passphrase.also { cachedPassphrase = it }
            } catch (_: Exception) {
                // Backup also failed
            }
        }

        return generateAndStorePassphrase().also { cachedPassphrase = it }
    }

    fun regeneratePassphrase(): ByteArray {
        prefs.edit()
            .remove(KEY_ENCRYPTED_PASSPHRASE)
            .remove(KEY_PASSPHRASE_IV)
            .remove(KEY_BACKUP_ENCRYPTED)
            .remove(KEY_BACKUP_IV)
            .apply()
        cachedPassphrase = null
        return generateAndStorePassphrase().also { cachedPassphrase = it }
    }

    private fun generateAndStorePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        storePassphrase(passphrase)
        return passphrase
    }    private fun storePassphrase(passphrase: ByteArray) {
        val keystoreKey = getOrCreateKeystoreKey()
        val keystoreCipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, keystoreKey)
        }
        val keystoreEncrypted = keystoreCipher.doFinal(passphrase)

        val backupKey = deriveBackupKey()
        val backupCipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, backupKey)
        }
        val backupEncrypted = backupCipher.doFinal(passphrase)

        val didPersist = prefs.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, bytesToHex(keystoreEncrypted))
            .putString(KEY_PASSPHRASE_IV, bytesToHex(keystoreCipher.iv))
            .putString(KEY_BACKUP_ENCRYPTED, bytesToHex(backupEncrypted))
            .putString(KEY_BACKUP_IV, bytesToHex(backupCipher.iv))
            .commit()

        if (!didPersist) {
            throw IllegalStateException("Failed to persist database passphrase")
        }
    }

    private fun ensureBackupExists(passphrase: ByteArray) {
        if (prefs.contains(KEY_BACKUP_ENCRYPTED)) return

        val backupKey = deriveBackupKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, backupKey)
        }
        val backupEncrypted = cipher.doFinal(passphrase)
        val didPersist = prefs.edit()
            .putString(KEY_BACKUP_ENCRYPTED, bytesToHex(backupEncrypted))
            .putString(KEY_BACKUP_IV, bytesToHex(cipher.iv))
            .commit()

        if (!didPersist) {
            throw IllegalStateException("Failed to persist backup database passphrase")
        }
    }

    private fun decryptWithKeystore(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        }
        return cipher.doFinal(encrypted)
    }

    private fun decryptWithBackup(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = deriveBackupKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        }
        return cipher.doFinal(encrypted)
    }

    private fun deriveBackupKey(): SecretKey {
        val secret = getDeviceFingerprint()
        val spec = PBEKeySpec(secret.toCharArray(), BACKUP_SALT, BACKUP_ITERATIONS, KEY_SIZE)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, KEY_ALGORITHM)
    }

    private fun getDeviceFingerprint(): String {
        val existing = prefs.getString(KEY_DEVICE_FINGERPRINT, null)
        if (existing != null) return existing

        val fingerprint = ByteArray(32).apply { java.security.SecureRandom().nextBytes(this) }
            .joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_DEVICE_FINGERPRINT, fingerprint).commit()
        return fingerprint
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
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

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    companion object {
        private const val PREFS_NAME = "dailylife_db_passphrase"
        private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
        private const val KEY_PASSPHRASE_IV = "passphrase_iv"
        private const val KEY_BACKUP_ENCRYPTED = "backup_encrypted"
        private const val KEY_BACKUP_IV = "backup_iv"
        private const val KEY_DEVICE_FINGERPRINT = "device_fingerprint"
        private const val KEY_ALIAS = "dailylife_db_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALGORITHM = "AES"
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_LENGTH = 32
        private val BACKUP_SALT = byteArrayOf(
            0x44, 0x61, 0x69, 0x6c, 0x79, 0x4c, 0x69, 0x66,
            0x65, 0x53, 0x61, 0x6c, 0x74, 0x56, 0x31, 0x00,
        )
        private const val BACKUP_ITERATIONS = 50000
    }
}
