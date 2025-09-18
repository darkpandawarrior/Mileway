package com.miletracker.core.network.config

import com.miletracker.core.data.model.state.LogMilesPluginConfig
import com.miletracker.core.data.model.state.TrackMilesPluginConfig

interface ConfigProvider {
    fun getTrackMilesConfig(): TrackMilesPluginConfig
    fun getLogMilesConfig(): LogMilesPluginConfig
    fun isMilesEnabled(): Boolean
    fun isLogMilesEnabled(): Boolean
    fun getCurrency(): String

    /** Workspace disclaimer shown on the start-journey consent step; null = no consent. */
    fun getJourneyDisclaimer(): String? = null

    /** Policy ceiling on reimbursable distance per day, in km. */
    fun getMaxDailyDistanceKm(): Double = 10.0

    /** When true, stopping a journey requires at least one branch check-in. */
    fun isBranchCheckInRequired(): Boolean = false
}
