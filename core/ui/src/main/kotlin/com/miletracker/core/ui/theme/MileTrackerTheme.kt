package com.miletracker.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private fun Color(value: Long) = androidx.compose.ui.graphics.Color(value)

// Dark-mode neutral tokens (independent of accent palette)
private val DarkSurface = Color(0xFF1A1C1E)
private val DarkOnSurface = Color(0xFFE3E2E6)
private val DarkSurfaceVariant = Color(0xFF41474D)
private val DarkOnSurfaceVariant = Color(0xFFC1C7CE)
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkOutline = Color(0xFF8B9198)

/** Build a light color scheme from the given [PaletteColors]. */
private fun lightSchemeFor(p: PaletteColors) = lightColorScheme(
    primary = p.primary,
    onPrimary = p.onPrimary,
    primaryContainer = p.primaryContainer,
    onPrimaryContainer = p.onPrimaryContainer,
    secondary = p.secondary,
    onSecondary = p.onSecondary,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnError,
    outline = Outline,
    background = Background,
    onBackground = OnBackground,
)

/** Build a dark color scheme from the given [PaletteColors]. */
private fun darkSchemeFor(p: PaletteColors) = darkColorScheme(
    primary = p.primaryDark,
    onPrimary = p.onPrimaryDark,
    primaryContainer = p.primaryContainerDark,
    onPrimaryContainer = p.onPrimaryContainerDark,
    secondary = p.secondaryDark,
    onSecondary = p.onSecondaryDark,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    outline = DarkOutline,
    background = DarkSurface,
    onBackground = DarkOnSurface,
)

@Composable
fun MileTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AccentPalette = AccentPalette.DEFAULT,
    content: @Composable () -> Unit
) {
    val paletteColors = palette.colors()
    val colorScheme = if (darkTheme) darkSchemeFor(paletteColors) else lightSchemeFor(paletteColors)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MileTrackerTypography,
        content = content,
    )
}
