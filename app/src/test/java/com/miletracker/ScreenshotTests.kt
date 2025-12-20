package com.miletracker

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.miletracker.core.ui.theme.MileTrackerTheme
import com.miletracker.feature.profile.ui.screens.RootGuardScreen
import com.miletracker.feature.tracking.ui.screens.TrackingSuccessScreen
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
            // Resolve relative to repo root regardless of Gradle working dir
            val moduleDir = File(System.getProperty("user.dir") ?: ".")
            val repoRoot = if (moduleDir.name == "app") moduleDir.parentFile else moduleDir
            File(repoRoot, "docs/screenshots").also { it.mkdirs() }
        }

        @BeforeClass @JvmStatic fun setupRecordMode() {
            System.setProperty("roborazzi.test.record", "true")
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

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
        composeRule.onRoot().captureRoboImage(
            File(screenshotsDir, "root_guard_screen.png").absolutePath
        )
    }

    @Test
    fun rootGuardScreen_clean() {
        composeRule.setContent {
            MileTrackerTheme {
                RootGuardScreen(
                    onContinue = {},
                    signals = emptyList(),
                )
            }
        }
        composeRule.onRoot().captureRoboImage(
            File(screenshotsDir, "root_guard_screen_clean.png").absolutePath
        )
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
        composeRule.onRoot().captureRoboImage(
            File(screenshotsDir, "tracking_success_screen.png").absolutePath
        )
    }
}
