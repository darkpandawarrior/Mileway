package com.miletracker.feature.logging.viewmodel

import androidx.lifecycle.ViewModel
import com.miletracker.feature.logging.model.ExpenseCategory
import com.miletracker.feature.logging.model.ExpenseRecord
import com.miletracker.feature.logging.model.ExpenseStatus
import com.miletracker.feature.logging.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ExpenseFilter { ALL, DRAFTS, PENDING, SETTLED }

data class ExpenseListState(
    val records: List<ExpenseRecord> = emptyList(),
    val activeFilter: ExpenseFilter = ExpenseFilter.ALL,
    val snackbarMessage: String? = null,
)

data class ExpenseFormState(
    val step: Int = 1,
    val category: ExpenseCategory? = null,
    val amountText: String = "",
    val merchantName: String = "",
    val note: String = "",
    val submitted: Boolean = false,
    val submittedAmount: Double = 0.0,
    val submittedId: String = "",
)

class ExpenseViewModel(
    private val repository: ExpenseRepository,
) : ViewModel() {
    private val _listState = MutableStateFlow(ExpenseListState(records = repository.getAll()))
    val listState: StateFlow<ExpenseListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    fun setFilter(filter: ExpenseFilter) {
        val filtered =
            when (filter) {
                ExpenseFilter.ALL -> repository.getAll()
                ExpenseFilter.DRAFTS -> repository.filterByStatus(ExpenseStatus.DRAFT)
                ExpenseFilter.PENDING -> repository.filterByStatus(ExpenseStatus.PENDING)
                ExpenseFilter.SETTLED -> repository.filterByStatus(ExpenseStatus.APPROVED)
            }
        _listState.update { it.copy(records = filtered, activeFilter = filter) }
    }

    fun selectCategory(cat: ExpenseCategory) {
        _formState.update { it.copy(category = cat, step = 2) }
    }

    fun setAmount(text: String) = _formState.update { it.copy(amountText = text) }

    fun setMerchant(name: String) = _formState.update { it.copy(merchantName = name) }

    fun setNote(note: String) = _formState.update { it.copy(note = note) }

    fun submitExpense() {
        val form = _formState.value
        val amount = form.amountText.toDoubleOrNull() ?: 0.0
        val id = "EXP-NEW-${(form.merchantName.hashCode() and 0x7FFF_FFFF) % 9000 + 1000}"
        _formState.update { it.copy(submitted = true, submittedAmount = amount, submittedId = id) }
    }

    fun resetForm() {
        _formState.value = ExpenseFormState()
    }

    fun getExpense(id: String): ExpenseRecord? = repository.getById(id)

    fun clearSnackbar() = _listState.update { it.copy(snackbarMessage = null) }
}
