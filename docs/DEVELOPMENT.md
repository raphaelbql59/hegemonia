# Development Guide

## 🛠️ Local Development Setup

### Prerequisites

- Node.js 20+
- Java 17+
- Docker & Docker Compose
- Git

### 1. Clone Repository

```bash
git clone <repo-url>
cd hegemonia
```

### 2. Setup Backend API

```bash
cd api
npm install
cp .env.example .env

# Edit .env with local settings
nano .env

# Start local database
docker-compose up postgres redis -d

# Run migrations
npm run prisma:push

# Start dev server
npm run dev
```

API will be available at `http://localhost:3000`

### 3. Setup Launcher

```bash
cd launcher
npm install

# Start dev
npm run dev
```

Launcher will open automatically in development mode.

### 4. Setup Minecraft Server (Optional for local testing)

```bash
cd server

# Download Fabric installer
wget https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.0/fabric-installer-1.0.0.jar

# Install Fabric server
java -jar fabric-installer-1.0.0.jar server -mcversion 1.20.1 -downloadMinecraft

# Start server
./start.sh
```

## 📂 Project Structure

```
hegemonia/
├── api/                    # Backend Node.js API
│   ├── src/
│   │   ├── routes/        # API endpoints
│   │   ├── services/      # Business logic
│   │   ├── models/        # Data models (optional)
│   │   ├── middlewares/   # Express middlewares
│   │   └── utils/         # Utilities
│   └── prisma/
│       └── schema.prisma  # Database schema
│
├── launcher/              # Electron launcher
│   ├── src/
│   │   ├── main/         # Electron main process
│   │   ├── renderer/     # React UI
│   │   ├── preload/      # IPC bridge
│   │   └── common/       # Shared types
│   └── webpack.*.js      # Build configs
│
├── mods/                  # Custom Fabric mods
│   ├── hegemonia-core/
│   ├── hegemonia-economy/
│   ├── hegemonia-warfare/
│   └── hegemonia-tech/
│
├── server/               # Minecraft server configs
│   ├── config/
│   └── scripts/
│
├── docs/                 # Documentation
└── docker-compose.yml    # Docker orchestration
```

## 🔨 Development Workflow

### API Development

1. Create new route in `api/src/routes/`
2. Add business logic in services if complex
3. Update Prisma schema if database changes needed
4. Run `npm run prisma:push` to update DB
5. Test with Postman/curl
6. Document endpoint in API docs

### Launcher Development

1. Main process changes: `launcher/src/main/`
2. UI changes: `launcher/src/renderer/`
3. Hot reload is enabled for both
4. Use `console.log` for debugging (shows in terminal)

### Mod Development

See `docs/MOD_DEVELOPMENT.md` for detailed guide.

## 🧪 Testing

### API Testing

```bash
cd api
npm test
```

### Launcher Testing

```bash
cd launcher
npm test
```

### Manual Testing

1. Start all services locally
2. Test launcher connection to API
3. Test mod download
4. Test game launch
5. Test in-game features

## 📝 Code Style

- Use TypeScript strict mode
- Follow ESLint rules
- Use Prettier for formatting
- Write descriptive commit messages

### Commit Message Format

```
<type>: <description>

[optional body]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Tests
- `chore`: Maintenance

Example:
```
feat: add nation creation command

Implement /nation create command with PostgreSQL persistence
```

## 🐛 Debugging

### API Debugging

```bash
# Enable debug logs
NODE_ENV=development npm run dev

# Database debugging
npm run prisma:studio
```

### Launcher Debugging

- Main process: Check terminal output
- Renderer: Open DevTools (automatically opens in dev mode)
- IPC communication: Add logs in preload script

### Minecraft Debugging

```bash
# Enable debug logs
# In server.properties:
debug=true

# View logs
tail -f logs/latest.log
```

## 🚀 Building for Production

### API

```bash
cd api
npm run build
npm start
```

### Launcher

```bash
cd launcher
npm run build
npm run package

# Platform specific
npm run package:win
npm run package:mac
npm run package:linux
```

## 📚 Resources

- [Electron Docs](https://www.electronjs.org/docs)
- [React Docs](https://react.dev/)
- [Prisma Docs](https://www.prisma.io/docs)
- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Minecraft Wiki](https://minecraft.wiki/)

## 💡 Tips

1. Use `prisma:studio` to visualize database
2. Use React DevTools for UI debugging
3. Keep mod code modular
4. Write documentation as you code
5. Test on different platforms before release

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Make changes
4. Test thoroughly
5. Submit pull request

See `CONTRIBUTING.md` for detailed guidelines.
