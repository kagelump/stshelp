# Quick Start Guide

Get up and running with STS Help in 5 minutes!

## Prerequisites

- Slay the Spire installed
- ModTheSpire + BaseMod installed
- Python 3.7+
- OpenAI API key

## 1. Get an API Key

Visit https://platform.openai.com/ and create an API key.

## 2. Install the Middleware

```bash
cd middleware
pip install -r requirements.txt

# Configure API key
cp config.example.json config.json
# Edit config.json and add your API key
```

## 3. Start the Server

```bash
# Linux/Mac
./start.sh

# Windows
start.bat

# Or manually
python server.py
```

You should see:
```
Starting STS Help Middleware
 * Running on http://127.0.0.1:5000
```

## 4. Build the Mod

```bash
# Copy required JARs to lib/ folder (see lib/README.md)
# Then build:
mvn clean package
```

## 5. Install the Mod

Copy `target/stshelp-1.0.0.jar` to your Slay the Spire mods folder.

## 6. Play!

1. Launch Slay the Spire with ModTheSpire
2. Enable STSHelp mod
3. Start a run
4. Click the Help button in the top panel
5. Get AI advice!

## Troubleshooting

### Server won't start
- Check Python is installed: `python3 --version`
- Install dependencies: `pip install -r requirements.txt`

### Mod won't load
- Verify JAR files in `lib/` folder
- Check ModTheSpire logs
- Ensure BaseMod is enabled

### No advice appears
- Ensure middleware server is running
- Check server logs for errors
- Verify API key is configured
- Check firewall settings

## Configuration

### Use a different model
Edit `middleware/config.json`:
```json
{
  "model": "gpt-4"
}
```

### Use local LLM
```json
{
  "openai_endpoint": "http://localhost:8080/v1/chat/completions",
  "model": "your-model"
}
```

## Testing

Run tests to verify everything works:
```bash
python test_middleware.py
```

All tests should pass!

## Next Steps

- Read [SETUP.md](SETUP.md) for detailed setup
- Check [ARCHITECTURE.md](ARCHITECTURE.md) to understand the system
- See [README.md](README.md) for full documentation

Enjoy your AI coach! 🎮
