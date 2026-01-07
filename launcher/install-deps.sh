#!/bin/bash
###############################################
# Script d'installation des dépendances launcher
###############################################

set -e

echo "🚀 Installation des dépendances pour le launcher Hegemonia..."

# Installer Rust
if ! command -v rustc &> /dev/null; then
    echo "📦 Installation de Rust..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
    source "$HOME/.cargo/env"
    echo "✅ Rust installé: $(rustc --version)"
else
    echo "✅ Rust déjà installé: $(rustc --version)"
fi

# Installer Node.js (via nvm)
if ! command -v node &> /dev/null; then
    echo "📦 Installation de Node.js..."
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    nvm install 20
    nvm use 20
    echo "✅ Node.js installé: $(node --version)"
else
    echo "✅ Node.js déjà installé: $(node --version)"
fi

# Installer dépendances système pour Tauri (Debian/Ubuntu)
if command -v apt-get &> /dev/null; then
    echo "📦 Installation des dépendances système Tauri..."
    sudo apt-get update
    sudo apt-get install -y \
        libwebkit2gtk-4.0-dev \
        build-essential \
        curl \
        wget \
        file \
        libssl-dev \
        libgtk-3-dev \
        libayatana-appindicator3-dev \
        librsvg2-dev
    echo "✅ Dépendances système installées"
fi

# Installer Tauri CLI
if ! command -v cargo-tauri &> /dev/null; then
    echo "📦 Installation de Tauri CLI..."
    cargo install tauri-cli
    echo "✅ Tauri CLI installé"
else
    echo "✅ Tauri CLI déjà installé"
fi

echo ""
echo "✅ Toutes les dépendances sont installées !"
echo ""
echo "Prochaines étapes:"
echo "  1. cd launcher/"
echo "  2. npm create tauri-app@latest"
echo ""
