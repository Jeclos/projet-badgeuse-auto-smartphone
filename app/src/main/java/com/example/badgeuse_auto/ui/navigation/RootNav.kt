package com.example.badgeuse_auto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.SettingsViewModel
import com.example.badgeuse_auto.ui.screens.MainScreen
import com.example.badgeuse_auto.ui.screens.SettingsScreen
import com.example.badgeuse_auto.ui.screens.StatisticsScreen

/* ----------------------------
   Destinations
   ---------------------------- */

object Destinations {
    const val MAIN = "main"
    const val STATS = "stats"
    const val SETTINGS = "settings"
}

/* ----------------------------
   Root Navigation
   ---------------------------- */

@Composable
fun RootNav(
    presenceViewModel: PresenceViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN
    ) {

        /* ----------------------------
           MAIN SCREEN
           ---------------------------- */
        composable(Destinations.MAIN) {
            MainScreen(
                viewModel = presenceViewModel,
                onNavigateStats = {
                    navController.navigate(Destinations.STATS)
                },
                onNavigateSettings = {
                    navController.navigate(Destinations.SETTINGS)
                },

            )
        }

        /* ----------------------------
           STATS SCREEN
           ---------------------------- */
        composable(Destinations.STATS) {
            StatisticsScreen(
                viewModel = presenceViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        /* ----------------------------
           SETTINGS SCREEN
           ---------------------------- */
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                presenceViewModel = presenceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
