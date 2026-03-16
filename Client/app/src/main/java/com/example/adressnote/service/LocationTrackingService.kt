package com.example.adressnote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import com.example.adressnote.BuildConfig
import com.example.adressnote.network.NotesApiService
import com.example.adressnote.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var locationManager: LocationManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var notesApiService: NotesApiService
    private lateinit var notificationManager: NotificationManager

    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private val notifiedEntrances = mutableSetOf<String>()

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val ALERT_NOTIFICATION_ID_START = 1000
        private const val CHANNEL_TRACKING_ID = "tracking_channel"
        private const val CHANNEL_ALERT_ID = "alert_channel"
        private var alertNotificationCounter = ALERT_NOTIFICATION_ID_START
    }

    private val locationListener = LocationListener { location ->
        currentLat = location.latitude
        currentLng = location.longitude
        Log.d("LocationTracking", "Location updated: ${location.latitude}, ${location.longitude}")
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        settingsManager = SettingsManager(this)
        notesApiService = NotesApiService(BuildConfig.BACKEND_URL)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
        startLocationUpdates()
        startPolling()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                5f,
                locationListener
            )
        } catch (e: SecurityException) {
            Log.e("LocationTracking", "No location permission: ${e.message}")
        }
    }

    private fun startPolling() {
        scope.launch {
            while (true) {
                if (!settingsManager.trackingEnabled) {
                    delay(5000)
                    continue
                }

                val lat = currentLat
                val lng = currentLng

                if (lat != null && lng != null) {
                    checkNearbyNotes(lat, lng)
                }

                delay(settingsManager.trackingInterval * 1000L)
            }
        }
    }

    private suspend fun checkNearbyNotes(lat: Double, lng: Double) {
        try {
            val radius = settingsManager.notificationRadius.toDouble()
            val notes = notesApiService.getNearby(lat, lng, radius)

            val currentKeys = notes.map { "${it.address}_${it.building}_${it.entrance}" }.toSet()
            notifiedEntrances.retainAll(currentKeys)

            for (note in notes) {
                val key = "${note.address}_${note.building}_${note.entrance}"
                if (key !in notifiedEntrances) {
                    sendNoteNotification(note.address, note.building, note.entrance, note.note)
                    notifiedEntrances.add(key)
                    Log.d("LocationTracking", "Notification sent for $key")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationTracking", "checkNearbyNotes error: ${e.message}", e)
        }
    }

    private fun sendNoteNotification(address: String, building: String, entrance: String, note: String) {
        val notification = Notification.Builder(this, CHANNEL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$address, $building - подъезд $entrance")
            .setContentText(note)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alertNotificationCounter++, notification)
    }

    private fun buildForegroundNotification(): Notification {
        return Notification.Builder(this, CHANNEL_TRACKING_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("AdressNotes")
            .setContentText("Отслеживание местоположения активно")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val trackingChannel = NotificationChannel(
            CHANNEL_TRACKING_ID,
            "Отслеживание",
            NotificationManager.IMPORTANCE_LOW
        )
        val alertChannel = NotificationChannel(
            CHANNEL_ALERT_ID,
            "Уведомления о заметках",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(trackingChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }
}