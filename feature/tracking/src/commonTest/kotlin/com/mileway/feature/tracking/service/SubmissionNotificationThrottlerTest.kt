package com.mileway.feature.tracking.service

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Wave-3 notification depth: SubmissionNotificationThrottler duplicate suppression (parity §3). */
class SubmissionNotificationThrottlerTest {
    @Test
    fun `suppresses a duplicate notification id within the window`() =
        runTest {
            var clock = 0L
            val throttler = SubmissionNotificationThrottler(now = { clock })

            assertTrue(throttler.allow(1), "first occurrence should be allowed")
            assertFalse(throttler.allow(1), "immediate repeat should be suppressed")
        }

    @Test
    fun `allows the same id again once the window has elapsed`() =
        runTest {
            var clock = 0L
            val throttler = SubmissionNotificationThrottler(now = { clock })

            assertTrue(throttler.allow(1))
            assertFalse(throttler.allow(1))

            clock += SubmissionNotificationThrottler.DEFAULT_WINDOW_MS
            assertTrue(throttler.allow(1), "should be allowed again after the window")
        }

    @Test
    fun `different ids are never throttled against each other`() =
        runTest {
            var clock = 0L
            val throttler = SubmissionNotificationThrottler(now = { clock })

            assertTrue(throttler.allow(1))
            assertTrue(throttler.allow(2), "a different notification id must not be suppressed")
        }
}
