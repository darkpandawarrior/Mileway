package com.miletracker.feature.tracking.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.data.model.db.CurrentTrackData
import com.miletracker.core.data.model.db.LocationData

@Composable
fun LiveTrackingOverviewCard(
    trackData: CurrentTrackData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Live Tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LiveIndicatorBadge(isPaused = trackData.isPaused)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                LiveMetric(
                    label = "Distance",
                    value = "%.2f km".format(trackData.distance / 1000.0),
                    icon = Icons.Default.GpsFixed,
                    color = MaterialTheme.colorScheme.primary
                )
                LiveMetric(
                    label = "Duration",
                    value = formatDuration(System.currentTimeMillis() - trackData.startTime),
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.secondary
                )
                LiveMetric(
                    label = "Speed",
                    value = "%.1f km/h".format(trackData.speed * 3.6),
                    icon = Icons.Default.Speed,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun LiveHealthMonitorCard(
    locationCount: Int,
    unsyncedCount: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("System Health", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                HealthIndicator(label = "GPS", value = "Active", color = Color(0xFF4CAF50), icon = Icons.Default.GpsFixed)
                HealthIndicator(label = "Points", value = "$locationCount", color = MaterialTheme.colorScheme.primary, icon = Icons.Default.NetworkCheck)
                HealthIndicator(label = "Battery", value = "OK", color = Color(0xFF8BC34A), icon = Icons.Default.Battery5Bar)
            }
        }
    }
}

@Composable
fun LiveSyncStatusCard(total: Long, unsynced: Long, modifier: Modifier = Modifier) {
    val synced = (total - unsynced).coerceAtLeast(0)
    val progress = if (total > 0) synced.toFloat() / total else 1f
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Sync Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("$synced/$total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = StrokeCap.Round,
                color = if (unsynced == 0L) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (unsynced == 0L) "All points synced" else "$unsynced points pending sync",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentEventsCard(events: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    if (events.isEmpty()) return
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recent Events", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            events.takeLast(3).reversed().forEach { (text, time) ->
                RecentEventItem(text, time)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun LiveMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HealthIndicator(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecentEventItem(text: String, time: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(timeAgo(time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LiveIndicatorBadge(isPaused: Boolean) {
    val infinite = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infinite.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse_alpha"
    )
    StatusBadge(
        text = if (isPaused) "PAUSED" else "LIVE",
        color = if (isPaused) Color(0xFFFF9800) else Color(0xFF4CAF50),
        modifier = if (isPaused) Modifier else Modifier.alpha(alpha)
    )
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0m"
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

private fun timeAgo(timestampMs: Long): String {
    val diffSec = (System.currentTimeMillis() - timestampMs) / 1000
    return when {
        diffSec < 60 -> "${diffSec}s ago"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        else -> "${diffSec / 3600}h ago"
    }
}
