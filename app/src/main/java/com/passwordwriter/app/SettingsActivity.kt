package com.passwordwriter.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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

class SettingsActivity : BaseActivity() {

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
            restartApp()
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
            restartApp()
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
        val icons = CategoryManager.AVAILABLE_ICONS
        val iconNames = icons.map { it.replaceFirstChar { c -> c.uppercase() } }
        val colors = CategoryManager.AVAILABLE_COLORS

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
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.activity_list_item, android.R.id.text1, names) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                view.findViewById<android.widget.TextView>(android.R.id.text1).apply {
                    val drawableId = CategoryManager.getIconDrawableId(icons[position])
                    setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0)
                    compoundDrawablePadding = 16
                }
                return view
            }
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_choose_icon))
            .setAdapter(adapter) { _, which ->
                CategoryManager.setIcon(this, category, icons[which])
                loadCategories()
            }
            .show()
    }

    private fun showColorPicker(category: String, defaultColors: List<Int>) {
        val names = defaultColors.map { c -> String.format("#%06X", 0xFFFFFF and c) } + getString(R.string.category_custom_color)
        val colors = defaultColors.map { c -> c } + listOf(0)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_choose_color))
            .setItems(names.toTypedArray()) { _, which ->
                if (which == colors.lastIndex) {
                    showCustomColorPicker(category)
                } else {
                    CategoryManager.setColor(this, category, colors[which])
                    loadCategories()
                }
            }
            .show()
    }

    private fun showCustomColorPicker(category: String) {
        val input = TextInputEditText(this)
        input.hint = "#FF5722"
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.category_custom_color))
            .setView(input)
            .setPositiveButton(getString(R.string.save_btn)) { _, _ ->
                val hex = input.text?.toString()?.trim() ?: ""
                try {
                    val color = android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
                    CategoryManager.setColor(this, category, color)
                    loadCategories()
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
                }
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

    companion object {
        fun start(activity: BaseActivity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}
