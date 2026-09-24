# NewGPT — Architecture Decision Log

## Purpose
Native Android AI client with cloud and local inference, designed around a provider-neutral domain layer and a local-first conversation store.

## 1. Platform
- Kotlin / Android.
- Jetpack Compose for presentation.
- Android API 35 target; compile SDK 36.
- Native C++/JNI boundary for local GGUF inference.

## 2. Presentation
- MVI-style immutable UI state.
- ViewModels own orchestration of UI actions.
- Compose screens are separated from domain/data implementations.
- Visual language: obsidian black, metallic 24K gold, ivory/titanium neutrals and restrained burgundy error accents.
- Agent area is separated from the normal chat and includes Agent Builder.

## 3. Domain
- `ProviderType` isolates provider selection.
- `AiInferenceStrategy` is the provider-neutral streaming contract and now accepts an optional agent system prompt.
- `ChatRepository` is the domain boundary for conversation persistence and inference.
- `AgentDefinition` is the portable agent contract: identity, system prompt, skills, declared tools, handoffs and enabled state.
- `AgentRuntime` executes single-agent turns and deterministic handoff pipelines.

## 4. Data
- Room is the local source of truth for messages.
- Provider credentials/configuration are stored locally in encrypted preferences.
- Agent definitions are stored locally in Android DataStore; they contain no API secrets.
- Ktor/OkHttp handles remote provider transport.
- GGUF files are copied into app-private storage before native loading.

## 5. Providers
- OpenAI.
- Anthropic.
- Gemini.
- OpenAI-compatible endpoints.
- Local GGUF through llama.cpp.

## 6. Local inference
- llama.cpp v0.4.1 is pinned in CMake.
- Native code initializes the llama backend once.
- Model loading, tokenization, context creation, decoding and sampling remain in C++.
- Generated pieces cross JNI through a callback and are exposed to Kotlin as a Flow.
- The selected GGUF model remains loaded between turns.
- Agent system prompts are passed into the GGUF prompt builder.

## 7. Agent System
The application now contains a built-in registry with Coordinator, Researcher, Architect, Coder, Writer, WDA Photo and Mobile Operator.

The Agent Builder supports:
- create,
- edit,
- enable/disable,
- delete,
- reset to defaults,
- system prompt editing,
- skills/tools declarations,
- handoff configuration.

A handoff pipeline executes the selected agent followed by its declared handoffs. Each later agent receives the preceding result as input for verification and improvement.

The `tools[]` field is declarative metadata, not an authorization mechanism. Real privileged capabilities must be connected through explicit adapters with their own validation and approval boundaries.

## 8. Security
- API keys are not committed to the repository.
- Credentials are masked in the settings UI.
- EncryptedSharedPreferences uses Android Keystore-backed key material.
- `.gitignore` excludes local properties, keystores and environment files.
- Agent definitions do not grant system permissions.
- The Mobile Operator agent is constrained by prompt-level safety guidance; Android Accessibility or notification actions are not silently enabled by the agent registry.

## 9. Reliability
- Remote providers validate non-2xx responses.
- Generation requests are serialized in the ViewModel.
- Agent conversations use separate Room conversation IDs.
- Clear-history action is blocked during generation.
- Agent handoff pipelines are deterministic in declared order.

## 10. Build
- AGP 8.13.2.
- Gradle 8.13.
- JDK 17.
- NDK 29.0.13113456.
- CMake 3.31.6.
- Room 2.8.5.

## 11. Verification boundary
The repository contains CI configuration for debug assembly and unit tests. The ChatGPT execution environment cannot resolve GitHub dependency hosts, so an actual APK build cannot be truthfully marked as passed from this environment. The authoritative final verification step is the GitHub Actions run or a local Android Studio/Gradle build.

## 12. Source-derived design decisions
The agent layer was expanded using concepts from the supplied source packages:
- `OmniMAS-Local-Android`: Planner → Grounding → Executor → Supervisor separation and local memory concept.
- `omni_mobile_agent_FINAL_V14`: explicit runtime boundary, verification gate and auditable evolutionary workflow.
- `gguf_agent_studio_apex_extreme_overdrive_pack`: workflow/DAG, policy-as-code, event-oriented execution and agent graph concepts.
- `gguf_agent_studio_apex_chatgpt_agents_installer_pack-6`: agent registry, per-agent chat and model/agent selection UX.

Only concepts supported by the supplied source material were carried into this Android implementation. No undocumented external endpoint or privileged Android operation was added.

## 13. Explicit non-decisions
- No API key is embedded in source.
- No cloud backend is required for local GGUF mode.
- No proprietary model weights are bundled in the repository.
- No agent definition automatically receives filesystem, network, Accessibility or notification privileges.
- No fabricated successful build result is recorded.
