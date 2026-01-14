# Required Libraries

This directory should contain the following JAR files from your Slay the Spire installation:

1. **desktop-1.0.jar** - The main Slay the Spire game JAR
   - Location: `<SteamLibrary>/steamapps/common/SlayTheSpire/desktop-1.0.jar`

2. **BaseMod.jar** - The BaseMod dependency
   - Download from: https://github.com/daviscook477/BaseMod/releases
   - Place in your mods folder first, then copy here

3. **ModTheSpire.jar** - The ModTheSpire loader
   - Download from: https://github.com/kiooeht/ModTheSpire/releases
   - Copy from your Slay the Spire installation directory

## Installation Steps

1. Copy `desktop-1.0.jar` from your Slay the Spire installation
2. Download and install BaseMod, then copy `BaseMod.jar` here
3. Copy `ModTheSpire.jar` from your Slay the Spire installation

After adding these files, you can build the mod with:
```bash
mvn clean package
```

## Note

These files are not included in the repository due to licensing restrictions.
You must obtain them from your own Slay the Spire installation.
