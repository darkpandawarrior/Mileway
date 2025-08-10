package com.miletracker.feature.profile.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.profile.ui.screens.HelpScreen
import com.miletracker.feature.profile.ui.screens.ProfileScreen
import com.miletracker.feature.profile.ui.screens.SettingsScreen

object ProfileRoutes {
    const val HOME = "profile_home"
    const val SETTINGS = "profile_settings"
    const val HELP = "profile_help"
}

/**
 * Profile nav graph.
 * [onOpenDebugMenu] is a callback supplied by the app shell so the profile
 * module itself does not need to depend on :feature:tracking.
 */
fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onOpenDebugMenu: () -> Unit = {},
) {
    composable(ProfileRoutes.HOME) {
        ProfileScreen(
            onOpenSettings = { navController.navigate(ProfileRoutes.SETTINGS) },
            onOpenHelp = { navController.navigate(ProfileRoutes.HELP) },
        )
    }
    composable(ProfileRoutes.SETTINGS) {
        SettingsScreen(
            onBack = { navController.popBackStack() },
            onOpenDebugMenu = onOpenDebugMenu,
        )
    }
    composable(ProfileRoutes.HELP) {
        HelpScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
