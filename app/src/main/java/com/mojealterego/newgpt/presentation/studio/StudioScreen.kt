package com.mojealterego.newgpt.presentation.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojealterego.newgpt.data.local.AppPreferencesStore
import com.mojealterego.newgpt.data.local.SecureSettings
import com.mojealterego.newgpt.data.remote.CreativeStudioService
import com.mojealterego.newgpt.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StudioViewModel @Inject constructor(
    private val prefs: AppPreferencesStore,
    private val secureSettings: SecureSettings,
    private val service: CreativeStudioService
) : ViewModel() {
    var status by mutableStateOf("")
        private set

    fun speech(voice: String, text: String) = run("TTS") {
        val bytes = service.textToSpeech(prefs.preferences.value.elevenLabsKey, voice, text)
        File.createTempFile("newgpt-voice-", ".mp3").apply { writeBytes(bytes) }.absolutePath
            .let { "Wygenerowano audio: " + it }
    }

    fun music(prompt: String) = run("Music") {
        val bytes = service.music(prefs.preferences.value.elevenLabsKey, prompt)
        File.createTempFile("newgpt-music-", ".mp3").apply { writeBytes(bytes) }.absolutePath
            .let { "Wygenerowano muzykę: " + it }
    }

    fun video(prompt: String) = run("Video") {
        "Wideo: " + service.generateVideo(prefs.preferences.value.xaiKey, prompt)
    }

    fun canva(title: String) = run("Canva") {
        val editUrl = service.createCanvaDesign(secureSettings.canvaAccessToken.value, title)
        "Canva edit URL: " + editUrl
    }

    private fun run(label: String, block: suspend () -> String) {
        viewModelScope.launch {
            status = "Generowanie " + label + "…"
            status = runCatching { block() }.getOrElse { "Błąd " + label + ": " + (it.message ?: "nieznany") }
        }
    }
}

@Composable
fun StudioScreen(onBack: () -> Unit, onSettings: () -> Unit, viewModel: StudioViewModel = hiltViewModel()) {
    var text by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("ElevenLabs") }

    BrandBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { BrandGlobalHeader(onMenu = onBack, onSettings = onSettings) }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrandPageHeader("CREATIVE STUDIO", "VOICE · MUSIC · VIDEO · DESIGN")

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    BrandPalette.Burgundy.copy(alpha = 0.68f),
                                    BrandPalette.GoldDeep.copy(alpha = 0.30f),
                                    BrandPalette.AnilineBlack,
                                    BrandPalette.Obsidian
                                )
                            )
                        )
                        .border(1.5.dp, BrandPalette.GoldBright, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "ElevenLabs · Voice · Music · xAI Video",
                            style = MaterialTheme.typography.headlineSmall,
                            color = BrandPalette.Ivory,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Twórz głos, muzykę i wideo z AI. Klucze API ustaw w Ustawieniach.",
                            color = BrandPalette.Ivory.copy(alpha = 0.86f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        tint = BrandPalette.GoldBright.copy(alpha = 0.32f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(22.dp).size(120.dp)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProviderTile("ElevenLabs", Icons.Default.Mic, selected == "ElevenLabs") { selected = "ElevenLabs" }
                    ProviderTile("Music", Icons.Default.MusicNote, selected == "Music") { selected = "Music" }
                    ProviderTile("xAI Video", Icons.Default.PlayArrow, selected == "xAI Video") { selected = "xAI Video" }
                    ProviderTile("Canva", Icons.Default.Palette, selected == "Canva") { selected = "Canva" }
                }

                LuxuryCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = BrandPalette.GoldBright)
                            Spacer(Modifier.width(8.dp))
                            Text("PROMPT / TEKST", color = BrandPalette.GoldBright, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("${text.length}/4000", color = BrandPalette.Titanium, style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { if (it.length <= 4000) text = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            placeholder = { Text("Opisz co chcesz wygenerować…") },
                            shape = RoundedCornerShape(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("Styl") })
                            AssistChip(onClick = {}, label = { Text("Jakość") })
                            AssistChip(onClick = {}, label = { Text("Czas") })
                        }
                    }
                }

                StudioAction("GENERUJ GŁOS", "ElevenLabs · Naturalny głos · Emocje · Dialogi", Icons.Default.Mic) {
                    viewModel.speech("JBFqnCBsd6RMkjVDRZzb", text)
                }
                StudioAction("GENERUJ MUZYKĘ", "AI Music · Style · Mood · własne teksty", Icons.Default.MusicNote) {
                    viewModel.music(text)
                }
                StudioAction("GENERUJ WIDEO", "xAI Video · Cinematic · Story · Produkcja", Icons.Default.PlayArrow) {
                    viewModel.video(text)
                }
                StudioAction("UTWÓRZ PROJEKT CANVA", "Grafika · Okładki · Posty · Prezentacje", Icons.Default.Palette) {
                    viewModel.canva(text.ifBlank { "NewGPT Creative Design" })
                }

                LuxuryCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = BrandPalette.GoldBright)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Canva Connect wymaga OAuth 2.0", color = BrandPalette.Ivory, fontWeight = FontWeight.Bold)
                            Text(
                                "Authorization Code + PKCE i odpowiednie scope zgodnie z dokumentacją Canva.",
                                color = BrandPalette.Titanium,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (viewModel.status.isNotBlank()) {
                    Text(viewModel.status, color = BrandPalette.GoldBright, modifier = Modifier.padding(horizontal = 8.dp))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProviderTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .weight(1f)
            .height(78.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) BrandPalette.Gold.copy(alpha = 0.22f) else BrandPalette.AnilineBlack)
            .border(1.5.dp, if (active) BrandPalette.GoldBright else BrandPalette.GoldDeep, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = if (active) BrandPalette.GoldBright else BrandPalette.Ivory, modifier = Modifier.size(25.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = BrandPalette.Ivory, maxLines = 1)
    }
}

@Composable
private fun StudioAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(BrandPalette.AnilineBlack, BrandPalette.Leather, BrandPalette.AnilineBlack)))
            .border(1.5.dp, BrandPalette.GoldDeep, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BrandPalette.Gold.copy(alpha = 0.18f))
                .border(1.dp, BrandPalette.Gold, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandPalette.GoldBright, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, color = BrandPalette.Ivory, fontWeight = FontWeight.Bold)
            Text(subtitle, color = BrandPalette.Titanium, style = MaterialTheme.typography.bodySmall)
        }
        Text("›", color = BrandPalette.GoldBright, style = MaterialTheme.typography.headlineMedium)
    }
}
