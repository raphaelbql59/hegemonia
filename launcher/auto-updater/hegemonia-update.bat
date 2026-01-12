@echo off
title Hegemonia Auto-Updater
cd /d "%~dp0"

:: Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    py --version >nul 2>&1
    if errorlevel 1 (
        echo [!] Python n'est pas installe.
        echo     Telechargez-le sur https://python.org
        pause
        exit /b 1
    )
    py update-hegemonia.py --launch
) else (
    python update-hegemonia.py --launch
)

pause
