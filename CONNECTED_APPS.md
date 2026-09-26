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
