package com.miletracker.feature.tracking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.tracking.ui.components.CameraCaptureSheet
import com.miletracker.feature.tracking.ui.components.CaptureMode
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
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
    onSuccess: (distKm: Double, reimbursable: Double, vehicleKey: String, startTime: Long, endTime: Long, transId: String?) -> Unit,
    onBack: () -> Unit,
    viewModel: MileageSubmissionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var captureRequest by remember { mutableStateOf<CaptureMode?>(null) }
    var odometerEnabled by remember { mutableStateOf(false) }
    var odoStart by remember { mutableStateOf("") }
    var odoEnd by remember { mutableStateOf("") }
    val receipts = remember { mutableStateListOf<String>() }

    LaunchedEffect(state) {
        val s = state
        if (s is SubmissionUiState.Success) {
            onSuccess(
                distanceKm,
                s.response.reimbursableAmount ?: 0.0,
                vehicleKey,
                startTime,
                endTime,
                s.response.transId
            )
        }
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Submit Journey",
                depth = NavigationDepth.LEVEL_2,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val s = state) {
                is SubmissionUiState.Idle -> {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        JourneySummaryCard(
                            distanceKm = distanceKm,
                            vehicleKey = vehicleKey,
                            startTime = startTime,
                            endTime = endTime
                        )
                        OdometerCard(
                            enabled = odometerEnabled,
                            startReading = odoStart,
                            endReading = odoEnd,
                            onEnabledChange = { odometerEnabled = it },
                            onStartChange = { odoStart = it },
                            onEndChange = { odoEnd = it },
                            onScan = { captureRequest = CaptureMode.ODOMETER }
                        )
                        ReceiptsCard(
                            receipts = receipts,
                            onAdd = { captureRequest = CaptureMode.PLAIN },
                            onRemove = { receipts.remove(it) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.submit(routeId, distanceKm, vehicleKey, startTime, endTime) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) { Text("Confirm & Submit") }
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                }
                is SubmissionUiState.Submitting -> {
                    Spacer(Modifier.weight(1f))
                    CircularProgressIndicator(Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Submitting journey…", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                }
                is SubmissionUiState.Success -> {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Journey Submitted!", style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center)
                    s.response.reimbursableAmount?.let { amt ->
                        Text("Reimbursable: ₹%.2f".format(amt), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { onSuccess(distanceKm, s.response.reimbursableAmount ?: 0.0, vehicleKey, startTime, endTime, s.response.transId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Done") }
                }
                is SubmissionUiState.Error -> {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Submission failed", style = MaterialTheme.typography.titleMedium)
                    Text(s.message, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { viewModel.submit(routeId, distanceKm, vehicleKey, startTime, endTime) },
                        modifier = Modifier.fillMaxWidth()) { Text("Retry") }
                    OutlinedButton(onClick = { viewModel.reset(); onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                }
            }
        }

            captureRequest?.let { mode ->
                CameraCaptureSheet(
                    mode = mode,
                    onDismiss = { captureRequest = null },
                    onOdometerReading = { reading ->
                        if (reading.isNotBlank()) odoEnd = reading
                        odometerEnabled = true
                        captureRequest = null
                    },
                    onPhotoCaptured = { uri ->
                        receipts.add(uri)
                        captureRequest = null
                    }
                )
            }
        }
    }
}

/** Review card summarising the journey before submission. */
@Composable
private fun JourneySummaryCard(
    distanceKm: Double,
    vehicleKey: String,
    startTime: Long,
    endTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Text(
                "Journey Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            SummaryRow(Icons.Default.Straighten, "Distance", "%.2f km".format(distanceKm))
            HorizontalDivider(Modifier.padding(vertical = DesignTokens.Spacing.s))
            SummaryRow(Icons.Default.Timer, "Duration", formatTripDuration(startTime, endTime))
            HorizontalDivider(Modifier.padding(vertical = DesignTokens.Spacing.s))
            SummaryRow(Icons.Default.DirectionsCar, "Vehicle", humanizeVehicleKey(vehicleKey))
        }
    }
}

@Composable
private fun SummaryRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(DesignTokens.Spacing.m))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

/**
 * Optional proof-of-journey receipts. Captures photos through the shared camera sheet
 * (mocked upload) and shows them as removable thumbnails.
 */
@Composable
private fun ReceiptsCard(
    receipts: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(DesignTokens.Spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Receipts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Optional — attach proof photos", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onAdd) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(DesignTokens.Spacing.s))
                    Text("Add")
                }
            }
            if (receipts.isNotEmpty()) {
                Spacer(Modifier.height(DesignTokens.Spacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
                ) {
                    receipts.forEach { uri ->
                        ReceiptThumbnail(uri = uri, onRemove = { onRemove(uri) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptThumbnail(uri: String, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(88.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = "Receipt photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        // Mocked "uploaded" badge.
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Uploaded",
            tint = DesignTokens.StatusColors.success,
            modifier = Modifier.align(Alignment.BottomStart).padding(2.dp).size(18.dp)
        )
        // Remove control.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(14.dp))
        }
    }
}

/** Turns a camelCase vehicle key (e.g. "fourWheelerPetrol") into "Four Wheeler Petrol". */
private fun humanizeVehicleKey(key: String): String {
    if (key.isBlank()) return "—"
    return key
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replaceFirstChar { it.uppercaseChar() }
}

private fun formatTripDuration(startTime: Long, endTime: Long): String {
    if (startTime <= 0 || endTime <= startTime) return "—"
    val totalMin = (endTime - startTime) / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/**
 * Optional odometer reading capture (stateless — state hoisted to the screen). Manual
 * start/end entry plus a "Scan with camera" action that runs the real camera + mocked
 * OCR to fill the end reading, and a demo auto-fill shortcut.
 */
@Composable
private fun OdometerCard(
    enabled: Boolean,
    startReading: String,
    endReading: String,
    onEnabledChange: (Boolean) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onScan: () -> Unit
) {
    val odometerDistance = run {
        val s = startReading.toDoubleOrNull()
        val e = endReading.toDoubleOrNull()
        if (s != null && e != null && e >= s) e - s else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Straighten, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(DesignTokens.Spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Odometer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Optional — record start & end readings", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }

            if (enabled) {
                Spacer(Modifier.height(DesignTokens.Spacing.m))
                Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                    OutlinedTextField(
                        value = startReading,
                        onValueChange = { onStartChange(it.filter(Char::isDigit)) },
                        label = { Text("Start") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endReading,
                        onValueChange = { onEndChange(it.filter(Char::isDigit)) },
                        label = { Text("End") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(DesignTokens.Spacing.s))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onScan) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(DesignTokens.Spacing.s))
                        Text("Scan with camera")
                    }
                    Spacer(Modifier.size(DesignTokens.Spacing.s))
                    TextButton(onClick = { onStartChange("48213"); onEndChange("48221") }) {
                        Text("Auto-fill")
                    }
                }
                if (odometerDistance != null) {
                    Text(
                        "Odometer distance: %.0f km".format(odometerDistance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
