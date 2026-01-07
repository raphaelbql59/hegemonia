# Hegemonia Launcher Simple

Launcher minimal pour se connecter rapidement au serveur Hegemonia.

## Installation

### Prérequis
- Python 3.8+
- Minecraft 1.20.4 installé (via le launcher officiel)

### Windows
```bash
python launcher.py
```

### Linux / macOS
```bash
python3 launcher.py
```

## Configuration

Éditez `launcher.py` et modifiez la ligne :
```python
self.server_ip = "VOTRE_IP_VPS"  # Remplacez par l'IP du serveur
```

## Utilisation

1. Lancez le launcher
2. Entrez votre pseudo
3. Cliquez sur "JOUER"
4. Le launcher ouvrira Minecraft et tentera de se connecter automatiquement

## Notes

- Le serveur est en mode offline (pas besoin de compte Minecraft premium)
- Si la connexion automatique échoue, connectez-vous manuellement via Minecraft

## Développement futur

Ce launcher est minimal. Le vrai launcher Tauri avec toutes les fonctionnalités sera développé plus tard.
