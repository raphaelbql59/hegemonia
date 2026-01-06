# Roadmap de Développement - Hegemonia

## 🎯 Philosophie de Développement

**MVP d'abord** : Lancer vite avec features essentielles, itérer selon feedback
**Développement itératif** : 1 feature à la fois, testée avant de passer à la suivante
**Budget** : 100€ + VPS → Pas de dépenses inutiles, focus open-source

---

## Phase 0 : Fondations (Semaine 1-2) ⚡ ACTUEL

### Objectif
Environnement de développement opérationnel + Launcher basique fonctionnel

### Tâches

**Infrastructure (2-3 jours)**
- [x] Architecture définie
- [ ] Setup VPS OVH (Docker + services de base)
- [ ] PostgreSQL + Redis configurés
- [ ] Serveur Fabric 1.20.1 vanilla qui tourne
- [ ] Génération map Earth (Terra ou pre-gen)

**Launcher v0.1 (3-4 jours)**
- [ ] Electron boilerplate (React + TypeScript)
- [ ] UI basique : Login + Bouton Play
- [ ] Auto-download mods/Fabric
- [ ] Lancement Minecraft avec profil custom
- [ ] Logs console affichés

**Backend API v0.1 (2-3 jours)**
- [ ] Express + Prisma setup
- [ ] Endpoint `/api/launcher/version`
- [ ] Endpoint `/api/launcher/mods` (liste fichiers)
- [ ] Auth basique (JWT simple)

**Livrables Phase 0**
✅ Serveur vanilla accessible
✅ Launcher lance le jeu avec mods
✅ Map Earth chargée

---

## Phase 1 : Core Gameplay (Semaine 3-5)

### Objectif
Système de nations + économie basique = Joueurs peuvent créer pays et commercer

### Mod `hegemonia-core` v0.1
- [ ] Commande `/nation create <nom>` (coût 0 HGN pour test)
- [ ] Commande `/nation invite <joueur>`
- [ ] Commande `/nation kick <joueur>`
- [ ] Stockage PostgreSQL (nations + membres)
- [ ] GUI in-game pour voir infos nation
- [ ] Attribution rôles (Leader, Membre)

### Mod `hegemonia-economy` v0.1
- [ ] Monnaie HGN (custom item + base données)
- [ ] Commande `/balance` et `/pay <joueur> <montant>`
- [ ] Salaire quotidien automatique (100 HGN/jour joué)
- [ ] Shop Admin basique (vendre ressources vanilla)
- [ ] Interface GUI shop (chest menu)

