# Quick Start Guide

Get up and running with STS Help in 5 minutes!

## Prerequisites

- Slay the Spire installed
- ModTheSpire + BaseMod installed
- OpenAI API key

## 1. Get an API Key

Visit https://platform.openai.com/ and create an API key.

## 2. Build the Mod

```bash
# Copy required JARs to lib/ folder (see lib/README.md)
# Then build:
mvn clean package
```

## 3. Install the Mod

Copy `target/stshelp-1.0.0.jar` to your Slay the Spire mods folder:
- Windows: `C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire\mods`
- Mac: `~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/mods`
- Linux: `~/.local/share/Steam/steamapps/common/SlayTheSpire/mods`

## 4. Configure API Key

Create `stshelp_config.json` in your Slay the Spire directory (same folder as the game executable):

```json
{
  "openai_api_key": "your-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo"
}
```

Or set environment variables:
```bash
export OPENAI_API_KEY="your-api-key-here"
```

## 5. Play!

1. Launch Slay the Spire with ModTheSpire
2. Enable STSHelp mod
3. Start a run
4. Click the Help button in the top panel
5. Get AI advice!

## Troubleshooting

### Mod won't load
- Verify JAR files in `lib/` folder
- Check ModTheSpire logs
- Ensure BaseMod is enabled

### No advice appears
- Verify API key is configured correctly
- Check ModTheSpire logs for errors
- Ensure you have internet connectivity
- Verify API endpoint is accessible

### API Errors
- Check your API key is valid
- Verify you have API credits/quota
- Try a different model if rate limited

## Configuration

### Use a different model
Edit `stshelp_config.json`:
```json
{
  "openai_api_key": "your-key",
  "model": "gpt-4"
}
```

### Use local LLM
```json
{
  "openai_api_key": "not-needed",
  "openai_endpoint": "http://localhost:8080/v1/chat/completions",
  "model": "your-model"
}
```

## Next Steps

- Read [SETUP.md](SETUP.md) for detailed setup
- Check [ARCHITECTURE.md](ARCHITECTURE.md) to understand the system
- See [README.md](README.md) for full documentation

Enjoy your AI coach! 🎮
