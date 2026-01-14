# Architecture Documentation

## System Overview

STS Help is a three-tier architecture system that provides AI-powered coaching for Slay the Spire through in-game integration.

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
│  │              │   AICoachClient (HTTP)       │    │  │
│  │              └──────┬───────────────────────┘    │  │
│  └─────────────────────┼──────────────────────────┘  │
└────────────────────────┼─────────────────────────────┘
                         │ HTTP POST (JSON)
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              Middleware Server (Python/Flask)           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Flask App (/advice endpoint)                     │  │
│  │  ┌───────────┐  ┌─────────────┐  ┌────────────┐  │  │
│  │  │  Receive  │─▶│   Strip to  │─▶│  Create    │  │  │
│  │  │Game State │  │  Essential  │  │  Prompt    │  │  │
│  │  └───────────┘  └─────────────┘  └─────┬──────┘  │  │
│  └─────────────────────────────────────────┼─────────┘  │
└────────────────────────────────────────────┼────────────┘
                                             │ OpenAI API
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

### 1. Frontend Layer (Java/BaseMod)

**Location:** `src/main/java/com/kagelump/stshelp/`

#### STSHelpMod.java
- **Purpose:** Main mod class, entry point for BaseMod
- **Responsibilities:**
  - Initialize mod and register with BaseMod
  - Create and manage HelpButton and AdviceScreen
  - Coordinate between UI components and backend
  - Handle mod lifecycle events
- **Key Methods:**
  - `initialize()`: Static entry point called by ModTheSpire
  - `receivePostInitialize()`: Setup after game loads
  - `receivePostUpdate()`: Called each frame
  - `requestAdvice()`: Triggered when Help button clicked

#### HelpButton.java
- **Purpose:** Top panel UI button
- **Extends:** `basemod.TopPanelItem`
- **Responsibilities:**
  - Render Help button in top panel
  - Handle click events
  - Trigger advice request flow
- **Integration:** Uses BaseMod's TopPanelItem system

#### GameStateExtractor.java
- **Purpose:** Extract essential game state data
- **Responsibilities:**
  - Query game state from AbstractDungeon
  - Filter to essential information:
    - Player HP, energy, gold, character
    - Complete deck composition
    - Relics
    - Combat state (hand, enemies, intents)
  - Serialize to JSON using Gson
- **Key Method:** `extractState()` returns JSON string

#### AICoachClient.java
- **Purpose:** HTTP client for middleware communication
- **Responsibilities:**
  - Send game state to middleware server
  - Handle async HTTP requests (non-blocking)
  - Parse responses
  - Error handling and timeouts
- **Design Pattern:** Async callback pattern
- **Configuration:** Loads endpoint from `stshelp_config.json`

#### AdviceScreen.java
- **Purpose:** In-game popup display for AI advice
- **Responsibilities:**
  - Render semi-transparent overlay
  - Display advice text with scrolling
  - Handle user input (ESC to close, click outside)
  - Pause game while open
- **Features:**
  - Scrollable text area
  - Professional UI styling
  - Modal behavior

### 2. Middleware Layer (Python/Flask)

**Location:** `middleware/`

#### server.py
- **Purpose:** Flask web server that processes requests
- **Endpoints:**
  - `POST /advice`: Main endpoint for advice requests
  - `GET /health`: Health check and configuration status

**Key Functions:**

1. **strip_to_essential_state()**
   - Reduces full game state to essential tokens
   - Optimizes for LLM context window
   - Keeps: HP, Deck, Relics, Enemy Intent, Hand
   - Output: Compact dictionary

2. **create_prompt()**
   - Constructs LLM prompt from essential state
   - Formats data in human-readable structure
   - Provides context and instructions to LLM
   - Different prompts for combat vs non-combat

3. **get_llm_advice()**
   - Calls OpenAI-compatible API
   - Handles authentication
   - Error handling and retry logic
   - Returns advice string

**Configuration:**
- Priority: Environment variables > config.json
- Supports multiple LLM backends
- Configurable model, endpoint, port

### 3. AI Layer (External LLM)

**Supported APIs:**
- OpenAI (GPT-3.5-turbo, GPT-4)
- Azure OpenAI
- LocalAI
- Any OpenAI-compatible API

**Request Format:**
```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "system",
      "content": "You are an expert Slay the Spire coach..."
    },
    {
      "role": "user",
      "content": "[Game state and prompt]"
    }
  ],
  "max_tokens": 200,
  "temperature": 0.7
}
```

## Data Flow

### Request Flow (User clicks Help button)

1. **User Interaction**
   ```
   User clicks Help button → HelpButton.onClick()
   ```

2. **State Extraction**
   ```
   STSHelpMod.requestAdvice() → GameStateExtractor.extractState()
   ```
   
