package com.mileway.feature.tracking.manager

import com.mileway.core.data.model.state.LogMilesPluginConfig
import com.mileway.core.data.model.state.TrackMilesPluginConfig
import com.mileway.core.network.config.ConfigProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// File-private FakeConfigProvider in AbnormalDetectionConfigTest.kt isn't visible here — a second
// minimal fake is cheaper than de-privatizing/sharing one for a single extra test.
private object FakeTrackingConfigProvider : ConfigProvider {
    override fun getTrackMilesConfig(): TrackMilesPluginConfig = TrackMilesPluginConfig()

    override fun getLogMilesConfig(): LogMilesPluginConfig = LogMilesPluginConfig()

    override fun isMilesEnabled(): Boolean = true

    override fun isLogMilesEnabled(): Boolean = true

    override fun getCurrency(): String = "USD"
}

class TrackingConfigTest {
    @Test
    fun `DEFAULT_TRACKING_CONFIG_JSON parses to the expected typed config`() {
        val parsed = TrackingConfigJsonSource.load()
        assertEquals(TrackingConfig.DEFAULT, parsed)
    }

    @Test
    fun `missing and unknown fields fall back to defaults without crashing`() {
        val partialWithUnknownField =
            """
            {
                "maxAccuracyThresholdM": 99.0,
                "someFutureServerField": "ignored"
            }
            """
        val parsed = TrackingConfigJsonSource.load(partialWithUnknownField)
        assertEquals(99.0, parsed.maxAccuracyThresholdM)
        // Everything else not present in the partial JSON falls back to DEFAULT.
        assertEquals(TrackingConfig.DEFAULT.minGpsIntervalMs, parsed.minGpsIntervalMs)
        assertEquals(TrackingConfig.DEFAULT.kalmanFilterEnabled, parsed.kalmanFilterEnabled)
        assertEquals(TrackingConfig.DEFAULT.abnormalDetection, parsed.abnormalDetection)
    }

    @Test
    fun `malformed JSON falls back to DEFAULT instead of throwing`() {
        val parsed = TrackingConfigJsonSource.load("{ not valid json")
        assertEquals(TrackingConfig.DEFAULT, parsed)
    }

    @Test
    fun `TrackingConfigManager trackingConfig Flow emits the config`() =
        runTest {
            val manager = TrackingConfigManager(configProvider = FakeTrackingConfigProvider)
            assertEquals(TrackingConfig.DEFAULT, manager.trackingConfig.first())
        }
}
