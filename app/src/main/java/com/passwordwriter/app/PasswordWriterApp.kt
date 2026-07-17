package com.passwordwriter.app

import android.app.Application
import android.util.Log
import com.passwordwriter.app.data.AppDatabase
import com.passwordwriter.app.data.CryptoManager
import com.passwordwriter.app.data.PasswordRepository

class PasswordWriterApp : Application() {

    lateinit var repository: PasswordRepository
        private set
    var isInitialized: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

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

    companion object {
        lateinit var instance: PasswordWriterApp
            private set
    }
}
