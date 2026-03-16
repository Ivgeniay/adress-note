package com.example.adressnote.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Note(
    val address: String,
    val building: String,
    val entrance: String,
    val note: String,
    val lat: Double,
    val lng: Double
)

class NotesApiService(private val baseUrl: String) {

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    suspend fun getAll(): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL("$baseUrl/notes").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = org.json.JSONArray(response)
                val notes = mutableListOf<Note>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    notes.add(
                        Note(
                            address = json.getString("address"),
                            building = json.getString("building"),
                            entrance = json.getString("entrance"),
                            note = json.getString("note"),
                            lat = json.getDouble("lat"),
                            lng = json.getDouble("lng")
                        )
                    )
                }
                notes
            } catch (e: Exception) {
                Log.e("NotesApiService", "getAll error: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun getNote(address: String, building: String, entrance: String): Note? {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "$baseUrl/notes/${encode(address)}/${encode(building)}/${encode(entrance)}"
                Log.d("NotesApiService", "getNote URL: $urlString")
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                Log.d("NotesApiService", "getNote responseCode: $responseCode")

                if (responseCode == 404) return@withContext null

                val response = connection.inputStream.bufferedReader().readText()
                Log.d("NotesApiService", "getNote response: $response")

                val json = JSONObject(response)
                Note(
                    address = json.getString("address"),
                    building = json.getString("building"),
                    entrance = json.getString("entrance"),
                    note = json.getString("note"),
                    lat = json.getDouble("lat"),
                    lng = json.getDouble("lng")
                )
            } catch (e: Exception) {
                Log.e("NotesApiService", "getNote error: ${e.message}", e)
                null
            }
        }
    }

    suspend fun getNearby(lat: Double, lng: Double, radius: Double): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/notes/nearby?lat=$lat&lng=$lng&radius=$radius")
                Log.d("NotesApiService", "getNearby URL: $url")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                Log.d("NotesApiService", "getNearby responseCode: $responseCode")

                if (responseCode != 200) return@withContext emptyList()

                val response = connection.inputStream.bufferedReader().readText()
                Log.d("NotesApiService", "getNearby response: $response")

                val jsonArray = org.json.JSONArray(response)
                val notes = mutableListOf<Note>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    notes.add(
                        Note(
                            address = json.getString("address"),
                            building = json.getString("building"),
                            entrance = json.getString("entrance"),
                            note = json.getString("note"),
                            lat = json.getDouble("lat"),
                            lng = json.getDouble("lng")
                        )
                    )
                }
                notes
            } catch (e: Exception) {
                Log.e("NotesApiService", "getNearby error: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun saveNote(note: Note): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existing = getNote(note.address, note.building, note.entrance)
                val method = if (existing != null) "PUT" else "POST"

                val urlString = if (method == "PUT")
                    "$baseUrl/notes/${encode(note.address)}/${encode(note.building)}/${encode(note.entrance)}"
                else
                    "$baseUrl/notes"

                Log.d("NotesApiService", "saveNote URL: $urlString method: $method")

                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val body = if (method == "PUT") {
                    JSONObject().apply { put("note", note.note) }.toString()
                } else {
                    JSONObject().apply {
                        put("address", note.address)
                        put("building", note.building)
                        put("entrance", note.entrance)
                        put("note", note.note)
                        put("lat", note.lat)
                        put("lng", note.lng)
                    }.toString()
                }

                Log.d("NotesApiService", "saveNote body: $body")

                OutputStreamWriter(connection.outputStream).use { it.write(body) }
                val responseCode = connection.responseCode
                Log.d("NotesApiService", "saveNote responseCode: $responseCode")
                responseCode in 200..201
            } catch (e: Exception) {
                Log.e("NotesApiService", "saveNote error: ${e.message}", e)
                false
            }
        }
    }

    suspend fun deleteNote(address: String, building: String, entrance: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "$baseUrl/notes/${encode(address)}/${encode(building)}/${encode(entrance)}"
                Log.d("NotesApiService", "deleteNote URL: $urlString")
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val responseCode = connection.responseCode
                Log.d("NotesApiService", "deleteNote responseCode: $responseCode")
                responseCode == 200
            } catch (e: Exception) {
                Log.e("NotesApiService", "deleteNote error: ${e.message}", e)
                false
            }
        }
    }
}