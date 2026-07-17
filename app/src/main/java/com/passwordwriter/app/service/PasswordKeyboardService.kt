package com.passwordwriter.app.service

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.passwordwriter.app.PasswordWriterApp
import com.passwordwriter.app.R
import com.passwordwriter.app.ThemeManager
import com.passwordwriter.app.ui.adapters.PasswordItemAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class PasswordKeyboardService : InputMethodService() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        PasswordWriterApp.instance.repository // ensure initialized
        applyLocale()
    }

    @Suppress("DEPRECATION")
    private fun applyLocale() {
        val lang = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun getThemeResId(): Int {
        return when (ThemeManager.getTheme(this)) {
            "light" -> R.style.Theme_PasswordWriter_Light
            "vintage" -> R.style.Theme_PasswordWriter_Vintage
            else -> R.style.Theme_PasswordWriter_Dark
        }
    }

    override fun onCreateInputView(): View {
        val themedContext = ContextThemeWrapper(this, getThemeResId())
        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.keyboard_view, null)

        view.findViewById<Button>(R.id.pickPasswordBtn).setOnClickListener {
            showPasswordList(view)
        }

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            view.findViewById<View>(R.id.mainView).visibility = View.VISIBLE
            view.findViewById<View>(R.id.passwordListView).visibility = View.GONE
        }

        return view
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun showPasswordList(view: View) {
        scope.launch {
            val repo = PasswordWriterApp.instance.repository
            val passwords = repo.getAllPasswords().first()

            if (passwords.isEmpty()) {
                Toast.makeText(this@PasswordKeyboardService, getString(R.string.dialog_no_passwords), Toast.LENGTH_SHORT).show()
                return@launch
            }

            val items = passwords.mapNotNull { entity ->
                val decrypted = repo.decryptPassword(entity)
                if (decrypted != null) {
                    PasswordItemAdapter.PopupItem(
                        title = entity.name,
                        subtitle = entity.username,
                        password = decrypted,
                        isPassword = true
                    )
                } else null
            }

            if (items.isEmpty()) {
                Toast.makeText(this@PasswordKeyboardService, getString(R.string.dialog_decrypt_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            val rv = view.findViewById<RecyclerView>(R.id.passwordList)
            rv.layoutManager = LinearLayoutManager(this@PasswordKeyboardService)
            rv.adapter = PasswordItemAdapter(items) { item ->
                if (item.password != null) {
                    commitPassword(item.password)
                    view.findViewById<View>(R.id.mainView).visibility = View.VISIBLE
                    view.findViewById<View>(R.id.passwordListView).visibility = View.GONE
                }
            }

            view.findViewById<View>(R.id.mainView).visibility = View.GONE
            view.findViewById<View>(R.id.passwordListView).visibility = View.VISIBLE
        }
    }

    private fun commitPassword(password: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(password, 1)
        Toast.makeText(this, getString(R.string.password_written), Toast.LENGTH_SHORT).show()
    }
}
