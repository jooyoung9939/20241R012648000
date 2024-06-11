package api

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "token_prefs"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"
    private const val NICKNAME = "nickname"
    private const val IS_LOGGED_IN = "is_logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(context: Context, token: String) {
        getPrefs(context).edit().putString(ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(ACCESS_TOKEN, null)
    }

    fun saveRefreshToken(context: Context, token: String) {
        getPrefs(context).edit().putString(REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(REFRESH_TOKEN, null)
    }

    fun saveNickname(context: Context, nickname: String) {
        getPrefs(context).edit().putString(NICKNAME, nickname).apply()
    }

    fun getNickname(context: Context): String? {
        return getPrefs(context).getString(NICKNAME, null)
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(IS_LOGGED_IN, false)
    }

    fun clearTokens(context: Context) {
        getPrefs(context).edit().remove(ACCESS_TOKEN).remove(REFRESH_TOKEN).remove(NICKNAME).apply()
    }
}
