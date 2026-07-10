package com.suspended.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val SuspendedShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuspendedTheme(
    appTheme: AppTheme = AppTheme.SPOTIFY_DARK,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (appTheme) {
        AppTheme.SPOTIFY_DARK -> DarkColorScheme
        AppTheme.MIDNIGHT_BLUE -> MidnightBlueScheme
        AppTheme.CRIMSON -> CrimsonScheme
        AppTheme.DYNAMIC -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicDarkColorScheme(context).copy(
                background = SpotifyBlack,
                surface = SpotifyDarkGray,
                surfaceVariant = SpotifyMediumGray,
                onBackground = SpotifyWhite,
                onSurface = SpotifyWhite
            ) else DarkColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = SuspendedTypography,
        shapes = SuspendedShapes,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
