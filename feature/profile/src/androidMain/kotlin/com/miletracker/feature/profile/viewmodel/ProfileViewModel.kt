package com.miletracker.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import com.miletracker.core.ui.theme.AccentPalette
import com.miletracker.core.ui.theme.AppLanguage
import com.miletracker.core.ui.theme.ExperimentalFlags
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.feature.profile.model.AccountAnalyticsSnapshot
import com.miletracker.feature.profile.model.ProfileUiState
import com.miletracker.feature.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single ViewModel that backs the whole account suite: the Account hub
 * ([com.miletracker.feature.profile.ui.screens.ProfileScreen]), the
 * Profile Details screen and the Preferences screen, plus the legacy Settings/Help routes.
 *
 * All data is read synchronously from [ProfileRepository] (offline mock data). The screens are
 * stateless and observe [uiState]; user intents are funnelled through the `intent*` methods.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val themeController: ThemeController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        repository.accounts().let { acc ->
            ProfileUiState(
                header = repository.header(),
                profile = repository.richProfile(),
                completion = repository.completion(),
                sessions = repository.sessions(),
                accounts = acc,
                selectedAccountId = acc.firstOrNull()?.id.orEmpty(),
                analytics = AccountAnalyticsSnapshot.demo(),
            )
        }
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun intentSwitchAccount(id: String) {
        _uiState.update { it.copy(selectedAccountId = id) }
    }

    /** `null` = follow system, `true` = force dark, `false` = force light. */
    val darkThemeOverride: StateFlow<Boolean?> = themeController.darkThemeOverride

    /** Currently selected accent palette. */
    val accentPalette: StateFlow<AccentPalette> = themeController.accentPalette

    /** Custom seed colour hex ("" = use the preset's seed). */
    val customSeedHex: StateFlow<String> = themeController.customSeedHex

    /** Wallpaper-derived dynamic colours (Android 12+). */
    val useSystemColors: StateFlow<Boolean> = themeController.useSystemColors

    /** Scheme-generation style name (TonalSpot, Vibrant, …). */
    val paletteStyle: StateFlow<String> = themeController.paletteStyle

    /** Currently selected app language. */
    val language: StateFlow<AppLanguage> = themeController.language

    /** Experimental optimization flags. */
    val experimentalFlags: StateFlow<ExperimentalFlags> = themeController.experimentalFlags

    private val _useMiles = MutableStateFlow(true)
    val useMiles: StateFlow<Boolean> = _useMiles.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleUnits() {
        _useMiles.value = !_useMiles.value
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun setDarkTheme(dark: Boolean?) = themeController.set(dark)

    fun setPalette(palette: AccentPalette) = themeController.setPalette(palette)

    fun setCustomSeed(hex: String) = themeController.setCustomSeed(hex)

    fun setUseSystemColors(enabled: Boolean) = themeController.setUseSystemColors(enabled)

    fun setPaletteStyle(style: String) = themeController.setPaletteStyle(style)

    fun setLanguage(language: AppLanguage) = themeController.setLanguage(language)

    fun toggleBatteryAwareTracking() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(batteryAwareTracking = !current.batteryAwareTracking))
    }

    fun toggleLowEndDeviceTuning() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(lowEndDeviceTuning = !current.lowEndDeviceTuning))
    }

    fun toggleAggressiveGpsFilter() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(aggressiveGpsFilter = !current.aggressiveGpsFilter))
    }

    fun resetCustomization() = themeController.resetCustomization()

    // ── Account hub: sessions dialog ──────────────────────────────────────────

    /** Opens the device-sessions list dialog on the Account hub. */
    fun intentOpenSessionsDialog() {
        _uiState.update { it.copy(showSessionsDialog = true) }
    }

    /** Dismisses the device-sessions list dialog. */
    fun intentDismissSessionsDialog() {
        _uiState.update { it.copy(showSessionsDialog = false) }
    }

    // ── Preferences screen: tonal-tile toggles ────────────────────────────────

    /** Flips the Push Notifications preference tile. */
    fun intentTogglePushNotifications() {
        _uiState.update { it.copy(preferences = it.preferences.copy(pushNotifications = !it.preferences.pushNotifications)) }
    }

    /** Flips the Usage Analytics preference tile (server-side analytics opt-in). */
    fun intentToggleUsageAnalytics() {
        _uiState.update { it.copy(preferences = it.preferences.copy(usageAnalytics = !it.preferences.usageAnalytics)) }
    }

    /**
     * Records a one-shot demo message (snackbar/toast) raised by a preference tile that, in the
     * real app, would deep-link into a system screen. The screen consumes it then calls
     * [intentClearPreferenceMessage].
     */
    fun intentRaisePreferenceMessage(message: String) {
        _uiState.update { it.copy(preferenceMessage = message) }
    }

    /** Clears the pending preference demo message once the screen has shown it. */
    fun intentClearPreferenceMessage() {
        _uiState.update { it.copy(preferenceMessage = null) }
    }
}
