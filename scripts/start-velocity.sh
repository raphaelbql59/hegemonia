#!/bin/bash
# ===========================
# HEGEMONIA - Velocity Proxy Startup Script
# ===========================

set -e

VELOCITY_JAR="velocity.jar"
MEMORY="1G"

# Vérifie si le JAR existe
if [ ! -f "$VELOCITY_JAR" ]; then
    echo "[ERROR] $VELOCITY_JAR not found!"
    echo "Please download Velocity from: https://papermc.io/downloads/velocity"
    exit 1
fi

# Génère le forwarding secret si nécessaire
if [ ! -f "forwarding.secret" ] || grep -q "REPLACE_WITH" forwarding.secret 2>/dev/null; then
    echo "[INFO] Generating new forwarding secret..."
    openssl rand -base64 32 > forwarding.secret
    echo "[INFO] Secret generated. Copy this to all Paper servers!"
fi

echo "=================================="
echo "  HEGEMONIA - Velocity Proxy"
echo "=================================="
echo ""
echo "Memory: $MEMORY"
echo "Port: 25565"
echo ""

# Démarre Velocity avec flags optimisés
exec java \
    -Xms$MEMORY \
    -Xmx$MEMORY \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=4M \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+ParallelRefProcEnabled \
    -XX:+AlwaysPreTouch \
    -XX:MaxInlineLevel=15 \
    -Dlog4j2.formatMsgNoLookups=true \
    -jar $VELOCITY_JAR
