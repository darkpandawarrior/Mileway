package com.miletracker.feature.tracking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.core.data.util.DateUtils
import com.miletracker.core.ui.components.ConfettiBurst
import com.miletracker.core.ui.components.EmptyState
import com.miletracker.core.ui.components.LoadingScreen
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.tracking.ui.components.CreateVoucherButton
import com.miletracker.feature.tracking.ui.components.NoJourneysThisWeekState
import com.miletracker.feature.tracking.ui.components.NoSubmissionsState
import com.miletracker.feature.tracking.ui.components.SavedTracksChipRow
import com.miletracker.feature.tracking.ui.components.SavedTracksFilterChip
import com.miletracker.feature.tracking.ui.components.SavedTracksSearchField
import com.miletracker.feature.tracking.ui.components.SavedTracksSegment
import com.miletracker.feature.tracking.ui.components.SavedTracksSegmentedToggle
import com.miletracker.feature.tracking.ui.components.SubmissionCard
import com.miletracker.feature.tracking.ui.components.SubmissionCardData
import com.miletracker.feature.tracking.ui.components.SubmissionDateHeader
import com.miletracker.feature.tracking.ui.components.SubmissionSelectionRow
import com.miletracker.feature.tracking.viewmodel.JourneyFilter
import com.miletracker.feature.tracking.viewmodel.SavedTracksTab
import com.miletracker.feature.tracking.viewmodel.SavedTracksUiState
import com.miletracker.feature.tracking.viewmodel.SavedTracksViewModel
import com.miletracker.feature.tracking.viewmodel.SubmissionFilter
import com.miletracker.feature.tracking.viewmodel.SubmissionItem
import com.miletracker.feature.tracking.viewmodel.SubmissionSource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top-level "Saved Tracks" tab — a Journeys/Submissions surface sitting above the bubble bottom bar.
 *
 * The public signature is intentionally stable: the integrator still passes [onTrackClick] and
 * [onStartNew], and the screen still owns its own [SavedTracksViewModel] via Koin.
 *
 * Layout:
 *  - Gradient ROOT header with aggregate stats (the "deeper = calmer" anchor).
 *  - A Journeys/Submissions segmented toggle, a rounded search field, and chip filters.
 *  - Journeys tab: the filtered journey card list (with a "No journeys this week" empty state).
 *  - Submissions tab: date-grouped submission cards with a long-press selection mode and a
 *    full-width "Create Voucher" CTA. Creating a voucher is a pure-demo acknowledgement (confetti).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTracksScreen(
    onTrackClick: (String) -> Unit,
    onStartNew: () -> Unit,
    viewModel: SavedTracksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pure-demo voucher acknowledgement: snackbar + confetti, then consume the one-shot flag.
    LaunchedEffect(uiState.voucherCreatedAck) {
        if (uiState.voucherCreatedAck) {
            snackbarHostState.showSnackbar("Voucher created")
            viewModel.onVoucherAckConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onStartNew,
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    text = { Text("Start Journey") },
                    // Lift above the floating bubble bar.
                    modifier = Modifier.padding(bottom = 88.dp)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize()) {
                TrackMilesHeader(tracks = uiState.tracks)
                SavedTracksBody(
                    uiState = uiState,
                    bottomPadding = padding.calculateBottomPadding(),
                    onTrackClick = onTrackClick,
                    viewModel = viewModel
                )
            }
        }

        // Celebration overlay floats above everything while the flag is set.
        if (uiState.voucherCreatedAck) {
            ConfettiBurst(modifier = Modifier.fillMaxSize())
        }
    }
}

