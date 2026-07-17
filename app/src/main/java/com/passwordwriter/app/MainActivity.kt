package com.passwordwriter.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.passwordwriter.app.data.PasswordEntity
import com.passwordwriter.app.ui.adapters.CategoryAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_generic, e.message), Toast.LENGTH_LONG).show()
            return
        }

        try {
            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_all_passwords -> { showAllPasswords(); true }
                    R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
                    else -> false
                }
            }

            // Search
            val searchItem = toolbar.menu.findItem(R.id.action_search)
            val searchView = searchItem?.actionView as? SearchView
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchPasswords(it) }
                    return true
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let { searchPasswords(it) }
                    return true
                }
            })

            findViewById<MaterialButton>(R.id.btnAddPassword).setOnClickListener {
                startActivity(Intent(this, AddPasswordActivity::class.java))
            }

            findViewById<MaterialButton>(R.id.btnAllPasswords).setOnClickListener {
                showAllPasswords()
            }

            findViewById<MaterialButton>(R.id.setupKeyboardBtn).setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }

            loadCategories()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_generic, e.message), Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadCategories()
            updateKeyboardStatus()
        } catch (_: Exception) { }
    }

    private fun updateKeyboardStatus() {
        val imeId = "$packageName/.service.PasswordKeyboardService"
        val enabled = try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
                ?.contains(imeId) == true
        } catch (_: Exception) { false }

        val keyboardCard = findViewById<View>(R.id.keyboardCard)
        if (keyboardCard != null) {
            keyboardCard.visibility = if (enabled) View.GONE else View.VISIBLE
        }

        findViewById<TextView>(R.id.keyboardStatusText)?.apply {
            text = if (enabled) {
                getString(R.string.keyboard_status_enabled)
            } else {
                getString(R.string.keyboard_instructions)
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                if (!app.isInitialized) return@launch
                val categories = app.repository.getAllCategories().first()
                findViewById<RecyclerView>(R.id.categoryRecyclerView)?.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = CategoryAdapter(this@MainActivity, categories, { cat ->
                        showCategoryPasswords(cat)
                    }, { cat ->
                        showCategoryOptions(cat)
                    })
                }
            } catch (_: Exception) { }
        }
    }

    private fun searchPasswords(query: String) {
        if (query.isBlank()) return
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val results = app.repository.searchPasswords("%$query%").first()
                showPasswordDialog(getString(R.string.action_search) + ": $query", results)
            } catch (_: Exception) { }
        }
    }

    private fun showAllPasswords() {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                showPasswordDialog(getString(R.string.action_all_passwords), app.repository.getAllPasswords().first())
            } catch (_: Exception) { }
        }
    }

    private fun showCategoryPasswords(category: String) {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val passwords = app.repository.getPasswordsByCategory(category).first()
                showPasswordDialog(category, passwords)
            } catch (_: Exception) { }
        }
    }

    private fun showPasswordDialog(title: String, passwords: List<PasswordEntity>) {
        if (passwords.isEmpty()) {
            Toast.makeText(this, getString(R.string.dialog_no_passwords), Toast.LENGTH_SHORT).show()
            return
        }

        val app = application as PasswordWriterApp
        val names = passwords.map { e ->
            if (e.username.isNotEmpty()) "${e.name} (${e.username})" else e.name
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(names) { _, which ->
                val entity = passwords[which]
                val decrypted = app.repository.decryptPassword(entity)
                if (decrypted != null) showActions(entity, decrypted)
                else Toast.makeText(this, getString(R.string.dialog_decrypt_error), Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton(getString(R.string.close_btn), null)
            .show()
    }

    private fun showActions(entity: PasswordEntity, plain: String) {
        AlertDialog.Builder(this)
            .setTitle(entity.name)
            .setItems(arrayOf(
                getString(R.string.action_copy),
                getString(R.string.action_edit),
                getString(R.string.action_delete)
            )) { _, which ->
                when (which) {
                    0 -> {
                        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("password", plain))
                        Toast.makeText(this, getString(R.string.password_copied), Toast.LENGTH_SHORT).show()
                    }
                    1 -> startActivity(Intent(this, AddPasswordActivity::class.java).apply {
                        putExtra("password_id", entity.id)
                    })
                    2 -> confirmDelete(entity)
                }
            }
            .show()
    }

    private fun showCategoryOptions(category: String) {
        AlertDialog.Builder(this)
            .setTitle(category)
            .setItems(arrayOf(
                getString(R.string.category_rename),
                getString(R.string.action_delete)
            )) { _, which ->
                when (which) {
                    0 -> renameCategory(category)
                    1 -> deleteCategory(category)
                }
            }
            .show()
    }

    private fun renameCategory(category: String) {
        val input = android.widget.EditText(this).apply { setText(category) }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_rename))
            .setView(input)
            .setPositiveButton(getString(R.string.save_btn)) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != category) {
                    lifecycleScope.launch {
                        try {
                            (application as PasswordWriterApp).repository.renameCategory(category, newName)
                            loadCategories()
                        } catch (_: Exception) { }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }

    private fun deleteCategory(category: String) {
        val options = arrayOf(
            getString(R.string.category_move_to_general),
            getString(R.string.category_delete_all)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_delete_options))
            .setItems(options) { _, which ->
                lifecycleScope.launch {
                    try {
                        val app = application as PasswordWriterApp
                        if (which == 0) {
                            app.repository.reassignCategory(category, "General")
                        } else {
                            app.repository.deleteCategory(category)
                        }
                        loadCategories()
                    } catch (_: Exception) { }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }

    private fun confirmDelete(entity: PasswordEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirm_delete_title, entity.name))
            .setPositiveButton(getString(R.string.dialog_delete_password)) { _, _ ->
                lifecycleScope.launch {
                    try {
                        (application as PasswordWriterApp).repository.deletePassword(entity)
                        loadCategories()
                    } catch (_: Exception) { }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }
}
