package com.miletracker.feature.tracking.ui.components

import androidx.compose.ui.graphics.Color

enum class IssueSeverity(val displayName: String, val color: Color) {
    Low("Low", Color(0xFF4CAF50)),
    Warning("Warning", Color(0xFFFF9800)),
    High("High", Color(0xFFFF5722)),
    Critical("Critical", Color(0xFFE91E63))
}

data class SystemIssue(
    val message: String,
    val impact: String,
    val severity: IssueSeverity
)
