# Setup Guide for STS Help

This guide will walk you through setting up the STS Help AI Coach system from scratch.

## Overview

The setup involves three main components:
1. The Java mod (runs inside Slay the Spire)
2. The Python middleware server (processes requests)
3. An OpenAI API key (or compatible LLM service)

## Prerequisites Checklist

Before starting, make sure you have:

- [ ] Slay the Spire installed (Steam version recommended)
- [ ] Java Development Kit (JDK) 8 or higher
- [ ] Maven (for building the Java mod)
- [ ] Python 3.7 or higher
- [ ] pip (Python package manager)
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

## Part 3: Setting Up the Middleware Server

### 1. Install Python Dependencies

```bash
cd middleware
pip install -r requirements.txt
```

### 2. Configure API Access

**Option A: Using config.json (Recommended)**

```bash
cp config.example.json config.json
```

Edit `config.json`:
```json
{
  "openai_api_key": "sk-your-actual-api-key-here",
  "openai_endpoint": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-3.5-turbo",
  "port": 5000
}
```

**Option B: Using Environment Variables**

```bash
# Linux/Mac
export OPENAI_API_KEY="sk-your-actual-api-key-here"
export OPENAI_MODEL="gpt-3.5-turbo"
export PORT=5000

# Windows (Command Prompt)
set OPENAI_API_KEY=sk-your-actual-api-key-here
set OPENAI_MODEL=gpt-3.5-turbo
set PORT=5000

# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-actual-api-key-here"
$env:OPENAI_MODEL="gpt-3.5-turbo"
$env:PORT=5000
```

### 3. Get an OpenAI API Key

1. Visit https://platform.openai.com/
2. Sign up or log in
3. Navigate to API Keys section
4. Create a new API key
5. Copy the key (it starts with `sk-`)
6. Add it to your configuration (step 2 above)

### Alternative: Use a Local LLM

If you want to avoid OpenAI and run everything locally:

1. Install LocalAI or similar: https://localai.io/
2. Update your config.json:
   ```json
   {
     "openai_endpoint": "http://localhost:8080/v1/chat/completions",
     "model": "your-local-model-name",
     "openai_api_key": "not-needed-for-local",
     "port": 5000
   }
   ```

## Part 4: Running the System

### 1. Start the Middleware Server

**Linux/Mac:**
```bash
cd middleware
./start.sh
```

**Windows:**
```
cd middleware
start.bat
```

**Or manually:**
```bash
cd middleware
python server.py
```

You should see:
```
Starting STS Help Middleware
Config: {...}
 * Running on http://0.0.0.0:5000
```

### 2. Launch Slay the Spire

1. Start ModTheSpire (MTS.bat or MTS.sh)
2. Ensure both BaseMod and STSHelp are checked
3. Click "Play"

### 3. Test the System

1. Start a new run in Slay the Spire
2. Look for a Help button in the top panel
3. Click the Help button
4. Wait for AI advice to appear (5-10 seconds)

## Part 5: Verification

### Check Server is Running

Open a browser and go to: http://localhost:5000/health

You should see:
```json
{
  "status": "ok",
  "config": {
    "has_api_key": true,
    "model": "gpt-3.5-turbo"
  }
}
```

### Check Server Logs

The middleware server will log all requests:
```
INFO:__main__:Received game state request
INFO:__main__:Essential state: {...}
INFO:__main__:Prompt created
INFO:__main__:Advice generated: [AI advice here]
```

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

### Server connection errors

- Ensure the middleware server is running (`python server.py`)
- Check firewall settings for port 5000
- Verify the endpoint in `stshelp_config.json` matches

### API errors

- Verify your API key is correct and active
- Check OpenAI API status: https://status.openai.com/
- Ensure you have API credits/quota remaining
- Try using a different model (e.g., gpt-3.5-turbo instead of gpt-4)

### Server won't start

```bash
# Check if port 5000 is already in use
# Linux/Mac
lsof -i :5000

# Windows
netstat -ano | findstr :5000

# Use a different port if needed
export PORT=5001
python server.py
```

Then update `stshelp_config.json`:
```json
{
  "endpoint": "http://localhost:5001/advice"
}
```

## Advanced Configuration

### Custom Endpoint

Create `stshelp_config.json` in your Slay the Spire directory:
```json
{
  "endpoint": "http://your-server:5000/advice"
}
```

### Using Different Models

Edit `middleware/config.json`:
```json
{
  "model": "gpt-4",  // More powerful but more expensive
  // or
  "model": "gpt-3.5-turbo"  // Faster and cheaper
}
```

### Running Server on Different Machine

1. On the server machine, start the middleware with:
   ```bash
   python server.py
   ```

2. On the game machine, create `stshelp_config.json`:
   ```json
   {
     "endpoint": "http://server-ip-address:5000/advice"
   }
   ```

3. Ensure firewall allows connections to port 5000

## Next Steps

Once everything is working:

1. Play some runs and test the AI advice
2. Adjust the prompt in `middleware/server.py` if needed
3. Experiment with different LLM models
4. Consider contributing improvements back to the project

## Getting Help

If you encounter issues:

1. Check the troubleshooting section above
2. Review server logs for error messages
3. Check the GitHub issues page
4. Open a new issue with:
   - Your OS and versions (Java, Python, etc.)
   - Error messages from logs
   - Steps to reproduce the problem

Enjoy your AI-powered Slay the Spire coaching!
