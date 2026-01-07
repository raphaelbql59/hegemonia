#!/bin/bash
###############################################
# Script de build du launcher en .exe Windows
###############################################

echo "🔨 Build Hegemonia Launcher..."

# Installer PyInstaller si nécessaire
pip install pyinstaller

# Build
pyinstaller --onefile \
    --windowed \
    --name "HegemoniaLauncher" \
    --icon=icon.ico \
    --add-data "README.md:." \
    launcher.py

echo "✅ Build terminé ! Fichier dans dist/HegemoniaLauncher.exe"
