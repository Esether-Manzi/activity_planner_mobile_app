package com.example.activityplanner.session


import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager handles login session persistence using SharedPreferences.
 * - Saves logged-in user ID and email.
 * - Provides methods to check login status and logout.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "activity_planner_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveUserSession(userId: Int, email: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id != -1) id else null
    }

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = getUserId() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
