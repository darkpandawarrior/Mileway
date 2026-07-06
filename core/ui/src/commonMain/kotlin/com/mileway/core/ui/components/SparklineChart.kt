package com.mileway.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Small commonMain line chart for a single telemetry series (speed/altitude/accel etc). No chart
 * library — just a [Canvas] polyline scaled to the values' own min/max, per Wave 3 live-map polish.
 *
 * Renders nothing when [values] has fewer than 2 points (nothing to draw a line between).
 */
@Composable
fun SparklineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidthDp: Float = 2f,
) {
    if (values.size < 2) return

    val min = values.min()
    val max = values.max()
    val range = (max - min).takeIf { it > 0f } ?: 1f

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val stepX = size.width / (values.size - 1)
        val path =
            androidx.compose.ui.graphics.Path().apply {
                values.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - ((value - min) / range) * size.height
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
        drawPath(path = path, color = color, style = Stroke(width = strokeWidthDp))
    }
}
