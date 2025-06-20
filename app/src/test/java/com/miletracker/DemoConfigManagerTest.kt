package com.miletracker

import com.miletracker.stub.DemoConfigManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests that DemoConfigManager returns sensible defaults
 * and correctly implements ConfigProvider.
 */
class DemoConfigManagerTest {

    private lateinit var manager: DemoConfigManager

    @Before
    fun setUp() {
        manager = DemoConfigManager()
    }

    @Test
    fun `isMilesEnabled returns true`() = assertTrue(manager.isMilesEnabled())

    @Test
    fun `isLogMilesEnabled returns true`() = assertTrue(manager.isLogMilesEnabled())

    @Test
    fun `currency is INR`() = assertEquals("INR", manager.getCurrency())

    @Test
    fun `getTrackMilesConfig has sensible defaults`() {
        val config = manager.getTrackMilesConfig()
        assertTrue(config.isTrackMilesEnabled)
        assertTrue(config.trackMilesV2)
        assertTrue(config.draftTrackMiles)
        assertTrue(config.allowPauseTrackMiles)
        assertTrue(config.saveTrackMilesEnabled)
        assertTrue(config.isDiscardJourneyEnabled)
        assertEquals("DEMO", config.tenantCode)
        assertEquals("INR", config.currency)
    }

    @Test
    fun `getLogMilesConfig has sensible defaults`() {
        val config = manager.getLogMilesConfig()
        assertTrue(config.logMilesEnabled)
        assertTrue(config.isMilesEditable)
        assertTrue(config.draftLogMiles)
        assertEquals("INR", config.currency)
        assertEquals("DEMO", config.tenantCode)
    }

    @Test
    fun `configState emits Success immediately`() {
        val state = manager.configState.value
        assertNotNull(state)
        assertTrue(state is com.miletracker.core.data.result.NetworkResult.Success)
        val data = (state as com.miletracker.core.data.result.NetworkResult.Success).data
        assertTrue(data.miles)
        assertTrue(data.logMiles)
    }
}
