# Hegemonia Custom Mods

This directory contains all custom Fabric mods for the Hegemonia server.

## Mods Structure

### hegemonia-core
Core gameplay mechanics:
- Nation system (creation, management, roles)
- Territory claims and management
- Government types
- Database integration

### hegemonia-economy
Economic system:
- HGN currency
- Market system (Admin Shop, International Commerce)
- Taxation
- Budget management
- Transactions

### hegemonia-warfare
War and combat:
- War declaration system
- War types (Conquest, Economic, etc.)
- Custom weapons
- Vehicles (tanks, helicopters)
- Siege mechanics

### hegemonia-tech
Technology progression:
- Technology tree (4 eras)
- Research system
- Unlock mechanics
- Era-specific items

## Development

Each mod follows standard Fabric mod structure:

```
mod-name/
├── src/main/
│   ├── java/com/hegemonia/modname/
│   │   ├── ModName.java          # Main mod class
│   │   ├── commands/              # Commands
│   │   ├── events/                # Event listeners
│   │   ├── database/              # DB integration
│   │   └── util/                  # Utilities
│   └── resources/
│       ├── fabric.mod.json        # Mod metadata
│       └── assets/                # Textures, models, etc.
├── gradle/
├── build.gradle
└── README.md
```

## Building Mods

```bash
cd hegemonia-core
./gradlew build

# Output: build/libs/hegemonia-core-1.0.0.jar
```

## Installation

1. Build all mods
2. Copy JARs to `server/mods/`
3. Restart server
4. Add to API's ModFile table for launcher distribution

## Dependencies

All mods depend on:
- Fabric API 0.92.0+
- Minecraft 1.20.1
- Java 17+

Common libraries:
- Cardinal Components (for data attachment)
- Polymer (for custom UI)

## Testing

Use a local Minecraft instance with the mods loaded to test before deploying to server.

## Documentation

See `docs/MOD_DEVELOPMENT.md` for detailed development guide.
