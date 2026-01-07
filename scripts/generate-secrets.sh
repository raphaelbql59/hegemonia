#!/bin/bash
# ===========================
# HEGEMONIA - Secrets Generator
# Génère tous les secrets nécessaires pour le déploiement
# ===========================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=================================="
echo "  HEGEMONIA - Secrets Generator"
echo "=================================="
echo ""

# Crée le dossier secrets s'il n'existe pas
mkdir -p "$PROJECT_DIR/secrets"

# Fonction pour générer un secret
generate_secret() {
    openssl rand -base64 32
}

# Fonction pour générer un mot de passe fort
generate_password() {
    openssl rand -base64 24 | tr -d '/+=' | head -c 32
}

# 1. Velocity Forwarding Secret
echo "[1/5] Generating Velocity forwarding secret..."
VELOCITY_SECRET=$(generate_secret)
echo "$VELOCITY_SECRET" > "$PROJECT_DIR/server/velocity/forwarding.secret"
echo "      -> server/velocity/forwarding.secret"

# 2. PostgreSQL Password
echo "[2/5] Generating PostgreSQL password..."
POSTGRES_PASSWORD=$(generate_password)
echo "$POSTGRES_PASSWORD" > "$PROJECT_DIR/secrets/postgres_password"
echo "      -> secrets/postgres_password"

# 3. NextAuth Secret
echo "[3/5] Generating NextAuth secret..."
NEXTAUTH_SECRET=$(generate_secret)
echo "$NEXTAUTH_SECRET" > "$PROJECT_DIR/secrets/nextauth_secret"
echo "      -> secrets/nextauth_secret"

# 4. JWT Secret
echo "[4/5] Generating JWT secret..."
JWT_SECRET=$(generate_secret)
echo "$JWT_SECRET" > "$PROJECT_DIR/secrets/jwt_secret"
echo "      -> secrets/jwt_secret"

# 5. Créer le fichier .env depuis le template
echo "[5/5] Creating .env file..."
if [ -f "$PROJECT_DIR/.env" ]; then
    echo "      [!] .env already exists, creating .env.new instead"
    ENV_FILE="$PROJECT_DIR/.env.new"
else
    ENV_FILE="$PROJECT_DIR/.env"
fi

cat > "$ENV_FILE" << EOF
# ===========================
# HEGEMONIA - Environment Variables
# Généré automatiquement le $(date +%Y-%m-%d)
# ===========================

# ===========================
# BASE DE DONNÉES
# ===========================
POSTGRES_USER=hegemonia
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
POSTGRES_DB=hegemonia_main

# ===========================
# WEB & API
# ===========================
NEXTAUTH_SECRET=$NEXTAUTH_SECRET
NEXTAUTH_URL=https://hegemonia.com
DATABASE_URL=postgresql://hegemonia:$POSTGRES_PASSWORD@postgres:5432/hegemonia_main
REDIS_URL=redis://redis:6379

# ===========================
# MINECRAFT
# ===========================
VELOCITY_SECRET=$VELOCITY_SECRET

# ===========================
# SÉCURITÉ
# ===========================
JWT_SECRET=$JWT_SECRET
RATE_LIMIT_API=100
RATE_LIMIT_AUTH=10

# ===========================
# DISCORD (À CONFIGURER)
# ===========================
DISCORD_BOT_TOKEN=
DISCORD_CLIENT_ID=
DISCORD_CLIENT_SECRET=
DISCORD_GUILD_ID=

# Webhooks
DISCORD_WEBHOOK_WARS=
DISCORD_WEBHOOK_ECONOMY=
DISCORD_WEBHOOK_DIPLOMACY=
DISCORD_WEBHOOK_LOGS=

# ===========================
# EMAIL (Optionnel)
# ===========================
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASSWORD=
SMTP_FROM=noreply@hegemonia.com

# ===========================
# PAIEMENTS (Optionnel)
# ===========================
STRIPE_PUBLIC_KEY=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=

# ===========================
# MONITORING
# ===========================
NETDATA_CLAIM_TOKEN=
NETDATA_CLAIM_ROOMS=

# ===========================
# BACKUPS (Optionnel)
# ===========================
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_S3_BUCKET=hegemonia-backups
AWS_REGION=eu-west-3

# ===========================
# DÉVELOPPEMENT
# ===========================
NODE_ENV=production
DEBUG=false
EOF

echo "      -> $ENV_FILE"

echo ""
echo "=================================="
echo "  Secrets generated successfully!"
echo "=================================="
echo ""
echo "IMPORTANT:"
echo "1. Keep these files secure and never commit them to Git!"
echo "2. The velocity secret has been placed in:"
echo "   - server/velocity/forwarding.secret"
echo "3. Update the Paper servers' config/paper-global.yml with the same secret"
echo "4. Configure Discord and other optional services in .env"
echo ""
echo "Next steps:"
echo "  1. Review the .env file"
echo "  2. Configure Discord bot tokens"
echo "  3. Run: docker-compose up -d"
echo ""
