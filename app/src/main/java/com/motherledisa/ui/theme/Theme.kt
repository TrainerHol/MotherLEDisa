package com.motherledisa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * MotherLEDisa color scheme from UI-SPEC.md.
 * Dark-only theme per design specification.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    secondary = SecondaryAccent,
    background = Background,
    surface = Surface,
    onBackground = OnSurfacePrimary,
    onSurface = OnSurfacePrimary,
    onSurfaceVariant = OnSurfaceSecondary,
    error = Destructive
)

/**
 * MotherLEDisa Material 3 theme.
 * Dark-only theme optimized for LED tower control app.
 */
@Composable
fun MotherLEDisaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
