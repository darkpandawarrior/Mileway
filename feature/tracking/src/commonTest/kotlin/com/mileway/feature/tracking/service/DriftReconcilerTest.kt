package com.mileway.feature.tracking.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Wave-2: DriftReconciler's 120s cadence + no-op-when-in-sync behavior. */
class DriftReconcilerTest {
    @Test
    fun `no-op before 120s even when seeded mismatch exists`() {
        var clock = 0L
        val reconciler = DriftReconciler(now = { clock })

        clock = DriftReconciler.RECONCILE_INTERVAL_MS - 1
        val result = reconciler.maybeReconcile(inMemoryCount = 5, dbCount = 8)
        assertNull(result)
    }

    @Test
    fun `corrects a seeded mismatch after 120s`() {
        var clock = 0L
        val reconciler = DriftReconciler(now = { clock })

        clock = DriftReconciler.RECONCILE_INTERVAL_MS
        val result = reconciler.maybeReconcile(inMemoryCount = 5, dbCount = 8)

        requireNotNull(result)
        assertEquals(true, result.isDiverged)
        assertEquals(8, result.dbCount)
    }

    @Test
    fun `no-op when in sync after 120s`() {
        var clock = 0L
        val reconciler = DriftReconciler(now = { clock })

        clock = DriftReconciler.RECONCILE_INTERVAL_MS
        val result = reconciler.maybeReconcile(inMemoryCount = 8, dbCount = 8)

        requireNotNull(result)
        assertEquals(false, result.isDiverged)
    }

    @Test
    fun `second check within the next 120s window is a no-op`() {
        var clock = 0L
        val reconciler = DriftReconciler(now = { clock })

        clock = DriftReconciler.RECONCILE_INTERVAL_MS
        reconciler.maybeReconcile(inMemoryCount = 8, dbCount = 8)

        clock += 1
        val result = reconciler.maybeReconcile(inMemoryCount = 5, dbCount = 8)
        assertNull(result)
    }
}
