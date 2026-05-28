package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.profile.model.CardStatus
import com.miletracker.feature.profile.model.CardTransaction
import com.miletracker.feature.profile.model.CorporateCard
import com.miletracker.feature.profile.viewmodel.AdvanceViewModel
import com.miletracker.core.common.formatDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdvanceViewModel = koinViewModel()
) {
    val state by viewModel.cardsState.collectAsState()
    val card = state.cards.find { it.id == cardId }
    val transactions = remember(cardId) { viewModel.getTransactionsForCard(cardId) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Card Details",
                subtitle = card?.let { "•••• ${it.lastFourDigits}" },
                depth = NavigationDepth.LEVEL_2,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (card == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Card not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = DesignTokens.Spacing.l,
                    vertical = DesignTokens.Spacing.l
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
            ) {
                item {
                    CardItem(card = card, onClick = {})
                }
                item {
                    // Balance card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = DesignTokens.Shape.roundedMd,
                        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Available Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${card.balanceRupees.formatDecimal(2)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Credit Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${card.creditLimitRupees.toLong()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
                    ) {
                        OutlinedButton(
                            onClick = { showBlockDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (card.status == CardStatus.ACTIVE) Icons.Filled.Block else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.size(DesignTokens.Spacing.s))
                            Text(if (card.status == CardStatus.ACTIVE) "Block Card" else "Unblock Card")
                        }
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(DesignTokens.Spacing.s))
                            Text("View PIN")
                        }
                    }
                }
                item {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(transactions, key = { it.id }) { txn ->
                    TransactionRow(txn = txn)
                }
                item { Spacer(Modifier.height(DesignTokens.Spacing.l)) }
            }
        }
    }

    if (showBlockDialog && card != null) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(if (card.status == CardStatus.ACTIVE) "Block Card?" else "Unblock Card?") },
            text = {
                Text(
                    if (card.status == CardStatus.ACTIVE)
                        "This card will be temporarily blocked. All future transactions will be declined."
                    else
                        "This card will be reactivated for use."
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.toggleCardBlock(cardId)
                    showBlockDialog = false
                }) {
                    Text(if (card.status == CardStatus.ACTIVE) "Block" else "Unblock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Card PIN") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("••••", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("For demo purposes, the PIN is not shown.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun TransactionRow(txn: CardTransaction) {
    val MONTHS = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = DesignTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)
    ) {
        Box(
            modifier = Modifier.size(36.dp).let {
                it
            },
            contentAlignment = Alignment.Center
        ) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(txn.category.firstOrNull()?.toString() ?: "?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.merchantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("${txn.category} · ${Instant.fromEpochMilliseconds(txn.dateMs).toLocalDateTime(TimeZone.currentSystemDefault()).let { ldt -> "${ldt.dayOfMonth} ${MONTHS[ldt.monthNumber - 1]}" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("₹${txn.amountRupees.toLong()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
