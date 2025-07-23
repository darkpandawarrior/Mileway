package com.miletracker.feature.tracking.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.core.data.util.DateUtils
import com.miletracker.core.ui.components.LoadingScreen
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.tracking.ui.components.ExportOptionsDialog
import com.miletracker.feature.tracking.viewmodel.ExportViewModel
import com.miletracker.feature.tracking.viewmodel.TrackDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenHwEvents: () -> Unit,
    viewModel: TrackDetailViewModel = koinViewModel(),
    exportViewModel: ExportViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by exportViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(routeId) { viewModel.load(routeId) }

    // Fire share intent once ready
    LaunchedEffect(exportState.shareIntent) {
        exportState.shareIntent?.let { intent ->
            context.startActivity(intent)
            exportViewModel.clearShareIntent()
        }
    }

    // Show error in snackbar
    LaunchedEffect(exportState.error) {
        exportState.error?.let { msg ->
            snackbarHostState.showSnackbar("Export failed: $msg")
            exportViewModel.clearError()
        }
    }

    if (showExportDialog) {
        ExportOptionsDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format, filter ->
                showExportDialog = false
                exportViewModel.export(context, routeId, format, filter)
            },
            trackName = uiState.track?.name
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DepthAwareTopBar(
                title = uiState.track?.name ?: "Journey Details",
                depth = NavigationDepth.LEVEL_1,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Export track")
                    }
                    IconButton(onClick = onOpenMap) {
                        Icon(Icons.Default.Map, contentDescription = "View Map")
                    }
                    IconButton(onClick = onOpenInsights) {
                        Icon(Icons.Default.Insights, contentDescription = "View Insights")
                    }
                    IconButton(onClick = onOpenHwEvents) {
                        Icon(Icons.Default.History, contentDescription = "Hardware Events")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) { LoadingScreen(); return@Scaffold }

        val track = uiState.track ?: return@Scaffold
        val gpsPoints = if (uiState.locations.isNotEmpty()) uiState.locations.size else track.locationCount
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.l),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
        ) {
            DetailSummaryCard(track = track)

            // Metrics grid (2 columns)
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                MetricTile(Icons.Default.Straighten, "Distance", track.getFormattedDistance(), Modifier.weight(1f))
                MetricTile(Icons.Default.Timer, "Duration", track.getFormattedDuration(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                MetricTile(Icons.Default.Speed, "Avg speed", "%.0f km/h".format(track.avgSpeedKmh), Modifier.weight(1f))
                MetricTile(Icons.Default.Place, "GPS points", gpsPoints.toString(), Modifier.weight(1f))
            }
            if (track.reimbursableAmount > 0 || track.selectedVehicleType.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                    MetricTile(
                        Icons.Default.CheckCircle, "Amount",
                        if (track.reimbursableAmount > 0) "₹%.0f".format(track.reimbursableAmount) else "—",
                        Modifier.weight(1f)
                    )
                    MetricTile(
                        Icons.Default.Map, "Vehicle",
                        humanizeVehicle(track.selectedVehicleType),
                        Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.xs))
            FilledTonalButton(onClick = { showExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Download, null, Modifier.size(18.dp)); Spacer(Modifier.size(DesignTokens.Spacing.s))
                Text(if (exportState.isExporting) "Exporting…" else "Export track data")
            }
            FilledTonalButton(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Map, null, Modifier.size(18.dp)); Spacer(Modifier.size(DesignTokens.Spacing.s))
                Text("View route map")
            }
            FilledTonalButton(onClick = onOpenInsights, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Insights, null, Modifier.size(18.dp)); Spacer(Modifier.size(DesignTokens.Spacing.s))
                Text("Trip insights")
            }
            FilledTonalButton(onClick = onOpenHwEvents, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.History, null, Modifier.size(18.dp)); Spacer(Modifier.size(DesignTokens.Spacing.s))
                Text("Hardware events")
            }
        }
    }
}

@Composable
private fun DetailSummaryCard(track: TrackDisplayData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedLg,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.raised)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignTokens.topBarGradientBrush())
                .padding(DesignTokens.Spacing.xl)
        ) {
            DetailStatusChip(isSubmitted = track.isSubmitted)
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = track.getFormattedDistance(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (track.startTime > 0) {
                Text(
                    text = DateUtils.epochToDisplayDate(track.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(DesignTokens.IconSize.actionTile))
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailStatusChip(isSubmitted: Boolean) {
    val label = if (isSubmitted) "Submitted" else "Saved"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = DesignTokens.Spacing.m, vertical = 4.dp)
    ) {
        if (isSubmitted) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(DesignTokens.IconSize.inline))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

/** Turns a camelCase vehicle key (e.g. "fourWheelerPetrol") into "Four Wheeler Petrol". */
private fun humanizeVehicle(key: String): String {
    if (key.isBlank()) return "—"
    return key.replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { it.uppercaseChar() }
}
