# NewGPT Connected Apps Fabric

## Cel
NewGPT treats cross-app integration as a first-class agent capability rather than a list of hard-coded one-off integrations.

## Reference baseline
The September 2026 Google Gemini Connected Apps rollout names Airtable, Linear, monday.com, PandaDoc, Wispr AI and Zoho for productivity; Adobe, Picsart, Squarespace and Webflow for creativity; and Apartments.com, Experian, Peloton and SeatGeek for lifestyle. Google also documents connected Google services, WhatsApp, Phone, Messages, Maps, YouTube, Google Workspace and Google Play. These are reference capabilities, not claims that NewGPT already has provider credentials or production access.

## NewGPT architecture
- `ConnectedAppRegistry` — normalized catalog of providers and actions.
- `IntegrationKind.APP_FUNCTIONS` — Android 16+ on-device app-function path.
- `IntegrationKind.OAUTH_API` — provider API/OAuth path.
- `IntegrationKind.ANDROID_INTENT` — explicit Android intent path where supported.
- `IntegrationKind.ACCESSIBILITY` — controlled UI automation fallback.
- `IntegrationKind.WEB` — browser workflow fallback.
- `ActionRisk` — read-only, write, sensitive.
- confirmation is required by default for write and sensitive operations.
- `AndroidAppFunctionsBridge` discovers and invokes AppFunctions when the platform and permissions permit it.

## Security model
1. Agent selects an app and action.
2. CapabilityBroker evaluates policy.
3. Risk level determines whether confirmation is required.
4. Connector executes through the narrowest available adapter.
5. Execution is auditable.
6. Destructive, financial, messaging and other consequential actions remain behind explicit approval.

## Beyond Gemini
The registry is deliberately provider-neutral. The target is not to copy Gemini one-to-one, but to combine AppFunctions, MCP, OAuth APIs, Android intents, controlled accessibility and browser automation behind one policy and evidence boundary.

## Current status
- Connected app catalog: `IMPLEMENTED`.
- Android AppFunctions bridge: `EXPERIMENTAL` because Android documents AppFunctions as experimental and availability is platform/permission dependent.
- Provider OAuth adapters: `PARTIAL`.
- Universal execution across every catalog provider: `NOT YET VERIFIED`.


## Connected App Fabric MAX

The fabric is now split into two layers:

### 1. Operational connector layer
`ConnectedAppRegistry` contains explicit provider contracts that NewGPT can reason about as executable integrations. These entries are the only source that may be treated as operational.

### 2. Discovery catalog layer
`ConnectedAppFabricSeed` provides a much broader discovery taxonomy. It currently covers categories for:
- Developers
- Authors
- Researchers
- Designers
- Photographers
- Video creators
- Audio and music
- Marketing
- Sales
- Productivity
- Data and BI
- Finance
- Legal and compliance
- Education
- Science and engineering
- Health and wellbeing
- Travel
- Commerce
- Communication
- Cloud and DevOps
- Cybersecurity
- Games and 3D
- Automation
- Social
- Enterprise

This catalog is metadata-only unless a matching adapter, authentication state and provider permissions exist.

### Fabric protocols

The routing contract supports:
- Android AppFunctions
- OAuth 2.0
- REST
- GraphQL
- MCP
- Webhooks/events
- Android Intents
- controlled Accessibility
- deep links
- browser/web workflows
- local files
- CLI/agent runtimes

### Fabric control plane

The intended execution path is:

Intent -> Discovery -> Capability Match -> Account/Auth -> Policy -> Risk -> Approval -> Adapter Selection -> Execution -> Evidence -> Audit

Adapter selection should prefer the narrowest and most structured transport available:
1. native AppFunction
2. verified provider API
3. MCP tool
4. explicit Android Intent
5. deep link
6. controlled Accessibility
7. browser/web fallback

A fallback must never silently escalate privilege. The same action risk and approval policy applies regardless of transport.

### Scale target

`ConnectedAppFabricPolicy` defines a 10,000-entry catalog capacity target, 512 actions per app, 16 parallel connectors and 128 workflow steps. These are engineering limits/targets, not a claim that 10,000 live provider adapters currently exist.

The catalog is intentionally manifest-driven so additional providers can be loaded without changing the core agent runtime. A future signed-manifest loader should validate:
- provider identity
- version
- action schemas
- OAuth scopes
- data classification
- risk classification
- confirmation requirements
- webhook/event declarations
- privacy policy URL
- adapter integrity/signature

### Why this architecture matters

Current connected-app systems are constrained by provider availability, account authorization, workspace controls and per-app capabilities. NewGPT therefore separates discovery from execution instead of pretending that a large directory automatically means thousands of working integrations. OpenAI's current documentation likewise distinguishes apps as connections to external services and notes that availability and actions depend on the app, account, workspace and permissions. [Source: OpenAI Help Center, Connected apps in ChatGPT.]

OpenAI also documents app-directory discovery, @-invocation/tool-menu invocation and action approval controls. NewGPT's fabric is designed to expose those same concepts while adding a provider-neutral routing layer across AppFunctions, APIs, MCP and Android-native transports. [Source: OpenAI, Developers can now submit apps to ChatGPT; OpenAI Help Center, admin controls for apps.]
