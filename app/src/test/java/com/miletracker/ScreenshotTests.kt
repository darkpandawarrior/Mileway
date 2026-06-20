package com.miletracker

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.miletracker.core.data.dao.HardwareEventDao
import com.miletracker.core.data.dao.LocationDao
import com.miletracker.core.data.dao.LogMilesDraftDao
import com.miletracker.core.data.dao.LogMilesFrequentRouteDao
import com.miletracker.core.data.dao.SavedTrackDao
import com.miletracker.core.data.dao.TripAttachmentDao
import com.miletracker.core.data.library.MediaLibraryDao
import com.miletracker.core.data.model.db.SavedTrack
import com.miletracker.core.data.session.CurrentTrackDataStore
import com.miletracker.core.data.settings.DemoSettingsRepository
import com.miletracker.core.maps.MapSurface
import com.miletracker.core.ui.di.coreUiModule
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.feature.approvals.di.approvalsModule
import com.miletracker.feature.approvals.ui.screens.ApprovalsScreen
import com.miletracker.feature.logging.di.loggingModule
import com.miletracker.feature.logging.ui.screens.LogMilesScreen
import com.miletracker.feature.profile.di.profileModule
import com.miletracker.feature.profile.ui.screens.RootGuardScreen
import com.miletracker.feature.tracking.di.trackingModule
import com.miletracker.feature.tracking.ui.screens.CheckInHistoryScreen
import com.miletracker.feature.tracking.ui.screens.CheckInHistoryItem
import com.miletracker.feature.tracking.ui.screens.GeoCheckInScreen
import com.miletracker.feature.tracking.ui.screens.HardwareEventsLogScreen
import com.miletracker.feature.tracking.ui.screens.SavedTracksScreen
import com.miletracker.feature.tracking.ui.screens.TrackInsightsScreen
import com.miletracker.feature.tracking.ui.screens.TrackMilesScreen
import com.miletracker.feature.tracking.ui.screens.TrackingSuccessScreen
import com.miletracker.stub.di.stubModule
import com.miletracker.ui.home.HomeScreenContent
import com.miletracker.ui.home.HomeUiState
import io.mockk.mockk
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

