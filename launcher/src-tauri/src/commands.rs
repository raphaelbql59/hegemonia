use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use std::process::Command;
use tauri::api::process::{Command as TauriCommand, CommandEvent};

#[derive(Debug, Serialize, Deserialize)]
pub struct SystemInfo {
    pub os: String,
    pub arch: String,
    pub java_version: Option<String>,
    pub ram_total: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MinecraftPath {
    pub path: String,
    pub exists: bool,
}

// ============================================================================
// Minecraft Commands
// ============================================================================

/// Lance Minecraft avec les arguments fournis
#[tauri::command]
pub async fn launch_minecraft(
    username: String,
    uuid: String,
    server_ip: String,
    server_port: String,
) -> Result<String, String> {
    let minecraft_path = get_minecraft_directory();

    if !minecraft_path.exists() {
        return Err("Minecraft n'est pas installé. Veuillez installer Minecraft Java Edition 1.20.4".to_string());
    }

    // Arguments Java pour lancer Minecraft
    let java_args = vec![
        "-Xmx2G",
        "-Xms1G",
        "-Djava.library.path=natives",
        "-cp",
        "libraries/*:versions/1.20.4/1.20.4.jar",
        "net.minecraft.client.main.Main",
        "--username", &username,
        "--version", "1.20.4",
        "--gameDir", minecraft_path.to_str().unwrap(),
        "--assetsDir", "assets",
        "--assetIndex", "1.20",
        "--uuid", &uuid,
        "--accessToken", "0",
        "--userType", "legacy",
        "--server", &server_ip,
        "--port", &server_port,
    ];

    match Command::new("java")
        .args(&java_args)
        .current_dir(&minecraft_path)
        .spawn()
    {
        Ok(_) => Ok("Minecraft lancé avec succès !".to_string()),
        Err(e) => Err(format!("Erreur lors du lancement de Minecraft: {}", e)),
    }
}

/// Vérifie si Minecraft est installé
#[tauri::command]
pub fn check_minecraft_installed() -> bool {
    let minecraft_path = get_minecraft_directory();
    minecraft_path.exists()
}

/// Retourne le chemin d'installation de Minecraft
#[tauri::command]
pub fn get_minecraft_path() -> MinecraftPath {
    let path = get_minecraft_directory();
    MinecraftPath {
        path: path.to_str().unwrap_or("").to_string(),
        exists: path.exists(),
    }
}

// ============================================================================
// Java Commands
// ============================================================================

/// Vérifie si Java est installé
#[tauri::command]
pub fn check_java_installed() -> bool {
    match Command::new("java").arg("-version").output() {
        Ok(_) => true,
        Err(_) => false,
    }
}

/// Retourne le chemin et la version de Java
#[tauri::command]
pub async fn get_java_path() -> Result<String, String> {
    match Command::new("java").arg("-version").output() {
        Ok(output) => {
            let version = String::from_utf8_lossy(&output.stderr);
            Ok(version.to_string())
        }
        Err(e) => Err(format!("Java n'est pas installé: {}", e)),
    }
}

// ============================================================================
// System Commands
// ============================================================================

/// Retourne les informations système
#[tauri::command]
pub fn get_system_info() -> SystemInfo {
    let os = std::env::consts::OS.to_string();
    let arch = std::env::consts::ARCH.to_string();

    let java_version = match Command::new("java").arg("-version").output() {
        Ok(output) => {
            let version = String::from_utf8_lossy(&output.stderr);
            Some(version.lines().next().unwrap_or("Unknown").to_string())
        }
        Err(_) => None,
    };

    SystemInfo {
        os,
        arch,
        java_version,
        ram_total: get_total_ram(),
    }
}

// ============================================================================
// Download Commands
// ============================================================================

/// Télécharge un fichier
#[tauri::command]
pub async fn download_file(
    url: String,
    destination: String,
) -> Result<String, String> {
    match reqwest::get(&url).await {
        Ok(response) => {
            match response.bytes().await {
                Ok(bytes) => {
                    match std::fs::write(&destination, &bytes) {
                        Ok(_) => Ok(format!("Fichier téléchargé vers: {}", destination)),
                        Err(e) => Err(format!("Erreur d'écriture: {}", e)),
                    }
                }
                Err(e) => Err(format!("Erreur de téléchargement: {}", e)),
            }
        }
        Err(e) => Err(format!("Erreur de requête: {}", e)),
    }
}

// ============================================================================
// Helper Functions
// ============================================================================

/// Retourne le chemin du répertoire .minecraft
fn get_minecraft_directory() -> PathBuf {
    let home = match std::env::var("HOME") {
        Ok(h) => h,
        Err(_) => match std::env::var("USERPROFILE") {
            Ok(h) => h,
            Err(_) => ".".to_string(),
        },
    };

    let path = PathBuf::from(&home);

    #[cfg(target_os = "windows")]
    {
        path.join("AppData").join("Roaming").join(".minecraft")
    }

    #[cfg(target_os = "macos")]
    {
        path.join("Library")
            .join("Application Support")
            .join("minecraft")
    }

    #[cfg(target_os = "linux")]
    {
        path.join(".minecraft")
    }
}

/// Retourne la RAM totale du système (en MB)
fn get_total_ram() -> u64 {
    // Simplified version - in production you'd use sysinfo crate
    4096 // Default 4GB
}
