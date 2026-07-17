package com.passwordwriter.app.data

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.passwordwriter.app.R
import org.json.JSONObject

object CategoryManager {

    private const val PREFS_NAME = "category_config"
    private const val KEY_ICONS = "category_icons"
    private const val KEY_COLORS = "category_colors"

    fun getIcon(context: Context, category: String): String {
        val json = getPrefs(context).getString(KEY_ICONS, "{}") ?: "{}"
        return JSONObject(json).optString(category, "general")
    }

    fun setIcon(context: Context, category: String, icon: String) {
        val json = getPrefs(context).getString(KEY_ICONS, "{}") ?: "{}"
        val obj = JSONObject(json)
        obj.put(category, icon)
        getPrefs(context).edit().putString(KEY_ICONS, obj.toString()).apply()
    }

    fun getColor(context: Context, category: String): Int {
        val json = getPrefs(context).getString(KEY_COLORS, "{}") ?: "{}"
        return JSONObject(json).optInt(category, 0)
    }

    fun setColor(context: Context, category: String, color: Int) {
        val json = getPrefs(context).getString(KEY_COLORS, "{}") ?: "{}"
        val obj = JSONObject(json)
        obj.put(category, color)
        getPrefs(context).edit().putString(KEY_COLORS, obj.toString()).apply()
    }

    fun getIconDrawableId(icon: String): Int {
        return when (icon) {
            "general" -> R.drawable.ic_category_general
            "social" -> R.drawable.ic_category_social
            "email" -> R.drawable.ic_category_email
            "finance" -> R.drawable.ic_category_finance
            "work" -> R.drawable.ic_category_work
            "shopping" -> R.drawable.ic_category_shopping
            "tech" -> R.drawable.ic_category_tech
            "entertainment" -> R.drawable.ic_category_entertainment
            "health" -> R.drawable.ic_category_health
            "education" -> R.drawable.ic_category_education
            "games" -> R.drawable.ic_category_games
            "notes" -> R.drawable.ic_category_notes
            "other" -> R.drawable.ic_category_other
            else -> R.drawable.ic_category_general
        }
    }

    val AVAILABLE_ICONS = listOf(
        "general", "social", "email", "finance", "work",
        "shopping", "tech", "entertainment", "health",
        "education", "games", "notes", "other"
    )

    val AVAILABLE_COLORS = listOf(
        0xFFE53935.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(),
        0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF00BCD4.toInt(),
        0xFF009688.toInt(), 0xFF4CAF50.toInt(), 0xFFCDDC39.toInt(),
        0xFFFF9800.toInt(), 0xFF795548.toInt(), 0xFF607D8B.toInt()
    )

    fun removeCategory(context: Context, category: String) {
        val iconsJson = getPrefs(context).getString(KEY_ICONS, "{}") ?: "{}"
        val iconsObj = JSONObject(iconsJson)
        iconsObj.remove(category)
        getPrefs(context).edit().putString(KEY_ICONS, iconsObj.toString()).apply()

        val colorsJson = getPrefs(context).getString(KEY_COLORS, "{}") ?: "{}"
        val colorsObj = JSONObject(colorsJson)
        colorsObj.remove(category)
        getPrefs(context).edit().putString(KEY_COLORS, colorsObj.toString()).apply()
    }

    fun renameCategory(context: Context, oldName: String, newName: String) {
        val icon = getIcon(context, oldName)
        val color = getColor(context, oldName)
        removeCategory(context, oldName)
        setIcon(context, newName, icon)
        setColor(context, newName, color)
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
}
