package com.miletracker.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class SavedTracksUiState(
    val tracks: List<TrackDisplayData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class SavedTracksViewModel(
    private val repository: SavedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedTracksUiState())
    val uiState: StateFlow<SavedTracksUiState> = _uiState.asStateFlow()

    init {
        repository.allTracksFlow()
            .onEach { tracks ->
                _uiState.update { it.copy(tracks = tracks, isLoading = false, error = null) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }
}
