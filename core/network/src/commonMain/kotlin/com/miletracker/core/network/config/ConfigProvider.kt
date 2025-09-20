package com.miletracker.core.network.config

import com.miletracker.core.data.model.state.LogMilesPluginConfig
import com.miletracker.core.data.model.state.TrackMilesPluginConfig
import com.miletracker.core.network.model.BusinessEntity
import com.miletracker.core.network.model.Office

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

    /** Offices a mileage expense can be billed against (submission picker). */
    fun getOffices(): List<Office> = emptyList()

    /** Business entities (legal companies) a submission can be filed under. */
    fun getBusinessEntities(): List<BusinessEntity> = emptyList()

    /** When true, the submission form must collect an office + entity. */
    fun isOfficeSelectionRequired(): Boolean = false
}
