package com.miletracker.feature.tracking.insights

import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.SavedTrack
import java.util.Calendar
import java.util.Locale

/**
 * Coordinator that runs all four analyzers and assembles a [RouteAnalysisResult].
 *
 * Composes the four route analyzers with no Android/DI or backend dependencies.
 * Each analyzer is pure Kotlin and independently testable.
 *
 * Route categorisation logic (preserved verbatim):
 *   - Weekday + hour 6-9 or 17-19  → "Commute"
 *   - Weekend                       → "Leisure"
 *   - avgSpeed < 10 km/h            → "Exercise"
 *   - else                          → "General Journey"
 */
class RouteAnalyzer(
    private val journeyQualityAnalyzer: JourneyQualityAnalyzer = JourneyQualityAnalyzer(),
    private val activityAnalyzer: ActivityAnalyzer = ActivityAnalyzer(),
    private val systemImpactAnalyzer: SystemImpactAnalyzer = SystemImpactAnalyzer(),
) {

    companion object {
        private const val KMH_CONVERSION = 3.6
    }

    fun analyze(
        track: SavedTrack,
        points: List<LocationData>,
        events: List<HardwareEvent> = emptyList()
    ): RouteAnalysisResult {
        val quality       = journeyQualityAnalyzer.analyze(track)
        val activity      = activityAnalyzer.analyze(points)
        val systemImpact  = systemImpactAnalyzer.analyze(track, points, events)
        val distQuality   = DistanceQualityAnalyzer.analyze(track, points)

        val summary   = buildSummary(track)
        val category  = categorizeRoute(track)
        val anomalies = detectAnomalies(track, points)

        return RouteAnalysisResult(
            quality       = quality,
            activity      = activity,
            systemImpact  = systemImpact,
            distanceQuality = distQuality,
            summary       = summary,
            category      = category,
            anomalies     = anomalies
        )
    }

    private fun buildSummary(track: SavedTrack): String {
        val distKm = track.distance / 1000.0
        val durMin = track.duration / 60_000.0
        val avgKmh = track.avgSpeed * KMH_CONVERSION
        return "Journey of %.1f km over %.0f min (avg %.1f km/h)".format(distKm, durMin, avgKmh)
    }

    private fun categorizeRoute(track: SavedTrack): String {
        val cal = Calendar.getInstance().apply { timeInMillis = track.startTime }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val avgKmh = track.avgSpeed * KMH_CONVERSION

        return when {
            dow in Calendar.MONDAY..Calendar.FRIDAY &&
                    (hour in 6..9 || hour in 17..19) -> "Commute"
            dow in Calendar.SATURDAY..Calendar.SUNDAY -> "Leisure"
            avgKmh < 10.0 -> "Exercise"
            else -> "General Journey"
        }
    }

    private fun detectAnomalies(track: SavedTrack, points: List<LocationData>): List<String> {
        val anomalies = mutableListOf<String>()
        if (track.wasMockLocationUsed) anomalies += "Mock locations detected, affecting data reliability"
        if (track.wasAppKilled)        anomalies += "App was terminated during tracking, causing potential data gaps"
        if (track.wasPhoneShutDown)    anomalies += "Device was restarted during tracking, interrupting data collection"

        if (track.totalLocationPoints > 0 && track.duration > 0) {
            val pointsPerMinute = track.totalLocationPoints.toDouble() / (track.duration / 60_000.0)
            if (pointsPerMinute < 3.0) {
                anomalies += "Low data density (%.1f points/min)".format(pointsPerMinute)
            }
        }
        return anomalies
    }
}
