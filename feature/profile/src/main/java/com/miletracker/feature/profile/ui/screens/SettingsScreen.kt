package com.miletracker.feature.profile.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
import com.miletracker.core.ui.theme.AccentPalette
import com.miletracker.core.ui.theme.AppLanguage
import com.miletracker.core.ui.theme.DesignTokens
import com.miletracker.core.ui.theme.DesignTokens.NavigationDepth
import com.miletracker.feature.profile.model.SettingsUiState
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDebugMenu: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val darkOverride by viewModel.darkThemeOverride.collectAsStateWithLifecycle()
    val useMiles by viewModel.useMiles.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val profile by viewModel.uiState.collectAsStateWithLifecycle()
    val accentPalette by viewModel.accentPalette.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val experimentalFlags by viewModel.experimentalFlags.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val about = SettingsUiState(darkThemeOverride = darkOverride, useMiles = useMiles)

    var showPalettePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Settings",
                depth = NavigationDepth.LEVEL_1,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionLabel("Account")
            val header = profile.header
            ListItem(
                headlineContent = { Text(header.name) },
                supportingContent = { Text(header.email) },
            )
            if (header.code.isNotBlank()) {
                ListItem(
                    headlineContent = { Text("Employee code") },
                    supportingContent = { Text(header.code) },
                )
            }
            if (header.tenant.isNotBlank()) {
                ListItem(
                    headlineContent = { Text("Organization") },
                    supportingContent = { Text(header.tenant) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            SettingsSectionLabel("Preferences")
            ListItem(
                headlineContent = { Text("Dark theme") },
                supportingContent = { Text("Override the system theme") },
                trailingContent = {
                    Switch(
                        checked = darkOverride ?: systemDark,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("Distance units") },
                supportingContent = { Text(if (useMiles) "Miles" else "Kilometers") },
                trailingContent = {
                    Switch(
                        checked = useMiles,
                        onCheckedChange = { viewModel.toggleUnits() },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text(if (notificationsEnabled) "Trip reminders on" else "Off") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            // ----------------------------------------------------------------
            // Customization section
            // ----------------------------------------------------------------
            SettingsSectionLabel("Customization")

            // Accent palette
            ListItem(
                headlineContent = { Text("Accent palette") },
                supportingContent = { Text(accentPalette.label) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.clickable { showPalettePicker = true },
            )

            // Language / locale
            ListItem(
                headlineContent = { Text("Language") },
                supportingContent = { Text(language.displayName) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.clickable { showLanguagePicker = true },
            )

            // Map provider (OSM only — multi-provider toggle not meaningful)
            ListItem(
                headlineContent = { Text("Map provider") },
                supportingContent = { Text("OpenStreetMap (fixed — single provider build)") },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            // Experimental toggles
            SettingsSectionLabel("Experimental")

            ListItem(
                headlineContent = { Text("Battery-aware tracking") },
                supportingContent = { Text("Reduce GPS polling below 15% battery (real effect)") },
                trailingContent = {
                    Switch(
                        checked = experimentalFlags.batteryAwareTracking,
                        onCheckedChange = { viewModel.toggleBatteryAwareTracking() },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("Low-end device tuning") },
                supportingContent = { Text("Fewer UI animations (cosmetic in demo)") },
                trailingContent = {
                    Switch(
                        checked = experimentalFlags.lowEndDeviceTuning,
                        onCheckedChange = { viewModel.toggleLowEndDeviceTuning() },
                    )
                },
            )
            ListItem(
                headlineContent = { Text("Aggressive GPS filter") },
                supportingContent = { Text("Tighter spike rejection radius — 40 m vs 80 m (real effect)") },
                trailingContent = {
                    Switch(
                        checked = experimentalFlags.aggressiveGpsFilter,
                        onCheckedChange = { viewModel.toggleAggressiveGpsFilter() },
                    )
                },
            )

            // Reset customization
            ListItem(
                headlineContent = { Text("Reset customization") },
                supportingContent = { Text("Restore palette, language, and experimental flags to defaults") },
                modifier = Modifier.clickable { showResetConfirm = true },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            SettingsSectionLabel("About")
            ListItem(
                headlineContent = { Text("App version") },
                supportingContent = { Text(about.appVersion) },
            )
            Text(
                text = "Demo build — mock data only, no network calls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.l,
                    vertical = DesignTokens.Spacing.s,
                ),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            SettingsSectionLabel("Developer")
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                headlineContent = { Text("Developer options") },
                supportingContent = { Text("Debug flags, profiles, export tools") },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.clickable(onClick = onOpenDebugMenu),
            )
        }
    }

    // -------------------------------------------------------------------------
    // Dialogs
    // -------------------------------------------------------------------------

    if (showPalettePicker) {
        SimpleSelectionDialog(
            title = "Accent palette",
            options = AccentPalette.entries.map { it.label },
            selected = accentPalette.label,
            onDismiss = { showPalettePicker = false },
            onSelect = { label ->
                showPalettePicker = false
                AccentPalette.entries.firstOrNull { it.label == label }?.let { viewModel.setPalette(it) }
            },
        )
    }

    if (showLanguagePicker) {
        SimpleSelectionDialog(
            title = "Language",
            options = AppLanguage.entries.map { it.displayName },
            selected = language.displayName,
            onDismiss = { showLanguagePicker = false },
            onSelect = { displayName ->
                showLanguagePicker = false
                val picked = AppLanguage.entries.firstOrNull { it.displayName == displayName }
                if (picked != null) {
                    viewModel.setLanguage(picked)
                    // Wire per-app locale via AppCompatDelegate — persisted by the platform.
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(picked.tag)
                    )
                }
            },
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset customization") },
            text = { Text("Palette, language, and experimental flags will return to defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    viewModel.resetCustomization()
                    // Also reset locale to English
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(AppLanguage.ENGLISH.tag)
                    )
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SimpleSelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    TextButton(
                        onClick = { onSelect(option) },
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.xs),
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            style = if (isSelected) MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ) else MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = DesignTokens.Spacing.l,
            vertical = DesignTokens.Spacing.s,
        ),
    )
}
