# Setup Guide for STS Help

This guide will walk you through setting up the STS Help AI Coach system from scratch.

## Overview

The setup involves two main components:
1. The Java mod (runs inside Slay the Spire)
2. An OpenAI API key (or compatible LLM service)

## Prerequisites Checklist

Before starting, make sure you have:

- [ ] Slay the Spire installed (Steam version recommended)
- [ ] Java Development Kit (JDK) 8 or higher
- [ ] Maven (for building the Java mod)
- [ ] OpenAI API key OR access to a compatible LLM API
- [ ] ModTheSpire installed
- [ ] BaseMod installed

## Part 1: Installing ModTheSpire and BaseMod

### Windows/Mac/Linux

1. **Download ModTheSpire**
   - Visit: https://github.com/kiooeht/ModTheSpire/releases
   - Download the latest `ModTheSpire.jar`
   - Place it in your Slay the Spire installation directory

2. **Download BaseMod**
   - Visit: https://github.com/daviscook477/BaseMod/releases
   - Download the latest `BaseMod.jar`
   - Place it in: `<SlayTheSpire>/mods/` directory

3. **Verify Installation**
   - Run `MTS.bat` (Windows) or `MTS.sh` (Mac/Linux)
   - You should see BaseMod listed in the mods

## Part 2: Building the STS Help Mod

### 1. Get the Required Dependencies

Copy these files to the `lib/` directory in this project:

From your Slay the Spire installation:
- `desktop-1.0.jar` (the main game file)
- `ModTheSpire.jar`

From your mods folder:
- `BaseMod.jar`

### 2. Build the Mod

```bash
# In the project root directory
mvn clean package
```

This will create `target/stshelp-1.0.0.jar`

### 3. Install the Mod

Copy `target/stshelp-1.0.0.jar` to your Slay the Spire mods folder:

**Windows:**
```
C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire\mods\
```

**Mac:**
```
~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/mods/
```

**Linux:**
```
~/.local/share/Steam/steamapps/common/SlayTheSpire/mods/
```

## Part 3: Configure API Access

### 1. Get an OpenAI API Key

1. Visit https://platform.openai.com/
2. Sign up or log in
3. Navigate to API Keys section
4. Create a new API key
5. Copy the key (it starts with `sk-`)

### 2. Create Configuration File

Create `stshelp_config.json` in your Slay the Spire directory (same folder as the game executable):

```json
{
  "openai_api_key": "sk-your-actual-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo"
}
```

**Alternative: Using Environment Variables**

```bash
# Linux/Mac
export OPENAI_API_KEY="sk-your-actual-api-key-here"
export OPENAI_MODEL="gpt-3.5-turbo"

# Windows (Command Prompt)
set OPENAI_API_KEY=sk-your-actual-api-key-here
set OPENAI_MODEL=gpt-3.5-turbo

# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-actual-api-key-here"
$env:OPENAI_MODEL="gpt-3.5-turbo"
```

### Alternative: Use a Local LLM

If you want to avoid OpenAI and run everything locally:

1. Install LocalAI or similar: https://localai.io/
2. Update your `stshelp_config.json`:
   ```json
   {
     "openai_api_key": "not-needed-for-local",
     "openai_endpoint": "http://localhost:8080/v1/chat/completions",
     "model": "your-local-model-name"
   }
   ```

## Part 4: Running the System

### 1. Launch Slay the Spire

1. Start ModTheSpire (MTS.bat or MTS.sh)
2. Ensure both BaseMod and STSHelp are checked
3. Click "Play"

### 2. Test the System

1. Start a new run in Slay the Spire
2. Look for a Help button in the top panel
3. Click the Help button
4. Wait for AI advice to appear (5-10 seconds)

## Part 5: Verification

### In-Game Verification

1. Start a run
2. Get into combat
3. Click Help button
4. You should see a popup with AI advice
5. Press ESC to close

## Troubleshooting

### Mod doesn't appear in ModTheSpire

- Verify all dependencies are in the `lib/` folder
- Check that Maven build completed successfully
- Ensure the JAR is in the correct mods folder

### "No active game" error

- You must be in an active run, not in the menu
- Try starting a new run and clicking Help again

### API errors

- Verify your API key is correct and active in `stshelp_config.json`
- Check OpenAI API status: https://status.openai.com/
- Ensure you have API credits/quota remaining
- Try using a different model (e.g., gpt-3.5-turbo instead of gpt-4)
- Check your internet connection

### No response or timeout

- Check ModTheSpire logs for error messages
- Verify your API endpoint is accessible
- Ensure firewall isn't blocking HTTPS traffic
- Try increasing timeout in the code if on slow connection

## Advanced Configuration

### Using Different Models

Edit `stshelp_config.json`:
```json
{
  "openai_api_key": "your-key",
  "model": "gpt-4"  // More powerful but more expensive
  // or
  "model": "gpt-3.5-turbo"  // Faster and cheaper
}
```

### Using Azure OpenAI

```json
{
  "openai_api_key": "your-azure-key",
  "openai_endpoint": "https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2023-05-15",
  "model": "gpt-35-turbo"
}
```

## Next Steps

Once everything is working:

1. Play some runs and test the AI advice
2. Experiment with different LLM models
3. Consider contributing improvements back to the project

## Getting Help

If you encounter issues:

1. Check the troubleshooting section above
2. Check ModTheSpire logs for error messages
3. Check the GitHub issues page
4. Open a new issue with:
   - Your OS and versions (Java, Maven, etc.)
   - Error messages from logs
   - Steps to reproduce the problem

Enjoy your AI-powered Slay the Spire coaching!
