package com.example.badgeuse_auto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.ui.screens.MainScreen
import com.example.badgeuse_auto.ui.screens.StatsScreen
import com.example.badgeuse_auto.ui.screens.WorkLocationScreen

object Destinations {
    const val MAIN = "main"
    const val STATS = "stats"
    const val WORK_LOCATION = "work_location"
}

/**
 * root navigation.
 *
 * @param viewModel le ViewModel partagé
 * @param onGeofenceUpdate callback fourni par MainActivity ; appelé quand WorkLocation change
 */
@Composable
fun RootNav(
    viewModel: PresenceViewModel,
    onGeofenceUpdate: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN
    ) {
        composable(Destinations.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onNavigateStats = { navController.navigate(Destinations.STATS) },
                onNavigateWorkLocation = { navController.navigate(Destinations.WORK_LOCATION) }
            )
        }

        composable(Destinations.STATS) {
            StatsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.WORK_LOCATION) {
            WorkLocationScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                // quand WorkLocationScreen enregistre ou supprime, on rappelle le callback
                onWorkLocationSaved = {
                    onGeofenceUpdate()
                }
            )
        }
    }
}
