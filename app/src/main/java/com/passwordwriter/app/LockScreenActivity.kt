package com.passwordwriter.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.passwordwriter.app.auth.AuthManager
import com.passwordwriter.app.data.CryptoManager
import com.passwordwriter.app.data.PasswordRepository

class LockScreenActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var passwordInput: TextInputEditText
    private lateinit var errorText: TextView
    private lateinit var unlockButton: MaterialButton
    private lateinit var biometricButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        authManager = AuthManager(this)

        if (authManager.isFirstTime()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        passwordInput = findViewById(R.id.masterPasswordInput)
        errorText = findViewById(R.id.errorText)
        unlockButton = findViewById(R.id.unlockButton)
        biometricButton = findViewById(R.id.biometricButton)

        unlockButton.setOnClickListener {
            val password = passwordInput.text?.toString() ?: ""
            if (authManager.verifyMasterPassword(password)) {
                PasswordRepository.masterPassword = password
                CryptoManager().storeDerivedKey(this, password)
                navigateToMain()
            } else {
                errorText.text = getString(R.string.lock_wrong)
                errorText.visibility = TextView.VISIBLE
            }
        }

        biometricButton.setOnClickListener {
            showBiometricAuth()
        }

        passwordInput.setOnEditorActionListener { _, _, _ ->
            unlockButton.performClick()
            true
        }

        showBiometricAuth()
    }

    private fun showBiometricAuth() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                navigateToMain()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.lock_title))
            .setSubtitle(getString(R.string.unlock_with_biometric))
            .setNegativeButtonText(getString(R.string.lock_btn))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
