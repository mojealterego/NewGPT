package com.mojealterego.newgpt.domain.integration

/**
 * OpenAI ecosystem capability map used by Connected App Fabric.
 *
 * This is an integration contract/catalog, not a claim that NewGPT has
 * credentials or production access to every OpenAI service.
 */
enum class OpenAIPlatformCapability {
    RESPONSES_API,
    AGENTS_SDK,
    BUILT_IN_WEB_SEARCH,
    BUILT_IN_FILE_SEARCH,
    COMPUTER_USE,
    FUNCTION_CALLING,
    STRUCTURED_OUTPUTS,
    VISION,
    IMAGE_GENERATION,
    AUDIO_TRANSCRIPTION,
    AUDIO_GENERATION,
    REALTIME_AUDIO,
    REASONING,
    FINE_TUNING,
    EVALUATIONS,
    DISTILLATION,
    MODERATION,
    REMOTE_MCP,
    CHATGPT_PLUGINS,
    CHATGPT_DEVELOPER_MODE,
    CHATGPT_APPS,
    WORKSPACE_AGENTS,
    AGENTIC_COMMERCE,
    ADS_API,
    CODEX
}

data class OpenAIPluginDescriptor(
    val id: String,
    val name: String,
    val category: String,
    val capabilities: Set<String>,
    val source: String = "chatgpt-plugin-catalog"
)

object OpenAIPlatformCatalog {
    val capabilities = OpenAIPlatformCapability.entries.toSet()

