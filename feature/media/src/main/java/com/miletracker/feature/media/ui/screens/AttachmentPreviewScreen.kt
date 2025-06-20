package com.miletracker.feature.media.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.media.ui.sheets.OcrResultBottomSheet
import com.miletracker.feature.media.viewmodel.MediaViewModel

/**
 * Full-bleed preview of the pending capture with a Retake / Run OCR / Use photo
 * action row. The OCR result sheet is state-driven off [MediaUiState.ocrSheetVisible].
 *
 * @param onRetake   discard pending and pop back to the camera/selection
 * @param onUsePhoto invoked after the pending attachment is confirmed
 */
@Composable
fun AttachmentPreviewScreen(
    viewModel: MediaViewModel,
    onRetake: () -> Unit,
    onUsePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pending = state.pendingItem

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (pending != null) {
                    AsyncImage(
                        model = pending.uri,
                        contentDescription = "Captured attachment preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No photo to preview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.isProcessing) {
                    CircularProgressIndicator()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.l),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.retake()
                        onRetake()
                    },
                    enabled = !state.isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }
                TextButton(
                    onClick = { viewModel.runOcr() },
                    enabled = pending != null && !state.isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Run OCR")
                }
                Button(
                    onClick = {
                        viewModel.confirmPending()
                        onUsePhoto()
                    },
                    enabled = pending != null && !state.isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use photo")
                }
            }
        }
    }

    val ocr = pending?.ocr
    if (state.ocrSheetVisible && ocr != null) {
        OcrResultBottomSheet(
            result = ocr,
            onConfirm = {
                viewModel.confirmPending()
                onUsePhoto()
            },
            onEdit = { viewModel.dismissSheet() },
            onDismiss = { viewModel.dismissSheet() }
        )
    }
}
