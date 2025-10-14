package com.miletracker.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import com.miletracker.feature.profile.model.AdvanceRecord
import com.miletracker.feature.profile.model.AdvanceStatus
import com.miletracker.feature.profile.model.CorporateCard
import com.miletracker.feature.profile.repository.AdvanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AdvanceTabFilter { ALL, PENDING, SETTLED }

data class AdvanceListState(
    val records: List<AdvanceRecord> = emptyList(),
    val activeTab: AdvanceTabFilter = AdvanceTabFilter.ALL
)

data class AdvanceFormState(
    val step: Int = 1,
    val amountText: String = "",
    val purpose: String = "",
    val requiredByDate: String = "",
    val declarationChecked: Boolean = false,
    val submitted: Boolean = false,
    val submittedId: String = "",
    val autoApproved: Boolean = false
)

data class CardsState(
    val cards: List<CorporateCard> = emptyList(),
    val snackbarMessage: String? = null
)

class AdvanceViewModel(
    private val repository: AdvanceRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(AdvanceListState(records = repository.advanceRecords))
    val listState: StateFlow<AdvanceListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(AdvanceFormState())
    val formState: StateFlow<AdvanceFormState> = _formState.asStateFlow()

    private val _cardsState = MutableStateFlow(CardsState(cards = repository.cards))
    val cardsState: StateFlow<CardsState> = _cardsState.asStateFlow()

    fun setTab(tab: AdvanceTabFilter) {
        val filtered = when (tab) {
            AdvanceTabFilter.ALL -> repository.advanceRecords
            AdvanceTabFilter.PENDING -> repository.advanceRecords.filter {
                it.status in listOf(AdvanceStatus.PENDING, AdvanceStatus.UNDER_REVIEW, AdvanceStatus.APPROVED)
            }
            AdvanceTabFilter.SETTLED -> repository.advanceRecords.filter {
                it.status in listOf(AdvanceStatus.DISBURSED, AdvanceStatus.REJECTED)
            }
        }
        _listState.update { it.copy(records = filtered, activeTab = tab) }
    }

    fun setAmount(text: String) = _formState.update { it.copy(amountText = text) }
    fun setPurpose(text: String) = _formState.update { it.copy(purpose = text) }
    fun setRequiredByDate(date: String) = _formState.update { it.copy(requiredByDate = date) }
    fun setDeclaration(checked: Boolean) = _formState.update { it.copy(declarationChecked = checked) }
    fun goToStep(step: Int) = _formState.update { it.copy(step = step) }

    fun submitAdvance() {
        val form = _formState.value
        val amount = form.amountText.toDoubleOrNull() ?: 0.0
        val id = "ADV-NEW-${(form.purpose.hashCode() and 0x7FFF_FFFF) % 9000 + 1000}"
        val autoApproved = amount < 10_000.0
        _formState.update { it.copy(submitted = true, submittedId = id, autoApproved = autoApproved) }
    }

    fun resetForm() { _formState.value = AdvanceFormState() }

    fun getCardById(id: String) = repository.getCardById(id)
    fun getTransactionsForCard(cardId: String) = repository.getTransactionsForCard(cardId)

    fun toggleCardBlock(cardId: String) {
        _cardsState.update { state ->
            state.copy(
                cards = state.cards.map { card ->
                    if (card.id == cardId) {
                        val newStatus = if (card.status == com.miletracker.feature.profile.model.CardStatus.ACTIVE)
                            com.miletracker.feature.profile.model.CardStatus.BLOCKED
                        else com.miletracker.feature.profile.model.CardStatus.ACTIVE
                        card.copy(status = newStatus)
                    } else card
                },
                snackbarMessage = "Card ${cardId.takeLast(4)} status updated"
            )
        }
    }

    fun clearSnackbar() = _cardsState.update { it.copy(snackbarMessage = null) }
}
