#!/bin/bash

# Hegemonia Backup Script

BACKUP_DIR="/data/backups"
WORLD_DIR="/data/world"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="hegemonia_backup_${DATE}.tar.gz"

echo "Creating backup: $BACKUP_NAME"

mkdir -p $BACKUP_DIR

# Create backup
tar -czf "${BACKUP_DIR}/${BACKUP_NAME}" \
  --exclude='*.log' \
  --exclude='session.lock' \
  $WORLD_DIR

echo "Backup completed: ${BACKUP_DIR}/${BACKUP_NAME}"

# Keep only last 7 backups
ls -t ${BACKUP_DIR}/hegemonia_backup_*.tar.gz | tail -n +8 | xargs -r rm

echo "Old backups cleaned up."
