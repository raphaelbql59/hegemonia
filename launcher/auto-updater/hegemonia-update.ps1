# Hegemonia Auto-Updater (PowerShell)
# Double-click or run: powershell -ExecutionPolicy Bypass -File hegemonia-update.ps1

$API_URL = "http://51.75.31.173:3001/api"
$MOD_NAME = "hegemonia-client"

function Get-MinecraftDir {
    $env:APPDATA + "\.minecraft"
}

function Get-ModsDir {
    $modsDir = (Get-MinecraftDir) + "\mods"
    if (-not (Test-Path $modsDir)) {
        New-Item -ItemType Directory -Path $modsDir -Force | Out-Null
    }
    return $modsDir
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "     HEGEMONIA - Auto-Updater" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

$modsDir = Get-ModsDir
Write-Host "[*] Dossier mods: $modsDir"

# Fetch manifest
Write-Host "[*] Verification des mises a jour..."
try {
    $manifest = Invoke-RestMethod -Uri "$API_URL/modpack/manifest" -TimeoutSec 10
} catch {
    Write-Host "[!] Impossible de contacter l'API: $_" -ForegroundColor Red
    Write-Host "[*] Lancement avec la version actuelle..." -ForegroundColor Yellow
    Read-Host "Appuyez sur Entree pour continuer"
    exit 0
}

# Find Hegemonia mod
$hegeMod = $manifest.mods | Where-Object { $_.id -eq $MOD_NAME }
if (-not $hegeMod) {
    Write-Host "[!] Mod Hegemonia non trouve" -ForegroundColor Red
    exit 1
}

$remoteVersion = $hegeMod.version
$modFilename = $hegeMod.file_name
$modSize = $hegeMod.size
$modPath = Join-Path $modsDir $modFilename

$needsUpdate = $false

if (-not (Test-Path $modPath)) {
    Write-Host "[*] Mod non installe, telechargement v$remoteVersion..." -ForegroundColor Cyan
    $needsUpdate = $true
} else {
    $localSize = (Get-Item $modPath).Length
    if ($localSize -ne $modSize) {
        Write-Host "[*] Mise a jour detectee, telechargement v$remoteVersion..." -ForegroundColor Cyan
        $needsUpdate = $true
    } else {
        Write-Host "[OK] Hegemonia v$remoteVersion est a jour!" -ForegroundColor Green
    }
}

if ($needsUpdate) {
    # Remove old versions
    Get-ChildItem -Path $modsDir -Filter "hegemonia-client*.jar" | ForEach-Object {
        Write-Host "    Suppression ancienne version: $($_.Name)" -ForegroundColor DarkGray
        Remove-Item $_.FullName -Force
    }

    # Download
    $modUrl = if ($hegeMod.url) { $hegeMod.url } else { "$API_URL/modpack/mods/$modFilename" }
    Write-Host "[*] Telechargement depuis $modUrl..."

    try {
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $modUrl -OutFile $modPath -TimeoutSec 60
        $ProgressPreference = 'Continue'

        $downloadedSize = (Get-Item $modPath).Length
        if ($downloadedSize -eq $modSize) {
            Write-Host "[OK] Hegemonia v$remoteVersion installe!" -ForegroundColor Green
        } else {
            Write-Host "[!] Telechargement incomplet ($downloadedSize / $modSize)" -ForegroundColor Red
            Remove-Item $modPath -Force
        }
    } catch {
        Write-Host "[!] Erreur de telechargement: $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[*] Pret a jouer! Lancez Minecraft avec Fabric." -ForegroundColor Green
Write-Host ""
Read-Host "Appuyez sur Entree pour fermer"
