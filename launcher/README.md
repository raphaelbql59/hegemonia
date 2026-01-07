# 🎮 Hegemonia Launcher - Professionnel Ultra Moderne

Launcher officiel pour le serveur Minecraft géopolitique Hegemonia.

## 🌟 Fonctionnalités

- ✅ **Authentification sécurisée** avec système de comptes custom
- ✅ **Lancement automatique** de Minecraft vers le serveur
- ✅ **Actualités en temps réel** affichées dans le launcher
- ✅ **Statistiques joueur** (nation, playtime, combat, économie)
- ✅ **Status serveur** en direct (joueurs connectés, ping)
- ✅ **Interface ultra moderne** avec animations fluides
- ✅ **Multi-plateforme** (Windows, Linux, macOS)

## 📋 Prérequis

### Systèmes d'exploitation supportés:
- Windows 10/11 (64-bit)
- Ubuntu 20.04+ / Debian 11+
- macOS 11.0+

### Logiciels requis:

1. **Node.js 20+** et **npm**
   - Windows: https://nodejs.org/
   - Linux: `sudo apt install nodejs npm`
   - macOS: `brew install node`

2. **Rust** (pour build Tauri)
   - Installation: https://rustup.rs/
   - Ou: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`

3. **Dépendances système** (Linux uniquement)
   ```bash
   sudo apt install libwebkit2gtk-4.0-dev \
       build-essential \
       curl \
       wget \
       file \
       libssl-dev \
       libgtk-3-dev \
       libayatana-appindicator3-dev \
       librsvg2-dev
   ```

4. **Minecraft Java Edition 1.20.4**
   - Le launcher nécessite que Minecraft soit installé

5. **PostgreSQL** (pour le backend)
   - Doit être configuré avec la base de données Hegemonia

## 🚀 Installation

### 1. Installer les dépendances

```bash
cd launcher
./install-deps.sh  # Installe Rust et toutes les dépendances
```

### 2. Installer les packages npm

#### Frontend (Tauri + React)
```bash
npm install
```

#### Backend API
```bash
cd api
npm install
cd ..
```

### 3. Configuration

#### Backend API (.env)
Créez un fichier `.env` dans `api/`:
```bash
cp api/.env.example api/.env
```

Modifiez les variables si nécessaire:
```env
PORT=3001
DB_HOST=localhost
DB_PORT=5432
DB_NAME=hegemonia
DB_USER=hegemonia
DB_PASSWORD=hegemonia_password
JWT_SECRET=votre_secret_jwt_tres_securise
```

### 4. Base de données

#### Créer les tables
```bash
cd api
npm run migrate
```

#### Créer le compte admin
```bash
npm run create-admin
```

**Credentials par défaut:**
- Email: `admin@hegemonia.fr`
- Password: `Hegemonia2024!`

⚠️ **IMPORTANT**: Changez ce mot de passe en production !

## 🎯 Démarrage

### Mode développement

Dans un terminal, démarrer l'API backend:
```bash
cd api
npm run dev
```

Dans un autre terminal, démarrer le launcher Tauri:
```bash
npm run tauri:dev
```

Le launcher s'ouvrira automatiquement avec hot-reload activé.

### Build production

#### Build backend API
```bash
cd api
npm run build
npm start  # Lance l'API en production
```

#### Build launcher
```bash
npm run tauri:build
```

Les executables seront dans `src-tauri/target/release/bundle/`:
- **Windows**: `HegemoniaLauncher.exe` (`.msi` installer aussi disponible)
- **Linux**: `.deb`, `.AppImage`
- **macOS**: `.dmg`, `.app`

## 📖 Utilisation

### Connexion

1. Lancez le launcher
2. Entrez vos identifiants (email + mot de passe)
3. Cliquez sur "Se connecter"

**Compte de test:**
- Email: `admin@hegemonia.fr`
- Password: `Hegemonia2024!`

### Inscription

L'inscription ne peut se faire **que via le site web** : https://hegemonia.fr/register

### Lancer Minecraft

1. Une fois connecté, cliquez sur le bouton **"JOUER"**
2. Le launcher va:
   - Vérifier que Minecraft 1.20.4 est installé
   - Lancer Minecraft automatiquement
   - Se connecter au serveur Hegemonia (51.75.31.173:25577)

## 🏗️ Architecture

```
launcher/
├── src/                    # Frontend React + TypeScript
│   ├── components/         # Composants UI réutilisables
│   ├── pages/             # Pages (Login, Dashboard)
│   ├── api/               # API clients
│   ├── store/             # State management (Zustand)
│   └── styles/            # CSS global (Tailwind)
├── src-tauri/             # Backend Rust (Tauri)
│   ├── src/
│   │   ├── main.rs        # Entry point
│   │   └── commands.rs    # Commands Tauri
│   ├── Cargo.toml
│   └── tauri.conf.json
├── api/                   # Backend API Express.js
│   ├── src/
│   │   ├── server.ts      # Server Express
│   │   ├── routes/        # Routes API
│   │   ├── db/            # Database & migrations
│   │   └── scripts/       # Scripts utilitaires
│   └── package.json
└── package.json           # Dependencies frontend
```

## 🔧 Scripts disponibles

### Frontend
- `npm run dev` - Dev mode Vite
- `npm run build` - Build frontend
- `npm run tauri:dev` - Dev mode Tauri (avec hot reload)
- `npm run tauri:build` - Build production (executables)

### Backend API
- `npm run dev` - Dev mode avec nodemon
- `npm run build` - Build TypeScript
- `npm start` - Production mode
- `npm run migrate` - Créer les tables PostgreSQL
- `npm run create-admin` - Créer le compte admin

## 🗄️ Base de données

Le launcher utilise la même base de données PostgreSQL que les plugins Minecraft (`hegemonia`).

### Tables créées:

- `launcher_users` - Comptes utilisateurs
- `launcher_news` - Actualités
- `launcher_sessions` - Sessions JWT

### Lien avec Minecraft:

La table `launcher_users` a une foreign key `minecraft_uuid` vers `hegemonia_players(uuid)`, permettant de lier un compte launcher à un personnage Minecraft.

## 🔐 Sécurité

- ✅ Mots de passe hashés avec bcrypt (12 rounds)
- ✅ Authentication JWT avec expiration
- ✅ Rate limiting sur les routes sensibles
- ✅ CORS configuré pour Tauri uniquement
- ✅ Helmet.js pour les headers sécurisés
- ✅ Validation des inputs avec Zod

## 🎨 Technologies utilisées

### Frontend
- **Tauri** - Framework cross-platform
- **React 18** - UI library
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Framer Motion** - Animations
- **Zustand** - State management
- **React Query** - Data fetching
- **React Router** - Routing
- **Sonner** - Toast notifications

### Backend
- **Express.js** - Server HTTP
- **PostgreSQL** - Database
- **JWT** - Authentication
- **Bcrypt** - Password hashing
- **Zod** - Validation
- **minecraft-server-util** - Server status ping

### Rust
- **Tauri** - Desktop app framework
- **Serde** - Serialization
- **Tokio** - Async runtime
- **Reqwest** - HTTP client

## 🐛 Dépannage

### Le launcher ne se lance pas

1. Vérifiez que Rust est installé: `rustc --version`
2. Vérifiez que Node.js est installé: `node --version`
3. Réinstallez les dépendances: `npm install` et `cd api && npm install`

### "Cannot connect to API"

1. Vérifiez que l'API backend est lancée: `cd api && npm run dev`
2. Vérifiez le port 3001: `curl http://localhost:3001/health`
3. Vérifiez les logs de l'API

### "Minecraft not found"

Le launcher cherche Minecraft dans:
- Windows: `%APPDATA%\.minecraft`
- Linux: `~/.minecraft`
- macOS: `~/Library/Application Support/minecraft`

Assurez-vous que Minecraft Java Edition 1.20.4 est installé.

### Erreurs de base de données

1. Vérifiez que PostgreSQL est démarré
2. Vérifiez la connexion: `psql -h localhost -U hegemonia -d hegemonia`
3. Lancez les migrations: `cd api && npm run migrate`

## 📝 TODO / Améliorations futures

- [ ] Auto-updater intégré
- [ ] Gestion des mods/resourcepacks automatique
- [ ] Intégration Discord Rich Presence
- [ ] Système de chat launcher
- [ ] Profils multiples
- [ ] Screenshots gallery
- [ ] Thèmes personnalisables

## 📄 Licence

© 2024 Hegemonia - Tous droits réservés

## 🆘 Support

En cas de problème:
1. Consultez ce README
2. Vérifiez les logs dans `api/` et la console Tauri
3. Contactez l'équipe Hegemonia

---

**Développé avec ❤️ par Claude Code pour Hegemonia**
