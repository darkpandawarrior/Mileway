package com.miletracker.feature.tracking.ui.screens.stubs

/**
 * Stub action hierarchy mirroring the enterprise mileageTracker action model.
 *
 * The full enterprise ViewModel is not available in MileTrackerDemo. These stubs
 * preserve the shape of the action sealed class so that all ported screen files
 * compile. Replace with the real implementations once the full ViewModel is ported.
 */
sealed interface TrackMilesAction

// ── Tracking lifecycle ─────────────────────────────────────────────────────────
object StartTracking : TrackMilesAction
object StopTracking : TrackMilesAction
object PauseTracking : TrackMilesAction
object ResumeTracking : TrackMilesAction
object DiscardJourney : TrackMilesAction
object HideConfirmDialog : TrackMilesAction
object DismissError : TrackMilesAction

// ── Navigation ────────────────────────────────────────────────────────────────
object OpenLogs : TrackMilesAction
object OpenMap : TrackMilesAction
object OpenSettings : TrackMilesAction
object OpenTrackSubmission : TrackMilesAction
object OpenManualCheckIn : TrackMilesAction
object OpenGeoCheckIn : TrackMilesAction
object OpenSavedTracks : TrackMilesAction
object OpenLoadingScreen : TrackMilesAction
object OpenDataStoreScreen : TrackMilesAction
object CheckInHistory : TrackMilesAction
object OutletsList : TrackMilesAction

data class OpenTrackDetail(val token: String) : TrackMilesAction
data class OpenTrackSubmissionWithContext(
    val routeId: String,
    val isDraft: Boolean = false,
    val isDiscarded: Boolean = false
) : TrackMilesAction

// ── Vehicle selection ─────────────────────────────────────────────────────────
data class SelectVehicle(val vehicleType: Any?) : TrackMilesAction
