package com.miletracker.feature.media.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.feature.media.model.AttachmentItem
import com.miletracker.feature.media.model.AttachmentSource
import com.miletracker.feature.media.model.UploadState
import com.miletracker.feature.media.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single immutable UI state for the whole media-capture flow. Shared across the
 * selection -> camera/gallery -> preview -> OCR-sheet journey via a nav-graph
 * scoped ViewModel.
 */
data class MediaUiState(
    val attachments: List<AttachmentItem> = emptyList(),
    val selectedSource: AttachmentSource? = null,
    val pendingItem: AttachmentItem? = null,
    val ocrSheetVisible: Boolean = false,
    val isProcessing: Boolean = false
)

class MediaViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    /** User tapped one of the source tiles (Camera / Gallery / Files). */
    fun onSourceSelected(source: AttachmentSource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    /** A photo was captured by the CameraX flow. */
    fun onCaptured(uri: String) {
        _uiState.update {
            it.copy(
                pendingItem = AttachmentItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    source = AttachmentSource.CAMERA,
                    capturedAtMillis = System.currentTimeMillis()
                ),
                ocrSheetVisible = false
            )
        }
    }

    /** An image was picked from the gallery (or files). */
    fun onPickedFromGallery(uri: String) {
        _uiState.update {
            it.copy(
                pendingItem = AttachmentItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    source = it.selectedSource ?: AttachmentSource.GALLERY,
                    capturedAtMillis = System.currentTimeMillis()
                ),
                ocrSheetVisible = false
            )
        }
    }

    /** Run the (mocked) OCR pass over the pending item and surface the result sheet. */
    fun runOcr() {
        val pending = _uiState.value.pendingItem ?: return
        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            val result = repository.runOcr(pending.uri)
            _uiState.update { state ->
                state.copy(
                    pendingItem = state.pendingItem?.copy(ocr = result),
                    ocrSheetVisible = true,
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Confirm the pending attachment: run the mocked upload, then add the finished
     * item to the gallery list and clear the pending slot + sheet.
     */
    fun confirmPending() {
        val pending = _uiState.value.pendingItem ?: return
        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            val uploading = pending.copy(uploadState = UploadState.Uploading)
            val done: UploadState = repository.upload(uploading)
            val finished = uploading.copy(uploadState = done)
            _uiState.update { state ->
                state.copy(
                    attachments = state.attachments + finished,
                    pendingItem = null,
                    ocrSheetVisible = false,
                    selectedSource = null,
                    isProcessing = false
                )
            }
        }
    }

    /** Discard the pending capture and let the user shoot again. */
    fun retake() {
        _uiState.update { it.copy(pendingItem = null, ocrSheetVisible = false) }
    }

    /** Hide the OCR result sheet without discarding the pending item. */
    fun dismissSheet() {
        _uiState.update { it.copy(ocrSheetVisible = false) }
    }
}
