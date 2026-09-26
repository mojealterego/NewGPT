package com.mojealterego.newgpt.domain.ecosystem

enum class AiEcosystemCategory {
    AGENTIC_AI,
    MODELS,
    MEMORY_RAG,
    DEVELOPER_TOOLS,
    SOCIAL_CONTENT,
    COMMERCE,
    MEDIA_VOICE,
    KNOWLEDGE_SOURCES
}

enum class AiEcosystemKind {
    FRAMEWORK,
    PLATFORM,
    MODEL,
    VECTOR_STORE,
    MEMORY,
    SAFETY,
    OBSERVABILITY,
    DEVELOPER_TOOL,
    SOCIAL,
    COMMERCE,
    MEDIA,
    KNOWLEDGE_SOURCE
}

data class AiEcosystemEntry(
    val id: String,
    val name: String,
    val category: AiEcosystemCategory,
    val kind: AiEcosystemKind,
    val requiresAdapter: Boolean = true
)

object AiEcosystemCatalog {
    private fun e(name: String, category: AiEcosystemCategory, kind: AiEcosystemKind) =
        AiEcosystemEntry(name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'), name, category, kind)

    val entries: List<AiEcosystemEntry> = listOf(
        e("n8n", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.PLATFORM),
        e("LangGraph", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("AutoGen", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("CrewAI", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("LlamaIndex", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("Pinecone", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.VECTOR_STORE),
        e("Weaviate", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.VECTOR_STORE),
        e("Hugging Face", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.PLATFORM),
        e("Cohere", AiEcosystemCategory.MODELS, AiEcosystemKind.PLATFORM),
        e("OpenAI GPT Builder", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.PLATFORM),
        e("NVIDIA", AiEcosystemCategory.MODELS, AiEcosystemKind.PLATFORM),
        e("OpenAgents", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("MetaGPT", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("AgentVerse", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("AutoGPT", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("BabyAGI", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("ReAct", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("LangChain Agent Executors", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("ChatDev", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("SupaAgent", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("AgentHub", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.PLATFORM),
        e("Camel", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("DUST", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.PLATFORM),
        e("GPT-4o", AiEcosystemCategory.MODELS, AiEcosystemKind.MODEL),
        e("Claude 3 Opus", AiEcosystemCategory.MODELS, AiEcosystemKind.MODEL),
        e("Mistral", AiEcosystemCategory.MODELS, AiEcosystemKind.MODEL),
        e("OpenDevin", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("Thought Source", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("LangChain Toolkits", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("BrowserPilot", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.DEVELOPER_TOOL),
        e("WebAgent", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("ToolLLM", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("Gorilla", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.FRAMEWORK),
        e("CrewAI Tools", AiEcosystemCategory.AGENTIC_AI, AiEcosystemKind.DEVELOPER_TOOL),
        e("LangChain Memory", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.MEMORY),
        e("MemGPT", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.MEMORY),
        e("Chroma", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.VECTOR_STORE),
        e("Qdrant", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.VECTOR_STORE),
        e("MemoryGraph", AiEcosystemCategory.MEMORY_RAG, AiEcosystemKind.MEMORY),
        e("Guardrails AI", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.SAFETY),
        e("Constitutional AI", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.SAFETY),
        e("OpenAI Moderation API", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.SAFETY),
        e("Red-Teaming Agents", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.SAFETY),
        e("Amazon Bedrock", AiEcosystemCategory.MODELS, AiEcosystemKind.PLATFORM),
        e("Anthropic API", AiEcosystemCategory.MODELS, AiEcosystemKind.PLATFORM),
        e("PromptLayer", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.OBSERVABILITY),
        e("Helicone", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.OBSERVABILITY),
        e("TruLens", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.OBSERVABILITY),
        e("Bubble", AiEcosystemCategory.DEVELOPER_TOOLS, AiEcosystemKind.PLATFORM),
        e("Grok", AiEcosystemCategory.MODELS, AiEcosystemKind.MODEL),

        e("FeedHive", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Ocoya", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Predis.ai", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Canva Magic Design", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Flick", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Brandwatch", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Sprout Social", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Buffer", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Later", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Metricool", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Facebook", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Facebook Reels", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("TikTok", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Reddit", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),
        e("Pinterest", AiEcosystemCategory.SOCIAL_CONTENT, AiEcosystemKind.SOCIAL),

        e("Amazon", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("ShareASale", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Awin", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Bluehost", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Fiverr", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Etsy", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Shopify", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Teachable", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Stan Store", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Mediavine", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("AdThrive", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Beehiiv", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Substack", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Amazon KDP", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Adobe Stock", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),
        e("Shutterstock", AiEcosystemCategory.COMMERCE, AiEcosystemKind.COMMERCE),

        e("VAPI", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),
        e("ElevenLabs", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),
        e("Opus", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),
        e("Udio", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),
        e("Suno AI", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),
        e("Spotify", AiEcosystemCategory.MEDIA_VOICE, AiEcosystemKind.MEDIA),

        e("TechCrunch", AiEcosystemCategory.KNOWLEDGE_SOURCES, AiEcosystemKind.KNOWLEDGE_SOURCE),
        e("The Verge", AiEcosystemCategory.KNOWLEDGE_SOURCES, AiEcosystemKind.KNOWLEDGE_SOURCE),
        e("MIT Tech Review", AiEcosystemCategory.KNOWLEDGE_SOURCES, AiEcosystemKind.KNOWLEDGE_SOURCE)
    )

    fun byCategory(category: AiEcosystemCategory): List<AiEcosystemEntry> =
        entries.filter { it.category == category }

    fun find(id: String): AiEcosystemEntry? = entries.firstOrNull { it.id == id }
}
