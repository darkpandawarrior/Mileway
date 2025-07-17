package com.miletracker

import com.miletracker.feature.tracking.service.location.GpsFix
import com.miletracker.feature.tracking.service.location.LocationProcessor
import com.miletracker.feature.tracking.service.location.haversineMeters
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the advanced location-tracking pipeline. Pure Kotlin — no device needed.
 */
class LocationProcessorTest {

    private fun fix(lat: Double, lng: Double, t: Long, speed: Float = 11f, mock: Boolean = false) =
        GpsFix(lat = lat, lng = lng, timeMs = t, speedMps = speed, isMock = mock)

    @Test
    fun `haversine matches known distance`() {
        // ~0.001 deg latitude ≈ 111 m.
        val d = haversineMeters(18.5000, 73.8, 18.5010, 73.8)
        assertTrue(d in 105.0..115.0, "expected ~111m, got $d")
    }

    @Test
    fun `normal movement accumulates cleaned distance`() {
        val proc = LocationProcessor()
        val r0 = proc.process(fix(18.5000, 73.8, 0), isPaused = false)
        assertTrue(r0 != null) // first fix is the anchor and is still persisted
        val r1 = proc.process(fix(18.5010, 73.8, 10_000), isPaused = false)
        assertTrue(r1 != null && r1.countedTowardDistance)
        assertTrue(proc.cleanedDistanceM in 105.0..115.0, "cleaned=${proc.cleanedDistanceM}")
        assertEquals(2, proc.totalPoints)
    }

    @Test
    fun `tiny jitter is suppressed`() {
        val proc = LocationProcessor()
        proc.process(fix(18.5000, 73.8, 0), isPaused = false)
        // ~2m move — below the 8m jitter threshold.
        val jitter = proc.process(fix(18.50002, 73.8, 10_000), isPaused = false)
        assertNull(jitter)
        assertEquals(0.0, proc.cleanedDistanceM, 0.001)
        assertEquals(1, proc.totalPoints)
    }

    @Test
    fun `gps spike is flagged abnormal and excluded from cleaned distance`() {
        val proc = LocationProcessor()
        proc.process(fix(18.5000, 73.8, 0), isPaused = false)
        proc.process(fix(18.5010, 73.8, 10_000), isPaused = false) // ~111m, counted
        val cleanedBefore = proc.cleanedDistanceM
        // Teleport ~11km in 1s → implausible speed.
        val spike = proc.process(fix(18.6000, 73.8, 11_000), isPaused = false)
        assertTrue(spike != null && spike.isAbnormal)
        assertEquals(cleanedBefore, proc.cleanedDistanceM, 0.001) // cleaned unchanged
        assertTrue(proc.abnormalDistanceM > 1000.0)
    }

    @Test
    fun `mock location is tracked separately from cleaned distance`() {
        val proc = LocationProcessor()
        proc.process(fix(18.5000, 73.8, 0), isPaused = false)
        proc.process(fix(18.5010, 73.8, 10_000), isPaused = false)
        val cleanedBefore = proc.cleanedDistanceM
        val mock = proc.process(fix(18.5020, 73.8, 20_000, mock = true), isPaused = false)
        assertTrue(mock != null && mock.isMock)
        assertEquals(cleanedBefore, proc.cleanedDistanceM, 0.001)
        assertTrue(proc.mockDistanceM > 100.0)
    }

    @Test
    fun `paused points do not add to cleaned distance`() {
        val proc = LocationProcessor()
        proc.process(fix(18.5000, 73.8, 0), isPaused = false)
        val paused = proc.process(fix(18.5010, 73.8, 10_000), isPaused = true)
        assertTrue(paused != null)
        assertFalse(paused.countedTowardDistance)
        assertEquals(0.0, proc.cleanedDistanceM, 0.001)
        assertTrue(proc.originalDistanceM > 100.0) // still counted in the raw figure
    }
}
