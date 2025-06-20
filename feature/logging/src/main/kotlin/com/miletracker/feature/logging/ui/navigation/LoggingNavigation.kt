package com.miletracker.feature.logging.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.logging.ui.screens.LogMilesScreen

object LoggingRoutes {
    const val HOME = "log_miles"
}

/**
 * Logging destinations as a reusable nav-graph builder so the app shell can host
 * the Log Miles screen inside a nested graph. [LogMilesScreen] resolves its own
 * ViewModel via Koin, so no arguments need to be threaded through navigation.
 */
fun NavGraphBuilder.loggingGraph(@Suppress("UNUSED_PARAMETER") navController: NavHostController) {
    composable(LoggingRoutes.HOME) {
        LogMilesScreen()
    }
}
