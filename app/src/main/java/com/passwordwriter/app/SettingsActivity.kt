package com.passwordwriter.app

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.passwordwriter.app.PasswordWriterApp
import com.passwordwriter.app.data.BackupManager
import com.passwordwriter.app.data.CategoryManager
import com.passwordwriter.app.ui.adapters.CategoryAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var languageGroup: RadioGroup
    private lateinit var themeGroup: RadioGroup
    private lateinit var passwordLockSwitch: SwitchMaterial
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

        languageGroup = findViewById(R.id.languageGroup)
        themeGroup = findViewById(R.id.themeGroup)
        passwordLockSwitch = findViewById(R.id.passwordLockSwitch)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        categoryList = findViewById(R.id.categoryList)

        // Language
        val currentLang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "en") ?: "en"
        when (currentLang) {
            "it" -> languageGroup.check(R.id.langItalian)
            "ru" -> languageGroup.check(R.id.langRussian)
            else -> languageGroup.check(R.id.langEnglish)
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val lang = when (checkedId) {
                R.id.langItalian -> "it"
                R.id.langRussian -> "ru"
                else -> "en"
            }
            getSharedPreferences("settings", MODE_PRIVATE).edit().putString("language", lang).apply()
            setLocale(lang)
            Toast.makeText(this, getString(R.string.applied), Toast.LENGTH_SHORT).show()
        }

        // Theme
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
            recreate()
        }

        // Password lock
        val lockEnabled = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("password_lock", true)
        passwordLockSwitch.isChecked = lockEnabled
        passwordLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("password_lock", isChecked).apply()
        }

        // Backup
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
                    context = this@SettingsActivity,
                    categories = categories,
                    onRename = { oldName -> showRenameDialog(oldName) },
                    onEdit = { category -> showCategoryEditDialog(category) }
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

    private fun showCategoryEditDialog(category: String) {
        val currentIcon = CategoryManager.getIcon(this, category)
        val currentColor = CategoryManager.getColor(this, category)

        val icons = CategoryManager.AVAILABLE_ICONS
        val iconNames = icons.map { it.replaceFirstChar { c -> c.uppercase() } }
        val iconDrawables = icons.map { CategoryManager.getIconDrawableId(it) }

        val colors = CategoryManager.AVAILABLE_COLORS
        val colorNames = colors.map { String.format("#%06X", 0xFFFFFF and it) }

        val items = arrayOf(
            getString(R.string.category_rename),
            getString(R.string.category_choose_icon),
            getString(R.string.category_choose_color)
        )

        AlertDialog.Builder(this)
            .setTitle(category)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showRenameDialog(category)
                    1 -> showIconPicker(category, icons, iconNames)
                    2 -> showColorPicker(category, colors)
                }
            }
            .show()
    }

    private fun showIconPicker(category: String, icons: List<String>, names: List<String>) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_choose_icon))
            .setItems(names.toTypedArray()) { _, which ->
                CategoryManager.setIcon(this, category, icons[which])
                loadCategories()
            }
            .show()
    }

    private fun showColorPicker(category: String, colors: List<Int>) {
        val colorNames = colors.map { String.format("#%06X", 0xFFFFFF and it) }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_choose_color))
            .setItems(colorNames.toTypedArray()) { _, which ->
                CategoryManager.setColor(this, category, colors[which])
                loadCategories()
            }
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
                            val app = application as PasswordWriterApp
                            CategoryManager.renameCategory(this@SettingsActivity, oldName, newName)
                            app.repository.renameCategory(oldName, newName)
                            loadCategories()
                        } catch (_: Exception) { }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}
