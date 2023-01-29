package com.example.login_demo

import android.content.Context
import android.content.SharedPreferences


class AppPreferences {

    companion object {

        val USER_ID = "userID"
        val TOKEN = "token"
        val PROFILE_PIC = "profile_pic"
        val USER_NAME = "username"
    }
    val APP_PREFERENCES_FILE_NAME = "userdata"
    private var preferences: SharedPreferences? = null

    constructor(context: Context) {
        preferences = context.getSharedPreferences(APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun getString(key: String?): String? {
        return preferences!!.getString(key, null)
    }

    fun putString(key: String?, value: String?) {
        val editor = preferences!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun clear() {
        val editor = preferences!!.edit()
        editor.clear()
        editor.apply()
    }
}