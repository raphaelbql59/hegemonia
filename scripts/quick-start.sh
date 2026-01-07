#!/bin/bash

#################################################################
# HEGEMONIA - Démarrage rapide du serveur de test
# Pour tester les plugins en développement
#################################################################

set -e

PROJECT_DIR="/home/hegemonia/hegemonia-project"
VELOCITY_VERSION="3.3.0-SNAPSHOT-408"
PAPER_VERSION="1.20.4"
PAPER_BUILD="497"

echo "========================================="
echo "  HEGEMONIA - Démarrage rapide"
echo "========================================="
echo ""

cd "$PROJECT_DIR"

# Créer les dossiers nécessaires
echo "📁 Création des dossiers..."
mkdir -p velocity
mkdir -p test-servers/earth/{plugins,config}
mkdir -p test-servers/lobby/{plugins,config}

# Télécharger Velocity si nécessaire
if [ ! -f "velocity/velocity.jar" ]; then
    echo "📥 Téléchargement de Velocity $VELOCITY_VERSION..."
    curl -L -o velocity/velocity.jar \
        "https://api.papermc.io/v2/projects/velocity/versions/$VELOCITY_VERSION/builds/$(curl -s https://api.papermc.io/v2/projects/velocity/versions/$VELOCITY_VERSION | jq -r '.builds[-1]')/downloads/velocity-$VELOCITY_VERSION-$(curl -s https://api.papermc.io/v2/projects/velocity/versions/$VELOCITY_VERSION | jq -r '.builds[-1]').jar"
fi

# Télécharger Paper si nécessaire
if [ ! -f "test-servers/earth/paper.jar" ]; then
    echo "📥 Téléchargement de Paper $PAPER_VERSION..."
    curl -L -o test-servers/earth/paper.jar \
        "https://api.papermc.io/v2/projects/paper/versions/$PAPER_VERSION/builds/$PAPER_BUILD/downloads/paper-$PAPER_VERSION-$PAPER_BUILD.jar"
    cp test-servers/earth/paper.jar test-servers/lobby/paper.jar
fi

# Builder les plugins
echo "🔨 Build des plugins..."
cd server/plugins
gradle clean shadowJar --no-daemon || {
    echo "❌ Erreur lors du build des plugins"
    echo "💡 Assurez-vous d'avoir exécuté install-tools.sh avec sudo"
    exit 1
}

# Copier les plugins buildés
echo "📦 Copie des plugins..."
cp hegemonia-core/build/libs/HegemoniaCore*.jar "$PROJECT_DIR/test-servers/earth/plugins/"
cp hegemonia-nations/build/libs/HegemoniaNations*.jar "$PROJECT_DIR/test-servers/earth/plugins/"
cp hegemonia-war/build/libs/HegemoniaWar*.jar "$PROJECT_DIR/test-servers/earth/plugins/"

cd "$PROJECT_DIR"

# Copier les configs
echo "⚙️  Copie des configurations..."
cp server/velocity/velocity.toml velocity/ 2>/dev/null || true
cp server/paper/earth/server.properties test-servers/earth/ 2>/dev/null || true
cp server/paper/earth/config/*.yml test-servers/earth/config/ 2>/dev/null || true

# Lancer PostgreSQL et Redis avec Docker
echo "🐳 Démarrage de PostgreSQL et Redis..."
docker compose up -d postgres redis || {
    echo "⚠️  Impossible de démarrer Docker. Continuons sans..."
}

# Attendre que les services soient prêts
echo "⏳ Attente des services (5s)..."
sleep 5

echo ""
echo "========================================="
echo "  ✅ Préparation terminée!"
echo "========================================="
echo ""
echo "Pour démarrer les serveurs:"
echo ""
echo "  Terminal 1 - Velocity (proxy):"
echo "    cd velocity"
echo "    java -Xms512M -Xmx1G -jar velocity.jar"
echo ""
echo "  Terminal 2 - Earth (serveur principal):"
echo "    cd test-servers/earth"
echo "    java -Xms4G -Xmx8G -jar paper.jar --nogui"
echo ""
echo "Connexion: localhost:25577 (Velocity)"
echo ""
echo "💡 Utilisez 'screen' pour garder les serveurs en arrière-plan:"
echo "    screen -S velocity"
echo "    screen -S earth"
echo ""
