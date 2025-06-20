package com.miletracker.feature.logging.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.network.ApprovedVehicle
import com.miletracker.core.data.model.network.CoordsV2
import com.miletracker.core.data.model.network.DistanceRequestV2
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.LogMilesService
import com.miletracker.core.data.model.network.LogMilesSubmitRequestV2
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.feature.logging.repository.LogMilesServiceRepository
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogMilesUiState(
    val vehicles: List<ApprovedVehicle> = emptyList(),
    val selectedVehicle: ApprovedVehicle? = null,
    val services: List<LogMilesService> = emptyList(),
    val selectedService: LogMilesService? = null,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val startLabel: String = "",
    val endLabel: String = "",
    val distanceKm: Double = 0.0,
    val reimbursableAmount: Double = 0.0,
    val isRoundTrip: Boolean = false,
    val isLoadingVehicles: Boolean = true,
    val isLoadingServices: Boolean = true,
    val isSubmitting: Boolean = false,
    val submissionResult: ExpenseSubmissionResponse? = null,
    val error: String? = null
)

class LogMilesViewModel(
    private val vehicleRepo: VehiclePricingRepository,
    private val serviceRepo: LogMilesServiceRepository,
    private val api: MileTrackerNetworkApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogMilesUiState())
    val uiState: StateFlow<LogMilesUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
        loadServices()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            runCatching { vehicleRepo.getVehicles(trackMiles = false) }
                .onSuccess { v ->
                    _uiState.update { it.copy(vehicles = v, selectedVehicle = v.firstOrNull(), isLoadingVehicles = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoadingVehicles = false) } }
        }
    }

    private fun loadServices() {
        viewModelScope.launch {
            runCatching { serviceRepo.getServices() }
                .onSuccess { s ->
                    _uiState.update { it.copy(services = s, selectedService = s.firstOrNull(), isLoadingServices = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoadingServices = false) } }
        }
    }

    fun selectVehicle(vehicle: ApprovedVehicle) {
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                reimbursableAmount = it.distanceKm * (vehicle.vehiclePricing ?: 0.0)
            )
        }
    }

    fun selectService(service: LogMilesService) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun setDistance(km: Double) {
        val pricing = _uiState.value.selectedVehicle?.vehiclePricing ?: 0.0
        _uiState.update { it.copy(distanceKm = km, reimbursableAmount = km * pricing) }
    }

    fun setRoundTrip(enabled: Boolean) {
        val base = _uiState.value.distanceKm
        val pricing = _uiState.value.selectedVehicle?.vehiclePricing ?: 0.0
        val effectiveKm = if (enabled) base * 2 else base / 2
        _uiState.update { it.copy(isRoundTrip = enabled, reimbursableAmount = effectiveKm * pricing) }
    }

    fun setStart(lat: Double, lng: Double, label: String) {
        _uiState.update { it.copy(startLat = lat, startLng = lng, startLabel = label) }
        recalculateDistance()
    }

    fun setEnd(lat: Double, lng: Double, label: String) {
        _uiState.update { it.copy(endLat = lat, endLng = lng, endLabel = label) }
        recalculateDistance()
    }

    private fun recalculateDistance() {
        val s = _uiState.value
        val sLat = s.startLat ?: return
        val sLng = s.startLng ?: return
        val eLat = s.endLat ?: return
        val eLng = s.endLng ?: return
        viewModelScope.launch {
            runCatching {
                api.distance(DistanceRequestV2(coords = listOf(CoordsV2(sLat, sLng), CoordsV2(eLat, eLng))))
            }.onSuccess { resp -> setDistance(resp.distance) }
        }
    }

    fun submit() {
        val s = _uiState.value
        val vehicle = s.selectedVehicle ?: return
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                api.logMiles(
                    LogMilesSubmitRequestV2(
                        vehicleType = vehicle.vehicleKey,
                        distance = s.distanceKm,
                        roundTrip = s.isRoundTrip,
                        origin = s.startLat?.let { lat -> CoordsV2(lat, s.startLng!!, s.startLabel) },
                        destination = s.endLat?.let { lat -> CoordsV2(lat, s.endLng!!, s.endLabel) }
                    )
                )
            }.onSuccess { resp ->
                _uiState.update { it.copy(isSubmitting = false, submissionResult = resp) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }

    fun resetSubmission() {
        _uiState.update { it.copy(submissionResult = null, error = null) }
    }
}
