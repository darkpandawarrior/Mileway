package com.mileway.feature.tracking.manager

import com.mileway.core.data.model.state.LogMilesPluginConfig
import com.mileway.core.data.model.state.TrackMilesPluginConfig
import com.mileway.core.data.settings.AbnormalDetectionOverrides
import com.mileway.core.network.config.ConfigProvider
import com.mileway.core.network.model.VendorCenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TrackingConfigManager(
    private val configProvider: ConfigProvider,
    // Wave-2 AbnormalDetectionConfig: overrides source is optional so every existing call site
    // (DI, tests) keeps compiling — omit it and abnormalDetectionConfig just emits DEFAULT forever.
    abnormalDetectionOverrides: Flow<AbnormalDetectionOverrides> = flowOf(AbnormalDetectionOverrides()),
) {
    /**
     * Hot-reload config Flow: debug settings can push [AbnormalDetectionOverrides] today, a real
     * server config could push them later — either way LocationProcessor just observes this Flow.
     * Any field left null in the overrides falls back to [AbnormalDetectionConfig.DEFAULT].
     */
    val abnormalDetectionConfig: Flow<AbnormalDetectionConfig> =
        abnormalDetectionOverrides.map { o ->
            val d = AbnormalDetectionConfig.DEFAULT
            AbnormalDetectionConfig(
                spikeHardGateM = o.spikeHardGateM ?: d.spikeHardGateM,
                gapTier5mMps = o.gapTier5mMps ?: d.gapTier5mMps,
                gapTier1hMps = o.gapTier1hMps ?: d.gapTier1hMps,
                gapTier6hMps = o.gapTier6hMps ?: d.gapTier6hMps,
                gapMaxDistanceM = o.gapMaxDistanceM ?: d.gapMaxDistanceM,
            )
        }

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

    fun getCheckInTypes(): List<String> = configProvider.getCheckInTypes()

    fun getCheckInFormSchema(type: String): List<Pair<String, String>> = configProvider.getCheckInFormSchema(type)

    fun getDemoLat(): Double = configProvider.getDemoLat()

    fun getDemoLng(): Double = configProvider.getDemoLng()

    fun getDemoAccuracyLabel(): String = configProvider.getDemoAccuracyLabel()

    fun getVendorCenters(): List<VendorCenter> = configProvider.getVendorCenters()

    fun getGeoCheckInRadiusMeters(): Double = configProvider.getGeoCheckInRadiusMeters()

    fun getMaxAccuracyThresholdM(): Double = configProvider.getMaxAccuracyThresholdM()
}
