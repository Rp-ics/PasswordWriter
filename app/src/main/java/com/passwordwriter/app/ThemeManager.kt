package com.passwordwriter.app

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object ThemeManager {
    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"
    const val THEME_VINTAGE = "vintage"
    const val THEME_BLUE = "blue"

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        val theme = prefs.getString("theme", THEME_DARK) ?: THEME_DARK
        when (theme) {
            THEME_LIGHT -> activity.setTheme(R.style.Theme_PasswordWriter_Light)
            THEME_VINTAGE -> activity.setTheme(R.style.Theme_PasswordWriter_Vintage)
            THEME_BLUE -> activity.setTheme(R.style.Theme_PasswordWriter_Blue)
            else -> activity.setTheme(R.style.Theme_PasswordWriter_Dark)
        }
    }

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        return prefs.getString("theme", THEME_DARK) ?: THEME_DARK
    }

    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .putString("theme", theme)
            .apply()
    }
}
