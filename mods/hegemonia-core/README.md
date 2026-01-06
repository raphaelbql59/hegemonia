# Hegemonia Core Mod

Core gameplay mod for Hegemonia Earth Nations server.

## Features

### Nation System
- Create and manage nations
- Invite/remove players
- Set government type (Monarchy, Democracy, Dictatorship, Republic)
- Treasury management
- Tax system

### Territory System
- Claim regions based on Earth map
- Territory ownership tracking
- Resource allocation per territory
- Border management

### Government Types

**Monarchy**
- Leader has absolute power
- Succession by designated heir
- Fast decision making

**Democracy**
- Leader elected every 30 days
- Major decisions require voting
- High population loyalty

**Dictatorship**
- Military control
- +20% military production
- Risk of rebellion

**Republic**
- Balanced system
- Elected leader + appointed ministers

### Roles
- Leader
- Minister of Economy
- Minister of Defense
- Minister of Interior
- Citizen

## Commands

### Nations
- `/nation create <name>` - Create a new nation
- `/nation disband` - Disband your nation (leader only)
- `/nation invite <player>` - Invite a player
- `/nation kick <player>` - Remove a player
- `/nation leave` - Leave your nation
- `/nation info [nation]` - View nation information
- `/nation list` - List all active nations
- `/nation setgovernment <type>` - Change government type

### Territories
- `/territory claim <region>` - Claim a territory
- `/territory unclaim <region>` - Unclaim a territory
- `/territory info <region>` - View territory information
- `/territory list [nation]` - List territories

### Roles
- `/nation promote <player> <role>` - Promote a player
- `/nation demote <player>` - Demote a player

## Database Schema

See `api/prisma/schema.prisma` for complete database schema.

## Development

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew runServer
```

## Dependencies
- Fabric API 0.92.0+
- Cardinal Components API
- PostgreSQL JDBC Driver
