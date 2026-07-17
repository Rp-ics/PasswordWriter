package com.passwordwriter.app

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.passwordwriter.app.data.AppDatabase
import com.passwordwriter.app.data.CryptoManager
import com.passwordwriter.app.data.PasswordRepository
import java.util.Locale

class PasswordWriterApp : Application() {

    lateinit var repository: PasswordRepository
        private set
    var isInitialized: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        applyLocale()

        try {
            val db = AppDatabase.getInstance(this)
            val crypto = CryptoManager()
            repository = PasswordRepository(db.passwordDao(), crypto)
            repository.initFromContext(this)
            isInitialized = true
        } catch (e: Exception) {
            Log.e("PasswordWriter", "Init error", e)
        }
    }

    @Suppress("DEPRECATION")
    fun applyLocale() {
        val lang = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    companion object {
        lateinit var instance: PasswordWriterApp
            private set
    }
}
