package com.miletracker.feature.tracking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miletracker.core.ui.AppHost
import com.miletracker.feature.tracking.ui.screens.HardwareEventsLogScreen
import com.miletracker.feature.tracking.ui.screens.LiveTrackScreen
import com.miletracker.feature.tracking.ui.screens.LocationMapScreen
import com.miletracker.feature.tracking.ui.screens.SavedTracksScreen
import com.miletracker.feature.tracking.ui.screens.TrackDetailScreen
import com.miletracker.feature.tracking.ui.screens.TrackInsightsScreen
import com.miletracker.feature.tracking.ui.screens.TrackMilesScreen
import com.miletracker.feature.tracking.ui.screens.TrackSubmissionScreen
import com.miletracker.feature.tracking.ui.screens.TrackingSuccessScreen

object TrackingRoutes {
    const val SAVED_TRACKS = "saved_tracks"
    const val LIVE_TRACK = "live_track/{routeId}"
    const val LIVE_MAP = "live_map/{routeId}"
    const val DETAIL = "detail/{routeId}"
    const val INSIGHTS = "insights/{routeId}"
    const val HW_EVENTS = "hw_events/{routeId}"
    const val ROUTE_MAP = "route_map/{routeId}"
    const val SUBMIT = "submit/{routeId}?distanceKm={distanceKm}&vehicleKey={vehicleKey}&startTime={startTime}&endTime={endTime}"
    const val SUCCESS = "success?distanceKm={distanceKm}&reimbursable={reimbursable}&vehicleKey={vehicleKey}&startTime={startTime}&endTime={endTime}&transId={transId}"

    fun liveTrack(routeId: String) = "live_track/$routeId"
    fun liveMap(routeId: String) = "live_map/$routeId"
    fun detail(routeId: String) = "detail/$routeId"
    fun insights(routeId: String) = "insights/$routeId"
    fun hwEvents(routeId: String) = "hw_events/$routeId"
    fun routeMap(routeId: String) = "route_map/$routeId"
    fun submit(routeId: String, distanceKm: Double, vehicleKey: String, startTime: Long, endTime: Long) =
        "submit/$routeId?distanceKm=$distanceKm&vehicleKey=$vehicleKey&startTime=$startTime&endTime=$endTime"
    fun success(distanceKm: Double, reimbursable: Double, vehicleKey: String, startTime: Long, endTime: Long, transId: String?) =
        "success?distanceKm=$distanceKm&reimbursable=$reimbursable&vehicleKey=$vehicleKey&startTime=$startTime&endTime=$endTime&transId=${transId ?: ""}"
}

/**
 * Standalone host used by [com.miletracker.feature.tracking.TrackMilesActivity].
 * Applies the theme itself. When hosted inside the app shell, use [trackingGraph]
 * directly instead so theming is applied only once at the root.
 */
@Composable
fun TrackingNavHost(navController: NavHostController = rememberNavController()) {
    AppHost {
        NavHost(navController = navController, startDestination = TrackingRoutes.SAVED_TRACKS) {
            trackingGraph(navController)
        }
    }
}

/**
 * The tracking destinations as a reusable nav-graph builder so the app shell can host
 * them inside a nested graph without an inner [NavHost] or duplicate theming.
 */
