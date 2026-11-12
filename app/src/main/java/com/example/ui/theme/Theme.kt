package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldYieldPrimary,
    onPrimary = TerminalVoid,
    primaryContainer = TerminalSurfaceVariant,
    onPrimaryContainer = EmeraldYieldBright,
    secondary = CyanCore,
    onSecondary = TerminalVoid,
    secondaryContainer = TerminalSurfaceVariant,
    onSecondaryContainer = CyanCore,
    tertiary = GoldAlert,
    onTertiary = TerminalVoid,
    background = TerminalVoid,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalCardBorder,
    error = RedRisk,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = TextPrimary,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimary,
    secondary = CyanCore,
    onSecondary = TerminalVoid,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightText,
    outline = TerminalCardBorder,
    error = RedRisk,
    onError = TextPrimary
)

@Composable
fun YieldEngineTheme(
    darkTheme: Boolean = true, // Default to deep terminal dark as confirmed
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
