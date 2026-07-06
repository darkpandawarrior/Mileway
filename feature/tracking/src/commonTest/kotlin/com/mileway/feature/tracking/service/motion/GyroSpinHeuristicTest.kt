package com.mileway.feature.tracking.service.motion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GyroSpinHeuristicTest {
    @Test
    fun `normal turning rate is not a spin`() {
        assertFalse(GyroSpinHeuristic.isPossibleSpin(0.5f, 0f, 0f))
    }

    @Test
    fun `sustained high gyro magnitude flags as possible spin`() {
        assertTrue(GyroSpinHeuristic.isPossibleSpin(4f, 0f, 0f))
    }

    @Test
    fun `magnitude across axes clears threshold`() {
        assertTrue(GyroSpinHeuristic.isPossibleSpin(3f, 3f, 0f))
    }

    @Test
    fun `exactly at threshold is not a spin (strict greater-than)`() {
        assertFalse(GyroSpinHeuristic.isPossibleSpin(GyroSpinHeuristic.SPIN_THRESHOLD_RAD_S, 0f, 0f))
    }
}
