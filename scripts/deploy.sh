#!/bin/bash
# ===========================
# HEGEMONIA - Deployment Script
# Script principal de déploiement
# ===========================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                                                          ║"
echo "║     ██╗  ██╗███████╗ ██████╗ ███████╗███╗   ███╗         ║"
echo "║     ██║  ██║██╔════╝██╔════╝ ██╔════╝████╗ ████║         ║"
echo "║     ███████║█████╗  ██║  ███╗█████╗  ██╔████╔██║         ║"
echo "║     ██╔══██║██╔══╝  ██║   ██║██╔══╝  ██║╚██╔╝██║         ║"
echo "║     ██║  ██║███████╗╚██████╔╝███████╗██║ ╚═╝ ██║         ║"
echo "║     ╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚══════╝╚═╝     ╚═╝         ║"
echo "║                                                          ║"
echo "║              DEPLOYMENT SCRIPT v1.0                      ║"
echo "║                                                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

cd "$PROJECT_DIR"

# Fonction d'aide
show_help() {
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  init        Initialize the project (generate secrets, setup)"
    echo "  start       Start all services"
    echo "  stop        Stop all services"
    echo "  restart     Restart all services"
    echo "  status      Show services status"
    echo "  logs        Show logs (use: logs [service])"
    echo "  update      Update and rebuild containers"
    echo "  backup      Create a backup"
    echo "  help        Show this help"
    echo ""
}

# Vérification des prérequis
check_prerequisites() {
    echo -e "${YELLOW}[*] Checking prerequisites...${NC}"

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[!] Docker is not installed${NC}"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}[!] Docker Compose is not installed${NC}"
        exit 1
    fi

    echo -e "${GREEN}[✓] Prerequisites OK${NC}"
}

# Initialisation
init_project() {
    echo -e "${YELLOW}[*] Initializing HEGEMONIA...${NC}"

    # Génère les secrets
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}[*] Generating secrets...${NC}"
        "$SCRIPT_DIR/generate-secrets.sh"
    else
        echo -e "${GREEN}[✓] .env already exists${NC}"
    fi

    # Crée les dossiers nécessaires
    echo -e "${YELLOW}[*] Creating directories...${NC}"
    mkdir -p data/postgres
    mkdir -p data/redis
    mkdir -p data/minecraft/{velocity,lobby,earth,wars,resources,events}
    mkdir -p data/backups
    mkdir -p logs

    # Télécharge les JARs si nécessaire
    echo -e "${YELLOW}[*] Checking server JARs...${NC}"

    if [ ! -f "data/minecraft/velocity/velocity.jar" ]; then
        echo -e "${YELLOW}    Downloading Velocity...${NC}"
        # Note: URL à mettre à jour avec la dernière version
        echo -e "${RED}    [!] Please download Velocity manually from: https://papermc.io/downloads/velocity${NC}"
        echo -e "${RED}    [!] Place it in: data/minecraft/velocity/velocity.jar${NC}"
    fi

    for server in lobby earth wars resources events; do
        if [ ! -f "data/minecraft/$server/paper.jar" ]; then
            echo -e "${YELLOW}    [!] Missing paper.jar for $server${NC}"
            echo -e "${RED}    Please download Paper from: https://papermc.io/downloads/paper${NC}"
            echo -e "${RED}    Place it in: data/minecraft/$server/paper.jar${NC}"
        fi
    done

    echo -e "${GREEN}[✓] Initialization complete${NC}"
}

# Démarrage des services
start_services() {
    echo -e "${YELLOW}[*] Starting HEGEMONIA services...${NC}"

    if docker compose version &> /dev/null; then
        docker compose up -d
    else
        docker-compose up -d
    fi

    echo -e "${GREEN}[✓] Services started${NC}"
    echo ""
    echo "Services:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
}

# Arrêt des services
stop_services() {
    echo -e "${YELLOW}[*] Stopping HEGEMONIA services...${NC}"

    if docker compose version &> /dev/null; then
        docker compose down
    else
        docker-compose down
    fi

    echo -e "${GREEN}[✓] Services stopped${NC}"
}

# Redémarrage
restart_services() {
    stop_services
    start_services
}

# Statut
show_status() {
    echo -e "${BLUE}[*] HEGEMONIA Services Status${NC}"
    echo ""

    if docker compose version &> /dev/null; then
        docker compose ps
    else
        docker-compose ps
    fi
}

# Logs
show_logs() {
    local service=$1

    if [ -z "$service" ]; then
        if docker compose version &> /dev/null; then
            docker compose logs -f --tail=100
        else
            docker-compose logs -f --tail=100
        fi
    else
        if docker compose version &> /dev/null; then
            docker compose logs -f --tail=100 "$service"
        else
            docker-compose logs -f --tail=100 "$service"
        fi
    fi
}

# Mise à jour
update_services() {
    echo -e "${YELLOW}[*] Updating HEGEMONIA...${NC}"

    # Pull les dernières images
    if docker compose version &> /dev/null; then
        docker compose pull
        docker compose up -d --build
    else
        docker-compose pull
        docker-compose up -d --build
    fi

    echo -e "${GREEN}[✓] Update complete${NC}"
}

# Backup
create_backup() {
    echo -e "${YELLOW}[*] Creating backup...${NC}"

    "$SCRIPT_DIR/backup.sh"

    echo -e "${GREEN}[✓] Backup complete${NC}"
}

# Main
check_prerequisites

case "${1:-help}" in
    init)
        init_project
        ;;
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "$2"
        ;;
    update)
        update_services
        ;;
    backup)
        create_backup
        ;;
    help|*)
        show_help
        ;;
esac
