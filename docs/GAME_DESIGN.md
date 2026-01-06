# Hegemonia - Game Design Document

## 🎯 Vision du Projet

Serveur Minecraft Earth RP où les joueurs créent et gèrent des nations réelles avec un système de guerre, diplomatie, économie et technologie ultra-réaliste. Plus profond et immersif que NationsGlory.

## 🌍 Concept de Base

### Map & Territoires
- **Carte Earth 1:500** - Monde réel avec frontières authentiques
- **Régions territoriales** : Découpage par provinces/états réels
- **Ressources géographiques** : Pétrole au Moyen-Orient, diamants en Afrique, etc.

## 🏛️ Système de Nations

### Création & Attribution
- **Au lancement** : Pays distribués aux premiers joueurs
  - Pays moyens : Gratuits (France, Allemagne, Italie, etc.)
  - Super-puissances : Réservées/vendues (USA, Russie, Chine)
- **Après lancement** : Pays revendables entre joueurs
- **Condition** : Minimum 3 joueurs actifs pour maintenir un pays

### Gouvernements (Impact gameplay)

**Monarchie**
- Leader = pouvoir absolu
- Succession : héritier désigné
- Avantage : Décisions rapides, stabilité
- Inconvénient : Risque de tyrannie, révolution possible

**Démocratie**
- Leader élu tous les 30 jours réels
- Décisions majeures = vote
- Avantage : Population + loyale, moral élevé
- Inconvénient : Décisions lentes, campagnes électorales coûteuses

**Dictature**
- Leader = contrôle militaire
- Pas d'élections, succession par coup d'état
- Avantage : +20% production militaire
- Inconvénient : Population mécontente, sanctions internationales

**République**
- Mix démocratie + efficacité
- Leader élu, conseillers nommés
- Équilibré

### Rôles dans une Nation
- **Chef d'État** : Déclare guerre, signe traités
- **Ministre Économie** : Gère budget, taxes, commerce
- **Ministre Défense** : Commande armée
- **Ministre Intérieur** : Gère population, lois internes
- **Citoyens** : Travaillent, votent, combattent

## 💰 Système Économique

### Monnaie
- **Hegemon (HGN)** : Monnaie unique mondiale
- Salaire de base : 100 HGN/jour joué
- Prix référence : Stack cobblestone = 5 HGN

### Commerce

**1. Marché Local (Capitale - Lobby)**
- Joueurs vendent petites quantités au Shop Admin
- Prix faibles mais accessibles
- Pas de taxes
- Ex : 1 diamant = 50 HGN

**2. Siège de Commerce International (Entreprises)**
- Pour vendre gros volumes
- Prix libres (offre/demande)
- Taxe dégressive : 10% → 5% → 2% selon volume
- Nécessite licence commerciale (5000 HGN)

**3. Commerce Inter-Nations**
- Entre gouvernements uniquement
- Contrats officiels
- Taxes imposées par chaque pays
- Traités commerciaux réduisent taxes

### Budget National
- **Revenus** : Taxes citoyens, commerce, ressources exportées
- **Dépenses** : Armée, infrastructures, recherche, salaires fonctionnaires

## ⚔️ Système de Guerre

### Types de Guerre

**Guerre de Conquête**
- Objectif : Capturer 60% territoire ennemi
- Durée : Illimitée jusqu'à victoire/capitulation
- Victoire : Annexion territoires + 30% trésor ennemi
- Coût : 50,000 HGN déclaration + 5000/jour

**Guerre Économique**
- Objectif : Réduire trésor ennemi à 0
- Blocus commercial actif
- Victoire : Sanctions + contrôle commerce (5 ans in-game)
- Pas de conquête territoriale

**Guerre d'Indépendance**
- Région qui veut se séparer
- Doit tenir 7 jours contre métropole
- 60% population régionale doit voter OUI

**Guerre Punitive**
- Durée max : 14 jours
- Objectif : Réparations ou destitution
- Pas de conquête
- Nécessite validation ONU (vote nations)

**Guerre Totale**
- Rare, conditions extrêmes
- Tout est permis, griefing autorisé dans zone guerre
- Victoire = capitulation complète

### Casus Belli (Raisons valides)
- Insulte diplomatique (-50 relations)
- Violation traité
- Soutien terroriste/rebelles
- Revendication historique
- Alliance défensive
- Génocide/crimes de guerre

### Mécanique de Combat
- **Armes custom** : Fusils, grenades, lance-roquettes
- **Véhicules** : Tanks, hélicos (mods custom)
- **Siège** : Bloquer accès ville, couper ressources
- **Moral** : Population démoralisée = malus production

## 🔬 Système Technologique

### Arbres Tech (Par Ère)

**Ère Médiévale** (Début)
- Armes : Épées, arcs
- Constructions : Châteaux, murailles
- Économie : Marchés basiques

**Ère Industrielle**
- Armes : Fusils, canons
- Constructions : Usines, rails
- Économie : Banques, bourses

**Ère Moderne**
- Armes : Automatiques, explosifs
- Constructions : Buildings, aéroports
- Économie : Trading international

**Ère Futuriste** (End-game)
- Armes : Plasma, drones
- Constructions : Gratte-ciels high-tech
- Économie : Crypto, spatial

### Recherche
- **Par nation** : Technos militaires, infrastructures (budget national)
- **Individuelle** : Crafts, métiers, skills (XP joueur)
- **Coût** : 10,000-100,000 HGN selon tech
- **Temps** : 2-7 jours réels

## 🎮 Progression Joueur

### Début (Jour 1-7)
- Kit de départ : Outils fer, 1000 HGN
- Tutoriel guidé
- Rejoindre un pays OU fonder alliance
- Quêtes débutant = 5000 HGN totales

### Moyen terme (Semaine 2-4)
- Choisir métier (Mineur, Commerçant, Soldat, etc.)
- Construire maison/entreprise
- Participer économie nationale
- Voter/participer politique

### Long terme (Mois+)
- Devenir ministre/chef d'État
- Créer entreprise internationale
- Mener guerres
- Débloquer techs avancées

## 🎨 Launcher Custom

### Interface
- **Style** : Dark, moderne, futuriste (inspiré Arc/Cyberpunk)
- **Couleurs** : Bleu électrique + Orange + Noir

### Features
1. **Auto-update** : Mods + launcher
2. **News feed** : Événements serveur en temps réel
3. **Carte interactive** : Territoires nations live
4. **Stats** : Classements nations, richesse, militaire
5. **Discord integration** : Voir qui est co
6. **Logs console** : Debug si crash

## 📊 Métriques de Succès

- 50+ joueurs actifs après 1 mois
- 10+ nations actives
- 2+ guerres majeures
- Économie stable (inflation < 10%/mois)

---

**Note** : Ce GDD est vivant, on ajuste selon retours joueurs et tests.
