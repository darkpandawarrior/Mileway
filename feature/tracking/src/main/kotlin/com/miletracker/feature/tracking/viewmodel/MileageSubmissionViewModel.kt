package com.miletracker.feature.tracking.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.display.OdometerCaptureResult
import com.miletracker.core.data.model.display.OdometerPurpose
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.PolicyViolation
import com.miletracker.core.data.model.network.SubmissionStatus
import com.miletracker.core.data.model.network.SubmitMilesRequestK
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.core.network.model.BusinessEntity
import com.miletracker.core.network.model.Office
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import com.miletracker.feature.tracking.repository.OfflinePlacesRepository
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

/** A required custom-form field rendered in the "Additional Details" section. */
enum class SubmissionFieldType { TEXT, DROPDOWN }

data class SubmissionField(
    val id: String,
    val label: String,
    val type: SubmissionFieldType,
    val required: Boolean = true,
    val options: List<String> = emptyList(),
)

/** Which submission sheet (if any) is presented. */
enum class SubmissionSheet { NONE, SUBMIT_CONFIRM, POLICY_VIOLATION, OFFICE_PICKER, ENTITY_PICKER, SMART_DISTANCE }

/** All form + policy + sheet state for the submission screen (single source of truth). */
@Stable
data class SubmissionFormUi(
    val offices: List<Office> = emptyList(),
    val entities: List<BusinessEntity> = emptyList(),
    val officeRequired: Boolean = false,
    val selectedOffice: Office? = null,
    val selectedEntity: BusinessEntity? = null,
    val fields: List<SubmissionField> = emptyList(),
    val values: Map<String, String> = emptyMap(),
    val saveAsDraft: Boolean = false,
    val sheet: SubmissionSheet = SubmissionSheet.NONE,
    val officeQuery: String = "",
    val entityQuery: String = "",
    val violations: List<PolicyViolation> = emptyList(),
    val askAuthorities: Boolean = false,
    val violationNote: String = "",
    val smartDistanceTrackedKm: Double = 0.0,
    val smartDistanceOdometerKm: Double = 0.0,
    // Loaded from Room on init — exposed to avoid hardcoded strings in the screen.
    val startAddress: String = "",
    val endAddress: String = "",
    val vehicleName: String = "",
    val vehicleRatePerKm: Double = 0.0,
    // Odometer readings stored in VM state (from camera capture or simulation).
    val simulatedStartOdo: Int? = null,
    val simulatedEndOdo: Int? = null,
    val isManualStartOdo: Boolean = false,
    val isManualEndOdo: Boolean = false,
    val odometerStartImageUri: String? = null,
    val odometerEndImageUri: String? = null,
    val odometerStartCaptureMs: Long? = null,
    val odometerEndCaptureMs: Long? = null,
) {
    /** Required items still missing — drives the "N remaining" checklist header. */
    val remainingRequirements: List<String>
        get() = buildList {
            if (officeRequired && selectedOffice == null) add("Office selection")
            if (officeRequired && selectedEntity == null) add("Entity selection")
            val missingFields = fields.count { it.required && values[it.id].isNullOrBlank() }
            if (missingFields > 0) add("Complete required fields")
        }

    val canSubmit: Boolean get() = remainingRequirements.isEmpty()
}

