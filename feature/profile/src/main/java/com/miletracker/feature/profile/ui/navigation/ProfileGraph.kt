package com.miletracker.feature.profile.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.profile.ui.screens.HelpScreen
import com.miletracker.feature.profile.ui.screens.PreferencesScreen
import com.miletracker.feature.profile.ui.screens.ProfileDetailsScreen
import com.miletracker.feature.profile.ui.screens.ProfileScreen
import com.miletracker.feature.profile.ui.screens.SettingsScreen

object ProfileRoutes {
    const val HOME = "profile_home"
    const val DETAILS = "profile_details"
    const val PREFERENCES = "preferences"
    const val SETTINGS = "profile_settings"
    const val HELP = "profile_help"
}

/**
 * Profile nav graph.
 *
 * The Account hub ([ProfileScreen]) routes into the new full-detail surfaces
 * ([ProfileDetailsScreen], [PreferencesScreen]) and the existing Settings/Help routes.
 * Notifications and About & Support reuse the Help route in this demo.
 *
 * [onOpenDebugMenu] is a callback supplied by the app shell so the profile
 * module itself does not need to depend on :feature:tracking.
 */
fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onOpenDebugMenu: () -> Unit = {},
) {
    composable(ProfileRoutes.HOME) {
        ProfileScreen(
            onOpenDetails = { navController.navigate(ProfileRoutes.DETAILS) },
            onOpenPreferences = { navController.navigate(ProfileRoutes.PREFERENCES) },
            onOpenNotifications = { navController.navigate(ProfileRoutes.HELP) },
            onOpenSettings = { navController.navigate(ProfileRoutes.SETTINGS) },
            onOpenAboutSupport = { navController.navigate(ProfileRoutes.HELP) },
        )
    }
    composable(ProfileRoutes.DETAILS) {
        ProfileDetailsScreen(
            onBack = { navController.popBackStack() },
        )
    }
    composable(ProfileRoutes.PREFERENCES) {
        PreferencesScreen(
            onBack = { navController.popBackStack() },
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