// Use plain Application to skip MileTrackerApplication.onCreate → startKoin
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTests {

    companion object {
        private val screenshotsDir: File by lazy {
            val moduleDir = File(System.getProperty("user.dir") ?: ".")
            val repoRoot = if (moduleDir.name == "app") moduleDir.parentFile else moduleDir
            File(repoRoot, "docs/screenshots").also { it.mkdirs() }
        }

        // Pre-seeded DAO shared across all tests that use SavedTracksViewModel.
        private val seededDao = FakeSavedTrackDao().also { dao ->
            val baseMs = 1_700_000_000_000L
            dao.preload(completedTrack("route-j1", "Pune → Hinjewadi", 12_400.0, baseMs - 86_400_000L))
            dao.preload(completedTrack("route-j2", "FC Road → Koregaon Park", 3_800.0, baseMs - 172_800_000L))
            dao.preload(completedTrack("route-j3", "Camp → Hadapsar", 7_100.0, baseMs - 259_200_000L))
            dao.preload(submittedTrack("route-s1", "Kothrud → Baner", 9_200.0, baseMs - 432_000_000L))
        }

        private fun completedTrack(routeId: String, name: String, distanceMeters: Double, startMs: Long) =
            SavedTrack(
                routeId = routeId, name = name, isCompleted = true,
                startLatitude = 18.5204, startLongitude = 73.8567,
                endLatitude = 18.5500, endLongitude = 73.8800,
                pausedLatitude = 0.0, pausedLongitude = 0.0,
                startTime = startMs, endTime = startMs + 3_600_000L,
                distance = distanceMeters, duration = 3_600_000L,
                selectedVehicleType = "fourWheelerPetrol", vehiclePricing = 10.0,
                createdAt = startMs, startedAtTimestamp = startMs,
                startedByEmployeeCode = "EMP001"
            )

        private fun submittedTrack(routeId: String, name: String, distanceMeters: Double, startMs: Long) =
            completedTrack(routeId, name, distanceMeters, startMs).copy(
                serverUploaded = true, submittedAmount = distanceMeters / 1000.0 * 10.0,
                submissionTime = startMs + 3_600_000L + 600_000L, pettyId = 9001L
            )

        private val fakeRoomLayer = module {
            single<SavedTrackDao> { seededDao }
            single<LocationDao> { mockk(relaxed = true) }
            single<HardwareEventDao> { mockk(relaxed = true) }
            single<LogMilesDraftDao> { mockk(relaxed = true) }
            single<LogMilesFrequentRouteDao> { mockk(relaxed = true) }
            single<TripAttachmentDao> { mockk(relaxed = true) }
            single<MediaLibraryDao> { mockk(relaxed = true) }
            single<CurrentTrackDataStore> { mockk(relaxed = true) }
            single<DemoSettingsRepository> { mockk(relaxed = true) }
            // Map screens (GeoCheckIn, MapScreen) inject MapSurface; the real flavor
            // surfaces need GMS / MapLibre native, so use a no-op fake on the JVM.
            single<MapSurface> { FakeMapSurface() }
        }

        @BeforeClass @JvmStatic
        fun setup() {
            System.setProperty("roborazzi.test.record", "true")
            try { stopKoin() } catch (_: Exception) {}
            startKoin {
                androidContext(mockk<Context>(relaxed = true))
                modules(fakeRoomLayer, coreUiModule, stubModule, trackingModule, loggingModule, profileModule, approvalsModule, appModule)
            }
        }

        @AfterClass @JvmStatic
        fun teardown() {
            try { stopKoin() } catch (_: Exception) {}
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

    // ── Existing baseline tests ────────────────────────────────────────────────

    @Test
    fun rootGuardScreen_withSignals() {
        composeRule.setContent {
            MileTrackerTheme {
                RootGuardScreen(
                    onContinue = {},
                    signals = listOf("su binary found at /system/xbin/su", "test-keys build"),
                )
            }
        }
        capture("root_guard_screen")
    }

    @Test
    fun rootGuardScreen_clean() {
        composeRule.setContent {
            MileTrackerTheme {
                RootGuardScreen(onContinue = {}, signals = emptyList())
            }
        }
        capture("root_guard_screen_clean")
    }

    @Test
    fun trackingSuccessScreen() {
        composeRule.setContent {
            MileTrackerTheme {
                TrackingSuccessScreen(
                    distanceKm = 12.4,
                    reimbursableAmount = 185.60,
                    vehicleName = "Honda City",
                    startTime = 1_700_000_000_000L,
                    endTime = 1_700_003_600_000L,
                    transactionId = "TXN-20241115-0042",
                    submissionStatus = "APPROVED",
                    violationCount = 0,
                    violationMessage = null,
                    voucherNumber = "V-2024-0112",
                    voucherAmount = 185.60,
                    onTrackNewJourney = {},
                    onViewExpense = {},
                    onCreateVoucher = {},
                )
            }
        }
        capture("tracking_success_screen")
    }

    // ── Phase XVIII.3 expansion ───────────────────────────────────────────────

    @Test
    fun checkInHistoryScreen_populated() {
        val baseMs = 1_700_000_000_000L
        val events = listOf(
            CheckInHistoryItem("c1", "Hinjewadi IT Park", "Geo check-in confirmed", baseMs - 3_600_000L, 18.5904, 73.7394, "GEO", false),
            CheckInHistoryItem("c2", "FC Road Café", "Manual check-in", baseMs - 10_800_000L, 18.5285, 73.8434, "MANUAL", true),
            CheckInHistoryItem("c3", "Magarpatta Office", "Geo check-in confirmed", baseMs - 25_200_000L, 18.5152, 73.9262, "GEO", false),
            CheckInHistoryItem("c4", "Koregaon Park", "Manual check-in", baseMs - 86_400_000L, 18.5362, 73.8940, "MANUAL", true),
            CheckInHistoryItem("c5", "Baner Road", "Geo check-in confirmed", baseMs - 172_800_000L, 18.5590, 73.7888, "GEO", false),
        )
        composeRule.setContent {
            MileTrackerTheme {
                CheckInHistoryScreen(events = events, onBack = {})
            }
        }
        capture("check_in_history_screen")
    }

    @Test
    fun homeScreenLoaded() {
        composeRule.setContent {
            MileTrackerTheme {
                HomeScreenContent(
                    state = HomeUiState(greetingName = "Siddharth", notificationCount = 3),
                    onStartTracking = {},
                    onAddExpense = {},
                    onOpenAccount = {},
                )
            }
        }
        capture("home_screen_loaded")
    }

    @Test
    fun approvalsScreen_pendingTab() {
        composeRule.setContent {
            MileTrackerTheme {
                ApprovalsScreen(onOpenDetail = {})
            }
        }
        capture("approvals_screen_pending_tab")
    }

    @Test
    fun savedTracksScreen_journeysTab() {
        composeRule.setContent {
            MileTrackerTheme {
                SavedTracksScreen(onTrackClick = {}, onStartNew = {})
            }
        }
        capture("saved_tracks_journeys_tab")
    }

    @Test
    fun logMilesStep1Screen() {
        composeRule.setContent {
            MileTrackerTheme {
                LogMilesScreen(onNext = {}, onOpenHistory = {})
            }
        }
        capture("log_miles_step1_screen")
    }

    @Test
    fun trackMilesIdleScreen() {
        composeRule.setContent {
            MileTrackerTheme {
                TrackMilesScreen(onStop = { _, _, _, _, _ -> }, onOpenMap = {}, onOpenHwEvents = {})
            }
        }
        capture("track_miles_idle_screen")
    }

    @Test
    fun hardwareEventsLogScreen() {
        composeRule.setContent {
            MileTrackerTheme {
                HardwareEventsLogScreen(routeId = "route-j1", onBack = {})
            }
        }
        capture("hardware_events_log_screen")
    }

    @Test
    fun trackInsightsScreen() {
        composeRule.setContent {
            MileTrackerTheme {
                TrackInsightsScreen(routeId = "route-j1", onBack = {})
            }
        }
        capture("track_insights_screen")
    }

    @Test
    fun geoCheckInScreen() {
        composeRule.setContent {
            MileTrackerTheme {
                GeoCheckInScreen(onBack = {})
            }
        }
        capture("geo_check_in_screen")
    }

    private fun capture(name: String) =
        composeRule.onRoot().captureRoboImage(File(screenshotsDir, "$name.png").absolutePath)
}
