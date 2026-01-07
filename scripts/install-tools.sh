#!/bin/bash

#################################################################
# HEGEMONIA - Installation des outils de développement
# À exécuter avec sudo sur le VPS
#################################################################

set -e

echo "========================================="
echo "  HEGEMONIA - Installation des outils"
echo "========================================="
echo ""

# Vérifier sudo
if [ "$EUID" -ne 0 ]; then
    echo "❌ Ce script doit être exécuté avec sudo"
    echo "Usage: sudo bash install-tools.sh"
    exit 1
fi

echo "✓ Droits root confirmés"
echo ""

# Mise à jour du système
echo "📦 Mise à jour du système..."
apt update -qq

# Installation de Java 21 (requis pour Paper 1.20.4+)
echo "☕ Installation de Java 21..."
apt install -y openjdk-21-jdk-headless

# Installation de Gradle
echo "🔨 Installation de Gradle..."
apt install -y gradle

# Installation de Docker et Docker Compose
echo "🐳 Installation de Docker..."
apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

# Ajouter la clé GPG Docker
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Ajouter le repository Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Installer Docker
apt update -qq
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Ajouter l'utilisateur hegemonia au groupe docker
usermod -aG docker hegemonia

# Installation d'outils utiles
echo "🛠️  Installation d'outils supplémentaires..."
apt install -y wget curl unzip zip git screen htop net-tools

# Vérification des installations
echo ""
echo "========================================="
echo "  Vérification des installations"
echo "========================================="
echo ""

echo "Java version:"
java -version
echo ""

echo "Gradle version:"
gradle --version | head -3
echo ""

echo "Docker version:"
docker --version
echo ""

echo "Docker Compose version:"
docker compose version
echo ""

echo "========================================="
echo "  ✅ Installation terminée avec succès!"
echo "========================================="
echo ""
echo "⚠️  IMPORTANT: Déconnectez-vous et reconnectez-vous pour que"
echo "    les permissions Docker prennent effet."
echo ""
echo "Ensuite, vous pourrez:"
echo "  1. Builder les plugins: cd ~/hegemonia-project/server/plugins && gradle build"
echo "  2. Lancer la stack: cd ~/hegemonia-project && docker compose up -d"
echo "  3. Démarrer les serveurs Minecraft"
echo ""
