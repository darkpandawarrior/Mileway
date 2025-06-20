package com.miletracker.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.miletracker.core.ui.components.bottombar.EnhancedBottomBar
import com.miletracker.core.ui.components.bottombar.EnhancedBottomNavItem
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.feature.logging.ui.navigation.LoggingRoutes
import com.miletracker.feature.logging.ui.navigation.loggingGraph
import com.miletracker.feature.media.ui.navigation.MediaRoutes
import com.miletracker.feature.media.ui.navigation.mediaGraph
import com.miletracker.feature.profile.ui.navigation.ProfileRoutes
import com.miletracker.feature.profile.ui.navigation.profileGraph
import com.miletracker.feature.tracking.ui.navigation.TrackingRoutes
import com.miletracker.feature.tracking.ui.navigation.trackingGraph
import org.koin.compose.koinInject

private data class TabSpec(
    val graphRoute: String,
    val startRoute: String,
    val item: EnhancedBottomNavItem
)

/**
 * Single-Activity bottom-navigation shell. Hosts every feature's nested nav graph and
 * applies the app theme exactly once, honouring the runtime [ThemeController] override.
 */
@Composable
fun MileTrackerAppRoot(themeController: ThemeController = koinInject()) {
    val systemDark = isSystemInDarkTheme()
    val override by themeController.darkThemeOverride.collectAsStateWithLifecycle()

    MileTrackerTheme(darkTheme = override ?: systemDark) {
        val navController = rememberNavController()

        val tabs = remember {
            listOf(
                TabSpec(
                    AppGraph.TRACK, TrackingRoutes.SAVED_TRACKS,
                    EnhancedBottomNavItem("Track", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
                ),
                TabSpec(
                    AppGraph.LOG, LoggingRoutes.HOME,
                    EnhancedBottomNavItem("Log", Icons.Filled.Edit, Icons.Outlined.Edit)
                ),
                TabSpec(
                    AppGraph.MEDIA, MediaRoutes.SELECTION,
                    EnhancedBottomNavItem("Capture", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt)
                ),
                TabSpec(
                    AppGraph.PROFILE, ProfileRoutes.HOME,
                    EnhancedBottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
                )
            )
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val selectedIndex = tabs.indexOfFirst { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.graphRoute } == true
        }.coerceAtLeast(0)

        Scaffold(
            bottomBar = {
                EnhancedBottomBar(
                    items = tabs.map { it.item },
                    selectedItemIndex = selectedIndex,
                    onItemSelected = { index ->
                        val tab = tabs[index]
                        navController.navigate(tab.graphRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppGraph.TRACK,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                navigation(startDestination = TrackingRoutes.SAVED_TRACKS, route = AppGraph.TRACK) {
                    trackingGraph(navController)
                }
                navigation(startDestination = LoggingRoutes.HOME, route = AppGraph.LOG) {
                    loggingGraph(navController)
                }
                navigation(startDestination = MediaRoutes.SELECTION, route = AppGraph.MEDIA) {
                    mediaGraph(navController)
                }
                navigation(startDestination = ProfileRoutes.HOME, route = AppGraph.PROFILE) {
                    profileGraph(navController)
                }
            }
        }
    }
}
