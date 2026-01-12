#!/usr/bin/env python3
"""
Hegemonia Auto-Updater
Downloads the latest Hegemonia client mod before launching Minecraft
"""

import os
import sys
import json
import hashlib
import platform
import subprocess
from pathlib import Path
from urllib.request import urlopen, Request
from urllib.error import URLError

# Configuration
API_URL = "http://51.75.31.173:3001/api"
MOD_NAME = "hegemonia-client"
CURRENT_VERSION_FILE = ".hegemonia-version"

def get_minecraft_dir():
    """Get the Minecraft directory based on OS"""
    home = Path.home()
    system = platform.system()

    if system == "Windows":
        return home / "AppData" / "Roaming" / ".minecraft"
    elif system == "Darwin":  # macOS
        return home / "Library" / "Application Support" / "minecraft"
    else:  # Linux
        return home / ".minecraft"

def get_mods_dir():
    """Get the mods directory"""
    minecraft_dir = get_minecraft_dir()
    mods_dir = minecraft_dir / "mods"
    mods_dir.mkdir(parents=True, exist_ok=True)
    return mods_dir

def fetch_manifest():
    """Fetch the modpack manifest from API"""
    try:
        url = f"{API_URL}/modpack/manifest"
        req = Request(url, headers={'User-Agent': 'Hegemonia-Updater/1.0'})
        with urlopen(req, timeout=10) as response:
            return json.loads(response.read().decode())
    except Exception as e:
        print(f"[!] Impossible de contacter l'API: {e}")
        return None

def get_local_version(mods_dir):
    """Get the currently installed version"""
    version_file = mods_dir / CURRENT_VERSION_FILE
    if version_file.exists():
        try:
            with open(version_file, 'r') as f:
                return json.load(f)
        except:
            pass
    return {"version": "0.0.0", "hash": ""}

def save_local_version(mods_dir, version, file_hash):
    """Save the installed version info"""
    version_file = mods_dir / CURRENT_VERSION_FILE
    with open(version_file, 'w') as f:
        json.dump({"version": version, "hash": file_hash}, f)

def download_mod(url, destination):
    """Download a mod file with progress"""
    try:
        req = Request(url, headers={'User-Agent': 'Hegemonia-Updater/1.0'})
        with urlopen(req, timeout=60) as response:
            total_size = int(response.headers.get('content-length', 0))
            downloaded = 0
            chunk_size = 8192

            with open(destination, 'wb') as f:
                while True:
                    chunk = response.read(chunk_size)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)

                    if total_size > 0:
                        percent = (downloaded / total_size) * 100
                        bar_len = 30
                        filled = int(bar_len * downloaded / total_size)
                        bar = '=' * filled + '-' * (bar_len - filled)
                        print(f"\r    [{bar}] {percent:.1f}%", end='', flush=True)

            print()  # New line after progress
            return True
    except Exception as e:
        print(f"\n[!] Erreur de telechargement: {e}")
        return False

def calculate_hash(filepath):
    """Calculate SHA256 hash of a file"""
    sha256 = hashlib.sha256()
    with open(filepath, 'rb') as f:
        for chunk in iter(lambda: f.read(8192), b''):
            sha256.update(chunk)
    return sha256.hexdigest()

def remove_old_versions(mods_dir, current_file):
    """Remove old versions of the Hegemonia client mod"""
    for file in mods_dir.glob("hegemonia-client*.jar"):
        if file.name != current_file:
            try:
                file.unlink()
                print(f"    Ancienne version supprimee: {file.name}")
            except Exception as e:
                print(f"    Impossible de supprimer {file.name}: {e}")

def update_mod():
    """Main update logic"""
    print()
    print("╔══════════════════════════════════════════════════╗")
    print("║           HEGEMONIA - Auto-Updater               ║")
    print("╚══════════════════════════════════════════════════╝")
    print()

    # Get directories
    mods_dir = get_mods_dir()
    print(f"[*] Dossier mods: {mods_dir}")

    # Fetch manifest
    print("[*] Verification des mises a jour...")
    manifest = fetch_manifest()
    if not manifest:
        print("[!] Impossible de verifier les mises a jour, lancement avec la version actuelle")
        return True

    # Find Hegemonia client mod in manifest
    hegemonia_mod = None
    for mod in manifest.get('mods', []):
        if mod['id'] == MOD_NAME:
            hegemonia_mod = mod
            break

    if not hegemonia_mod:
        print("[!] Mod Hegemonia non trouve dans le manifest")
        return False

    remote_version = hegemonia_mod['version']
    mod_filename = hegemonia_mod['file_name']
    mod_size = hegemonia_mod['size']

    # Check local version
    local_info = get_local_version(mods_dir)
    mod_path = mods_dir / mod_filename

    needs_update = False

    if not mod_path.exists():
        print(f"[*] Mod non installe, telechargement v{remote_version}...")
        needs_update = True
    elif local_info['version'] != remote_version:
        print(f"[*] Mise a jour disponible: v{local_info['version']} -> v{remote_version}")
        needs_update = True
    else:
        # Verify file integrity
        if mod_path.stat().st_size != mod_size:
            print("[*] Fichier corrompu, re-telechargement...")
            needs_update = True
        else:
            print(f"[OK] Hegemonia v{remote_version} est a jour!")

    if needs_update:
        # Download URL
        mod_url = hegemonia_mod.get('url') or f"{API_URL}/modpack/mods/{mod_filename}"

        print(f"[*] Telechargement de {mod_filename}...")
        temp_path = mods_dir / f"{mod_filename}.tmp"

        if download_mod(mod_url, temp_path):
            # Verify download
            if temp_path.stat().st_size == mod_size:
                # Remove old versions
                remove_old_versions(mods_dir, mod_filename)

                # Move new version
                if mod_path.exists():
                    mod_path.unlink()
                temp_path.rename(mod_path)

                # Save version info
                file_hash = calculate_hash(mod_path)
                save_local_version(mods_dir, remote_version, file_hash)

                print(f"[OK] Hegemonia v{remote_version} installe avec succes!")
            else:
                print("[!] Telechargement incomplet, fichier supprime")
                temp_path.unlink()
                return False
        else:
            if temp_path.exists():
                temp_path.unlink()
            return False

    print()
    return True

def main():
    """Main entry point"""
    success = update_mod()

    # If arguments provided, launch Minecraft
    if len(sys.argv) > 1 and sys.argv[1] == "--launch":
        minecraft_dir = get_minecraft_dir()
        system = platform.system()

        print("[*] Lancement de Minecraft...")

        if system == "Windows":
            # Try to find and launch Minecraft launcher
            launcher_paths = [
                Path(os.environ.get('PROGRAMFILES', '')) / "Minecraft Launcher" / "MinecraftLauncher.exe",
                Path(os.environ.get('LOCALAPPDATA', '')) / "Programs" / "Minecraft Launcher" / "MinecraftLauncher.exe",
            ]
            for path in launcher_paths:
                if path.exists():
                    subprocess.Popen([str(path)])
                    break
        elif system == "Darwin":
            subprocess.Popen(["open", "-a", "Minecraft"])
        else:
            # Linux - try common launchers
            subprocess.Popen(["minecraft-launcher"])

    return 0 if success else 1

if __name__ == "__main__":
    sys.exit(main())
