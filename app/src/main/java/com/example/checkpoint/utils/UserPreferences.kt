package com.example.checkpoint.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Class for managing user preferences, including dark/light theme
 */
class UserPreferences(context: Context) {

    companion object {
        private const val PREF_NAME = "checkpoint_preferences"
        private const val KEY_NIGHT_MODE = "night_mode"

            // Singleton instance
        @Volatile private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Gets the current theme mode
     * @return true if in dark mode, false if in light mode
     */
    fun isNightMode(): Boolean {
        return prefs.getBoolean(KEY_NIGHT_MODE, false)
    }

    /**
     * Toggles the theme mode and applies it
     */
    fun toggleNightMode() {
        val isCurrentlyNightMode = isNightMode()
        prefs.edit().putBoolean(KEY_NIGHT_MODE, !isCurrentlyNightMode).apply()

        applyTheme(!isCurrentlyNightMode)
    }

    /**
     * Applies the saved theme at application start
     */
    fun applyThemeOnStart() {
        applyTheme(isNightMode())
    }

    private fun applyTheme(isNightMode: Boolean) {
        val mode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
