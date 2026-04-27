package com.raulshma.dailylife.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeystoreHelper {

    fun getOrCreateKey(
        alias: String,
        keyAlgorithm: String = KeyProperties.KEY_ALGORITHM_AES,
        blockMode: String = KeyProperties.BLOCK_MODE_GCM,
        padding: String = KeyProperties.ENCRYPTION_PADDING_NONE,
        keySize: Int = 256,
    ): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(keyAlgorithm, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(blockMode)
            .setEncryptionPaddings(padding)
            .setKeySize(keySize)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
}
