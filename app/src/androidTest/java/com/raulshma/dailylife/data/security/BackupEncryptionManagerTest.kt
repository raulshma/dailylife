package com.raulshma.dailylife.data.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupEncryptionManagerTest {

    @Test
    fun encryptAndDecrypt_roundTripsPlaintext() {
        val manager = BackupEncryptionManager()
        val plaintext = "DailyLife backup snapshot data".toByteArray(Charsets.UTF_8)

        val encrypted = manager.encrypt(plaintext)
        assertNotEquals(plaintext.toList(), encrypted.toList())

        val decrypted = manager.decrypt(encrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun encryptProducesDifferentOutputForSamePlaintext() {
        val manager = BackupEncryptionManager()
        val plaintext = "DailyLife backup snapshot data".toByteArray(Charsets.UTF_8)

        val encrypted1 = manager.encrypt(plaintext)
        val encrypted2 = manager.encrypt(plaintext)
        assertNotEquals(encrypted1.toList(), encrypted2.toList())
    }

    @Test
    fun encryptAndDecrypt_emptyData() {
        val manager = BackupEncryptionManager()
        val plaintext = ByteArray(0)

        val encrypted = manager.encrypt(plaintext)
        val decrypted = manager.decrypt(encrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun encryptAndDecrypt_largeData() {
        val manager = BackupEncryptionManager()
        val plaintext = ByteArray(64 * 1024) { it.toByte() }

        val encrypted = manager.encrypt(plaintext)
        val decrypted = manager.decrypt(encrypted)
        assertArrayEquals(plaintext, decrypted)
    }
}
