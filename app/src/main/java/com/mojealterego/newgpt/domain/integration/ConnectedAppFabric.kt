package com.mojealterego.newgpt.domain.integration

/**
 * Connected App Fabric — scalable catalog and execution contract.
 *
 * The seed catalog is intentionally separated from verified runtime connectors.
 * Entries marked CATALOG are discoverable metadata; they do not imply that an
 * OAuth/API adapter is already authenticated or production-ready.
 */
enum class FabricCategory(
    val title: String,
    val description: String
) {
    DEVELOPERS("Dla programistów", "Kod, repozytoria, CI/CD, chmura, API, bazy danych i observability."),
    AUTHORS("Dla autorów", "Pisanie, publikacja, research, redakcja, dokumenty i wiedza."),
    RESEARCHERS("Dla badaczy", "Literatura, dane, źródła, notebooki, eksperymenty i analiza."),
    DESIGNERS("Dla projektantów", "UI/UX, grafika, prototypowanie, design systems i assety."),
    PHOTOGRAPHERS("Dla fotografów", "Asset management, RAW, retusz, portfolio, publikacja i backup."),
    VIDEO("Dla twórców wideo", "Montaż, generowanie, hosting, review, napisy i publikacja."),
    AUDIO("Dla audio i muzyki", "DAW, streaming, podcasty, mastering, biblioteki i dystrybucja."),
    MARKETING("Marketing", "SEO, kampanie, social, analityka, content i automatyzacja."),
    SALES("Sprzedaż", "CRM, leady, oferty, pipeline, outreach i customer success."),
    PRODUCTIVITY("Produktywność", "Dokumenty, zadania, kalendarze, notatki, spotkania i automatyzacje."),
    DATA("Dane i BI", "SQL, warehouse, ETL, BI, analityka i wizualizacja."),
    FINANCE("Finanse", "Księgowość, faktury, płatności, budżety i raportowanie."),
    LEGAL("Prawo i compliance", "Dokumenty prawne, e-sign, GRC, audyt i zarządzanie ryzykiem."),
    EDUCATION("Edukacja", "LMS, kursy, quizy, tutoring, materiały i klasy."),
    SCIENCE("Nauka i inżynieria", "Obliczenia, CAD, laboratoria, symulacje i dane naukowe."),
    HEALTH("Zdrowie i wellbeing", "Rezerwacje, fitness, wellness i bezpieczne dane zdrowotne."),
    TRAVEL("Podróże", "Loty, hotele, mapy, wydarzenia, wynajem i planowanie."),
    COMMERCE("Zakupy i commerce", "Sklepy, marketplace, katalogi, zamówienia i fulfillment."),
    COMMUNICATION("Komunikacja", "Email, komunikatory, VoIP, helpdesk i współpraca."),
    CLOUD_DEVOPS("Cloud i DevOps", "Cloud, deployment, kontenery, IaC, logi i monitoring."),
    SECURITY("Cybersecurity", "IAM, SIEM, secrets, vulnerability management i security operations."),
    GAMES_3D("Gry i 3D", "Game engines, asset stores, 3D, mocap i publishing."),
    AUTOMATION("Automatyzacja", "Workflow engines, webhooks, schedulery i agentic automation."),
    SOCIAL("Social", "Publikacja, społeczności, monitoring i moderacja."),
    ENTERPRISE("Enterprise", "ERP, ITSM, HR, procurement, knowledge management i governance.")
}

enum class FabricProtocol {
    APP_FUNCTIONS, OAUTH2, REST, GRAPHQL, MCP, WEBHOOK, ANDROID_INTENT,
    ACCESSIBILITY, DEEP_LINK, WEB, LOCAL_FILE, CLI
}

enum class FabricAuth {
    NONE, OAUTH2, API_KEY, TOKEN, DEVICE, ENTERPRISE_SSO, USER_APPROVAL
}

enum class FabricAvailability {
    CATALOG, ADAPTER_READY, VERIFIED, CONNECTED
}

data class FabricAppDescriptor(
    val id: String,
    val name: String,
    val category: FabricCategory,
    val tags: Set<String> = emptySet(),
    val protocols: Set<FabricProtocol> = setOf(FabricProtocol.OAUTH2, FabricProtocol.REST),
    val auth: Set<FabricAuth> = setOf(FabricAuth.OAUTH2),
    val availability: FabricAvailability = FabricAvailability.CATALOG,
    val actionFamilies: Set<String> = setOf("search", "read"),
    val source: String = "fabric-seed"
)

/**
 * Large discovery catalog. These are metadata entries, not claims of live
 * provider connectivity. Live support is determined by an installed adapter,
 * authentication state and provider permissions.
 */
object ConnectedAppFabricSeed {
    private fun p(
        id: String,
        name: String,
        category: FabricCategory,
        vararg tags: String
    ) = FabricAppDescriptor(id, name, category, tags.toSet())

