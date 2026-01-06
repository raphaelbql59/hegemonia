# 🚀 Quick Start Guide

## Développement Local (Recommandé)

### 1. Démarrer l'environnement de développement

```bash
# Démarrer tous les services (API + PostgreSQL + Redis)
./dev.sh start

# Voir les logs en temps réel
./dev.sh logs
```

### 2. Initialiser la base de données

```bash
# Créer les tables
./dev.sh db-migrate

# (Optionnel) Ouvrir Prisma Studio pour voir la DB
./dev.sh db-studio
```

### 3. Services disponibles

- **API**: http://localhost:3000
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Prisma Studio**: http://localhost:5555 (si lancé)

### 4. Commandes utiles

```bash
./dev.sh logs-api      # Logs de l'API uniquement
./dev.sh shell-api     # Shell dans le container API
./dev.sh shell-db      # Shell PostgreSQL
./dev.sh restart       # Redémarrer les services
./dev.sh stop          # Arrêter les services
./dev.sh status        # Voir l'état des services
```

## Développement du Launcher

```bash
cd launcher
npm install
npm run dev
```

Le launcher s'ouvrira automatiquement avec hot-reload.

## Tester l'API

### Health check
```bash
curl http://localhost:3000/health
```

### Récupérer les nations
```bash
curl http://localhost:3000/api/nations
```

### Récupérer les mods pour le launcher
```bash
curl http://localhost:3000/api/launcher/mods
```

## Workflow de Développement

### 1. Modifier le code de l'API

Les changements dans `api/src/` sont automatiquement rechargés (hot-reload activé).

```bash
# Voir les logs en temps réel
./dev.sh logs-api
```

### 2. Modifier le schéma de base de données

```bash
# Éditer api/prisma/schema.prisma
nano api/prisma/schema.prisma

# Appliquer les changements
./dev.sh db-migrate
```

### 3. Débugger

```bash
# Voir tous les logs
./dev.sh logs

# Accéder au shell de l'API pour des commandes
./dev.sh shell-api

# Voir la base de données
./dev.sh db-studio
```

## Déploiement sur VPS

### Production Quick Deploy

```bash
# Sur votre VPS
ssh root@votre-ip
git clone <repo-url> /opt/hegemonia
cd /opt/hegemonia
chmod +x deploy.sh
sudo ./deploy.sh
```

### Mise à jour du serveur

```bash
cd /opt/hegemonia
git pull
docker-compose build
docker-compose up -d
```

## Problèmes Courants

### L'API ne démarre pas

```bash
# Vérifier les logs
./dev.sh logs-api

# Reconstruire le container
./dev.sh build
./dev.sh start
```

### La base de données est corrompue

```bash
# Reset complet (ATTENTION: supprime les données)
./dev.sh db-reset
```

### Port déjà utilisé

```bash
# Trouver le process
lsof -i :3000  # pour l'API
lsof -i :5432  # pour PostgreSQL

# Ou arrêter les services
./dev.sh stop
```

## Prochaines Étapes

1. ✅ Environnement de dev configuré
2. ⏳ Développer les mods Fabric
3. ⏳ Configurer la carte Earth
4. ⏳ Tester le launcher avec le serveur
5. ⏳ Déployer sur VPS

## Besoin d'Aide ?

- **Logs**: `./dev.sh logs`
- **Status**: `./dev.sh status`
- **Documentation**: Voir `/docs`
- **Issues**: GitHub Issues

Happy coding! 🚀
