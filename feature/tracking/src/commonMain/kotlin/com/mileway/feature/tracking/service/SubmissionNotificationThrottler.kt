package com.mileway.feature.tracking.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Wave-3 notification depth (parity §3): throttles submission-flow notifications (quality score,
 * violation alert, completion summary) so a resubmission or duplicate callback doesn't spam the
 * same notification id twice within [windowMs]. Mirrors [EventThrottler]'s shape (mutex-guarded
 * last-seen map, injected clock for fake-clock tests) but keyed by notification id rather than
 * [com.mileway.core.data.model.db.EventType] since submission notifications aren't hardware events.
 */
class SubmissionNotificationThrottler(
    private val now: () -> Long,
    private val windowMs: Long = DEFAULT_WINDOW_MS,
) {
    companion object {
        const val DEFAULT_WINDOW_MS = 10_000L
    }

    private val mutex = Mutex()
    private val lastSeenAt = mutableMapOf<Int, Long>()

    /** Returns true if the notification with [id] should be posted now, false if it should be suppressed. */
    suspend fun allow(id: Int): Boolean =
        mutex.withLock {
            val nowMs = now()
            val last = lastSeenAt[id]
            if (last != null && nowMs - last < windowMs) {
                false
            } else {
                lastSeenAt[id] = nowMs
                true
            }
        }
}
