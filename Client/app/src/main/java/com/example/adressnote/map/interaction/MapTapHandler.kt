package com.example.adressnote.map.interaction

import android.util.Log
import com.example.adressnote.network.GeocoderService
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.Map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapTapHandler(
    private val map: Map,
    private val geocoderService: GeocoderService,
    private val scope: CoroutineScope,
    private val onEntranceTapped: (address: String, building: String, entrance: String, lat: Double, lng: Double) -> Unit
) : GeoObjectTapListener {

    init {
        map.addTapListener(this)
    }

    override fun onObjectTap(event: GeoObjectTapEvent): Boolean {
        val geoObject = event.geoObject
        val entrance = geoObject.name

        Log.d("MapTapHandler", "=== TAP ===")
        Log.d("MapTapHandler", "name: $entrance")

        if (entrance == null) {
            Log.d("MapTapHandler", "Тап не на подъезд - игнорируем")
            return false
        }

        val point = geoObject.geometry.firstOrNull()?.point
        if (point == null) {
            Log.d("MapTapHandler", "Нет координат - игнорируем")
            return false
        }

        Log.d("MapTapHandler", "lat=${point.latitude}, lng=${point.longitude}")

        val selectionMetadata = geoObject.metadataContainer
            .getItem(com.yandex.mapkit.map.GeoObjectSelectionMetadata::class.java)
        if (selectionMetadata != null) {
            map.selectGeoObject(selectionMetadata)
        }

        scope.launch(Dispatchers.Main) {
            val result = geocoderService.getAddress(point.latitude, point.longitude)
            if (result == null) {
                Log.e("MapTapHandler", "Геокодер вернул null")
                return@launch
            }
            Log.d("MapTapHandler", "address: ${result.address}")
            Log.d("MapTapHandler", "building: ${result.building}")
            Log.d("MapTapHandler", "entrance: $entrance")
            onEntranceTapped(result.address, result.building, entrance, point.latitude, point.longitude)
        }

        return true
    }

    fun release() {
        map.removeTapListener(this)
    }
}