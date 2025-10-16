package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.stub.AnalyticsMockData
import com.miletracker.stub.RecentActivityItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsHomeScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val data = AnalyticsMockData

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0F4C75), Color(0xFF1B6CA8))))
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Last 30 days · all categories", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹%,.0f".format(data.totalSpend), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("total spend", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = DesignTokens.Spacing.l,
                    vertical = DesignTokens.Spacing.l
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
            ) {
                item { SpendingOverviewCard(data) }
                item { CategoryBreakdownCard(data, onOpenDetail) }
                item { PolicyHealthCard(data) }
                item {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(data.recentActivity) { item ->
                    RecentActivityRow(item)
                }
                item {
                    Text("Quick Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(DesignTokens.Spacing.s))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s), verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)) {
                        data.quickInsights.forEach { insight ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = DesignTokens.Shape.chip
                            ) {
                                Text(
                                    insight,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.m, vertical = DesignTokens.Spacing.s)
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(DesignTokens.Spacing.l)) }
            }
        }
    }
}

@Composable
private fun SpendingOverviewCard(data: AnalyticsMockData) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("7-Day Spending", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp)) {
                    Text("vs last week +12%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(DesignTokens.Spacing.m))

            // Bar chart
            val series = data.weeklySeries
            val maxAmount = series.maxOf { it.amountRupees }
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                val barCount = series.size
                val totalWidth = size.width
                val barWidth = totalWidth / (barCount * 1.8f)
                val gap = totalWidth / (barCount * 1.8f) * 0.8f
                val totalBlock = barWidth + gap
                val leftPad = (totalWidth - totalBlock * barCount + gap) / 2

                series.forEachIndexed { i, day ->
                    val barHeight = (day.amountRupees / maxAmount).toFloat() * size.height * 0.85f
                    val x = leftPad + i * totalBlock
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.xs))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                series.forEach { day ->
                    Text(day.dayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(data: AnalyticsMockData, onOpenDetail: (String) -> Unit) {
    val categoryColors = mapOf(
        "Mileage" to Color(0xFF2563EB),
        "Expense" to Color(0xFF7C3AED),
        "Travel" to Color(0xFFEA580C),
        "Advance" to Color(0xFF0F766E)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Text("Category Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(DesignTokens.Spacing.m))

            val total = data.categoryTotals.values.sum()
            // Horizontal stacked bar
            Row(
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp))
            ) {
                data.categoryTotals.entries.forEach { (category, amount) ->
                    val fraction = (amount / total).toFloat()
                    val color = categoryColors[category] ?: MaterialTheme.colorScheme.primary
                    Box(modifier = Modifier.fillMaxSize().weight(fraction).background(color))
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.m))
            data.categoryTotals.entries.forEach { (category, amount) ->
                val color = categoryColors[category] ?: MaterialTheme.colorScheme.primary
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(category) }.padding(vertical = DesignTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                    Text(category, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("₹%,.0f".format(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PolicyHealthCard(data: AnalyticsMockData) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val sweep = 360f * (data.compliancePercent / 100f)
                    drawArc(
                        color = surfaceVariantColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    "${data.compliancePercent}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Policy Health", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(DesignTokens.Spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                    PolicyStat(label = "Violations", value = "${data.violationCount}", color = DesignTokens.StatusColors.warning)
                    PolicyStat(label = "Hard Stops", value = "${data.hardStopCount}", color = DesignTokens.StatusColors.error)
                }
            }
        }
    }
}

@Composable
private fun PolicyStat(label: String, value: String, color: Color) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecentActivityRow(item: RecentActivityItem) {
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = DesignTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                item.category.first().toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("${item.subtitle} · ${sdf.format(Date(item.dateMs))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("₹%,.0f".format(item.amountRupees), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
