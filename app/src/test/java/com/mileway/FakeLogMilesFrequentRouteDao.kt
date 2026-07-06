package com.mileway

import com.mileway.core.data.dao.LogMilesFrequentRouteDao
import com.mileway.core.data.model.db.LogMilesFrequentRouteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake for [LogMilesFrequentRouteDao] (Wave 3) — `LogMilesViewModel.observeFrequentRoutes()`
 * `collectLatest`s `observeAllRoutes()` in `init`, and a bare `mockk(relaxed = true)` returns a
 * null-backed `Flow` that crashes that collector (same reason [FakeLogMilesDraftDao] exists).
 */
class FakeLogMilesFrequentRouteDao : LogMilesFrequentRouteDao {
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
