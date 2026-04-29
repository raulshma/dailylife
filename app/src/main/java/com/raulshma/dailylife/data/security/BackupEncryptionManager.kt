package com.raulshma.dailylife.data.security

import com.raulshma.dailylife.data.security.KeystoreHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class BackupEncryptionManager {

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    fun encryptFile(inputFile: File, outputFile: File) {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        FileOutputStream(outputFile).use { fos ->
            fos.write(cipher.iv)
            FileInputStream(inputFile).use { fis ->
                val buffer = ByteArray(CHUNK_SIZE)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    val output = cipher.update(buffer, 0, read)
                    if (output != null) fos.write(output)
                }
            }
            val final = cipher.doFinal()
            if (final != null) fos.write(final)
        }
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val iv = encryptedData.copyOfRange(0, IV_LENGTH)
        val ciphertext = encryptedData.copyOfRange(IV_LENGTH, encryptedData.size)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey = KeystoreHelper.getOrCreateKey(KEY_ALIAS)

    companion object {
        private const val KEY_ALIAS = "dailylife_backup_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 16
        private const val IV_LENGTH = 12
        private const val CHUNK_SIZE = 64 * 1024
    }
}