    val apps: List<FabricAppDescriptor> = listOf(
        // Developers / code / repositories
        p("github", "GitHub", FabricCategory.DEVELOPERS, "git", "issues", "pull-requests", "actions"),
        p("gitlab", "GitLab", FabricCategory.DEVELOPERS, "git", "ci", "issues"),
        p("bitbucket", "Bitbucket", FabricCategory.DEVELOPERS, "git", "pipelines"),
        p("azure-devops", "Azure DevOps", FabricCategory.DEVELOPERS, "git", "boards", "pipelines"),
        p("linear", "Linear", FabricCategory.DEVELOPERS, "issues", "projects"),
        p("jira", "Jira", FabricCategory.DEVELOPERS, "issues", "project-management"),
        p("confluence", "Confluence", FabricCategory.DEVELOPERS, "docs", "knowledge"),
        p("stackoverflow", "Stack Overflow", FabricCategory.DEVELOPERS, "knowledge", "qa"),
        p("npm", "npm", FabricCategory.DEVELOPERS, "packages"),
        p("pypi", "PyPI", FabricCategory.DEVELOPERS, "python", "packages"),
        p("docker-hub", "Docker Hub", FabricCategory.DEVELOPERS, "containers"),
        p("docker", "Docker", FabricCategory.DEVELOPERS, "containers"),
        p("kubernetes", "Kubernetes", FabricCategory.DEVELOPERS, "containers", "orchestration"),
        p("vercel", "Vercel", FabricCategory.DEVELOPERS, "deploy", "web"),
        p("netlify", "Netlify", FabricCategory.DEVELOPERS, "deploy", "web"),
        p("render", "Render", FabricCategory.DEVELOPERS, "deploy", "cloud"),
        p("railway", "Railway", FabricCategory.DEVELOPERS, "deploy", "cloud"),
        p("digitalocean", "DigitalOcean", FabricCategory.DEVELOPERS, "cloud", "vps"),
        p("aws", "AWS", FabricCategory.CLOUD_DEVOPS, "cloud", "lambda", "s3"),
        p("azure", "Microsoft Azure", FabricCategory.CLOUD_DEVOPS, "cloud", "functions"),
        p("gcp", "Google Cloud", FabricCategory.CLOUD_DEVOPS, "cloud", "bigquery"),
        p("cloudflare", "Cloudflare", FabricCategory.CLOUD_DEVOPS, "dns", "workers", "security"),
        p("terraform", "Terraform", FabricCategory.CLOUD_DEVOPS, "iac"),
        p("pulumi", "Pulumi", FabricCategory.CLOUD_DEVOPS, "iac"),
        p("sentry", "Sentry", FabricCategory.CLOUD_DEVOPS, "errors", "observability"),
        p("datadog", "Datadog", FabricCategory.CLOUD_DEVOPS, "observability"),
        p("grafana", "Grafana", FabricCategory.CLOUD_DEVOPS, "metrics", "observability"),
        p("prometheus", "Prometheus", FabricCategory.CLOUD_DEVOPS, "metrics"),
        p("postman", "Postman", FabricCategory.DEVELOPERS, "api", "testing"),
        p("insomnia", "Insomnia", FabricCategory.DEVELOPERS, "api", "testing"),
        p("swagger", "Swagger", FabricCategory.DEVELOPERS, "openapi", "api"),
        p("openapi", "OpenAPI", FabricCategory.DEVELOPERS, "api", "schema"),
        p("supabase", "Supabase", FabricCategory.DEVELOPERS, "postgres", "auth", "storage"),
        p("firebase", "Firebase", FabricCategory.DEVELOPERS, "mobile", "backend"),
        p("neon", "Neon", FabricCategory.DEVELOPERS, "postgres", "serverless"),
        p("mongodb", "MongoDB", FabricCategory.DEVELOPERS, "database", "document"),
        p("redis", "Redis", FabricCategory.DEVELOPERS, "cache", "database"),
        p("snowflake", "Snowflake", FabricCategory.DATA, "warehouse", "sql"),
        p("databricks", "Databricks", FabricCategory.DATA, "lakehouse", "spark"),
        p("huggingface", "Hugging Face", FabricCategory.DEVELOPERS, "models", "datasets", "spaces"),

        // Authors / writing / publishing
        p("notion", "Notion", FabricCategory.AUTHORS, "notes", "knowledge", "docs"),
        p("google-docs", "Google Docs", FabricCategory.AUTHORS, "documents"),
        p("microsoft-word", "Microsoft Word", FabricCategory.AUTHORS, "documents"),
        p("grammarly", "Grammarly", FabricCategory.AUTHORS, "editing"),
        p("prowritingaid", "ProWritingAid", FabricCategory.AUTHORS, "editing"),
        p("hemingway", "Hemingway Editor", FabricCategory.AUTHORS, "editing"),
        p("scrivener", "Scrivener", FabricCategory.AUTHORS, "book-writing"),
        p("atticus", "Atticus", FabricCategory.AUTHORS, "publishing"),
        p("vellum", "Vellum", FabricCategory.AUTHORS, "ebooks"),
        p("reedsy", "Reedsy", FabricCategory.AUTHORS, "publishing"),
        p("kindle-direct-publishing", "Kindle Direct Publishing", FabricCategory.AUTHORS, "publishing"),
        p("draft2digital", "Draft2Digital", FabricCategory.AUTHORS, "distribution"),
        p("kobo-writing-life", "Kobo Writing Life", FabricCategory.AUTHORS, "publishing"),
        p("substack", "Substack", FabricCategory.AUTHORS, "newsletter"),
        p("medium", "Medium", FabricCategory.AUTHORS, "publishing"),
        p("wordpress", "WordPress", FabricCategory.AUTHORS, "cms"),
        p("ghost", "Ghost", FabricCategory.AUTHORS, "publishing"),
        p("beehiiv", "beehiiv", FabricCategory.AUTHORS, "newsletter"),
        p("convertkit", "Kit", FabricCategory.AUTHORS, "newsletter", "email"),
        p("pandadoc", "PandaDoc", FabricCategory.AUTHORS, "documents", "esign"),
        p("docusign", "DocuSign", FabricCategory.AUTHORS, "esign"),
        p("dropbox-paper", "Dropbox Paper", FabricCategory.AUTHORS, "docs"),
        p("evernote", "Evernote", FabricCategory.AUTHORS, "notes"),

        // Research / knowledge
        p("google-scholar", "Google Scholar", FabricCategory.RESEARCHERS, "papers"),
        p("semantic-scholar", "Semantic Scholar", FabricCategory.RESEARCHERS, "papers"),
        p("crossref", "Crossref", FabricCategory.RESEARCHERS, "metadata"),
        p("arxiv", "arXiv", FabricCategory.RESEARCHERS, "papers"),
        p("pubmed", "PubMed", FabricCategory.RESEARCHERS, "biomedical"),
        p("scopus", "Scopus", FabricCategory.RESEARCHERS, "bibliography"),
        p("web-of-science", "Web of Science", FabricCategory.RESEARCHERS, "bibliography"),
        p("researchgate", "ResearchGate", FabricCategory.RESEARCHERS, "research-network"),
        p("zotero", "Zotero", FabricCategory.RESEARCHERS, "references"),
        p("mendeley", "Mendeley", FabricCategory.RESEARCHERS, "references"),
        p("connected-papers", "Connected Papers", FabricCategory.RESEARCHERS, "literature-map"),
        p("papers-with-code", "Papers with Code", FabricCategory.RESEARCHERS, "ml"),
        p("kaggle", "Kaggle", FabricCategory.RESEARCHERS, "datasets", "notebooks"),
        p("figshare", "Figshare", FabricCategory.RESEARCHERS, "datasets"),
        p("zenodo", "Zenodo", FabricCategory.RESEARCHERS, "datasets", "archive"),
        p("orcid", "ORCID", FabricCategory.RESEARCHERS, "researcher-id"),
        p("wolfram-alpha", "Wolfram|Alpha", FabricCategory.SCIENCE, "computation"),
        p("wolfram-cloud", "Wolfram Cloud", FabricCategory.SCIENCE, "computation"),
        p("overleaf", "Overleaf", FabricCategory.RESEARCHERS, "latex"),
        p("jupyter", "Jupyter", FabricCategory.RESEARCHERS, "notebooks"),
        p("colab", "Google Colab", FabricCategory.RESEARCHERS, "notebooks", "gpu"),

        // Design
        p("figma", "Figma", FabricCategory.DESIGNERS, "ui", "prototype"),
        p("figjam", "FigJam", FabricCategory.DESIGNERS, "whiteboard"),
        p("canva", "Canva", FabricCategory.DESIGNERS, "design", "presentation"),
        p("adobe-creative-cloud", "Adobe Creative Cloud", FabricCategory.DESIGNERS, "creative"),
        p("photoshop", "Adobe Photoshop", FabricCategory.DESIGNERS, "image"),
        p("illustrator", "Adobe Illustrator", FabricCategory.DESIGNERS, "vector"),
        p("indesign", "Adobe InDesign", FabricCategory.DESIGNERS, "layout"),
        p("after-effects", "Adobe After Effects", FabricCategory.DESIGNERS, "motion"),
        p("framer", "Framer", FabricCategory.DESIGNERS, "web", "prototype"),
        p("webflow", "Webflow", FabricCategory.DESIGNERS, "web"),
        p("squarespace", "Squarespace", FabricCategory.DESIGNERS, "web"),
        p("wix", "Wix", FabricCategory.DESIGNERS, "web"),
        p("sketch", "Sketch", FabricCategory.DESIGNERS, "ui"),
        p("miro", "Miro", FabricCategory.DESIGNERS, "whiteboard"),
        p("penpot", "Penpot", FabricCategory.DESIGNERS, "open-source", "ui"),
        p("zeplin", "Zeplin", FabricCategory.DESIGNERS, "handoff"),
        p("lottie-files", "LottieFiles", FabricCategory.DESIGNERS, "animation"),
        p("dribbble", "Dribbble", FabricCategory.DESIGNERS, "portfolio"),
        p("behance", "Behance", FabricCategory.DESIGNERS, "portfolio"),
        p("picsart", "Picsart", FabricCategory.DESIGNERS, "image", "creative"),

        // Photography
        p("lightroom", "Adobe Lightroom", FabricCategory.PHOTOGRAPHERS, "raw", "catalog"),
        p("capture-one", "Capture One", FabricCategory.PHOTOGRAPHERS, "raw", "color"),
        p("photo-mechanic", "Photo Mechanic", FabricCategory.PHOTOGRAPHERS, "ingest"),
        p("pixieset", "Pixieset", FabricCategory.PHOTOGRAPHERS, "gallery", "delivery"),
        p("smugmug", "SmugMug", FabricCategory.PHOTOGRAPHERS, "portfolio"),
        p("500px", "500px", FabricCategory.PHOTOGRAPHERS, "portfolio"),
        p("flickr", "Flickr", FabricCategory.PHOTOGRAPHERS, "photo-library"),
        p("unsplash", "Unsplash", FabricCategory.PHOTOGRAPHERS, "stock"),
        p("pexels", "Pexels", FabricCategory.PHOTOGRAPHERS, "stock"),
        p("shutterstock", "Shutterstock", FabricCategory.PHOTOGRAPHERS, "stock"),
        p("getty-images", "Getty Images", FabricCategory.PHOTOGRAPHERS, "stock"),
        p("istock", "iStock", FabricCategory.PHOTOGRAPHERS, "stock"),
        p("viewbug", "ViewBug", FabricCategory.PHOTOGRAPHERS, "community"),
        p("format", "Format", FabricCategory.PHOTOGRAPHERS, "portfolio"),
        p("zenfolio", "Zenfolio", FabricCategory.PHOTOGRAPHERS, "portfolio"),
        p("photo-shelter", "PhotoShelter", FabricCategory.PHOTOGRAPHERS, "archive", "sales"),

        // Video
        p("premiere-pro", "Adobe Premiere Pro", FabricCategory.VIDEO, "editing"),
        p("davinci-resolve", "DaVinci Resolve", FabricCategory.VIDEO, "editing", "color"),
        p("final-cut-pro", "Final Cut Pro", FabricCategory.VIDEO, "editing"),
        p("capcut", "CapCut", FabricCategory.VIDEO, "editing", "social"),
        p("veed", "VEED", FabricCategory.VIDEO, "editing"),
        p("descript", "Descript", FabricCategory.VIDEO, "transcription", "editing"),
        p("runway", "Runway", FabricCategory.VIDEO, "generative-video"),
        p("kling", "Kling", FabricCategory.VIDEO, "generative-video"),
        p("pika", "Pika", FabricCategory.VIDEO, "generative-video"),
        p("luma", "Luma", FabricCategory.VIDEO, "generative-video", "3d"),
        p("sora", "Sora", FabricCategory.VIDEO, "generative-video"),
        p("heygen", "HeyGen", FabricCategory.VIDEO, "avatar", "video"),
        p("synthesia", "Synthesia", FabricCategory.VIDEO, "avatar"),
        p("veo", "Veo", FabricCategory.VIDEO, "generative-video"),
        p("youtube", "YouTube", FabricCategory.VIDEO, "publishing"),
        p("vimeo", "Vimeo", FabricCategory.VIDEO, "hosting"),
        p("frameio", "Frame.io", FabricCategory.VIDEO, "review"),
        p("wistia", "Wistia", FabricCategory.VIDEO, "hosting", "analytics"),

        // Audio / music
        p("spotify", "Spotify", FabricCategory.AUDIO, "streaming"),
        p("apple-music", "Apple Music", FabricCategory.AUDIO, "streaming"),
        p("soundcloud", "SoundCloud", FabricCategory.AUDIO, "publishing"),
        p("bandcamp", "Bandcamp", FabricCategory.AUDIO, "distribution"),
        p("spotify-for-artists", "Spotify for Artists", FabricCategory.AUDIO, "artist"),
        p("distrokid", "DistroKid", FabricCategory.AUDIO, "distribution"),
        p("tunecore", "TuneCore", FabricCategory.AUDIO, "distribution"),
        p("elevenlabs", "ElevenLabs", FabricCategory.AUDIO, "voice"),
        p("suno", "Suno", FabricCategory.AUDIO, "music-generation"),
        p("udio", "Udio", FabricCategory.AUDIO, "music-generation"),
        p("audiomack", "Audiomack", FabricCategory.AUDIO, "streaming"),
        p("podbean", "Podbean", FabricCategory.AUDIO, "podcast"),
        p("buzzsprout", "Buzzsprout", FabricCategory.AUDIO, "podcast"),
        p("riverside", "Riverside", FabricCategory.AUDIO, "recording"),
        p("descript-audio", "Descript Audio", FabricCategory.AUDIO, "editing"),
        p("adobe-audition", "Adobe Audition", FabricCategory.AUDIO, "editing"),

        // Marketing / analytics
        p("google-analytics", "Google Analytics", FabricCategory.MARKETING, "analytics"),
        p("google-search-console", "Google Search Console", FabricCategory.MARKETING, "seo"),
        p("semrush", "Semrush", FabricCategory.MARKETING, "seo"),
        p("ahrefs", "Ahrefs", FabricCategory.MARKETING, "seo"),
        p("moz", "Moz", FabricCategory.MARKETING, "seo"),
        p("surfer", "Surfer", FabricCategory.MARKETING, "seo"),
        p("hubspot", "HubSpot", FabricCategory.MARKETING, "crm", "marketing"),
        p("mailchimp", "Mailchimp", FabricCategory.MARKETING, "email"),
        p("klaviyo", "Klaviyo", FabricCategory.MARKETING, "email", "commerce"),
        p("brevo", "Brevo", FabricCategory.MARKETING, "email"),
        p("activecampaign", "ActiveCampaign", FabricCategory.MARKETING, "automation"),
        p("meta-ads", "Meta Ads", FabricCategory.MARKETING, "ads"),
        p("google-ads", "Google Ads", FabricCategory.MARKETING, "ads"),
        p("linkedin-campaign-manager", "LinkedIn Campaign Manager", FabricCategory.MARKETING, "ads"),
        p("tiktok-ads", "TikTok Ads", FabricCategory.MARKETING, "ads"),
        p("buffer", "Buffer", FabricCategory.MARKETING, "social"),
        p("hootsuite", "Hootsuite", FabricCategory.MARKETING, "social"),
        p("sprout-social", "Sprout Social", FabricCategory.MARKETING, "social"),
        p("later", "Later", FabricCategory.MARKETING, "social"),

        // Sales / CRM
        p("salesforce", "Salesforce", FabricCategory.SALES, "crm"),
        p("pipedrive", "Pipedrive", FabricCategory.SALES, "crm"),
        p("close", "Close", FabricCategory.SALES, "crm"),
        p("zoho-crm", "Zoho CRM", FabricCategory.SALES, "crm"),
        p("monday-sales", "monday sales CRM", FabricCategory.SALES, "crm"),
        p("freshsales", "Freshsales", FabricCategory.SALES, "crm"),
        p("apollo", "Apollo", FabricCategory.SALES, "prospecting"),
        p("lemlist", "lemlist", FabricCategory.SALES, "outreach"),
        p("outreach", "Outreach", FabricCategory.SALES, "sales-engagement"),
        p("gong", "Gong", FabricCategory.SALES, "conversation-intelligence"),
        p("intercom", "Intercom", FabricCategory.SALES, "support"),
        p("zendesk", "Zendesk", FabricCategory.SALES, "support"),
        p("freshdesk", "Freshdesk", FabricCategory.SALES, "support"),

        // Productivity / office
        p("gmail", "Gmail", FabricCategory.PRODUCTIVITY, "email"),
        p("outlook", "Microsoft Outlook", FabricCategory.PRODUCTIVITY, "email"),
        p("google-calendar", "Google Calendar", FabricCategory.PRODUCTIVITY, "calendar"),
        p("outlook-calendar", "Outlook Calendar", FabricCategory.PRODUCTIVITY, "calendar"),
        p("google-drive", "Google Drive", FabricCategory.PRODUCTIVITY, "storage"),
        p("onedrive", "OneDrive", FabricCategory.PRODUCTIVITY, "storage"),
        p("sharepoint", "SharePoint", FabricCategory.ENTERPRISE, "documents"),
        p("dropbox", "Dropbox", FabricCategory.PRODUCTIVITY, "storage"),
        p("box", "Box", FabricCategory.PRODUCTIVITY, "storage"),
        p("slack", "Slack", FabricCategory.COMMUNICATION, "chat"),
        p("microsoft-teams", "Microsoft Teams", FabricCategory.COMMUNICATION, "chat", "meetings"),
        p("zoom", "Zoom", FabricCategory.COMMUNICATION, "meetings"),
        p("google-meet", "Google Meet", FabricCategory.COMMUNICATION, "meetings"),
        p("todoist", "Todoist", FabricCategory.PRODUCTIVITY, "tasks"),
        p("asana", "Asana", FabricCategory.PRODUCTIVITY, "projects"),
        p("trello", "Trello", FabricCategory.PRODUCTIVITY, "kanban"),
        p("clickup", "ClickUp", FabricCategory.PRODUCTIVITY, "projects"),
        p("monday", "monday.com", FabricCategory.PRODUCTIVITY, "projects"),
        p("airtable", "Airtable", FabricCategory.PRODUCTIVITY, "database"),
        p("coda", "Coda", FabricCategory.PRODUCTIVITY, "docs"),
        p("obsidian", "Obsidian", FabricCategory.PRODUCTIVITY, "knowledge"),
        p("roam", "Roam Research", FabricCategory.RESEARCHERS, "knowledge"),
        p("readwise", "Readwise", FabricCategory.RESEARCHERS, "reading"),
        p("otter", "Otter.ai", FabricCategory.PRODUCTIVITY, "transcription"),
        p("granola", "Granola", FabricCategory.PRODUCTIVITY, "meetings"),

        // Data / BI
        p("power-bi", "Power BI", FabricCategory.DATA, "bi"),
        p("tableau", "Tableau", FabricCategory.DATA, "bi"),
        p("looker", "Looker", FabricCategory.DATA, "bi"),
        p("metabase", "Metabase", FabricCategory.DATA, "bi"),
        p("superset", "Apache Superset", FabricCategory.DATA, "bi"),
        p("mode", "Mode", FabricCategory.DATA, "analytics"),
        p("hex", "Hex", FabricCategory.DATA, "notebooks"),
        p("dbt", "dbt", FabricCategory.DATA, "transform"),
        p("fivetran", "Fivetran", FabricCategory.DATA, "etl"),
        p("airbyte", "Airbyte", FabricCategory.DATA, "etl"),
        p("segment", "Segment", FabricCategory.DATA, "customer-data"),
        p("mixpanel", "Mixpanel", FabricCategory.DATA, "product-analytics"),
        p("amplitude", "Amplitude", FabricCategory.DATA, "product-analytics"),
        p("posthog", "PostHog", FabricCategory.DATA, "product-analytics"),
        p("sql", "SQL Database Fabric", FabricCategory.DATA, "sql"),
        p("postgres", "PostgreSQL", FabricCategory.DATA, "database"),
        p("mysql", "MySQL", FabricCategory.DATA, "database"),
        p("sqlite", "SQLite", FabricCategory.DATA, "database"),

        // Finance
        p("stripe", "Stripe", FabricCategory.FINANCE, "payments"),
        p("paypal", "PayPal", FabricCategory.FINANCE, "payments"),
        p("wise", "Wise", FabricCategory.FINANCE, "payments"),
        p("quickbooks", "QuickBooks", FabricCategory.FINANCE, "accounting"),
        p("xero", "Xero", FabricCategory.FINANCE, "accounting"),
        p("freshbooks", "FreshBooks", FabricCategory.FINANCE, "accounting"),
        p("wave", "Wave", FabricCategory.FINANCE, "accounting"),
        p("chargebee", "Chargebee", FabricCategory.FINANCE, "billing"),
        p("paddle", "Paddle", FabricCategory.FINANCE, "billing"),
        p("revolut-business", "Revolut Business", FabricCategory.FINANCE, "banking"),
        p("wise-business", "Wise Business", FabricCategory.FINANCE, "banking"),
        p("expensify", "Expensify", FabricCategory.FINANCE, "expenses"),

        // Legal / compliance
        p("docusign-legal", "DocuSign", FabricCategory.LEGAL, "esign"),
        p("adobe-sign", "Adobe Acrobat Sign", FabricCategory.LEGAL, "esign"),
        p("clio", "Clio", FabricCategory.LEGAL, "legal-practice"),
        p("ironclad", "Ironclad", FabricCategory.LEGAL, "contracts"),
        p("docusign-clm", "DocuSign CLM", FabricCategory.LEGAL, "contracts"),
        p("notarize", "Notarize", FabricCategory.LEGAL, "notary"),
        p("vanta", "Vanta", FabricCategory.LEGAL, "compliance"),
        p("drata", "Drata", FabricCategory.LEGAL, "compliance"),
        p("one-trust", "OneTrust", FabricCategory.LEGAL, "privacy"),
        p("servicenow-grc", "ServiceNow GRC", FabricCategory.LEGAL, "grc"),

        // Education
        p("moodle", "Moodle", FabricCategory.EDUCATION, "lms"),
        p("canvas-lms", "Canvas LMS", FabricCategory.EDUCATION, "lms"),
        p("blackboard", "Blackboard", FabricCategory.EDUCATION, "lms"),
        p("coursera", "Coursera", FabricCategory.EDUCATION, "courses"),
        p("udemy", "Udemy", FabricCategory.EDUCATION, "courses"),
        p("edx", "edX", FabricCategory.EDUCATION, "courses"),
        p("duolingo", "Duolingo", FabricCategory.EDUCATION, "language"),
        p("quizlet", "Quizlet", FabricCategory.EDUCATION, "flashcards"),
        p("kahoot", "Kahoot!", FabricCategory.EDUCATION, "quiz"),
        p("classroom", "Google Classroom", FabricCategory.EDUCATION, "classroom"),

        // Science / engineering
        p("matlab", "MATLAB", FabricCategory.SCIENCE, "numerical"),
        p("simulink", "Simulink", FabricCategory.SCIENCE, "simulation"),
        p("ansys", "Ansys", FabricCategory.SCIENCE, "simulation", "engineering"),
        p("comsol", "COMSOL", FabricCategory.SCIENCE, "simulation"),
        p("solidworks", "SOLIDWORKS", FabricCategory.SCIENCE, "cad"),
        p("autocad", "AutoCAD", FabricCategory.SCIENCE, "cad"),
        p("fusion-360", "Fusion 360", FabricCategory.SCIENCE, "cad"),
        p("freecad", "FreeCAD", FabricCategory.SCIENCE, "cad"),
        p("blender", "Blender", FabricCategory.GAMES_3D, "3d"),
        p("unity", "Unity", FabricCategory.GAMES_3D, "game-engine"),
        p("unreal-engine", "Unreal Engine", FabricCategory.GAMES_3D, "game-engine"),
        p("godot", "Godot", FabricCategory.GAMES_3D, "game-engine"),
        p("autodesk-maya", "Autodesk Maya", FabricCategory.GAMES_3D, "3d"),
        p("autodesk-3ds-max", "3ds Max", FabricCategory.GAMES_3D, "3d"),

        // Health / fitness
        p("peloton", "Peloton", FabricCategory.HEALTH, "fitness"),
        p("strava", "Strava", FabricCategory.HEALTH, "fitness"),
        p("fitbit", "Fitbit", FabricCategory.HEALTH, "fitness"),
        p("garmin", "Garmin", FabricCategory.HEALTH, "fitness"),
        p("myfitnesspal", "MyFitnessPal", FabricCategory.HEALTH, "nutrition"),
        p("zocdoc", "Zocdoc", FabricCategory.HEALTH, "appointments"),
        p("calendly-health", "Healthcare Scheduling Fabric", FabricCategory.HEALTH, "appointments"),

        // Travel / experiences
        p("airbnb", "Airbnb", FabricCategory.TRAVEL, "lodging"),
        p("booking", "Booking.com", FabricCategory.TRAVEL, "lodging"),
        p("expedia", "Expedia", FabricCategory.TRAVEL, "travel"),
        p("tripadvisor", "Tripadvisor", FabricCategory.TRAVEL, "travel"),
        p("skyscanner", "Skyscanner", FabricCategory.TRAVEL, "flights"),
        p("kayak", "KAYAK", FabricCategory.TRAVEL, "flights"),
        p("getyourguide", "GetYourGuide", FabricCategory.TRAVEL, "activities"),
        p("opentable", "OpenTable", FabricCategory.TRAVEL, "restaurants"),
        p("uber", "Uber", FabricCategory.TRAVEL, "mobility"),
        p("lyft", "Lyft", FabricCategory.TRAVEL, "mobility"),
        p("rentalcars", "Rentalcars.com", FabricCategory.TRAVEL, "cars"),

        // Commerce
        p("shopify", "Shopify", FabricCategory.COMMERCE, "store"),
        p("woocommerce", "WooCommerce", FabricCategory.COMMERCE, "store"),
        p("etsy", "Etsy", FabricCategory.COMMERCE, "marketplace"),
        p("amazon", "Amazon", FabricCategory.COMMERCE, "commerce"),
        p("ebay", "eBay", FabricCategory.COMMERCE, "marketplace"),
        p("allegro", "Allegro", FabricCategory.COMMERCE, "marketplace"),
        p("empik", "Empik", FabricCategory.COMMERCE, "books", "commerce"),
        p("inpost", "InPost", FabricCategory.COMMERCE, "delivery"),
        p("ups", "UPS", FabricCategory.COMMERCE, "logistics"),
        p("fedex", "FedEx", FabricCategory.COMMERCE, "logistics"),
        p("dhl", "DHL", FabricCategory.COMMERCE, "logistics"),

        // Communication
        p("whatsapp", "WhatsApp", FabricCategory.COMMUNICATION, "messaging"),
        p("telegram", "Telegram", FabricCategory.COMMUNICATION, "messaging"),
        p("signal", "Signal", FabricCategory.COMMUNICATION, "messaging"),
        p("discord", "Discord", FabricCategory.COMMUNICATION, "community"),
        p("messenger", "Messenger", FabricCategory.COMMUNICATION, "messaging"),
        p("twilio", "Twilio", FabricCategory.COMMUNICATION, "sms", "voice"),
        p("sendgrid", "SendGrid", FabricCategory.COMMUNICATION, "email"),
        p("mailgun", "Mailgun", FabricCategory.COMMUNICATION, "email"),
        p("postmark", "Postmark", FabricCategory.COMMUNICATION, "email"),
        p("vonage", "Vonage", FabricCategory.COMMUNICATION, "voice", "sms"),

        // Security
        p("okta", "Okta", FabricCategory.SECURITY, "iam", "sso"),
        p("auth0", "Auth0", FabricCategory.SECURITY, "iam"),
        p("1password", "1Password", FabricCategory.SECURITY, "secrets"),
        p("bitwarden", "Bitwarden", FabricCategory.SECURITY, "secrets"),
        p("crowdstrike", "CrowdStrike", FabricCategory.SECURITY, "endpoint"),
        p("sentinelone", "SentinelOne", FabricCategory.SECURITY, "endpoint"),
        p("splunk", "Splunk", FabricCategory.SECURITY, "siem"),
        p("elastic", "Elastic", FabricCategory.SECURITY, "siem", "search"),
        p("wiz", "Wiz", FabricCategory.SECURITY, "cloud-security"),
        p("snyk", "Snyk", FabricCategory.SECURITY, "code-security"),
        p("sonarqube", "SonarQube", FabricCategory.SECURITY, "code-quality"),

        // Automation
        p("zapier", "Zapier", FabricCategory.AUTOMATION, "workflow"),
        p("make", "Make", FabricCategory.AUTOMATION, "workflow"),
        p("n8n", "n8n", FabricCategory.AUTOMATION, "workflow", "self-hosted"),
        p("activepieces", "Activepieces", FabricCategory.AUTOMATION, "workflow"),
        p("power-automate", "Power Automate", FabricCategory.AUTOMATION, "workflow"),
        p("ifttt", "IFTTT", FabricCategory.AUTOMATION, "automation"),
        p("webhooks", "Webhooks", FabricCategory.AUTOMATION, "events"),
        p("cron", "Cron Scheduler", FabricCategory.AUTOMATION, "schedule"),

        // Enterprise
        p("servicenow", "ServiceNow", FabricCategory.ENTERPRISE, "itsm", "workflow"),
        p("sap", "SAP", FabricCategory.ENTERPRISE, "erp"),
        p("oracle", "Oracle", FabricCategory.ENTERPRISE, "erp"),
        p("workday", "Workday", FabricCategory.ENTERPRISE, "hr"),
        p("bamboohr", "BambooHR", FabricCategory.ENTERPRISE, "hr"),
        p("rippling", "Rippling", FabricCategory.ENTERPRISE, "hr"),
        p("deel", "Deel", FabricCategory.ENTERPRISE, "hr", "global-employment"),
        p("greenhouse", "Greenhouse", FabricCategory.ENTERPRISE, "recruiting"),
        p("lever", "Lever", FabricCategory.ENTERPRISE, "recruiting"),
        p("coupa", "Coupa", FabricCategory.ENTERPRISE, "procurement"),
        p("docusign-enterprise", "DocuSign Enterprise", FabricCategory.ENTERPRISE, "esign"),

        // Social / publishing
        p("instagram", "Instagram", FabricCategory.SOCIAL, "social"),
        p("facebook", "Facebook", FabricCategory.SOCIAL, "social"),
        p("linkedin", "LinkedIn", FabricCategory.SOCIAL, "professional"),
        p("x", "X", FabricCategory.SOCIAL, "social"),
        p("tiktok", "TikTok", FabricCategory.SOCIAL, "video"),
        p("threads", "Threads", FabricCategory.SOCIAL, "social"),
        p("reddit", "Reddit", FabricCategory.SOCIAL, "community"),
        p("mastodon", "Mastodon", FabricCategory.SOCIAL, "federated"),
        p("bluesky", "Bluesky", FabricCategory.SOCIAL, "social"),
        p("pinterest", "Pinterest", FabricCategory.SOCIAL, "visual"),
        p("tumblr", "Tumblr", FabricCategory.SOCIAL, "publishing")
    )
}

