@file:Suppress("ktlint:standard:function-naming")

package com.miletracker.feature.media.ui.previews

import androidx.compose.runtime.Composable
import com.miletracker.core.ui.previews.PreviewLightDark
import com.miletracker.core.ui.previews.PreviewMatrix
import com.miletracker.feature.media.model.OcrResult
import com.miletracker.feature.media.ui.sheets.OcrResultBottomSheet

@PreviewLightDark
@Composable
internal fun PreviewOcrResultConfident() {
    OcrResultBottomSheet(
        result =
            OcrResult(
                rawText = "87452",
                detectedOdometer = "87452",
                confidence = 0.97f,
                watermarkApplied = false,
            ),
        onConfirm = {},
        onEdit = {},
        onDismiss = {},
    )
}

@PreviewMatrix
@Composable
internal fun PreviewOcrResultLowConfidence() {
    OcrResultBottomSheet(
        result =
            OcrResult(
                rawText = "8?452",
                detectedOdometer = null,
                confidence = 0.42f,
                watermarkApplied = false,
            ),
        onConfirm = {},
        onEdit = {},
        onDismiss = {},
    )
}
