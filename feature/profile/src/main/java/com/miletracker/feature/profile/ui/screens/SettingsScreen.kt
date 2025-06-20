package com.miletracker.feature.profile.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
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
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val darkOverride by viewModel.darkThemeOverride.collectAsStateWithLifecycle()
    val useMiles by viewModel.useMiles.collectAsStateWithLifecycle()
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
                .padding(innerPadding),
        ) {
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

            HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.s))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.l,
                    vertical = DesignTokens.Spacing.s,
                ),
            )
            ListItem(
                headlineContent = { Text("App version") },
                supportingContent = { Text(about.appVersion) },
            )
            Text(
                text = "Demo build — all data is mocked",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.l,
                    vertical = DesignTokens.Spacing.s,
                ),
            )
        }
    }
}
