package com.mileway.core.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * DataStore-backed source for the abnormal-detection thresholds a debug/customization screen can
 * override today (and a server config could override later — see AbnormalDetectionConfig).
 * `null` in [overrides] means "not set", so callers fall back to the default threshold.
 */
interface AbnormalDetectionSettingsSource {
    val overrides: Flow<AbnormalDetectionOverrides>
}

/**
 * Every field is nullable — `null` means "use the default", non-null means "override this one
 * threshold". Kept separate from `AbnormalDetectionConfig` (feature/tracking) since core/data
 * doesn't depend on the feature module; TrackingConfigManager maps this onto the full config.
 */
data class AbnormalDetectionOverrides(
    val spikeHardGateM: Double? = null,
    val gapTier5mMps: Double? = null,
    val gapTier1hMps: Double? = null,
    val gapTier6hMps: Double? = null,
    val gapMaxDistanceM: Double? = null,
)
