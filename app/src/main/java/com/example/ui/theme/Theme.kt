package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekBluePrimary,
    secondary = SleekBlueSecondary,
    tertiary = AmberAccent,
    background = SleekDarkBg,
    surface = SleekDarkSurface,
    onPrimary = SleekDarkTextPrimary,
    onSecondary = SleekDarkTextPrimary,
    onTertiary = SleekDarkTextPrimary,
    onBackground = SleekDarkTextPrimary,
    onSurface = SleekDarkTextPrimary,
    outline = SleekDarkBorder,
    surfaceVariant = Color(0xFF1A2235),
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = SleekBluePrimary,
    secondary = SleekBlueSecondary,
    tertiary = SleekBlueContainer,
    background = SleekLightBg,
    surface = SleekLightSurface,
    onPrimary = SleekLightSurface,
    onSecondary = SleekLightSurface,
    onBackground = SleekTextPrimary,
    onSurface = SleekTextPrimary,
    outline = SleekBorderLight,
    outlineVariant = SleekBorderDashed,
    surfaceVariant = SleekSurfaceVariant,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
