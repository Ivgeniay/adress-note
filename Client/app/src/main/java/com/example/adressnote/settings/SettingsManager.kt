package com.example.adressnote.settings

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("adressnote_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATION_RADIUS = "notification_radius"
        private const val KEY_TRACKING_ENABLED = "tracking_enabled"
        private const val KEY_TRACKING_INTERVAL = "tracking_interval"
        const val DEFAULT_RADIUS = 30
        const val DEFAULT_TRACKING_ENABLED = true
        const val DEFAULT_TRACKING_INTERVAL = 30
    }

    var notificationRadius: Int
        get() = prefs.getInt(KEY_NOTIFICATION_RADIUS, DEFAULT_RADIUS)
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_RADIUS, value).apply()

    var trackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRACKING_ENABLED, DEFAULT_TRACKING_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_TRACKING_ENABLED, value).apply()

    var trackingInterval: Int
        get() = prefs.getInt(KEY_TRACKING_INTERVAL, DEFAULT_TRACKING_INTERVAL)
        set(value) = prefs.edit().putInt(KEY_TRACKING_INTERVAL, value).apply()
}