#!/bin/bash

# Hegemonia Server Start Script

echo "Starting Hegemonia Minecraft Server..."

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configuration
SERVER_DIR="/data"
JAR_FILE="fabric-server-launch.jar"
MIN_RAM="8G"
MAX_RAM="16G"

# JVM Flags optimized for Minecraft 1.20.1
JVM_FLAGS="-Xms${MIN_RAM} -Xmx${MAX_RAM} \
-XX:+UseG1GC \
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
-XX:MaxTenuringThreshold=1 \
-Dusing.aikars.flags=https://mcflags.emc.gs \
-Daikars.new.flags=true"

cd $SERVER_DIR

echo -e "${GREEN}[Hegemonia]${NC} Server directory: $SERVER_DIR"
echo -e "${GREEN}[Hegemonia]${NC} RAM allocation: ${MIN_RAM} - ${MAX_RAM}"
echo -e "${GREEN}[Hegemonia]${NC} Starting server..."

# Start server
java $JVM_FLAGS -jar $JAR_FILE nogui

echo -e "${YELLOW}[Hegemonia]${NC} Server stopped."
