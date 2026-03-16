package com.example.adressnote.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

class UserLocationManager(
    private val context: Context,
    private val mapView: MapView
) {
    private var userLocationLayer: UserLocationLayer? = null

    fun init() {
        if (!hasLocationPermission()) return

        userLocationLayer = MapKitFactory.getInstance()
            .createUserLocationLayer(mapView.mapWindow)
        userLocationLayer?.isVisible = true
        userLocationLayer?.isHeadingModeActive = true
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

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}