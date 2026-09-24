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

## 3. Domain
- `ProviderType` isolates provider selection.
- `AiInferenceStrategy` is the provider-neutral streaming contract.
- `ChatRepository` is the domain boundary for conversation persistence and inference.

## 4. Data
- Room is the local source of truth for messages.
- Provider credentials/configuration are stored locally in encrypted preferences.
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

## 7. Security
- API keys are not committed to the repository.
- Credentials are masked in the settings UI.
- EncryptedSharedPreferences uses Android Keystore-backed key material.
- `.gitignore` excludes local properties, keystores and environment files.

## 8. Reliability
- Remote providers validate non-2xx responses.
- Generation requests are serialized in the ViewModel.
- Chat history is retained locally.
- Clear-history action is blocked during generation.

## 9. Build
- AGP 8.13.2.
- Gradle 8.13.
- JDK 17.
- NDK 29.0.13113456.
- CMake 3.31.6.
- Room 2.8.5.

## 10. Verification boundary
The repository contains CI configuration for debug assembly and unit tests. The ChatGPT execution environment cannot resolve GitHub dependency hosts, so an actual APK build cannot be truthfully marked as passed from this environment. The authoritative final verification step is the GitHub Actions run or a local Android Studio/Gradle build.

## 11. Explicit non-decisions
- No API key is embedded in source.
- No cloud backend is required for local GGUF mode.
- No proprietary model weights are bundled in the repository.
- No fabricated successful build result is recorded.