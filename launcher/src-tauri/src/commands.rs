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

/// Lance Minecraft avec les arguments fournis
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
        "Launch attempt at: {:?}\nUsername: {}\nUUID: {}\nServer: {}:{}\nMinecraft path: {:?}\n",
        std::time::SystemTime::now(),
        username,
        uuid,
        server_ip,
        server_port,
        minecraft_path
    );
    let _ = std::fs::write(&log_path, &log_content);

    if !minecraft_path.exists() {
        return Err("Minecraft n'est pas installé. Veuillez installer Minecraft Java Edition 1.20.4".to_string());
    }

    // Check if versions/1.20.4 exists
    let version_path = minecraft_path.join("versions").join("1.20.4");
    if !version_path.exists() {
        return Err("La version 1.20.4 de Minecraft n'est pas installée. Lancez le launcher Minecraft officiel pour installer la version 1.20.4".to_string());
    }

    // Build classpath with proper separator for Windows
    #[cfg(target_os = "windows")]
    let classpath_sep = ";";
    #[cfg(not(target_os = "windows"))]
    let classpath_sep = ":";

    let version_jar = version_path.join("1.20.4.jar");
    let libraries_path = minecraft_path.join("libraries");

    // Simplified classpath for now
    let classpath = format!(
        "{}{}{}{}*",
        version_jar.to_str().unwrap_or(""),
        classpath_sep,
        libraries_path.to_str().unwrap_or(""),
        std::path::MAIN_SEPARATOR
    );

    let natives_path = minecraft_path.join("versions").join("1.20.4").join("natives");
    let assets_path = minecraft_path.join("assets");
    let game_dir = minecraft_path.to_str().unwrap_or(".");

    // Arguments Java pour lancer Minecraft
    let java_args = vec![
        "-Xmx2G".to_string(),
        "-Xms1G".to_string(),
        format!("-Djava.library.path={}", natives_path.to_str().unwrap_or("natives")),
        "-cp".to_string(),
        classpath,
        "net.minecraft.client.main.Main".to_string(),
        "--username".to_string(), username.clone(),
        "--version".to_string(), "1.20.4".to_string(),
        "--gameDir".to_string(), game_dir.to_string(),
        "--assetsDir".to_string(), assets_path.to_str().unwrap_or("assets").to_string(),
        "--assetIndex".to_string(), "16".to_string(),
        "--uuid".to_string(), uuid.clone(),
        "--accessToken".to_string(), "0".to_string(),
        "--userType".to_string(), "legacy".to_string(),
        "--server".to_string(), server_ip.clone(),
        "--port".to_string(), server_port.clone(),
    ];

    // Log the command for debugging
    let cmd_log = format!("Command: java {}\n", java_args.join(" "));
    let _ = std::fs::write(&log_path, format!("{}{}", log_content, cmd_log));

    match Command::new("java")
        .args(&java_args)
        .current_dir(&minecraft_path)
        .spawn()
    {
        Ok(child) => {
            // Log success
            let _ = std::fs::write(&log_path, format!("{}{}Minecraft process spawned with PID: {:?}\n", log_content, cmd_log, child.id()));
            Ok(format!("Minecraft lancé avec succès ! (PID: {:?})", child.id()))
        },
        Err(e) => {
            let err_msg = format!("Erreur lors du lancement de Minecraft: {}", e);
            let _ = std::fs::write(&log_path, format!("{}{}ERROR: {}\n", log_content, cmd_log, err_msg));
            Err(err_msg)
        },
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
