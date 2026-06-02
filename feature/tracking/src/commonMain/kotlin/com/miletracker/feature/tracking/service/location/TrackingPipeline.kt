@file:Suppress("ktlint:standard:max-line-length", "ktlint:standard:property-naming", "ktlint:standard:comment-wrapping")

package com.miletracker.feature.tracking.service.location

import com.miletracker.core.data.model.db.LocationData
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Platform-independent GPS fix. Sources convert their native location into this so the
 * [LocationProcessor] stays pure Kotlin (unit-testable without Android/Play Services).
 */
data class GpsFix(
    val lat: Double,
    val lng: Double,
    val timeMs: Long,
    val speedMps: Float = 0f,
    val accuracyM: Float = 0f,
    val bearingDeg: Float = 0f,
    val altitudeM: Double = 0.0,
    val provider: String = "fused",
    val isMock: Boolean = false,
)

/** Latest IMU readings attached to each persisted point (zeros if no sensors). */
data class SensorSnapshot(
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
)

/** Result of processing one fix: the row to persist plus classification flags. */
data class ProcessResult(
    val location: LocationData,
    val displacementM: Double,
    val isAbnormal: Boolean,
    val isMock: Boolean,
    val countedTowardDistance: Boolean,
)

/**
 * Running accumulators for a session, exposed so the service can persist live stats.
 */
data class TrackStats(
    val totalPoints: Int,
    val originalDistanceM: Double,
    val cleanedDistanceM: Double,
    val abnormalDistanceM: Double,
    val mockDistanceM: Double,
    val avgSpeedMps: Double,
    val maxSpeedMps: Double,
)

/**
 * Advanced point-by-point processor: distance accumulation (Haversine), GPS-jitter
 * suppression, mock-location handling, and spike/abnormal detection (implausible implied
 * speed). Distance is split into original / cleaned / abnormal / mock buckets so the UI can
 * present a trustworthy "cleaned" distance while retaining the raw figure.
 *
 * Pure Kotlin — no Android dependency — so it is fully covered by JVM unit tests.
 */
class LocationProcessor(
    private val jitterDistanceM: Double = 8.0,
    // ~252 km/h — anything faster is a GPS spike
    private val maxPlausibleSpeedMps: Double = 70.0,
    private val deviceModel: String = "",
    private val appVersionName: String = "",
    initialStats: TrackStats? = null,
) {
    private var last: GpsFix? = null
    private var firstFix: GpsFix? = null

    var totalPoints = 0
        private set
    var originalDistanceM = 0.0
        private set
    var cleanedDistanceM = 0.0
        private set
    var abnormalDistanceM = 0.0
        private set
    var mockDistanceM = 0.0
        private set
    var maxSpeedMps = 0.0
        private set
    private var speedSum = 0.0
    private var speedCount = 0

    init {
        // Resume accumulators from a persisted session (e.g. after the tracking service is
        // restarted across a reboot). `last` stays null so the first fix of the resumed
        // segment is an anchor: the gap travelled while not tracking never counts as distance.
        initialStats?.let { s ->
            totalPoints = s.totalPoints
            originalDistanceM = s.originalDistanceM
            cleanedDistanceM = s.cleanedDistanceM
            abnormalDistanceM = s.abnormalDistanceM
            mockDistanceM = s.mockDistanceM
            maxSpeedMps = s.maxSpeedMps
            // The exact speed-sample count isn't persisted; weighting by totalPoints keeps
            // the running average continuous across the restart.
            speedSum = s.avgSpeedMps * s.totalPoints
            speedCount = s.totalPoints
        }
    }

    val avgSpeedMps: Double get() = if (speedCount > 0) speedSum / speedCount else 0.0
    val firstLat: Double? get() = firstFix?.lat
    val firstLng: Double? get() = firstFix?.lng

    fun stats() =
        TrackStats(
            totalPoints = totalPoints,
            originalDistanceM = originalDistanceM,
            cleanedDistanceM = cleanedDistanceM,
            abnormalDistanceM = abnormalDistanceM,
            mockDistanceM = mockDistanceM,
            avgSpeedMps = avgSpeedMps,
            maxSpeedMps = maxSpeedMps,
        )

    /**
     * Feed a fix. Returns the row to persist, or null when the fix is suppressed as jitter.
     * @param isPaused when true the point is recorded but does not add to the cleaned distance.
     */
    fun process(
        fix: GpsFix,
        isPaused: Boolean,
        sensors: SensorSnapshot = SensorSnapshot(),
    ): ProcessResult? {
        val prev = last
        if (firstFix == null) firstFix = fix

        val displacement = if (prev != null) haversineMeters(prev.lat, prev.lng, fix.lat, fix.lng) else 0.0
        val dtSec = if (prev != null) max(1L, (fix.timeMs - prev.timeMs) / 1000L) else 1L
        val impliedSpeed = displacement / dtSec

        // Suppress stationary GPS jitter (small wander while parked). Keep the anchor so a
        // later genuine move is measured from the last *persisted* point.
        if (prev != null && displacement < jitterDistanceM && !fix.isMock) {
            return null
        }

        val abnormal = prev != null && impliedSpeed > maxPlausibleSpeedMps
        var counted = false

        if (prev != null) {
            originalDistanceM += displacement
            when {
                fix.isMock -> mockDistanceM += displacement
                abnormal -> abnormalDistanceM += displacement
                isPaused -> { /* recorded but excluded from cleaned distance */ }
                else -> {
                    cleanedDistanceM += displacement
                    counted = true
                }
            }
        }

        if (!isPaused && !abnormal && !fix.isMock) {
            val sp = fix.speedMps.toDouble()
            speedSum += sp
            speedCount++
            if (sp > maxSpeedMps) maxSpeedMps = sp
        }

        last = fix
        totalPoints++

        val row =
            LocationData(
                activity = if (isPaused) "PAUSED" else "DRIVING",
                speed = fix.speedMps,
                lat = fix.lat,
                lng = fix.lng,
                // set by the service before insert
                token = "",
                date = fix.timeMs,
                displacement = displacement,
                accuracy = fix.accuracyM,
                isMock = fix.isMock,
                isAbnormal = abnormal,
                isPaused = isPaused,
                // set by the service from BatteryManager
                batteryPercentage = 0.0,
                gyroscopeX = sensors.gyroX,
                gyroscopeY = sensors.gyroY,
                gyroscopeZ = sensors.gyroZ,
                accelerometerX = sensors.accelX,
                accelerometerY = sensors.accelY,
                accelerometerZ = sensors.accelZ,
                provider = fix.provider,
                bearing = fix.bearingDeg,
                altitude = fix.altitudeM,
                locationTime = fix.timeMs,
                deviceModel = deviceModel,
                appVersionName = appVersionName,
            )
        return ProcessResult(
            location = row,
            displacementM = displacement,
            isAbnormal = abnormal,
            isMock = fix.isMock,
            countedTowardDistance = counted,
        )
    }
}

/** Great-circle distance in metres between two lat/lng points. */
fun haversineMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val earthRadiusM = 6_371_000.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLng = (lng2 - lng1) * PI / 180.0
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLng / 2) * sin(dLng / 2)
    return earthRadiusM * (2 * atan2(sqrt(a), sqrt(1 - a)))
}
