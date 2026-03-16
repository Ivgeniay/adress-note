package com.example.adressnote

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class AdressNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}