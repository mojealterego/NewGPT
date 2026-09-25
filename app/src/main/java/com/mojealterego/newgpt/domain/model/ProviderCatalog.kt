package com.mojealterego.newgpt.domain.model

data class ProviderPreset(
    val id: String,
    val name: String,
    val baseUrl: String,
    val defaultModel: String,
    val note: String
)

object ProviderCatalog {
    val presets = listOf(
        ProviderPreset("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", "openrouter/auto", "Multi-provider routing"),
        ProviderPreset("groq", "Groq", "https://api.groq.com/openai/v1", "openai/gpt-oss-20b", "OpenAI-compatible"),
        ProviderPreset("mistral", "Mistral AI", "https://api.mistral.ai/v1", "mistral-large-latest", "OpenAI-compatible"),
        ProviderPreset("deepseek", "DeepSeek", "https://api.deepseek.com", "deepseek-flash", "OpenAI-compatible"),
        ProviderPreset("xai", "xAI / Grok", "https://api.x.ai/v1", "grok-4.7", "OpenAI-compatible"),
        ProviderPreset("perplexity", "Perplexity", "https://api.perplexity.ai", "sonar", "OpenAI-compatible"),
        ProviderPreset("together", "Together AI", "https://api.together.xyz/v1", "meta-llama/Llama-3.3-70B-Instruct-Turbo", "OpenAI-compatible"),
        ProviderPreset("fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1", "accounts/fireworks/models/llama-v3p1-8b-instruct", "OpenAI-compatible"),
        ProviderPreset("cerebras", "Cerebras", "https://api.cerebras.ai/v1", "llama-3.3-70b", "OpenAI-compatible"),
        ProviderPreset("sambanova", "SambaNova", "https://api.sambanova.ai/v1", "Meta-Llama-3.3-70B-Instruct", "OpenAI-compatible"),
        ProviderPreset("deepinfra", "DeepInfra", "https://api.deepinfra.com/v1/openai", "meta-llama/Meta-Llama-3.1-70B-Instruct", "OpenAI-compatible"),
        ProviderPreset("nvidia", "NVIDIA NIM", "https://integrate.api.nvidia.com/v1", "meta/llama-3.1-70b-instruct", "OpenAI-compatible"),
        ProviderPreset("novita", "Novita AI", "https://api.novita.ai/v3/openai", "meta-llama/llama-3.1-70b-instruct", "OpenAI-compatible"),
        ProviderPreset("cohere", "Cohere", "https://api.cohere.ai/compatibility/v1", "command-a-plus-05-2026", "OpenAI-compatible compatibility API"),
        ProviderPreset("cloudflare-workers-ai", "Cloudflare Workers AI", "", "@cf/openai/gpt-oss-20b", "Wymaga account-specific Workers AI base URL"),
        ProviderPreset("azure-openai", "Azure OpenAI / Foundry", "", "", "Wymaga endpointu zasobu Azure i deployment/modelu"),
        ProviderPreset("custom", "Custom OpenAI-compatible", "", "", "Dowolny zgodny endpoint")
    )

    fun find(id: String): ProviderPreset? = presets.firstOrNull { it.id == id }
}
