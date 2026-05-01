package com.miletracker.feature.tracking.repository

import com.miletracker.core.data.dao.HardwareEventDao
import com.miletracker.core.data.model.db.EventAudience
import com.miletracker.core.data.model.db.EventType
import com.miletracker.core.data.model.db.HardwareEvent
import kotlinx.coroutines.flow.Flow

class HardwareEventRepository(private val dao: HardwareEventDao) {

    suspend fun insert(event: HardwareEvent): Result<Long> = runCatching { dao.insert(event) }

    suspend fun insertAll(events: List<HardwareEvent>): Result<List<Long>> = runCatching { dao.insertAll(events) }

    suspend fun getEventsForRoute(routeId: String): Result<List<HardwareEvent>> = runCatching {
        dao.getEventsByToken(routeId)
    }

    fun observeEventsForRoute(routeId: String): Flow<List<HardwareEvent>> =
        dao.observeEventsByToken(routeId)

    suspend fun getEventsForRouteByType(routeId: String, eventTypes: List<EventType>): Result<List<HardwareEvent>> =
        runCatching { dao.getEventsByTokenAndTypes(routeId, eventTypes) }

    suspend fun getEventsForRouteByAudience(routeId: String, audiences: List<EventAudience>): Result<List<HardwareEvent>> =
        runCatching { dao.getEventsByTokenAndAudience(routeId, audiences) }

    suspend fun deleteEventsForRoute(routeId: String): Result<Int> = runCatching {
        dao.deleteEventsByToken(routeId)
    }

    suspend fun getRecentEvents(limit: Int = 50): Result<List<HardwareEvent>> = runCatching {
        dao.getRecentEvents(limit)
    }
}
