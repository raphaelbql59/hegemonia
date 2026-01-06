# 🌍 Hegemonia - Earth Nations RP Server

> **Un serveur Minecraft Earth RP avec système de nations, économie, guerres et technologies. Comprend un launcher custom ultra-moderne.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.15.0-orange.svg)](https://fabricmc.net/)
[![Node.js](https://img.shields.io/badge/Node.js-20.x-green.svg)](https://nodejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ Caractéristiques Principales

### 🏛️ Système de Nations
- Créez et gérez votre propre nation sur une carte Earth réaliste
- 4 types de gouvernements (Monarchie, Démocratie, Dictature, République)
- Système de rôles (Leader, Ministres, Citoyens)
- Gestion de trésorerie et taxation

### 💰 Économie Complexe
- Monnaie unique (HGN)
- 3 types de marchés (Local, International, Inter-Nations)
- Commerce de ressources
- Budget national

### ⚔️ Système de Guerre
- 5 types de guerres différents (Conquête, Économique, Indépendance, Punitive, Totale)
- Casus Belli réalistes
- Armes et véhicules custom
- Système de siège

### 🔬 Arbre Technologique
- 4 ères (Médiéval, Industriel, Moderne, Futuriste)
- Recherche nationale et individuelle
- Technologies déblocables
- Progression par ères

### 🚀 Launcher Custom
- Interface ultra-moderne (Electron + React)
- Auto-update des mods
- News du serveur en temps réel
- Carte interactive du monde
- Stats et classements

## 📦 Stack Technique

### Backend
- **API**: Node.js 20 + Express + TypeScript
- **Base de données**: PostgreSQL 15
- **Cache**: Redis 7
- **ORM**: Prisma

### Serveur Minecraft
- **Version**: 1.20.1
- **Modloader**: Fabric
- **Mods custom**: 4 mods principaux (Core, Economy, Warfare, Tech)

### Launcher
- **Framework**: Electron 28
- **Frontend**: React 18 + TypeScript
- **Build**: Webpack 5

### Infrastructure
- **Orchestration**: Docker Compose
- **Proxy**: Nginx
- **Serveur**: VPS OVH 64GB RAM

## 🚀 Quick Start

### Pour les Joueurs

1. **Téléchargez le launcher** (à venir)
2. **Installez et lancez**
3. **Connectez-vous**
4. **Jouez !**

### Pour les Développeurs

Voir [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) pour le guide complet.

### Déploiement Serveur

Voir [README_DEPLOYMENT.md](README_DEPLOYMENT.md) pour les instructions complètes.

**Quick deploy sur VPS :**

```bash
git clone <repo-url> /opt/hegemonia
cd /opt/hegemonia
chmod +x deploy.sh
sudo ./deploy.sh
```

## 📁 Structure du Projet

```
hegemonia/
├── api/                    # Backend API (Node.js + Prisma)
│   ├── src/
│   │   ├── routes/        # Endpoints REST
│   │   ├── services/      # Business logic
│   │   └── utils/         # Utilities
│   └── prisma/
│       └── schema.prisma  # Database schema
│
├── launcher/              # Launcher Electron
│   ├── src/
│   │   ├── main/         # Process principal
│   │   ├── renderer/     # Interface React
│   │   └── preload/      # IPC bridge
│   └── webpack.*.js      # Build configs
│
├── mods/                  # Mods Fabric custom
│   ├── hegemonia-core/    # Nations & territoires
│   ├── hegemonia-economy/ # Économie
│   ├── hegemonia-warfare/ # Guerres & combat
│   └── hegemonia-tech/    # Technologies
│
├── server/               # Config serveur Minecraft
│   ├── config/
│   └── scripts/
│
├── docs/                 # Documentation
│   ├── GAME_DESIGN.md
│   ├── TECHNICAL_ARCHITECTURE.md
│   └── ROADMAP.md
│
└── docker-compose.yml    # Orchestration
```

## 📚 Documentation

- **[Game Design](docs/GAME_DESIGN.md)** - Design complet du gameplay
- **[Technical Architecture](docs/TECHNICAL_ARCHITECTURE.md)** - Architecture technique
- **[Roadmap](docs/ROADMAP.md)** - Plan de développement (8 phases)
- **[Development Guide](docs/DEVELOPMENT.md)** - Guide développeur
- **[Deployment Guide](README_DEPLOYMENT.md)** - Guide de déploiement

## 🎯 Roadmap

**Phase 0** - Fondations (Semaine 1-2) ✅
- ✅ Infrastructure Docker
- ✅ API Backend complète
- ✅ Launcher Electron
- ⏳ Serveur Fabric vanilla

**Phase 1** - Core Gameplay (Semaine 3-5)
- ⏳ Système de nations
- ⏳ Économie basique
- ⏳ Territoires

**Phase 2** - Gouvernements & Diplomatie (Semaine 6-7)
**Phase 3** - Guerre Basique (Semaine 8-10)
**Phase 4** - Contenu Militaire (Semaine 11-13)
**Phase 5** - Technologies (Semaine 14-16)
**Phase 6** - Launcher Avancé (Semaine 17-18)
**Phase 7** - Polish & Optimisation (Semaine 19-20)
**Phase 8** - Alpha Publique (Semaine 21+)

Voir [docs/ROADMAP.md](docs/ROADMAP.md) pour les détails complets.

## 🛠️ Development

### Prérequis
- Node.js 20+
- Java 17+
- Docker & Docker Compose
- Git

### Setup Local

```bash
# Clone
git clone <repo-url>
cd hegemonia

# Backend API
cd api
npm install
docker-compose up postgres redis -d
npm run prisma:push
npm run dev

# Launcher
cd launcher
npm install
npm run dev
```

## 🤝 Contribution

Les contributions sont les bienvenues ! Voir [CONTRIBUTING.md](CONTRIBUTING.md).

1. Fork le projet
2. Créez une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📝 Licence

MIT License - Voir [LICENSE](LICENSE) pour plus de détails.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Documentation**: `/docs`
- **Discord**: (à venir)

## 🎮 Crédits

Développé avec ❤️ pour la communauté Minecraft RP.

**Inspirations**: NationsGlory, EarthMC

**Technologies utilisées**:
- Minecraft 1.20.1
- Fabric Mod Loader
- Electron
- React
- Node.js
- PostgreSQL
- Redis
- Docker

---

**⭐ Si ce projet vous plaît, n'hésitez pas à mettre une étoile !**
