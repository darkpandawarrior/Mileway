package com.miletracker.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.SavedTrack
import com.miletracker.core.data.model.db.TripAttachmentEntity
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.core.data.model.display.toDisplayData
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.TripAttachmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackDetailUiState(
    val track: TrackDisplayData? = null,
    val rawTrack: SavedTrack? = null,
    val locations: List<LocationData> = emptyList(),
    val attachments: List<TripAttachmentEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TrackDetailViewModel(
    private val trackRepository: SavedTrackRepository,
    private val locationRepository: LocationRepository,
    private val attachmentRepository: TripAttachmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackDetailUiState())
    val uiState: StateFlow<TrackDetailUiState> = _uiState.asStateFlow()

    fun load(routeId: String) {
        viewModelScope.launch {
            val track = trackRepository.getByRouteId(routeId)
            _uiState.update { it.copy(track = track?.toDisplayData(), rawTrack = track, isLoading = false) }
        }
        locationRepository.locationsForToken(routeId)
            .onEach { locs -> _uiState.update { it.copy(locations = locs) } }
            .catch { e -> _uiState.update { it.copy(error = e.message) } }
            .launchIn(viewModelScope)

        attachmentRepository.attachmentsForTrack(routeId)
            .onEach { attachments -> _uiState.update { it.copy(attachments = attachments) } }
            .catch { e -> _uiState.update { it.copy(error = e.message) } }
            .launchIn(viewModelScope)
    }
}
