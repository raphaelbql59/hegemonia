# 🗺️ HEGEMONIA - PLAN DE DÉVELOPPEMENT DÉTAILLÉ

**Version:** 1.0
**Date:** 2026-01-07
**Objectif:** Roadmap technique et stratégie de développement

---

## 📋 TABLE DES MATIÈRES

1. [Approche Générale](#approche-générale)
2. [Stratégie de Développement](#stratégie-de-développement)
3. [Ordre de Priorité](#ordre-de-priorité)
4. [Dépendances entre Phases](#dépendances-entre-phases)
5. [Stack Technique Détaillée](#stack-technique-détaillée)
6. [Workflow de Développement](#workflow-de-développement)
7. [Tests et Validation](#tests-et-validation)
8. [Déploiement Continu](#déploiement-continu)

---

## 🎯 APPROCHE GÉNÉRALE

### Philosophie de Développement

**HEGEMONIA** suit une approche de développement **itérative et incrémentale** :

1. **Fondations d'abord** - Infrastructure solide avant features
2. **MVP rapide** - Version minimale fonctionnelle pour tests
3. **Itération continue** - Amélioration progressive
4. **Feedback loops** - Tests utilisateurs réguliers
5. **Documentation parallèle** - Code documenté au fur et à mesure

### Méthodologie

```
Phase N:
  ├─ 1. Planification détaillée (1 jour)
  ├─ 2. Développement (70% du temps)
  ├─ 3. Tests (20% du temps)
  ├─ 4. Documentation (10% du temps)
  └─ 5. Review & Ajustements
```

### Principes SOLID

Tous les plugins custom suivront les principes SOLID :
- **S**ingle Responsibility
- **O**pen/Closed
- **L**iskov Substitution
- **I**nterface Segregation
- **D**ependency Inversion

---

## 🚀 STRATÉGIE DE DÉVELOPPEMENT

### Phase 1-6 : Cœur du Gameplay (CRITIQUE)

Ces phases constituent le **Minimum Viable Product (MVP)** :

```
Infrastructure → Nations → Guerre → Économie
```

**Objectif MVP:** Serveur jouable avec features essentielles
**Timeline MVP:** ~2 mois

### Phase 7-15 : Features Avancées (IMPORTANTE)

Features qui enrichissent l'expérience :

```
Énergie → Militaire → Jobs → Religion → Quêtes → Intel → Events → Diplo → Tech
```

**Objectif:** Profondeur et complexité
**Timeline:** ~3 mois

### Phase 16-20 : Polish & Lancement (FINITION)

Préparation au lancement :

```
Web → Anti-cheat → Optimisation → Documentation → Lancement
```

**Objectif:** Production-ready
**Timeline:** ~1 mois

---

## 📊 ORDRE DE PRIORITÉ

### Niveau 1 : CRITIQUE (Ne peut pas lancer sans)

```
✅ Phase 0  : Planification
🔴 Phase 1  : Infrastructure
🔴 Phase 3  : Carte Earth
🔴 Phase 4  : Nations
🔴 Phase 5  : Guerre
🔴 Phase 6  : Économie
🔴 Phase 17 : Anti-cheat
🔴 Phase 18 : Optimisation
```

### Niveau 2 : IMPORTANTE (Fortement recommandé)

```
🟠 Phase 2  : Launcher
🟠 Phase 7  : Énergie
🟠 Phase 8  : Militaire (armes/véhicules)
🟠 Phase 14 : Diplomatie
🟠 Phase 16 : Web + API
🟠 Phase 19 : Documentation
```

### Niveau 3 : UTILE (Améliore l'expérience)

```
🟡 Phase 9  : Jobs
🟡 Phase 11 : Quêtes
🟡 Phase 12 : Intel/Espionnage
🟡 Phase 13 : Events auto
🟡 Phase 15 : Technologies
```

### Niveau 4 : BONUS (Nice to have)

```
🟢 Phase 10 : Religion
```

---

## 🔗 DÉPENDANCES ENTRE PHASES

### Graphe de Dépendances

```
Phase 0 (Planification)
  │
  ├──→ Phase 1 (Infrastructure) ────┐
  │                                  │
  │                    ┌─────────────┴──────────────┐
  │                    │                            │
  ├──→ Phase 2 (Launcher)              Phase 3 (Carte) ←─ Parallèle possible
  │    [Indépendant]                        │
  │                                         │
  │                                  Phase 4 (Nations) ←─── Base système
  │                                         │
  │                        ┌────────────────┼──────────────┐
  │                        │                │              │
  │                  Phase 5 (Guerre)  Phase 6 (Éco)  Phase 14 (Diplo)
  │                        │                │              │
  │          ┌─────────────┼────────────────┼──────────────┤
  │          │             │                │              │
  │    Phase 7 (Énergie)  Phase 8      Phase 9 (Jobs)   Phase 15
  │          │         (Militaire)          │           (Tech)
  │          │             │                │              │
  │          └─────────────┼────────────────┼──────────────┤
  │                        │                │              │
  │                   Phase 10         Phase 11        Phase 12
  │                  (Religion)       (Quêtes)        (Intel)
  │                        │                │              │
  │                        └────────────────┼──────────────┘
  │                                         │
  │                                    Phase 13 (Events)
  │                                         │
  ├──→ Phase 16 (Web/API) ←─────────────────┘
  │         │
  │         ├──→ Phase 17 (Anti-cheat)
  │         │
  │         └──→ Phase 18 (Optimisation)
  │                     │
  │              Phase 19 (Documentation)
  │                     │
  └──────────────→ Phase 20 (Lancement)
```

### Phases Parallélisables

Certaines phases peuvent être développées en parallèle :

**Groupe 1 (Après Phase 1):**
- Phase 2 (Launcher) - Indépendant
- Phase 3 (Carte) - Indépendant

**Groupe 2 (Après Phase 4):**
- Phase 5 (Guerre)
- Phase 6 (Économie)
- Phase 14 (Diplomatie)

**Groupe 3 (Après Phase 6):**
- Phase 7 (Énergie)
- Phase 8 (Militaire)
- Phase 9 (Jobs)
- Phase 15 (Tech)

---

## 💻 STACK TECHNIQUE DÉTAILLÉE

### Backend Minecraft

#### Plugins Custom (Kotlin)

**Setup Projet Type:**

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Redis
    implementation("redis.clients:jedis:5.1.0")

    // Utilities
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    // Config
    implementation("org.spongepowered:configurate-yaml:4.1.2")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
}
```

**Structure Type Plugin:**

```
src/
├── main/
│   ├── kotlin/
│   │   └── net/hegemonia/{plugin}/
│   │       ├── {Plugin}Main.kt         # Entry point
│   │       ├── commands/               # Commandes
│   │       ├── listeners/              # Event listeners
│   │       ├── models/                 # Data classes
│   │       ├── database/               # DB access
│   │       │   ├── DatabaseManager.kt
│   │       │   ├── tables/             # Exposed tables
│   │       │   └── repositories/       # Data repos
│   │       ├── cache/                  # Redis cache
│   │       ├── services/               # Business logic
│   │       ├── utils/                  # Utilities
│   │       └── config/                 # Configuration
│   └── resources/
│       ├── plugin.yml
│       └── config.yml
└── test/
    └── kotlin/
        └── net/hegemonia/{plugin}/
            └── ...tests...
```

#### Mods Custom (Fabric)

**Setup Projet Type:**

```gradle
// build.gradle
plugins {
    id 'fabric-loom' version '1.5-SNAPSHOT'
    id 'org.jetbrains.kotlin.jvm' version '1.9.22'
}

dependencies {
    // Minecraft
    minecraft "com.mojang:minecraft:1.20.4"
    mappings "net.fabricmc:yarn:1.20.4+build.3:v2"

    // Fabric
    modImplementation "net.fabricmc:fabric-loader:0.15.3"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.92.0+1.20.4"
    modImplementation "net.fabricmc:fabric-language-kotlin:1.10.17+kotlin.1.9.22"

    // Custom libs
    include implementation("...") // Libs embarquées
}
```

### Web Stack

#### Frontend (Next.js)

**Structure Projet:**

```
web/frontend/
├── src/
│   ├── app/                    # App Router (Next.js 14)
│   │   ├── (public)/          # Routes publiques
│   │   │   ├── page.tsx       # Accueil
│   │   │   ├── nations/
│   │   │   ├── wars/
│   │   │   └── economy/
│   │   ├── (auth)/            # Routes authentifiées
│   │   │   ├── dashboard/
│   │   │   └── profile/
│   │   ├── api/               # API routes
│   │   └── layout.tsx
│   ├── components/            # Composants React
│   │   ├── ui/               # Composants UI base
│   │   ├── nations/
│   │   ├── wars/
│   │   └── economy/
│   ├── lib/                   # Utils
│   │   ├── api/              # API clients
│   │   ├── auth/             # Auth helpers
│   │   └── utils/
│   ├── hooks/                # Custom hooks
│   ├── types/                # TypeScript types
│   └── styles/               # Styles globaux
├── public/
│   ├── images/
│   └── icons/
├── next.config.js
├── tailwind.config.js
├── tsconfig.json
└── package.json
```

**Technologies:**

```json
{
  "dependencies": {
    "next": "^14.1.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "typescript": "^5.3.3",
    "@prisma/client": "^5.8.0",
    "swr": "^2.2.4",
    "chart.js": "^4.4.1",
    "react-chartjs-2": "^5.2.0",
    "leaflet": "^1.9.4",
    "react-leaflet": "^4.2.1",
    "zustand": "^4.5.0",
    "zod": "^3.22.4",
    "@auth/core": "^0.18.6",
    "next-auth": "^5.0.0-beta.4"
  },
  "devDependencies": {
    "tailwindcss": "^3.4.1",
    "prettier": "^3.2.4",
    "eslint": "^8.56.0"
  }
}
```

#### Backend API

Intégré dans Next.js via API Routes + serveur externe si nécessaire.

### Base de Données

#### PostgreSQL Schema

**Migrations avec Exposed:**

```kotlin
// Migration exemple
object V1__InitialSchema : Migration {
    override fun run() {
        SchemaUtils.create(
            Nations,
            Players,
            Regions,
            Wars,
            Treaties,
            Transactions
        )
    }
}
```

#### Redis Structure

```kotlin
object RedisKeys {
    // Nations
    fun nation(uuid: UUID) = "nation:$uuid"
    fun nationsLeaderboard() = "nations:leaderboard"

    // Players
    fun playerSession(uuid: UUID) = "player:session:$uuid"
    fun playersOnline() = "players:online"

    // Economy
    fun marketPrice(resource: String) = "market:price:$resource"
    fun marketHistory(resource: String) = "market:history:$resource"

    // Cache TTL
    const val NATION_TTL = 300 // 5 min
    const val MARKET_TTL = 60 // 1 min
    const val SESSION_TTL = 3600 // 1h
}
```

### Launcher (Tauri)

**Structure Projet:**

```
launcher/
├── src-tauri/              # Backend Rust
│   ├── src/
│   │   ├── main.rs
│   │   ├── auth/          # Microsoft auth
│   │   ├── downloader/    # Téléchargement mods
│   │   ├── launcher/      # Lancement MC
│   │   └── updater/       # Auto-update
│   ├── Cargo.toml
│   └── tauri.conf.json
├── src/                    # Frontend React
│   ├── components/
│   ├── pages/
│   ├── hooks/
│   ├── utils/
│   └── styles/
├── package.json
└── vite.config.ts
```

---

## 🔄 WORKFLOW DE DÉVELOPPEMENT

### Git Workflow

**Branching Strategy:**

```
main (production)
  │
  ├── develop (développement principal)
  │     │
  │     ├── feature/nations-system
  │     ├── feature/war-mechanics
  │     ├── feature/economy-markets
  │     │
  │     ├── fix/nation-claim-bug
  │     └── fix/economy-duplication
  │
  └── hotfix/critical-crash (merge direct à main)
```

**Commit Convention:**

```
[TYPE] Scope: Description courte

Body détaillé si nécessaire

BREAKING CHANGE: Si changement majeur
Fixes #123
```

Types:
- `[FEATURE]` - Nouvelle fonctionnalité
- `[FIX]` - Correction de bug
- `[REFACTOR]` - Refactoring
- `[DOCS]` - Documentation
- `[TEST]` - Tests
- `[PERF]` - Performance
- `[STYLE]` - Formatage
- `[CHORE]` - Maintenance

**Exemples:**

```
[FEATURE] Nations: Add government types system

Implemented democracy, monarchy, dictatorship, oligarchy, and theocracy government types with associated mechanics.

[FIX] Economy: Fix market price calculation

Market prices were not properly calculated when supply was zero. Added fallback to base price.

Fixes #45

[PERF] Database: Optimize nation queries with indexes

Added indexes on frequently queried columns (type, owner_nation_id).
Query time reduced from 250ms to 15ms.
```

### Code Review

**Checklist:**

- [ ] Code suit les conventions Kotlin/TypeScript
- [ ] Tests écrits et passent
- [ ] Documentation à jour
- [ ] Pas de secrets hardcodés
- [ ] Performances acceptables
- [ ] Compatibilité vérifiée
- [ ] Logs appropriés

### CI/CD Pipeline

**GitHub Actions:**

```yaml
# .github/workflows/plugins.yml
name: Build Plugins

on:
  push:
    branches: [develop, main]
    paths:
      - 'server/plugins/**'
  pull_request:
    branches: [develop]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}

      - name: Build plugins
        run: ./gradlew build

      - name: Run tests
        run: ./gradlew test

      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: plugins
          path: server/plugins/*/build/libs/*.jar
```

---

## 🧪 TESTS ET VALIDATION

### Types de Tests

#### 1. Tests Unitaires (Plugins)

```kotlin
// Exemple test HegemoniaNations
@Test
fun `test nation creation with valid parameters`() {
    val nation = Nation(
        name = "France",
        tag = "FRA",
        type = NationType.MAJOR,
        governmentType = GovernmentType.DEMOCRACY
    )

    assertEquals("France", nation.name)
    assertEquals("FRA", nation.tag)
    assertTrue(nation.isValid())
}

@Test
fun `test nation cannot declare war without casus belli`() {
    val nationA = createTestNation("A")
    val nationB = createTestNation("B")

    val result = warManager.declareWar(nationA, nationB, emptyList())

    assertFalse(result.success)
    assertEquals(WarErrorCode.NO_CASUS_BELLI, result.errorCode)
}
```

#### 2. Tests d'Intégration

```kotlin
@Test
fun `test full war declaration flow`() {
    // Setup
    val attacker = createNationWithTerritory()
    val defender = createNationWithTerritory()

    // Action
    val warGoal = WarGoal(WarGoalType.ANNEX_REGION, defender.capitalRegion)
    val war = warManager.declareWar(attacker, defender, listOf(warGoal))

    // Verify
    assertTrue(war.isActive())
    assertEquals(1, war.warGoals.size)
    assertTrue(notificationService.wasSent(defender))
}
```

#### 3. Tests de Charge (Performance)

```kotlin
@Test
fun `test 100 concurrent nation queries`() = runBlocking {
    val start = System.currentTimeMillis()

    val jobs = (1..100).map { id ->
        async {
            nationRepository.getNation(UUID.randomUUID())
        }
    }

    jobs.awaitAll()
    val duration = System.currentTimeMillis() - start

    assertTrue(duration < 1000, "100 queries took ${duration}ms (should be < 1000ms)")
}
```

#### 4. Tests E2E (End-to-End)

Utiliser un framework comme Playwright pour tester le site web complet.

### Validation Manuelle

**Checklist par Phase:**

- [ ] Tests fonctionnels complets
- [ ] Tests cross-server (si applicable)
- [ ] Tests avec vrais joueurs (beta)
- [ ] Vérification performances (TPS, latence)
- [ ] Vérification logs (pas d'erreurs)
- [ ] Tests edge cases
- [ ] Tests charge (stress test)

---

## 🚢 DÉPLOIEMENT CONTINU

### Environnements

```
Development (local)
  ↓
Staging (serveur test)
  ↓
Production (serveur principal)
```

### Processus de Déploiement

#### 1. Build

```bash
# Plugins
./gradlew clean shadowJar

# Web
cd web/frontend && npm run build

# Launcher
cd launcher && npm run tauri:build
```

#### 2. Tests

```bash
# Unitaires + Intégration
./gradlew test

# E2E
npm run test:e2e
```

#### 3. Déploiement

```bash
# Via Docker Compose
docker-compose down
docker-compose pull
docker-compose up -d

# Vérification santé
./scripts/health-check.sh
```

#### 4. Rollback si problème

```bash
# Retour version précédente
git checkout v1.2.3
docker-compose up -d
```

### Monitoring Post-Déploiement

```
✅ Serveur démarre correctement
✅ Joueurs peuvent se connecter
✅ TPS > 19.5
✅ Pas d'erreurs critiques dans logs
✅ Base de données répond
✅ Redis opérationnel
✅ Site web accessible
```

---

## 📈 MÉTRIQUES DE SUCCÈS

### Par Phase

Chaque phase doit atteindre ces critères avant validation :

- ✅ **Fonctionnel:** Feature complète et utilisable
- ✅ **Testé:** Tests passent (unitaires + intégration)
- ✅ **Performant:** Pas de dégradation TPS
- ✅ **Documenté:** README + code comments
- ✅ **Reviewé:** Code review effectuée

### Global Projet

- ✅ TPS moyen >= 19.5
- ✅ Latence moyenne < 50ms
- ✅ Temps démarrage serveur < 2min
- ✅ Uptime >= 99.5%
- ✅ 0 bug critique en production
- ✅ 90%+ satisfaction joueurs beta

---

## 🎓 BONNES PRATIQUES

### Code Quality

```kotlin
// ✅ BON
class NationManager(
    private val repository: NationRepository,
    private val cache: CacheService
) {
    suspend fun getNation(id: UUID): Nation? {
        return cache.get("nation:$id")
            ?: repository.find(id)?.also { cache.set("nation:$id", it) }
    }
}

// ❌ MAUVAIS
class NationManager {
    fun getNation(id: UUID): Nation? {
        val db = Database.connect() // Créer connexion à chaque fois
        return db.query("SELECT * FROM nations WHERE id = '$id'") // SQL injection
    }
}
```

### Sécurité

```kotlin
// ✅ Toujours valider côté serveur
fun handleNationCreate(player: Player, name: String) {
    // Vérifications
    if (!player.hasPermission("hegemonia.nation.create")) {
        player.sendMessage("Pas de permission")
        return
    }

    if (name.length < 3 || name.length > 50) {
        player.sendMessage("Nom invalide (3-50 caractères)")
        return
    }

    if (nationRepository.existsByName(name)) {
        player.sendMessage("Nation déjà existante")
        return
    }

    // OK, créer nation
    val nation = nationService.createNation(player, name)
    player.sendMessage("Nation ${nation.name} créée!")
}
```

### Performance

```kotlin
// ✅ Batch operations
suspend fun loadNations(ids: List<UUID>): List<Nation> {
    return transaction {
        Nations.select { Nations.id inList ids }
            .map { it.toNation() }
    }
}

// ❌ N+1 queries
suspend fun loadNations(ids: List<UUID>): List<Nation> {
    return ids.map { id ->
        transaction {
            Nations.select { Nations.id eq id }
                .single()
                .toNation()
        }
    }
}
```

---

## 🔧 OUTILS DE DÉVELOPPEMENT

### IDE Recommandé

- **IntelliJ IDEA Ultimate** (plugins Kotlin, Minecraft)
- **VS Code** (pour web/launcher)

### Extensions Utiles

**IntelliJ:**
- Kotlin
- Minecraft Development
- Database Navigator
- Rainbow Brackets

**VS Code:**
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- Rust Analyzer (pour Tauri)

### Outils CLI

```bash
# Java/Kotlin
sdk install java 21.0.1-tem
sdk install gradle 8.5

# Node.js
nvm install 20
nvm use 20

# Rust (pour Tauri)
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Docker
# (suivre docs officielles)
```

---

## 📚 RESSOURCES

### Documentation Officielle

- **Paper:** https://docs.papermc.io/
- **Velocity:** https://docs.papermc.io/velocity
- **Kotlin:** https://kotlinlang.org/docs/
- **Exposed:** https://github.com/JetBrains/Exposed/wiki
- **Next.js:** https://nextjs.org/docs
- **Tauri:** https://tauri.app/v2/
- **PostgreSQL:** https://www.postgresql.org/docs/
- **Redis:** https://redis.io/docs/

### Communautés

- **Paper Discord:** https://discord.gg/papermc
- **Spigot Forums:** https://www.spigotmc.org/
- **Minecraft Dev Discord:** https://discord.gg/mDgzrMT

---

## 🎯 CONCLUSION

Ce plan de développement fournit une roadmap claire et structurée pour le projet HEGEMONIA. En suivant cette méthodologie et ces bonnes pratiques, nous assurerons :

- ✅ Code de qualité professionnelle
- ✅ Performance optimale
- ✅ Sécurité robuste
- ✅ Maintenabilité long-terme
- ✅ Succès du projet

**Prochaine étape:** Commencer Phase 1 - Infrastructure Serveur

---

*Document vivant - À mettre à jour au fil du projet*

*Dernière mise à jour: 2026-01-07*
