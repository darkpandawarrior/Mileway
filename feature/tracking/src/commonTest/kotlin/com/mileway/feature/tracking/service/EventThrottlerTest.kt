package com.mileway.feature.tracking.service

import com.mileway.core.data.model.db.EventType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Wave-2 SmartEventLogger: EventThrottler per-type rate limiting (parity §2.8). */
class EventThrottlerTest {
    @Test
    fun `suppresses a second same-type event within the window`() =
        runTest {
            var clock = 0L
            val throttler = EventThrottler(now = { clock })

            assertTrue(throttler.allow(EventType.MOCK_LOCATION), "first occurrence should be allowed")
            assertFalse(throttler.allow(EventType.MOCK_LOCATION), "immediate repeat should be suppressed")
        }

    @Test
    fun `allows the same type again once the window has elapsed`() =
        runTest {
            var clock = 0L
            val throttler = EventThrottler(now = { clock })

            assertTrue(throttler.allow(EventType.ABNORMAL_LOCATION))
            assertFalse(throttler.allow(EventType.ABNORMAL_LOCATION))

            clock += EventThrottler.ABNORMAL_LOCATION_INTERVAL_MS
            assertTrue(throttler.allow(EventType.ABNORMAL_LOCATION), "should be allowed again after the interval")
        }

    @Test
    fun `critical one-shot types are never suppressed even back-to-back`() =
        runTest {
            var clock = 0L
            val throttler = EventThrottler(now = { clock })

            assertTrue(throttler.allow(EventType.TRACKING_STARTED))
            assertTrue(throttler.allow(EventType.TRACKING_STARTED))
            assertTrue(throttler.allow(EventType.TRACKING_STOPPED))
            assertTrue(throttler.allow(EventType.TRACKING_PAUSED))
            assertTrue(throttler.allow(EventType.TRACKING_RESUMED))
        }

    @Test
    fun `unlisted event types are never throttled`() =
        runTest {
            var clock = 0L
            val throttler = EventThrottler(now = { clock })

            assertTrue(throttler.allow(EventType.GPS_LOST))
            assertTrue(throttler.allow(EventType.GPS_LOST))
        }
}
