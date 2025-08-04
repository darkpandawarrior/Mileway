package com.miletracker.feature.tracking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.EventAudience
import com.miletracker.core.data.model.db.EventType
import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.feature.tracking.checkin.CheckInValidator
import com.miletracker.feature.tracking.checkin.CheckInValidator.CheckInLocation
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Orchestrates both manual and geo check-in flows for the offline demo.
 *
 * All validation is done locally via [CheckInValidator] (Haversine distance vs a configured
 * radius). No network calls are made — this is a fully offline implementation.
 *
 * Persistence: a [LocationData] row is written with [LocationData.wasCheckInPoint] = true
 * and [LocationData.checkInType] set to "MANUAL" or "GEO". A [HardwareEvent] is also
 * logged to the hardware_events table for the audit trail.
 */
class CheckInViewModel(
    private val locationRepo: LocationRepository,
    private val hardwareEventRepo: HardwareEventRepository,
    private val currentTrackRepository: CurrentTrackRepository,
    /** Geofence locations to validate against for geo check-in. */
    private val geoCheckInLocations: List<CheckInLocation>,
    /** Default radius in metres used when a location has no per-location override. */
    private val defaultRadiusMeters: Double = 100.0
) : ViewModel() {

    companion object {
        private const val TAG = "CheckInViewModel"
    }

    data class UiState(
        val isSubmitting: Boolean = false,
        val checkInSuccess: Boolean = false,
        val successMessage: String = "",
        val error: String? = null,
        // Geo check-in radius-warning state
        val showRadiusWarning: Boolean = false,
        val radiusWarningMessage: String = "",
        val pendingValidationResult: CheckInValidator.ValidationResult? = null,
        // Manual check-in sheet
        val showManualCheckInSheet: Boolean = false,
        val manualReason: String = "",
        // Geo check-in sheet
        val showGeoCheckInSheet: Boolean = false,
        // Current route token for log records
        val currentToken: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Keep the current token up-to-date from the live DataStore flow
            currentTrackRepository.currentTrackFlow.collect { session ->
                _uiState.update { it.copy(currentToken = session.token) }
            }
        }
    }

    // ── Manual check-in ──────────────────────────────────────────────────────────

    fun openManualCheckIn() {
        _uiState.update { it.copy(showManualCheckInSheet = true, error = null) }
    }

    fun dismissManualCheckIn() {
        _uiState.update { it.copy(showManualCheckInSheet = false, manualReason = "") }
    }

    fun updateManualReason(text: String) {
        _uiState.update { it.copy(manualReason = text) }
    }

    /**
     * Records a manual check-in at the most-recently persisted location for the active trip.
     *
     * Persists a [LocationData] row with [LocationData.wasCheckInPoint] = true and
     * [LocationData.checkInType] = "MANUAL". Also logs a [HardwareEvent].
     */
    fun submitManualCheckIn() {
        val snapshot = _uiState.value
        if (snapshot.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val token = snapshot.currentToken.ifBlank {
                    currentTrackRepository.currentTrackFlow.first().token
                }
                if (token.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "No active tracking session found.") }
                    return@launch
                }

                // Grab the last persisted location for this trip
                val lastLocation: LocationData? = try {
                    locationRepo.locationsForToken(token).first().lastOrNull()
                } catch (e: Exception) {
                    null
                }

                val now = System.currentTimeMillis()
                val checkInRecord = if (lastLocation != null) {
                    lastLocation.copy(
                        id = 0,
                        date = now,
                        uploaded = false,
                        wasCheckInPoint = true,
                        checkInType = "MANUAL",
                        reason = snapshot.manualReason.take(200)
                    )
                } else {
                    // No previous location yet — create a minimal placeholder record
                    LocationData(
                        token = token,
                        lat = 0.0, lng = 0.0,
                        activity = "MANUAL_CHECK_IN",
                        speed = 0f,
                        batteryPercentage = 0.0,
                        date = now,
                        uploaded = false,
                        wasCheckInPoint = true,
                        checkInType = "MANUAL",
                        reason = snapshot.manualReason.take(200)
                    )
                }

                locationRepo.insert(checkInRecord)

                // Log a HardwareEvent for the audit trail
                val hwEvent = HardwareEvent(
                    token = token,
                    eventType = EventType.CHECK_IN,
                    event = "Manual check-in recorded",
                    lat = checkInRecord.lat.takeIf { it != 0.0 },
                    lng = checkInRecord.lng.takeIf { it != 0.0 },
                    time = now,
                    audience = EventAudience.USER,
                    metadata = if (snapshot.manualReason.isNotBlank()) "Reason: ${snapshot.manualReason}" else null
                )
                hardwareEventRepo.insert(hwEvent)

                Log.i(TAG, "Manual check-in saved for token=${token.take(8)}…")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showManualCheckInSheet = false,
                        manualReason = "",
                        checkInSuccess = true,
                        successMessage = "Manual check-in recorded."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Manual check-in failed", e)
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Check-in failed.") }
            }
        }
    }

    // ── Geo check-in ────────────────────────────────────────────────────────────

    fun openGeoCheckIn() {
        _uiState.update { it.copy(showGeoCheckInSheet = true, error = null) }
    }

    fun dismissGeoCheckIn() {
        _uiState.update { it.copy(showGeoCheckInSheet = false, showRadiusWarning = false, pendingValidationResult = null) }
    }

    /**
     * Validates [userLat]/[userLng] against the nearest mock check-in location.
     *
     * - Within radius  → records a GEO check-in immediately.
     * - Outside radius → shows the radius-warning sheet with the distance outside.
     */
    fun validateAndGeoCheckIn(userLat: Double, userLng: Double) {
        if (geoCheckInLocations.isEmpty()) {
            _uiState.update { it.copy(error = "No check-in locations configured.", showGeoCheckInSheet = false) }
            return
        }

        val result = CheckInValidator.validate(
            userLat = userLat,
            userLng = userLng,
            candidates = geoCheckInLocations,
            defaultRadiusMeters = defaultRadiusMeters
        )

        if (result.withinRadius) {
            persistGeoCheckIn(result)
        } else {
            val message = CheckInValidator.buildOutsideRadiusMessage(result)
            Log.d(TAG, "Geo check-in outside radius: ${result.distanceOutside.toInt()} m outside")
            _uiState.update {
                it.copy(
                    showGeoCheckInSheet = false,
                    showRadiusWarning = true,
                    radiusWarningMessage = message,
                    pendingValidationResult = result
                )
            }
        }
    }

    fun dismissRadiusWarning() {
        _uiState.update { it.copy(showRadiusWarning = false, pendingValidationResult = null) }
    }

    /**
     * Called from the radius-warning sheet when the user explicitly overrides and wants to
     * record the check-in anyway (same UX pattern as the enterprise updateAndCheckIn).
     */
    fun forceGeoCheckInDespiteRadius() {
        val pending = _uiState.value.pendingValidationResult ?: return
        _uiState.update { it.copy(showRadiusWarning = false, pendingValidationResult = null) }
        persistGeoCheckIn(pending, isOverride = true)
    }

    // ── Success acknowledgement ──────────────────────────────────────────────────

    fun acknowledgeSuccess() {
        _uiState.update { it.copy(checkInSuccess = false, successMessage = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private fun persistGeoCheckIn(result: CheckInValidator.ValidationResult, isOverride: Boolean = false) {
        val snapshot = _uiState.value
        if (snapshot.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val token = snapshot.currentToken.ifBlank {
                    currentTrackRepository.currentTrackFlow.first().token
                }
                if (token.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "No active tracking session found.") }
                    return@launch
                }

                val now = System.currentTimeMillis()
                val checkInType = if (isOverride) "GEO_OVERRIDE" else "GEO"
                val checkInRecord = LocationData(
                    token = token,
                    lat = result.userLat,
                    lng = result.userLng,
                    activity = "GEO_CHECK_IN",
                    speed = 0f,
                    batteryPercentage = 0.0,
                    date = now,
                    uploaded = false,
                    wasCheckInPoint = true,
                    checkInType = checkInType,
                    miscellaneous = result.nearestLocation.name,
                    reason = if (isOverride) "Override: ${result.distanceOutside.toInt()} m outside radius" else ""
                )

                locationRepo.insert(checkInRecord)

                val hwEvent = HardwareEvent(
                    token = token,
                    eventType = EventType.CHECK_IN,
                    event = if (isOverride) "Geo check-in recorded (override)" else "Geo check-in recorded",
                    lat = result.userLat,
                    lng = result.userLng,
                    time = now,
                    audience = EventAudience.USER,
                    metadata = buildString {
                        append("Location: ${result.nearestLocation.name}")
                        append(", Distance: ${result.distanceMeters.toInt()} m")
                        append(", Radius: ${result.effectiveRadius.toInt()} m")
                        if (isOverride) append(", Override: ${result.distanceOutside.toInt()} m outside")
                    }
                )
                hardwareEventRepo.insert(hwEvent)

                Log.i(TAG, "Geo check-in ($checkInType) saved for token=${token.take(8)}… at ${result.nearestLocation.name}")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showGeoCheckInSheet = false,
                        checkInSuccess = true,
                        successMessage = "Checked in at ${result.nearestLocation.name}."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geo check-in failed", e)
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Check-in failed.") }
            }
        }
    }
}
