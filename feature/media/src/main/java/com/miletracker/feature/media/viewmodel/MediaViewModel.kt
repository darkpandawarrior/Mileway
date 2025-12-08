package com.miletracker.feature.media.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.feature.media.model.AttachmentItem
import com.miletracker.feature.media.model.AttachmentSource
import com.miletracker.feature.media.model.FlashMode
import com.miletracker.feature.media.model.UploadState
import com.miletracker.feature.media.repository.MediaLibraryRepository
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
 *
 * @param attachments     confirmed attachments shown in the selection grid
 * @param selectedSource  the source tile last tapped (drives the camera/gallery launch)
 * @param pendingItem     the single in-flight capture awaiting preview/confirmation
 * @param pendingBatch    multi-capture buffer: every photo taken before the user confirms.
 *                        Drives the preview thumbnail grid + the "Use N photos" confirm bar.
 *                        The first entry mirrors [pendingItem] for the single-photo flow.
 * @param flashMode       current camera flash mode, cycled by the in-camera toggle
 * @param ocrSheetVisible whether the OCR result sheet is currently shown
 * @param isProcessing    a blocking op (OCR / upload) is running
 */
data class MediaUiState(
    val attachments: List<AttachmentItem> = emptyList(),
    val selectedSource: AttachmentSource? = null,
    val pendingItem: AttachmentItem? = null,
    val pendingBatch: List<AttachmentItem> = emptyList(),
    val flashMode: FlashMode = FlashMode.AUTO,
    val ocrSheetVisible: Boolean = false,
    val isProcessing: Boolean = false
)

class MediaViewModel(
    private val repository: MediaRepository,
    private val libraryRepository: MediaLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    /** User tapped one of the source tiles (Camera / Gallery / Files). */
    fun onSourceSelected(source: AttachmentSource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    /** Cycle the camera flash mode AUTO -> ON -> OFF -> AUTO. */
    fun cycleFlashMode() {
        _uiState.update {
            val next = when (it.flashMode) {
                FlashMode.AUTO -> FlashMode.ON
                FlashMode.ON -> FlashMode.OFF
                FlashMode.OFF -> FlashMode.AUTO
            }
            it.copy(flashMode = next)
        }
    }

    /**
     * A photo was captured by the CameraX flow. Sets it as the pending item and
     * appends it to the multi-capture [MediaUiState.pendingBatch] so the preview screen
     * can offer a "review / use N photos" grid before any are committed.
     */
    fun onCaptured(uri: String) {
        val item = AttachmentItem(
            id = UUID.randomUUID().toString(),
            uri = uri,
            source = AttachmentSource.CAMERA,
            capturedAtMillis = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(
                pendingItem = item,
                pendingBatch = it.pendingBatch + item,
                ocrSheetVisible = false
            )
        }
    }

    /** An image was picked from the gallery (or files). */
    fun onPickedFromGallery(uri: String) {
        _uiState.update {
            val item = AttachmentItem(
                id = UUID.randomUUID().toString(),
                uri = uri,
                source = it.selectedSource ?: AttachmentSource.GALLERY,
                capturedAtMillis = System.currentTimeMillis()
            )
            it.copy(
                pendingItem = item,
                pendingBatch = it.pendingBatch + item,
                ocrSheetVisible = false
            )
        }
    }

    /**
     * Remove a single not-yet-confirmed capture from the multi-capture batch (delete
     * overlay on a preview thumbnail). Keeps [MediaUiState.pendingItem] pointed at a
     * surviving item, or clears it when the batch becomes empty.
     */
    fun removeFromBatch(id: String) {
        _uiState.update { state ->
            val batch = state.pendingBatch.filterNot { it.id == id }
            state.copy(
                pendingBatch = batch,
                pendingItem = when {
                    batch.isEmpty() -> null
                    state.pendingItem?.id == id -> batch.last()
                    else -> state.pendingItem
                },
                ocrSheetVisible = state.ocrSheetVisible && batch.isNotEmpty()
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
                val updated = state.pendingItem?.copy(ocr = result)
                state.copy(
                    pendingItem = updated,
                    // Keep the matching batch entry in sync so the OCR result rides along
                    // when the whole batch is committed.
                    pendingBatch = state.pendingBatch.map {
                        if (updated != null && it.id == updated.id) updated else it
                    },
                    ocrSheetVisible = true,
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Confirm the pending capture(s): run the mocked upload over every item in the
     * multi-capture batch (falling back to the single pending item), then add the
     * finished items to the gallery list and clear the pending slot + sheet.
     */
    fun confirmPending() {
        val state = _uiState.value
        val toUpload = state.pendingBatch.ifEmpty { listOfNotNull(state.pendingItem) }
        if (toUpload.isEmpty()) return
        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            val finished = toUpload.map { item ->
                val uploading = item.copy(uploadState = UploadState.Uploading)
                val done: UploadState = repository.upload(uploading)
                uploading.copy(uploadState = done)
            }
            finished.forEach { libraryRepository.save(it) }
            _uiState.update { current ->
                current.copy(
                    attachments = current.attachments + finished,
                    pendingItem = null,
                    pendingBatch = emptyList(),
                    ocrSheetVisible = false,
                    selectedSource = null,
                    isProcessing = false
                )
            }
        }
    }

    /** Discard the entire pending batch and let the user shoot again. */
    fun retake() {
        _uiState.update {
            it.copy(pendingItem = null, pendingBatch = emptyList(), ocrSheetVisible = false)
        }
    }

    /** Hide the OCR result sheet without discarding the pending item. */
    fun dismissSheet() {
        _uiState.update { it.copy(ocrSheetVisible = false) }
    }
}
