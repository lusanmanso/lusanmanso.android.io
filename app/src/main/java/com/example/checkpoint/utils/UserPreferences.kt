// Author: Pair Programmer
// OS support: All
// Description: Manages user preferences like theme settings using SharedPreferences.
package com.example.checkpoint.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

public class UserPreferences private constructor(context: Context) { // Made constructor private for singleton

    public companion object {
        private const val PREF_NAME = "checkpoint_preferences"
        private const val KEY_NIGHT_MODE = "night_mode"

        @Volatile private var INSTANCE: UserPreferences? = null

        public fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun isNightMode(): Boolean {
        return prefs.getBoolean(KEY_NIGHT_MODE, false)
    }

    public fun toggleNightMode() {
        val isCurrentlyNightMode = isNightMode()
        prefs.edit { putBoolean(KEY_NIGHT_MODE, !isCurrentlyNightMode) }

        applyTheme(!isCurrentlyNightMode)
    }

    public fun applyThemeOnStart() {
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