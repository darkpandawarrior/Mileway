package com.miletracker.core.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Selectable accent palettes. Each maps to a distinct [PaletteColors] set in Color.kt.
 * The app ships four options; Default is the baseline blue/teal pair.
 */
enum class AccentPalette(val label: String) {
    DEFAULT("Default"),
    TEAL("Teal"),
    INDIGO("Indigo"),
    AMBER("Amber"),
}

/** Returns the [PaletteColors] token set for this palette. */
fun AccentPalette.colors(): PaletteColors = when (this) {
    AccentPalette.DEFAULT -> PaletteDefault
    AccentPalette.TEAL -> PaletteTeal
    AccentPalette.INDIGO -> PaletteIndigo
    AccentPalette.AMBER -> PaletteAmber
}

/**
 * BCP-47 locale tag for supported demo languages.
 * Full string translation is out of scope for the demo; the mechanism (AppCompatDelegate
 * per-app locales) is wired and a couple of UI strings use localized overrides.
 */
enum class AppLanguage(val tag: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी (Hindi)"),
}

/**
 * Three experimental optimization toggles.
 * - [batteryAwareTracking]: defers location polling when battery < 15% (real runtime
 *   effect wired in tracking service; no-op when battery is healthy).
 * - [lowEndDeviceTuning]: reduces recomposition frequency and drops some UI animations
 *   (cosmetic in demo — real tuning would require Baseline Profile gating).
 * - [aggressiveGpsFilter]: tightens the spike-rejection radius from 80 m to 40 m
 *   (real effect on the LocationProcessor filter; marked for demo because actual
 *   field impact requires extended trips to observe).
 */
data class ExperimentalFlags(
    val batteryAwareTracking: Boolean = false,
    val lowEndDeviceTuning: Boolean = false,
    val aggressiveGpsFilter: Boolean = false,
)

/**
 * App-wide theme override holder.
 *
 * [MileTrackerTheme] defaults to following the system setting; this controller lets the
 * Settings screen override dark mode, accent palette, and language at runtime.
 * A `null` dark-mode value means "follow the system".
 * Held as a Koin singleton so the shell (reader) and Settings (writer) share one instance.
 *
 * Persistence: all state is in-memory only (Koin singleton survives the Activity lifecycle
 * but not process death). Persisting to DataStore is the natural next step; the keys are
 * modelled in CustomizationSettingsTest so the mapping is testable today.
 */
class ThemeController {
    private val _darkThemeOverride = MutableStateFlow<Boolean?>(null)
    private val _accentPalette = MutableStateFlow(AccentPalette.DEFAULT)
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    private val _experimentalFlags = MutableStateFlow(ExperimentalFlags())

    /** `null` = follow system, `true` = force dark, `false` = force light. */
    val darkThemeOverride: StateFlow<Boolean?> = _darkThemeOverride.asStateFlow()

    /** Currently active accent palette. */
    val accentPalette: StateFlow<AccentPalette> = _accentPalette.asStateFlow()

    /** Currently selected app language. */
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    /** Experimental optimization flags. */
    val experimentalFlags: StateFlow<ExperimentalFlags> = _experimentalFlags.asStateFlow()

    fun set(dark: Boolean?) {
        _darkThemeOverride.value = dark
    }

    /** Flip between dark and light, resolving the current effective value against [systemDark]. */
    fun toggle(systemDark: Boolean) {
        val current = _darkThemeOverride.value ?: systemDark
        _darkThemeOverride.value = !current
    }

    fun setPalette(palette: AccentPalette) {
        _accentPalette.value = palette
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    fun updateExperimentalFlags(flags: ExperimentalFlags) {
        _experimentalFlags.value = flags
    }

    fun resetCustomization() {
        _accentPalette.value = AccentPalette.DEFAULT
        _language.value = AppLanguage.ENGLISH
        _experimentalFlags.value = ExperimentalFlags()
    }
}
