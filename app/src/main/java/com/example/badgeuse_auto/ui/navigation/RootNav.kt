package com.example.badgeuse_auto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.SettingsViewModel
import com.example.badgeuse_auto.ui.screens.MainScreen
import com.example.badgeuse_auto.ui.screens.StatisticsScreen
import com.example.badgeuse_auto.ui.screens.WorkLocationScreen
import com.example.badgeuse_auto.ui.screens.SettingsScreen

object Destinations {
    const val MAIN = "main"
    const val STATS = "stats"
    const val WORK_LOCATION = "work_location"
    const val SETTINGS = "settings"
}

@Composable
fun RootNav(
    presenceViewModel: PresenceViewModel,
    settingsViewModel: SettingsViewModel,
    onGeofenceUpdate: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN
    ) {

        // ---------------------------------------------------------------------
        // MAIN SCREEN
        // ---------------------------------------------------------------------
        composable(Destinations.MAIN) {
            MainScreen(
                viewModel = presenceViewModel,
                onNavigateStats = { navController.navigate(Destinations.STATS) },
                onNavigateWorkLocation = { navController.navigate(Destinations.WORK_LOCATION) },
                onNavigateSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        // ---------------------------------------------------------------------
        // STATS SCREEN
        // ---------------------------------------------------------------------
        composable(Destinations.STATS) {
            StatisticsScreen(
                viewModel = presenceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        // ---------------------------------------------------------------------
        // WORK LOCATION SCREEN
        // ---------------------------------------------------------------------
        composable(Destinations.WORK_LOCATION) {
            WorkLocationScreen(
                viewModel = presenceViewModel,
                onBack = { navController.popBackStack() },
                onWorkLocationSaved = { onGeofenceUpdate() }
            )
        }

        // ---------------------------------------------------------------------
        // SETTINGS SCREEN
        // ---------------------------------------------------------------------
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
