# NewGPT

Native Android AI chat client built from the supplied NewGPT architecture specification.

## Implemented foundation
- Kotlin + Jetpack Compose
- Clean Architecture + MVI-style state
- Room Single Source of Truth for chat history
- Ktor transport layer
- Secure local provider configuration using Android Keystore-backed encrypted preferences
- Multi-provider strategy layer: OpenAI, Anthropic, Google Gemini, OpenAI-compatible endpoints, Local GGUF bridge
- Android Storage Access Framework import for .gguf model files
- Android 15 edge-to-edge entry point
- Native JNI boundary isolated from domain/data layers
- GitHub Actions debug build and unit-test pipeline

## Architecture
presentation/ → chat, settings, theme
domain/ → model, repository, strategy, usecase
data/ → local Room/security/GGUF, remote providers, repository
di/ → dependency injection

## Provider flow
Compose UI → MVI ViewModel → UseCase → ChatRepository → AiInferenceStrategy → OpenAI | Anthropic | Gemini | OpenAI-compatible | Local GGUF

## Local GGUF status
The repository now pins llama.cpp v0.4.1 through CMake FetchContent and links it into the Android JNI library. The Kotlin layer imports a GGUF file into app-private storage; the native layer loads the model, tokenizes the prompt, runs llama.cpp decoding and streams generated pieces back through JNI callbacks. The CMake configuration follows the upstream Android guidance: arm64-v8a uses KleidiAI when available, while GGML_NATIVE/OpenMP/OpenSSL/llamafile are disabled for the Android build.

## Security
API keys are never committed to source. They are stored locally through an encrypted preferences container backed by Android Keystore. Provider configuration stays on-device.

## Build
Requirements: JDK 17, Android SDK API 36, NDK 29.0.13113456, CMake 3.31.6 and Gradle 8.13.

Run: gradle assembleDebug
Run tests: gradle testDebugUnitTest

## Source basis
The supplied specification calls for Clean Architecture, MVI, Compose, Room, Ktor streaming, encrypted API keys, provider strategies, offline-first history and a GGUF/JNI local inference layer. Pages 20–27 define the multi-provider/GGUF strategy and encrypted settings; pages 35–47 define the native engine, Room repository, use cases, MVI and Compose UI.

The implementation is merged to `main`. Local APK compilation still needs to be executed in an Android/Gradle environment because the ChatGPT execution environment cannot resolve GitHub DNS for dependency downloads.