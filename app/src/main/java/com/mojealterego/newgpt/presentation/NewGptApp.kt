package com.mojealterego.newgpt.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mojealterego.newgpt.presentation.agents.AgentBuilderScreen
import com.mojealterego.newgpt.presentation.agents.AgentsScreen
import com.mojealterego.newgpt.presentation.chat.ChatScreen
import com.mojealterego.newgpt.presentation.settings.SettingsScreen
import com.mojealterego.newgpt.presentation.studio.StudioScreen
import com.mojealterego.newgpt.presentation.theme.NewGptTheme

@Composable
fun NewGptApp() {
    NewGptTheme {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "chat") {
            composable("chat") {
                ChatScreen(
                    onAgents = { nav.navigate("agents") },
                    onSettings = { nav.navigate("settings") },
                    onStudio = { nav.navigate("studio") }
                )
            }
            composable("agents") {
                AgentsScreen(
                    onBuilder = { nav.navigate("agent-builder") },
                    onSettings = { nav.navigate("settings") }
                )
            }
            composable("agent-builder") {
                AgentBuilderScreen(onBack = { nav.popBackStack() })
            }
            composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable("studio") { StudioScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
