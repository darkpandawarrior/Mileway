package com.miletracker.feature.tracking.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.miletracker.core.ui.mvi.BaseViewModel
import com.miletracker.feature.tracking.export.TrackExportManager
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.ui.components.ExportFormat
import com.miletracker.feature.tracking.ui.components.LocationDataFilter
import kotlinx.coroutines.launch

data class ExportUiState(
    val isExporting: Boolean = false,
    val shareIntent: Intent? = null,
    val error: String? = null,
)

sealed interface ExportAction {
    data object ClearShareIntent : ExportAction

    data object ClearError : ExportAction
}

sealed interface ExportEffect

class ExportViewModel(
    private val trackRepository: SavedTrackRepository,
    private val locationRepository: LocationRepository,
    private val hardwareEventRepository: HardwareEventRepository,
) : BaseViewModel<ExportUiState, ExportEffect, ExportAction>(ExportUiState()) {
    override fun onAction(action: ExportAction) {
        when (action) {
            ExportAction.ClearShareIntent -> setState { copy(shareIntent = null) }
            ExportAction.ClearError -> setState { copy(error = null) }
        }
    }

    fun export(
        context: Context,
        routeId: String,
        format: ExportFormat,
        filter: LocationDataFilter,
    ) {
        setState { copy(isExporting = true, error = null, shareIntent = null) }

        viewModelScope.launch {
            try {
                val track =
                    trackRepository.getByRouteId(routeId)
                        ?: error("Track not found: $routeId")

                var locations = locationRepository.getForToken(routeId)

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

                setState { copy(isExporting = false, shareIntent = intent) }
            } catch (e: Exception) {
                setState { copy(isExporting = false, error = e.message ?: "Export failed") }
            }
        }
    }
}
