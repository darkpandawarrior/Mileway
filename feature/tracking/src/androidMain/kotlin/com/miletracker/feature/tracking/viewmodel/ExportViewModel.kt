package com.miletracker.feature.tracking.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.feature.tracking.export.TrackExportManager
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.ui.components.ExportFormat
import com.miletracker.feature.tracking.ui.components.LocationDataFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportUiState(
    val isExporting: Boolean = false,
    val shareIntent: Intent? = null,
    val error: String? = null,
)

class ExportViewModel(
    private val trackRepository: SavedTrackRepository,
    private val locationRepository: LocationRepository,
    private val hardwareEventRepository: HardwareEventRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun export(
        context: Context,
        routeId: String,
        format: ExportFormat,
        filter: LocationDataFilter,
    ) {
        _uiState.update { it.copy(isExporting = true, error = null, shareIntent = null) }

        viewModelScope.launch {
            try {
                val track =
                    trackRepository.getByRouteId(routeId)
                        ?: error("Track not found: $routeId")

                var locations = locationRepository.getForToken(routeId)

                // Apply filters
                if (filter.excludeMock) locations = locations.filter { !it.isMock }
                if (filter.excludeAbnormal) locations = locations.filter { !it.isAbnormal }
                if (filter.excludePaused) locations = locations.filter { !it.isPaused }
                if (filter.onlyCheckpoints) locations = locations.filter { it.wasCheckInPoint }
                filter.minAccuracy?.let { min -> locations = locations.filter { it.accuracy >= min } }
                filter.maxAccuracy?.let { max -> locations = locations.filter { it.accuracy <= max } }
                filter.minBatteryLevel?.let { minBat ->
                    locations = locations.filter { it.batteryPercentage >= minBat }
                }

                val events = hardwareEventRepository.getEventsForRoute(routeId).getOrElse { emptyList() }

                val content = TrackExportManager.buildContent(format, track, locations, events)
                val intent = TrackExportManager.buildShareIntent(context, format, track.name, content)

                _uiState.update { it.copy(isExporting = false, shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message ?: "Export failed") }
            }
        }
    }

    /** Call after the share intent has been consumed so it isn't re-fired. */
    fun clearShareIntent() {
        _uiState.update { it.copy(shareIntent = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
