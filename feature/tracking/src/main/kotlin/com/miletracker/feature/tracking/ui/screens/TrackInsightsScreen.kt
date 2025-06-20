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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.tracking.ui.components.DataQualityReportCard
import com.miletracker.feature.tracking.ui.components.SectionHeader
import com.miletracker.feature.tracking.ui.components.StatItem
import com.miletracker.feature.tracking.ui.components.formatDuration
import com.miletracker.feature.tracking.ui.components.qualityColor
import com.miletracker.feature.tracking.viewmodel.TrackInsightsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackInsightsScreen(
    routeId: String,
    onBack: () -> Unit,
    viewModel: TrackInsightsViewModel = koinViewModel()
) {
    LaunchedEffect(routeId) { viewModel.loadTrackInsights(routeId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val track by viewModel.track.collectAsState()

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Track Insights",
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
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "Error loading insights", color = MaterialTheme.colorScheme.error)
                    }
                }
                insights != null -> {
                    val data = insights!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Quality score header
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = qualityColor(data.qualityScore).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${data.qualityScore}",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = qualityColor(data.qualityScore)
                                    )
                                    Text("Quality Score", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        data.qualityLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = qualityColor(data.qualityScore),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        item {
                            SectionHeader("Journey Summary")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatItem(
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    label = "Distance",
                                    value = "%.2f km".format(data.distanceKm),
                                    modifier = Modifier.weight(1f)
                                )
                                StatItem(
                                    icon = Icons.Default.Timer,
                                    label = "Duration",
                                    value = formatDuration(data.durationMs),
                                    modifier = Modifier.weight(1f)
                                )
                                StatItem(
                                    icon = Icons.Default.Speed,
                                    label = "Avg Speed",
                                    value = "%.1f km/h".format(data.avgSpeedKmh),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            DataQualityReportCard(
                                qualityScore = data.qualityScore,
                                locationCount = data.locationCount,
                                mockCount = data.mockLocationCount,
                                abnormalCount = data.abnormalLocationCount,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (data.activityBreakdown.isNotEmpty()) {
                            item {
                                SectionHeader("Activity Breakdown")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        data.activityBreakdown.entries
                                            .sortedByDescending { it.value }
                                            .forEach { (activity, count) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(activity.lowercase().replaceFirstChar { it.uppercase() })
                                                    Text("$count points", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                    }
                                }
                            }
                        }

                        if (data.recommendations.isNotEmpty()) {
                            item {
                                SectionHeader("Recommendations")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        data.recommendations.forEach { rec ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Icon(
                                                    Icons.Default.LightMode,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp).padding(top = 2.dp),
                                                    tint = Color(0xFFFFC107)
                                                )
                                                Text(
                                                    " $rec",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}
