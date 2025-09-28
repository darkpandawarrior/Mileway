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
import com.miletracker.feature.tracking.ui.screens.CheckInHistoryItem
import com.miletracker.feature.tracking.ui.screens.CheckInHistoryScreen
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
    const val CHECK_IN_HISTORY = "check_in_history"
    const val ROUTE_MAP = "route_map/{routeId}"
    const val SUBMIT = "submit/{routeId}?distanceKm={distanceKm}&vehicleKey={vehicleKey}&startTime={startTime}&endTime={endTime}"
    const val SUCCESS = "success?distanceKm={distanceKm}&reimbursable={reimbursable}&vehicleName={vehicleName}" +
        "&startTime={startTime}&endTime={endTime}&transId={transId}&status={status}" +
        "&violationCount={violationCount}&violationMsg={violationMsg}" +
        "&voucherNumber={voucherNumber}&voucherAmount={voucherAmount}"

    fun liveTrack(routeId: String) = "live_track/$routeId"
    fun liveMap(routeId: String) = "live_map/$routeId"
    fun detail(routeId: String) = "detail/$routeId"
    fun insights(routeId: String) = "insights/$routeId"
    fun hwEvents(routeId: String) = "hw_events/$routeId"
    fun routeMap(routeId: String) = "route_map/$routeId"
    fun submit(routeId: String, distanceKm: Double, vehicleKey: String, startTime: Long, endTime: Long) =
        "submit/$routeId?distanceKm=$distanceKm&vehicleKey=$vehicleKey&startTime=$startTime&endTime=$endTime"

    fun success(r: SubmissionResult): String {
        fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
        return "success?distanceKm=${r.distanceKm}&reimbursable=${r.reimbursableAmount}" +
            "&vehicleName=${enc(r.vehicleName)}&startTime=${r.startTime}&endTime=${r.endTime}" +
            "&transId=${enc(r.transactionId ?: "")}&status=${r.submissionStatus}" +
            "&violationCount=${r.violationCount}&violationMsg=${enc(r.violationMessage ?: "")}" +
            "&voucherNumber=${enc(r.voucherNumber ?: "")}&voucherAmount=${r.voucherAmount}"
    }
}

/** Everything the success screen needs, produced by the submission screen on submit. */
data class SubmissionResult(
    val distanceKm: Double,
    val reimbursableAmount: Double,
    val vehicleName: String,
    val startTime: Long,
    val endTime: Long,
    val transactionId: String?,
    val submissionStatus: String,
    val violationCount: Int,
    val violationMessage: String?,
    val voucherNumber: String?,
    val voucherAmount: Double,
)

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
                    onOpenHwEvents = { navController.navigate(TrackingRoutes.hwEvents(routeId)) },
                    onOpenCheckInHistory = { navController.navigate(TrackingRoutes.CHECK_IN_HISTORY) }
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

            composable(TrackingRoutes.CHECK_IN_HISTORY) {
                CheckInHistoryScreen(
                    events = DemoCheckInHistory.items,
                    onBack = { navController.popBackStack() }
                )
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
                    onSuccess = { result ->
                        navController.navigate(TrackingRoutes.success(result)) {
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
                    navArgument("vehicleName") { type = NavType.StringType; defaultValue = "" },
                    navArgument("startTime") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("endTime") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("transId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("status") { type = NavType.StringType; defaultValue = "SUCCESS" },
                    navArgument("violationCount") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("violationMsg") { type = NavType.StringType; defaultValue = "" },
                    navArgument("voucherNumber") { type = NavType.StringType; defaultValue = "" },
                    navArgument("voucherAmount") { type = NavType.FloatType; defaultValue = 0f }
                )
            ) { backStack ->
                val args = backStack.arguments!!
                fun dec(s: String?) = s.orEmpty().let { java.net.URLDecoder.decode(it, "UTF-8") }
                val toSaved = {
                    navController.navigate(TrackingRoutes.SAVED_TRACKS) {
                        popUpTo(TrackingRoutes.SAVED_TRACKS) { inclusive = true }
                    }
                }
                TrackingSuccessScreen(
                    distanceKm = args.getFloat("distanceKm").toDouble(),
                    reimbursableAmount = args.getFloat("reimbursable").toDouble(),
                    vehicleName = dec(args.getString("vehicleName")),
                    startTime = args.getLong("startTime"),
                    endTime = args.getLong("endTime"),
                    transactionId = dec(args.getString("transId")).ifBlank { null },
                    submissionStatus = args.getString("status") ?: "SUCCESS",
                    violationCount = args.getInt("violationCount"),
                    violationMessage = dec(args.getString("violationMsg")).ifBlank { null },
                    voucherNumber = dec(args.getString("voucherNumber")).ifBlank { null },
                    voucherAmount = args.getFloat("voucherAmount").toDouble(),
                    onTrackNewJourney = toSaved,
                    onViewExpense = toSaved,
                    onCreateVoucher = {},
                )
            }
}

private object DemoCheckInHistory {
    private const val DAY_MS = 86_400_000L
    private const val HOUR_MS = 3_600_000L
    private const val BASE = 1_718_200_000_000L

    val items = listOf(
        CheckInHistoryItem("CI-001", "Speedline Transport Co.", "Client visit – Q2 review", BASE, 18.5204, 73.8567, "Client visit", false),
        CheckInHistoryItem("CI-002", "Metro Cargo Movers", "Pickup confirmation", BASE - 2 * HOUR_MS, 18.5480, 73.8718, "Pickup", false),
        CheckInHistoryItem("CI-003", "CityLink Telecom Services", "SIM card bulk order", BASE - 5 * HOUR_MS, 18.5601, 73.8234, "Delivery", true),
        CheckInHistoryItem("CI-004", "Eastern Freight Lines", null, BASE - DAY_MS, 18.5120, 73.9012, "Geo check-in", false),
        CheckInHistoryItem("CI-005", "Southside Auto Works", "Vehicle service drop-off", BASE - DAY_MS - 3 * HOUR_MS, 18.4890, 73.8350, "Service", true),
        CheckInHistoryItem("CI-006", "Westgate Fleet Services", "Fleet inspection", BASE - 2 * DAY_MS, 18.5913, 73.7389, "Inspection", false),
        CheckInHistoryItem("CI-007", "Hadapsar Logistics Park", null, BASE - 3 * DAY_MS, 18.5089, 73.9260, "Geo check-in", false),
        CheckInHistoryItem("CI-008", "Speedline Transport Co.", "Follow-up delivery", BASE - 4 * DAY_MS, 18.5204, 73.8567, "Delivery", true),
        CheckInHistoryItem("CI-009", "Metro Cargo Movers", "Returns processing", BASE - 6 * DAY_MS, 18.5480, 73.8718, "Returns", false),
        CheckInHistoryItem("CI-010", "CityLink Telecom Services", "Invoice collection", BASE - 8 * DAY_MS, 18.5601, 73.8234, "Invoice", true),
    )
}
