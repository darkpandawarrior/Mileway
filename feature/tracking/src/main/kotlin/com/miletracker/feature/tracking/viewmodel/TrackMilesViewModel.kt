package com.miletracker.feature.tracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.SavedTrack
import com.miletracker.core.data.model.network.ApprovedVehicle
import com.miletracker.core.data.model.state.TrackMilesPluginConfig
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TrackMilesPhase { IDLE, TRACKING, PAUSED, STOPPED, SUBMITTED }

data class TrackMilesUiState(
    val phase: TrackMilesPhase = TrackMilesPhase.IDLE,
    val config: TrackMilesPluginConfig = TrackMilesPluginConfig(),
    val vehicles: List<ApprovedVehicle> = emptyList(),
    val selectedVehicle: ApprovedVehicle? = null,
    val currentRouteId: String? = null,
    val distanceKm: Double = 0.0,
    val durationMs: Long = 0L,
    val reimbursableAmount: Double = 0.0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TrackMilesViewModel(
    private val configManager: TrackingConfigManager,
    private val vehicleRepo: VehiclePricingRepository,
    private val trackRepo: SavedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackMilesUiState())
    val uiState: StateFlow<TrackMilesUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
        loadVehicles()
        restoreActiveTrack()
    }

    private fun loadConfig() {
        _uiState.update { it.copy(config = configManager.getTrackMilesConfig()) }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            runCatching { vehicleRepo.getVehicles(trackMiles = true) }
                .onSuccess { vehicles ->
                    _uiState.update { it.copy(vehicles = vehicles, selectedVehicle = vehicles.firstOrNull()) }
                }
                .onFailure { Log.w("TrackMilesVM", "Failed to load vehicles", it) }
        }
    }

    private fun restoreActiveTrack() {
        viewModelScope.launch {
            val active = trackRepo.getActiveTrack() ?: return@launch
            _uiState.update {
                it.copy(
                    phase = TrackMilesPhase.TRACKING,
                    currentRouteId = active.routeId,
                    distanceKm = active.distance / 1000.0
                )
            }
        }
    }

    fun selectVehicle(vehicle: ApprovedVehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
    }

    fun startTracking() {
        val vehicle = _uiState.value.selectedVehicle ?: return
        val routeId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val track = SavedTrack(
                routeId = routeId,
                name = "Journey ${java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(now))}",
                startLatitude = 0.0, startLongitude = 0.0,
                endLatitude = 0.0, endLongitude = 0.0,
                pausedLatitude = 0.0, pausedLongitude = 0.0,
                startTime = now, endTime = -1L,
                distance = 0.0, duration = 0L,
                selectedVehicleType = vehicle.vehicleKey ?: "",
                vehiclePricing = vehicle.vehiclePricing ?: 0.0,
                createdAt = now, startedAtTimestamp = now, startedByEmployeeCode = "EMP001"
            )
            trackRepo.insert(track)
            _uiState.update { it.copy(phase = TrackMilesPhase.TRACKING, currentRouteId = routeId, distanceKm = 0.0, startTime = now) }
        }
    }

    fun pauseTracking() {
        _uiState.update { it.copy(phase = TrackMilesPhase.PAUSED) }
    }

    fun resumeTracking() {
        _uiState.update { it.copy(phase = TrackMilesPhase.TRACKING) }
    }

    fun stopTracking() {
        _uiState.update { it.copy(phase = TrackMilesPhase.STOPPED, endTime = System.currentTimeMillis()) }
    }

    fun updateDistance(km: Double) {
        val pricing = _uiState.value.selectedVehicle?.vehiclePricing ?: 0.0
        _uiState.update { it.copy(distanceKm = km, reimbursableAmount = km * pricing) }
    }

    fun discardTracking() {
        _uiState.update { TrackMilesUiState(config = it.config, vehicles = it.vehicles, selectedVehicle = it.selectedVehicle) }
    }
}
