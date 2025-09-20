package com.miletracker.feature.tracking.manager

import com.miletracker.core.data.model.state.LogMilesPluginConfig
import com.miletracker.core.data.model.state.TrackMilesPluginConfig
import com.miletracker.core.network.config.ConfigProvider

class TrackingConfigManager(private val configProvider: ConfigProvider) {
    fun getTrackMilesConfig(): TrackMilesPluginConfig = configProvider.getTrackMilesConfig()
    fun getLogMilesConfig(): LogMilesPluginConfig = configProvider.getLogMilesConfig()
    fun isMilesEnabled(): Boolean = configProvider.isMilesEnabled()
    fun isLogMilesEnabled(): Boolean = configProvider.isLogMilesEnabled()
    fun getCurrency(): String = configProvider.getCurrency()
    fun getJourneyDisclaimer(): String? = configProvider.getJourneyDisclaimer()
    fun getMaxDailyDistanceKm(): Double = configProvider.getMaxDailyDistanceKm()
    fun isBranchCheckInRequired(): Boolean = configProvider.isBranchCheckInRequired()
    fun getOffices() = configProvider.getOffices()
    fun getBusinessEntities() = configProvider.getBusinessEntities()
    fun isOfficeSelectionRequired(): Boolean = configProvider.isOfficeSelectionRequired()
}
