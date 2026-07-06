package com.mileway.feature.tracking.service.motion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HarshAccelDetectorTest {
    @Test
    fun `below threshold is not harsh`() {
        assertFalse(HarshAccelDetector.isHarsh(1f, 0f, 0f))
    }

    @Test
    fun `above threshold is harsh (hard acceleration)`() {
        assertTrue(HarshAccelDetector.isHarsh(5f, 0f, 0f))
    }

    @Test
    fun `above threshold is harsh (hard braking, negative sign)`() {
        assertTrue(HarshAccelDetector.isHarsh(-5f, 0f, 0f))
    }

    @Test
    fun `magnitude across axes clears threshold`() {
        assertTrue(HarshAccelDetector.isHarsh(3f, 3f, 0f))
    }

    @Test
    fun `exactly at threshold is not harsh (strict greater-than)`() {
        assertFalse(HarshAccelDetector.isHarsh(HarshAccelDetector.HARSH_THRESHOLD_MPS2, 0f, 0f))
    }
}
