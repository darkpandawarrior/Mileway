package com.mileway.feature.tracking.manager

import kotlinx.serialization.Serializable

/**
 * All abnormal-detection thresholds [LocationProcessor] uses, in one runtime-tweakable object.
 * [DEFAULT] holds the values that used to be hardcoded constants on `LocationProcessor.Companion`
 * — moving them here doesn't change behavior, it just gives debug settings today (and server
 * config later) a single object to override. `@Serializable` (Wave 4): nested inside
 * [TrackingConfig] for the local-JSON/server config source.
 */
@Serializable
data class AbnormalDetectionConfig(
    // C.1a: speed-band jitter gates.
    val walkingMaxMps: Double = 2.5,
    val cyclingMaxMps: Double = 7.0,
    val walkingJitterM: Double = 2.0,
    val cyclingJitterM: Double = 3.0,
    val drivingJitterM: Double = 5.0,
    val stationarySpeedMps: Double = 1.2,
    val stationaryJitterM: Double = 1.2,
    // C.1c: movement-history window.
    val speedHistorySize: Int = 5,
    val movementHistoryMps: Double = 1.5,
    // C.1b: instant-teleport hard gate.
    val spikeHardGateM: Double = 5_000.0,
    // C.1d: gap-recovery tiers (seconds + relaxed speed caps, m/s).
    val gapMinSec: Long = 30L,
    val gap5mSec: Long = 300L,
    val gap1hSec: Long = 3_600L,
    val gap6hSec: Long = 21_600L,
    val gapTier5mMps: Double = 150.0,
    val gapTier1hMps: Double = 100.0,
    val gapTier6hMps: Double = 60.0,
    val gapMaxDistanceM: Double = 10_000.0,
) {
    companion object {
        /** Exactly today's LocationProcessor constants — the no-override baseline. */
        val DEFAULT = AbnormalDetectionConfig()
    }
}
