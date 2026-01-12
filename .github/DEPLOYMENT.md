# Configuration du Deploiement Automatique

## Secrets GitHub a configurer

Allez dans **Settings > Secrets and variables > Actions** de votre repo GitHub et ajoutez :

| Secret | Description | Valeur |
|--------|-------------|--------|
| `SERVER_HOST` | IP du serveur | `51.75.31.173` |
| `SERVER_USER` | Utilisateur SSH | `hegemonia` |
| `SERVER_SSH_KEY` | Cle SSH privee | (voir ci-dessous) |

## Generer la cle SSH

Sur le serveur, executez :

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
cat ~/.ssh/github_deploy  # Copiez cette cle privee dans SERVER_SSH_KEY
```

## Workflows disponibles

### 1. Client Mod (`client-mod.yml`)
- **Declencheur** : Push sur `client-mod/**`
- **Actions** : Build + deploy sur l'API
- **Resultat** : Mod auto-mis a jour pour les joueurs

### 2. Server Plugins (`server-plugins.yml`)
- **Declencheur** : Push sur `server/plugins/**`
- **Actions** : Build tous les plugins + deploy
- **Resultat** : Plugins deployes (restart manuel requis)

### 3. Launcher (`launcher.yml`)
- **Declencheur** : Push sur `launcher/**`
- **Actions** : Build Windows + Linux
- **Resultat** : Artifacts disponibles en telechargement

## Flux de travail

```
Code push → GitHub Actions build → Deploy auto → Joueurs recup auto la MAJ
```

Les joueurs n'ont rien a faire, le mod se met a jour automatiquement via l'API.
