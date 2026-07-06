package com.mileway.feature.logging.repository

import com.mileway.core.data.dao.LogMilesFrequentRouteDao
import com.mileway.core.data.model.db.LogMilesFrequentRouteEntity
import com.mileway.feature.logging.ui.model.LocationEntry
import com.mileway.feature.logging.ui.model.LocationStop
import com.mileway.feature.logging.ui.model.PoiCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** In-memory [LogMilesFrequentRouteDao] mirroring `app`'s screenshot-Koin `FakeLogMilesFrequentRouteDao`. */
private class FakeLogMilesFrequentRouteDao : LogMilesFrequentRouteDao {
    private val routes = MutableStateFlow<Map<String, LogMilesFrequentRouteEntity>>(emptyMap())

    override fun observeAllRoutes(): Flow<List<LogMilesFrequentRouteEntity>> = routes.map { it.values.toList() }

    override suspend fun getByKey(routeKey: String): LogMilesFrequentRouteEntity? = routes.value[routeKey]

    override suspend fun getAllRoutes(): List<LogMilesFrequentRouteEntity> = routes.value.values.toList()

    override suspend fun deleteByKeys(routeKeys: List<String>) {
        routes.value = routes.value - routeKeys.toSet()
    }

    override suspend fun upsert(route: LogMilesFrequentRouteEntity) {
        routes.value = routes.value + (route.routeKey to route)
    }
}

/**
 * Wave 3 acceptance: recording a submitted route caches it keyed by its stop names, bumps
 * use-count on a repeat submission of the same route, and [LogMilesFrequentRouteRepository
 * .topRoutes] surfaces the most-used routes first, ready for one-tap retrace.
 */
class LogMilesFrequentRouteRepositoryTest {
    private val stops =
        listOf(
            LocationStop(id = 0L, entry = LocationEntry("Home", "Home subtitle", 18.52, 73.85, PoiCategory.OTHER)),
            LocationStop(id = 1L, entry = LocationEntry("Office", "Office subtitle", 18.55, 73.91, PoiCategory.OFFICE)),
        )

    @Test
    fun `recording a submission caches the route keyed by its stops`() =
        runTest {
            val dao = FakeLogMilesFrequentRouteDao()
            val repo = LogMilesFrequentRouteRepository(dao)

            repo.recordSubmission(stops, distanceKm = 12.0, nowMillis = 1_000L)

            val cached = dao.getByKey(LogMilesFrequentRouteRepository.keyFor(stops))
            assertEquals(1, cached?.useCount)
            assertEquals(12.0, cached?.distanceKm)
        }

    @Test
    fun `resubmitting the same route bumps its use count instead of duplicating it`() =
        runTest {
            val dao = FakeLogMilesFrequentRouteDao()
            val repo = LogMilesFrequentRouteRepository(dao)

            repo.recordSubmission(stops, distanceKm = 12.0, nowMillis = 1_000L)
            repo.recordSubmission(stops, distanceKm = 12.5, nowMillis = 2_000L)

            val all = dao.getAllRoutes()
            assertEquals(1, all.size)
            assertEquals(2, all.single().useCount)
            assertEquals(12.5, all.single().distanceKm)
        }

    @Test
    fun `topRoutes surfaces the most-used route first`() =
        runTest {
            val dao = FakeLogMilesFrequentRouteDao()
            val repo = LogMilesFrequentRouteRepository(dao)
            val otherStops =
                listOf(
                    LocationStop(id = 2L, entry = LocationEntry("Gym", "Gym subtitle", 18.50, 73.80, PoiCategory.OTHER)),
                    LocationStop(id = 3L, entry = LocationEntry("Mall", "Mall subtitle", 18.60, 73.95, PoiCategory.OTHER)),
                )

            repo.recordSubmission(otherStops, distanceKm = 5.0, nowMillis = 1_000L)
            repo.recordSubmission(stops, distanceKm = 12.0, nowMillis = 2_000L)
            repo.recordSubmission(stops, distanceKm = 12.0, nowMillis = 3_000L)

            val top = repo.topRoutes().first()
            assertEquals(LogMilesFrequentRouteRepository.keyFor(stops), top.first().routeKey)
        }

    @Test
    fun `a route with fewer than two stops is not cached`() =
        runTest {
            val dao = FakeLogMilesFrequentRouteDao()
            val repo = LogMilesFrequentRouteRepository(dao)

            repo.recordSubmission(stops.take(1), distanceKm = 0.0, nowMillis = 1_000L)

            assertNull(dao.getByKey(LogMilesFrequentRouteRepository.keyFor(stops.take(1))))
        }
}
