# 🌍 HEGEMONIA - Serveur Minecraft Géopolitique

![Status](https://img.shields.io/badge/status-en%20d%C3%A9veloppement-yellow)
![Version](https://img.shields.io/badge/version-0.1.0--alpha-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.20.4-green)
![License](https://img.shields.io/badge/license-Proprietary-red)

**HEGEMONIA** est un serveur Minecraft géopolitique révolutionnaire qui simule un monde Earth réaliste où les joueurs créent des nations, font la guerre, développent leur économie, et influencent l'histoire mondiale.

---

## 📋 Vue d'Ensemble

### Concept

Inspiré de Nation's Glory mais significativement amélioré, HEGEMONIA offre :

- 🗺️ **Carte Earth 1:750** - Monde réaliste avec topographie et ressources géographiquement correctes
- 🏛️ **Système de Nations** - Créez et gérez votre nation avec différents types de gouvernement
- ⚔️ **Guerre Tactique** - Batailles, sièges, objectifs stratégiques avec créneaux horaires
- 💰 **Économie Dynamique** - Marché basé sur l'offre/demande, entreprises, ressources stratégiques
- 🔋 **Système d'Énergie** - Centrales électriques, réseaux, production/consommation
- 🚗 **Véhicules Modernes** - Voitures, tanks, avions, bateaux avec combat réaliste
- 💼 **Métiers et Progression** - 20+ métiers avec spécialisations et compétences uniques
- 🌐 **Intégration Web** - Site interactif avec carte live, statistiques, et API complète

### Caractéristiques Principales

#### Système de Nations
- Gouvernements variés (Démocratie, Monarchie, Dictature, etc.)
- Claims automatiques par régions prédéfinies (pas de grief manuel)
- Système d'empire et vassaux
- Ministères et rôles gouvernementaux
- Élections et votes (selon régime)

#### Système de Guerre
- Déclaration avec objectifs (War Goals)
- Batailles en temps réel dans zones définies
- Sièges de points stratégiques
- War Score dynamique
- Traités de paix négociables
- Créneaux horaires (18h-23h semaine, 14h-00h weekend)

#### Économie
- Monnaie unique : Hegemonia Dollar (H$)
- Marché capital (lobby) avec prix fixes
- Bourse internationale dynamique
- Entreprises joueurs/nations
- Taxes et commerce international
- Chaînes de production réalistes

#### Militaire
- Armes modernes (fusils, sniper, roquettes, etc.)
- Véhicules (terrestres, aériens, maritimes)
- Programme nucléaire (conditions strictes)
- Arsenal varié et balancé

### Stack Technique

**Backend:**
- Velocity Proxy (routage)
- Paper/Purpur 1.20.4 (serveurs)
- Kotlin (plugins custom)
- Fabric/Forge (mods custom)

**Base de Données:**
- PostgreSQL 15+ (données principales)
- Redis 7+ (cache, pub/sub)

**Frontend:**
- Next.js 14 (site web)
- TypeScript + Tailwind CSS
- Leaflet/BlueMap (cartes)

**Launcher:**
- Tauri 2 (Rust + React)
- Auto-update et sync mods

---

## 📁 Structure du Projet

```
hegemonia-project/
├── docs/                          # Documentation
│   ├── ARCHITECTURE.md           # Architecture système
│   ├── DEVELOPMENT_PLAN.md       # Plan de développement
│   ├── INSTALLATION.md           # Guide installation (à venir)
│   ├── CONFIGURATION.md          # Guide configuration (à venir)
│   └── API.md                    # Documentation API (à venir)
├── server/                        # Serveurs Minecraft
│   ├── velocity/                 # Proxy Velocity
│   ├── paper/                    # Serveurs Paper/Purpur
│   │   ├── lobby/
│   │   ├── earth/               # Serveur principal
│   │   ├── wars/
│   │   ├── resources/
│   │   └── events/
│   ├── plugins/                  # Plugins custom
│   │   ├── HegemoniaNations/
│   │   ├── HegemoniaEconomy/
│   │   ├── HegemoniaWar/
│   │   └── ...
│   ├── mods/                     # Mods custom
│   │   ├── HegemoniaEnergy/
│   │   ├── HegemoniaWarfare/
│   │   └── ...
│   └── configs/                  # Configurations
├── database/                      # Base de données
│   ├── postgresql/
│   └── redis/
├── web/                          # Site web
│   ├── frontend/                # Next.js app
│   ├── backend/                 # Backend API (si séparé)
│   └── api/
├── launcher/                     # Launcher custom
├── scripts/                      # Scripts utilitaires
├── backups/                      # Backups
├── PROGRESS.md                   # Suivi progression
├── README.md                     # Ce fichier
└── .gitignore
```

---

## 🚀 Démarrage Rapide

### Prérequis

- VPS/Serveur dédié : Debian 11+, 64GB RAM (recommandé)
- Java 21 (Temurin/OpenJDK)
- Docker + Docker Compose
- Node.js 20+
- PostgreSQL 15+
- Redis 7+

### Installation (Développement)

```bash
# 1. Cloner le repository
git clone https://github.com/votre-org/hegemonia-project.git
cd hegemonia-project

# 2. Installation dépendances
./scripts/install-dependencies.sh

# 3. Configuration base de données
docker-compose up -d postgres redis

# 4. Configuration initiale
cp .env.example .env
# Éditer .env avec vos paramètres

# 5. Build plugins
cd server/plugins/HegemoniaNations
./gradlew shadowJar

# 6. Démarrer serveurs
./scripts/start-servers.sh

# 7. Lancer site web (dev)
cd web/frontend
npm install
npm run dev
```

### Installation (Production)

Voir [docs/INSTALLATION.md](docs/INSTALLATION.md) (à venir)

---

## 📖 Documentation

- [📐 Architecture](docs/ARCHITECTURE.md) - Vue d'ensemble architecture système
- [🗺️ Plan de Développement](docs/DEVELOPMENT_PLAN.md) - Roadmap et stratégie
- [📈 Progression](PROGRESS.md) - Suivi détaillé de l'avancement
- 🔧 [Installation](docs/INSTALLATION.md) - Guide installation complet (à venir)
- ⚙️ [Configuration](docs/CONFIGURATION.md) - Configuration serveurs (à venir)
- 🌐 [API](docs/API.md) - Documentation API REST (à venir)

---

## 🎯 Roadmap

### Phase 0 : Planification ✅ (EN COURS)
- [x] Structure projet
- [x] Architecture documentée
- [x] Plan de développement
- [ ] Initialisation Git

### Phase 1 : Infrastructure (Janvier 2026)
- [ ] Sécurisation VPS
- [ ] Configuration Velocity + serveurs
- [ ] Base de données PostgreSQL
- [ ] Cache Redis

### Phase 2 : Launcher Custom (Janvier 2026)
- [ ] Interface Tauri
- [ ] Authentification Microsoft
- [ ] Téléchargement automatique mods
- [ ] Auto-updater

### Phase 3 : Carte Earth (Janvier-Février 2026)
- [ ] Génération carte 1:750
- [ ] Placement ressources
- [ ] Définition régions
- [ ] Optimisation

### Phase 4-6 : Gameplay Core (Février-Mars 2026)
- [ ] Plugin HegemoniaNations (nations, gouvernement)
- [ ] Plugin HegemoniaWar (guerres, batailles)
- [ ] Plugin HegemoniaEconomy (économie, marchés)

### Phase 7-15 : Features Avancées (Mars-Mai 2026)
- [ ] Système d'énergie
- [ ] Armes et véhicules
- [ ] Métiers et progression
- [ ] Religion, quêtes, espionnage
- [ ] Technologies

### Phase 16-20 : Lancement (Juin-Juillet 2026)
- [ ] Site web + API
- [ ] Anti-cheat
- [ ] Optimisation performances
- [ ] Documentation
- [ ] Beta testing
- [ ] **LANCEMENT PUBLIC**

**Date cible lancement :** Juillet 2026

---

## 🤝 Contribution

Pour le moment, le projet est en développement privé. La contribution sera ouverte après la beta.

### Workflow Git

```bash
# Créer une branche feature
git checkout -b feature/nom-feature

# Développer, commit
git add .
git commit -m "[FEATURE] Scope: Description"

# Push et créer PR
git push origin feature/nom-feature
```

### Conventions

- **Commits :** `[TYPE] Scope: Description`
  - Types: FEATURE, FIX, REFACTOR, DOCS, TEST, PERF, STYLE, CHORE
- **Branches :** `feature/`, `fix/`, `hotfix/`
- **Code :** Suivre les conventions Kotlin/TypeScript
- **Documentation :** Toujours documenter les nouvelles features

---

## 📊 Statut Actuel

**Version :** 0.1.0-alpha
**Phase :** 0 - Planification (95%)
**Progression globale :** 5%

Voir [PROGRESS.md](PROGRESS.md) pour le détail complet.

---

## 📝 Licence

Ce projet est propriétaire. Tous droits réservés.

**© 2026 Hegemonia Team**

---

## 📞 Contact

- **Discord :** (à venir)
- **Site web :** (à venir)
- **Email :** (à venir)

---

## 🙏 Remerciements

- Nation's Glory pour l'inspiration
- La communauté Minecraft
- PaperMC pour leur excellent travail
- Tous les contributeurs open-source

---

**Construisons ensemble le meilleur serveur géopolitique Minecraft !** 🌍⚔️💰

*Dernière mise à jour : 2026-01-07*
