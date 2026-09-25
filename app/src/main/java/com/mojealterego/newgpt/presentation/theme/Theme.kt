package com.mojealterego.newgpt.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val NewGptDarkScheme = darkColorScheme(
    primary = BrandPalette.Gold,
    onPrimary = BrandPalette.Obsidian,
    primaryContainer = Color(0xFF3A2C08),
    onPrimaryContainer = BrandPalette.GoldBright,
    secondary = BrandPalette.Titanium,
    onSecondary = BrandPalette.Obsidian,
    secondaryContainer = BrandPalette.Leather,
    onSecondaryContainer = BrandPalette.Ivory,
    tertiary = BrandPalette.GoldBright,
    onTertiary = BrandPalette.Obsidian,
    background = BrandPalette.Obsidian,
    onBackground = BrandPalette.Ivory,
    surface = BrandPalette.AnilineBlack,
    onSurface = BrandPalette.Ivory,
    surfaceVariant = Color(0xFF1D1D21),
    onSurfaceVariant = Color(0xFFD4D0C6),
    outline = Color(0xFF665D45),
    outlineVariant = Color(0xFF302D27),
    error = Color(0xFFE57373),
    errorContainer = BrandPalette.Burgundy
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