/** Gradient ROOT header with title + summary stats — the screen's anchor ("deeper = calmer"). */
@Composable
private fun TrackMilesHeader(tracks: List<TrackDisplayData>) {
    val totalTrips = tracks.size
    val totalKm = tracks.sumOf { it.distanceKm }
    val totalReimbursable = tracks.filter { it.isSubmitted }.sumOf { it.reimbursableAmount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignTokens.topBarGradientBrush())
            .statusBarsPadding()
            .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.l)
    ) {
        Column {
            Text(
                text = "Saved Tracks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Track and manage your journeys",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(DesignTokens.Spacing.l))
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                HeaderStat(value = totalTrips.toString(), label = "Trips", modifier = Modifier.weight(1f))
                HeaderStat(value = "%.0f km".format(totalKm), label = "Distance", modifier = Modifier.weight(1f))
                HeaderStat(value = "₹%.0f".format(totalReimbursable), label = "Reimbursed", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(DesignTokens.Shape.roundedSm)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = DesignTokens.Spacing.m, horizontal = DesignTokens.Spacing.s)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}

/**
 * Below-header content: the toggle/search/chips controls and the per-tab list. Everything below
 * the gradient header lives in a single [LazyColumn] so the controls scroll with the content.
 */
@Composable
private fun SavedTracksBody(
    uiState: SavedTracksUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTrackClick: (String) -> Unit,
    viewModel: SavedTracksViewModel
) {
    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(
            top = DesignTokens.Spacing.l,
            bottom = bottomPadding + 140.dp,
            start = DesignTokens.Spacing.l,
            end = DesignTokens.Spacing.l
        ),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
    ) {
        // Controls shared by both tabs.
        item {
            SavedTracksSegmentedToggle(
                selected = if (uiState.tab == SavedTracksTab.JOURNEYS) SavedTracksSegment.JOURNEYS else SavedTracksSegment.SUBMISSIONS,
                journeyCount = uiState.journeyCount,
                submissionCount = uiState.submissionCount,
                onSelect = { segment ->
                    viewModel.onTabSelected(
                        if (segment == SavedTracksSegment.JOURNEYS) SavedTracksTab.JOURNEYS else SavedTracksTab.SUBMISSIONS
                    )
                }
            )
        }

        when (uiState.tab) {
            SavedTracksTab.JOURNEYS -> journeysSection(uiState, onTrackClick, viewModel)
            SavedTracksTab.SUBMISSIONS -> submissionsSection(uiState, viewModel)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Journeys tab
// ─────────────────────────────────────────────────────────────────────────────

private fun androidx.compose.foundation.lazy.LazyListScope.journeysSection(
    uiState: SavedTracksUiState,
    onTrackClick: (String) -> Unit,
    viewModel: SavedTracksViewModel
) {
    item {
        SavedTracksSearchField(
            query = uiState.journeySearch,
            placeholder = "Search journeys…",
            onQueryChange = viewModel::onJourneySearchChanged,
            onFilterClick = {}
        )
    }
    item {
        SavedTracksChipRow {
            SavedTracksFilterChip(
                label = "This Week",
                selected = uiState.journeyFilter == JourneyFilter.THIS_WEEK,
                onClick = { viewModel.onJourneyFilterSelected(JourneyFilter.THIS_WEEK) }
            )
            SavedTracksFilterChip(
                label = "Kept",
                selected = uiState.journeyFilter == JourneyFilter.KEPT,
                onClick = { viewModel.onJourneyFilterSelected(JourneyFilter.KEPT) }
            )
            SavedTracksFilterChip(
                label = "All",
                selected = uiState.journeyFilter == JourneyFilter.ALL,
                onClick = { viewModel.onJourneyFilterSelected(JourneyFilter.ALL) }
            )
        }
    }

    val journeys = filterJourneys(uiState)
    when {
        journeys.isEmpty() && uiState.journeyFilter == JourneyFilter.THIS_WEEK && uiState.journeySearch.isBlank() -> {
            item {
                NoJourneysThisWeekState(onViewAll = { viewModel.onJourneyFilterSelected(JourneyFilter.ALL) })
            }
        }
        journeys.isEmpty() -> {
            item {
                EmptyState(
                    title = if (uiState.journeySearch.isNotBlank()) "No matching journeys" else "No saved journeys",
                    subtitle = if (uiState.journeySearch.isNotBlank()) "Try a different search term" else "Tap 'Start Journey' to record your first trip"
                )
            }
        }
        else -> items(journeys, key = { it.token }) { track ->
            JourneyCard(track = track, onClick = { onTrackClick(track.token) })
        }
    }
}

/**
 * Applies the active Journeys-tab search and chip filter to the track list. "Kept" reuses the
 * submitted flag as the demo's "retained" signal; "This Week" keeps tracks from the last 7 days.
 */
private fun filterJourneys(uiState: SavedTracksUiState): List<TrackDisplayData> {
    val now = System.currentTimeMillis()
    val weekAgo = now - 7L * 24 * 60 * 60 * 1000
    return uiState.tracks
        .filter { track ->
            when (uiState.journeyFilter) {
                JourneyFilter.THIS_WEEK -> track.startTime >= weekAgo
                JourneyFilter.KEPT -> track.isSubmitted
                JourneyFilter.ALL -> true
            }
        }
        .filter { track ->
            uiState.journeySearch.isBlank() ||
                (track.name?.contains(uiState.journeySearch, ignoreCase = true) == true) ||
                track.token.contains(uiState.journeySearch, ignoreCase = true)
        }
}

@Composable
private fun JourneyCard(track: TrackDisplayData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(DesignTokens.IconSize.actionTile)
                    )
                }
                Spacer(Modifier.width(DesignTokens.Spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.name ?: "Journey",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (track.startTime > 0) {
                        Text(
                            text = DateUtils.epochToDisplayDate(track.startTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(isSubmitted = track.isSubmitted)
            }

            Spacer(Modifier.height(DesignTokens.Spacing.m))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
            ) {
                Metric(label = "Distance", value = track.getFormattedDistance(), modifier = Modifier.weight(1f))
                Metric(label = "Duration", value = track.getFormattedDuration(), modifier = Modifier.weight(1f))
                Metric(
                    label = "Amount",
                    value = if (track.reimbursableAmount > 0) "₹%.0f".format(track.reimbursableAmount) else "—",
                    valueColor = if (track.reimbursableAmount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun StatusChip(isSubmitted: Boolean) {
    val (label, color) = if (isSubmitted) {
        "Submitted" to DesignTokens.StatusColors.success
    } else {
        "Saved" to DesignTokens.StatusColors.info
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(DesignTokens.Shape.chip)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = DesignTokens.Spacing.s, vertical = 4.dp)
    ) {
        if (isSubmitted) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(DesignTokens.IconSize.inline))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Submissions tab
// ─────────────────────────────────────────────────────────────────────────────

private fun androidx.compose.foundation.lazy.LazyListScope.submissionsSection(
    uiState: SavedTracksUiState,
    viewModel: SavedTracksViewModel
) {
    item {
        SavedTracksSearchField(
            query = uiState.submissionSearch,
            placeholder = "Search submissions…",
            onQueryChange = viewModel::onSubmissionSearchChanged,
            onFilterClick = {}
        )
    }
    // Primary submission filter chips.
    item {
        SavedTracksChipRow {
            SavedTracksFilterChip(
                label = "All (${uiState.submissionCount})",
                selected = uiState.submissionFilter == SubmissionFilter.ALL,
                onClick = { viewModel.onSubmissionFilterSelected(SubmissionFilter.ALL) }
            )
            SavedTracksFilterChip(
                label = "Unclaimed (${uiState.unclaimedCount})",
                selected = uiState.submissionFilter == SubmissionFilter.UNCLAIMED,
                onClick = { viewModel.onSubmissionFilterSelected(SubmissionFilter.UNCLAIMED) }
            )
            SavedTracksFilterChip(
                label = "Filed (${uiState.filedCount})",
                selected = uiState.submissionFilter == SubmissionFilter.FILED,
                onClick = { viewModel.onSubmissionFilterSelected(SubmissionFilter.FILED) }
            )
        }
    }
    // Secondary source chips.
    item {
        SavedTracksChipRow {
            SavedTracksFilterChip(
                label = "All",
                selected = uiState.submissionSource == SubmissionSource.ALL,
                onClick = { viewModel.onSubmissionSourceSelected(SubmissionSource.ALL) }
            )
            SavedTracksFilterChip(
                label = "New Tracker",
                selected = uiState.submissionSource == SubmissionSource.NEW_TRACKER,
                onClick = { viewModel.onSubmissionSourceSelected(SubmissionSource.NEW_TRACKER) }
            )
            SavedTracksFilterChip(
                label = "Other",
                selected = uiState.submissionSource == SubmissionSource.OTHER,
                onClick = { viewModel.onSubmissionSourceSelected(SubmissionSource.OTHER) }
            )
        }
    }

    // Selection-mode header + Create Voucher CTA.
    if (uiState.selectionMode && uiState.selectedSubmissionIds.isNotEmpty()) {
        item {
            SubmissionSelectionRow(
                selectedCount = uiState.selectedSubmissionIds.size,
                onClearSelection = viewModel::onClearSelection
            )
        }
        val voucherCount = uiState.selectedUnclaimedIds.size
        if (voucherCount > 0) {
            item {
                CreateVoucherButton(count = voucherCount, onClick = viewModel::onCreateVoucher)
            }
        }
    }

    val submissions = uiState.filteredSubmissions
    if (submissions.isEmpty()) {
        item {
            NoSubmissionsState(
                title = if (uiState.submissionSearch.isNotBlank()) "No matching submissions" else "No submissions yet",
                subtitle = if (uiState.submissionSearch.isNotBlank()) "Try a different search term" else "Submit mileage from your journeys to see them here"
            )
        }
    } else {
        // Date-grouped: a header per day, then the day's submission cards.
        val grouped = submissions.groupBy { DateUtils.epochToDisplayDate(it.expenseDateMillis) }
        grouped.forEach { (date, daySubmissions) ->
            item(key = "header_$date") {
                SubmissionDateHeader(label = date)
            }
            items(daySubmissions, key = { it.id }) { submission ->
                SubmissionCard(
                    data = submission.toCardData(),
                    isSelected = submission.id in uiState.selectedSubmissionIds,
                    selectionMode = uiState.selectionMode,
                    onClick = {
                        if (uiState.selectionMode) viewModel.onSubmissionTapped(submission.id)
                        else viewModel.onSubmissionSelectionToggled(submission.id)
                    },
                    onLongClick = { viewModel.onSubmissionLongPressed(submission.id) }
                )
            }
        }
    }
}

/** Maps the ViewModel's [SubmissionItem] onto the stateless card's render data. */
private fun SubmissionItem.toCardData() = SubmissionCardData(
    id = id,
    transId = transId,
    amount = amount,
    expenseDateMillis = expenseDateMillis,
    attachmentCount = attachmentCount,
    violationCount = violationCount,
    acknowledged = acknowledged,
    isNewTracker = isNewTracker,
    voucherCreated = voucherCreated
)
