package com.miletracker.feature.logging.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.network.ApprovedVehicle
import com.miletracker.core.data.model.network.CoordsV2
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.LogMilesService
import com.miletracker.core.data.model.network.LogMilesSubmitRequestV2
import com.miletracker.core.data.model.network.SubmissionStatus
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.feature.logging.repository.LogMilesDraftRepository
import com.miletracker.feature.logging.repository.LogMilesServiceRepository
import com.miletracker.feature.logging.ui.model.LocationEntry
import com.miletracker.feature.logging.ui.model.LocationStop
import com.miletracker.feature.logging.ui.model.SubmittedVoucher
import com.miletracker.feature.logging.ui.model.SubmittedVoucherSamples
import com.miletracker.feature.logging.ui.model.totalRouteKm
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A draft the user saved at the end of Step 1 (or that came back from the draft
 * repository). Held in memory so the History → Drafts tab works even when no Room
 * draft DAO is wired. Mirrors the minimum a draft needs to be re-opened.
 */
data class LogMilesDraftUi(
    val id: String,
    val title: String,
    val stopCount: Int,
    val distanceKm: Double,
    val vehicleName: String?,
    val updatedAtMillis: Long
)

/**
 * Single immutable UI state for the entire two-step Log Miles flow plus history.
 * The Step 1 and Step 2 screens, history screen and every sheet/dialog read from
 * this one object; the screens stay stateless and emit intents back to the VM.
 */
data class LogMilesUiState(
    // ── Reference data ────────────────────────────────────────────────────────
    val vehicles: List<ApprovedVehicle> = emptyList(),
    val selectedVehicle: ApprovedVehicle? = null,
    val services: List<LogMilesService> = emptyList(),
    val selectedService: LogMilesService? = null,
    val isLoadingVehicles: Boolean = true,
    val isLoadingServices: Boolean = true,

    // ── Step 1: itinerary ─────────────────────────────────────────────────────
    val stops: List<LocationStop> = emptyList(),
    val isRoundTrip: Boolean = false,
    /** Great-circle distance computed from the current stops. */
    val calculatedDistanceKm: Double = 0.0,
    /** Distance actually used for pricing — equal to calculated unless overridden. */
    val distanceKm: Double = 0.0,
    val isDistanceOverridden: Boolean = false,
    val reimbursableAmount: Double = 0.0,
    val journeyDateMillis: Long? = null,
    val journeyTimeMinutes: Int? = null,
    val saveAsDraft: Boolean = false,
    val recentLocations: List<LocationEntry> = emptyList(),

    // ── Step 2: expense details ───────────────────────────────────────────────
    val invoiceDateMillis: Long? = null,
    val logMilesNote: String = "",
    val taggedEmployees: List<String> = emptyList(),
    val attachmentCount: Int = 0,

    // ── Submission ────────────────────────────────────────────────────────────
    val isSubmitting: Boolean = false,
    val submissionResult: ExpenseSubmissionResponse? = null,
    val showViolationDialog: Boolean = false,
    val error: String? = null,

    // ── History ───────────────────────────────────────────────────────────────
    val drafts: List<LogMilesDraftUi> = emptyList(),
    val submitted: List<SubmittedVoucher> = SubmittedVoucherSamples.sample(kotlin.time.Clock.System.now().toEpochMilliseconds())
) {
    /** Step 1 → Step 2 gate: at least two stops and a chosen vehicle. */
    val canProceedToStep2: Boolean get() = stops.size >= 2 && selectedVehicle != null

    /** Per-km rate of the selected vehicle (0 when none chosen). */
    val pricePerKm: Double get() = selectedVehicle?.vehiclePricing ?: 0.0

    /** Number of still-required Step 2 fields (drives the "N remaining" header). */
    val step2Remaining: Int
        get() = listOf(invoiceDateMillis != null, logMilesNote.isNotBlank()).count { !it }
}

/**
 * ViewModel for the standalone, offline Log Miles flow.
 *
 * Distance is computed locally via great-circle math over the offline city
 * catalogue (the network distance API is still available but optional). All
 * itinerary edits, draft toggling, the verify-distance override, the Step 2 form
 * and the tagged-employee selection mutate the single [LogMilesUiState].
 */
