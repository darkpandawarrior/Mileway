package com.miletracker.core.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide theme override holder.
 *
 * [MileTrackerTheme] defaults to following the system setting; this controller lets the
 * Settings screen override it at runtime. A `null` value means "follow the system".
 * Held as a Koin singleton so the shell (reader) and Settings (writer) share one instance.
 */
class ThemeController {
    private val _darkThemeOverride = MutableStateFlow<Boolean?>(null)

    /** `null` = follow system, `true` = force dark, `false` = force light. */
    val darkThemeOverride: StateFlow<Boolean?> = _darkThemeOverride.asStateFlow()

    fun set(dark: Boolean?) {
        _darkThemeOverride.value = dark
    }

    /** Flip between dark and light, resolving the current effective value against [systemDark]. */
    fun toggle(systemDark: Boolean) {
        val current = _darkThemeOverride.value ?: systemDark
        _darkThemeOverride.value = !current
    }
}
