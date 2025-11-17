package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miletracker.core.ui.theme.DesignTokens

private data class DelegationEntry(
    val id: String,
    val name: String,
    val scope: String,
    val expires: String,
    val isActive: Boolean,
)

private data class DelegatedByEntry(
    val id: String,
    val delegatorName: String,
    val scope: String,
    val until: String,
)

private val MY_DELEGATIONS = listOf(
    DelegationEntry("D001", "Priya Sharma", "Mileage & Expense", "30 Jun 2026", true),
    DelegationEntry("D002", "Rahul Mehra", "All categories", "15 Jul 2026", true),
    DelegationEntry("D003", "Kavitha Rao", "Travel only", "01 Jun 2026", false),
)

private val DELEGATED_TO_ME = listOf(
    DelegatedByEntry("DB001", "Vikram Nair", "Travel", "Returning 20 Jun 2026"),
    DelegatedByEntry("DB002", "Sunita Pillai", "Expense & Advance", "Returning 25 Jun 2026"),
)

@Composable
fun DelegationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val delegationStates = remember {
        mutableStateOf(MY_DELEGATIONS.associate { it.id to it.isActive })
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF7B1FA2))))
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delegation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Manage who acts on your behalf", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add delegation", tint = Color.White)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(DesignTokens.Spacing.l),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l),
            ) {
                item {
                    SectionHeader(icon = Icons.Default.PersonAddAlt, title = "My Delegations", subtitle = "People you've authorised to act for you")
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = DesignTokens.Shape.roundedMd,
                        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
                    ) {
                        Column {
                            MY_DELEGATIONS.forEachIndexed { index, entry ->
                                val active = delegationStates.value[entry.id] ?: entry.isActive
                                DelegationRow(
                                    entry = entry,
                                    isActive = active,
                                    onToggle = {
                                        delegationStates.value = delegationStates.value.toMutableMap().also { map ->
                                            map[entry.id] = !active
                                        }
                                    },
                                )
                                if (index < MY_DELEGATIONS.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(DesignTokens.Spacing.s)) }

                item {
                    SectionHeader(icon = Icons.Default.Group, title = "Delegated To Me", subtitle = "You're covering for these people")
                }
                item {
                    if (DELEGATED_TO_ME.isEmpty()) {
                        Text(
                            "No active incoming delegations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = DesignTokens.Shape.roundedMd,
                            elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
                        ) {
                            Column {
                                DELEGATED_TO_ME.forEachIndexed { index, entry ->
                                    DelegatedByRow(entry = entry)
                                    if (index < DELEGATED_TO_ME.lastIndex) {
                                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(DesignTokens.Spacing.s))
                        Text("Add New Delegation")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDelegationDialog(onDismiss = { showAddDialog = false })
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DelegationRow(
    entry: DelegationEntry,
    isActive: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                entry.name.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.width(DesignTokens.Spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(entry.scope, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Expires ${entry.expires}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = isActive, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun DelegatedByRow(entry: DelegatedByEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.l, vertical = DesignTokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF6A1B9A).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                entry.delegatorName.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A),
            )
        }
        Spacer(Modifier.width(DesignTokens.Spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.delegatorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(entry.scope, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(entry.until, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
        ) {
            Text(
                "Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun AddDelegationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delegation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m)) {
                Text(
                    "In the full version, search for a team member and select the scope and duration of the delegation.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ) {
                    Text(
                        "This action is illustrative in the demo.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(DesignTokens.Spacing.m),
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Got it") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
