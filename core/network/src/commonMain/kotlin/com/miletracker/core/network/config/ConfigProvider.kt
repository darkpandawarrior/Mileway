package com.miletracker.core.network.config

import com.miletracker.core.data.model.state.LogMilesPluginConfig
import com.miletracker.core.data.model.state.TrackMilesPluginConfig

interface ConfigProvider {
    fun getTrackMilesConfig(): TrackMilesPluginConfig
    fun getLogMilesConfig(): LogMilesPluginConfig
    fun isMilesEnabled(): Boolean
    fun isLogMilesEnabled(): Boolean
    fun getCurrency(): String
}
