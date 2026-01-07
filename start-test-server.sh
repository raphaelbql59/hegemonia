#!/bin/bash

##################################################################
# HEGEMONIA - Script de démarrage rapide du serveur de test
##################################################################

PROJECT_DIR="/home/hegemonia/hegemonia-project"
cd "$PROJECT_DIR"

echo "========================================="
echo "  HEGEMONIA - Démarrage serveur de test"
echo "========================================="
echo ""

# Vérifier que les JARs sont présents
if [ ! -f "test-servers/earth/paper.jar" ]; then
    echo "❌ Paper.jar introuvable. Exécutez d'abord quick-start.sh"
    exit 1
fi

if [ ! -f "test-servers/velocity/velocity.jar" ]; then
    echo "❌ Velocity.jar introuvable. Exécutez d'abord quick-start.sh"
    exit 1
fi

# Lancer PostgreSQL et Redis avec Docker (si disponible)
if command -v docker &> /dev/null; then
    echo "🐳 Démarrage de PostgreSQL et Redis..."
    docker compose up -d postgres redis 2>/dev/null || echo "⚠️  Docker non configuré, continuons sans..."
    sleep 3
else
    echo "⚠️  Docker non installé - Les plugins ne fonctionneront pas sans base de données"
    echo "💡 Exécutez: sudo bash scripts/install-tools.sh"
    echo ""
fi

echo ""
echo "🎮 Serveurs Minecraft prêts à démarrer !"
echo ""
echo "════════════════════════════════════════"
echo "  COMMANDES DE DÉMARRAGE"
echo "════════════════════════════════════════"
echo ""
echo "Terminal 1 - Velocity (Proxy) :"
echo "  cd $PROJECT_DIR/test-servers/velocity"
echo "  java -Xms512M -Xmx1G -jar velocity.jar"
echo ""
echo "Terminal 2 - Earth (Serveur principal) :"
echo "  cd $PROJECT_DIR/test-servers/earth"
echo "  java -Xms4G -Xmx8G -jar paper.jar --nogui"
echo ""
echo "════════════════════════════════════════"
echo ""
echo "💡 Avec 'screen' pour lancer en arrière-plan :"
echo ""
echo "  # Velocity"
echo "  screen -S velocity"
echo "  cd $PROJECT_DIR/test-servers/velocity && java -Xms512M -Xmx1G -jar velocity.jar"
echo "  # Ctrl+A puis D pour détacher"
echo ""
echo "  # Earth"
echo "  screen -S earth"
echo "  cd $PROJECT_DIR/test-servers/earth && java -Xms4G -Xmx8G -jar paper.jar --nogui"
echo "  # Ctrl+A puis D pour détacher"
echo ""
echo "════════════════════════════════════════"
echo ""
echo "📊 Informations :"
echo "  • Velocity : 0.0.0.0:25577 (connexion joueurs)"
echo "  • Earth    : 127.0.0.1:25566 (backend)"
echo "  • Plugins  : HegemoniaCore + HegemoniaNations"
echo ""
echo "🔗 Connexion depuis Minecraft :"
echo "  Adresse : VOTRE_IP:25577"
echo ""
echo "Pour trouver votre IP publique :"
echo "  curl ifconfig.me"
echo ""
