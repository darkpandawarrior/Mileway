package com.miletracker.feature.tracking.export

import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.SavedTrack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pure-Kotlin CSV exporter. No Android deps — fully unit-testable on the JVM.
 * Returns a String that the caller writes to a file.
 */
object CsvExporter {

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private const val HEADER =
        "id,token,timestamp_ms,iso_timestamp,latitude,longitude,accuracy_m,speed_mps," +
        "activity,displacement_m,is_mock,is_abnormal,is_paused,was_manually_paused,provider," +
        "bearing_deg,altitude_m,battery_pct,battery_opt_on,power_saver_on," +
        "uploaded,no_network,app_killed,phone_shutdown,inactivity_ms,reason," +
        "check_in,check_in_type,gyro_x,gyro_y,gyro_z,location_time_ms,ram_bytes"

    fun export(
        track: SavedTrack,
        locations: List<LocationData>,
        @Suppress("UNUSED_PARAMETER") events: List<HardwareEvent>
    ): String = buildString {
        appendLine("# Track: ${track.name}  routeId: ${track.routeId}")
        appendLine("# Distance: ${"%.3f".format(track.distance / 1000.0)} km  Duration: ${track.duration} ms")
        appendLine(HEADER)
        for (loc in locations) {
            appendLine(rowFor(loc))
        }
    }

    private fun rowFor(loc: LocationData): String = listOf(
        loc.id,
        esc(loc.token),
        loc.date,
        esc(isoFmt.format(Date(loc.date))),
        loc.lat,
        loc.lng,
        loc.accuracy,
        loc.speed,
        esc(loc.activity),
        loc.displacement,
        loc.isMock,
        loc.isAbnormal,
        loc.isPaused,
        loc.wasManuallyPaused,
        esc(loc.provider),
        loc.bearing,
        loc.altitude,
        loc.batteryPercentage,
        loc.wasBatteryOptimizationEnabled,
        loc.wasPowerSaverModeEnabled,
        loc.uploaded,
        loc.wasCapturedWhenNoNetwork,
        loc.wasAppKilled,
        loc.wasPhoneShutdown,
        loc.inActivityTime,
        esc(loc.reason ?: ""),
        loc.wasCheckInPoint,
        esc(loc.checkInType),
        loc.gyroscopeX,
        loc.gyroscopeY,
        loc.gyroscopeZ,
        loc.locationTime,
        loc.ramUsage
    ).joinToString(",")

    private fun esc(v: String): String =
        if (v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r'))
            "\"${v.replace("\"", "\"\"")}\""
        else v
}
