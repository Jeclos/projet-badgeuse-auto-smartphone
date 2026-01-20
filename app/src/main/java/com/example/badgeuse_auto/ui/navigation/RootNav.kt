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
import com.example.badgeuse_auto.ui.location.LocationViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.badgeuse_auto.export.PdfPreviewScreen
import com.example.badgeuse_auto.export.sharePdf
import com.example.badgeuse_auto.export.printPdf


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
    locationViewModel: LocationViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN
    ) {

        composable(Destinations.MAIN) {
            MainScreen(
                viewModel = presenceViewModel,
                locationViewModel = locationViewModel,
                onNavigateStats = {
                    navController.navigate(Destinations.STATS)
                },
                onNavigateSettings = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(Destinations.STATS) {
            StatisticsScreen(
                viewModel = presenceViewModel,
                navController = navController, // 👈 LIGNE À AJOUTER
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                presenceViewModel = presenceViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "pdfPreview/{pdfPath}",
            arguments = listOf(
                navArgument("pdfPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val pdfPath = backStackEntry.arguments!!.getString("pdfPath")!!
            val context = LocalContext.current

            PdfPreviewScreen(
                pdfPath = pdfPath,
                onShare = { file -> sharePdf(context, file) },
                onPrint = { file -> printPdf(context, file) }
            )
        }

    }
}
