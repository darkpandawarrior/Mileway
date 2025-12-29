package com.miletracker.core.data.model.display

enum class OdometerPurpose { START, END }

data class OdometerCaptureResult(
    val purpose: OdometerPurpose,
    val imageUri: String,
    val reading: Int,
    val isManual: Boolean,
    val captureTimeMs: Long,
)
