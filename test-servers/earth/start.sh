#!/bin/bash
# ============================================
# HEGEMONIA - Test Server Startup Script
# ============================================

# Configuration
JAVA_OPTS="-Xms2G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200"
PAPER_OPTS="--nogui"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Change to server directory
cd "$(dirname "$0")"

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}       HEGEMONIA - Test Server${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERREUR] Java non trouvé!${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
echo -e "${YELLOW}[INFO]${NC} Java version: $JAVA_VERSION"

# Check paper.jar
if [ ! -f "paper.jar" ]; then
    echo -e "${RED}[ERREUR] paper.jar non trouvé!${NC}"
    exit 1
fi

# Check plugins
echo -e "${YELLOW}[INFO]${NC} Plugins Hegemonia:"
ls -1 plugins/Hegemonia*.jar 2>/dev/null | while read jar; do
    echo -e "  - $(basename $jar)"
done

echo ""
echo -e "${GREEN}[START]${NC} Démarrage du serveur sur le port 25566..."
echo -e "${YELLOW}[INFO]${NC} Utilisez 'stop' dans la console pour arrêter proprement."
echo ""

# Start server
exec java $JAVA_OPTS -jar paper.jar $PAPER_OPTS
