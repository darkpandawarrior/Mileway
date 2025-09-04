package com.miletracker.core.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1565C0)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD6E4FF)
val OnPrimaryContainer = Color(0xFF001B4F)

val Secondary = Color(0xFF00897B)
val OnSecondary = Color(0xFFFFFFFF)

val Surface = Color(0xFFF8F9FA)
val OnSurface = Color(0xFF1A1C1E)
val SurfaceVariant = Color(0xFFE3E8EF)
val OnSurfaceVariant = Color(0xFF41474D)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)

val Outline = Color(0xFF72787E)
val Background = Color(0xFFF8F9FA)
val OnBackground = Color(0xFF1A1C1E)

// Status colours
val StatusGreen = Color(0xFF2E7D32)
val StatusAmber = Color(0xFFF57F17)
val StatusRed = Color(0xFFB71C1C)
val StatusBlue = Color(0xFF1565C0)

// Track map colours
val TrackPolyline = Color(0xFF1565C0)
val TrackStart = Color(0xFF2E7D32)
val TrackEnd = Color(0xFFBA1A1A)
val TrackPause = Color(0xFFF57F17)

// ---------------------------------------------------------------------------
// Accent palette token sets
// Each palette defines a light-mode primary/secondary/primaryContainer triple.
// The dark variants are derived by MileTrackerTheme using the same relative
// hue shift that the Default dark scheme uses.
// ---------------------------------------------------------------------------

/** Light-mode color tokens for each selectable accent palette. */
data class PaletteColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    // dark-mode equivalents
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
    val secondaryDark: Color,
    val onSecondaryDark: Color,
)

val PaletteDefault = PaletteColors(
    primary = Color(0xFF1565C0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B4F),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFF9ABEFF),
    onPrimaryDark = Color(0xFF00317E),
    primaryContainerDark = Color(0xFF0047B0),
    onPrimaryContainerDark = Color(0xFFD6E4FF),
    secondaryDark = Color(0xFF80CBC4),
    onSecondaryDark = Color(0xFF00403B),
)

val PaletteTeal = PaletteColors(
    primary = Color(0xFF00695C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF006064),
    onSecondary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFF80CBC4),
    onPrimaryDark = Color(0xFF00352F),
    primaryContainerDark = Color(0xFF004D45),
    onPrimaryContainerDark = Color(0xFFB2DFDB),
    secondaryDark = Color(0xFF80DEEA),
    onSecondaryDark = Color(0xFF002F31),
)

val PaletteIndigo = PaletteColors(
    primary = Color(0xFF283593),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE0FF),
    onPrimaryContainer = Color(0xFF000C56),
    secondary = Color(0xFF5C6BC0),
    onSecondary = Color(0xFFFFFFFF),
    primaryDark = Color(0xFFBEC6FF),
    onPrimaryDark = Color(0xFF001B78),
    primaryContainerDark = Color(0xFF3649A0),
    onPrimaryContainerDark = Color(0xFFDDE0FF),
    secondaryDark = Color(0xFFC0C7FF),
    onSecondaryDark = Color(0xFF2A3280),
)

val PaletteAmber = PaletteColors(
    primary = Color(0xFFE65100),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4A1700),
    secondary = Color(0xFFF9A825),
    onSecondary = Color(0xFF000000),
    primaryDark = Color(0xFFFFB77C),
    onPrimaryDark = Color(0xFF5C1A00),
    primaryContainerDark = Color(0xFFA83B00),
    onPrimaryContainerDark = Color(0xFFFFE0B2),
    secondaryDark = Color(0xFFFFD54F),
    onSecondaryDark = Color(0xFF3E2A00),
)
