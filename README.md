# STS Help - AI Coach for Slay the Spire

An on-demand LLM-powered coaching system for Slay the Spire, providing strategic advice via an in-game Help button.

## Architecture

The system consists of three layers:

1. **Frontend (Java/BaseMod)**: Adds a TopPanelItem Help button to the game UI
2. **Middleware (Python/Flask)**: Processes game state and communicates with LLM API
3. **AI Layer**: OpenAI-compatible LLM that generates strategic advice

```
┌─────────────────┐
│  Slay the Spire │
│   + BaseMod     │
│   + STSHelp Mod │
└────────┬────────┘
         │ HTTP POST
         │ (Game State JSON)
         ▼
┌─────────────────┐
│ Python Server   │
│ (Flask)         │
│ - State Filter  │
│ - Prompt Gen    │
└────────┬────────┘
         │ OpenAI API
         │ (Chat Completion)
         ▼
┌─────────────────┐
│  LLM API        │
│ (OpenAI/Compat) │
└────────┬────────┘
         │ Advice Text
         ▼
┌─────────────────┐
│  In-Game Screen │
│  (Advice Text)  │
└─────────────────┘
```

## Features

- **In-game Help Button**: Click to get instant AI advice
- **Context-Aware**: Analyzes your current HP, deck, relics, and combat situation
- **Combat Assistance**: Provides turn-by-turn tactical advice during fights
- **Strategic Guidance**: Offers deck-building and run strategy tips
- **OpenAI Compatible**: Works with OpenAI API or compatible alternatives

## Installation

### Prerequisites

- Slay the Spire (Steam version)
- ModTheSpire
- BaseMod
- Java 8 or higher
- Python 3.7 or higher
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

### Step 2: Set Up the Middleware

1. Navigate to the middleware directory:
   ```bash
   cd middleware
   ```

2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Configure the API:
   ```bash
   cp config.example.json config.json
   # Edit config.json with your API key
   ```

   Or set environment variables:
   ```bash
   export OPENAI_API_KEY="your-api-key-here"
   export OPENAI_MODEL="gpt-3.5-turbo"  # or gpt-4
   ```

4. Start the middleware server:
   ```bash
   python server.py
   ```

   The server will start on `http://localhost:5000`

### Step 3: Configure the Mod (Optional)

Create a `stshelp_config.json` file in your Slay the Spire directory:

```json
{
  "endpoint": "http://localhost:5000/advice"
}
```

## Usage

1. Start the middleware server (see Step 2 above)
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

### Middleware Configuration

Edit `middleware/config.json`:

```json
{
  "openai_api_key": "your-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo",
  "port": 5000
}
```

### Using Alternative LLM APIs

The system supports any OpenAI-compatible API. For example:

**LocalAI:**
```json
{
  "openai_endpoint": "http://localhost:8080/v1/chat/completions",
  "model": "ggml-gpt4all-j"
}
```

**Azure OpenAI:**
```json
{
  "openai_endpoint": "https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2023-05-15",
  "openai_api_key": "your-azure-key"
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
│   └── AICoachClient.java       # HTTP client for middleware
├── middleware/
│   ├── server.py                # Flask server
│   ├── requirements.txt         # Python dependencies
│   └── config.example.json      # Configuration template
├── pom.xml                      # Maven build file
└── README.md                    # This file
```

## Troubleshooting

### "No active game" Error
- Make sure you're in an active run before clicking the Help button

### Connection Errors
- Ensure the middleware server is running
- Check that the endpoint in `stshelp_config.json` matches the server address
- Verify firewall settings aren't blocking localhost:5000

### API Errors
- Verify your API key is correct
- Check API rate limits and quotas
- Ensure the model name is valid

### No Response
- Check middleware server logs for errors
- Verify network connectivity
- Increase timeout values if on slow connection

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
