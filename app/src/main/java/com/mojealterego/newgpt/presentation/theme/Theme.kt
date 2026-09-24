package com.mojealterego.newgpt.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val Obsidian = Color(0xFF050506)
private val ObsidianSurface = Color(0xFF101012)
private val ObsidianRaised = Color(0xFF17171A)
private val Gold24K = Color(0xFFD4AF37)
private val GoldHighlight = Color(0xFFF3D77A)
private val Ivory = Color(0xFFF5F1E7)
private val Titanium = Color(0xFFB8BBC2)
private val Burgundy = Color(0xFF5E1724)

private val NewGptDarkScheme = darkColorScheme(
    primary = Gold24K,
    onPrimary = Obsidian,
    primaryContainer = Color(0xFF3A3010),
    onPrimaryContainer = GoldHighlight,
    secondary = Titanium,
    onSecondary = Obsidian,
    secondaryContainer = ObsidianRaised,
    onSecondaryContainer = Ivory,
    tertiary = GoldHighlight,
    onTertiary = Obsidian,
    background = Obsidian,
    onBackground = Ivory,
    surface = ObsidianSurface,
    onSurface = Ivory,
    surfaceVariant = Color(0xFF222227),
    onSurfaceVariant = Color(0xFFD2D0C9),
    outline = Color(0xFF6F674E),
    error = Color(0xFFE57373),
    errorContainer = Burgundy
)

private val NewGptTypography = Typography().let {
    val serif = FontFamily.Serif
    it.copy(
        displayLarge = it.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        displayMedium = it.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        displaySmall = it.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        headlineLarge = it.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        headlineMedium = it.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        headlineSmall = it.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        titleLarge = it.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleMedium = it.titleMedium.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        titleSmall = it.titleSmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        bodyLarge = it.bodyLarge.copy(fontFamily = serif),
        bodyMedium = it.bodyMedium.copy(fontFamily = serif),
        bodySmall = it.bodySmall.copy(fontFamily = serif),
        labelLarge = it.labelLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        labelMedium = it.labelMedium.copy(fontFamily = serif),
        labelSmall = it.labelSmall.copy(fontFamily = serif)
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
