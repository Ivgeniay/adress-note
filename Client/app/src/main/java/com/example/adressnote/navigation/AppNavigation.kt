package com.example.adressnote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adressnote.permissions.StartScreenPermissions
import com.example.adressnote.ui.screens.MapScreen
import com.example.adressnote.ui.screens.StartScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            StartScreenPermissions(
                onAllGranted = {
                    StartScreen(onStartClick = { navController.navigate("map") })
                }
            )
        }
        composable("map") {
            MapScreen()
        }
    }
}