package com.mojealterego.newgpt.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mojealterego.newgpt.R

object BrandPalette {
    val Obsidian = Color(0xFF050506)
    val AnilineBlack = Color(0xFF0B0B0D)
    val Leather = Color(0xFF111114)
    val Gold = Color(0xFFD4AF37)
    val GoldBright = Color(0xFFF6D978)
    val GoldDeep = Color(0xFF8A6500)
    val Burgundy = Color(0xFF651827)
    val BottleGreen = Color(0xFF123C2C)
    val RoyalBlue = Color(0xFF182F68)
    val Ivory = Color(0xFFF5F1E7)
    val Titanium = Color(0xFFB9BCC3)
}

@Composable
fun BrandBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BrandPalette.Obsidian,
                        BrandPalette.AnilineBlack,
                        Color(0xFF08080A),
                        BrandPalette.Obsidian
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPalette.Gold.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.50f, size.height * 0.08f),
                    radius = size.minDimension * 0.62f
                ),
                radius = size.minDimension * 0.62f,
                center = Offset(size.width * 0.50f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPalette.Burgundy.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.02f, size.height * 0.78f),
                    radius = size.minDimension * 0.50f
                ),
                radius = size.minDimension * 0.50f,
                center = Offset(size.width * 0.02f, size.height * 0.78f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPalette.BottleGreen.copy(alpha = 0.09f), Color.Transparent),
                    center = Offset(size.width * 0.98f, size.height * 0.68f),
                    radius = size.minDimension * 0.44f
                ),
                radius = size.minDimension * 0.44f,
                center = Offset(size.width * 0.98f, size.height * 0.68f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPalette.RoyalBlue.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.12f),
                    radius = size.minDimension * 0.38f
                ),
                radius = size.minDimension * 0.38f,
                center = Offset(size.width * 0.92f, size.height * 0.12f)
            )
        }
        content()
    }
}

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Image(
        painter = painterResource(if (compact) R.drawable.newgpt_mark else R.drawable.newgpt_brand),
        contentDescription = "NewGPT — MojeAlterego",
        modifier = modifier.clip(RoundedCornerShape(18.dp))
    )
}

@Composable
fun BrandSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = BrandPalette.GoldBright,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun GoldRule(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        BrandPalette.GoldDeep,
                        BrandPalette.GoldBright,
                        BrandPalette.GoldDeep,
                        Color.Transparent
                    )
                )
            )
    )
}
