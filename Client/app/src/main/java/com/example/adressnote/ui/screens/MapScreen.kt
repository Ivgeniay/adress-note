package com.example.adressnote.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adressnote.BuildConfig
import com.example.adressnote.map.interaction.MapTapHandler
import com.example.adressnote.map.location.UserLocationManager
import com.example.adressnote.network.GeocoderService
import com.example.adressnote.network.NotesApiService
import com.example.adressnote.service.LocationTrackingService
import com.example.adressnote.settings.SettingsManager
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

data class SelectedEntrance(
    val address: String,
    val building: String,
    val entrance: String,
    val lat: Double,
    val lng: Double,
    val openedAt: Long = System.currentTimeMillis()
)

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }
    val userLocationManager = remember { UserLocationManager(context, mapView) }
    val geocoderService = remember { GeocoderService(BuildConfig.GEOCODER_API_KEY) }
    val apiService = remember { NotesApiService(BuildConfig.BACKEND_URL) }
    val settingsManager = remember { SettingsManager(context) }

    var selectedEntrance by remember { mutableStateOf<SelectedEntrance?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val mapTapHandler = remember {
        MapTapHandler(
            map = mapView.mapWindow.map,
            geocoderService = geocoderService,
            scope = scope,
            onEntranceTapped = { address, building, entrance, lat, lng ->
                selectedEntrance = SelectedEntrance(address, building, entrance, lat, lng)
            }
        )
    }

    DisposableEffect(Unit) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        userLocationManager.init()
        userLocationManager.moveToUserLocation()

        if (settingsManager.trackingEnabled) {
            context.startForegroundService(
                Intent(context, LocationTrackingService::class.java)
            )
        }

        onDispose {
            mapTapHandler.release()
            userLocationManager.release()
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showSettings = true },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Настройки"
                )
            }
            FloatingActionButton(
                onClick = { userLocationManager.moveToUserLocation() }
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Моя локация"
                )
            }
        }
    }

    selectedEntrance?.let { entrance ->
        NoteBottomSheet(
            address = entrance.address,
            building = entrance.building,
            entrance = entrance.entrance,
            lat = entrance.lat,
            lng = entrance.lng,
            openedAt = entrance.openedAt,
            apiService = apiService,
            onDismiss = {
                selectedEntrance = null
                mapView.mapWindow.map.deselectGeoObject()
            }
        )
    }

    if (showSettings) {
        SettingsBottomSheet(
            settingsManager = settingsManager,
            onDismiss = {
                showSettings = false
                userLocationManager.updateRadius()
            }
        )
    }
}