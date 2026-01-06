# Architecture Technique - Hegemonia

## 🔧 Stack Technique

### Serveur Minecraft
- **Version** : Minecraft 1.20.1 (Java Edition)
- **Modloader** : Fabric 1.20.1
- **Server Software** : Fabric Server
- **Plugins Layer** : Fabric API + Custom mods
- **Base RAM** : 16GB alloués (sur VPS 64GB)
- **CPU** : 4-8 cores dédiés

### Base de Données
- **PostgreSQL 15** : Données nations, économie, joueurs
- **Redis** : Cache, sessions, données temps-réel
- **InfluxDB** : Metrics/stats (optionnel)

### Backend API
- **Node.js 20 + TypeScript**
- **Framework** : Express.js ou Fastify
- **ORM** : Prisma
- **WebSocket** : Socket.io (données temps-réel launcher)

### Launcher
- **Framework** : Electron 27+
- **Frontend** : React 18 + TypeScript
- **State** : Zustand ou Jotai
- **Style** : TailwindCSS + Framer Motion
- **Update** : electron-updater

## 📦 Mods Custom à Développer

### 1. `hegemonia-core` (Mod principal)
**Responsabilités :**
- Système de nations (création, gestion, rôles)
- Territoires et claims par région
- Système de gouvernement
- Events custom (guerre déclarée, etc.)

**Tech :**
- Fabric API
- Cardinal Components (data attachée aux joueurs/monde)
- Polymer (UI custom in-game)

### 2. `hegemonia-economy`
**Responsabilités :**
- Gestion monnaie (HGN)
- Shops (Marché, Commerce International)
- Budget national
- Taxes et salaires

**Intégration :**
- API REST pour stats économiques
- PostgreSQL pour transactions

### 3. `hegemonia-warfare`
**Responsabilités :**
- Système de guerre (types, casus belli)
- Armes custom (fusils, grenades, etc.)
- Véhicules (tanks, hélicos)
- Siège et moral

**Dépendances possibles :**
- **Immersive Engineering** (base machines/véhicules) - à évaluer
- Ou 100% custom avec Fabric rendering

### 4. `hegemonia-tech`
**Responsabilités :**
- Arbre technologique
- Recherche (nation + individuelle)
- Unlock crafts et items par ère

### 5. `hegemonia-integration`
**Responsabilités :**
- Communication avec API backend
- Sync données temps-réel
- WebSocket client in-game

## 🗺️ Mods Externes à Utiliser

### Map & Environnement
- **Terra 1-to-1** ou **Earth map pre-gen** : Carte Earth réaliste
- **Terralith** : Biomes améliorés (optionnel)
- **Xaero's Minimap** : Navigation (whitelist)

### Qualité de vie
- **Jade** : Affichage blocks/entities (HWYLA alternative)
- **ModMenu** : Menu mods propre
- **Fabric Language Kotlin** : Support Kotlin (si besoin)

### Performance
- **Lithium** : Optimisation serveur
- **FerriteCore** : Réduction RAM
- **Krypton** : Optimisation réseau
- **Chunky** : Pre-génération map

### Communication
- **PlasmoVoice** : Voice chat proximity (immersion RP)

## 🏗️ Architecture Réseau

```
┌─────────────────┐
│  Launcher       │
│  (Electron)     │
└────────┬────────┘
         │
         ├─> Auto-update (GitHub Releases)
         ├─> News/Stats (API REST)
         └─> WebSocket (events live)
              │
              ▼
    ┌──────────────────┐
    │   Backend API    │
    │   (Node.js)      │
    └────────┬─────────┘
             │
             ├─> PostgreSQL (données)
             ├─> Redis (cache)
             └─> Minecraft Server (RCON + WebSocket)
                      │
                      ▼
         ┌────────────────────────┐
         │  Minecraft Server      │
         │  Fabric 1.20.1         │
         │  + Mods Custom         │
         └────────────────────────┘
```

## 💾 Structure Base de Données

### Tables Principales

**nations**
- id, name, leader_uuid, government_type
- treasury, tax_rate, created_at
- capital_location

**players**
- uuid, username, nation_id, role
- balance, last_login, playtime
- profession

**territories**
- id, region_name, nation_id
- resource_type, production_rate
- coordinates (polygon)

**wars**
- id, attacker_id, defender_id, war_type
- start_date, end_date, status
- casus_belli

**technologies**
- id, nation_id, tech_name, tier
- unlocked_at, cost

**transactions**
- id, from_uuid, to_uuid, amount
- type (trade/salary/tax), timestamp

## 🚀 Déploiement VPS (OVH 64GB)

### Docker Compose

```yaml
services:
  minecraft:
    image: itzg/minecraft-server
    ports:
      - "25565:25565"
    volumes:
      - ./server:/data
    environment:
      TYPE: FABRIC
      VERSION: 1.20.1
      MEMORY: 16G

  postgres:
    image: postgres:15
    volumes:
      - ./db:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: hegemonia

  redis:
    image: redis:7-alpine

  api:
    build: ./api
    ports:
      - "3000:3000"
    depends_on:
      - postgres
      - redis

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

### Ressources Allouées
- **Minecraft** : 16GB RAM, 4 cores
- **PostgreSQL** : 4GB RAM, 2 cores
- **Redis** : 2GB RAM, 1 core
- **API** : 2GB RAM, 2 cores
- **Nginx** : 1GB RAM, 1 core
- **Système** : 39GB libres

## 📡 API REST Endpoints

```
GET  /api/nations              - Liste nations
GET  /api/nations/:id          - Détails nation
GET  /api/players/:uuid        - Profil joueur
GET  /api/wars                 - Guerres actives
GET  /api/map/territories      - Carte territoires (GeoJSON)
GET  /api/economy/market       - Prix marché
POST /api/auth/login           - Login launcher
GET  /api/launcher/version     - Dernière version
GET  /api/launcher/mods        - Liste mods + hash
```

## 🔐 Sécurité

- **Whitelist** : Activée (via launcher uniquement)
- **Anti-cheat** : Integrated dans mods custom
- **Rate limiting** : API (100 req/min)
- **Encryption** : HTTPS/WSS uniquement
- **Backup** : Quotidien (monde + DB)

## 📈 Monitoring

- **Prometheus** : Metrics serveur
- **Grafana** : Dashboards
- **Logs** : Centralisés (Loki ou ELK)

---

**Prochaine étape** : Roadmap de développement par phases
