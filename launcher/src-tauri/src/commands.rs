use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use std::process::Command;

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

/// Lance Minecraft avec le profil Hegemonia via le launcher officiel
#[tauri::command]
pub async fn launch_minecraft(
    username: String,
    uuid: String,
    server_ip: String,
    server_port: String,
) -> Result<String, String> {
    let minecraft_path = get_minecraft_directory();
    let hegemonia_path = get_hegemonia_directory();

    // Create .hegemonia directory if it doesn't exist
    if !hegemonia_path.exists() {
        if let Err(e) = std::fs::create_dir_all(&hegemonia_path) {
            return Err(format!("Impossible de créer le dossier .hegemonia: {}", e));
        }
    }

    // Save launch info for debugging
    let log_path = hegemonia_path.join("launch.log");
    let log_content = format!(
        "Launch attempt at: {:?}\nUsername: {}\nUUID: {}\nServer: {}:{}\nMinecraft path: {:?}\nHegemonia path: {:?}\n",
        std::time::SystemTime::now(),
        username,
        uuid,
        server_ip,
        server_port,
        minecraft_path,
        hegemonia_path
    );
    let _ = std::fs::write(&log_path, &log_content);

    if !minecraft_path.exists() {
        return Err("Minecraft n'est pas installé. Veuillez installer Minecraft Java Edition 1.20.4".to_string());
    }

    // Copy mods to .minecraft/mods for the game to load them
    let source_mods = hegemonia_path.join("mods");
    let target_mods = minecraft_path.join("mods");

    if source_mods.exists() {
        // Create mods directory in .minecraft
        if let Err(e) = std::fs::create_dir_all(&target_mods) {
            let _ = std::fs::write(&log_path, format!("{}Warning: Could not create mods dir: {}\n", log_content, e));
        }

        // Copy each mod file (ALWAYS overwrite to get updates)
        if let Ok(entries) = std::fs::read_dir(&source_mods) {
            for entry in entries.flatten() {
                let src = entry.path();
                if src.extension().map(|e| e == "jar").unwrap_or(false) {
                    let dest = target_mods.join(entry.file_name());
                    // Always copy/overwrite to ensure updates are applied
                    let _ = std::fs::copy(&src, &dest);
                }
            }
        }
    }

    // Create server.dat to auto-connect (optional)
    let servers_file = minecraft_path.join("servers.dat");
    if !servers_file.exists() {
        // This would require NBT writing, skip for now
    }

    // Launch Minecraft Launcher with Hegemonia profile
    #[cfg(target_os = "windows")]
    {
        // Try to find Minecraft Launcher
        let launcher_paths = vec![
            std::env::var("LOCALAPPDATA").unwrap_or_default() + "\\Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local\\runtime\\java-runtime-gamma\\windows-x64\\java-runtime-gamma\\bin\\javaw.exe",
            std::env::var("PROGRAMFILES(X86)").unwrap_or_default() + "\\Minecraft Launcher\\MinecraftLauncher.exe",
            std::env::var("PROGRAMFILES").unwrap_or_default() + "\\Minecraft Launcher\\MinecraftLauncher.exe",
        ];

        // Try to open Minecraft Launcher
        let launcher_exe = std::env::var("LOCALAPPDATA").unwrap_or_default() + "\\Programs\\Minecraft Launcher\\MinecraftLauncher.exe";

        let _ = std::fs::write(&log_path, format!("{}Trying to launch: {}\n", log_content, launcher_exe));

        // Use shell open to launch Minecraft Launcher
        match Command::new("cmd")
            .args(["/C", "start", "", "minecraft://"])
            .spawn()
        {
            Ok(_) => {
                let _ = std::fs::write(&log_path, format!("{}Minecraft Launcher opened via minecraft:// protocol\n", log_content));
                Ok("Le launcher Minecraft s'ouvre. Sélectionnez le profil 'Hegemonia' et cliquez sur Jouer !".to_string())
            }
            Err(e) => {
                // Fallback: try direct exe
                match Command::new(&launcher_exe).spawn() {
                    Ok(_) => {
                        let _ = std::fs::write(&log_path, format!("{}Minecraft Launcher opened directly\n", log_content));
                        Ok("Le launcher Minecraft s'ouvre. Sélectionnez le profil 'Hegemonia' et cliquez sur Jouer !".to_string())
                    }
                    Err(e2) => {
                        let err = format!("Impossible d'ouvrir le launcher Minecraft: {} / {}", e, e2);
                        let _ = std::fs::write(&log_path, format!("{}ERROR: {}\n", log_content, err));
                        Err(err)
                    }
                }
            }
        }
    }

    #[cfg(target_os = "macos")]
    {
        match Command::new("open")
            .args(["-a", "Minecraft"])
            .spawn()
        {
            Ok(_) => Ok("Le launcher Minecraft s'ouvre. Sélectionnez le profil 'Hegemonia' et cliquez sur Jouer !".to_string()),
            Err(e) => Err(format!("Impossible d'ouvrir Minecraft: {}", e)),
        }
    }

    #[cfg(target_os = "linux")]
    {
        match Command::new("minecraft-launcher").spawn() {
            Ok(_) => Ok("Le launcher Minecraft s'ouvre. Sélectionnez le profil 'Hegemonia' et cliquez sur Jouer !".to_string()),
            Err(e) => Err(format!("Impossible d'ouvrir le launcher Minecraft: {}", e)),
        }
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

/// Retourne le chemin du répertoire .hegemonia
fn get_hegemonia_directory() -> PathBuf {
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
        path.join("AppData").join("Roaming").join(".hegemonia")
    }

    #[cfg(target_os = "macos")]
    {
        path.join("Library")
            .join("Application Support")
            .join("hegemonia")
    }

    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    {
        path.join(".hegemonia")
    }
}

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
