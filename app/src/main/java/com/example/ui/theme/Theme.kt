package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintOnSecondaryContainer,
    tertiary = SageTertiary,
    onTertiary = SageOnTertiary,
    background = FocusBlack,
    onBackground = TextPrimary,
    surface = FocusSurface,
    onSurface = TextPrimary,
    surfaceVariant = FocusSurfaceContainer,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = FocusSurfaceContainer,
    surfaceContainerHigh = FocusSurfaceContainerHigh,
    outline = BorderEmeraldSubtle,
    outlineVariant = Color(0xFF152C20),
    error = DangerRed,
    onError = Color(0xFF600000),
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerOnContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Always enforce premium dark green & black aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