/**
 * Capacity and routing policy for the fabric. The catalog can grow by loading
 * signed provider manifests; the app must not hard-code a fixed provider count.
 */
object ConnectedAppFabricPolicy {
    const val TARGET_CATALOG_CAPACITY = 10_000
    const val MAX_ACTIONS_PER_APP = 512
    const val MAX_SEARCH_RESULTS = 100
    const val MAX_PARALLEL_CONNECTORS = 16
    const val MAX_WORKFLOW_STEPS = 128

    val primaryCategories = FabricCategory.entries.toList()

    fun search(query: String): List<FabricAppDescriptor> {
        if (query.isBlank()) return ConnectedAppFabricSeed.apps.take(MAX_SEARCH_RESULTS)
        return ConnectedAppFabricSeed.apps
            .asSequence()
            .map { app ->
                val haystack = listOf(app.name, app.id, app.category.title, app.tags.joinToString(" ")).joinToString(" ")
                val score = haystack.lowercase().let { text ->
                    when {
                        app.name.equals(query, true) -> 100
                        app.name.contains(query, true) -> 80
                        text.contains(query.lowercase()) -> 40
                        else -> 0
                    }
                }
                app to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(MAX_SEARCH_RESULTS)
            .map { it.first }
            .toList()
    }

    fun byCategory(category: FabricCategory): List<FabricAppDescriptor> =
        ConnectedAppFabricSeed.apps.filter { it.category == category }
}
