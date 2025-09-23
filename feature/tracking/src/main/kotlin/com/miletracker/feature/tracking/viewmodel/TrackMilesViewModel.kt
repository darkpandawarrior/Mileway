package com.miletracker.feature.tracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.SavedTrack
import com.miletracker.core.data.model.network.ApprovedVehicle
import com.miletracker.core.data.model.state.TrackMilesPluginConfig
import com.miletracker.feature.tracking.checkin.CheckInValidator.CheckInLocation
import com.miletracker.feature.tracking.manager.LocationTrackingController
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TrackMilesPhase { IDLE, TRACKING, PAUSED, STOPPED, SUBMITTED }

/** Signal-quality bucket for the gauge accent ring, derived from GPS point density. */
enum class TrackSignal { GOOD, FAIR, POOR }

/** Which face the hero gauge shows. */
enum class HeroGaugeMode { COMPASS, ACTIVITY }

/** Which bottom sheet (if any) the tracking screen is currently presenting. */
enum class TrackSheet { NONE, JOURNEY_GUIDE, VEHICLE_PICKER, VENDOR_PICKER, PAUSE, RESUME, CONSENT }

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
    // ── Live gauge / stats telemetry (from the foreground tracking session) ──────
    val speedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val bearingDegrees: Float = 0f,
    val totalPoints: Long = 0L,
    val unsyncedPoints: Long = 0L,
    val trackingActivity: String = "Stationary",
    val signal: TrackSignal = TrackSignal.GOOD,
    /** Human-readable current position (last fix coordinates; the demo has no geocoder). */
    val currentLocationLabel: String = "Waiting for location…",
    val gaugeMode: HeroGaugeMode = HeroGaugeMode.COMPASS,
    val pauseReason: String? = null,
    // ── Start-flow / sheet orchestration (single-source-of-truth in the VM) ──────
    val activeSheet: TrackSheet = TrackSheet.NONE,
    val vehicleQuery: String = "",
    val vendorQuery: String = "",
    val startOdometer: Int? = null,
    val draftEnabled: Boolean = false,
    val pauseSelectedReason: String? = null,
    val pauseCustomReason: String = "",
    val resumeNotes: String = "",
    val centers: List<CheckInLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** History count surfaced as the small chip on the hero card. */
    val pointsLabel: Int get() = totalPoints.toInt()
}

