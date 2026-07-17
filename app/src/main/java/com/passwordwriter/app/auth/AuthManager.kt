package com.passwordwriter.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.passwordwriter.app.data.CryptoManager

class AuthManager(context: Context) {

    private val crypto = CryptoManager()
    private val prefs: SharedPreferences

    init {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(
            "passwordwriter_auth",
            keyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isMasterPasswordSet(): Boolean {
        return prefs.contains(KEY_HASH)
    }

    fun setMasterPassword(password: String) {
        val hash = crypto.hashPassword(password)
        prefs.edit().putString(KEY_HASH, hash).apply()
    }

    fun verifyMasterPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_HASH, null) ?: return false
        return crypto.verifyPassword(password, storedHash)
    }

    fun isFirstTime(): Boolean {
        return !isMasterPasswordSet()
    }

    companion object {
        private const val KEY_HASH = "master_password_hash"
    }
}
