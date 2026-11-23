package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class UserSession(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String? = null,
    val loggedInAt: Long = System.currentTimeMillis()
)

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "t_minus_mkt_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for devices/test environments with KeyStore limitations
            context.getSharedPreferences("t_minus_mkt_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_EMAIL = "key_user_email"
        private const val KEY_DISPLAY_NAME = "key_user_display_name"
        private const val KEY_PHOTO_URL = "key_user_photo_url"
        private const val KEY_ID_TOKEN = "key_user_id_token"
        private const val KEY_LOGGED_IN_AT = "key_logged_in_at"
    }

    fun saveSession(user: UserSession) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_PHOTO_URL, user.photoUrl)
            .putString(KEY_ID_TOKEN, user.idToken)
            .putLong(KEY_LOGGED_IN_AT, user.loggedInAt)
            .apply()
    }

    fun getSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "Marketing Director") ?: "Marketing Director"
        val photoUrl = prefs.getString(KEY_PHOTO_URL, null)
        val idToken = prefs.getString(KEY_ID_TOKEN, null)
        val loggedInAt = prefs.getLong(KEY_LOGGED_IN_AT, System.currentTimeMillis())

        return UserSession(
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            idToken = idToken,
            loggedInAt = loggedInAt
        )
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
