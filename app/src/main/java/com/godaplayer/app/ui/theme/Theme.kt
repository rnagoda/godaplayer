package com.godaplayer.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GodaDarkColorScheme = darkColorScheme(
    primary = GodaColors.PrimaryAccent,
    onPrimary = GodaColors.PrimaryBackground,
    primaryContainer = GodaColors.SecondaryAccent,
    onPrimaryContainer = GodaColors.PrimaryText,

    secondary = GodaColors.SecondaryAccent,
    onSecondary = GodaColors.PrimaryBackground,
    secondaryContainer = GodaColors.TertiaryBackground,
    onSecondaryContainer = GodaColors.PrimaryText,

    tertiary = GodaColors.PrimaryAccent,
    onTertiary = GodaColors.PrimaryBackground,

    background = GodaColors.PrimaryBackground,
    onBackground = GodaColors.PrimaryText,

    surface = GodaColors.SecondaryBackground,
    onSurface = GodaColors.PrimaryText,
    surfaceVariant = GodaColors.TertiaryBackground,
    onSurfaceVariant = GodaColors.SecondaryText,

    error = GodaColors.Error,
    onError = GodaColors.PrimaryBackground,

    outline = GodaColors.DividerColor,
    outlineVariant = GodaColors.DisabledText,

    inverseSurface = GodaColors.PrimaryText,
    inverseOnSurface = GodaColors.PrimaryBackground,
    inversePrimary = GodaColors.SecondaryAccent
)

@Composable
fun GodaPlayerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = GodaDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GodaTypography,
        content = content
    )
}
