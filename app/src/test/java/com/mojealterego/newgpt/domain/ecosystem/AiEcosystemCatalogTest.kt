package com.mojealterego.newgpt.domain.ecosystem

import com.mojealterego.newgpt.domain.agent.AgentToolCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiEcosystemCatalogTest {
    @Test
    fun catalogContainsEveryRequestedEcosystemItemExactlyOnce() {
        val names = AiEcosystemCatalog.entries.map { it.name }
        assertEquals(names.size, names.toSet().size)

        val required = listOf(
            "n8n", "LangGraph", "AutoGen", "CrewAI", "LlamaIndex", "Pinecone", "Weaviate",
            "Hugging Face", "Cohere", "OpenAI GPT Builder", "NVIDIA", "OpenAgents", "MetaGPT",
            "AgentVerse", "AutoGPT", "BabyAGI", "ReAct", "LangChain Agent Executors", "ChatDev",
            "SupaAgent", "AgentHub", "Camel", "DUST", "GPT-4o", "Claude 3 Opus", "Mistral",
            "OpenDevin", "Thought Source", "LangChain Toolkits", "BrowserPilot", "WebAgent",
            "ToolLLM", "Gorilla", "CrewAI Tools", "LangChain Memory", "MemGPT", "Chroma",
            "Qdrant", "MemoryGraph", "Guardrails AI", "Constitutional AI", "OpenAI Moderation API",
            "Red-Teaming Agents", "Amazon Bedrock", "Anthropic API", "PromptLayer", "Helicone",
            "TruLens", "Bubble", "Grok", "FeedHive", "Ocoya", "Predis.ai", "Canva Magic Design",
            "Flick", "Brandwatch", "Sprout Social", "Buffer", "Later", "Metricool", "Facebook",
            "Facebook Reels", "TikTok", "Reddit", "Pinterest", "Amazon", "ShareASale", "Awin",
            "Bluehost", "Fiverr", "Etsy", "Shopify", "Teachable", "Stan Store", "Mediavine",
            "AdThrive", "Beehiiv", "Substack", "Amazon KDP", "Adobe Stock", "Shutterstock",
            "VAPI", "ElevenLabs", "Opus", "Udio", "Suno AI", "Spotify", "TechCrunch",
            "The Verge", "MIT Tech Review"
        )

        required.forEach { name ->
            assertTrue("Missing ecosystem entry: $name", names.contains(name))
        }
    }

    @Test
    fun catalogExposesStableCategories() {
        val categories = AiEcosystemCatalog.entries.groupBy { it.category }
        assertTrue(categories.containsKey(AiEcosystemCategory.AGENTIC_AI))
        assertTrue(categories.containsKey(AiEcosystemCategory.MODELS))
        assertTrue(categories.containsKey(AiEcosystemCategory.MEMORY_RAG))
        assertTrue(categories.containsKey(AiEcosystemCategory.DEVELOPER_TOOLS))
        assertTrue(categories.containsKey(AiEcosystemCategory.SOCIAL_CONTENT))
        assertTrue(categories.containsKey(AiEcosystemCategory.COMMERCE))
        assertTrue(categories.containsKey(AiEcosystemCategory.MEDIA_VOICE))
        assertTrue(categories.containsKey(AiEcosystemCategory.KNOWLEDGE_SOURCES))
    }

    @Test
    fun ecosystemEntriesAreExposedAsAgentTools() {
        val toolIds = AgentToolCatalog.builtIns.map { it.id }.toSet()
        AiEcosystemCatalog.entries.forEach { entry ->
            assertTrue("Missing tool bridge for: " + entry.name, entry.id in toolIds)
        }
    }
}
