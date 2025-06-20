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
}
