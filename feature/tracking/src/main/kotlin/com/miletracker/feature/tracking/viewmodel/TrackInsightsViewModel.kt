package com.miletracker.feature.tracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.SavedTrack
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

data class TrackInsightData(
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val distanceKm: Double,
    val durationMs: Long,
    val locationCount: Int,
    val mockLocationCount: Int,
    val abnormalLocationCount: Int,
    val pauseCount: Int,
    val qualityScore: Int,
    val qualityLabel: String,
    val activityBreakdown: Map<String, Int>,
    val recommendations: List<String>
)

class TrackInsightsViewModel(
    private val savedTrackRepository: SavedTrackRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    companion object { private const val TAG = "TrackInsightsVM" }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _track = MutableStateFlow<SavedTrack?>(null)
    val track: StateFlow<SavedTrack?> = _track

    private val _insights = MutableStateFlow<TrackInsightData?>(null)
    val insights: StateFlow<TrackInsightData?> = _insights

    fun loadTrackInsights(routeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val track = savedTrackRepository.getByRouteId(routeId) ?: run {
                    _error.value = "Track not found"
                    _isLoading.value = false
                    return@launch
                }
                _track.value = track

                val locations = locationRepository.getForToken(routeId)
                _insights.value = analyzeLocally(track, locations)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading insights", e)
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun analyzeLocally(track: SavedTrack, locations: List<LocationData>): TrackInsightData {
        val mockCount = locations.count { it.isMock }
        val abnormalCount = locations.count { it.isAbnormal }
        val pauseCount = locations.count { it.isPaused }

        val activityBreakdown = locations.groupBy { it.activity }
            .mapValues { it.value.size }

        val avgSpeedKmh = track.avgSpeed.let { if (it > 0) it * 3.6 else 0.0 }
        val maxSpeedKmh = track.maxSpeed.let { if (it > 0) it * 3.6 else 0.0 }
        val distanceKm = track.distance / 1000.0
        val durationMs = max(0L, track.endTime - track.startTime)

        val qualityScore = computeQualityScore(track, locations, mockCount, abnormalCount)
        val qualityLabel = when {
            qualityScore >= 90 -> "Excellent"
            qualityScore >= 75 -> "Good"
            qualityScore >= 55 -> "Fair"
            qualityScore >= 35 -> "Poor"
            else -> "Critical"
        }

        val recommendations = buildRecommendations(track, mockCount, abnormalCount)

        return TrackInsightData(
            avgSpeedKmh = avgSpeedKmh,
            maxSpeedKmh = maxSpeedKmh,
            distanceKm = distanceKm,
            durationMs = durationMs,
            locationCount = locations.size,
            mockLocationCount = mockCount,
            abnormalLocationCount = abnormalCount,
            pauseCount = pauseCount,
            qualityScore = qualityScore,
            qualityLabel = qualityLabel,
            activityBreakdown = activityBreakdown,
            recommendations = recommendations
        )
    }

    private fun computeQualityScore(
        track: SavedTrack, locations: List<LocationData>,
        mockCount: Int, abnormalCount: Int
    ): Int {
        var score = 100
        if (locations.isEmpty()) return 0
        val mockRatio = mockCount.toDouble() / locations.size
        val abnormalRatio = abnormalCount.toDouble() / locations.size
        score -= (mockRatio * 40).roundToInt()
        score -= (abnormalRatio * 25).roundToInt()
        if (track.wasBatteryOptimizationEnabled) score -= 10
        if (track.wasPowerSaverEnabled) score -= 10
        if (track.wasAppKilled) score -= 15
        if (track.wasPermissionsViolated) score -= 20
        return score.coerceIn(0, 100)
    }

    private fun buildRecommendations(track: SavedTrack, mockCount: Int, abnormalCount: Int): List<String> {
        val recs = mutableListOf<String>()
        if (mockCount > 0) recs.add("Disable mock location apps for accurate distance tracking.")
        if (abnormalCount > 5) recs.add("Keep the device in an open area for better GPS signal.")
        if (track.wasBatteryOptimizationEnabled) recs.add("Disable battery optimization for MileTracker to avoid interruptions.")
        if (track.wasPowerSaverEnabled) recs.add("Turn off power saver mode while tracking for best accuracy.")
        if (track.wasAppKilled) recs.add("Avoid closing the app while tracking — use the pause button instead.")
        if (recs.isEmpty()) recs.add("Great tracking session! GPS accuracy was consistent throughout.")
        return recs
    }
}
