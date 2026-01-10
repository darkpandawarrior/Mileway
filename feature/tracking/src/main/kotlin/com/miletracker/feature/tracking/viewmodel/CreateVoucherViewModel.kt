package com.miletracker.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.display.TrackDisplayData
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.VoucherRecord
import com.miletracker.feature.tracking.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateVoucherUiState(
    val step: Int = 0,
    val expenses: List<TrackDisplayData> = emptyList(),
    val selectedTokens: Set<String> = emptySet(),
    val title: String = "",
    val category: String = "Travel",
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val submittedVoucherNumber: String? = null,
    val isLoading: Boolean = true,
)

class CreateVoucherViewModel(
    private val savedTrackRepository: SavedTrackRepository,
    private val voucherRepository: VoucherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoucherUiState())
    val uiState: StateFlow<CreateVoucherUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            val tracks = savedTrackRepository.completedTracksFlow().first()
                .filter { it.isSubmitted && it.reimbursableAmount > 0 }
            val defaultTitle = "Voucher — ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())}"
            _uiState.update { it.copy(expenses = tracks, title = defaultTitle, isLoading = false) }
        }
    }

    fun toggleSelection(token: String) {
        _uiState.update { state ->
            val selected = state.selectedTokens.toMutableSet()
            if (selected.contains(token)) selected.remove(token) else selected.add(token)
            state.copy(selectedTokens = selected)
        }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedTokens = it.expenses.map { e -> e.token }.toSet()) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedTokens = emptySet()) }
    }

    fun setTitle(v: String) = _uiState.update { it.copy(title = v) }
    fun setCategory(v: String) = _uiState.update { it.copy(category = v) }
    fun setNotes(v: String) = _uiState.update { it.copy(notes = v) }

    fun goToStep(step: Int) = _uiState.update { it.copy(step = step) }

    fun submit() {
        val state = _uiState.value
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val number = "V-${System.currentTimeMillis() % 10_000}"
            val total = state.expenses
                .filter { state.selectedTokens.contains(it.token) }
                .sumOf { it.reimbursableAmount }
            voucherRepository.save(
                VoucherRecord(
                    voucherNumber = number,
                    title = state.title,
                    category = state.category,
                    totalAmount = total,
                    notes = state.notes,
                    expenseRouteIds = state.selectedTokens.toList(),
                    createdAtMs = System.currentTimeMillis(),
                )
            )
            _uiState.update { it.copy(isSubmitting = false, submittedVoucherNumber = number, step = 3) }
        }
    }

    val totalAmount: Double
        get() {
            val state = _uiState.value
            return state.expenses
                .filter { state.selectedTokens.contains(it.token) }
                .sumOf { it.reimbursableAmount }
        }
}
