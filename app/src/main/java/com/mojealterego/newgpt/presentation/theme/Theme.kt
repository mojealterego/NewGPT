package com.mojealterego.newgpt.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val Obsidian = Color(0xFF0B0B0D)
private val ObsidianSurface = Color(0xFF151518)
private val Gold = Color(0xFFD4AF37)
private val Ivory = Color(0xFFF4F0E6)
private val Titanium = Color(0xFFB8BBC2)
private val Burgundy = Color(0xFF5E1724)

private val NewGptDarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Obsidian,
    primaryContainer = Color(0xFF3A3010),
    onPrimaryContainer = Ivory,
    secondary = Titanium,
    onSecondary = Obsidian,
    secondaryContainer = Color(0xFF292A2F),
    onSecondaryContainer = Ivory,
    background = Obsidian,
    onBackground = Ivory,
    surface = ObsidianSurface,
    onSurface = Ivory,
    surfaceVariant = Color(0xFF222227),
    onSurfaceVariant = Color(0xFFD2D0C9),
    error = Color(0xFFE57373),
    errorContainer = Burgundy
)

private val NewGptTypography = Typography().let {
    it.copy(
        headlineLarge = it.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium),
        headlineSmall = it.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium),
        titleLarge = it.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
        bodyLarge = it.bodyLarge.copy(fontFamily = FontFamily.Serif),
        bodyMedium = it.bodyMedium.copy(fontFamily = FontFamily.Serif)
    )
}

@Composable
fun NewGptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NewGptDarkScheme,
        typography = NewGptTypography,
        content = content
    )
}
