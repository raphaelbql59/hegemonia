#!/bin/bash

# Add player to whitelist via RCON

if [ -z "$1" ]; then
  echo "Usage: $0 <username>"
  exit 1
fi

USERNAME=$1
RCON_HOST=${RCON_HOST:-"localhost"}
RCON_PORT=${RCON_PORT:-25575}
RCON_PASSWORD=${RCON_PASSWORD:-"hegemonia2024"}

echo "Adding $USERNAME to whitelist..."

# Using mcrcon or similar tool
docker exec hegemonia-minecraft rcon-cli whitelist add $USERNAME

echo "Done!"
