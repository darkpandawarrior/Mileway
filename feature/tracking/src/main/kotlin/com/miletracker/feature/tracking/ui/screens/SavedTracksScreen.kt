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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.core.data.util.DateUtils
import com.miletracker.core.ui.components.EmptyState
import com.miletracker.core.ui.components.LoadingScreen
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.tracking.viewmodel.SavedTracksUiState
import com.miletracker.feature.tracking.viewmodel.SavedTracksViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTracksScreen(
    onTrackClick: (String) -> Unit,
    onStartNew: () -> Unit,
    viewModel: SavedTracksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartNew,
                icon = { Icon(Icons.Default.PlayArrow, null) },
                text = { Text("Start Journey") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            TrackMilesHeader(tracks = uiState.tracks)
            SavedTracksContent(
                uiState = uiState,
                bottomPadding = padding.calculateBottomPadding(),
                onTrackClick = onTrackClick
            )
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
                text = "Track Miles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Your recorded journeys",
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

@Composable
private fun SavedTracksContent(
    uiState: SavedTracksUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTrackClick: (String) -> Unit
) {
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.tracks.isEmpty() -> EmptyState(
            title = "No saved journeys",
            subtitle = "Tap 'Start Journey' to record your first trip"
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(
                top = DesignTokens.Spacing.l,
                bottom = bottomPadding + 96.dp,
                start = DesignTokens.Spacing.l,
                end = DesignTokens.Spacing.l
            ),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
        ) {
            items(uiState.tracks) { track ->
                JourneyCard(track = track, onClick = { onTrackClick(track.token) })
            }
        }
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
