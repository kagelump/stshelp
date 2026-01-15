# STS Help - AI Coach for Slay the Spire

An on-demand LLM-powered coaching system for Slay the Spire, providing strategic advice via an in-game Help button.

## Architecture

The system consists of two layers:

1. **Game Mod (Java/BaseMod)**: Extracts game state, communicates with LLM API, and displays advice
2. **AI Layer**: OpenAI-compatible LLM that generates strategic advice

```
┌─────────────────────────────────┐
│      Slay the Spire             │
│       + BaseMod                 │
│       + STSHelp Mod (Java)      │
│  ┌───────────────────────────┐  │
│  │ - Game State Extractor    │  │
│  │ - Prompt Generator        │  │
│  │ - LLM Client              │  │
│  │ - Advice Display          │  │
│  └───────────┬───────────────┘  │
└──────────────┼──────────────────┘
               │ HTTPS POST
               │ (OpenAI API)
               ▼
┌─────────────────────────────────┐
│      LLM API                    │
│  (OpenAI/Compatible)            │
│  - GPT-3.5-turbo / GPT-4        │
│  - LocalAI / Ollama             │
│  - Azure OpenAI                 │
└──────────────┬──────────────────┘
               │ Advice Text
               ▼
      (Displayed in-game)
```

## Features

- **In-game Help Button**: Click to get instant AI advice
- **Context-Aware**: Analyzes your current HP, deck, relics, and combat situation
- **Combat Assistance**: Provides turn-by-turn tactical advice during fights
- **Strategic Guidance**: Offers deck-building and run strategy tips
- **Direct LLM Integration**: Communicates directly with OpenAI API or compatible alternatives
- **No External Dependencies**: All processing happens within the Java mod

## Installation

### Prerequisites

- Slay the Spire (Steam version)
- ModTheSpire
- BaseMod
- Java 8 or higher
- OpenAI API key or compatible LLM API

### Step 1: Install the Mod

1. Clone this repository
2. Build the mod:
   ```bash
   mvn clean package
   ```
3. Copy `target/stshelp-1.0.0.jar` to your Slay the Spire mods folder:
   - Windows: `C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire\mods`
   - Mac: `~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/mods`
   - Linux: `~/.local/share/Steam/steamapps/common/SlayTheSpire/mods`

### Step 2: Configure the API

Create a `stshelp_config.json` file in your Slay the Spire directory (same directory as the game executable):

```json
{
  "openai_api_key": "your-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo"
}
```

Alternatively, set environment variables:
```bash
export OPENAI_API_KEY="your-api-key-here"
export OPENAI_ENDPOINT="https://api.openai.com/v1/chat/completions"
export OPENAI_MODEL="gpt-3.5-turbo"  # or gpt-4
```

## Usage

1. Configure your OpenAI API key (see Installation Step 2)
2. Launch Slay the Spire with ModTheSpire
3. Start a run
4. Click the Help button in the top panel (or press the configured hotkey)
5. Wait for AI advice to appear in a popup window
6. Press ESC or click outside the popup to close

## Game State Information

The mod extracts and sends the following information to the AI:

### Player Info
- Current HP / Max HP
- Current Energy
- Gold
- Character class

### Deck & Relics
- All cards in deck (with upgrade status)
- All relics

### Combat Info (when in battle)
- Current hand
- Draw pile size
- Discard pile size
- Enemy information:
  - Name
  - HP
  - Intent (attack, defend, buff, etc.)
  - Intended damage

## Configuration

### Mod Configuration

Edit `stshelp_config.json` in your Slay the Spire directory:

```json
{
  "openai_api_key": "your-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo"
}
```

### Using Alternative LLM APIs

The system supports any OpenAI-compatible API. For example:

**LocalAI:**
```json
{
  "openai_api_key": "not-needed-for-local",
  "openai_endpoint": "http://localhost:8080/v1/chat/completions",
  "model": "ggml-gpt4all-j"
}
```

**Azure OpenAI:**
```json
{
  "openai_api_key": "your-azure-key",
  "openai_endpoint": "https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2023-05-15",
  "model": "gpt-35-turbo"
}
```

## Development

### Building from Source

```bash
# Build Java mod
mvn clean package

# Run tests (if available)
mvn test
```

### Project Structure

```
stshelp/
├── src/main/java/com/kagelump/stshelp/
│   ├── STSHelpMod.java          # Main mod class
│   ├── HelpButton.java          # Top panel help button
│   ├── AdviceScreen.java        # In-game advice display
│   ├── GameStateExtractor.java  # Extracts game state
│   ├── AICoachClient.java       # Orchestrates LLM communication
│   └── LLMClient.java           # Direct LLM API client
├── pom.xml                      # Maven build file
└── README.md                    # This file
```

## Troubleshooting

### "No active game" Error
- Make sure you're in an active run before clicking the Help button

### API Key Errors
- Verify your API key is correct in `stshelp_config.json`
- Check that the file is in the correct location (Slay the Spire directory)
- Alternatively, set the `OPENAI_API_KEY` environment variable

### API Errors
- Verify your API key is correct
- Check API rate limits and quotas
- Ensure the model name is valid
- Check your internet connection

### No Response
- Check the ModTheSpire logs for errors
- Verify your API endpoint is accessible
- Ensure firewall isn't blocking HTTPS traffic to the API endpoint

## Privacy & Security

- Game state data is sent to the configured LLM API endpoint
- No personal information is collected or transmitted
- API keys should be kept secure and not committed to version control
- Consider using local LLM alternatives for complete privacy

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Credits

- Built for Slay the Spire by MegaCrit
- Uses BaseMod by daviscook477, kiooeht, and contributors
- Powered by OpenAI GPT models or compatible alternatives

## Support

For issues and feature requests, please use the GitHub issue tracker.
