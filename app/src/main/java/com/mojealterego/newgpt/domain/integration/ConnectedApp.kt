package com.mojealterego.newgpt.domain.integration

/** Unified registry for cross-app agent integrations. */
enum class IntegrationKind { APP_FUNCTIONS, OAUTH_API, ANDROID_INTENT, ACCESSIBILITY, WEB }
enum class ActionRisk { READ_ONLY, WRITE, SENSITIVE }

data class ConnectedAppAction(
    val id: String,
    val description: String,
    val risk: ActionRisk,
    val confirmationRequired: Boolean = risk != ActionRisk.READ_ONLY
)

data class ConnectedApp(
    val id: String,
    val name: String,
    val category: String,
    val kinds: Set<IntegrationKind>,
    val actions: List<ConnectedAppAction>,
    val source: String = "catalog",
    val enabledByDefault: Boolean = false
)

object ConnectedAppRegistry {
    private fun app(id: String, name: String, category: String, vararg actions: ConnectedAppAction, kinds: Set<IntegrationKind> = setOf(IntegrationKind.APP_FUNCTIONS, IntegrationKind.OAUTH_API)) =
        ConnectedApp(id, name, category, kinds, actions.toList())

    val catalog: List<ConnectedApp> = listOf(
        app("gmail", "Gmail", "Google", ConnectedAppAction("search", "Search mail", ActionRisk.READ_ONLY), ConnectedAppAction("draft", "Create draft", ActionRisk.WRITE)),
        app("calendar", "Google Calendar", "Google", ConnectedAppAction("search", "Find events", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create event", ActionRisk.WRITE)),
        app("drive", "Google Drive", "Google", ConnectedAppAction("search", "Find files", ActionRisk.READ_ONLY), ConnectedAppAction("upload", "Upload file", ActionRisk.WRITE)),
        app("docs", "Google Docs", "Google", ConnectedAppAction("search", "Find documents", ActionRisk.READ_ONLY), ConnectedAppAction("edit", "Edit document", ActionRisk.WRITE)),
        app("sheets", "Google Sheets", "Google", ConnectedAppAction("read", "Read spreadsheet data", ActionRisk.READ_ONLY), ConnectedAppAction("write", "Write spreadsheet data", ActionRisk.WRITE)),
        app("maps", "Google Maps", "Google", ConnectedAppAction("search", "Search places", ActionRisk.READ_ONLY), ConnectedAppAction("directions", "Plan route", ActionRisk.READ_ONLY)),
        app("youtube", "YouTube", "Google", ConnectedAppAction("search", "Search videos", ActionRisk.READ_ONLY), ConnectedAppAction("play", "Play media", ActionRisk.WRITE)),
        app("play", "Google Play", "Google", ConnectedAppAction("search", "Find apps", ActionRisk.READ_ONLY), ConnectedAppAction("install", "Install app", ActionRisk.SENSITIVE)),
        app("photos", "Google Photos", "Google", ConnectedAppAction("search", "Find photos", ActionRisk.READ_ONLY)),
        app("tasks", "Google Tasks", "Google", ConnectedAppAction("list", "Read tasks", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create task", ActionRisk.WRITE)),
        app("keep", "Google Keep", "Google", ConnectedAppAction("search", "Find notes", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create note", ActionRisk.WRITE)),
        app("phone", "Phone", "Communication", ConnectedAppAction("call", "Place phone call", ActionRisk.SENSITIVE), kinds = setOf(IntegrationKind.ANDROID_INTENT, IntegrationKind.ACCESSIBILITY)),
        app("messages", "Messages", "Communication", ConnectedAppAction("compose", "Compose message", ActionRisk.WRITE), ConnectedAppAction("send", "Send message", ActionRisk.SENSITIVE), kinds = setOf(IntegrationKind.ANDROID_INTENT, IntegrationKind.ACCESSIBILITY)),
        app("whatsapp", "WhatsApp", "Communication", ConnectedAppAction("message", "Send WhatsApp message", ActionRisk.SENSITIVE), ConnectedAppAction("call", "Call contact", ActionRisk.SENSITIVE), kinds = setOf(IntegrationKind.APP_FUNCTIONS, IntegrationKind.ANDROID_INTENT, IntegrationKind.ACCESSIBILITY)),
        app("spotify", "Spotify", "Music", ConnectedAppAction("search", "Search music", ActionRisk.READ_ONLY), ConnectedAppAction("play", "Play music", ActionRisk.WRITE), kinds = setOf(IntegrationKind.APP_FUNCTIONS, IntegrationKind.ANDROID_INTENT, IntegrationKind.OAUTH_API)),
        app("airtable", "Airtable", "Productivity", ConnectedAppAction("search", "Search bases and records", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create record", ActionRisk.WRITE)),
        app("linear", "Linear", "Productivity", ConnectedAppAction("search", "Search issues", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create issue", ActionRisk.WRITE), ConnectedAppAction("update", "Update issue", ActionRisk.WRITE)),
        app("monday", "monday.com", "Productivity", ConnectedAppAction("search", "Search boards", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create item", ActionRisk.WRITE)),
        app("pandadoc", "PandaDoc", "Productivity", ConnectedAppAction("search", "Find documents", ActionRisk.READ_ONLY), ConnectedAppAction("send", "Send document", ActionRisk.SENSITIVE)),
        app("wispr", "Wispr AI", "Productivity", ConnectedAppAction("dictate", "Create dictated text", ActionRisk.WRITE)),
        app("zoho", "Zoho", "Productivity", ConnectedAppAction("search", "Search CRM/workspace", ActionRisk.READ_ONLY), ConnectedAppAction("update", "Update record", ActionRisk.WRITE)),
        app("adobe", "Adobe", "Creative", ConnectedAppAction("search", "Find creative assets", ActionRisk.READ_ONLY), ConnectedAppAction("edit", "Edit asset", ActionRisk.WRITE)),
        app("picsart", "Picsart", "Creative", ConnectedAppAction("edit", "Edit image", ActionRisk.WRITE), ConnectedAppAction("generate", "Generate creative asset", ActionRisk.WRITE)),
        app("squarespace", "Squarespace", "Creative", ConnectedAppAction("search", "Inspect site content", ActionRisk.READ_ONLY), ConnectedAppAction("edit", "Edit website", ActionRisk.SENSITIVE)),
        app("webflow", "Webflow", "Creative", ConnectedAppAction("search", "Inspect site", ActionRisk.READ_ONLY), ConnectedAppAction("publish", "Publish website changes", ActionRisk.SENSITIVE)),
        app("notion", "Notion", "Productivity", ConnectedAppAction("search", "Search workspace", ActionRisk.READ_ONLY), ConnectedAppAction("create", "Create page", ActionRisk.WRITE), ConnectedAppAction("update", "Update page", ActionRisk.WRITE)),
        app("wix", "Wix", "Creative", ConnectedAppAction("search", "Inspect site", ActionRisk.READ_ONLY), ConnectedAppAction("edit", "Edit website", ActionRisk.WRITE)),
        app("granola", "Granola", "Productivity", ConnectedAppAction("search", "Search meeting notes", ActionRisk.READ_ONLY)),
        app("otter", "Otter.ai", "Productivity", ConnectedAppAction("search", "Search transcripts", ActionRisk.READ_ONLY)),
        app("fever", "Fever", "Lifestyle", ConnectedAppAction("search", "Find experiences", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Book experience", ActionRisk.SENSITIVE)),
        app("getyourguide", "GetYourGuide", "Travel", ConnectedAppAction("search", "Find activities", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Book activity", ActionRisk.SENSITIVE)),
        app("localiza", "Localiza", "Travel", ConnectedAppAction("search", "Find rental cars", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Book rental", ActionRisk.SENSITIVE)),
        app("opentable", "OpenTable", "Travel", ConnectedAppAction("search", "Find restaurants", ActionRisk.READ_ONLY), ConnectedAppAction("reserve", "Reserve table", ActionRisk.SENSITIVE)),
        app("ticketmaster", "Ticketmaster", "Entertainment", ConnectedAppAction("search", "Find events", ActionRisk.READ_ONLY), ConnectedAppAction("buy", "Buy ticket", ActionRisk.SENSITIVE)),
        app("iheart", "iHeartRadio", "Music", ConnectedAppAction("search", "Find stations", ActionRisk.READ_ONLY), ConnectedAppAction("play", "Play station", ActionRisk.WRITE)),
        app("pandora", "Pandora", "Music", ConnectedAppAction("search", "Find stations", ActionRisk.READ_ONLY), ConnectedAppAction("play", "Play station", ActionRisk.WRITE)),
        app("angi", "Angi", "Home", ConnectedAppAction("search", "Find professionals", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Request service", ActionRisk.SENSITIVE)),
        app("thumbtack", "Thumbtack", "Home", ConnectedAppAction("search", "Find providers", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Request service", ActionRisk.SENSITIVE)),
        app("zocdoc", "Zocdoc", "Health", ConnectedAppAction("search", "Find appointments", ActionRisk.READ_ONLY), ConnectedAppAction("book", "Book appointment", ActionRisk.SENSITIVE)),
        app("apartments", "Apartments.com", "Lifestyle", ConnectedAppAction("search", "Search apartments", ActionRisk.READ_ONLY)),
        app("experian", "Experian", "Lifestyle", ConnectedAppAction("read", "View eligible credit information", ActionRisk.READ_ONLY)),
        app("peloton", "Peloton", "Fitness", ConnectedAppAction("search", "Find workouts", ActionRisk.READ_ONLY), ConnectedAppAction("start", "Start workout", ActionRisk.WRITE)),
        app("seatgeek", "SeatGeek", "Entertainment", ConnectedAppAction("search", "Find tickets", ActionRisk.READ_ONLY), ConnectedAppAction("buy", "Buy ticket", ActionRisk.SENSITIVE))
    )

    /** Existing operational contracts plus the scalable discovery catalog. */
    val fabricCatalog: List<FabricAppDescriptor>
        get() = ConnectedAppFabricSeed.apps

    fun find(id: String): ConnectedApp? = catalog.firstOrNull { it.id == id }

    fun search(query: String): List<ConnectedApp> = catalog.filter {
        it.name.contains(query, true) ||
            it.category.contains(query, true) ||
            it.actions.any { action -> action.description.contains(query, true) }
    }

    fun searchFabric(query: String): List<FabricAppDescriptor> =
        ConnectedAppFabricPolicy.search(query)

    fun byFabricCategory(category: FabricCategory): List<FabricAppDescriptor> =
        ConnectedAppFabricPolicy.byCategory(category)

    /**
     * Returns true only for providers represented by the operational contract
     * above. Discovery metadata never silently becomes an executable connector.
     */
    fun hasOperationalAdapter(id: String): Boolean = catalog.any { it.id == id }
}