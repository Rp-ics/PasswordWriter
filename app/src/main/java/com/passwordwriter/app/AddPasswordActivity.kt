package com.passwordwriter.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.passwordwriter.app.data.CategoryManager
import com.passwordwriter.app.data.PasswordEntity
import com.passwordwriter.app.service.PasswordGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddPasswordActivity : AppCompatActivity() {

    private lateinit var categoryInput: android.widget.AutoCompleteTextView
    private lateinit var nameInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var generateButton: Button

    private var editId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_password)

        categoryInput = findViewById(R.id.categoryInput)
        nameInput = findViewById(R.id.nameInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        generateButton = findViewById(R.id.generateButton)

        loadCategorySuggestions()

        editId = intent.getLongExtra("password_id", -1).takeIf { it != -1L }

        if (editId != null) {
            loadPasswordForEdit(editId!!)
            deleteButton.visibility = android.view.View.VISIBLE
            deleteButton.setOnClickListener { confirmDelete() }
        }

        saveButton.setOnClickListener { savePassword() }
        generateButton.setOnClickListener { showGeneratorDialog() }
    }

    private fun loadCategorySuggestions() {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val categories = app.repository.getAllCategories().first()
                val adapter = ArrayAdapter(
                    this@AddPasswordActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
                categoryInput.setAdapter(adapter)
            } catch (_: Exception) { }
        }
    }

    private fun showGeneratorDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_generator, null)
        val lengthSlider = dialogView.findViewById<Slider>(R.id.genLengthSlider)
        val lengthText = dialogView.findViewById<TextView>(R.id.genLengthText)
        val upperSwitch = dialogView.findViewById<SwitchMaterial>(R.id.genUpperSwitch)
        val lowerSwitch = dialogView.findViewById<SwitchMaterial>(R.id.genLowerSwitch)
        val digitSwitch = dialogView.findViewById<SwitchMaterial>(R.id.genDigitSwitch)
        val symbolSwitch = dialogView.findViewById<SwitchMaterial>(R.id.genSymbolSwitch)
        val previewText = dialogView.findViewById<TextView>(R.id.genPreviewText)
        val generateBtn = dialogView.findViewById<Button>(R.id.genGenerateBtn)

        lengthText.text = getString(R.string.generator_length, 16)
        lengthSlider.addOnChangeListener { _, value, _ ->
            lengthText.text = getString(R.string.generator_length, value.toInt())
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.generator_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.action_use), null)
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()

        generateBtn.setOnClickListener {
            val opts = PasswordGenerator.Options(
                length = lengthSlider.value.toInt(),
                useUpper = upperSwitch.isChecked,
                useLower = lowerSwitch.isChecked,
                useDigits = digitSwitch.isChecked,
                useSymbols = symbolSwitch.isChecked
            )
            val password = PasswordGenerator.generate(opts)
            previewText.text = password
            previewText.visibility = TextView.VISIBLE
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = previewText.text.toString()
            if (text.isNotBlank() && text != getString(R.string.generator_title)) {
                passwordInput.setText(text)
                dialog.dismiss()
            } else {
                Toast.makeText(this, getString(R.string.action_generate), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePassword() {
        val category = categoryInput.text?.toString()?.trim()?.ifBlank { "General" } ?: "General"
        val name = nameInput.text?.toString()?.trim() ?: ""
        val username = usernameInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString() ?: ""

        if (name.isEmpty()) {
            nameInput.error = getString(R.string.label_name)
            return
        }
        if (password.isEmpty()) {
            findViewById<TextInputLayout>(R.id.passwordLayout).error = getString(R.string.label_password)
            return
        }

        val catIcon = CategoryManager.getIcon(this, category)
        val catColor = CategoryManager.getColor(this, category)

        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val entity = PasswordEntity(
                    id = editId ?: 0,
                    category = category,
                    name = name,
                    username = username,
                    encryptedPassword = password,
                    notes = "",
                    categoryColor = catColor,
                    categoryIcon = catIcon
                )

                if (editId != null) {
                    app.repository.updatePassword(entity)
                } else {
                    app.repository.savePassword(entity)
                }
                Toast.makeText(this@AddPasswordActivity,
                    if (editId != null) "Updated" else "Saved",
                    Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddPasswordActivity, getString(R.string.error_generic, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadPasswordForEdit(id: Long) {
        lifecycleScope.launch {
            try {
                val app = application as PasswordWriterApp
                val entity = app.repository.getPasswordById(id) ?: return@launch
                val decrypted = app.repository.decryptPassword(entity) ?: return@launch

                categoryInput.setText(entity.category)
                nameInput.setText(entity.name)
                usernameInput.setText(entity.username)
                passwordInput.setText(decrypted)
            } catch (_: Exception) { }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.action_delete))
            .setMessage(getString(R.string.dialog_confirm_delete_msg))
            .setPositiveButton(getString(R.string.dialog_delete_password)) { _, _ ->
                lifecycleScope.launch {
                    try {
                        val app = application as PasswordWriterApp
                        editId?.let { app.repository.deletePasswordById(it) }
                        Toast.makeText(this@AddPasswordActivity, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (_: Exception) { }
                }
            }
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show()
    }
}
