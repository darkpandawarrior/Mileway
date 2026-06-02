package com.miletracker.feature.tracking.repository

import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.LogMilesSubmitRequestV2
import com.miletracker.core.network.api.MileTrackerNetworkApi

class LogMilesSubmissionRepository(private val api: MileTrackerNetworkApi) {
    suspend fun submitLogMiles(request: LogMilesSubmitRequestV2): ExpenseSubmissionResponse = api.logMiles(request)
}
