package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkCanvas,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldBright,
    secondary = AccentEmerald,
    onSecondary = TextPrimary,
    background = DarkCanvas,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun AthkarTheme(
    content: @Composable () -> Unit
) {
    // Provide RTL (Right-To-Left) layout direction for Arabic language
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
