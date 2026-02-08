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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.profile.model.CardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardRequestScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf(CardType.VISA) }
    var expanded by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        CardRequestSuccessContent(
            cardType = cardType,
            onBack = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Request Corporate Card",
                subtitle = "KYC Verification",
                depth = NavigationDepth.LEVEL_2,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.l)
                ) {
                    Button(
                        onClick = { submitted = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = fullName.isNotBlank() && address.isNotBlank()
                    ) {
                        Text("Submit Card Request")
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DesignTokens.Spacing.l)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l)
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.s))

            Text("Personal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                placeholder = { Text("As per your employee record") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Office / Mailing Address") },
                placeholder = { Text("Building, street, city, PIN") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Card Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = cardType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Card Network") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    CardType.entries.forEach { type ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                cardType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = DesignTokens.Shape.roundedMd
            ) {
                Text(
                    "Processing time: 5–7 business days after verification. A physical card will be couriered to your registered address.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(DesignTokens.Spacing.l)
                )
            }

            Spacer(Modifier.height(DesignTokens.Spacing.xxl))
        }
    }
}

@Composable
private fun CardRequestSuccessContent(
    cardType: CardType,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.HourglassBottom,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Request Submitted",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${cardType.name.lowercase().replaceFirstChar { it.uppercase() }} Corporate Card",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your request is under review. You'll receive confirmation within 5–7 business days.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Cards")
        }
    }
}
