package com.miletracker.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.SubmitMilesRequestK
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SubmissionUiState {
    object Idle : SubmissionUiState()
    object Submitting : SubmissionUiState()
    data class Success(val response: ExpenseSubmissionResponse) : SubmissionUiState()
    data class Error(val message: String) : SubmissionUiState()
}

class MileageSubmissionViewModel(
    private val api: MileTrackerNetworkApi,
    private val trackRepository: SavedTrackRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Idle)
    val state: StateFlow<SubmissionUiState> = _state.asStateFlow()

    fun submit(routeId: String, distanceKm: Double, vehicleKey: String, startTime: Long, endTime: Long) {
        _state.update { SubmissionUiState.Submitting }
        viewModelScope.launch {
            runCatching {
                api.submitMiles(
                    SubmitMilesRequestK(
                        token = routeId,
                        vehicleType = vehicleKey,
                        distance = distanceKm,
                        startTime = startTime,
                        endTime = endTime,
                        submissionTime = System.currentTimeMillis()
                    )
                )
            }.onSuccess { response ->
                val transId = response.transId ?: "DEMO-${System.currentTimeMillis()}"
                trackRepository.markSubmitted(routeId, transId, response.reimbursableAmount ?: 0.0)
                _state.update { SubmissionUiState.Success(response) }
            }.onFailure { e ->
                _state.update { SubmissionUiState.Error(e.message ?: "Submission failed") }
            }
        }
    }

    fun reset() { _state.update { SubmissionUiState.Idle } }
}
