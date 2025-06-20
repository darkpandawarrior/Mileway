package com.miletracker.feature.tracking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.tracking.viewmodel.TrackMilesUiState
import com.miletracker.feature.tracking.viewmodel.TrackMilesPhase
import com.miletracker.feature.tracking.viewmodel.TrackMilesViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMilesScreen(
    onStop: (id: String, distKm: Double, vehicleKey: String, startTime: Long, endTime: Long) -> Unit,
    onOpenMap: () -> Unit,
    onOpenHwEvents: () -> Unit,
    viewModel: TrackMilesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isActive = uiState.phase == TrackMilesPhase.TRACKING || uiState.phase == TrackMilesPhase.PAUSED

    Scaffold(
        topBar = { DepthAwareTopBar(title = "Track Miles", depth = NavigationDepth.ROOT) },
        floatingActionButton = {
            if (isActive) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallFloatingActionButton(onClick = onOpenHwEvents) {
                        Icon(Icons.Default.History, contentDescription = "Hardware Events")
                    }
                    FloatingActionButton(onClick = onOpenMap) {
                        Icon(Icons.Default.Map, contentDescription = "Open Map")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle selector (shown only when idle)
            if (uiState.phase == TrackMilesPhase.IDLE && uiState.vehicles.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    TextField(
                        value = uiState.selectedVehicle?.vehicleName ?: "Select vehicle",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, null) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        uiState.vehicles.forEach { v ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(v.vehicleName ?: "", style = MaterialTheme.typography.bodyMedium)
                                        Text("₹${v.vehiclePricing}/km", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { viewModel.selectVehicle(v); expanded = false }
                            )
                        }
                    }
                }
            }

            // Hero tracking card (shown during/after tracking)
            if (uiState.phase != TrackMilesPhase.IDLE) {
                HeroTrackingCard(uiState = uiState)
            }

            Spacer(Modifier.weight(1f))

            // Control buttons
            when (uiState.phase) {
                TrackMilesPhase.IDLE -> {
                    Button(
                        onClick = { viewModel.startTracking() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = uiState.selectedVehicle != null
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Journey")
                    }
                }
                TrackMilesPhase.TRACKING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(onClick = { viewModel.pauseTracking() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Pause, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Pause")
                        }
                        Button(onClick = { viewModel.stopTracking() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Stop, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Stop")
                        }
                    }
                }
                TrackMilesPhase.PAUSED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { viewModel.discardTracking() }, modifier = Modifier.weight(1f)) { Text("Discard") }
                        Button(onClick = { viewModel.resumeTracking() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("Resume")
                        }
                    }
                }
                TrackMilesPhase.STOPPED -> {
                    val routeId = uiState.currentRouteId
                    Button(
                        onClick = {
                            if (routeId != null) onStop(
                                routeId,
                                uiState.distanceKm,
                                uiState.selectedVehicle?.vehicleKey ?: "",
                                uiState.startTime,
                                uiState.endTime
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) { Text("Submit Journey") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.discardTracking() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Discard")
                    }
                }
                else -> {}
            }
        }
    }
}

/**
 * Prominent hero card surfacing the live journey: big distance, a ticking duration,
 * average speed and reimbursable amount, on a brand gradient with a pulsing status pill.
 */
@Composable
private fun HeroTrackingCard(uiState: TrackMilesUiState) {
    // Live-ticking elapsed time while actively tracking; otherwise use the recorded duration.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isTracking = uiState.phase == TrackMilesPhase.TRACKING
    LaunchedEffect(isTracking, uiState.startTime) {
        while (isTracking) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedMs = when {
        isTracking && uiState.startTime > 0 -> nowMs - uiState.startTime
        else -> uiState.durationMs
    }.coerceAtLeast(0L)
    val avgSpeed = if (elapsedMs > 0) uiState.distanceKm / (elapsedMs / 3_600_000.0) else 0.0

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
            StatusPill(phase = uiState.phase)
            Spacer(Modifier.height(DesignTokens.Spacing.l))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.2f".format(uiState.distanceKm),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            uiState.selectedVehicle?.vehicleName?.let { vehicle ->
                Text(vehicle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            }
            Spacer(Modifier.height(DesignTokens.Spacing.l))
            Row(modifier = Modifier.fillMaxWidth()) {
                HeroMetric("Duration", formatElapsed(elapsedMs), Modifier.weight(1f))
                HeroMetric("Avg speed", "%.0f km/h".format(avgSpeed), Modifier.weight(1f))
                HeroMetric("Amount", "₹%.0f".format(uiState.reimbursableAmount), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun StatusPill(phase: TrackMilesPhase) {
    val label = when (phase) {
        TrackMilesPhase.TRACKING -> "Tracking in progress"
        TrackMilesPhase.PAUSED -> "Paused"
        TrackMilesPhase.STOPPED -> "Ready to submit"
        else -> ""
    }
    // Pulsing dot only while actively tracking.
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(DesignTokens.Shape.chip)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = DesignTokens.Spacing.m, vertical = 6.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .alpha(if (phase == TrackMilesPhase.TRACKING) dotAlpha else 1f)
                .clip(CircleShape)
                .background(Color.White)
        )
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
