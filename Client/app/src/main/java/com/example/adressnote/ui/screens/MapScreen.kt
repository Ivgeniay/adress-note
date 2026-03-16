package com.example.adressnote.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adressnote.BuildConfig
import com.example.adressnote.map.interaction.MapTapHandler
import com.example.adressnote.map.location.UserLocationManager
import com.example.adressnote.network.GeocoderService
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }
    val userLocationManager = remember { UserLocationManager(context, mapView) }
    val geocoderService = remember { GeocoderService(BuildConfig.GEOCODER_API_KEY) }
    val mapTapHandler = remember {
        MapTapHandler(
            map = mapView.mapWindow.map,
            geocoderService = geocoderService,
            scope = scope,
            onEntranceTapped = { address, building, entrance ->
                // TODO: навигация на экран заметки
            }
        )
    }

    DisposableEffect(Unit) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        userLocationManager.init()
        userLocationManager.moveToUserLocation()
        onDispose {
            mapTapHandler.release()
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        FloatingActionButton(
            onClick = { userLocationManager.moveToUserLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Моя локация"
            )
        }
    }
}