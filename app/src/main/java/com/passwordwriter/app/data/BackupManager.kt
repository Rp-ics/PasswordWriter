package com.passwordwriter.app.data

import android.content.Context
import android.net.Uri
import com.passwordwriter.app.PasswordWriterApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(private val context: Context) {

    private val crypto = CryptoManager()

    fun export(uri: Uri, masterPassword: String): Result<Unit> = runCatching {
        val repo = PasswordWriterApp.instance.repository
        val passwords = runBlocking { repo.getAllPasswords().first() }

        val json = JSONArray()
        for (p in passwords) {
            val decrypted = repo.decryptPassword(p) ?: continue
            json.put(JSONObject().apply {
                put("name", p.name)
                put("username", p.username)
                put("password", decrypted)
                put("category", p.category)
                put("notes", p.notes)
                put("categoryColor", p.categoryColor)
                put("categoryIcon", p.categoryIcon)
            })
        }

        val plaintext = json.toString(2)
        val encrypted = crypto.encrypt(plaintext, masterPassword)

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream).use { writer ->
                writer.write(encrypted)
            }
        } ?: throw Exception("Cannot open file")
    }

    fun `import`(uri: Uri, masterPassword: String): Result<Int> = runCatching {
        val encrypted = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        } ?: throw Exception("Cannot open file")

        val plaintext = crypto.decrypt(encrypted, masterPassword)
            ?: throw SecurityException("Wrong master password or corrupted file")

        val json = JSONArray(plaintext)
        val repo = PasswordWriterApp.instance.repository
        var count = 0

        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val entity = PasswordEntity(
                name = obj.optString("name", "Unknown"),
                username = obj.optString("username", ""),
                encryptedPassword = obj.optString("password", ""),
                category = obj.optString("category", "General"),
                notes = obj.optString("notes", ""),
                categoryColor = obj.optInt("categoryColor", 0),
                categoryIcon = obj.optString("categoryIcon", "other")
            )
            runBlocking { repo.savePassword(entity) }
            count++
        }

        count
    }
}
