package com.passwordwriter.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.passwordwriter.app.auth.AuthManager
import com.passwordwriter.app.data.CryptoManager
import com.passwordwriter.app.data.PasswordRepository

class SetupActivity : BaseActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmInput: TextInputEditText
    private lateinit var errorText: TextView
    private lateinit var setupButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        authManager = AuthManager(this)

        if (!authManager.isFirstTime()) {
            navigateToLock()
            return
        }

        passwordInput = findViewById(R.id.masterPasswordInput)
        confirmInput = findViewById(R.id.confirmPasswordInput)
        errorText = findViewById(R.id.errorText)
        setupButton = findViewById(R.id.setupButton)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                errorText.visibility = TextView.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        passwordInput.addTextChangedListener(watcher)
        confirmInput.addTextChangedListener(watcher)

        setupButton.setOnClickListener {
            val password = passwordInput.text?.toString() ?: ""
            val confirm = confirmInput.text?.toString() ?: ""

            when {
                password.length < 6 -> {
                    errorText.text = getString(R.string.setup_short)
                    errorText.visibility = TextView.VISIBLE
                }
                password != confirm -> {
                    errorText.text = getString(R.string.setup_mismatch)
                    errorText.visibility = TextView.VISIBLE
                }
                else -> {
                    PasswordRepository.masterPassword = password
                    CryptoManager().storeDerivedKey(this, password)
                    authManager.setMasterPassword(password)
                    navigateToMain()
                }
            }
        }
    }

    private fun navigateToLock() {
        startActivity(Intent(this, LockScreenActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
