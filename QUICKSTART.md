# 🚀 HEGEMONIA - Guide de Démarrage Rapide

## Étape 1 : Installer les outils (UNE SEULE FOIS)

```bash
cd /home/hegemonia/hegemonia-project
sudo bash scripts/install-tools.sh
```

Puis **déconnectez-vous et reconnectez-vous** pour que les permissions Docker prennent effet.

---

## Étape 2 : Démarrer le serveur de test

```bash
cd /home/hegemonia/hegemonia-project
bash scripts/quick-start.sh
```

Ce script va :
- ✅ Télécharger Velocity et Paper
- ✅ Builder tous les plugins (Core, Nations, War)
- ✅ Lancer PostgreSQL + Redis via Docker
- ✅ Tout configurer automatiquement

---

## Étape 3 : Lancer les serveurs

### Terminal 1 - Velocity (Proxy)
```bash
cd /home/hegemonia/hegemonia-project/velocity
java -Xms512M -Xmx1G -jar velocity.jar
```

### Terminal 2 - Earth (Serveur principal)
```bash
cd /home/hegemonia/hegemonia-project/test-servers/earth
java -Xms4G -Xmx8G -jar paper.jar --nogui
```

Ou avec `screen` pour les garder en arrière-plan :
```bash
# Démarrer Velocity
screen -S velocity
cd /home/hegemonia/hegemonia-project/velocity
java -Xms512M -Xmx1G -jar velocity.jar
# Ctrl+A puis D pour détacher

# Démarrer Earth
screen -S earth
cd /home/hegemonia/hegemonia-project/test-servers/earth
java -Xms4G -Xmx8G -jar paper.jar --nogui
# Ctrl+A puis D pour détacher

# Revenir à un screen
screen -r velocity
screen -r earth

# Lister les screens
screen -ls
```

---

## Étape 4 : Configurer le pare-feu

Ouvrir les ports nécessaires :

```bash
# Port Velocity (connexion joueurs)
sudo ufw allow 25577/tcp

# Ports Docker (si besoin)
sudo ufw allow 5432/tcp  # PostgreSQL
sudo ufw allow 6379/tcp  # Redis

# Activer le pare-feu
sudo ufw enable
```

---

## Étape 5 : Se connecter

### Depuis ton PC local
1. Ouvre Minecraft 1.20.4
2. Multijoueur → Ajouter un serveur
3. Adresse : **ADRESSE_IP_VPS:25577**
4. Connecte-toi !

### Trouver l'IP du VPS
```bash
curl ifconfig.me
```

---

## 🔧 Commandes utiles

### Rebuild les plugins après modification
```bash
cd /home/hegemonia/hegemonia-project/server/plugins
gradle clean shadowJar --no-daemon
cp hegemonia-*/build/libs/*.jar ../../../test-servers/earth/plugins/
```

### Redémarrer un serveur
```bash
# Dans le terminal du serveur
stop

# Ou depuis screen
screen -S earth -X stuff "stop\n"
```

### Logs Docker
```bash
docker compose logs -f postgres
docker compose logs -f redis
```

### Status des services
```bash
docker compose ps
screen -ls
```

---

## 🐛 Dépannage

### "java: command not found"
→ Exécute `sudo bash scripts/install-tools.sh`

### "Permission denied" pour Docker
→ Déconnecte-toi et reconnecte-toi : `exit` puis reconnexion SSH

### Le serveur ne démarre pas
→ Vérifie les logs dans `test-servers/earth/logs/latest.log`

### Impossible de se connecter
→ Vérifie que le port 25577 est ouvert : `sudo ufw status`

---

## 📝 Notes

- **Velocity** : Proxy principal (port 25577)
- **Earth** : Serveur de jeu principal
- **PostgreSQL** : Base de données (port 5432)
- **Redis** : Cache (port 6379)

Le serveur est en mode **développement** - tout est local sur le VPS pour tester rapidement.
