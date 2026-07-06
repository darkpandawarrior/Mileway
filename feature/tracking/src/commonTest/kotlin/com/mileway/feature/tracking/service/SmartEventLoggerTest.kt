package com.mileway.feature.tracking.service

import com.mileway.core.data.dao.HardwareEventDao
import com.mileway.core.data.model.db.EventAudience
import com.mileway.core.data.model.db.EventType
import com.mileway.core.data.model.db.HardwareEvent
import com.mileway.feature.tracking.repository.HardwareEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Wave-2 SmartEventLogger: inserts allowed events, suppresses throttled ones (parity §2.8). */
class SmartEventLoggerTest {
    private fun event(type: EventType) = HardwareEvent(token = "t", eventType = type, event = type.name, audience = EventAudience.USER)

    @Test
    fun `inserts allowed events and suppresses throttled ones`() =
        runTest {
            val dao = FakeHardwareEventDao()
            var clock = 0L
            val logger = SmartEventLogger(HardwareEventRepository(dao), EventThrottler(now = { clock }))

            logger.log(event(EventType.MOCK_LOCATION))
            logger.log(event(EventType.MOCK_LOCATION)) // suppressed, same window
            assertEquals(1, dao.inserted.size)

            clock += EventThrottler.MOCK_LOCATION_INTERVAL_MS
            logger.log(event(EventType.MOCK_LOCATION)) // window elapsed, allowed
            assertEquals(2, dao.inserted.size)
        }

    @Test
    fun `critical events are always inserted`() =
        runTest {
            val dao = FakeHardwareEventDao()
            val logger = SmartEventLogger(HardwareEventRepository(dao), EventThrottler(now = { 0L }))

            logger.log(event(EventType.TRACKING_STARTED))
            logger.log(event(EventType.TRACKING_STARTED))
            assertEquals(2, dao.inserted.size)
        }
}

private class FakeHardwareEventDao : HardwareEventDao {
    val inserted = mutableListOf<HardwareEvent>()

    override suspend fun insert(event: HardwareEvent): Long {
        inserted.add(event)
        return inserted.size.toLong()
    }

    override suspend fun insertAll(events: List<HardwareEvent>): List<Long> {
        inserted.addAll(events)
        return events.map { inserted.size.toLong() }
    }

    override suspend fun insertEvents(events: List<HardwareEvent>) {
        inserted.addAll(events)
    }

    override suspend fun getEventsByToken(token: String): List<HardwareEvent> = inserted.filter { it.token == token }

    override fun observeEventsByToken(token: String): Flow<List<HardwareEvent>> = flowOf(getEventsByTokenSync(token))

    private fun getEventsByTokenSync(token: String) = inserted.filter { it.token == token }

    override suspend fun getEventsByTokenAndTypes(
        token: String,
        types: List<EventType>,
    ): List<HardwareEvent> = inserted.filter { it.token == token && it.eventType in types }

    override suspend fun getEventsByTokenAndAudience(
        token: String,
        audiences: List<EventAudience>,
    ): List<HardwareEvent> = inserted.filter { it.token == token && it.audience in audiences }

    override suspend fun getEventsWithLocationByToken(token: String): List<HardwareEvent> =
        inserted.filter { it.token == token && it.lat != null && it.lng != null }

    override suspend fun getEventsByTokenAndTimeRange(
        token: String,
        startTime: Long,
        endTime: Long,
    ): List<HardwareEvent> = inserted.filter { it.token == token && it.time in startTime..endTime }

    override suspend fun getEventCountByToken(token: String): Int = inserted.count { it.token == token }

    override suspend fun getEventCountByTokenAndType(
        token: String,
        eventType: EventType,
    ): Int = inserted.count { it.token == token && it.eventType == eventType }

    override suspend fun deleteEventsOlderThan(cutoffTime: Long): Int {
        val before = inserted.size
        inserted.removeAll { it.time < cutoffTime }
        return before - inserted.size
    }

    override suspend fun deleteEventsByToken(token: String): Int {
        val before = inserted.size
        inserted.removeAll { it.token == token }
        return before - inserted.size
    }

    override suspend fun deleteByToken(token: String) {
        inserted.removeAll { it.token == token }
    }

    override suspend fun getUnsyncedEvents(limit: Int): List<HardwareEvent> = inserted.filter { !it.uploaded }.take(limit)

    override suspend fun getUnsyncedEventsByToken(token: String): List<HardwareEvent> = inserted.filter { it.token == token && !it.uploaded }

    override suspend fun markEventsAsUploaded(ids: List<Long>): Int = 0

    override suspend fun markEventsAsSynced(eventIds: List<Long>) {}

    override suspend fun getRecentEvents(limit: Int): List<HardwareEvent> = inserted.takeLast(limit)

    override suspend fun getDistinctEventTypesByToken(token: String): List<EventType> = inserted.filter { it.token == token }.map { it.eventType }.distinct()
}
