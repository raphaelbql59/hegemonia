# 📦 HEGEMONIA - Guide d'Installation

**Version:** 1.0
**Date:** 2026-01-07
**Pour:** Debian 11 (64GB RAM)

---

## 📋 Table des Matières

1. [Prérequis](#prérequis)
2. [Installation Rapide](#installation-rapide)
3. [Installation Détaillée](#installation-détaillée)
4. [Configuration](#configuration)
5. [Vérification](#vérification)
6. [Maintenance](#maintenance)
7. [Dépannage](#dépannage)

---

## 🎯 Prérequis

### Matériel

- **VPS/Serveur Dédié**
  - CPU : 8+ cores (Intel/AMD récent)
  - RAM : 64 GB minimum
  - Storage : 500 GB+ SSD NVMe
  - Network : 1 Gbps minimum

### Système

- **OS :** Debian 11 (Bullseye) 64-bit
- **Accès :** SSH avec privilèges sudo/root
- **Domaine :** (Optionnel) Un domaine pointant vers votre serveur

### Compétences

- Connaissances de base en Linux/SSH
- Compréhension de Docker (utile mais pas obligatoire)

---

## 🚀 Installation Rapide

**Pour les utilisateurs expérimentés :** Installation automatisée en ~20 minutes.

```bash
# 1. Connexion au serveur
ssh root@VOTRE_IP

# 2. Cloner le repository
git clone https://github.com/votre-org/hegemonia-project.git /home/hegemonia/hegemonia-project
cd /home/hegemonia/hegemonia-project

# 3. Sécuriser le VPS (IMPORTANT)
chmod +x scripts/secure-vps.sh
./scripts/secure-vps.sh

# 4. Se reconnecter avec le nouveau port SSH
ssh -p 2222 minecraft@VOTRE_IP

# 5. Installer HEGEMONIA
cd /home/hegemonia/hegemonia-project
chmod +x scripts/install-hegemonia.sh
./scripts/install-hegemonia.sh

# 6. Éditer la configuration
nano .env
# Configurer les valeurs nécessaires

# 7. Redémarrer les services
docker-compose restart

# 8. Vérifier
docker-compose ps
```

**C'est tout !** Votre serveur est maintenant opérationnel sur `VOTRE_IP:25565`.

---

## 🔧 Installation Détaillée

### Étape 1 : Préparation du Serveur

#### 1.1 Connexion Initiale

```bash
# Connexion SSH en tant que root
ssh root@VOTRE_IP

# Mise à jour initiale
apt update && apt upgrade -y
```

#### 1.2 Clonage du Projet

```bash
# Créer le dossier et cloner
git clone https://github.com/votre-org/hegemonia-project.git /home/hegemonia/hegemonia-project

# Se positionner dans le projet
cd /home/hegemonia/hegemonia-project
```

### Étape 2 : Sécurisation du VPS

**⚠️ CRITIQUE** - Cette étape sécurise votre serveur contre les attaques.

```bash
# Rendre le script exécutable
chmod +x scripts/secure-vps.sh

# Exécuter le script
./scripts/secure-vps.sh
```

Le script va :

1. ✅ Mettre à jour le système
2. ✅ Créer l'utilisateur `minecraft`
3. ✅ Configurer SSH sécurisé (port personnalisé, clé uniquement)
4. ✅ Installer et configurer Fail2Ban
5. ✅ Configurer le firewall UFW
6. ✅ Optimiser le swap et le kernel
7. ✅ Activer les mises à jour de sécurité automatiques

**⚠️ IMPORTANT** : Notez le nouveau port SSH (défaut : 2222) avant de vous déconnecter !

#### 2.1 Ajouter Votre Clé SSH

Si vous n'avez pas encore de clé SSH :

```bash
# Sur votre machine locale
ssh-keygen -t ed25519 -C "votre@email.com"

# Copier la clé publique
cat ~/.ssh/id_ed25519.pub
```

Le script vous demandera de coller cette clé pendant l'installation.

#### 2.2 Test de Connexion SSH

```bash
# Depuis votre machine locale
ssh -p 2222 minecraft@VOTRE_IP

# Devrait fonctionner sans mot de passe
```

Si la connexion échoue, **NE FERMEZ PAS** votre session root actuelle ! Vérifiez la configuration.

### Étape 3 : Installation des Composants

Maintenant connecté en tant qu'utilisateur `minecraft` :

```bash
cd /home/hegemonia/hegemonia-project

# Rendre le script exécutable
chmod +x scripts/install-hegemonia.sh

# Lancer l'installation
./scripts/install-hegemonia.sh
```

Le script va installer :

1. ✅ Dépendances système (curl, git, etc.)
2. ✅ Docker et Docker Compose
3. ✅ Générer les secrets de sécurité
4. ✅ Créer les répertoires nécessaires
5. ✅ Copier les configurations
6. ✅ Démarrer tous les services

**Durée estimée :** 10-15 minutes

### Étape 4 : Configuration

#### 4.1 Fichier `.env`

Le fichier `.env` a été créé automatiquement avec des secrets sécurisés. Vous devez maintenant le personnaliser :

```bash
nano .env
```

**Configuration minimale :**

```bash
# Base de données (déjà configuré avec un mot de passe fort)
POSTGRES_PASSWORD=xxx  # Ne pas changer

# Velocity secret (déjà configuré)
VELOCITY_SECRET=xxx    # Ne pas changer

# Web/API (configurer)
NEXTAUTH_URL=https://votre-domaine.com  # Votre domaine
NEXTAUTH_SECRET=xxx                      # Déjà généré

# Discord (optionnel pour le moment)
DISCORD_BOT_TOKEN=
DISCORD_WEBHOOK_WARS=
# ... etc
```

Enregistrer et quitter (`Ctrl+X`, `Y`, `Enter`).

#### 4.2 Redémarrer les Services

```bash
docker-compose restart
```

### Étape 5 : Vérification

#### 5.1 Vérifier les Services

```bash
docker-compose ps
```

Tous les services doivent être `Up` :

```
NAME                   STATUS
hegemonia-postgres     Up
hegemonia-redis        Up
hegemonia-velocity     Up
hegemonia-lobby        Up
hegemonia-earth        Up
hegemonia-wars         Up
hegemonia-resources    Up
hegemonia-events       Up
hegemonia-nginx        Up
hegemonia-netdata      Up
```

#### 5.2 Vérifier les Logs

```bash
# Velocity (proxy principal)
docker-compose logs -f velocity

# Earth (serveur principal)
docker-compose logs -f earth
```

Cherchez `Done!` dans les logs = serveur démarré.

#### 5.3 Test de Connexion Minecraft

1. Ouvrir Minecraft (version 1.20.4)
2. Multijoueur → Ajouter un serveur
3. Adresse : `VOTRE_IP:25565`
4. Se connecter

Vous devriez arriver dans le **Lobby** !

---

## ⚙️ Configuration Avancée

### SSL/HTTPS avec Let's Encrypt

Une fois votre domaine configuré :

```bash
# Installer Certbot
sudo apt install -y certbot python3-certbot-nginx

# Obtenir un certificat
sudo certbot --nginx -d votre-domaine.com -d www.votre-domaine.com

# Renouvellement automatique (déjà configuré par Certbot)
```

### Backups Automatiques

Configurer un cron pour les backups quotidiens :

```bash
# Éditer le crontab
crontab -e

# Ajouter (backup à 3h du matin)
0 3 * * * /home/hegemonia/hegemonia-project/scripts/backup.sh >> /home/hegemonia/hegemonia-project/backups/backup.log 2>&1
```

### Monitoring Netdata

Accédez à Netdata via :

```
http://VOTRE_IP:19999
```

**⚠️ Sécurité** : Pour la production, sécurisez Netdata avec un mot de passe ou limitez l'accès IP.

---

## 🛠️ Maintenance

### Commandes Utiles

```bash
# Voir tous les services
docker-compose ps

# Voir les logs d'un service
docker-compose logs -f [service]

# Redémarrer un service
docker-compose restart [service]

# Arrêter tous les services
docker-compose down

# Démarrer tous les services
docker-compose up -d

# Voir l'utilisation ressources
docker stats

# Entrer dans un conteneur
docker-compose exec [service] bash
```

### Mise à Jour

```bash
cd /home/hegemonia/hegemonia-project

# Pull dernières modifications
git pull

# Reconstruire si nécessaire
docker-compose build

# Redémarrer
docker-compose up -d
```

### Backup Manuel

```bash
./scripts/backup.sh
```

Les backups sont stockés dans `/home/hegemonia/hegemonia-project/backups/`

### Restauration depuis Backup

```bash
./scripts/restore.sh backups/hegemonia-backup-YYYYMMDD_HHMMSS.tar.gz
```

---

## 🔍 Dépannage

### Problème : Service ne démarre pas

```bash
# Voir les logs détaillés
docker-compose logs [service]

# Vérifier la configuration
docker-compose config

# Redémarrer proprement
docker-compose down
docker-compose up -d
```

### Problème : Impossible de se connecter au serveur Minecraft

**Vérifications :**

1. Velocity est démarré :
   ```bash
   docker-compose logs velocity
   ```

2. Port 25565 ouvert dans le firewall :
   ```bash
   sudo ufw status
   ```

3. Velocity secret correspond dans tous les fichiers :
   - `.env`
   - `server/velocity/velocity.toml`
   - `server/configs/paper-global.yml`

### Problème : Base de données inaccessible

```bash
# Vérifier PostgreSQL
docker-compose exec postgres psql -U hegemonia -d hegemonia_main

# Si connexion impossible, vérifier les logs
docker-compose logs postgres
```

### Problème : RAM insuffisante

Si vous avez moins de 64GB de RAM, ajustez les allocations dans `docker-compose.yml` :

```yaml
earth:
  environment:
    MEMORY: "16G"  # Au lieu de 24G
```

### Problème : Logs remplissent le disque

```bash
# Nettoyer les logs Docker
docker system prune -a --volumes

# Configurer la rotation des logs
sudo nano /etc/docker/daemon.json
```

Ajouter :
```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

Redémarrer Docker :
```bash
sudo systemctl restart docker
```

---

## 📊 Monitoring et Performances

### Vérifier les TPS (Ticks Per Second)

```bash
# Depuis la console du serveur
docker-compose exec earth rcon-cli

# Dans RCON
> tps
```

TPS optimal : **≥ 19.5**

### Profiling avec Spark

```bash
# Installer Spark plugin
# Télécharger depuis https://spark.lucko.me/download
# Placer dans server/paper/earth/plugins/

# Profiler CPU
/spark profiler start

# Après 30 secondes
/spark profiler stop
```

### Vérifier la RAM

```bash
docker stats
```

---

## 🔒 Sécurité

### Checklist Sécurité

- [ ] SSH sur port personnalisé (non-standard)
- [ ] Authentification par clé uniquement (pas de mot de passe)
- [ ] Root login désactivé
- [ ] Fail2Ban actif
- [ ] UFW firewall configuré
- [ ] Mises à jour automatiques activées
- [ ] Backups automatiques configurés
- [ ] Secrets forts dans `.env`
- [ ] PostgreSQL non exposé publiquement
- [ ] Redis non exposé publiquement

### Rotation des Secrets

```bash
# Régénérer les secrets
cd /home/hegemonia/hegemonia-project

# Velocity secret
VELOCITY_SECRET=$(openssl rand -base64 32)
sed -i "s/VELOCITY_SECRET=.*/VELOCITY_SECRET=$VELOCITY_SECRET/" .env

# Mettre à jour aussi dans les fichiers de config
# Puis redémarrer
docker-compose restart
```

---

## 📞 Support

**En cas de problème :**

1. Consultez les logs : `docker-compose logs -f`
2. Vérifiez [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
3. Rejoignez notre Discord : (à venir)
4. Ouvrez une issue sur GitHub

---

## ✅ Post-Installation

Une fois l'installation terminée :

1. **Configurez LuckPerms** pour la gestion des permissions
2. **Téléchargez les plugins premium** (Grim AntiCheat, etc.)
3. **Configurez BlueMap** pour la carte web
4. **Générez la carte Earth** (Phase 3)
5. **Installez les plugins custom** (Phase 4-6)

Consultez le [PROGRESS.md](../PROGRESS.md) pour voir les prochaines étapes du développement.

---

**Installation complétée avec succès ?** Passez à la Phase 2 : Launcher Custom ! 🚀

*Dernière mise à jour : 2026-01-07*