### Territoires v0.1
- [ ] Division map en régions (JSON config)
- [ ] Commande `/territory claim <région>` (si adjacent)
- [ ] Carte des territoires dans launcher (statique pour l'instant)

**Livrables Phase 1**
✅ Joueurs créent nations
✅ Économie fonctionne (monnaie + shop)
✅ Territoires claimables

---

## Phase 2 : Gouvernements & Diplomatie (Semaine 6-7)

### Objectif
Systèmes politiques + relations entre nations

### Gouvernements
- [ ] Choix type gouvernement à création nation
- [ ] Effets gameplay :
  - Monarchie : décisions instantanées
  - Démocratie : votes pour actions majeures
  - Dictature : +20% prod militaire
- [ ] Élections (si démocratie) : /vote tous les 30 jours

### Diplomatie
- [ ] Commande `/diplomacy relations <nation>` (voir relation -100 à +100)
- [ ] Commande `/diplomacy treaty <nation> <type>` (Alliance, Non-Agression)
- [ ] Events : déclaration traité notifie tous les joueurs
- [ ] Rupture traité = casus belli

**Livrables Phase 2**
✅ Gouvernements impactent gameplay
✅ Nations signent traités
✅ Relations diplomatiques trackées

---

## Phase 3 : Guerre Basique (Semaine 8-10)

### Objectif
Premier système de guerre fonctionnel (Guerre de Conquête uniquement)

### Mod `hegemonia-warfare` v0.1
- [ ] Commande `/war declare <nation> <casus_belli>`
- [ ] Validation casus belli (doit avoir raison valide)
- [ ] État de guerre activé (PvP forcé dans territoires contestés)
- [ ] Système de points :
  - Kill ennemi = +10 pts
  - Capturer chunk = +50 pts
  - Défendre chunk = +30 pts
- [ ] Victoire si 60% territoire capturé OU 1000 points
- [ ] Traité de paix : `/war peace <nation>` (négociation)

### Combat Vanilla
- [ ] Armes vanilla pour commencer
- [ ] Armes custom (Phase 4)

**Livrables Phase 3**
✅ Guerres déclarables avec raisons valides
✅ PvP fonctionne dans zones guerre
✅ Victoire/défaite déterminée

---

## Phase 4 : Contenu Militaire (Semaine 11-13)

### Objectif
Armes et véhicules custom

### Armes Custom
- [ ] Fusils (craft + munitions)
- [ ] Grenades
- [ ] Lance-roquettes (end-game)
- [ ] Système de dégâts custom (headshot, armor pen)

### Véhicules (Simple)
- [ ] Tanks (entity custom avec steering)
- [ ] Hélicos (si faisable, sinon Phase 5)
- [ ] Spawner avec craft coûteux

### Types de Guerre Avancés
- [ ] Guerre Économique
- [ ] Guerre Punitive
- [ ] Guerre d'Indépendance

**Livrables Phase 4**
✅ Armes custom fonctionnelles
✅ Tanks contrôlables
✅ Tous types de guerre implémentés

---

## Phase 5 : Technologies (Semaine 14-16)

### Objectif
Arbre tech + progression par ères

### Mod `hegemonia-tech` v1.0
- [ ] Définir arbre tech (JSON config)
- [ ] 4 Ères : Médiéval, Industriel, Moderne, Futur
- [ ] Interface GUI recherche
- [ ] Recherche nationale (budget) vs individuelle (XP)
- [ ] Unlock crafts selon tech

### Contenu par Ère
- [ ] Médiéval : Épées, arcs, châteaux
- [ ] Industriel : Fusils, rails, usines
- [ ] Moderne : Auto, avions (items), gratte-ciels
- [ ] Futur : Plasma, drones, sci-fi

**Livrables Phase 5**
✅ Système tech fonctionnel
✅ Progression par ères
✅ 20+ technologies disponibles

---

## Phase 6 : Launcher Avancé (Semaine 17-18)

### Objectif
Launcher avec features premium

### Features
- [ ] News feed (depuis API)
- [ ] Carte interactive live (territoires nations)
- [ ] Stats classements (richesse, militaire, territoire)
- [ ] Discord RPC (afficher statut)
- [ ] Thème customisable
- [ ] Multi-langues (FR/EN)

### Backend
- [ ] WebSocket pour events temps-réel
- [ ] Endpoints stats avancées
- [ ] Système de news (admin dashboard)

**Livrables Phase 6**
✅ Launcher version 1.0 complet
✅ Expérience premium

---

## Phase 7 : Polish & Optimisation (Semaine 19-20)

### Objectif
Stabilité + préparation lancement public

### Optimisation
- [ ] Profiling performance serveur
- [ ] Optimisation requêtes DB (indexes, caching)
- [ ] Réduction lag (chunk loading, entities)
- [ ] Load testing (simul 50+ joueurs)

### QoL (Quality of Life)
- [ ] Tutoriel in-game complet
- [ ] Commandes help améliorées
- [ ] GUI polie et intuitive
- [ ] Traductions complètes

### Sécurité
- [ ] Anti-cheat renforcé
- [ ] Protection DDOS (Cloudflare)
- [ ] Backup automatique (quotidien)

**Livrables Phase 7**
✅ Serveur stable 50+ joueurs
✅ Aucun bug critique
✅ Prêt pour lancement

---

## Phase 8 : Alpha Publique (Semaine 21+)

### Objectif
Ouverture alpha fermée → beta ouverte → release

### Alpha (50 joueurs max)
- [ ] Invitations Discord
- [ ] Feedback actif
- [ ] Ajustements équilibrage

### Beta (200 joueurs max)
- [ ] Ouverte au public
- [ ] Marketing (YouTube, forums MC)
- [ ] Events de lancement

### Release
- [ ] Serveur stable
- [ ] Campagne marketing
- [ ] Objectif : 500+ joueurs

---

## 💰 Budget Utilisation (100€)

### Répartition
- **Assets graphiques** : 30€ (Logo, UI launcher, textures custom)
- **Marketing** : 40€ (Pubs Discord, YouTube creators)
- **Tools** : 20€ (Domaine, SSL, CDN si besoin)
- **Réserve** : 10€ (imprévus)

### Économies
- Tout en open-source (0€ licences)
- Dev vous-même (0€ dev freelance)
- VPS déjà payé (0€ hosting)

---

## 📊 Indicateurs de Succès

**Phase 1** : 5 joueurs testent → nations créées
**Phase 3** : Première guerre PvP réussie
**Phase 5** : Tech tree complète jouable
**Phase 7** : Serveur 50 joueurs sans lag
**Phase 8** : 100+ joueurs actifs après 1 mois

---

## ⚡ Prochaine Action

**On commence par Phase 0 ?**
- Setup VPS + Serveur Fabric
- Launcher basique
- Map Earth

Ou vous voulez ajuster le plan d'abord ?
