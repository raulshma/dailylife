package com.raulshma.dailylife.data.db

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages a secure passphrase for SQLCipher database encryption.
 * The passphrase is randomly generated on first access and encrypted
 * using Android Keystore for storage.
 */
class DatabasePassphraseManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPassphrase(): ByteArray {
        val encryptedPassphrase = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
            ?.let { hexToBytes(it) }
        val iv = prefs.getString(KEY_PASSPHRASE_IV, null)
            ?.let { hexToBytes(it) }

        return if (encryptedPassphrase != null && iv != null) {
            decryptPassphrase(encryptedPassphrase, iv)
        } else {
            generateAndStorePassphrase()
        }
    }

    private fun generateAndStorePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        val encrypted = cipher.doFinal(passphrase)
        prefs.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, bytesToHex(encrypted))
            .putString(KEY_PASSPHRASE_IV, bytesToHex(cipher.iv))
            .apply()
        return passphrase
    }

    private fun decryptPassphrase(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        }
        return cipher.doFinal(encrypted)
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
        private const val KEY_ALIAS = "dailylife_db_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_LENGTH = 32
    }
}
