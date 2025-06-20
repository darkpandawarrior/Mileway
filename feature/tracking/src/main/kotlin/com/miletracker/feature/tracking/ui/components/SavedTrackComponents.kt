package com.miletracker.feature.tracking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.core.data.util.DateUtils

@Composable
fun SavedTrackOverviewCard(track: TrackDisplayData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.name ?: "Track Miles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        DateUtils.epochToDisplayDate(track.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(
                    text = if (track.isSubmitted) "Submitted" else "Draft",
                    color = if (track.isSubmitted) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    icon = Icons.Default.Place,
                    label = "Distance",
                    value = track.getFormattedDistance(),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Duration",
                    value = track.getFormattedDuration(),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                StatItem(
                    icon = Icons.Default.Speed,
                    label = "Avg Speed",
                    value = "%.1f km/h".format(track.avgSpeedKmh),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }

            if (track.isSubmitted && track.reimbursableAmount > 0) {
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Reimbursable", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "₹%.2f".format(track.reimbursableAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun DataQualityReportCard(
    qualityScore: Int,
    locationCount: Int,
    mockCount: Int,
    abnormalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Data Quality", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QualityDot(qualityScore)
                    Text(
                        " $qualityScore%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = qualityColor(qualityScore)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            DataQualityItem(label = "Total Points", value = "$locationCount")
            DataQualityItem(
                label = "Mock Locations",
                value = "$mockCount",
                trend = if (mockCount == 0) TrendDirection.STABLE else TrendDirection.DOWN
            )
            DataQualityItem(
                label = "Abnormal Points",
                value = "$abnormalCount",
                trend = if (abnormalCount < 5) TrendDirection.STABLE else TrendDirection.DOWN
            )
        }
    }
}
