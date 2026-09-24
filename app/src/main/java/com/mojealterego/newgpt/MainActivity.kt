package com.mojealterego.newgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mojealterego.newgpt.data.local.AppPreferencesStore
import com.mojealterego.newgpt.presentation.NewGptApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val language = AppPreferencesStore.loadLanguage(applicationContext)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NewGptApp() }
    }
}
