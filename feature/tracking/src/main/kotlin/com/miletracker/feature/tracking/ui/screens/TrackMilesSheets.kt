package com.miletracker.feature.tracking.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miletracker.feature.tracking.ui.screens.stubs.TrackMilesAction
import com.miletracker.feature.tracking.viewmodel.TrackMilesViewModel

// ─── UI component note ───────────────────────────────────────────────────────
// A few richer bottom-sheet components (guiding sheet, vehicle picker, vendor
// search, plugin-config local) are not yet available in MileTrackerDemo.
// Each sheet below is stubbed with a visible placeholder; replace the stubs with
// real implementations as the shared design system grows.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Host composable for all bottom sheets in TrackMilesScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMilesSheetsHost(
    screenState: TrackMilesScreenState,
    viewModel: TrackMilesViewModel,
    selectedVehicleKey: String?,
    onAction: (TrackMilesAction) -> Unit
) {
    val context = LocalContext.current

    // ── Actions Sheet ──────────────────────────────────────────────────────────
    if (screenState.showActionsSheet) {
        val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { screenState.showActionsSheet = false },
            sheetState = actionsSheetState
        ) {
            // STUB: Replace Box below with:
            //   CompositionLocalProvider(LocalTrackMilesPluginConfig provides pluginConfig) {
            //       SheetKoffeeHost {
            //           Box(modifier = Modifier.navigationBarsPadding()) {
            //               TrackingGuidingSheet(
            //                   viewModel = viewModel,
            //                   onDismiss = { screenState.showActionsSheet = false },
            //                   showQuickActions = false
            //               )
            //           }
            //       }
            //   }
            Box(modifier = Modifier.navigationBarsPadding().padding(24.dp)) {
                Text("TrackingGuidingSheet — stub (composeUIKit not available in MileTrackerDemo)")
            }
        }
    }

    // ── Vehicle Picker Sheet ───────────────────────────────────────────────────
    if (screenState.showVehicleSheet) {
        val vehicleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { screenState.showVehicleSheet = false },
            sheetState = vehicleSheetState
        ) {
            // STUB: Replace Box below with:
            //   SheetKoffeeHost {
            //       Box(modifier = Modifier.navigationBarsPadding()) {
            //           VehiclePickerSheet(
            //               vehicleTypes = pickerVehicles,
            //               selectedVehicle = selectedVehicleKey ?: "",
            //               onSelect = { vehicle ->
            //                   onAction(SelectVehicle(vehicle))
            //                   screenState.showVehicleSheet = false
            //               }
            //           )
            //       }
            //   }
            Box(modifier = Modifier.navigationBarsPadding().padding(24.dp)) {
                Text("VehiclePickerSheet — stub (composeUIKit not available in MileTrackerDemo)")
            }
        }
    }

    // ── Center Picker Sheet ────────────────────────────────────────────────────
    if (screenState.showCenterPickerSheet) {
        // STUB: Replace with VendorSearchBottomSheet once composeUIKit is available.
        // Original source used VendorSearchBottomSheet with:
        //   onVendorClick  → CommonUtilsKT.openInGoogleMaps(context, lat, lng, title)
        //   onVendorLongPress → screenState.selectedVendor = vendor; showVendorOptionsDialog = true
        //   isCenterList = true, apiType = ApiType.CONTACTS, allowMultipleCheckIn = ..., etc.
        Log.d("TrackMilesSheets", "showCenterPickerSheet=true — VendorSearchBottomSheet stub")
        Toast.makeText(context, "Center picker — not available in demo build", Toast.LENGTH_SHORT).show()
        screenState.showCenterPickerSheet = false
    }
}
