package com.example.adressnote.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class GeocoderResult(
    val address: String,
    val building: String
)

class GeocoderService(private val apiKey: String) {

    suspend fun getAddress(lat: Double, lng: Double): GeocoderResult? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://geocode-maps.yandex.ru/1.x/" +
                        "?apikey=$apiKey" +
                        "&geocode=$lng,$lat" +
                        "&format=json" +
                        "&results=1"

                val response = URL(url).readText()
                val json = JSONObject(response)

                val featureMember = json
                    .getJSONObject("response")
                    .getJSONObject("GeoObjectCollection")
                    .getJSONArray("featureMember")

                if (featureMember.length() == 0) return@withContext null

                val geoObject = featureMember
                    .getJSONObject(0)
                    .getJSONObject("GeoObject")

                val metaData = geoObject
                    .getJSONObject("metaDataProperty")
                    .getJSONObject("GeocoderMetaData")
                    .getJSONObject("Address")

                val components = metaData.getJSONArray("Components")

                var street = ""
                var building = ""

                for (i in 0 until components.length()) {
                    val component = components.getJSONObject(i)
                    when (component.getString("kind")) {
                        "street" -> street = component.getString("name")
                        "house" -> building = component.getString("name")
                    }
                }

                if (street.isEmpty()) return@withContext null

                GeocoderResult(address = street, building = building)
            } catch (e: Exception) {
                Log.e("GeocoderService", "Error: ${e.message}", e)
                null
            }
        }
    }
}