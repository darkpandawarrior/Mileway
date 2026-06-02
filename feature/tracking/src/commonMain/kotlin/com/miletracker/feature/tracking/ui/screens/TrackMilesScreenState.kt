@file:Suppress("ktlint:standard:max-line-length", "ktlint:standard:property-naming", "ktlint:standard:comment-wrapping")

package com.miletracker.feature.tracking.ui.screens

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Lightweight domain types referenced by the tracking screen state.

/** Emergency/outlet contact shown on the tracking surface. */
data class ContactV2(
    val title: String? = null,
    val coords: ContactCoords? = null,
)

/** Coordinates attached to a contact. */
data class ContactCoords(val lat: Double? = null, val lng: Double? = null)

/** Permission categories the tracking screen can prompt for. */
enum class PermissionType {
    LocationForeground,
    LocationBackground,
    Notifications,
    Overlay,
    BatteryOptimization,
    PowerSaverDisabled,
    AutoStart,
    Camera,
    CallPhone,
    BluetoothConnect,
    ExactAlarm,
    RecordAudio,
    GpsEnabled,
}

/**
 * State holder for TrackMilesScreen UI state.
 * Consolidates all the transient UI flags and dialog states.
 */
@Stable
class TrackMilesScreenState {
    // Sheet states
    var showActionsSheet by mutableStateOf(false)
    var showVehicleSheet by mutableStateOf(false)
    var showCenterPickerSheet by mutableStateOf(false)

    // Dialog states
    var showPermissionDialog by mutableStateOf(false)
    var showDiscardConfirmation by mutableStateOf(false)
    var showVendorOptionsDialog by mutableStateOf(false)
    var showUpdateLocationConfirmDialog by mutableStateOf(false)
    var showPauseReason by mutableStateOf(false)
    var showResumeConfirmation by mutableStateOf(false)
    var showNetworkDetailDialog by mutableStateOf(false)
    var showTechnicalDetails by mutableStateOf(false)
    var showActivityDetail by mutableStateOf(false)

    // Image viewer states
    var showViewer by mutableStateOf(false)
    var viewerUrl by mutableStateOf("")

    // Current location chip

    /** Controls whether the "open in maps" bottom sheet for the current location is visible. */
    var showCurrentLocationSheet by mutableStateOf(false)

    // Form states
    var pauseReasonText by mutableStateOf("")

    // Vendor selection
    var selectedVendor by mutableStateOf<ContactV2?>(null)

    // Permission handling
    var pendingPermissions by mutableStateOf<List<PermissionType>>(emptyList())
    var pendingAction by mutableStateOf<(() -> Unit)?>(null)

    /**
     * Close all sheets and dialogs
     */
    fun closeAllSheets() {
        showActionsSheet = false
        showVehicleSheet = false
        showCenterPickerSheet = false
        showPermissionDialog = false
        showDiscardConfirmation = false
        showVendorOptionsDialog = false
        showUpdateLocationConfirmDialog = false
        showPauseReason = false
        showResumeConfirmation = false
        showNetworkDetailDialog = false
        showViewer = false
        showActivityDetail = false
        showCurrentLocationSheet = false
    }

    /**
     * Reset permission-related state
     */
    fun clearPendingPermissions() {
        pendingPermissions = emptyList()
        pendingAction = null
    }

    /**
     * Reset vendor selection state
     */
    fun clearVendorSelection() {
        selectedVendor = null
        showVendorOptionsDialog = false
        showUpdateLocationConfirmDialog = false
    }
}
