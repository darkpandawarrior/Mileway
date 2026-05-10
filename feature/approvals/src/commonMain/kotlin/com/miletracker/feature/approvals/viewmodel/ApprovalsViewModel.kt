package com.miletracker.feature.approvals.viewmodel

import androidx.lifecycle.ViewModel
import com.miletracker.feature.approvals.model.ApprovalItem
import com.miletracker.feature.approvals.model.ApprovalStatus
import com.miletracker.feature.approvals.model.ClarificationMessage
import com.miletracker.feature.approvals.repository.ApprovalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ApprovalTabFilter { ALL, PENDING, APPROVED, REJECTED }

data class ApprovalsUiState(
    val items: List<ApprovalItem> = ApprovalsRepository.all,
    val activeTab: ApprovalTabFilter = ApprovalTabFilter.ALL,
    val snackbarMessage: String? = null,
)

data class ApprovalDetailState(
    val item: ApprovalItem? = null,
    val thread: List<ClarificationMessage> = emptyList(),
    val showClarificationSheet: Boolean = false,
    val draftMessage: String = "",
    val localStatus: ApprovalStatus? = null,
    val snackbarMessage: String? = null,
)

class ApprovalsViewModel : ViewModel() {

    private val _list = MutableStateFlow(ApprovalsUiState())
    val listState: StateFlow<ApprovalsUiState> = _list.asStateFlow()

    private val _detail = MutableStateFlow(ApprovalDetailState())
    val detailState: StateFlow<ApprovalDetailState> = _detail.asStateFlow()

    fun setTab(tab: ApprovalTabFilter) = _list.update { it.copy(activeTab = tab) }

    fun openDetail(id: String) {
        val item = _list.value.items.firstOrNull { it.id == id } ?: return
        _detail.value = ApprovalDetailState(
            item = item,
            thread = ApprovalsRepository.clarificationThread(id),
        )
    }

    fun approve() {
        val id = _detail.value.item?.id ?: return
        _detail.update { it.copy(localStatus = ApprovalStatus.APPROVED, snackbarMessage = "Request approved") }
        _list.update { s -> s.copy(items = ApprovalsRepository.approve(id)) }
    }

    fun reject() {
        val id = _detail.value.item?.id ?: return
        _detail.update { it.copy(localStatus = ApprovalStatus.REJECTED, snackbarMessage = "Request rejected") }
        _list.update { s -> s.copy(items = ApprovalsRepository.reject(id)) }
    }

    fun openClarificationSheet() = _detail.update { it.copy(showClarificationSheet = true) }
    fun closeClarificationSheet() = _detail.update { it.copy(showClarificationSheet = false) }
    fun setDraftMessage(text: String) = _detail.update { it.copy(draftMessage = text) }

    fun sendClarification() {
        val text = _detail.value.draftMessage.trim()
        if (text.isBlank()) return
        val newMsg = ClarificationMessage(text, isFromRequester = false, timestampMs = kotlin.time.Clock.System.now().toEpochMilliseconds())
        _detail.update { it.copy(thread = it.thread + newMsg, draftMessage = "", showClarificationSheet = false) }
    }

    fun clearSnackbar() {
        _list.update { it.copy(snackbarMessage = null) }
        _detail.update { it.copy(snackbarMessage = null) }
    }
}
