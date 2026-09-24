# NewGPT — Project & Grant Brief

## Project
NewGPT is a native Android AI workspace combining cloud inference, local GGUF inference and a configurable agent runtime in one local-first application.

## Problem
AI workflows are often fragmented between separate chat clients, model runtimes, automation tools and specialist applications. NewGPT provides a single Android workspace where users can select a provider, use a local model, define specialist agents and compose agents into deterministic handoff pipelines.

## Technical innovation
- Provider-neutral inference abstraction.
- Local GGUF inference through llama.cpp and JNI.
- Persistent local conversation history.
- Persistent agent definitions in Android DataStore.
- Agent Builder without recompiling the application.
- Declarative tool catalog with explicit capability classes.
- Agent graph validation, cycle detection and bounded handoff depth.
- Agent-level cancellation.
- Native Android UI using Jetpack Compose.
- CI-generated debug APK and AAB artifacts.

## Agent architecture
The system separates:
1. Agent definition — identity, prompt, skills, tools and handoffs.
2. Agent runtime — execution and orchestration.
3. Chat repository — persistence and inference boundary.
4. Provider strategies — cloud/local model implementation.
5. Tool adapters — future privileged integrations with explicit validation and approval.

The current tool catalog is intentionally declarative. It does not grant Android permissions or silently execute privileged operations.

## Target users
The implementation is suitable as a foundation for:
- developers,
- technical researchers,
- creators,
- AI power users,
- teams experimenting with local/private AI workflows.

No market-size or adoption claims are made here; those require independent evidence for a specific grant programme.

## Current deliverable
The repository contains a working Android implementation with:
- chat,
- provider configuration,
- local GGUF model import,
- agent registry,
- Agent Builder,
- agent chat,
- handoff pipelines,
- graph validation,
- tests,
- CI build and artifact publication.

## Verification
The latest GitHub Actions Android CI run completed successfully for debug assembly and unit tests. The workflow also builds a debug AAB and uploads Android artifacts.

## Development roadmap
### Next engineering layer
- explicit tool-adapter interfaces,
- approval gates for consequential actions,
- structured workflow/DAG editor,
- model-specific generation controls,
- richer markdown/code presentation,
- multiple user-created conversations,
- instrumentation/device test suite.

### Production hardening
- signed release build configuration,
- privacy/account lifecycle where cloud accounts are introduced,
- crash reporting with explicit consent and data minimisation,
- performance profiling on representative Android hardware.

## Funding note
This document deliberately avoids inventing grant eligibility, funding amounts, beneficiaries, economic impact or partnership claims. Those fields should be completed against the requirements of the specific grant programme.
