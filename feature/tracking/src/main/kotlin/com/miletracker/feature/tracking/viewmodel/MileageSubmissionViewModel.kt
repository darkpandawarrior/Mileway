package com.miletracker.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.TripAttachmentEntity
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.SubmitMilesRequestK
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.TripAttachmentRepository
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
    private val trackRepository: SavedTrackRepository,
    private val attachmentRepository: TripAttachmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Idle)
    val state: StateFlow<SubmissionUiState> = _state.asStateFlow()

    // ---------------------------------------------------------------------------
    // Pending attachments collected from UI before submit
    // ---------------------------------------------------------------------------

    /** URIs of receipt photos staged by the user (not yet persisted). */
    private val _pendingReceipts = MutableStateFlow<List<String>>(emptyList())
    val pendingReceipts: StateFlow<List<String>> = _pendingReceipts.asStateFlow()

    /** URI + OCR of the odometer-start photo staged by the user. */
    private val _pendingOdoStart = MutableStateFlow<Pair<String, String?>?>(null)
    val pendingOdoStart: StateFlow<Pair<String, String?>?> = _pendingOdoStart.asStateFlow()

    /** URI + OCR of the odometer-end photo staged by the user. */
    private val _pendingOdoEnd = MutableStateFlow<Pair<String, String?>?>(null)
    val pendingOdoEnd: StateFlow<Pair<String, String?>?> = _pendingOdoEnd.asStateFlow()

    // ---------------------------------------------------------------------------
    // Staging (called from UI as photos are captured)
    // ---------------------------------------------------------------------------

    fun addReceipt(uri: String) {
        _pendingReceipts.update { it + uri }
    }

    fun removeReceipt(uri: String) {
        _pendingReceipts.update { it - uri }
    }

    fun setOdometerStart(uri: String, ocrText: String?) {
        _pendingOdoStart.value = uri to ocrText
    }

    fun setOdometerEnd(uri: String, ocrText: String?) {
        _pendingOdoEnd.value = uri to ocrText
    }

    // ---------------------------------------------------------------------------
    // Submit: persist attachments locally, then record the trip as submitted
    // ---------------------------------------------------------------------------

    fun submit(routeId: String, distanceKm: Double, vehicleKey: String, startTime: Long, endTime: Long) {
        _state.update { SubmissionUiState.Submitting }
        viewModelScope.launch {
            // 1. Persist all staged attachments to local Room before any network call.
            persistPendingAttachments(routeId)

            // 2. Call the (stub) network API.
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

    private suspend fun persistPendingAttachments(routeId: String) {
        _pendingReceipts.value.forEach { uri ->
            attachmentRepository.addReceipt(routeId, uri)
        }
        _pendingOdoStart.value?.let { (uri, ocr) ->
            attachmentRepository.setOdometerStart(routeId, uri, ocr)
        }
        _pendingOdoEnd.value?.let { (uri, ocr) ->
            attachmentRepository.setOdometerEnd(routeId, uri, ocr)
        }
    }

    fun reset() {
        _state.update { SubmissionUiState.Idle }
        _pendingReceipts.value = emptyList()
        _pendingOdoStart.value = null
        _pendingOdoEnd.value = null
    }
}
