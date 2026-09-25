package com.mojealterego.newgpt.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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


@Composable
fun BrandTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit) = {}
) {
    androidx.compose.material3.TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, color = BrandPalette.Ivory)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = BrandPalette.GoldBright)
                }
            }
        },
        navigationIcon = {
            onBack?.let {
                androidx.compose.material3.IconButton(onClick = it) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Wstecz",
                        tint = BrandPalette.GoldBright
                    )
                }
            }
        },
        actions = actions,
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = BrandPalette.AnilineBlack.copy(alpha = 0.96f)
        )
    )
}

@Composable
fun AgentPortrait(
    agentId: String,
    size: androidx.compose.ui.unit.Dp = 72.dp,
    showNumber: Boolean = true
) {
    val accent = when (agentId) {
        "wda-photo" -> BrandPalette.Burgundy
        "creative-director" -> BrandPalette.RoyalBlue
        "memory-architect" -> BrandPalette.BottleGreen
        "evolution-engineer" -> Color(0xFF5B3A86)
        "gguf-engineer" -> BrandPalette.Titanium
        "mobile-operator" -> Color(0xFF6B4E24)
        "researcher", "web-researcher", "rag-master" -> BrandPalette.RoyalBlue
        "architect", "coder" -> BrandPalette.GoldDeep
        "writer" -> Color(0xFF704214)
        else -> BrandPalette.Gold
    }
    val number = when (agentId) {
        "coordinator" -> "1"
        "researcher" -> "2"
        "architect" -> "3"
        "coder" -> "4"
        "writer" -> "5"
        "wda-photo" -> "6"
        "mobile-operator" -> "7"
        "rag-master" -> "8"
        "web-researcher" -> "9"
        "creative-director" -> "10"
        "gguf-engineer" -> "11"
        "memory-architect" -> "12"
        "evolution-engineer" -> "13"
        else -> "•"
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        BrandPalette.GoldBright.copy(alpha = 0.96f),
                        accent.copy(alpha = 0.82f),
                        BrandPalette.Obsidian
                    )
                )
            )
            .border(2.dp, BrandPalette.GoldBright.copy(alpha = 0.92f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.78f)
                .clip(CircleShape)
                .border(1.dp, BrandPalette.GoldDeep, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (showNumber) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrandPalette.Ivory,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AgentIdentity(agentId: String, name: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgentPortrait(agentId = agentId, size = 52.dp)
        Column(Modifier.padding(start = 12.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = BrandPalette.Ivory)
            Text(
                agentId.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = BrandPalette.GoldBright
            )
        }
    }
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = BrandPalette.Leather.copy(alpha = 0.88f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BrandPalette.GoldDeep.copy(alpha = 0.55f)
        )
    ) {
        content()
    }
}
