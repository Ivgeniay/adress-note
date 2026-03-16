package com.example.adressnote.settings

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("adressnote_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATION_RADIUS = "notification_radius"
        const val DEFAULT_RADIUS = 30
    }

    var notificationRadius: Int
        get() = prefs.getInt(KEY_NOTIFICATION_RADIUS, DEFAULT_RADIUS)
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_RADIUS, value).apply()
}