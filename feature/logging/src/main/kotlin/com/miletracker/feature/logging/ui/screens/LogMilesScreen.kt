package com.miletracker.feature.logging.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.logging.viewmodel.LogMilesViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMilesScreen(viewModel: LogMilesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Success overlay
    uiState.submissionResult?.let { result ->
        Scaffold(topBar = { DepthAwareTopBar(title = "Log Miles", depth = NavigationDepth.ROOT) }) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                Text("Miles Logged!", style = MaterialTheme.typography.headlineMedium)
                result.reimbursableAmount?.let { amt ->
                    Text("₹%.2f reimbursable".format(amt), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(32.dp))
                Button(onClick = { viewModel.resetSubmission() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Log Another")
                }
            }
        }
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Log Miles") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle dropdown
            var vehicleExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = vehicleExpanded, onExpandedChange = { vehicleExpanded = it }) {
                TextField(
                    value = uiState.selectedVehicle?.vehicleName ?: "Select vehicle",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Vehicle") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(vehicleExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = vehicleExpanded, onDismissRequest = { vehicleExpanded = false }) {
                    uiState.vehicles.forEach { v ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(v.vehicleName ?: "", style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${v.vehiclePricing}/km", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { viewModel.selectVehicle(v); vehicleExpanded = false }
                        )
                    }
                }
            }

            // Service dropdown (if services available)
            if (uiState.services.isNotEmpty()) {
                var serviceExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = serviceExpanded, onExpandedChange = { serviceExpanded = it }) {
                    TextField(
                        value = uiState.selectedService?.name ?: "Select service",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Service") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(serviceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = serviceExpanded, onDismissRequest = { serviceExpanded = false }) {
                        uiState.services.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = { viewModel.selectService(s); serviceExpanded = false }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Distance field
            var distanceText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = distanceText,
                onValueChange = { distanceText = it; it.toDoubleOrNull()?.let(viewModel::setDistance) },
                label = { Text("Distance (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("km") }
            )

            // Round trip toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = uiState.isRoundTrip,
                    onClick = { viewModel.setRoundTrip(!uiState.isRoundTrip) },
                    label = { Text("Round Trip") },
                    leadingIcon = { Icon(Icons.Default.SwapHoriz, null, Modifier.size(16.dp)) }
                )
            }

            // Pricing display
            if (uiState.reimbursableAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated Reimbursement", style = MaterialTheme.typography.bodyMedium)
                    Text("₹%.2f".format(uiState.reimbursableAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }

            // Error
            uiState.error?.let { err ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(err, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Submit
            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isSubmitting && uiState.distanceKm > 0 && uiState.selectedVehicle != null
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Miles")
                }
            }
        }
    }
}
