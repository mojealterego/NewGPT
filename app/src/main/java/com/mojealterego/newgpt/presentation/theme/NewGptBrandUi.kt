package com.mojealterego.newgpt.presentation.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mojealterego.newgpt.R

val LocalNewGptNavigate = androidx.compose.runtime.compositionLocalOf<(String) -> Unit> { {} }

private val Gold = Color(0xFFE1B84A)
private val GoldBright = Color(0xFFFFE7A0)
private val GoldDeep = Color(0xFF8D6412)
private val Obsidian = Color(0xFF050506)
private val Panel = Color(0xCC0B0B0D)
private val PanelRaised = Color(0xE6121215)
private val Ivory = Color(0xFFF4EEDF)

@Composable
fun NewGptBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020203), Obsidian, Color(0xFF0A0805), Color(0xFF020203))
                )
            )
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val glow = Brush.radialGradient(
                colors = listOf(Color(0x55E1B84A), Color.Transparent),
                center = Offset(w * .52f, h * .18f),
                radius = w * .72f
            )
            drawRect(glow)
            val line = Path()
            line.moveTo(-w * .15f, h * .72f)
            line.cubicTo(w * .18f, h * .55f, w * .62f, h * .94f, w * 1.12f, h * .62f)
            drawPath(line, Brush.linearGradient(listOf(Color.Transparent, Color(0x66D4AF37), Color.Transparent)), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2f))
            val line2 = Path()
            line2.moveTo(-w * .1f, h * .34f)
            line2.cubicTo(w * .35f, h * .22f, w * .65f, h * .48f, w * 1.1f, h * .28f)
            drawPath(line2, Brush.linearGradient(listOf(Color.Transparent, Color(0x44F3D77A), Color.Transparent)), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
        }
        content()
    }
}

@Composable
fun NewGptHeader(
    title: String? = null,
    subtitle: String = "MOJEALTEREGO",
    onMenu: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xE8030304))
            .border(BorderStroke(1.dp, Color(0xAA8D6412)))
    ) {
        Row(
            Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { (onBack ?: onMenu)?.invoke() }) {
                Icon(
                    imageVector = if (onBack != null) Icons.Default.ArrowBack else Icons.Default.Menu,
                    contentDescription = if (onBack != null) "Wstecz" else "Menu",
                    tint = GoldBright,
                    modifier = Modifier.size(30.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_newgpt_image),
                contentDescription = "NewGPT",
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.width(1.dp).height(46.dp).background(GoldDeep))
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title ?: "NewGPT", style = MaterialTheme.typography.titleLarge.copy(fontSize = 25.sp), color = Ivory, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Gold, letterSpacing = 2.sp)
            }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń", tint = Gold) }
            if (onSettings != null) IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Ustawienia", tint = GoldBright) }
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, GoldBright, GoldDeep, Color.Transparent))))
    }
}

@Composable
fun PremiumTitle(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(50.dp).background(Brush.linearGradient(listOf(GoldBright, Gold)), RoundedCornerShape(18.dp))
            ) { Icon(Icons.Default.ArrowBack, null, tint = Obsidian) }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp), color = Ivory, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Gold, letterSpacing = 2.sp) }
        }
    }
}

@Composable
fun GoldCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color(0xCCB8861B))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Box(
                            Modifier.size(44.dp).border(1.dp, Gold, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) { Icon(it, null, tint = GoldBright) }
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge, color = Ivory)
                }
                HorizontalDivider(color = Color(0x555F4A18))
            }
            content()
        }
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Obsidian,
            disabledContainerColor = Color(0xFF393632),
            disabledContentColor = Color(0xFF8E8980)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
    ) {
        icon?.let { Icon(it, null); Spacer(Modifier.width(10.dp)) }
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutlineGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GoldDeep),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ivory)
    ) {
        icon?.let { Icon(it, null, tint = Gold); Spacer(Modifier.width(8.dp)) }
        Text(text)
    }
}

@Composable
fun GoldSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Obsidian,
            checkedTrackColor = Gold,
            uncheckedThumbColor = Color(0xFFB0B0B0),
            uncheckedTrackColor = Color(0xFF27272A),
            uncheckedBorderColor = GoldDeep
        )
    )
}

@Composable
fun NewGptBottomNav(
    selected: String,
    onPanel: () -> Unit,
    onAgents: () -> Unit,
    onMemory: () -> Unit,
    onTools: () -> Unit,
    onSettings: () -> Unit
) {
    val navigate = LocalNewGptNavigate.current
    NavigationBar(containerColor = Color(0xF5070708), tonalElevation = 0.dp) {
        val items = listOf(
            Triple("Panel", Icons.Default.Home, { navigate("chat") }),
            Triple("Agenci", Icons.Default.SmartToy, { navigate("agents") }),
            Triple("Wiedza", Icons.Default.Storage, { navigate("memory") }),
            Triple("Narzędzia", Icons.Default.Build, { navigate("cognitive") }),
            Triple("Ustawienia", Icons.Default.Settings, { navigate("settings") })
        )
        items.forEach { (label, icon, action) ->
            NavigationBarItem(
                selected = selected == label,
                onClick = action,
                icon = { Icon(icon, label) },
                label = { Text(label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Obsidian,
                    selectedTextColor = GoldBright,
                    indicatorColor = Gold,
                    unselectedIconColor = Color(0xFF9EA0A7),
                    unselectedTextColor = Color(0xFF9EA0A7)
                )
            )
        }
    }
}

@Composable
fun PremiumScaffold(
    selected: String,
    title: String? = null,
    subtitle: String = "MOJEALTEREGO",
    onMenu: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onPanel: () -> Unit,
    onAgents: () -> Unit,
    onMemory: () -> Unit,
    onTools: () -> Unit,
    onSettingsNav: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    NewGptBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NewGptHeader(
                    title = title,
                    subtitle = subtitle,
                    onMenu = onMenu,
                    onBack = onBack,
                    onDelete = onDelete,
                    onSettings = onSettings
                )
            },
            bottomBar = {
                NewGptBottomNav(selected, onPanel, onAgents, onMemory, onTools, onSettingsNav)
            }
        ) { padding -> content(padding) }
    }
}
