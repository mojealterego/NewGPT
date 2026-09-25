package com.mojealterego.newgpt.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val Obsidian = Color(0xFF030304)
private val ObsidianSurface = Color(0xFF0B0B0D)
private val ObsidianRaised = Color(0xFF151518)
private val Gold24K = Color(0xFFE1B84A)
private val GoldHighlight = Color(0xFFFFE7A0)
private val GoldDeep = Color(0xFF8D6412)
private val Ivory = Color(0xFFF4EEDF)
private val Titanium = Color(0xFFB8BBC2)
private val Burgundy = Color(0xFF5E1724)

private val NewGptDarkScheme = darkColorScheme(
    primary = Gold24K,
    onPrimary = Obsidian,
    primaryContainer = Color(0xFF4B3910),
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
    surfaceVariant = Color(0xFF1A1A1E),
    onSurfaceVariant = Color(0xFFD8D2C5),
    outline = GoldDeep,
    outlineVariant = Color(0xFF51472F),
    error = Color(0xFFFF8A80),
    errorContainer = Burgundy
)

private val NewGptTypography = Typography().let {
    val serif = FontFamily.Serif
    it.copy(
        displayLarge = it.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        displayMedium = it.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        displaySmall = it.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        headlineLarge = it.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
        headlineMedium = it.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
        headlineSmall = it.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleLarge = it.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleMedium = it.titleMedium.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        titleSmall = it.titleSmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium),
        bodyLarge = it.bodyLarge.copy(fontFamily = serif),
        bodyMedium = it.bodyMedium.copy(fontFamily = serif),
        bodySmall = it.bodySmall.copy(fontFamily = serif),
        labelLarge = it.labelLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        labelMedium = it.labelMedium.copy(fontFamily = serif),
        labelSmall = it.labelSmall.copy(fontFamily = serif, fontWeight = FontWeight.Medium)
    )
}

private val NewGptShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun NewGptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NewGptDarkScheme,
        typography = NewGptTypography,
        shapes = NewGptShapes,
        content = content
    )
}