fun NavGraphBuilder.trackingGraph(navController: NavHostController) {

            composable(TrackingRoutes.SAVED_TRACKS) {
                SavedTracksScreen(
                    onTrackClick = { routeId -> navController.navigate(TrackingRoutes.detail(routeId)) },
                    onStartNew = {
                        val newId = java.util.UUID.randomUUID().toString()
                        navController.navigate(TrackingRoutes.liveTrack(newId))
                    }
                )
            }

            composable(
                route = TrackingRoutes.LIVE_TRACK,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                val routeId = backStack.arguments?.getString("routeId") ?: return@composable
                TrackMilesScreen(
                    onStop = { id, distKm, vehicleKey, startTime, endTime ->
                        navController.navigate(TrackingRoutes.submit(id, distKm, vehicleKey, startTime, endTime)) {
                            popUpTo(TrackingRoutes.SAVED_TRACKS)
                        }
                    },
                    onOpenMap = { navController.navigate(TrackingRoutes.liveMap(routeId)) },
                    onOpenHwEvents = { navController.navigate(TrackingRoutes.hwEvents(routeId)) }
                )
            }

            composable(
                route = TrackingRoutes.LIVE_MAP,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                backStack.arguments?.getString("routeId") ?: return@composable
                LocationMapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = TrackingRoutes.DETAIL,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                val routeId = backStack.arguments?.getString("routeId") ?: return@composable
                TrackDetailScreen(
                    routeId = routeId,
                    onBack = { navController.popBackStack() },
                    onOpenInsights = { navController.navigate(TrackingRoutes.insights(routeId)) },
                    onOpenMap = { navController.navigate(TrackingRoutes.routeMap(routeId)) },
                    onOpenHwEvents = { navController.navigate(TrackingRoutes.hwEvents(routeId)) }
                )
            }

            composable(
                route = TrackingRoutes.INSIGHTS,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                val routeId = backStack.arguments?.getString("routeId") ?: return@composable
                TrackInsightsScreen(routeId = routeId, onBack = { navController.popBackStack() })
            }

            composable(
                route = TrackingRoutes.HW_EVENTS,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                val routeId = backStack.arguments?.getString("routeId") ?: return@composable
                HardwareEventsLogScreen(routeId = routeId, onBack = { navController.popBackStack() })
            }

            composable(
                route = TrackingRoutes.ROUTE_MAP,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) { backStack ->
                backStack.arguments?.getString("routeId") ?: return@composable
                LocationMapScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = TrackingRoutes.SUBMIT,
                arguments = listOf(
                    navArgument("routeId") { type = NavType.StringType },
                    navArgument("distanceKm") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("vehicleKey") { type = NavType.StringType; defaultValue = "" },
                    navArgument("startTime") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("endTime") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { backStack ->
                val args = backStack.arguments!!
                val routeId = args.getString("routeId")!!
                TrackSubmissionScreen(
                    routeId = routeId,
                    distanceKm = args.getFloat("distanceKm").toDouble(),
                    vehicleKey = args.getString("vehicleKey") ?: "",
                    startTime = args.getLong("startTime"),
                    endTime = args.getLong("endTime"),
                    onSuccess = { distKm, reimbursable, vehicleKey, startTime, endTime, transId ->
                        navController.navigate(
                            TrackingRoutes.success(distKm, reimbursable, vehicleKey, startTime, endTime, transId)
                        ) {
                            popUpTo(TrackingRoutes.SAVED_TRACKS)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = TrackingRoutes.SUCCESS,
                arguments = listOf(
                    navArgument("distanceKm") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("reimbursable") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("vehicleKey") { type = NavType.StringType; defaultValue = "" },
                    navArgument("startTime") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("endTime") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("transId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val args = backStack.arguments!!
                val transId = args.getString("transId").orEmpty().ifBlank { null }
                TrackingSuccessScreen(
                    distanceKm = args.getFloat("distanceKm").toDouble(),
                    reimbursableAmount = args.getFloat("reimbursable").toDouble(),
                    vehicleKey = args.getString("vehicleKey") ?: "",
                    startTime = args.getLong("startTime"),
                    endTime = args.getLong("endTime"),
                    transId = transId,
                    onViewSavedTracks = {
                        navController.navigate(TrackingRoutes.SAVED_TRACKS) {
                            popUpTo(TrackingRoutes.SAVED_TRACKS) { inclusive = true }
                        }
                    },
                    onHome = { navController.popBackStack(TrackingRoutes.SAVED_TRACKS, false) }
                )
            }
}
