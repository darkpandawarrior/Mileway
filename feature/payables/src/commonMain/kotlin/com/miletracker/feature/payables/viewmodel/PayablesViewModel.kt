package com.miletracker.feature.payables.viewmodel

import androidx.lifecycle.ViewModel
import com.miletracker.feature.payables.model.Invoice
import com.miletracker.feature.payables.model.NewLineItemDraft
import com.miletracker.feature.payables.model.PurchaseOrder
import com.miletracker.feature.payables.repository.PayablesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PayablesHomeState(
    val purchaseOrders: List<PurchaseOrder> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
)

data class CreatePoFormState(
    val step: Int = 1,
    val vendorName: String = "",
    val deliveryDate: String = "",
    val officeLocation: String = "Head Office – Pune",
    val lineItems: List<NewLineItemDraft> = listOf(NewLineItemDraft()),
    val submitted: Boolean = false,
    val submittedId: String = "",
)

class PayablesViewModel(
    private val repository: PayablesRepository,
) : ViewModel() {
    private val _homeState =
        MutableStateFlow(
            PayablesHomeState(
                purchaseOrders = repository.purchaseOrders,
                invoices = repository.invoices,
            ),
        )
    val homeState: StateFlow<PayablesHomeState> = _homeState.asStateFlow()

    private val _formState = MutableStateFlow(CreatePoFormState())
    val formState: StateFlow<CreatePoFormState> = _formState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ── Step 1 form fields ──────────────────────────────────────────────────
    fun setVendorName(name: String) = _formState.update { it.copy(vendorName = name) }

    fun setDeliveryDate(date: String) = _formState.update { it.copy(deliveryDate = date) }

    fun setOfficeLocation(loc: String) = _formState.update { it.copy(officeLocation = loc) }

    fun goToStep2() = _formState.update { it.copy(step = 2) }

    fun goToStep1() = _formState.update { it.copy(step = 1) }

    // ── Step 2 line items ───────────────────────────────────────────────────
    fun addLineItem() =
        _formState.update {
            it.copy(lineItems = it.lineItems + NewLineItemDraft())
        }

    fun removeLineItem(index: Int) =
        _formState.update {
            it.copy(lineItems = it.lineItems.toMutableList().also { list -> list.removeAt(index) })
        }

    fun updateLineItem(
        index: Int,
        item: NewLineItemDraft,
    ) = _formState.update {
        it.copy(lineItems = it.lineItems.toMutableList().also { list -> list[index] = item })
    }

    fun submitPo() {
        val form = _formState.value
        val id = "PO-NEW-${(form.vendorName.hashCode() and 0x7FFF_FFFF) % 9000 + 1000}"
        _formState.update { it.copy(submitted = true, submittedId = id) }
    }

    fun resetForm() {
        _formState.value = CreatePoFormState()
    }

    fun getPoById(id: String) = repository.getPoById(id)

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    val officeLocations =
        listOf(
            "Head Office – Pune",
            "North Branch – Mumbai",
            "South Branch – Bengaluru",
            "East Branch – Kolkata",
        )
}