3. **JSON Serialization**
   ```
   Game objects → Gson → JSON string
   Example:
   {
     "player": {"current_hp": 45, "max_hp": 80, ...},
     "deck": ["Strike", "Defend", ...],
     "relics": ["Burning Blood", ...],
     "combat": {
       "enemies": [{"name": "Jaw Worm", "intent": "ATTACK", ...}]
     }
   }
   ```

4. **HTTP Request**
   ```
   AICoachClient → POST localhost:5000/advice
   Headers: Content-Type: application/json
   Body: [Game state JSON]
   ```

5. **Middleware Processing**
   ```
   Flask receives POST →
   strip_to_essential_state() →
   create_prompt() →
   get_llm_advice()
   ```

6. **LLM API Call**
   ```
   POST https://api.openai.com/v1/chat/completions
   Headers: Authorization: Bearer [API_KEY]
   Body: [Chat completion request]
   ```

7. **Response Flow**
   ```
   LLM → Middleware → HTTP Response →
   AICoachClient callback →
   AdviceScreen.showAdvice() →
   Display to user
   ```

### Error Handling Flow

Each layer handles errors independently:

1. **Java Layer:**
   - Network errors → Show "Connection failed" message
   - Timeout → Show "Request timed out"
   - No active game → Show "No active game"

2. **Middleware Layer:**
   - Invalid JSON → HTTP 400
   - LLM API error → Return error message
   - Timeout → Return timeout message

3. **LLM Layer:**
   - Auth error → Middleware catches, returns message
   - Rate limit → Middleware catches, returns message

## State Reduction Strategy

The middleware strips game state to essential information to:
- Reduce LLM token usage (cost optimization)
- Improve response speed
- Focus AI on relevant information

**Full State (~1000+ tokens) → Essential State (~200 tokens)**

Example reduction:
```
Before: Complete card objects with costs, types, descriptions, etc.
After:  ["Strike", "Defend+", "Carnage+"]

Before: Full enemy objects with powers, animation states, etc.
After:  {"name": "Jaw Worm", "hp": "25/44", "intent": "ATTACK", "damage": 11}
```

## Security Considerations

1. **API Key Protection:**
   - Never commit config.json with real keys
   - Use environment variables in production
   - .gitignore includes config.json

2. **Network Security:**
   - HTTP connections (localhost only by default)
   - No sensitive player data collected
   - Game state only (no personal information)

3. **Input Validation:**
   - Middleware validates JSON structure
   - Timeout limits on HTTP requests
   - Error messages sanitized

## Performance Characteristics

**Latency Breakdown:**
- State extraction: <10ms
- HTTP request: 10-50ms (localhost)
- Middleware processing: 10-20ms
- LLM API call: 1-5 seconds
- **Total: 1-5 seconds typically**

**Resource Usage:**
- Java mod: Minimal (<1% CPU, <10MB RAM)
- Python middleware: <50MB RAM
- Network: ~5KB per request

## Extensibility Points

1. **Custom State Extractors:**
   - Modify `GameStateExtractor.java` to include more data
   - Add potion info, map data, etc.

2. **Alternative LLM Backends:**
   - Any OpenAI-compatible API works
   - Local models via LocalAI, Ollama, etc.
   - Custom prompt engineering in `create_prompt()`

3. **UI Customization:**
   - `AdviceScreen.java` can be styled
   - Add hotkeys, tooltips, etc.
   - Multiple display modes

4. **Advanced Features:**
   - Cache common scenarios
   - Pre-generate advice for common situations
   - Add user feedback mechanism
   - Track advice effectiveness

## Build and Deployment

**Java Mod:**
```bash
mvn clean package
→ target/stshelp-1.0.0.jar
```

**Python Middleware:**
```bash
pip install -r requirements.txt
python server.py
```

**Dependencies:**
- Java: BaseMod, ModTheSpire, STS game JARs
- Python: Flask, requests
- External: OpenAI API or compatible

## Configuration Files

- `pom.xml`: Maven build configuration
- `middleware/requirements.txt`: Python dependencies
- `middleware/config.json`: Runtime configuration
- `stshelp_config.json`: Mod configuration (optional)

## Testing Strategy

1. **Unit Tests:** (Future)
   - Test state extraction logic
   - Test prompt generation
   - Mock LLM responses

2. **Integration Tests:**
   - Test full request flow
   - Test error handling
   - Test different game states

3. **Manual Testing:**
   - In-game testing with different scenarios
   - Combat situations
   - Non-combat situations
   - Error cases

## Future Enhancements

1. **Caching:** Cache advice for similar game states
2. **Analytics:** Track which advice is most helpful
3. **Fine-tuning:** Custom model trained on STS strategies
4. **Offline Mode:** Pre-generated advice for common situations
5. **Multi-language:** Support for different languages
6. **Voice Output:** Text-to-speech for advice
7. **Replay Analysis:** Post-game run analysis
