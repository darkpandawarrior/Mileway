package com.miletracker.feature.profile.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.lifecycle.ViewModel
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.feature.profile.model.ProfileUiState
import com.miletracker.feature.profile.model.SettingsTile
import com.miletracker.feature.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val themeController: ThemeController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            header = repository.header(),
            tiles = listOf(
                SettingsTile(id = TILE_SETTINGS, label = "Settings", icon = Icons.Default.Settings),
                SettingsTile(id = "notifications", label = "Notifications", icon = Icons.Default.Notifications),
                SettingsTile(id = "help", label = "Help", icon = Icons.AutoMirrored.Filled.HelpOutline),
                SettingsTile(id = "about", label = "About", icon = Icons.Default.Info),
            ),
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** `null` = follow system, `true` = force dark, `false` = force light. */
    val darkThemeOverride: StateFlow<Boolean?> = themeController.darkThemeOverride

    private val _useMiles = MutableStateFlow(true)
    val useMiles: StateFlow<Boolean> = _useMiles.asStateFlow()

    fun toggleUnits() {
        _useMiles.value = !_useMiles.value
    }

    fun setDarkTheme(dark: Boolean?) = themeController.set(dark)

    companion object {
        const val TILE_SETTINGS = "settings"
    }
}
