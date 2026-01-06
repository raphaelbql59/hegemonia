#!/bin/bash

# Hegemonia - Deployment Script for OVH VPS
# This script sets up the entire infrastructure

set -e

echo "🚀 Starting Hegemonia deployment..."

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}Please run as root (sudo)${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Running as root${NC}"

# Update system
echo -e "${YELLOW}Updating system packages...${NC}"
apt update && apt upgrade -y

# Install Docker
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Installing Docker...${NC}"
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    systemctl enable docker
    systemctl start docker
    echo -e "${GREEN}✓ Docker installed${NC}"
else
    echo -e "${GREEN}✓ Docker already installed${NC}"
fi

# Install Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Installing Docker Compose...${NC}"
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✓ Docker Compose installed${NC}"
else
    echo -e "${GREEN}✓ Docker Compose already installed${NC}"
fi

# Install additional tools
echo -e "${YELLOW}Installing additional tools...${NC}"
apt install -y git curl wget htop nano ufw fail2ban

# Configure firewall
echo -e "${YELLOW}Configuring firewall...${NC}"
ufw allow 22/tcp      # SSH
ufw allow 80/tcp      # HTTP
ufw allow 443/tcp     # HTTPS
ufw allow 25565/tcp   # Minecraft
ufw --force enable
echo -e "${GREEN}✓ Firewall configured${NC}"

# Create directories
echo -e "${YELLOW}Creating directories...${NC}"
mkdir -p /opt/hegemonia
mkdir -p /opt/hegemonia/backups
mkdir -p /opt/hegemonia/logs

# Clone repository (if not already present)
if [ ! -d "/opt/hegemonia/.git" ]; then
    echo -e "${YELLOW}Enter your git repository URL:${NC}"
    read -r REPO_URL
    git clone "$REPO_URL" /opt/hegemonia
else
    echo -e "${GREEN}✓ Repository already cloned${NC}"
fi

cd /opt/hegemonia

# Build and start services
echo -e "${YELLOW}Building and starting services...${NC}"
docker-compose build
docker-compose up -d

echo -e "${GREEN}✓ Services started${NC}"

# Wait for services to be healthy
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

# Check services status
docker-compose ps

# Setup automatic backups
echo -e "${YELLOW}Setting up automatic backups...${NC}"
cat > /opt/hegemonia/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/hegemonia/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup Minecraft world
docker exec hegemonia-minecraft tar czf /data/backup_${DATE}.tar.gz /data/world
docker cp hegemonia-minecraft:/data/backup_${DATE}.tar.gz ${BACKUP_DIR}/
docker exec hegemonia-minecraft rm /data/backup_${DATE}.tar.gz

# Backup database
docker exec hegemonia-db pg_dump -U hegemonia hegemonia > ${BACKUP_DIR}/db_backup_${DATE}.sql

# Delete old backups (keep last 7 days)
find ${BACKUP_DIR} -name "*.tar.gz" -mtime +7 -delete
find ${BACKUP_DIR} -name "*.sql" -mtime +7 -delete

echo "Backup completed: ${DATE}"
EOF

chmod +x /opt/hegemonia/backup.sh

# Add cron job for daily backups
(crontab -l 2>/dev/null; echo "0 4 * * * /opt/hegemonia/backup.sh >> /opt/hegemonia/logs/backup.log 2>&1") | crontab -

echo -e "${GREEN}✓ Backup system configured${NC}"

# Display information
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Hegemonia Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "Minecraft Server: ${YELLOW}$(curl -s ifconfig.me):25565${NC}"
echo -e "API Endpoint: ${YELLOW}http://$(curl -s ifconfig.me):3000${NC}"
echo ""
echo -e "To view logs: ${YELLOW}docker-compose logs -f${NC}"
echo -e "To restart: ${YELLOW}docker-compose restart${NC}"
echo -e "To stop: ${YELLOW}docker-compose down${NC}"
echo ""
echo -e "${YELLOW}Important: Update passwords in docker-compose.yml before going to production!${NC}"
echo ""
