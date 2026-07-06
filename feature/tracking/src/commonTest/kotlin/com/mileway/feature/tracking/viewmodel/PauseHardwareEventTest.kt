package com.mileway.feature.tracking.viewmodel

import com.mileway.core.data.model.db.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [buildPauseHardwareEvent] is the mapping used by [TrackMilesViewModel.pauseTracking] to persist
 * the pause-reason sheet's choice — a quick reason or "Other" free-text — onto a
 * TRACKING_PAUSED hardware event via its existing `metadata` field (no schema change).
 */
class PauseHardwareEventTest {
    @Test
    fun `quick reason is carried in event metadata`() {
        val event = buildPauseHardwareEvent(routeId = "route-1", reason = "Traffic", timeMillis = 1_000L)

        assertEquals(EventType.TRACKING_PAUSED, event.eventType)
        assertEquals("route-1", event.token)
        assertEquals("Reason: Traffic", event.metadata)
    }

    @Test
    fun `custom free-text reason is carried in event metadata`() {
        val event = buildPauseHardwareEvent(routeId = "route-1", reason = "Stopped for a client meeting", timeMillis = 1_000L)

        assertEquals("Reason: Stopped for a client meeting", event.metadata)
    }

    @Test
    fun `blank or null reason leaves metadata null`() {
        assertNull(buildPauseHardwareEvent("route-1", null, 1_000L).metadata)
        assertNull(buildPauseHardwareEvent("route-1", "   ", 1_000L).metadata)
    }
}
