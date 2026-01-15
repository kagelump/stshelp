# Architecture Documentation

## System Overview

STS Help is a two-tier architecture system that provides AI-powered coaching for Slay the Spire through in-game integration.

```
┌─────────────────────────────────────────────────────────┐
│                   Slay the Spire Game                   │
│  ┌───────────────────────────────────────────────────┐  │
│  │              STSHelp Mod (Java)                   │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │  │
│  │  │  Help    │  │  Game    │  │   Advice     │   │  │
│  │  │  Button  │─▶│  State   │  │   Screen     │   │  │
│  │  │          │  │Extractor │  │              │   │  │
│  │  └──────────┘  └────┬─────┘  └──────▲───────┘   │  │
│  │                     │                │           │  │
│  │              ┌──────▼────────────────┴──────┐    │  │
│  │              │   AICoachClient              │    │  │
│  │              │   ┌──────────────────────┐   │    │  │
│  │              │   │    LLMClient         │   │    │  │
│  │              │   │  - Prompt Creation   │   │    │  │
│  │              │   │  - API Communication │   │    │  │
│  │              │   └──────────┬───────────┘   │    │  │
│  │              └──────────────┼───────────────┘    │  │
│  └─────────────────────────────┼──────────────────┘  │
└────────────────────────────────┼─────────────────────┘
                                 │ HTTPS POST (JSON)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────┐
│             LLM API (OpenAI or Compatible)              │
│  ┌───────────────────────────────────────────────────┐  │
│  │  GPT-3.5-turbo / GPT-4 / Local LLM               │  │
│  │  - Receives game state prompt                     │  │
│  │  - Generates strategic advice                     │  │
│  │  - Returns text response                          │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          │ Advice Text
                          ▼
                     (Returns to user)
```

## Component Details

### Java Mod Components

**Location:** `src/main/java/com/kagelump/stshelp/`

#### STSHelpMod.java
- **Purpose:** Main mod class, entry point for BaseMod
- **Responsibilities:**
  - Initialize mod and register with BaseMod
  - Create and manage HelpButton and AdviceScreen
  - Coordinate between UI components and LLM client
  - Handle mod lifecycle events
- **Key Methods:**
  - `initialize()`: Static entry point called by ModTheSpire
  - `requestAdvice()`: Triggered when Help button clicked

#### AICoachClient.java
- **Purpose:** Orchestrator for LLM communication
- **Responsibilities:**
  - Load configuration from file or environment
  - Manage async requests (non-blocking)
  - Coordinate with LLMClient
- **Configuration:** Loads from `stshelp_config.json` or environment variables

#### LLMClient.java
- **Purpose:** Direct LLM API communication
- **Responsibilities:**
  - Create prompts from game state JSON
  - Send HTTPS requests to OpenAI-compatible APIs
  - Parse LLM responses
- **Key Methods:**
  - `createPrompt()`: Generates LLM prompt from game state
  - `getAdvice()`: Sends request and returns advice

#### GameStateExtractor.java
- **Purpose:** Extract essential game state data
- **Responsibilities:**
  - Extract player info, deck, relics, combat state
  - Serialize to JSON using Gson

#### Other Components
- **HelpButton.java**: Top panel UI button
- **AdviceScreen.java**: In-game popup for displaying advice

## Data Flow

1. User clicks Help button → `STSHelpMod.requestAdvice()`
2. Extract game state → `GameStateExtractor.extractState()`
3. Create prompt → `LLMClient.createPrompt()`
4. Call LLM API → `LLMClient.getAdvice()`
5. Display response → `AdviceScreen.showAdvice()`

## Configuration

Configuration is loaded from:
1. Environment variables (highest priority)
2. `stshelp_config.json` file
3. Default values

**Supported settings:**
- `openai_api_key`: API key for authentication
- `openai_endpoint`: API endpoint URL (default: OpenAI)
- `model`: LLM model name (default: gpt-3.5-turbo)

## Key Design Decisions

1. **Direct LLM Integration**: Eliminated middleware layer for simplicity
2. **Async Processing**: Non-blocking API calls to avoid freezing the game
3. **Flexible Configuration**: Supports multiple LLM providers
4. **Self-Contained**: All logic in Java, no external dependencies beyond LLM API

## Build

```bash
mvn clean package
```

Output: `target/stshelp-1.0.0.jar`