class TrackMilesViewModel(
    private val configManager: TrackingConfigManager,
    private val vehicleRepo: VehiclePricingRepository,
    private val trackRepo: SavedTrackRepository,
    private val trackingController: LocationTrackingController,
    private val currentTrackRepo: CurrentTrackRepository,
    private val locationRepo: LocationRepository,
    private val geoCheckInLocations: List<CheckInLocation> = emptyList()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackMilesUiState())
    val uiState: StateFlow<TrackMilesUiState> = _uiState.asStateFlow()

    private var liveObserveJob: Job? = null
    private var sessionObserveJob: Job? = null
    private var bearingObserveJob: Job? = null

    init {
        loadConfig()
        loadVehicles()
        observeSession()
        restoreActiveTrack()
    }

    /**
     * Stream the live foreground session (speed, point counts, activity, pause reason)
     * straight into the gauge/stats telemetry. Independent of the per-route distance feed.
     */
    private fun observeSession() {
        sessionObserveJob?.cancel()
        sessionObserveJob = currentTrackRepo.currentTrackFlow
            .onEach { s ->
                // Signal quality from point density: dense fixes = good, sparse = poor.
                val signal = when {
                    s.totalLocationPoints >= 8 -> TrackSignal.GOOD
                    s.totalLocationPoints >= 3 -> TrackSignal.FAIR
                    else -> TrackSignal.POOR
                }
                _uiState.update {
                    it.copy(
                        speedKmh = s.speed * 3.6,
                        avgSpeedKmh = s.avgSpeed * 3.6,
                        maxSpeedKmh = s.maxSpeed * 3.6,
                        totalPoints = s.totalLocationPoints,
                        unsyncedPoints = s.unsyncedLocationPoints,
                        trackingActivity = s.trackingActivity.ifBlank { "Stationary" },
                        pauseReason = s.pauseReason,
                        signal = signal
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Synthesize the gauge bearing from the live route. Emulators report no magnetometer,
     * so we use the last fix's pipeline-computed bearing, falling back to the heading
     * between the last two points.
     */
    private fun observeBearing(routeId: String) {
        bearingObserveJob?.cancel()
        bearingObserveJob = locationRepo.locationsForToken(routeId)
            .onEach { points ->
                if (points.isEmpty()) return@onEach
                val last = points.last()
                val bearing = when {
                    last.bearing != 0f -> last.bearing
                    points.size >= 2 -> headingBetween(points[points.size - 2], last)
                    else -> 0f
                }
                val locationLabel = "%.4f, %.4f".format(last.lat, last.lng)
                _uiState.update { it.copy(bearingDegrees = bearing, currentLocationLabel = locationLabel) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadConfig() {
        _uiState.update {
            it.copy(config = configManager.getTrackMilesConfig(), centers = geoCheckInLocations)
        }
    }

    // ── Start-flow & sheet orchestration (MVI intents) ──────────────────────────

    fun openJourneyGuide() = _uiState.update { it.copy(activeSheet = TrackSheet.JOURNEY_GUIDE) }
    fun dismissSheet() = _uiState.update { it.copy(activeSheet = TrackSheet.NONE) }

    fun openVehiclePicker() = _uiState.update { it.copy(activeSheet = TrackSheet.VEHICLE_PICKER) }
    fun setVehicleQuery(q: String) = _uiState.update { it.copy(vehicleQuery = q) }

    /** Pick a vehicle by key from the [vehicles] list and return to the journey guide. */
    fun pickVehicle(key: String) {
        val vehicle = _uiState.value.vehicles.firstOrNull { it.vehicleKey == key } ?: return
        _uiState.update {
            it.copy(selectedVehicle = vehicle, activeSheet = TrackSheet.JOURNEY_GUIDE, vehicleQuery = "")
        }
    }

    /** Simulate an odometer capture (the demo has no real OCR in this flow). */
    fun captureStartOdometer() {
        // Deterministic mock reading so the checklist completes without a camera round-trip.
        val reading = 45_000 + (_uiState.value.vehicles.size * 57)
        _uiState.update { it.copy(startOdometer = reading) }
    }

    fun toggleDraft(enabled: Boolean) = _uiState.update { it.copy(draftEnabled = enabled) }

    /** "Start Tracking" pressed in the guide: show consent if configured, else start now. */
    fun requestStartTracking() {
        val disclaimer = configManager.getJourneyDisclaimer()
        if (!disclaimer.isNullOrBlank()) {
            _uiState.update { it.copy(activeSheet = TrackSheet.CONSENT) }
        } else {
            beginTracking()
        }
    }

    fun acceptConsentAndStart() = beginTracking()

    private fun beginTracking() {
        _uiState.update { it.copy(activeSheet = TrackSheet.NONE) }
        startTracking()
    }

    // Pause / resume sheets
    fun openPauseSheet() = _uiState.update { it.copy(activeSheet = TrackSheet.PAUSE) }
    fun setPauseReason(reason: String?) = _uiState.update { it.copy(pauseSelectedReason = reason) }
    fun setPauseCustomReason(text: String) = _uiState.update { it.copy(pauseCustomReason = text) }
    fun confirmPause(reason: String) {
        _uiState.update { it.copy(activeSheet = TrackSheet.NONE) }
        pauseTracking(reason)
    }

    fun openResumeSheet() = _uiState.update { it.copy(activeSheet = TrackSheet.RESUME) }
    fun setResumeNotes(notes: String) = _uiState.update { it.copy(resumeNotes = notes) }
    fun confirmResume() {
        _uiState.update { it.copy(activeSheet = TrackSheet.NONE, resumeNotes = "") }
        resumeTracking()
    }

    // Vendor / center picker
    fun openVendorPicker() = _uiState.update { it.copy(activeSheet = TrackSheet.VENDOR_PICKER) }
    fun setVendorQuery(q: String) = _uiState.update { it.copy(vendorQuery = q) }
    fun pickVendor(id: String) {
        // Selecting a center is acknowledged; the demo records it implicitly via check-in.
        _uiState.update { it.copy(activeSheet = TrackSheet.NONE, vendorQuery = "") }
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
                    distanceKm = active.distance / 1000.0,
                    startTime = active.startTime
                )
            }
            observeLive(active.routeId)
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
            _uiState.update {
                it.copy(phase = TrackMilesPhase.TRACKING, currentRouteId = routeId, distanceKm = 0.0, startTime = now)
            }
            // Kick off the advanced foreground tracking service and observe its live writes.
            trackingController.start(routeId)
            observeLive(routeId)
            observeBearing(routeId)
        }
    }

    /** Toggle the hero gauge between the compass face and the activity timeline. */
    fun toggleGaugeMode() {
        _uiState.update {
            it.copy(
                gaugeMode = if (it.gaugeMode == HeroGaugeMode.COMPASS) {
                    HeroGaugeMode.ACTIVITY
                } else {
                    HeroGaugeMode.COMPASS
                }
            )
        }
    }

    /** Stream the service's live writes to `saved_tracks` into the UI in real time. */
    private fun observeLive(routeId: String) {
        liveObserveJob?.cancel()
        liveObserveJob = trackRepo.observeByRouteId(routeId)
            .onEach { track ->
                if (track == null) return@onEach
                val pricing = _uiState.value.selectedVehicle?.vehiclePricing ?: track.vehiclePricing
                val km = track.distance / 1000.0
                _uiState.update {
                    it.copy(
                        distanceKm = km,
                        durationMs = track.duration,
                        reimbursableAmount = km * pricing
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun pauseTracking(reason: String? = null) {
        _uiState.value.currentRouteId?.let { trackingController.pause(it) }
        _uiState.update { it.copy(phase = TrackMilesPhase.PAUSED, pauseReason = reason) }
    }

    fun resumeTracking() {
        _uiState.value.currentRouteId?.let { trackingController.resume(it) }
        _uiState.update { it.copy(phase = TrackMilesPhase.TRACKING) }
    }

    fun stopTracking() {
        _uiState.value.currentRouteId?.let { trackingController.stop(it) }
        liveObserveJob?.cancel()
        _uiState.update { it.copy(phase = TrackMilesPhase.STOPPED, endTime = System.currentTimeMillis()) }
    }

    fun updateDistance(km: Double) {
        val pricing = _uiState.value.selectedVehicle?.vehiclePricing ?: 0.0
        _uiState.update { it.copy(distanceKm = km, reimbursableAmount = km * pricing) }
    }

    fun discardTracking() {
        _uiState.value.currentRouteId?.let { trackingController.stop(it) }
        liveObserveJob?.cancel()
        bearingObserveJob?.cancel()
        _uiState.update { TrackMilesUiState(config = it.config, vehicles = it.vehicles, selectedVehicle = it.selectedVehicle) }
    }

    override fun onCleared() {
        liveObserveJob?.cancel()
        sessionObserveJob?.cancel()
        bearingObserveJob?.cancel()
        super.onCleared()
    }

    private companion object {
        /** Great-circle initial bearing (degrees, 0–360) between two fixes. */
        fun headingBetween(
            a: com.miletracker.core.data.model.db.LocationData,
            b: com.miletracker.core.data.model.db.LocationData
        ): Float {
            val lat1 = a.lat * kotlin.math.PI / 180.0
            val lat2 = b.lat * kotlin.math.PI / 180.0
            val dLon = (b.lng - a.lng) * kotlin.math.PI / 180.0
            val y = kotlin.math.sin(dLon) * kotlin.math.cos(lat2)
            val x = kotlin.math.cos(lat1) * kotlin.math.sin(lat2) -
                kotlin.math.sin(lat1) * kotlin.math.cos(lat2) * kotlin.math.cos(dLon)
            val deg = kotlin.math.atan2(y, x) * 180.0 / kotlin.math.PI
            return ((deg + 360.0) % 360.0).toFloat()
        }
    }
}
