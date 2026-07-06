package com.mileway.feature.tracking.service

import com.mileway.core.data.model.db.HardwareEvent
import com.mileway.feature.tracking.repository.HardwareEventRepository

/**
 * Wave-2 SmartEventLogger (parity §2.8): the single choke point hardware events should flow
 * through before hitting [HardwareEventRepository.insert] — consults [EventThrottler] first so
 * high-frequency noise (repeated MOCK_LOCATION/ABNORMAL_LOCATION per fix) is rate-limited, while
 * critical one-shot lifecycle events always land.
 */
class SmartEventLogger(
    private val repository: HardwareEventRepository,
    private val throttler: EventThrottler,
) {
    /** Inserts [event] unless [EventThrottler] suppresses its type for this occurrence. */
    suspend fun log(event: HardwareEvent) {
        if (throttler.allow(event.eventType)) {
            repository.insert(event)
        }
    }
}
