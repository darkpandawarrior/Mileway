package com.miletracker.feature.tracking.repository

import com.miletracker.core.data.model.network.ApprovedVehicle
import com.miletracker.core.network.api.MileTrackerNetworkApi

class VehiclePricingRepository(private val api: MileTrackerNetworkApi) {

    suspend fun getVehicles(trackMiles: Boolean = true): List<ApprovedVehicle> =
        api.vehicles(trackMiles).vehicles

    suspend fun getPricing(): Map<String, Double> = api.pricing().data
}
