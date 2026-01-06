# Hegemonia - Minecraft Custom Server Launcher

Un launcher moderne pour serveur Minecraft moddé avec système de distribution automatique des mods.

## 🎮 Fonctionnalités

### Launcher Client
- Interface moderne et intuitive (Electron + React)
- Auto-update du launcher et des mods
- Gestion des profils utilisateurs
- Téléchargement optimisé des ressources
- Vérification d'intégrité des fichiers
- Support multi-versions
- Console de logs intégrée

### Serveur
- Configuration Forge/Fabric optimisée
- Gestion centralisée des mods
- Scripts de démarrage automatiques
- Monitoring des performances
- Backup automatique

### Système de Distribution
- API REST pour la distribution des mods
- Vérification des versions
- Téléchargements incrémentaux
- CDN ready

## 📁 Structure du Projet

```
hegemonia/
├── launcher/           # Application Electron du launcher
│   ├── src/
│   │   ├── main/      # Process principal Electron
│   │   ├── renderer/  # Interface React
│   │   └── common/    # Code partagé
│   └── package.json
├── server/            # Configuration serveur Minecraft
│   ├── mods/          # Mods du serveur
│   ├── config/        # Configurations
│   └── scripts/       # Scripts de gestion
├── api/               # API de distribution
│   └── src/
└── docs/              # Documentation
```

## 🚀 Installation

### Prérequis
- Node.js 18+
- Java 17+ (pour Minecraft 1.18+)
- 4GB RAM minimum

### Launcher
```bash
cd launcher
npm install
npm run dev
```

### API de Distribution
```bash
cd api
npm install
npm start
```

## 🔧 Configuration

Voir la documentation dans `/docs` pour la configuration détaillée.

## 📝 Licence

MIT
