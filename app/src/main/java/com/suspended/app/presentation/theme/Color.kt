package com.suspended.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val SpotifyBlack = Color(0xFF121212)
val SpotifyDarkGray = Color(0xFF1E1E1E)
val SpotifyMediumGray = Color(0xFF282828)
val SpotifyLightGray = Color(0xFFB3B3B3)
val SpotifyWhite = Color(0xFFFFFFFF)
val SpotifyGreen = Color(0xFF1DB954)
val SpotifyGreenDark = Color(0xFF1AA34A)
val SpotifyGreenLight = Color(0xFF1ED760)
val GradientTop = Color(0xFF1A1A2E)
val GradientBottom = Color(0xFF16213E)
val ErrorRed = Color(0xFFCF6679)
val OnSurfaceVariant = Color(0xFF8A8A8A)

val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = SpotifyBlack,
    primaryContainer = SpotifyGreenDark,
    secondary = SpotifyLightGray,
    background = SpotifyBlack,
    surface = SpotifyDarkGray,
    surfaceVariant = SpotifyMediumGray,
    onBackground = SpotifyWhite,
    onSurface = SpotifyWhite,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed
)

val MidnightBlueScheme = darkColorScheme(
    primary = Color(0xFF4F86F7),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2E5CB8),
    secondary = Color(0xFFA0C0FF),
    background = Color(0xFF0D1321),
    surface = Color(0xFF161B29),
    surfaceVariant = Color(0xFF1D2438),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF90A4AE),
    error = ErrorRed
)

val CrimsonScheme = darkColorScheme(
    primary = Color(0xFFE63950),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB32338),
    secondary = Color(0xFFFFB3BC),
    background = Color(0xFF1A0E10),
    surface = Color(0xFF241417),
    surfaceVariant = Color(0xFF331D21),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFBCAAA4),
    error = ErrorRed
)
