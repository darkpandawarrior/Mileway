package com.miletracker.feature.tracking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.util.DateUtils
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.tracking.ui.components.AdditionalDetailsForm
import com.miletracker.feature.tracking.ui.components.AttachmentsSection
import com.miletracker.feature.tracking.ui.components.FormField
import com.miletracker.feature.tracking.ui.components.FormFieldType
import com.miletracker.feature.tracking.ui.components.JourneySummaryCard
import com.miletracker.feature.tracking.ui.components.LocationDetailsCard
import com.miletracker.feature.tracking.ui.components.MileageDraftBottomBar
import com.miletracker.feature.tracking.ui.components.OdometerReadingsCard
import com.miletracker.feature.tracking.ui.components.OfficeEntitySelectRow
import com.miletracker.feature.tracking.ui.components.PendingDataSyncCard
import com.miletracker.feature.tracking.ui.components.SubmissionChecklistHeader
import com.miletracker.feature.tracking.ui.components.SubmissionTabChips
import com.miletracker.feature.tracking.ui.navigation.SubmissionResult
import com.miletracker.feature.tracking.ui.sheets.EntityPickerSheet
import com.miletracker.feature.tracking.ui.sheets.OfficePickerSheet
import com.miletracker.feature.tracking.ui.sheets.PolicyViolationSheet
import com.miletracker.feature.tracking.ui.sheets.SmartDistanceSheet
import com.miletracker.feature.tracking.ui.sheets.SubmitConfirmSheet
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
import com.miletracker.feature.tracking.viewmodel.SubmissionFieldType
import com.miletracker.feature.tracking.viewmodel.SubmissionSheet
import com.miletracker.feature.tracking.viewmodel.SubmissionUiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSubmissionScreen(
    routeId: String,
    distanceKm: Double,
    vehicleKey: String,
    startTime: Long,
    endTime: Long,
    onSuccess: (SubmissionResult) -> Unit,
    onBack: () -> Unit,
    viewModel: MileageSubmissionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val form by viewModel.form.collectAsState()

    // Local odometer readings (capture simulates an OCR reading).
    var startOdo by remember { mutableStateOf<Int?>(null) }
    var endOdo by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf("Journey") }
    var smartDistanceVerified by remember { mutableStateOf(false) }
    var smartDistanceExplanation by remember { mutableStateOf("") }

    val durationMs = (endTime - startTime).coerceAtLeast(0L)
    val odometerDistanceKm = if (startOdo != null && endOdo != null) (endOdo!! - startOdo!!).toDouble() else null

    // Trigger SmartDistanceSheet automatically when odometer discrepancy exceeds 15%.
    LaunchedEffect(odometerDistanceKm) {
        val odometer = odometerDistanceKm ?: return@LaunchedEffect
        val discrepancy = if (distanceKm > 0.0) kotlin.math.abs(odometer - distanceKm) / distanceKm else 0.0
        if (discrepancy > 0.15) viewModel.openSmartDistanceSheet(distanceKm, odometer)
    }

    // On a finalized submission, hand the full result to the success screen.
    LaunchedEffect(state) {
        val s = state
        if (s is SubmissionUiState.Success) {
            val r = s.response
            onSuccess(
                SubmissionResult(
                    distanceKm = distanceKm,
                    reimbursableAmount = r.reimbursableAmount ?: 0.0,
                    vehicleName = vehicleKey,
                    startTime = startTime,
                    endTime = endTime,
                    transactionId = r.transId ?: r.transaction?.id,
                    submissionStatus = r.submissionStatus.name,
                    violationCount = r.violations.size,
                    violationMessage = r.violations.firstOrNull()?.message,
                    voucherNumber = r.issuedVoucher?.number,
                    voucherAmount = r.issuedVoucher?.amount ?: 0.0,
                )
            )
        }
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Submit Track Miles",
                subtitle = "Fill out the details given below",
                depth = NavigationDepth.LEVEL_2,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            MileageDraftBottomBar(
                saveAsDraft = form.saveAsDraft,
                onToggleDraft = viewModel::toggleDraft,
                submitEnabled = form.canSubmit && state !is SubmissionUiState.Submitting,
                onDiscard = onBack,
                onSubmit = viewModel::openSubmitConfirm,
                infoText = "Review journey details and required fields before submitting.",
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.l),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l),
            ) {
                SubmissionChecklistHeader(
                    remaining = form.remainingRequirements.size,
                    requirements = form.remainingRequirements,
                )

                SubmissionTabChips(
                    tabs = listOf("Journey", "Vehicle", "Odometer", "Forms"),
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                )

                PendingDataSyncCard(pendingPoints = 1, totalPoints = 7)

                JourneySummaryCard(
                    distanceText = "%.2f km".format(distanceKm),
                    durationText = formatDuration(durationMs),
                    maxSpeedText = "—",
                    avgSpeedText = "—",
                )

                LocationDetailsCard(
                    startAddress = "Trip start point",
                    endAddress = "Trip end point",
                    startTime = DateUtils.epochToTime12h(startTime),
                    endTime = DateUtils.epochToTime12h(endTime),
                )

                OdometerReadingsCard(
                    startReading = startOdo,
                    endReading = endOdo,
                    isManualStart = false,
                    isManualEnd = false,
                    odometerDistanceKm = odometerDistanceKm,
                    onCaptureStart = { startOdo = 45_000 },
                    onCaptureEnd = { endOdo = 45_000 + distanceKm.toInt().coerceAtLeast(1) },
                )

                AdditionalDetailsForm(
                    fields = form.fields.map { f ->
                        FormField(
                            id = f.id,
                            label = f.label,
                            type = if (f.type == SubmissionFieldType.DROPDOWN) FormFieldType.DROPDOWN else FormFieldType.TEXT,
                            value = form.values[f.id].orEmpty(),
                            required = f.required,
                            options = f.options,
                            errorText = if (f.required && form.values[f.id].isNullOrBlank()) "${f.label} is required" else null,
                        )
                    },
                    onValueChange = viewModel::setFormValue,
                )

                if (form.officeRequired) {
                    OfficeEntitySelectRow(
                        label = "Office",
                        value = form.selectedOffice?.let { "${it.code} - ${it.name}" },
                        requiredHint = "Required field",
                        onClick = viewModel::openOfficePicker,
                    )
                    OfficeEntitySelectRow(
                        label = "Entity",
                        value = form.selectedEntity?.name,
                        requiredHint = "Required field",
                        onClick = viewModel::openEntityPicker,
                    )
                }

                AttachmentsSection(
                    attachments = emptyList(),
                    onAdd = {},
                    onRemove = {},
                )
            }

            if (state is SubmissionUiState.Submitting) {
                Box(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // ── Submission sheets (single source of truth: form.sheet) ──────────────────
    when (form.sheet) {
        SubmissionSheet.SUBMIT_CONFIRM -> SubmitConfirmSheet(
            onConfirm = { viewModel.submit(routeId, distanceKm, vehicleKey, startTime, endTime) },
            onCancel = viewModel::dismissSheet,
            onDismiss = viewModel::dismissSheet,
        )

        SubmissionSheet.POLICY_VIOLATION -> PolicyViolationSheet(
            violations = form.violations,
            askAuthoritiesSelected = form.askAuthorities,
            note = form.violationNote,
            onToggleAskAuthorities = { viewModel.setAskAuthorities(!form.askAuthorities) },
            onNoteChange = viewModel::setViolationNote,
            onSubmit = viewModel::resolvePolicyAndFinalize,
            onDismiss = viewModel::dismissSheet,
        )

        SubmissionSheet.OFFICE_PICKER -> OfficePickerSheet(
            offices = form.offices,
            query = form.officeQuery,
            onQueryChange = viewModel::setOfficeQuery,
            onSelect = viewModel::selectOffice,
            onDismiss = viewModel::dismissSheet,
        )

        SubmissionSheet.ENTITY_PICKER -> EntityPickerSheet(
            entities = form.entities,
            query = form.entityQuery,
            onQueryChange = viewModel::setEntityQuery,
            onSelect = viewModel::selectEntity,
            onDismiss = viewModel::dismissSheet,
        )

        SubmissionSheet.SMART_DISTANCE -> SmartDistanceSheet(
            trackedKm = form.smartDistanceTrackedKm,
            odometerKm = form.smartDistanceOdometerKm,
            verified = smartDistanceVerified,
            explanation = smartDistanceExplanation,
            onVerifiedChange = { smartDistanceVerified = it },
            onExplanationChange = { smartDistanceExplanation = it },
            onStop = viewModel::dismissSheet,
            onContinue = viewModel::dismissSheet,
            onDismiss = viewModel::dismissSheet,
        )

        SubmissionSheet.NONE -> Unit
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
