@echo off
title Hegemonia Launcher
color 0B
echo.
echo  ========================================
echo        HEGEMONIA LAUNCHER
echo    Serveur Geopolitique Minecraft
echo  ========================================
echo.

REM Verifier si Python est installe
python --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Python detecte
    echo.
    echo Lancement du launcher...
    python launcher_auth.py
    goto :end
)

REM Essayer python3
python3 --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Python3 detecte
    echo.
    echo Lancement du launcher...
    python3 launcher_auth.py
    goto :end
)

REM Essayer py
py --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Python (py) detecte
    echo.
    echo Lancement du launcher...
    py launcher_auth.py
    goto :end
)

echo.
echo [ERREUR] Python n'est pas installe sur votre ordinateur.
echo.
echo Pour installer Python:
echo   1. Allez sur https://www.python.org/downloads/
echo   2. Telechargez Python 3.11 ou superieur
echo   3. IMPORTANT: Cochez "Add Python to PATH" lors de l'installation
echo   4. Relancez ce fichier
echo.
echo Appuyez sur une touche pour ouvrir le site de telechargement Python...
pause >nul
start https://www.python.org/downloads/

:end
echo.
pause