    /**
     * Named services surfaced by the supplied ChatGPT plugins directory.
     * The directory is a changing catalog; entries here are discovery metadata.
     */
    val plugins: List<OpenAIPluginDescriptor> = listOf(
        OpenAIPluginDescriptor("gmail", "Gmail", "Communication", setOf("mail", "search", "draft")),
        OpenAIPluginDescriptor("google-drive", "Google Drive", "Productivity", setOf("drive", "docs", "sheets", "slides")),
        OpenAIPluginDescriptor("outlook-email", "Outlook Email", "Communication", setOf("mail", "triage", "draft")),
        OpenAIPluginDescriptor("github", "GitHub", "Developer Tools", setOf("repositories", "issues", "pull-requests", "ci")),
        OpenAIPluginDescriptor("sharepoint", "SharePoint", "Enterprise", setOf("sites", "files", "search")),
        OpenAIPluginDescriptor("slack", "Slack", "Communication", setOf("chat", "search", "management")),
        OpenAIPluginDescriptor("google-calendar", "Google Calendar", "Productivity", setOf("calendar", "scheduling")),
        OpenAIPluginDescriptor("notion", "Notion", "Productivity", setOf("specs", "research", "meetings", "knowledge")),
        OpenAIPluginDescriptor("linear", "Linear", "Developer Tools", setOf("products", "issues", "projects")),
        OpenAIPluginDescriptor("clickup", "ClickUp", "Productivity", setOf("tasks", "projects")),
        OpenAIPluginDescriptor("asana", "Asana", "Productivity", setOf("tasks", "projects")),
        OpenAIPluginDescriptor("dropbox", "Dropbox", "Productivity", setOf("files", "sharing")),
        OpenAIPluginDescriptor("canva", "Canva", "Creativity", setOf("design", "review", "edit")),
        OpenAIPluginDescriptor("figma", "Figma", "Creativity", setOf("design", "prototype", "design-to-code")),
        OpenAIPluginDescriptor("gamma", "Gamma", "Creativity", setOf("presentations", "docs")),
        OpenAIPluginDescriptor("descript", "Descript", "Creativity", setOf("video", "editing")),
        OpenAIPluginDescriptor("adobe", "Adobe", "Creativity", setOf("design", "combine", "edit")),
        OpenAIPluginDescriptor("supabase", "Supabase", "Developer Tools", setOf("database", "query")),
        OpenAIPluginDescriptor("vercel", "Vercel", "Developer Tools", setOf("build", "deploy", "agents")),
        OpenAIPluginDescriptor("lovable", "Lovable", "Developer Tools", setOf("apps", "websites")),
        OpenAIPluginDescriptor("replit", "Replit", "Developer Tools", setOf("apps", "development")),
        OpenAIPluginDescriptor("openai-developers", "OpenAI Developers", "Developer Tools", setOf("api", "agents", "chatgpt-apps")),
        OpenAIPluginDescriptor("hubspot", "HubSpot", "Business", setOf("crm", "insights", "actions")),
        OpenAIPluginDescriptor("attio", "Attio", "Business", setOf("crm")),
        OpenAIPluginDescriptor("salesforce", "Salesforce", "Business", setOf("crm", "records")),
        OpenAIPluginDescriptor("clay", "Clay", "Business", setOf("gtm", "data", "functions")),
        OpenAIPluginDescriptor("intercom", "Intercom", "Business", setOf("customers", "contacts", "tickets")),
        OpenAIPluginDescriptor("posthog", "PostHog", "Data & Analytics", setOf("product-data")),
        OpenAIPluginDescriptor("amplitude", "Amplitude", "Data & Analytics", setOf("product-intelligence")),
        OpenAIPluginDescriptor("mixpanel", "Mixpanel", "Data & Analytics", setOf("analytics")),
        OpenAIPluginDescriptor("bigquery", "BigQuery", "Data & Analytics", setOf("sql", "resources")),
        OpenAIPluginDescriptor("consensus", "Consensus", "Education & Research", setOf("scientific-research")),
        OpenAIPluginDescriptor("sider-scholar", "Sider Scholar", "Education & Research", setOf("papers")),
        OpenAIPluginDescriptor("scispace", "SciSpace", "Education & Research", setOf("research")),
        OpenAIPluginDescriptor("scite", "Scite", "Education & Research", setOf("evidence", "science")),
        OpenAIPluginDescriptor("wolfram", "Wolfram", "Education & Research", setOf("computation", "knowledge")),
        OpenAIPluginDescriptor("midpage-legal", "Midpage Legal Research", "Education & Research", setOf("legal-research")),
        OpenAIPluginDescriptor("malwarebytes", "Malwarebytes", "Security", setOf("url", "domain", "phone-check")),
        OpenAIPluginDescriptor("bitdefender", "Bitdefender", "Security", setOf("url-check")),
        OpenAIPluginDescriptor("vanta", "Vanta", "Security", setOf("trust", "compliance")),
        OpenAIPluginDescriptor("public-equity", "Public Equity Investing", "Finance", setOf("equities", "earnings", "etf")),
        OpenAIPluginDescriptor("alpaca", "Alpaca", "Finance", setOf("market-data", "stocks", "crypto")),
        OpenAIPluginDescriptor("binance", "Binance", "Finance", setOf("market-data", "crypto")),
        OpenAIPluginDescriptor("stripe", "Stripe", "Finance", setOf("payments")),
        OpenAIPluginDescriptor("health", "Health", "Healthcare", setOf("health-data")),
        OpenAIPluginDescriptor("coros", "COROS", "Healthcare", setOf("workouts", "fitness-data")),
        OpenAIPluginDescriptor("myfitnesspal", "MyFitnessPal", "Healthcare", setOf("nutrition", "meal-plans")),
        OpenAIPluginDescriptor("skyscanner", "Skyscanner", "Travel", setOf("flights")),
        OpenAIPluginDescriptor("trip-com", "Trip.com", "Travel", setOf("flights", "trains")),
        OpenAIPluginDescriptor("flight-network", "Flight Network", "Travel", setOf("flights", "booking")),
        OpenAIPluginDescriptor("edreams", "eDreams", "Travel", setOf("flights", "hotels")),
        OpenAIPluginDescriptor("wikiloc", "Wikiloc", "Travel", setOf("trails")),
        OpenAIPluginDescriptor("apple-music", "Apple Music", "Entertainment", setOf("playlists", "music")),
        OpenAIPluginDescriptor("ticketmaster", "Ticketmaster", "Entertainment", setOf("events")),
        OpenAIPluginDescriptor("shazam", "Shazam", "Entertainment", setOf("music-identification")),
        OpenAIPluginDescriptor("spotify", "Spotify", "Entertainment", setOf("music", "podcasts")),
        OpenAIPluginDescriptor("etsy", "Etsy", "Commerce", setOf("shopping")),
        OpenAIPluginDescriptor("alltrails", "AllTrails", "Other", setOf("hiking", "trails")),
        OpenAIPluginDescriptor("superhuman-mail", "Superhuman Mail", "Communication", setOf("email"))
    )

    fun find(id: String): OpenAIPluginDescriptor? = plugins.firstOrNull { it.id == id }
    fun search(query: String): List<OpenAIPluginDescriptor> =
        plugins.filter {
            it.id.contains(query, true) ||
                it.name.contains(query, true) ||
                it.category.contains(query, true) ||
                it.capabilities.any { capability -> capability.contains(query, true) }
        }
}
