#!/bin/bash

# Hegemonia Development Helper Script
# Provides easy commands for development workflow

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Help
show_help() {
    print_header "Hegemonia Development Tools"
    echo ""
    echo "Usage: ./dev.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start          Start all development services"
    echo "  stop           Stop all services"
    echo "  restart        Restart all services"
    echo "  logs           Show logs (all services)"
    echo "  logs-api       Show API logs only"
    echo "  logs-db        Show database logs"
    echo "  shell-api      Enter API container shell"
    echo "  shell-db       Enter PostgreSQL shell"
    echo "  db-reset       Reset database (WARNING: deletes all data)"
    echo "  db-migrate     Run Prisma migrations"
    echo "  db-studio      Open Prisma Studio"
    echo "  build          Rebuild all containers"
    echo "  clean          Clean all containers and volumes"
    echo "  status         Show services status"
    echo "  test           Run tests"
    echo ""
}

# Start development environment
start_dev() {
    print_header "Starting Development Environment"
    docker-compose -f docker-compose.dev.yml up -d
    print_success "Services started!"
    print_info "API: http://localhost:3000"
    print_info "PostgreSQL: localhost:5432"
    print_info "Redis: localhost:6379"
    echo ""
    print_info "Run './dev.sh logs' to see logs"
}

# Stop services
stop_dev() {
    print_header "Stopping Services"
    docker-compose -f docker-compose.dev.yml down
    print_success "Services stopped"
}

# Restart services
restart_dev() {
    print_header "Restarting Services"
    docker-compose -f docker-compose.dev.yml restart
    print_success "Services restarted"
}

# Show logs
show_logs() {
    print_header "Showing Logs (Ctrl+C to exit)"
    docker-compose -f docker-compose.dev.yml logs -f
}

show_api_logs() {
    print_header "API Logs (Ctrl+C to exit)"
    docker-compose -f docker-compose.dev.yml logs -f api
}

show_db_logs() {
    print_header "Database Logs (Ctrl+C to exit)"
    docker-compose -f docker-compose.dev.yml logs -f postgres
}

# Shell access
api_shell() {
    print_header "Entering API Container Shell"
    docker-compose -f docker-compose.dev.yml exec api sh
}

db_shell() {
    print_header "Entering PostgreSQL Shell"
    docker-compose -f docker-compose.dev.yml exec postgres psql -U hegemonia -d hegemonia
}

# Database operations
reset_db() {
    print_error "WARNING: This will delete all data!"
    read -p "Are you sure? (yes/no): " confirm
    if [ "$confirm" = "yes" ]; then
        print_header "Resetting Database"
        docker-compose -f docker-compose.dev.yml exec api npm run prisma:push -- --force-reset
        print_success "Database reset complete"
    else
        print_info "Cancelled"
    fi
}

migrate_db() {
    print_header "Running Database Migrations"
    docker-compose -f docker-compose.dev.yml exec api npm run prisma:push
    print_success "Migrations complete"
}

studio_db() {
    print_header "Opening Prisma Studio"
    print_info "Opening at http://localhost:5555"
    docker-compose -f docker-compose.dev.yml exec api npm run prisma:studio
}

# Build
build_all() {
    print_header "Building All Containers"
    docker-compose -f docker-compose.dev.yml build --no-cache
    print_success "Build complete"
}

# Clean
clean_all() {
    print_error "WARNING: This will remove all containers and volumes!"
    read -p "Are you sure? (yes/no): " confirm
    if [ "$confirm" = "yes" ]; then
        print_header "Cleaning Everything"
        docker-compose -f docker-compose.dev.yml down -v
        docker system prune -f
        print_success "Cleanup complete"
    else
        print_info "Cancelled"
    fi
}

# Status
show_status() {
    print_header "Services Status"
    docker-compose -f docker-compose.dev.yml ps
}

# Run tests
run_tests() {
    print_header "Running Tests"
    docker-compose -f docker-compose.dev.yml exec api npm test
}

# Main command handler
case "$1" in
    start)
        start_dev
        ;;
    stop)
        stop_dev
        ;;
    restart)
        restart_dev
        ;;
    logs)
        show_logs
        ;;
    logs-api)
        show_api_logs
        ;;
    logs-db)
        show_db_logs
        ;;
    shell-api)
        api_shell
        ;;
    shell-db)
        db_shell
        ;;
    db-reset)
        reset_db
        ;;
    db-migrate)
        migrate_db
        ;;
    db-studio)
        studio_db
        ;;
    build)
        build_all
        ;;
    clean)
        clean_all
        ;;
    status)
        show_status
        ;;
    test)
        run_tests
        ;;
    *)
        show_help
        ;;
esac
