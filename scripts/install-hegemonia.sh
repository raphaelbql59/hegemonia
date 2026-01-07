#!/bin/bash

# ===========================
# HEGEMONIA - Installation Script
# Install all dependencies and setup the project
# ===========================

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo ""
    echo -e "${BLUE}=== $1 ===${NC}"
    echo ""
}

# Check if running as minecraft user (or root for initial setup)
if [[ $EUID -eq 0 ]] && [[ -d /home/minecraft ]]; then
    log_warn "Running as root. Consider running as 'minecraft' user."
fi

PROJECT_DIR="/home/hegemonia/hegemonia-project"
cd "$PROJECT_DIR" || { log_error "Project directory not found!"; exit 1; }

# ===========================
# 1. INSTALL DEPENDENCIES
# ===========================

log_step "Installing system dependencies"

sudo apt update
sudo apt install -y \
    curl \
    wget \
    git \
    gnupg \
    lsb-release \
    ca-certificates \
    software-properties-common \
    apt-transport-https \
    build-essential \
    openssl

log_info "System dependencies installed"

# ===========================
# 2. INSTALL DOCKER
# ===========================

log_step "Installing Docker"

if command -v docker &> /dev/null; then
    log_warn "Docker already installed, skipping..."
else
    # Add Docker GPG key
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

    # Add Docker repository
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    # Install Docker
    sudo apt update
    sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    # Add current user to docker group
    if [[ $EUID -ne 0 ]]; then
        sudo usermod -aG docker $USER
        log_info "Added $USER to docker group. You may need to log out and back in."
    fi

    # Enable Docker service
    sudo systemctl enable docker
    sudo systemctl start docker

    log_info "Docker installed successfully"
fi

# ===========================
# 3. INSTALL DOCKER COMPOSE
# ===========================

log_step "Installing Docker Compose"

if command -v docker-compose &> /dev/null; then
    log_warn "Docker Compose already installed, skipping..."
else
    # Install Docker Compose
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose

    # Verify installation
    docker-compose --version

    log_info "Docker Compose installed successfully"
fi

# ===========================
# 4. CONFIGURE ENVIRONMENT
# ===========================

log_step "Configuring environment variables"

if [[ ! -f .env ]]; then
    log_info "Creating .env file from template..."
    cp .env.example .env

    # Generate secrets
    log_info "Generating secure secrets..."

    # Generate PostgreSQL password
    POSTGRES_PASS=$(openssl rand -base64 32)
    sed -i "s/POSTGRES_PASSWORD=.*/POSTGRES_PASSWORD=$POSTGRES_PASS/" .env

    # Generate Velocity secret
    VELOCITY_SECRET=$(openssl rand -base64 32)
    sed -i "s/VELOCITY_SECRET=.*/VELOCITY_SECRET=$VELOCITY_SECRET/" .env

    # Update velocity.toml with the secret
    sed -i "s/secret = \".*\"/secret = \"$VELOCITY_SECRET\"/" server/velocity/velocity.toml

    # Update paper-global.yml with the secret
    sed -i "s/secret: \".*\"/secret: \"$VELOCITY_SECRET\"/" server/configs/paper-global.yml

    # Generate NextAuth secret
    NEXTAUTH_SECRET=$(openssl rand -base64 32)
    sed -i "s/NEXTAUTH_SECRET=.*/NEXTAUTH_SECRET=$NEXTAUTH_SECRET/" .env

    # Generate JWT secret
    JWT_SECRET=$(openssl rand -base64 32)
    sed -i "s/JWT_SECRET=.*/JWT_SECRET=$JWT_SECRET/" .env

    log_info "Secrets generated and configured"
    log_warn "⚠️  Please edit .env file to configure additional settings!"
else
    log_warn ".env file already exists, skipping..."
fi

# ===========================
# 5. CREATE NECESSARY DIRECTORIES
# ===========================

log_step "Creating necessary directories"

mkdir -p server/velocity/plugins
mkdir -p server/paper/{lobby,earth,wars,resources,events}/plugins
mkdir -p backups
mkdir -p nginx/conf.d
mkdir -p database/postgresql/backups

# Copy default configurations
if [[ ! -f server/paper/lobby/config/paper-global.yml ]]; then
    mkdir -p server/paper/lobby/config
    cp server/configs/paper-global.yml server/paper/lobby/config/
    cp server/configs/paper-world-defaults.yml server/paper/lobby/config/
    cp server/configs/server.properties server/paper/lobby/
