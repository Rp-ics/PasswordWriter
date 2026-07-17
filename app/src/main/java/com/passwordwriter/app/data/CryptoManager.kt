package com.passwordwriter.app.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager {

    private val algorithm = "AES/GCM/NoPadding"
    private val keyLength = 256
    private val iterationCount = 100_000
    private val gcmTagLength = 128

    fun encrypt(plaintext: String, masterPassword: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(masterPassword, salt)
        return encryptWithKey(plaintext, key, salt)
    }

    fun decrypt(encryptedData: String, masterPassword: String): String? {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            val salt = combined.copyOfRange(0, 16)
            val iv = combined.copyOfRange(16, 28)
            val ciphertext = combined.copyOfRange(28, combined.size)
            val key = deriveKey(masterPassword, salt)
            decryptWithKey(ciphertext, key, iv)
        } catch (e: Exception) {
            null
        }
    }

    fun encryptWithKey(plaintext: String, key: SecretKeySpec, salt: ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }): String {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(gcmTagLength, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = salt + iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptWithKey(encryptedData: String, key: SecretKeySpec): String? {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            val salt = combined.copyOfRange(0, 16)
            val iv = combined.copyOfRange(16, 28)
            val ciphertext = combined.copyOfRange(28, combined.size)
            decryptWithKey(ciphertext, key, iv)
        } catch (e: Exception) {
            null
        }
    }

    private fun decryptWithKey(ciphertext: ByteArray, key: SecretKeySpec, iv: ByteArray): String? {
        return try {
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(gcmTagLength, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun hashPassword(password: String, salt: ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }): String {
        val key = deriveKey(password, salt)
        val hash = key.encoded
        val combined = salt + hash
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val combined = Base64.decode(storedHash, Base64.NO_WRAP)
            val salt = combined.copyOfRange(0, 16)
            val originalHash = combined.copyOfRange(16, combined.size)
            val key = deriveKey(password, salt)
            originalHash.contentEquals(key.encoded)
        } catch (e: Exception) {
            false
        }
    }

    fun storeDerivedKey(context: Context, masterPassword: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(masterPassword, salt).encoded
        getSecurePrefs(context).edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_VALUE, Base64.encodeToString(key, Base64.NO_WRAP))
            .apply()
    }

    fun loadDerivedKey(context: Context): SecretKeySpec? {
        return try {
            val prefs = getSecurePrefs(context)
            val salt = prefs.getString(KEY_SALT, null) ?: return null
            val key = prefs.getString(KEY_VALUE, null) ?: return null
            SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "AES")
        } catch (e: Exception) {
            null
        }
    }

    fun isDerivedKeyAvailable(context: Context): Boolean {
        return try {
            getSecurePrefs(context).contains(KEY_SALT)
        } catch (e: Exception) {
            false
        }
    }

    fun ensureDefaultKey(context: Context) {
        val prefs = context.getSharedPreferences("pw_default", Context.MODE_PRIVATE)
        if (!prefs.contains("default_key")) {
            val keyBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
            prefs.edit().putString("default_key", Base64.encodeToString(keyBytes, Base64.NO_WRAP)).apply()
        }
    }

    fun getDefaultKey(context: Context): SecretKeySpec? {
        return try {
            val prefs = context.getSharedPreferences("pw_default", Context.MODE_PRIVATE)
            val encoded = prefs.getString("default_key", null) ?: return null
            SecretKeySpec(Base64.decode(encoded, Base64.NO_WRAP), "AES")
        } catch (e: Exception) {
            null
        }
    }

    private fun getSecurePrefs(context: Context) = runCatching {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "pw_crypto_keys",
            keyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrNull() ?: throw SecurityException("Cannot create secure storage")

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    companion object {
        private const val KEY_SALT = "derived_salt"
        private const val KEY_VALUE = "derived_key"
    }
}
