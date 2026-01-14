package com.client.smartpigclient.Utils

import android.content.Context

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN = "auth_token"

    fun getToken(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_TOKEN, "") ?: ""
    }
}
