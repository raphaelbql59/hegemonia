#!/bin/bash
# ===========================
# HEGEMONIA - Paper Server Startup Script
# Flags JVM optimisés (Aikar's Flags)
# ===========================

set -e

# Configuration par défaut
PAPER_JAR="${PAPER_JAR:-paper.jar}"
MEMORY="${MEMORY:-4G}"
SERVER_NAME="${SERVER_NAME:-paper}"

# Vérifie si le JAR existe
if [ ! -f "$PAPER_JAR" ]; then
    echo "[ERROR] $PAPER_JAR not found!"
    echo "Please download Paper from: https://papermc.io/downloads/paper"
    exit 1
fi

# Copie le forwarding secret si disponible
if [ -f "/shared/forwarding.secret" ]; then
    VELOCITY_SECRET=$(cat /shared/forwarding.secret)
    if [ -f "config/paper-global.yml" ]; then
        sed -i "s/CHANGEME_SAME_AS_VELOCITY_SECRET/$VELOCITY_SECRET/g" config/paper-global.yml
        echo "[INFO] Velocity secret configured"
    fi
fi

# Accepte l'EULA automatiquement
echo "eula=true" > eula.txt

echo "=================================="
echo "  HEGEMONIA - $SERVER_NAME Server"
echo "=================================="
echo ""
echo "Memory: $MEMORY"
echo "JAR: $PAPER_JAR"
echo ""

# Détermine les flags selon la RAM allouée
# Pour 12GB ou plus, utiliser les flags pour gros serveurs
MEMORY_NUM=$(echo $MEMORY | sed 's/[^0-9]//g')

if [ "$MEMORY_NUM" -ge 12 ]; then
    # Flags pour 12GB+ RAM (serveurs Earth, gros)
    AIKAR_FLAGS="-XX:+UseG1GC \
        -XX:+ParallelRefProcEnabled \
        -XX:MaxGCPauseMillis=200 \
        -XX:+UnlockExperimentalVMOptions \
        -XX:+DisableExplicitGC \
        -XX:+AlwaysPreTouch \
        -XX:G1NewSizePercent=40 \
        -XX:G1MaxNewSizePercent=50 \
        -XX:G1HeapRegionSize=16M \
        -XX:G1ReservePercent=15 \
        -XX:G1HeapWastePercent=5 \
        -XX:G1MixedGCCountTarget=4 \
        -XX:InitiatingHeapOccupancyPercent=20 \
        -XX:G1MixedGCLiveThresholdPercent=90 \
        -XX:G1RSetUpdatingPauseTimePercent=5 \
        -XX:SurvivorRatio=32 \
        -XX:+PerfDisableSharedMem \
        -XX:MaxTenuringThreshold=1"
else
    # Flags standard (< 12GB)
    AIKAR_FLAGS="-XX:+UseG1GC \
        -XX:+ParallelRefProcEnabled \
        -XX:MaxGCPauseMillis=200 \
        -XX:+UnlockExperimentalVMOptions \
        -XX:+DisableExplicitGC \
        -XX:+AlwaysPreTouch \
        -XX:G1NewSizePercent=30 \
        -XX:G1MaxNewSizePercent=40 \
        -XX:G1HeapRegionSize=8M \
        -XX:G1ReservePercent=20 \
        -XX:G1HeapWastePercent=5 \
        -XX:G1MixedGCCountTarget=4 \
        -XX:InitiatingHeapOccupancyPercent=15 \
        -XX:G1MixedGCLiveThresholdPercent=90 \
        -XX:G1RSetUpdatingPauseTimePercent=5 \
        -XX:SurvivorRatio=32 \
        -XX:+PerfDisableSharedMem \
        -XX:MaxTenuringThreshold=1"
fi

# Flags de sécurité Log4j
SECURITY_FLAGS="-Dlog4j2.formatMsgNoLookups=true"

# Flags Paper
PAPER_FLAGS="--nogui --nojline"

# Démarre le serveur
exec java \
    -Xms$MEMORY \
    -Xmx$MEMORY \
    $AIKAR_FLAGS \
    $SECURITY_FLAGS \
    -jar $PAPER_JAR \
    $PAPER_FLAGS
