package com.miletracker.feature.tracking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.LoadingScreen
import com.miletracker.core.ui.components.StatCard
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
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
    viewModel: TrackDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(routeId) { viewModel.load(routeId) }

    Scaffold(
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Journey Details", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Distance", track.getFormattedDistance(), Modifier.weight(1f))
                StatCard("Duration", track.getFormattedDuration(), Modifier.weight(1f))
            }
            if (track.reimbursableAmount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Amount", "₹%.2f".format(track.reimbursableAmount), Modifier.weight(1f))
                    StatCard("Vehicle", track.selectedVehicleType, Modifier.weight(1f))
                }
            }
            HorizontalDivider()
            Text("Location Points: ${uiState.locations.size}", style = MaterialTheme.typography.bodyMedium)
            if (track.locationCount > 0) {
                Text("${track.locationCount} GPS points recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (track.isSubmitted) {
                Spacer(Modifier.height(8.dp))
                Text("✓ Submitted", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
