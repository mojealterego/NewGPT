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
The repository contains the Android JNI boundary and model-file import flow. The current native implementation is a safe bridge stub; it does not embed the full llama.cpp source tree yet. The bridge is isolated so the llama.cpp backend can be linked without changing the Kotlin/domain architecture.

Upstream llama.cpp currently publishes Android arm64 builds. The next native milestone is to pin a tested llama.cpp revision and wire its C API into newgpt_jni.cpp.

## Security
API keys are never committed to source. They are stored locally through an encrypted preferences container backed by Android Keystore. Provider configuration stays on-device.

## Build
Requirements: JDK 17, Android SDK API 35/36, NDK 27, Gradle 8.13.

Run: gradle assembleDebug
Run tests: gradle testDebugUnitTest

## Source basis
The supplied specification calls for Clean Architecture, MVI, Compose, Room, Ktor streaming, encrypted API keys, provider strategies, offline-first history and a GGUF/JNI local inference layer. Pages 20–27 define the multi-provider/GGUF strategy and encrypted settings; pages 35–47 define the native engine, Room repository, use cases, MVI and Compose UI.

This repository now contains the initial Android codebase instead of an empty placeholder.