package com.miletracker.feature.logging.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.miletracker.feature.logging.ui.screens.LogMilesHistoryScreen
import com.miletracker.feature.logging.ui.screens.LogMilesScreen
import com.miletracker.feature.logging.ui.screens.LogMilesStep2Screen
import com.miletracker.feature.logging.ui.screens.LogMilesSuccessScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Route constants for the Log Miles flow.
 *
 * [HOME] remains the canonical entry (Step 1) so the app shell hosts the same
 * destination as before. The remaining routes are pushed full-screen flows.
 */
object LoggingRoutes {
    /** Step 1 — journey basics + travelled locations (top-level tab). */
    const val HOME = "log_miles"

    /** Step 2 — expense details + submission. */
    const val STEP2 = "log_miles/step2"

    /** Post-submission success route. */
    const val SUCCESS = "log_miles/success"

    /** Drafts + submitted history. */
    const val HISTORY = "log_miles/history"
}

/**
 * Logging destinations as a reusable nav-graph builder so the app shell can host
 * the Log Miles flow inside a nested graph.
 *
 * The two-step flow shares a single [com.miletracker.feature.logging.viewmodel.LogMilesViewModel]
 * across Step 1, Step 2 and the success route by resolving it from the back-stack
 * entry of the [LoggingRoutes.HOME] destination — so the itinerary built on Step 1
 * is still present on Step 2 and the submission result on the success screen. The
 * History screen reads the same shared state (drafts/submitted) the same way.
 *
 * [LogMilesScreen] still resolves its own ViewModel by default, so the public
 * entry signature is unchanged for existing callers.
 */
fun NavGraphBuilder.loggingGraph(navController: NavHostController) {
    composable(LoggingRoutes.HOME) { entry ->
        // Anchor the shared VM to the HOME back-stack entry.
        val viewModel = koinViewModel<com.miletracker.feature.logging.viewmodel.LogMilesViewModel>(
            viewModelStoreOwner = entry
        )
        LogMilesScreen(
            viewModel = viewModel,
            onNext = { navController.navigate(LoggingRoutes.STEP2) },
            onOpenHistory = { navController.navigate(LoggingRoutes.HISTORY) }
        )
    }

    composable(LoggingRoutes.STEP2) {
        val homeEntry = rememberHomeEntry(navController)
        val viewModel = koinViewModel<com.miletracker.feature.logging.viewmodel.LogMilesViewModel>(
            viewModelStoreOwner = homeEntry
        )
        LogMilesStep2Screen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onSubmitted = {
                navController.navigate(LoggingRoutes.SUCCESS) {
                    // Drop Step 2 so Back from success returns to a fresh Step 1.
                    popUpTo(LoggingRoutes.STEP2) { inclusive = true }
                }
            }
        )
    }

    composable(LoggingRoutes.SUCCESS) {
        val homeEntry = rememberHomeEntry(navController)
        val viewModel = koinViewModel<com.miletracker.feature.logging.viewmodel.LogMilesViewModel>(
            viewModelStoreOwner = homeEntry
        )
        LogMilesSuccessScreen(
            viewModel = viewModel,
            onLogAnother = {
                navController.navigate(LoggingRoutes.HOME) {
                    popUpTo(LoggingRoutes.HOME) { inclusive = true }
                }
            }
        )
    }

    composable(LoggingRoutes.HISTORY) {
        val homeEntry = rememberHomeEntry(navController)
        val viewModel = koinViewModel<com.miletracker.feature.logging.viewmodel.LogMilesViewModel>(
            viewModelStoreOwner = homeEntry
        )
        LogMilesHistoryScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onOpenDraft = {
                // Re-opening a draft returns to Step 1 where the itinerary lives.
                navController.popBackStack(LoggingRoutes.HOME, inclusive = false)
            }
        )
    }
}

/**
 * Resolves the [LoggingRoutes.HOME] back-stack entry so deeper destinations share
 * the same ViewModel store as Step 1. Falls back to the current entry if HOME is
 * not on the back stack (defensive — should not happen within this flow).
 */
@androidx.compose.runtime.Composable
private fun rememberHomeEntry(navController: NavHostController) =
    androidx.compose.runtime.remember(navController.currentBackStackEntry) {
        runCatching { navController.getBackStackEntry(LoggingRoutes.HOME) }.getOrNull()
    } ?: navController.currentBackStackEntry!!
