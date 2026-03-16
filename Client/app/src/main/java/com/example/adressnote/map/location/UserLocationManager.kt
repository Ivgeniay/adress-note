package com.example.adressnote.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.adressnote.settings.SettingsManager
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView

class UserLocationManager(
    private val context: Context,
    private val mapView: MapView
) {
    private var userLocationLayer: UserLocationLayer? = null
    private var radiusCircle: CircleMapObject? = null
    private val settingsManager = SettingsManager(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val locationListener = LocationListener { location ->
        updateRadiusCircle(location.latitude, location.longitude)
    }

    fun init() {
        if (!hasLocationPermission()) return

        userLocationLayer = MapKitFactory.getInstance()
            .createUserLocationLayer(mapView.mapWindow)
        userLocationLayer?.isVisible = true
        userLocationLayer?.isHeadingModeActive = true

        userLocationLayer?.setObjectListener(object : UserLocationObjectListener {
            override fun onObjectAdded(userLocationView: UserLocationView) {
                updateRadiusCircle()
            }

            override fun onObjectRemoved(userLocationView: UserLocationView) {}

            override fun onObjectUpdated(
                userLocationView: UserLocationView,
                event: ObjectEvent
            ) {}
        })

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L,
                2f,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000L,
                2f,
                locationListener
            )
        } catch (e: SecurityException) {
            android.util.Log.e("UserLocationManager", "No location permission: ${e.message}")
        }
    }

    fun moveToUserLocation() {
        if (!hasLocationPermission()) return

        userLocationLayer?.cameraPosition()?.let { position ->
            mapView.mapWindow.map.move(
                CameraPosition(
                    position.target,
                    17f,
                    0f,
                    0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }

    fun updateRadius() {
        updateRadiusCircle()
    }

    fun release() {
        locationManager.removeUpdates(locationListener)
    }

    private fun updateRadiusCircle(lat: Double? = null, lng: Double? = null) {
        val position = if (lat != null && lng != null) {
            Point(lat, lng)
        } else {
            userLocationLayer?.cameraPosition()?.target
        } ?: return

        val radius = settingsManager.notificationRadius.toFloat()

        if (radiusCircle == null) {
            radiusCircle = mapView.mapWindow.map.mapObjects.addCircle(
                Circle(position, radius)
            ).apply {
                fillColor = 0x330080FF.toInt()
                strokeColor = 0x990080FF.toInt()
                strokeWidth = 2f
            }
        } else {
            radiusCircle?.geometry = Circle(position, radius)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}