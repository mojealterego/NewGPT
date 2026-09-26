package com.mojealterego.newgpt.domain.agent

import kotlinx.serialization.Serializable

@Serializable
data class AgentTool(
    val id: String,
    val name: String,
    val description: String,
    val capability: Capability,
    val requiresApproval: Boolean = true
) {
    @Serializable
    enum class Capability {
        READ_ONLY,
        NETWORK,
        FILE_SYSTEM,
        ANDROID_UI,
        NOTIFICATIONS,
        CONNECTED_APPS,
        CODE_EXECUTION
    }
}

object AgentToolCatalog {
    val builtIns = listOf(
        AgentTool("web", "Web", "Dostęp do zasobów HTTP(S).", AgentTool.Capability.NETWORK),
        AgentTool("web-search", "Web Search", "Warstwa wyszukiwania sieciowego.", AgentTool.Capability.NETWORK),
        AgentTool("web-fetch", "Web Fetch", "Pobieranie i ekstrakcja treści strony.", AgentTool.Capability.NETWORK),
        AgentTool("documents", "Documents", "Analiza dostarczonych dokumentów.", AgentTool.Capability.READ_ONLY),
        AgentTool("rag", "RAG", "Lokalne wyszukiwanie kontekstu w bazie wiedzy.", AgentTool.Capability.READ_ONLY, false),
        AgentTool("memory-graph", "Memory Graph", "Pamięć robocza, trwała i graf skojarzeń.", AgentTool.Capability.FILE_SYSTEM),
        AgentTool("huggingface", "Hugging Face", "Wyszukiwanie i pobieranie modeli/danych z Hub.", AgentTool.Capability.NETWORK),
        AgentTool("repository", "Repository", "Praca z kodem i plikami repozytorium.", AgentTool.Capability.FILE_SYSTEM),
        AgentTool("git", "Git", "Operacje na repozytoriach Git.", AgentTool.Capability.FILE_SYSTEM),
        AgentTool("compiler", "Compiler", "Weryfikacja kodu przez narzędzia budowania.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("python", "Python", "Obliczenia i automatyzacja przez środowisko Python.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("shell", "Shell", "Wykonywanie poleceń systemowych.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("calculator", "Calculator", "Precyzyjne obliczenia.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("vision", "Vision", "Analiza wejścia obrazowego.", AgentTool.Capability.NETWORK),
        AgentTool("ocr", "OCR", "Ekstrakcja tekstu z obrazu.", AgentTool.Capability.READ_ONLY),
        AgentTool("image-generation", "Image Generation", "Generowanie i edycja obrazów.", AgentTool.Capability.NETWORK),
        AgentTool("video-generation", "Video Generation", "Generowanie i edycja wideo.", AgentTool.Capability.NETWORK),
        AgentTool("voice-generation", "Voice Generation", "Generowanie mowy i voice-over.", AgentTool.Capability.NETWORK),
        AgentTool("music-generation", "Music Generation", "Generowanie muzyki i ścieżek audio.", AgentTool.Capability.NETWORK),
        AgentTool("app-builder", "App Builder", "Budowanie aplikacji przez workflow agentowy.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("creative-studio", "Creative Studio", "Orkiestracja obrazu, audio, głosu i wideo.", AgentTool.Capability.NETWORK),
        AgentTool("evolution-lab", "Evolution Lab", "Kontrolowana pętla propose/evaluate/keep/reject.", AgentTool.Capability.CODE_EXECUTION),
        AgentTool("accessibility", "Android UI", "Planowanie działań na interfejsie Androida.", AgentTool.Capability.ANDROID_UI),
        AgentTool("notifications", "Notifications", "Praca z powiadomieniami urządzenia.", AgentTool.Capability.NOTIFICATIONS),
        AgentTool("connected-apps", "Connected Apps", "Orkiestracja połączonych aplikacji przez AppFunctions, API, Intents i kontrolowane UI.", AgentTool.Capability.CONNECTED_APPS),
        AgentTool("browser", "Browser", "Kontrolowany dostęp do stron i formularzy.", AgentTool.Capability.NETWORK),
        AgentTool("memory", "Memory", "Zarządzanie trwałym kontekstem użytkownika.", AgentTool.Capability.FILE_SYSTEM),
        AgentTool("translation", "Translation", "Tłumaczenie i lokalizacja treści.", AgentTool.Capability.NETWORK)
    )

    fun find(id: String): AgentTool? = builtIns.firstOrNull { it.id == id }
}
