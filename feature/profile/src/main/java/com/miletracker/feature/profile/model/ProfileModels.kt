package com.miletracker.feature.profile.model

import androidx.compose.ui.graphics.vector.ImageVector

/** Header shown at the top of the profile screen. */
data class ProfileHeader(
    val name: String,
    val email: String,
    val code: String,
    val tenant: String,
    val initials: String,
)

/** A single tappable settings entry rendered as a card on the profile screen. */
data class SettingsTile(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

data class ProfileUiState(
    val header: ProfileHeader,
    val tiles: List<SettingsTile> = emptyList(),
)

data class SettingsUiState(
    val darkThemeOverride: Boolean?,
    val useMiles: Boolean = true,
    val appVersion: String = "1.0.0 (demo)",
)
