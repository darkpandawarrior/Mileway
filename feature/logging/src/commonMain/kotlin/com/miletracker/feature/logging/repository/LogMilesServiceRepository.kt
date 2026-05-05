package com.miletracker.feature.logging.repository

import com.miletracker.core.data.model.network.LogMilesService
import com.miletracker.core.network.api.MileTrackerNetworkApi

class LogMilesServiceRepository(private val api: MileTrackerNetworkApi) {
    suspend fun getServices(isInsideTrip: Boolean = false): List<LogMilesService> =
        api.fetchLogMilesServices(isInsideTrip).services
            ?.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val name = dto.name ?: return@mapNotNull null
                LogMilesService(id, name, dto.glCode ?: "")
            } ?: emptyList()
}
