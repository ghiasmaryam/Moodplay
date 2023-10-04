package com.music.moodplay.presentation.utils

import android.content.Context
import android.content.SharedPreferences

class CustomSharedPreference(context: Context) {

    private val PREFS_NAME = "MoodPlay"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    fun saveStringValue(KEY_NAME: String, value: String) {
        editor.putString(KEY_NAME, value)
        editor.commit()
    }

    fun getStringValue(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, "")
    }

    fun clearSharedPreference() {
        editor.clear()
        editor.commit()
    }

}