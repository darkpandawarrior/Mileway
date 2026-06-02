package com.miletracker.feature.tracking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.theme.DesignTokens

// ─────────────────────────────────────────────────────────────────────────────
// Section tab chips with progress bar
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Horizontal section-tab chip row (Journey / Vehicle / Odometer / Forms) with a thin progress bar
 * above it. The progress fraction is derived from the index of [selected] among [tabs], so it
 * advances as the user moves through the sections.
 *
 * @param tabs Ordered tab labels.
 * @param selected The currently active tab label; the matching chip is filled.
 * @param onSelect Invoked with the tapped tab label.
 */
@Composable
fun SubmissionTabChips(
    tabs: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)
    val progress =
        if (tabs.isEmpty()) 0f else ((selectedIndex + 1).toFloat() / tabs.size).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.m))

        Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)) {
            tabs.forEach { tab ->
                SectionTabChip(
                    label = tab,
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                )
            }
        }
    }
}

/** A single pill chip in [SubmissionTabChips]; filled when selected, outlined otherwise. */
@Composable
private fun SectionTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val content =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val border =
        if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    Surface(
        shape = CircleShape,
        color = container,
        border = border,
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.s),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Static polyline thumbnail (Canvas-based route preview)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Canvas-drawn route thumbnail from a list of (lat, lng) coordinate pairs.
 * Normalises the bounding box to fill the canvas with some padding.
 * When the list is empty or has only 1 point, draws a dashed placeholder line.
 */
@Composable
fun StaticPolylineThumbnail(
    latLngs: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier,
    thumbHeight: Dp = 120.dp,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .clip(DesignTokens.Shape.roundedSm)
                .background(bgColor),
    ) {
        Canvas(modifier = Modifier.matchParentSize().padding(16.dp)) {
            if (latLngs.size < 2) {
                drawLine(
                    color = lineColor.copy(alpha = 0.3f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 4.dp.toPx(),
                    pathEffect =
                        androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f),
                            0f,
                        ),
                )
                return@Canvas
            }
            val lats = latLngs.map { it.first }
            val lngs = latLngs.map { it.second }
            val minLat = lats.min()
            val maxLat = lats.max()
            val minLng = lngs.min()
            val maxLng = lngs.max()
            val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

            fun toOffset(
                lat: Double,
                lng: Double,
            ) = Offset(
                x = ((lng - minLng) / lngRange * size.width).toFloat(),
                y = size.height - ((lat - minLat) / latRange * size.height).toFloat(),
            )

            val path = androidx.compose.ui.graphics.Path()
            val first = latLngs.first()
            path.moveTo(toOffset(first.first, first.second).x, toOffset(first.first, first.second).y)
            for (i in 1 until latLngs.size) {
                val o = toOffset(latLngs[i].first, latLngs[i].second)
                path.lineTo(o.x, o.y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style =
                    Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
            val startPt = toOffset(latLngs.first().first, latLngs.first().second)
            val endPt = toOffset(latLngs.last().first, latLngs.last().second)
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF22C55E), radius = 6.dp.toPx(), center = startPt)
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFFEF4444), radius = 6.dp.toPx(), center = endPt)
        }
    }
}
