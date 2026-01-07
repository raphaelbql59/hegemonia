#!/bin/bash

# ===========================
# HEGEMONIA - Backup Script
# Automated backup of all critical data
# ===========================

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[BACKUP]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Configuration
PROJECT_DIR="/home/hegemonia/hegemonia-project"
BACKUP_DIR="$PROJECT_DIR/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="hegemonia-backup-$DATE"
RETENTION_DAYS=7

cd "$PROJECT_DIR"

log_info "Starting backup: $BACKUP_NAME"

# Create backup directory
mkdir -p "$BACKUP_DIR/$BACKUP_NAME"

# ===========================
# 1. BACKUP POSTGRESQL
# ===========================

log_info "Backing up PostgreSQL databases..."

# Get database credentials from .env
source .env

# Backup main database
docker-compose exec -T postgres pg_dump -U $POSTGRES_USER hegemonia_main | gzip > "$BACKUP_DIR/$BACKUP_NAME/hegemonia_main.sql.gz"
log_info "✓ Main database backed up"

# Backup web database
docker-compose exec -T postgres pg_dump -U $POSTGRES_USER hegemonia_web | gzip > "$BACKUP_DIR/$BACKUP_NAME/hegemonia_web.sql.gz"
log_info "✓ Web database backed up"

# ===========================
# 2. BACKUP REDIS (if needed)
# ===========================

log_info "Backing up Redis data..."
docker-compose exec -T redis redis-cli SAVE
docker cp hegemonia-redis:/data/dump.rdb "$BACKUP_DIR/$BACKUP_NAME/redis-dump.rdb" 2>/dev/null || log_warn "No Redis data to backup"

# ===========================
# 3. BACKUP MINECRAFT WORLDS
# ===========================

log_info "Backing up Minecraft worlds..."

# Lobby world
if [ -d "server/paper/lobby/world" ]; then
    tar -czf "$BACKUP_DIR/$BACKUP_NAME/lobby-world.tar.gz" -C server/paper/lobby world
    log_info "✓ Lobby world backed up"
fi

# Earth world (most important)
if [ -d "server/paper/earth/world" ]; then
    tar -czf "$BACKUP_DIR/$BACKUP_NAME/earth-world.tar.gz" -C server/paper/earth world
    log_info "✓ Earth world backed up"
fi

# Wars worlds
if [ -d "server/paper/wars/world" ]; then
    tar -czf "$BACKUP_DIR/$BACKUP_NAME/wars-world.tar.gz" -C server/paper/wars world
    log_info "✓ Wars world backed up"
fi

# Resources worlds
if [ -d "server/paper/resources/world" ]; then
    tar -czf "$BACKUP_DIR/$BACKUP_NAME/resources-world.tar.gz" -C server/paper/resources world
    log_info "✓ Resources world backed up"
fi

# ===========================
# 4. BACKUP CONFIGURATIONS
# ===========================

log_info "Backing up configurations..."

tar -czf "$BACKUP_DIR/$BACKUP_NAME/configs.tar.gz" \
    server/velocity/velocity.toml \
    server/configs/ \
    .env \
    docker-compose.yml \
    nginx/

log_info "✓ Configurations backed up"

# ===========================
# 5. BACKUP PLUGINS DATA
# ===========================

log_info "Backing up plugin data..."

# Backup plugin configs and data directories
for server_name in lobby earth wars resources events; do
    if [ -d "server/paper/$server_name/plugins" ]; then
        tar -czf "$BACKUP_DIR/$BACKUP_NAME/$server_name-plugins-data.tar.gz" \
            -C server/paper/$server_name plugins
    fi
done

log_info "✓ Plugin data backed up"

# ===========================
# 6. CREATE ARCHIVE
# ===========================

log_info "Creating final archive..."

cd "$BACKUP_DIR"
tar -czf "$BACKUP_NAME.tar.gz" "$BACKUP_NAME"
rm -rf "$BACKUP_NAME"

BACKUP_SIZE=$(du -h "$BACKUP_NAME.tar.gz" | cut -f1)
log_info "✓ Backup created: $BACKUP_NAME.tar.gz ($BACKUP_SIZE)"

# ===========================
# 7. CLEANUP OLD BACKUPS
# ===========================

log_info "Cleaning up old backups (retention: $RETENTION_DAYS days)..."

find "$BACKUP_DIR" -name "hegemonia-backup-*.tar.gz" -type f -mtime +$RETENTION_DAYS -delete

BACKUP_COUNT=$(find "$BACKUP_DIR" -name "hegemonia-backup-*.tar.gz" | wc -l)
log_info "✓ Cleanup complete. Total backups: $BACKUP_COUNT"

# ===========================
# 8. VERIFY BACKUP
# ===========================

log_info "Verifying backup integrity..."

if tar -tzf "$BACKUP_DIR/$BACKUP_NAME.tar.gz" >/dev/null 2>&1; then
    log_info "✓ Backup verified successfully"
else
    log_error "✗ Backup verification failed!"
    exit 1
fi

# ===========================
# 9. UPLOAD TO CLOUD (Optional)
# ===========================

if [ ! -z "$AWS_ACCESS_KEY_ID" ] && [ ! -z "$AWS_S3_BUCKET" ]; then
    log_info "Uploading to AWS S3..."

    if command -v aws &> /dev/null; then
        aws s3 cp "$BACKUP_DIR/$BACKUP_NAME.tar.gz" "s3://$AWS_S3_BUCKET/hegemonia/" --storage-class STANDARD_IA
        log_info "✓ Backup uploaded to S3"
    else
        log_warn "AWS CLI not installed, skipping cloud upload"
    fi
fi

# ===========================
# SUMMARY
# ===========================

echo ""
echo "==================================="
echo "  BACKUP COMPLETED SUCCESSFULLY"
echo "==================================="
echo ""
echo "📦 Backup: $BACKUP_NAME.tar.gz"
echo "📏 Size: $BACKUP_SIZE"
echo "📁 Location: $BACKUP_DIR"
echo "📊 Total backups: $BACKUP_COUNT"
echo ""
echo "To restore:"
echo "  ./scripts/restore.sh $BACKUP_NAME.tar.gz"
echo ""

log_info "Backup completed successfully!"
