package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.feature.profile.model.CardStatus
import com.miletracker.feature.profile.model.CardType
import com.miletracker.feature.profile.model.CorporateCard
import com.miletracker.feature.profile.viewmodel.AdvanceViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsHomeScreen(
    onBack: () -> Unit,
    onOpenCard: (String) -> Unit,
    onRequestCard: () -> Unit,
    onOpenQr: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AdvanceViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRequestCard,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Request Card") },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Box(
                modifier =
                    Modifier
                        .background(Brush.horizontalGradient(listOf(Color(0xFF880E4F), Color(0xFFE91E63))))
                        .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Corporate Cards", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "${state.cards.size} card${if (state.cards.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                    IconButton(onClick = onOpenQr) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Pay", tint = Color.White)
                    }
                }
            }

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        horizontal = DesignTokens.Spacing.l,
                        vertical = DesignTokens.Spacing.l,
                    ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l),
            ) {
                items(state.cards, key = { it.id }) { card ->
                    CardItem(
                        card = card,
                        onClick = { onOpenCard(card.id) },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CardItem(
    card: CorporateCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardGradient =
        when (card.cardType) {
            CardType.VISA -> Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1)))
            CardType.MASTERCARD -> Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF880E4F)))
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(cardGradient)
                    .padding(20.dp),
        ) {
            if (card.status == CardStatus.BLOCKED) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = Color.Red.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Filled.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("BLOCKED", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Text(
                        text = card.cardType.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column {
                    Text(
                        text = "•••• •••• •••• ${card.lastFourDigits}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("CARD HOLDER", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            Text(card.holderName, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            Text(card.expiryDate, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