class MileageSubmissionViewModel(
    private val api: MileTrackerNetworkApi,
    private val trackRepository: SavedTrackRepository,
    private val attachmentRepository: TripAttachmentRepository,
    private val configManager: TrackingConfigManager,
) : ViewModel() {

    private val _state = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Idle)
    val state: StateFlow<SubmissionUiState> = _state.asStateFlow()

    private val _form = MutableStateFlow(
        SubmissionFormUi(
            offices = configManager.getOffices(),
            entities = configManager.getBusinessEntities(),
            officeRequired = configManager.isOfficeSelectionRequired(),
            // Two representative required fields (the source uses server-driven forms).
            fields = listOf(
                SubmissionField("purpose", "Purpose of travel", SubmissionFieldType.TEXT),
                SubmissionField(
                    "gender", "Gender", SubmissionFieldType.DROPDOWN,
                    options = listOf("Male", "Female", "Others"),
                ),
            ),
        )
    )
    val form: StateFlow<SubmissionFormUi> = _form.asStateFlow()

    /** Snapshot of the last submission response, for the success screen. */
    private val _lastResponse = MutableStateFlow<ExpenseSubmissionResponse?>(null)
    val lastResponse: StateFlow<ExpenseSubmissionResponse?> = _lastResponse.asStateFlow()

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
    // Submission form intents (single source of truth in the VM)
    // ---------------------------------------------------------------------------

    fun setFormValue(id: String, value: String) =
        _form.update { it.copy(values = it.values + (id to value)) }

    fun toggleDraft(enabled: Boolean) = _form.update { it.copy(saveAsDraft = enabled) }

    fun openOfficePicker() = _form.update { it.copy(sheet = SubmissionSheet.OFFICE_PICKER) }
    fun openEntityPicker() = _form.update { it.copy(sheet = SubmissionSheet.ENTITY_PICKER) }
    fun setOfficeQuery(q: String) = _form.update { it.copy(officeQuery = q) }
    fun setEntityQuery(q: String) = _form.update { it.copy(entityQuery = q) }
    fun selectOffice(code: String) = _form.update {
        it.copy(selectedOffice = it.offices.firstOrNull { o -> o.code == code }, sheet = SubmissionSheet.NONE)
    }
    fun selectEntity(name: String) = _form.update {
        it.copy(selectedEntity = it.entities.firstOrNull { e -> e.name == name }, sheet = SubmissionSheet.NONE)
    }

    fun openSubmitConfirm() = _form.update { it.copy(sheet = SubmissionSheet.SUBMIT_CONFIRM) }
    fun openSmartDistanceSheet(trackedKm: Double, odometerKm: Double) = _form.update {
        it.copy(
            sheet = SubmissionSheet.SMART_DISTANCE,
            smartDistanceTrackedKm = trackedKm,
            smartDistanceOdometerKm = odometerKm,
        )
    }
    fun dismissSheet() = _form.update { it.copy(sheet = SubmissionSheet.NONE) }

    fun setAskAuthorities(enabled: Boolean) = _form.update { it.copy(askAuthorities = enabled) }
    fun setViolationNote(note: String) = _form.update { it.copy(violationNote = note) }

    /** True once the policy-violation resolution is complete enough to proceed. */
    val policyResolved: Boolean
        get() = _form.value.askAuthorities && _form.value.violationNote.isNotBlank()

    // ---------------------------------------------------------------------------
    // Track info loading (addresses, vehicle)
    // ---------------------------------------------------------------------------

    /**
     * Loads start/end addresses and vehicle info from Room for the given routeId.
     * Safe to call multiple times — subsequent calls are no-ops if already loaded.
     */
    fun loadTrackInfo(routeId: String, vehicleKey: String, distanceKm: Double) {
        if (_form.value.startAddress.isNotEmpty()) return
        viewModelScope.launch {
            val track = trackRepository.getByRouteId(routeId)
            val startAddr = if (track != null)
                OfflinePlacesRepository.addressFor(track.startLatitude, track.startLongitude)
            else "Start Location"
            val endAddr = if (track != null)
                OfflinePlacesRepository.addressFor(track.endLatitude, track.endLongitude)
            else "End Location"
            // Resolve vehicle display name and rate from the vehicles list.
            val vehicles = runCatching { api.vehicles(trackMiles = true).vehicles }.getOrElse { emptyList() }
            val vehicle = vehicles.firstOrNull { it.vehicleKey == vehicleKey }
            _form.update {
                it.copy(
                    startAddress = startAddr,
                    endAddress = endAddr,
                    vehicleName = vehicle?.vehicleName ?: vehicleKey,
                    vehicleRatePerKm = vehicle?.vehiclePricing ?: 0.0,
                )
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Odometer simulation (stored in VM, not local remember)
    // ---------------------------------------------------------------------------

    /** Simulate capturing the start odometer (uses a plausible base reading). */
    fun simulateCaptureStartOdo(distanceKm: Double) {
        val baseReading = 45_000
        _form.update { it.copy(simulatedStartOdo = baseReading, isManualStartOdo = false) }
    }

    /** Simulate capturing the end odometer based on the start reading + distance. */
    fun simulateCaptureEndOdo(distanceKm: Double) {
        val start = _form.value.simulatedStartOdo ?: 45_000
        _form.update {
            it.copy(
                simulatedEndOdo = start + distanceKm.toInt().coerceAtLeast(1),
                isManualEndOdo = false
            )
        }
    }

    /** Store a real camera capture result for the START odometer. */
    fun captureOdometerStart(result: OdometerCaptureResult) {
        _form.update {
            it.copy(
                simulatedStartOdo = result.reading,
                isManualStartOdo = result.isManual,
                odometerStartImageUri = result.imageUri,
                odometerStartCaptureMs = result.captureTimeMs,
            )
        }
    }

    /** Store a real camera capture result for the END odometer. Recomputes distance. */
    fun captureOdometerEnd(result: OdometerCaptureResult) {
        val startReading = _form.value.simulatedStartOdo ?: 45_000
        val distKm = (result.reading - startReading).coerceAtLeast(0).toDouble()
        _form.update {
            it.copy(
                simulatedEndOdo = result.reading,
                isManualEndOdo = result.isManual,
                odometerEndImageUri = result.imageUri,
                odometerEndCaptureMs = result.captureTimeMs,
                smartDistanceOdometerKm = distKm,
            )
        }
    }

    // ---------------------------------------------------------------------------
    // Submit: persist attachments locally, then record the trip as submitted
    // ---------------------------------------------------------------------------

    // Finalize parameters stashed while a policy-violation response awaits resolution.
    private var pendingFinalize: PendingFinalize? = null

    private data class PendingFinalize(val routeId: String, val response: ExpenseSubmissionResponse)

    fun submit(routeId: String, distanceKm: Double, vehicleKey: String, startTime: Long, endTime: Long) {
        _form.update { it.copy(sheet = SubmissionSheet.NONE) }
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
                _lastResponse.value = response
                val needsResolution = response.submissionStatus == SubmissionStatus.POLICY_VIOLATION ||
                    response.submissionStatus == SubmissionStatus.HARD_STOP
                if (needsResolution && response.violations.isNotEmpty()) {
                    // Park the response and surface the policy-violation sheet for the user
                    // to choose a resolution before the expense is finalized.
                    pendingFinalize = PendingFinalize(routeId, response)
                    _form.update { it.copy(violations = response.violations, sheet = SubmissionSheet.POLICY_VIOLATION) }
                    _state.update { SubmissionUiState.Idle }
                } else {
                    finalize(routeId, response)
                }
            }.onFailure { e ->
                _state.update { SubmissionUiState.Error(e.message ?: "Submission failed") }
            }
        }
    }

    /** Called from the policy-violation sheet once the user picks a resolution + note. */
    fun resolvePolicyAndFinalize() {
        val pending = pendingFinalize ?: return
        _form.update { it.copy(sheet = SubmissionSheet.NONE) }
        finalize(pending.routeId, pending.response)
        pendingFinalize = null
    }

    private fun finalize(routeId: String, response: ExpenseSubmissionResponse) {
        viewModelScope.launch {
            val transId = response.transId ?: "DEMO-${System.currentTimeMillis()}"
            trackRepository.markSubmitted(routeId, transId, response.reimbursableAmount ?: 0.0)
            _state.update { SubmissionUiState.Success(response) }
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
