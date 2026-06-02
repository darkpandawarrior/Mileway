package com.miletracker.feature.tracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.CurrentTrackData
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.state.UiState
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class LiveTrackingUiState {
    object Initial : LiveTrackingUiState()

    object Loading : LiveTrackingUiState()

    data class Success(val trackData: CurrentTrackData, val locationPoints: List<LocationData>) : LiveTrackingUiState()

    data class Error(val message: String) : LiveTrackingUiState()
}

class LiveTrackViewModel(
    private val locationRepository: LocationRepository,
    private val currentTrackRepository: CurrentTrackRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "LiveTrackViewModel"
        private const val AUTO_REFRESH_INTERVAL_MS = 5000L
    }

    private val _currentTrackState = MutableStateFlow<UiState<CurrentTrackData>>(UiState.Initial)
    val currentTrackState: StateFlow<UiState<CurrentTrackData>> = _currentTrackState.asStateFlow()

    private val _locationPointsState = MutableStateFlow<UiState<List<LocationData>>>(UiState.Initial)
    val locationPointsState: StateFlow<UiState<List<LocationData>>> = _locationPointsState.asStateFlow()

    private val _liveTrackingState = MutableStateFlow<LiveTrackingUiState>(LiveTrackingUiState.Initial)
    val liveTrackingState: StateFlow<LiveTrackingUiState> = _liveTrackingState.asStateFlow()

    private val _hardwareEventsState = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val hardwareEventsState: StateFlow<List<Pair<String, Long>>> = _hardwareEventsState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var locationObserverJob: Job? = null

    init {
        initializeTrackingData()
        setupCombinedState()
    }

    private fun initializeTrackingData() {
        viewModelScope.launch {
            try {
                loadCurrentTrackData()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing tracking data", e)
                _currentTrackState.value = UiState.Error("Failed to initialize: ${e.message}")
            }
        }
    }

    private fun setupCombinedState() {
        viewModelScope.launch {
            combine(_currentTrackState, _locationPointsState) { trackState, locationState ->
                when {
                    trackState is UiState.Loading || locationState is UiState.Loading ->
                        LiveTrackingUiState.Loading
                    trackState is UiState.Error ->
                        LiveTrackingUiState.Error(trackState.message)
                    locationState is UiState.Error ->
                        LiveTrackingUiState.Error(locationState.message)
                    trackState is UiState.Success && locationState is UiState.Success ->
                        LiveTrackingUiState.Success(trackState.data, locationState.data)
                    trackState is UiState.Success ->
                        LiveTrackingUiState.Success(trackState.data, emptyList())
                    else -> LiveTrackingUiState.Initial
                }
            }.catch { e ->
                emit(LiveTrackingUiState.Error("Error: ${e.message}"))
            }.collect { _liveTrackingState.value = it }
        }
    }

    private suspend fun loadCurrentTrackData() {
        _currentTrackState.value = UiState.Loading
        currentTrackRepository.getCurrentTrackDataRawAsync().fold(
            onSuccess = { trackData ->
                _currentTrackState.value = UiState.Success(trackData)
                currentTrackRepository.getHardwareEventQueueSnapshot()
                    .onSuccess { _hardwareEventsState.value = it }

                if (trackData.isTracking && trackData.token.isNotEmpty()) {
                    startLocationObservation(trackData.token)
                    startAutoRefresh()
                } else {
                    stopLocationObservation()
                    stopAutoRefresh()
                }
            },
            onFailure = { e ->
                Log.e(TAG, "Failed to load track data", e)
                _currentTrackState.value = UiState.Error("Failed to load: ${e.message}")
            },
        )
    }

    private fun startLocationObservation(token: String) {
        locationObserverJob?.cancel()
        locationObserverJob =
            viewModelScope.launch {
                _locationPointsState.value = UiState.Loading
                locationRepository.locationsForToken(token)
                    .catch { e ->
                        _locationPointsState.value = UiState.Error("Failed to load locations: ${e.message}")
                    }
                    .collect { locations ->
                        _locationPointsState.value = UiState.Success(locations)
                    }
            }
    }

    private fun stopLocationObservation() {
        locationObserverJob?.cancel()
        locationObserverJob = null
        _locationPointsState.value = UiState.Success(emptyList())
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob =
            viewModelScope.launch {
                while (true) {
                    delay(AUTO_REFRESH_INTERVAL_MS)
                    val state = _currentTrackState.value
                    if (state is UiState.Success && state.data.isTracking) {
                        refreshCurrentTrackData()
                    } else {
                        break
                    }
                }
            }
    }

    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    private suspend fun refreshCurrentTrackData() {
        currentTrackRepository.getCurrentTrackDataRawAsync().fold(
            onSuccess = { trackData ->
                val prev = _currentTrackState.value
                _currentTrackState.value = UiState.Success(trackData)
                if (prev is UiState.Success && prev.data.isTracking != trackData.isTracking) {
                    if (trackData.isTracking && trackData.token.isNotEmpty()) {
                        startLocationObservation(trackData.token)
                    } else {
                        stopLocationObservation()
                        stopAutoRefresh()
                    }
                }
                currentTrackRepository.getHardwareEventQueueSnapshot()
                    .onSuccess { _hardwareEventsState.value = it }
            },
            onFailure = { Log.w(TAG, "Refresh failed") },
        )
    }

    fun refreshTrackingData() {
        viewModelScope.launch { loadCurrentTrackData() }
    }

    fun clearError() {
        if (_currentTrackState.value is UiState.Error) _currentTrackState.value = UiState.Initial
        if (_locationPointsState.value is UiState.Error) _locationPointsState.value = UiState.Initial
    }

    override fun onCleared() {
        autoRefreshJob?.cancel()
        locationObserverJob?.cancel()
        super.onCleared()
    }
}
