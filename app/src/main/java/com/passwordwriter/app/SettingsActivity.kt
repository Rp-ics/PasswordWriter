package com.passwordwriter.app

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.passwordwriter.app.PasswordWriterApp
import com.passwordwriter.app.data.BackupManager
import com.passwordwriter.app.ui.adapters.CategoryAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeGroup: RadioGroup
    private lateinit var exportButton: MaterialButton
    private lateinit var importButton: MaterialButton
    private lateinit var categoryList: RecyclerView
    private lateinit var backupManager: BackupManager

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) showPasswordPrompt { password ->
            lifecycleScope.launch {
                backupManager.export(uri, password)
                    .onSuccess { Toast.makeText(this@SettingsActivity, getString(R.string.export_success), Toast.LENGTH_SHORT).show() }
                    .onFailure { e -> Toast.makeText(this@SettingsActivity, getString(R.string.error_generic, e.message ?: ""), Toast.LENGTH_LONG).show() }
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) showPasswordPrompt { password ->
            lifecycleScope.launch {
                backupManager.`import`(uri, password)
                    .onSuccess { count -> Toast.makeText(this@SettingsActivity, getString(R.string.import_success) + " ($count)", Toast.LENGTH_SHORT).show(); loadCategories() }
                    .onFailure { e -> Toast.makeText(this@SettingsActivity, getString(R.string.error_generic, e.message ?: ""), Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backupManager = BackupManager(this)

        themeGroup = findViewById(R.id.themeGroup)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        categoryList = findViewById(R.id.categoryList)

        when (ThemeManager.getTheme(this)) {
            "light" -> themeGroup.check(R.id.themeLight)
            "vintage" -> themeGroup.check(R.id.themeVintage)
            else -> themeGroup.check(R.id.themeDark)
        }

        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.themeLight -> "light"
                R.id.themeVintage -> "vintage"
                else -> "dark"
            }
            ThemeManager.saveTheme(this, theme)
            Toast.makeText(this, getString(R.string.applied), Toast.LENGTH_SHORT).show()
        }

        exportButton.setOnClickListener {
            exportLauncher.launch("PasswordWriter_Backup.pwb")
        }

        importButton.setOnClickListener {
            importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val categories = app.repository.getAllCategories().first()
                categoryList.layoutManager = LinearLayoutManager(this@SettingsActivity)
                categoryList.adapter = CategoryAdapter(
                    categories = categories,
                    onRename = { oldName -> showRenameDialog(oldName) },
                    onDelete = { category -> showDeleteCategoryDialog(category) }
                )
            } catch (_: Exception) { }
        }
    }

    private fun showPasswordPrompt(onPassword: (String) -> Unit) {
        val input = TextInputEditText(this)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.backup_import_prompt))
            .setView(input)
            .setPositiveButton(getString(R.string.save_btn)) { _, _ ->
                val password = input.text?.toString() ?: ""
                if (password.isNotEmpty()) onPassword(password)
                else Toast.makeText(this, getString(R.string.lock_wrong), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }

    private fun showRenameDialog(oldName: String) {
        val input = TextInputEditText(this)
        input.setText(oldName)
        input.setSelection(oldName.length)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.rename_category))
            .setView(input)
            .setPositiveButton(getString(R.string.save_btn)) { _, _ ->
                val newName = input.text?.toString()?.trim() ?: return@setPositiveButton
                if (newName.isNotEmpty() && newName != oldName) {
                    lifecycleScope.launch {
                        try {
                            (application as PasswordWriterApp).repository.renameCategory(oldName, newName)
                            loadCategories()
                        } catch (_: Exception) { }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }

    private fun showDeleteCategoryDialog(category: String) {
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

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}
