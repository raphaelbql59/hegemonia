# HEGEMONIA - Test Servers

## Serveur Earth (Principal)

### Configuration
- **Port**: 25566
- **Version**: Paper 1.20.4
- **Mode**: Offline (pour les tests)

### Plugins installés
- **HegemoniaCore** - Core système
- **HegemoniaEconomy** - Système économique
- **HegemoniaNations** - Gestion des nations, empires et élections
- **HegemoniaWar** - Système de guerre et batailles

### Démarrage

```bash
cd test-servers/earth
./start.sh
```

### Commandes principales

#### Nations
```
/nation create <nom> <tag>     - Créer une nation
/nation info [nation]          - Informations sur une nation
/nation join <nation>          - Rejoindre une nation
/nation leave                  - Quitter sa nation
/nation empire                 - Voir l'empire
/nation vassalize <nation>     - Vassaliser une nation
/nation tribute                - Collecter les tributs
/nation election               - Voir l'élection en cours
/nation vote <candidat>        - Voter pour un candidat
```

#### Économie
```
/balance                       - Voir son solde
/pay <joueur> <montant>        - Transférer de l'argent
/shop                          - Ouvrir le menu du shop
```

#### Guerre
```
/war declare <nation> <raison> - Déclarer la guerre
/war status                    - Voir les guerres en cours
/battle join                   - Rejoindre une bataille
/battle leave                  - Quitter une bataille
```

### Connexion Client

1. Lancer le launcher Hegemonia
2. Se connecter avec un pseudo
3. Le launcher télécharge automatiquement les mods
4. Cliquer sur "Jouer"
5. Se connecter à `localhost:25566`