fi

# Repeat for other servers
for server_name in earth wars resources events; do
    if [[ ! -f server/paper/$server_name/config/paper-global.yml ]]; then
        mkdir -p server/paper/$server_name/config
        cp server/configs/paper-global.yml server/paper/$server_name/config/
        cp server/configs/paper-world-defaults.yml server/paper/$server_name/config/
        cp server/configs/server.properties server/paper/$server_name/
    fi
done

log_info "Directories created and configurations copied"

# ===========================
# 6. SETUP NGINX CONFIGURATION
# ===========================

log_step "Setting up Nginx configuration"

cat > nginx/nginx.conf <<'EOF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 2048;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_conn_zone $binary_remote_addr zone=addr:10m;

    # Include server configs
    include /etc/nginx/conf.d/*.conf;
}
EOF

cat > nginx/conf.d/default.conf <<'EOF'
# Health check endpoint
server {
    listen 80;
    server_name _;

    location /health {
        access_log off;
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }

    # Redirect all HTTP to HTTPS (when SSL is configured)
    # Uncomment after setting up Let's Encrypt
    # return 301 https://$server_name$request_uri;
}

# Main website (when ready)
# server {
#     listen 443 ssl http2;
#     server_name hegemonia.com www.hegemonia.com;
#
#     ssl_certificate /etc/letsencrypt/live/hegemonia.com/fullchain.pem;
#     ssl_certificate_key /etc/letsencrypt/live/hegemonia.com/privkey.pem;
#
#     location / {
#         proxy_pass http://web:3000;
#         proxy_http_version 1.1;
#         proxy_set_header Upgrade $http_upgrade;
#         proxy_set_header Connection 'upgrade';
#         proxy_set_header Host $host;
#         proxy_cache_bypass $http_upgrade;
#     }
# }
EOF

log_info "Nginx configuration created"

# ===========================
# 7. BUILD AND START SERVICES
# ===========================

log_step "Building and starting Docker services"

log_info "Pulling Docker images..."
docker-compose pull

log_info "Starting services..."
docker-compose up -d postgres redis

log_info "Waiting for database to be ready..."
sleep 10

log_info "Database initialized. Starting Minecraft servers..."
docker-compose up -d

log_info "All services started!"

# ===========================
# 8. VERIFY INSTALLATION
# ===========================

log_step "Verifying installation"

sleep 5

# Check running containers
RUNNING=$(docker-compose ps --services --filter "status=running" | wc -l)
TOTAL=$(docker-compose ps --services | wc -l)

echo "Services running: $RUNNING/$TOTAL"

if [[ $RUNNING -eq $TOTAL ]]; then
    log_info "✅ All services are running!"
else
    log_warn "⚠️  Some services are not running. Check with: docker-compose ps"
fi

# ===========================
# 9. DISPLAY INFORMATION
# ===========================

clear
echo "========================================="
echo "   HEGEMONIA INSTALLATION COMPLETE!"
echo "========================================="
echo ""
echo "✅ Docker installed"
echo "✅ Docker Compose installed"
echo "✅ Environment configured"
echo "✅ Services started"
echo ""
echo "📊 Service Status:"
docker-compose ps
echo ""
echo "🎮 Minecraft Server:"
echo "   Connect to: YOUR_SERVER_IP:25565"
echo ""
echo "🌐 Web Services:"
echo "   API: http://YOUR_SERVER_IP:3000"
echo "   Monitoring: http://YOUR_SERVER_IP:19999 (Netdata)"
echo ""
echo "📚 Useful Commands:"
echo "   View logs: docker-compose logs -f [service]"
echo "   Restart service: docker-compose restart [service]"
echo "   Stop all: docker-compose down"
echo "   Start all: docker-compose up -d"
echo ""
echo "📝 Next Steps:"
echo "1. Edit .env file with your specific configuration"
echo "2. Configure your domain DNS to point to this server"
echo "3. Set up SSL certificates: certbot --nginx"
echo "4. Upload custom plugins to server/plugins/"
echo "5. Upload custom mods to server/mods/"
echo "6. Configure permissions with LuckPerms"
echo ""
echo "📖 Documentation: /home/hegemonia/hegemonia-project/docs/"
echo ""
echo "========================================="

log_info "Installation completed successfully!"
