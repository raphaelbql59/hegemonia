# 🏛️ ARCHITECTURE HEGEMONIA

**Version:** 1.0
**Date:** 2026-01-07
**Auteur:** Équipe Hegemonia

---

## 📋 TABLE DES MATIÈRES

1. [Vue d'Ensemble](#vue-densemble)
2. [Architecture Système](#architecture-système)
3. [Stack Technologique](#stack-technologique)
4. [Architecture Réseau](#architecture-réseau)
5. [Base de Données](#base-de-données)
6. [Composants Principaux](#composants-principaux)
7. [Flux de Données](#flux-de-données)
8. [Sécurité](#sécurité)
9. [Performances](#performances)
10. [Dépendances](#dépendances)
11. [Déploiement](#déploiement)

---

## 🎯 VUE D'ENSEMBLE

### Concept Global

**HEGEMONIA** est un serveur Minecraft géopolitique révolutionnaire qui simule un monde Earth réaliste à l'échelle 1:750 où les joueurs créent et gèrent des nations, font la guerre, développent leur économie, et influencent l'histoire mondiale.

### Objectifs Principaux

- ✅ Simuler des relations internationales complexes et réalistes
- ✅ Offrir une économie dynamique basée sur l'offre et la demande
- ✅ Créer un système de guerre tactique et stratégique
- ✅ Fournir une expérience immersive avec mods et plugins custom
- ✅ Assurer des performances optimales pour 50-100 joueurs simultanés
- ✅ Intégrer une plateforme web complète et un launcher personnalisé

### Spécifications Serveur

- **Hébergement:** VPS OVH
- **OS:** Debian 11
- **RAM:** 64 Go
- **Capacité initiale:** 50-100 joueurs simultanés
- **Langue:** Français
- **Budget plugins premium:** 100€ max

---

## 🏗️ ARCHITECTURE SYSTÈME

### Vue Globale

```
┌─────────────────────────────────────────────────────────────────┐
│                        COUCHE CLIENT                            │
├─────────────────────────────────────────────────────────────────┤
│  Launcher Custom (Tauri)  │  Site Web (Next.js)  │  Discord Bot │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
┌───────────────▼───────────────┐   ┌──────────▼──────────┐
│      VELOCITY PROXY           │   │    API REST         │
│   (Point d'entrée unique)     │   │   (Backend)         │
└───────────────┬───────────────┘   └──────────┬──────────┘
                │                               │
    ┌───────────┼───────────────────┬───────────┼──────┐
    │           │                   │           │      │
┌───▼───┐   ┌──▼──┐   ┌──────┐  ┌─▼──┐   ┌────▼────┐ │
│ LOBBY │   │EARTH│   │ WARS │  │RES │   │ EVENTS  │ │
│Server │   │Main │   │Server│  │Srv │   │ Server  │ │
└───┬───┘   └──┬──┘   └──┬───┘  └─┬──┘   └────┬────┘ │
    │          │         │        │           │      │
    └──────────┴─────────┴────────┴───────────┴──────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
    ┌───────▼────────┐           ┌─────────▼────────┐
    │  PostgreSQL    │           │      Redis       │
    │  (Database)    │           │     (Cache)      │
    └────────────────┘           └──────────────────┘
```

### Architecture Multi-Serveur

Le projet utilise une architecture **multi-serveur** via **Velocity Proxy** pour :
- Répartir la charge entre plusieurs serveurs spécialisés
- Permettre des transferts transparents entre mondes
- Isoler les ressources par type d'activité
- Faciliter la maintenance sans interruption totale

#### Serveurs Minecraft

| Serveur | Type | RAM | Rôle | Plugins Principaux |
|---------|------|-----|------|-------------------|
| **Velocity** | Proxy | 2GB | Routeur central, auth | VelocityCore |
| **Lobby** | Paper | 4GB | Hub central, marchés | HegemoniaNations, HegemoniaEconomy |
| **Earth** | Purpur | 24GB | Monde principal géopolitique | ALL custom plugins |
| **Wars** | Paper | 8GB | Instances de guerre | HegemoniaWar, HegemoniaWarfare |
| **Resources** | Paper | 8GB | Mondes de farm/minage | HegemoniaJobs, HegemoniaEconomy |
| **Events** | Paper | 4GB | Events spéciaux | HegemoniaEvents, HegemoniaQuests |

---

## 💻 STACK TECHNOLOGIQUE

### Backend Minecraft

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Proxy** | Velocity | Latest | Moderne, performant, extensible |
| **Serveur Core** | Paper/Purpur | 1.20.4+ | Optimisations, compatibilité plugins |
| **Langage Plugins** | Kotlin | 1.9+ | Moderne, concis, Java interop |
| **Build Tool** | Gradle | 8.5+ | Standard, puissant, cache efficace |
| **Mods** | Fabric/Forge | Latest | Mods client/serveur (armes, véhicules) |

### Base de Données

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **SGBD Principal** | PostgreSQL | 15+ | Robuste, ACID, JSON support |
| **Cache** | Redis | 7+ | Ultra-rapide, pub/sub, sessions |
| **Connexion Pool** | PgBouncer | Latest | Optimise connexions BD |
| **ORM** | Exposed (Kotlin) | Latest | Type-safe, Kotlin-native |

### Frontend & Web

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Framework** | Next.js | 14+ | SSR, performance, SEO |
| **Langage** | TypeScript | 5+ | Type safety, meilleure DX |
| **Styling** | Tailwind CSS | 3+ | Utility-first, rapide |
| **ORM Web** | Prisma | 5+ | Type-safe, migrations faciles |
| **Maps** | Leaflet/BlueMap | Latest | Cartes interactives |

### Launcher Custom

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Framework** | Tauri | 2+ | Léger (Rust), sécurisé |
| **UI** | React | 18+ | Composants réutilisables |
| **Styling** | Tailwind CSS | 3+ | Cohérence avec le site |
| **Auth** | Microsoft Auth | Latest | Authentification Minecraft |

### DevOps & Infrastructure

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Conteneurisation** | Docker | Latest | Isolation, reproductibilité |
| **Orchestration** | Docker Compose | Latest | Simple, efficace pour mono-serveur |
| **Reverse Proxy** | Nginx | Latest | Performance, SSL termination |
| **CI/CD** | GitHub Actions | - | Gratuit, intégré Git |
| **Monitoring** | Netdata | Latest | Léger, temps réel |
| **Backups** | Restic | Latest | Incrémental, chiffré |
| **SSL** | Let's Encrypt | - | Gratuit, automatisé |

### Communication

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Bot Discord** | Discord.js | Latest | Riche en features, bien maintenu |
| **Webhooks** | Custom | - | Notifications temps réel |

---

## 🌐 ARCHITECTURE RÉSEAU

### Flux de Connexion Joueur

```
1. Joueur lance Launcher Custom
   ↓
2. Auth Microsoft/Mojang
   ↓
3. Téléchargement/Vérification mods
   ↓
4. Connexion → Velocity Proxy (port 25565)
   ↓
5. Velocity → Routage vers Lobby
   ↓
6. Joueur choisit destination
   ↓
7. Transfert transparent vers serveur cible
```

### Ports Réseau

| Service | Port | Protocole | Exposition |
|---------|------|-----------|------------|
| **Velocity Proxy** | 25565 | TCP | Public |
| **Lobby** | 25566 | TCP | Interne |
| **Earth** | 25567 | TCP | Interne |
| **Wars** | 25568 | TCP | Interne |
| **Resources** | 25569 | TCP | Interne |
| **Events** | 25570 | TCP | Interne |
| **PostgreSQL** | 5432 | TCP | Interne |
| **Redis** | 6379 | TCP | Interne |
| **Web (HTTP)** | 80 | TCP | Public → Redirect |
| **Web (HTTPS)** | 443 | TCP | Public |
| **API** | 3001 | TCP | Interne (via Nginx) |
| **SSH** | Custom | TCP | Public (IP whitelist) |

### Sécurité Réseau

```
┌─────────────────────────────────────┐
│         FIREWALL (UFW)              │
├─────────────────────────────────────┤
│  ✅ Port 25565 (Minecraft)          │
│  ✅ Port 80/443 (Web)               │
│  ✅ Port SSH custom (whitelisted)   │
│  ❌ Tous les autres ports BLOQUÉS   │
└─────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────┐
│      FAIL2BAN + Rate Limiting       │
│  Protection DDoS et brute-force     │
└─────────────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────┐
│        NGINX Reverse Proxy          │
│  - SSL Termination                  │
│  - Load Balancing                   │
│  - Rate Limiting                    │
└─────────────────────────────────────┘
```

---

## 🗄️ BASE DE DONNÉES

### Schéma PostgreSQL

#### Base `hegemonia_main`

```sql
-- Nations et Gouvernement
CREATE TABLE nations (
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    tag VARCHAR(5) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL, -- MINOR, REGIONAL, MAJOR, SUPERPOWER
    government_type VARCHAR(50) NOT NULL,
    capital_region_id UUID,
    president_uuid UUID,
    treasury DECIMAL(20, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE nation_citizens (
    nation_id UUID REFERENCES nations(id),
    player_uuid UUID,
    role VARCHAR(50), -- CITIZEN, MINISTER, PRESIDENT
    joined_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (nation_id, player_uuid)
);

-- Territoires et Régions
CREATE TABLE regions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20), -- NATION, STATE, PROVINCE
    parent_region_id UUID REFERENCES regions(id),
    owner_nation_id UUID REFERENCES nations(id),
    capital_coords JSONB, -- {x, y, z}
    bounds JSONB, -- Polygon [[x1,z1], [x2,z2], ...]
    resources JSONB, -- {resource: abundance}
    created_at TIMESTAMP DEFAULT NOW()
);

-- Joueurs
CREATE TABLE players (
    uuid UUID PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    current_nation_id UUID REFERENCES nations(id),
    balance DECIMAL(20, 2) DEFAULT 0,
    reputation_individual JSONB, -- {category: score}
    first_join TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW(),
    playtime_minutes INTEGER DEFAULT 0
);

-- Économie
CREATE TABLE enterprises (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    owner_uuid UUID,
    owner_nation_id UUID REFERENCES nations(id),
    location_nation_id UUID REFERENCES nations(id),
    capital DECIMAL(20, 2) DEFAULT 0,
    employees INTEGER DEFAULT 0,
    efficiency DECIMAL(5, 2) DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE market_prices (
    resource VARCHAR(50) PRIMARY KEY,
    current_price DECIMAL(10, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    from_uuid UUID,
    to_uuid UUID,
    amount DECIMAL(20, 2) NOT NULL,
    type VARCHAR(50), -- TRADE, TAX, SALARY, etc.
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Diplomatie
CREATE TABLE treaties (
    id UUID PRIMARY KEY,
    name VARCHAR(200),
    type VARCHAR(50), -- PEACE, ALLIANCE, TRADE, etc.
    nations JSONB, -- Array of nation UUIDs
    terms JSONB, -- Conditions du traité
    signed_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE relations (
    nation_a_id UUID REFERENCES nations(id),
    nation_b_id UUID REFERENCES nations(id),
    status VARCHAR(50), -- FRIENDLY, NEUTRAL, HOSTILE, etc.
    value INTEGER DEFAULT 0, -- -100 à +100
    updated_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (nation_a_id, nation_b_id)
);

-- Guerres
CREATE TABLE wars (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    attackers JSONB, -- Array of nation UUIDs
    defenders JSONB,
    war_goals JSONB, -- Objectifs des deux côtés
    war_score INTEGER DEFAULT 0, -- -100 à +100
    status VARCHAR(20) DEFAULT 'ACTIVE',
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP
);

CREATE TABLE battles (
    id UUID PRIMARY KEY,
    war_id UUID REFERENCES wars(id),
    location JSONB, -- {x, y, z, world}
    attacker_nation_id UUID REFERENCES nations(id),
    defender_nation_id UUID REFERENCES nations(id),
    winner_nation_id UUID REFERENCES nations(id),
    participants JSONB, -- Players involved
    casualties JSONB, -- Stats
    fought_at TIMESTAMP DEFAULT NOW()
);

-- Jobs et Métiers
CREATE TABLE player_jobs (
    player_uuid UUID REFERENCES players(uuid),
    job_type VARCHAR(50),
    level INTEGER DEFAULT 1,
    xp INTEGER DEFAULT 0,
    specialization VARCHAR(50),
    PRIMARY KEY (player_uuid, job_type)
);

-- Logs et Audit
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    actor_uuid UUID,
    target_uuid UUID,
    details JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Index pour performances
CREATE INDEX idx_nations_type ON nations(type);
CREATE INDEX idx_players_nation ON players(current_nation_id);
CREATE INDEX idx_regions_owner ON regions(owner_nation_id);
CREATE INDEX idx_wars_status ON wars(status);
CREATE INDEX idx_transactions_date ON transactions(created_at);
CREATE INDEX idx_audit_logs_date ON audit_logs(created_at);
```

#### Base `hegemonia_web`

```sql
-- Utilisateurs web (comptes site/forum)
CREATE TABLE web_users (
    id SERIAL PRIMARY KEY,
    minecraft_uuid UUID UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(100),
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id INTEGER REFERENCES web_users(id),
    token VARCHAR(255) UNIQUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE api_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES web_users(id),
    token VARCHAR(255) UNIQUE,
    name VARCHAR(100),
    permissions JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);
```

### Redis Structure

```
# Cache données nations
nation:{uuid} → JSON nation data
nations:list → Sorted Set (by power)

# Cache prix marché
market:prices → Hash {resource: price}
market:history:{resource} → Time Series

# Sessions joueurs
player:session:{uuid} → JSON session data
player:online → Set of UUIDs

# Pub/Sub
channel:cross-server → Messages inter-serveurs
channel:wars → Notifications guerres
channel:economy → Updates économie

# Leaderboards
leaderboard:wealth → Sorted Set
leaderboard:military → Sorted Set
leaderboard:nations → Sorted Set

# Rate Limiting
ratelimit:api:{ip} → Counter with TTL
```

---

## 🧩 COMPOSANTS PRINCIPAUX

### 1. Plugins Custom (Kotlin/Java)

| Plugin | Package | Dépendances | Description |
|--------|---------|-------------|-------------|
| **HegemoniaNations** | `net.hegemonia.nations` | PostgreSQL, Redis | Gestion nations, territoires, gouvernement |
| **HegemoniaEconomy** | `net.hegemonia.economy` | HegemoniaNations, Vault | Économie, marchés, entreprises |
| **HegemoniaWar** | `net.hegemonia.war` | HegemoniaNations | Système de guerre, batailles, sièges |
| **HegemoniaDiplomacy** | `net.hegemonia.diplomacy` | HegemoniaNations | Traités, relations, ONU |
| **HegemoniaJobs** | `net.hegemonia.jobs` | HegemoniaEconomy | Métiers, progression, XP |
| **HegemoniaQuests** | `net.hegemonia.quests` | ALL | Système de quêtes |
| **HegemoniaEvents** | `net.hegemonia.events` | HegemoniaNations | Événements automatiques |
| **HegemoniaIntel** | `net.hegemonia.intel` | HegemoniaNations | Réputation, espionnage |
| **HegemoniaTech** | `net.hegemonia.tech` | HegemoniaNations | Recherche, technologies |
| **HegemoniaFaith** | `net.hegemonia.faith` | HegemoniaNations | Système de religion |

### 2. Mods Custom (Fabric/Forge)

| Mod | Side | Description |
|-----|------|-------------|
| **HegemoniaEnergy** | Client+Server | Système d'énergie, centrales, réseaux |
| **HegemoniaWarfare** | Client+Server | Armes modernes, véhicules, combat |
| **HegemoniaVehicles** | Client+Server | Voitures, tanks, avions, bateaux |
| **HegemoniaUI** | Client | Interface custom, HUD amélioré |

### 3. Plugins Premium (Budget 100€)

| Plugin | Prix | Usage | Justification |
|--------|------|-------|---------------|
| **BlueMap** | Gratuit | Carte web 3D | Meilleure carte web disponible |
| **Oraxen** | 15€ | Items custom | Ressources, items spéciaux |
| **ModelEngine** | Gratuit | Modèles 3D | Véhicules, structures |
| **MythicMobs** | Gratuit | Mobs custom | NPCs, événements |
| **Citizens** | Gratuit | NPCs | Personnages, marchands |
| **ProtocolLib** | Gratuit | Packets | Nécessaire pour certains plugins |
| **Grim AntiCheat** | 60€ | Anti-cheat | Meilleur anti-cheat disponible |
| **Reserve** | 25€ | Buffer | Imprévus/autres besoins |

Total: ~75€ / 100€

---

## 🔄 FLUX DE DONNÉES

### Connexion Joueur

```
1. Client → Velocity Proxy
   - Authentification
   - Anti-VPN check
   - Chargement données joueur (PostgreSQL)

2. Velocity → Serveur cible (Lobby par défaut)
   - Session créée (Redis)
   - Chargement inventaire cross-server

3. Serveur → Client
   - Envoi monde, chunks
   - Chargement UI custom (mod)
   - Sync données nation (si membre)
```

### Transaction Économique

```
1. Joueur initie transaction (achat/vente)
   ↓
2. Validation côté serveur
   - Fonds suffisants?
   - Item disponible?
   - Permissions OK?
   ↓
3. PostgreSQL: INSERT transaction
   ↓
4. Redis: UPDATE cache prix
   ↓
5. Pub/Sub: Notifier autres serveurs
   ↓
6. API: Webhook vers site web (update graphiques)
   ↓
7. Discord: Notification si transaction importante
```

### Déclaration de Guerre

```
1. Président nation A déclare guerre à nation B
   ↓
2. HegemoniaWar: Validation conditions
   - Casus belli valide?
   - Créneau horaire OK?
   - Trêve respectée?
   ↓
3. PostgreSQL: INSERT war
   ↓
4. Redis: Pub/Sub notification ALL servers
   ↓
5. Discord: Annonce dans #guerres
   ↓
6. Site web: Update page guerres
   ↓
7. In-game: Message broadcast + son alerte
```

---

## 🔒 SÉCURITÉ

### Niveaux de Sécurité

#### 1. Infrastructure (OS/Réseau)

```
✅ SSH key-only, port custom, IP whitelist
✅ UFW firewall strict
✅ Fail2Ban (SSH, Minecraft, Web)
✅ Certificats SSL Let's Encrypt
✅ Sauvegardes chiffrées automatiques (Restic)
✅ Monitoring 24/7 (Netdata)
✅ Updates automatiques sécurité (unattended-upgrades)
```

#### 2. Application (Minecraft)

```
✅ Anti-cheat premium (Grim AntiCheat)
✅ Validation côté serveur SYSTÉMATIQUE
✅ Permissions granulaires (LuckPerms)
✅ Rate limiting (actions par seconde)
✅ Logs complets (CoreProtect, custom)
✅ Hash vérification mods (launcher)
✅ Isolation serveurs (Docker)
```

#### 3. Données

```
✅ PostgreSQL: Connexions chiffrées (SSL)
✅ Mots de passe hashés (bcrypt)
✅ Tokens JWT pour API
✅ Validation inputs (SQL injection protection)
✅ RGPD compliant (export données, suppression)
✅ Backups incrémentaux quotidiens
✅ Chiffrement backups (AES-256)
```

#### 4. Web/API

```
✅ HTTPS obligatoire
✅ Headers sécurité (HSTS, CSP, etc.)
✅ Rate limiting API (express-rate-limit)
✅ CORS strict
✅ Authentification JWT
✅ Sanitization inputs
✅ Protection CSRF
```

---

## ⚡ PERFORMANCES

### Optimisations Minecraft

#### Paper/Purpur Configuration

```yaml
# paper-global.yml
chunk-loading:
  async-chunks: true
  autoconfig-send-distance: true

entity-activation-range:
  animals: 16
  monsters: 24
  raiders: 48
  misc: 8
  water: 16
  villagers: 16
  flying-monsters: 32

# paper-world-defaults.yml
mob-spawner-tick-rate: 2
optimize-explosions: true
max-auto-save-chunks-per-tick: 8
```

#### JVM Flags (Aikar's Flags optimisés)

```bash
# 24GB pour Earth server
-Xms24G -Xmx24G
-XX:+UseG1GC
-XX:+ParallelRefProcEnabled
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+DisableExplicitGC
-XX:+AlwaysPreTouch
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1HeapRegionSize=8M
-XX:G1ReservePercent=20
-XX:G1HeapWastePercent=5
-XX:G1MixedGCCountTarget=4
-XX:InitiatingHeapOccupancyPercent=15
-XX:G1MixedGCLiveThresholdPercent=90
-XX:G1RSetUpdatingPauseTimePercent=5
-XX:SurvivorRatio=32
-XX:+PerfDisableSharedMem
-XX:MaxTenuringThreshold=1
```

### Optimisations Base de Données

#### PostgreSQL (postgresql.conf)

```conf
# Adaptions pour 64GB RAM système (4GB alloués PostgreSQL)
shared_buffers = 1GB
effective_cache_size = 3GB
maintenance_work_mem = 256MB
work_mem = 16MB

# Checkpoints
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100

# Connexions
max_connections = 100
```

#### Redis (redis.conf)

```conf
maxmemory 2gb
maxmemory-policy allkeys-lru
save ""  # Pas de persistence (cache pur)
```

### Optimisations Web

```javascript
// Next.js - next.config.js
module.exports = {
  swcMinify: true,
  compress: true,
  images: {
    formats: ['image/avif', 'image/webp'],
  },
  experimental: {
    optimizeCss: true,
  }
}
```

---

## 🔗 DÉPENDANCES

### Matrice de Dépendances

```
HegemoniaCore (base)
  ↓
  ├─→ HegemoniaNations ← HegemoniaEconomy
  │        ↓                    ↓
  │        ├─→ HegemoniaWar ←──┤
  │        ├─→ HegemoniaDiplomacy
  │        ├─→ HegemoniaJobs ←─┤
  │        ├─→ HegemoniaTech
  │        ├─→ HegemoniaFaith
  │        └─→ HegemoniaIntel
  │
  └─→ HegemoniaQuests (dépend de tous)
       HegemoniaEvents (dépend de tous)
```

### Bibliothèques Externes

**Plugins:**
- Kotlin stdlib 1.9+
- Exposed (ORM)
- HikariCP (connexion pool)
- Jedis (Redis client)
- Kyori Adventure (composants texte)

**Web:**
- Next.js 14
- Prisma ORM
- SWR (data fetching)
- Chart.js (graphiques)
- Leaflet (cartes)

**Launcher:**
- Tauri 2
- React 18
- @microsoft/authentication (Minecraft auth)

---

## 🚀 DÉPLOIEMENT

### Architecture Docker

```yaml
# docker-compose.yml structure
services:
  postgres:
    image: postgres:15-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    networks:
      - hegemonia-backend

  redis:
    image: redis:7-alpine
    networks:
      - hegemonia-backend

  velocity:
    build: ./server/velocity
    ports:
      - "25565:25565"
    networks:
      - hegemonia-backend

  # ... autres serveurs

  web:
    build: ./web/frontend
    depends_on:
      - postgres
      - redis
    networks:
      - hegemonia-backend

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    networks:
      - hegemonia-backend

volumes:
  postgres_data:
  minecraft_data:

networks:
  hegemonia-backend:
    driver: bridge
```

### Processus de Déploiement

```
1. Préparation serveur (Ansible)
   - Install Docker, Docker Compose
   - Configuration firewall
   - Configuration SSH
   - Install monitoring

2. Build images
   - CI/CD GitHub Actions
   - Tests automatiques
   - Build Docker images
   - Push vers registry

3. Déploiement
   - Pull images sur serveur
   - docker-compose up -d
   - Migrations base de données
   - Vérifications santé

4. Post-déploiement
   - Tests smoke
   - Monitoring alertes
   - Backup immédiat
```

### Rollback Strategy

```
1. Tag chaque déploiement (Git + Docker)
2. Garder N-1 version prête
3. Script rollback automatique
4. Backup avant chaque déploiement
5. Rollback DB si nécessaire
```

---

## 📊 MÉTRIQUES DE SUCCÈS

### KPIs Techniques

- ✅ TPS serveur: >= 19.5 (minimum)
- ✅ Latence moyenne: < 50ms
- ✅ Temps chargement chunks: < 3s
- ✅ Uptime: >= 99.5%
- ✅ Temps requête API: < 200ms
- ✅ Temps chargement site: < 2s

### KPIs Joueurs

- ✅ Joueurs actifs quotidiens: 30+
- ✅ Temps session moyen: 2h+
- ✅ Rétention J7: >= 50%
- ✅ Nations actives: 10+
- ✅ Guerres actives par semaine: 2+

---

## 📝 CONCLUSION

Cette architecture a été conçue pour être:

- **Scalable:** Ajout de serveurs facile si croissance
- **Maintenable:** Code modulaire, documentation complète
- **Performante:** Optimisations à chaque niveau
- **Sécurisée:** Défense en profondeur
- **Évolutive:** Nouveaux features ajoutables facilement

Le projet HEGEMONIA est ambitieux mais réalisable avec une approche méthodique phase par phase.

---

**Document vivant - À mettre à jour au fil du projet**

*Dernière mise à jour: 2026-01-07*