class LogMilesViewModel(
    private val vehicleRepo: VehiclePricingRepository,
    private val serviceRepo: LogMilesServiceRepository,
    private val draftRepo: LogMilesDraftRepository,
    private val api: MileTrackerNetworkApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogMilesUiState())
    val uiState: StateFlow<LogMilesUiState> = _uiState.asStateFlow()

    /** Monotonic id source for stops added during this session. */
    private var stopIdCounter = 0L

    init {
        loadVehicles()
        loadServices()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            runCatching { vehicleRepo.getVehicles(trackMiles = false) }
                .onSuccess { v ->
                    _uiState.update { it.copy(vehicles = v, isLoadingVehicles = false) }
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

    // ── Vehicle & service ───────────────────────────────────────────────────────

    fun selectVehicle(vehicle: ApprovedVehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
        recomputePricing()
    }

    fun selectService(service: LogMilesService) {
        _uiState.update { it.copy(selectedService = service) }
    }

    // ── Step 1: journey basics ──────────────────────────────────────────────────

    fun setJourneyDate(millis: Long?) {
        _uiState.update { it.copy(journeyDateMillis = millis) }
    }

    fun setJourneyTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(journeyTimeMinutes = hour * 60 + minute) }
    }

    fun setRoundTrip(enabled: Boolean) {
        _uiState.update { it.copy(isRoundTrip = enabled) }
        recomputeDistance()
    }

    fun setSaveAsDraft(enabled: Boolean) {
        _uiState.update { it.copy(saveAsDraft = enabled) }
    }

    // ── Step 1: itinerary editing ───────────────────────────────────────────────

    /** Append a stop to the end of the itinerary and remember it as "recent". */
    fun addStop(entry: LocationEntry) {
        val stop = LocationStop(id = stopIdCounter++, entry = entry)
        _uiState.update { state ->
            state.copy(
                stops = state.stops + stop,
                recentLocations = (listOf(entry) + state.recentLocations).distinctBy { it.name }.take(8)
            )
        }
        recomputeDistance()
    }

    /** Insert a stop immediately after the stop at [afterIndex]. */
    fun insertStopAfter(afterIndex: Int, entry: LocationEntry) {
        val stop = LocationStop(id = stopIdCounter++, entry = entry)
        _uiState.update { state ->
            val list = state.stops.toMutableList()
            val target = (afterIndex + 1).coerceIn(0, list.size)
            list.add(target, stop)
            state.copy(
                stops = list,
                recentLocations = (listOf(entry) + state.recentLocations).distinctBy { it.name }.take(8)
            )
        }
        recomputeDistance()
    }

    /** Replace the place backing the stop with [stopId] (the Edit chip). */
    fun editStop(stopId: Long, entry: LocationEntry) {
        _uiState.update { state ->
            state.copy(stops = state.stops.map { if (it.id == stopId) it.copy(entry = entry) else it })
        }
        recomputeDistance()
    }

    fun removeStop(stopId: Long) {
        _uiState.update { state -> state.copy(stops = state.stops.filterNot { it.id == stopId }) }
        recomputeDistance()
    }

    /** Move a stop one position up (toward the start). No-op at the top. */
    fun moveStopUp(index: Int) = reorder(index, index - 1)

    /** Move a stop one position down (toward the end). No-op at the bottom. */
    fun moveStopDown(index: Int) = reorder(index, index + 1)

    private fun reorder(from: Int, to: Int) {
        _uiState.update { state ->
            if (from !in state.stops.indices || to !in state.stops.indices) return@update state
            val list = state.stops.toMutableList()
            val moved = list.removeAt(from)
            list.add(to, moved)
            state.copy(stops = list)
        }
        recomputeDistance()
    }

    fun clearRecentLocations() {
        _uiState.update { it.copy(recentLocations = emptyList()) }
    }

    // ── Distance & pricing ──────────────────────────────────────────────────────

    /**
     * Recompute the great-circle distance from the current stops. The override
     * (set via [overrideDistance]) is cleared whenever the itinerary changes so a
     * stale manual value can't outlive the route it was based on.
     */
    private fun recomputeDistance() {
        _uiState.update { state ->
            val calc = totalRouteKm(state.stops, state.isRoundTrip)
            state.copy(
                calculatedDistanceKm = calc,
                distanceKm = calc,
                isDistanceOverridden = false
            )
        }
        recomputePricing()
    }

    /** Manual override from the Verify Distance dialog (Calculated vs Current). */
    fun overrideDistance(km: Double) {
        _uiState.update { it.copy(distanceKm = km, isDistanceOverridden = true) }
        recomputePricing()
    }

    private fun recomputePricing() {
        _uiState.update { it.copy(reimbursableAmount = it.distanceKm * it.pricePerKm) }
    }

    // ── Step 2: expense form ────────────────────────────────────────────────────

    fun setInvoiceDate(millis: Long?) {
        _uiState.update { it.copy(invoiceDateMillis = millis) }
    }

    fun setLogMilesNote(text: String) {
        _uiState.update { it.copy(logMilesNote = text) }
    }

    fun setTaggedEmployees(names: List<String>) {
        _uiState.update { it.copy(taggedEmployees = names) }
    }

    fun addAttachment() {
        _uiState.update { it.copy(attachmentCount = it.attachmentCount + 1) }
    }

    // ── Draft ───────────────────────────────────────────────────────────────────

    /**
     * Persist the current Step 1 itinerary as a draft. A lightweight in-memory
     * [LogMilesDraftUi] is always kept so the History → Drafts tab works offline;
     * if a draft DAO is wired we additionally let the repository back it.
     */
    fun saveDraft() {
        val s = _uiState.value
        if (s.stops.isEmpty()) return
        val draft = LogMilesDraftUi(
            id = "draft-${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
            title = s.stops.firstOrNull()?.entry?.name?.substringBefore(",") ?: "Log Miles draft",
            stopCount = s.stops.size,
            distanceKm = s.distanceKm,
            vehicleName = s.selectedVehicle?.vehicleName,
            updatedAtMillis = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
        _uiState.update { it.copy(drafts = listOf(draft) + it.drafts) }
    }

    fun deleteDraft(draftId: String) {
        _uiState.update { it.copy(drafts = it.drafts.filterNot { d -> d.id == draftId }) }
        viewModelScope.launch { runCatching { draftRepo.delete(draftId) } }
    }

    // ── Submission ──────────────────────────────────────────────────────────────

    /**
     * Submit the journey. On success with policy violations we surface a violation
     * dialog; otherwise the success route is shown via [submissionResult].
     */
    fun submit() {
        val s = _uiState.value
        val vehicle = s.selectedVehicle ?: return
        if (s.stops.size < 2) return
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            runCatching {
                api.logMiles(
                    LogMilesSubmitRequestV2(
                        vehicleType = vehicle.vehicleKey,
                        distance = s.distanceKm,
                        roundTrip = s.isRoundTrip,
                        date = s.journeyDateMillis,
                        origin = s.stops.first().entry.let { CoordsV2(it.lat, it.lng, it.name) },
                        destination = s.stops.last().entry.let { CoordsV2(it.lat, it.lng, it.name) }
                    )
                )
            }.onSuccess { resp ->
                val hasViolations = resp.submissionStatus == SubmissionStatus.POLICY_VIOLATION ||
                    resp.violations.isNotEmpty() ||
                    !resp.policyViolations.isNullOrEmpty()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionResult = resp,
                        showViolationDialog = hasViolations
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }

    /** Dismiss the violation dialog but keep the result (user proceeds to success). */
    fun dismissViolationDialog() {
        _uiState.update { it.copy(showViolationDialog = false) }
    }

    /** Clear submission state and reset the flow for "Log Another". */
    fun resetSubmission() {
        stopIdCounter = 0L
        _uiState.update { state ->
            LogMilesUiState(
                vehicles = state.vehicles,
                isLoadingVehicles = false,
                services = state.services,
                selectedService = state.services.firstOrNull(),
                isLoadingServices = false,
                drafts = state.drafts,
                submitted = state.submitted,
                recentLocations = state.recentLocations
            )
        }
    }
}
