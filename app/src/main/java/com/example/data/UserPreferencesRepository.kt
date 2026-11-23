package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists application-wide user preferences (language and theme mode)
 * so that both MainActivity and LockScreenActivity seamlessly synchronize
 * the user's choices.
 */
class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isPortuguese: Boolean
        get() = prefs.getBoolean(KEY_IS_PORTUGUESE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_PORTUGUESE, value).apply()
        }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()
        }

    var isSystemLockScreenEnabled: Boolean
        get() = prefs.getBoolean(KEY_SYSTEM_LOCK_SCREEN_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SYSTEM_LOCK_SCREEN_ENABLED, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "t_minus_mkt_preferences"
        private const val KEY_IS_PORTUGUESE = "pref_is_portuguese"
        private const val KEY_IS_DARK_MODE = "pref_is_dark_mode"
        private const val KEY_SYSTEM_LOCK_SCREEN_ENABLED = "pref_system_lock_screen_enabled"
    }
}
