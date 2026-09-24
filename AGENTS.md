# NewGPT — Agent Runtime & Agent Builder

## Cel
NewGPT posiada lokalny rejestr agentów, runtime agentowy, czat per-agent oraz prosty pipeline handoffów. Agent Builder pozwala tworzyć i modyfikować definicje bez zmiany kodu aplikacji.

## Architektura
```
Chat / Agents UI
      │
      ├── Agent Builder
      │      └── AgentRepository → DataStore
      │
      └── AgentsViewModel
             │
             ▼
        AgentRuntime
             │
             ├── pojedynczy agent
             └── handoff pipeline
                    │
                    ▼
             ChatRepository
                    │
          ┌─────────┼──────────┐
          ▼         ▼          ▼
       OpenAI   Anthropic   Gemini
          │         │          │
          ├──── OpenAI-compatible
          └──── Local GGUF / llama.cpp
```

## Wbudowane agenty
- **Coordinator** — planowanie, delegowanie i weryfikacja.
- **Researcher** — evidence, porównania i analiza źródeł.
- **Architect** — architektura, kontrakty i bezpieczeństwo.
- **Coder** — implementacja Kotlin/Android/Python i testy.
- **Writer** — dokumentacja, redakcja i materiały kreatywne.
- **WDA Photo** — fotografia, prompt engineering i continuity.
- **Mobile Operator** — bezpieczne planowanie działań Androida.

## Agent Definition
Każdy agent posiada: `id`, `name`, `description`, `systemPrompt`, `skills[]`, `tools[]`, `handoffs[]`, `enabled`.

`tools` i `skills` są obecnie deklaratywnym kontraktem. Nie nadają same w sobie uprawnień do systemu.

## Agent Builder
Builder zapisuje definicje w lokalnym Android DataStore. Dostępne operacje: utworzenie, edycja, system prompt, skills, tools, handoffs, włączenie/wyłączenie, usunięcie i reset do domyślnych.

## Handoff pipeline
Agent z wpisanymi `handoffs` może uruchomić pipeline. Każdy kolejny agent otrzymuje rezultat poprzednika jako materiał do weryfikacji i ulepszenia.

## Bezpieczeństwo
Agent Definition nie jest mechanizmem autoryzacji. Uprzywilejowane operacje powinny być wykonywane wyłącznie przez jawne adaptery z własnym mechanizmem zgody i walidacji.

## Materiały referencyjne
Wykorzystano koncepcje z dostarczonych materiałów: Planner → Grounding → Executor → Supervisor, bezpieczna granica runtime, workflow/DAG, policy-as-code, agent registry i agent chat.
