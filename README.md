# NewGPT

Native Android AI client with cloud and local inference, now extended with a persistent agent runtime and Agent Builder.

## Cognitive OS foundation\n\nThe current implementation includes bitemporal memory, a cognitive decision-cycle layer, capability/policy gating, hybrid RAG orchestration primitives, bounded mutation/evolution primitives, and a Cognitive Control Center. Experimental modules remain isolated until benchmarked.\n\n## Implemented foundation
- Kotlin + Jetpack Compose
- Clean Architecture + MVI-style state
- Room Single Source of Truth for chat history
- Ktor transport layer
- Secure local provider configuration using Android Keystore-backed encrypted preferences
- Multi-provider strategy layer: OpenAI, Anthropic, Google Gemini, OpenAI-compatible endpoints, Local GGUF
- Android Storage Access Framework import for .gguf model files
- Android 15 edge-to-edge entry point
- Native JNI boundary isolated from domain/data layers
- GitHub Actions debug build and unit-test pipeline

## Agent system
NewGPT now includes a local agent registry and runtime.

Built-in agents:
- **Coordinator** — planning, delegation and verification
- **Researcher** — evidence and comparison
- **Architect** — system architecture and security
- **Coder** — implementation and testing
- **Writer** — documentation and editing
- **WDA Photo** — photography and prompt engineering
- **Mobile Operator** — Android UI task planning with explicit safety constraints

Agent definitions contain:
- identity and description,
- system prompt,
- skills,
- declared tools,
- handoffs,
- enabled state.

The Agent Builder can create, edit, enable/disable, delete and reset agents. Definitions are stored locally in Android DataStore.

### Handoff pipeline

Agents can declare other agents as handoffs. The Agent screen can execute the selected agent followed by its declared handoffs. Each subsequent agent receives the previous result for verification and improvement.

```
User task
   ↓
Coordinator
   ↓
Researcher / Architect / Coder / Writer
   ↓
verified result
```

The `tools[]` field is metadata only. It does not grant filesystem, network, Accessibility or notification privileges. Privileged integrations must be connected through explicit adapters with their own validation and approval boundary.

## Architecture

```
presentation/
  chat/
  agents/
  settings/
  theme/

domain/
  agent/
  model/
  repository/
  strategy/
  usecase/

data/
  local/
    Room
    DataStore
    secure settings
    GGUF/JNI
  remote/
    provider strategies

di/
```

## Provider flow

Normal chat:

```
Compose UI → MVI ViewModel → UseCase → ChatRepository → AiInferenceStrategy
→ OpenAI | Anthropic | Gemini | OpenAI-compatible | Local GGUF
```

Agent chat:

```
Agents UI → AgentsViewModel → AgentRuntime → AgentRepository + ChatRepository
→ provider strategy with native system prompt
```

## Local GGUF status

The repository pins llama.cpp v0.4.1 through CMake FetchContent and links it into the Android JNI library. The Kotlin layer imports a GGUF file into app-private storage; the native layer loads the model, tokenizes the prompt, runs llama.cpp decoding and streams generated pieces back through JNI callbacks.

## Security

API keys are never committed to source. They are stored locally through an encrypted preferences container backed by Android Keystore. Agent definitions contain no secrets and do not grant privileges.

## Build

Requirements: JDK 17, Android SDK API 36, NDK 29.0.13113456, CMake 3.31.6 and Gradle 8.13.

Run:

```bash
gradle assembleDebug
gradle testDebugUnitTest
```

## Documentation

- `README2.md` — architecture decision log
- `AGENTS.md` — agent runtime and Agent Builder architecture

## Source basis

The agent layer was expanded using concepts from the supplied source packages:
- `OmniMAS-Local-Android`
- `omni_mobile_agent_FINAL_V14`
- `gguf_agent_studio_apex_extreme_overdrive_pack`
- `gguf_agent_studio_apex_chatgpt_agents_installer_pack-6`

Only concepts supported by those supplied materials were carried into the Android implementation.

## Verification boundary

The repository contains CI configuration for debug assembly and unit tests. Local APK compilation still needs to be executed in an Android/Gradle environment because the ChatGPT execution environment cannot resolve GitHub dependency hosts. No successful build result is claimed without an actual build.
