package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val PulseColorScheme = ColorScheme(
    primary = Orange500,
    onPrimary = Color.Black,
    primaryContainer = Orange500.copy(alpha = 0.3f),
    onPrimaryContainer = Orange500,
    secondary = Slate400,
    onSecondary = Color.Black,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate300,
    background = Color.Black,
    onBackground = Color.White,
    surfaceContainer = Slate900,
    onSurface = Color.White,
    onSurfaceVariant = Slate400,
    error = Color(0xFFEF4444),
    onError = Color.White,
)

@Composable
fun PulseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PulseColorScheme,
        typography = Typography,
        content = content
    )
}
