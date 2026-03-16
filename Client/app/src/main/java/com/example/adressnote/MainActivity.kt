package com.example.adressnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.adressnote.navigation.AppNavigation
import com.example.adressnote.ui.theme.AdressNoteTheme
import com.yandex.mapkit.MapKitFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        setContent {
            AdressNoteTheme {
                AppNavigation()
            }
        }
    }
}