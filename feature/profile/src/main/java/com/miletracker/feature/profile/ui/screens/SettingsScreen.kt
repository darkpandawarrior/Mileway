package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miletracker.core.ui.components.topbar.DepthAwareTopBar
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
    val systemDark = isSystemInDarkTheme()
    val about = SettingsUiState(darkThemeOverride = darkOverride, useMiles = useMiles)

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
