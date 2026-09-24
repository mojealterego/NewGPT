package com.mojealterego.newgpt.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mojealterego.newgpt.presentation.chat.ChatScreen
import com.mojealterego.newgpt.presentation.settings.SettingsScreen
import com.mojealterego.newgpt.presentation.theme.NewGptTheme

@Composable
fun NewGptApp() {
    NewGptTheme {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "chat") {
            composable("chat") { ChatScreen(onSettings = { nav.navigate("settings") }) }
            composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
