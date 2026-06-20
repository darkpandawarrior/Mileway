package com.miletracker.feature.cards.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.common.asString
import com.miletracker.core.ui.mvi.ScreenStateContent
import com.miletracker.feature.cards.model.CardModel
import com.miletracker.feature.cards.model.CardShippingAddress
import com.miletracker.feature.cards.model.CardStatus
import com.miletracker.feature.cards.model.CardTransactionModel
import com.miletracker.feature.cards.model.CardTxnClaimStatus
import com.miletracker.feature.cards.ui.components.CardAccent
import com.miletracker.feature.cards.ui.components.CardFace
import com.miletracker.feature.cards.ui.components.formatMoney
import com.miletracker.feature.cards.viewmodel.CardDetailAction
import com.miletracker.feature.cards.viewmodel.CardDetailEffect
import com.miletracker.feature.cards.viewmodel.CardDetailUiState
import com.miletracker.feature.cards.viewmodel.CardDetailViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardDetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cardId) { viewModel.onAction(CardDetailAction.Load(cardId)) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CardDetailEffect.ShowToast -> scope.launch { snackbarHostState.showSnackbar(effect.message.asString()) }
            }
        }
    }

    CardDetailContent(state, snackbarHostState, viewModel::onAction, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardDetailContent(
    state: CardDetailUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (CardDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScreenStateContent(
            state = state.card,
            modifier = Modifier.padding(padding),
        ) { card ->
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CardFace(card)
                BalanceHeader(card)
                if (card.isKycPending) KycPendingBanner(onResend = { onAction(CardDetailAction.ResendKyc) })
                CardControls(card, onAction)
                HorizontalDivider()
                Text("Transactions", style = MaterialTheme.typography.titleMedium)
                ClaimTabs(state.claimTab, onSelect = { onAction(CardDetailAction.SelectClaimTab(it)) })
                ScreenStateContent(state = state.transactions, modifier = Modifier.fillMaxWidth()) { txns ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        txns.forEach { TransactionRow(it) }
                    }
                }
            }
        }
    }

    if (state.showMonthlyLimitDialog) {
        MonthlyLimitDialog(
            onConfirm = { onAction(CardDetailAction.SetMonthlyLimit(it)) },
            onDismiss = { onAction(CardDetailAction.DismissMonthlyLimit) },
        )
    }
    if (state.showPhysicalCardDialog) {
        PhysicalCardDialog(
            onConfirm = { onAction(CardDetailAction.IssuePhysicalCard(it)) },
            onDismiss = { onAction(CardDetailAction.DismissPhysicalCard) },
        )
    }
}

@Composable
private fun BalanceHeader(card: CardModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(card.cardHolderName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            card.employeeEmail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Available Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatMoney(card.balance, card.currency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun KycPendingBanner(onResend: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("KYC Pending", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "KYC is pending for your account. We've emailed you a KYC link; tap below to resend it.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(onClick = onResend) { Text("Send KYC Link") }
        }
    }
}

@Composable
private fun CardControls(
    card: CardModel,
    onAction: (CardDetailAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = { onAction(CardDetailAction.ToggleBlock) }, modifier = Modifier.weight(1f)) {
            Text(if (card.status == CardStatus.BLOCKED) "Unblock" else "Block")
        }
        OutlinedButton(onClick = { onAction(CardDetailAction.ToggleFreeze) }, modifier = Modifier.weight(1f)) {
            Text(if (card.isFrozen) "Unfreeze" else "Freeze")
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = { onAction(CardDetailAction.OpenMonthlyLimit) }, modifier = Modifier.weight(1f)) {
            Text("Set Monthly Limit")
        }
        if (card.status != CardStatus.PHYSICAL_ISSUED) {
            OutlinedButton(onClick = { onAction(CardDetailAction.OpenPhysicalCard) }, modifier = Modifier.weight(1f)) {
                Text("Issue Physical Card")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimTabs(
    selected: CardTxnClaimStatus,
    onSelect: (CardTxnClaimStatus) -> Unit,
) {
    val entries = CardTxnClaimStatus.entries
    ScrollableTabRow(
        selectedTabIndex = entries.indexOf(selected),
        edgePadding = 0.dp,
    ) {
        entries.forEach { status ->
            Tab(
                selected = status == selected,
                onClick = { onSelect(status) },
                text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}

@Composable
private fun TransactionRow(txn: CardTransactionModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(txn.merchantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(txn.txnNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(formatMoney(txn.amount, txn.currency), style = MaterialTheme.typography.bodyMedium, color = CardAccent)
    }
}

@Composable
private fun MonthlyLimitDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Limit") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { ch -> ch.isDigit() } },
                label = { Text("Monthly Limit") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { value.toDoubleOrNull()?.let(onConfirm) },
                enabled = value.toDoubleOrNull() != null,
            ) { Text("Set Limit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PhysicalCardDialog(
    onConfirm: (CardShippingAddress) -> Unit,
    onDismiss: () -> Unit,
) {
    var address by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    val valid = address.isNotBlank() && city.isNotBlank() && pincode.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Address Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter address details to ship your physical card.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true)
                OutlinedTextField(locality, { locality = it }, label = { Text("Locality") }, singleLine = true)
                OutlinedTextField(city, { city = it }, label = { Text("City") }, singleLine = true)
                OutlinedTextField(state, { state = it }, label = { Text("State") }, singleLine = true)
                OutlinedTextField(pincode, { pincode = it }, label = { Text("Pincode") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(CardShippingAddress(address, locality, city, state, pincode)) },
                enabled = valid,
            ) { Text("Ship Card") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
