package com.miletracker.feature.profile.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.profile.ui.screens.ProfileScreen
import com.miletracker.feature.profile.ui.screens.SettingsScreen

object ProfileRoutes {
    const val HOME = "profile_home"
    const val SETTINGS = "profile_settings"
}

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable(ProfileRoutes.HOME) {
        ProfileScreen(
            onOpenSettings = { navController.navigate(ProfileRoutes.SETTINGS) },
        )
    }
    composable(ProfileRoutes.SETTINGS) {
        SettingsScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
