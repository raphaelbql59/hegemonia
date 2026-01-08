use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use std::fs;
use tauri::Window;

const MINECRAFT_VERSION: &str = "1.20.4";
const FABRIC_VERSION: &str = "0.15.6";
const HEGEMONIA_CDN: &str = "https://cdn.hegemonia.net";

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct DownloadProgress {
    pub file_name: String,
    pub current: u64,
    pub total: u64,
    pub percentage: f32,
    pub status: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ModInfo {
    pub id: String,
    pub name: String,
    pub version: String,
    pub file_name: String,
    pub sha256: String,
    pub size: u64,
    pub required: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct HegemoniaPack {
    pub version: String,
    pub minecraft_version: String,
    pub fabric_version: String,
    pub mods: Vec<ModInfo>,
    pub resource_pack: Option<ResourcePackInfo>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ResourcePackInfo {
    pub name: String,
    pub version: String,
    pub file_name: String,
    pub sha256: String,
    pub size: u64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct InstallationStatus {
    pub minecraft_installed: bool,
    pub fabric_installed: bool,
    pub mods_installed: bool,
    pub mods_outdated: Vec<String>,
    pub resource_pack_installed: bool,
    pub needs_update: bool,
}

/// Get the Hegemonia installation directory
pub fn get_hegemonia_dir() -> PathBuf {
    let home = std::env::var("HOME")
        .or_else(|_| std::env::var("USERPROFILE"))
        .unwrap_or_else(|_| ".".to_string());

    let path = PathBuf::from(&home);

    #[cfg(target_os = "windows")]
    {
        path.join("AppData").join("Roaming").join(".hegemonia")
    }

    #[cfg(target_os = "macos")]
    {
        path.join("Library").join("Application Support").join("hegemonia")
    }

    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    {
        path.join(".hegemonia")
    }
}

/// Get the Minecraft directory
pub fn get_minecraft_dir() -> PathBuf {
    let home = std::env::var("HOME")
        .or_else(|_| std::env::var("USERPROFILE"))
        .unwrap_or_else(|_| ".".to_string());

    let path = PathBuf::from(&home);

    #[cfg(target_os = "windows")]
    {
        path.join("AppData").join("Roaming").join(".minecraft")
    }

    #[cfg(target_os = "macos")]
    {
        path.join("Library").join("Application Support").join("minecraft")
    }

    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    {
        path.join(".minecraft")
    }
}

/// Check installation status
#[tauri::command]
pub async fn check_installation_status() -> Result<InstallationStatus, String> {
    let hegemonia_dir = get_hegemonia_dir();
    let minecraft_dir = get_minecraft_dir();

    let minecraft_installed = minecraft_dir.exists();
    let fabric_installed = minecraft_dir
        .join("versions")
        .join(format!("fabric-loader-{}-{}", FABRIC_VERSION, MINECRAFT_VERSION))
        .exists();

    let mods_dir = hegemonia_dir.join("mods");
    let mods_installed = mods_dir.exists() && mods_dir.read_dir().map(|d| d.count() > 0).unwrap_or(false);

    let resource_pack_installed = hegemonia_dir.join("resourcepacks").join("hegemonia.zip").exists();

    // Check for outdated mods
    let mods_outdated = Vec::new(); // TODO: implement version checking

    let needs_update = !mods_installed || !resource_pack_installed || !mods_outdated.is_empty();

    Ok(InstallationStatus {
        minecraft_installed,
        fabric_installed,
        mods_installed,
        mods_outdated,
        resource_pack_installed,
        needs_update,
    })
}

/// Fetch the Hegemonia modpack manifest
#[tauri::command]
pub async fn fetch_modpack_manifest() -> Result<HegemoniaPack, String> {
    // For now, return a hardcoded manifest
    // In production, this would fetch from CDN
    Ok(HegemoniaPack {
        version: "1.0.0".to_string(),
        minecraft_version: MINECRAFT_VERSION.to_string(),
        fabric_version: FABRIC_VERSION.to_string(),
        mods: vec![
            ModInfo {
                id: "fabric-api".to_string(),
                name: "Fabric API".to_string(),
                version: "0.92.0".to_string(),
                file_name: "fabric-api-0.92.0+1.20.4.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 2000000,
                required: true,
            },
            ModInfo {
                id: "sodium".to_string(),
                name: "Sodium".to_string(),
                version: "0.5.8".to_string(),
                file_name: "sodium-fabric-0.5.8+mc1.20.4.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 1500000,
                required: true,
            },
            ModInfo {
                id: "lithium".to_string(),
                name: "Lithium".to_string(),
                version: "0.12.1".to_string(),
                file_name: "lithium-fabric-mc1.20.4-0.12.1.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 500000,
                required: true,
            },
            ModInfo {
                id: "iris".to_string(),
                name: "Iris Shaders".to_string(),
                version: "1.6.17".to_string(),
                file_name: "iris-mc1.20.4-1.6.17.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 2500000,
                required: false,
            },
            ModInfo {
                id: "modmenu".to_string(),
                name: "Mod Menu".to_string(),
                version: "9.0.0".to_string(),
                file_name: "modmenu-9.0.0.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 300000,
                required: true,
            },
            ModInfo {
                id: "worldmap".to_string(),
                name: "Xaero's World Map".to_string(),
                version: "1.37.8".to_string(),
                file_name: "XaerosWorldMap_1.37.8_Fabric_1.20.4.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 1000000,
                required: true,
            },
            ModInfo {
                id: "minimap".to_string(),
                name: "Xaero's Minimap".to_string(),
                version: "24.0.3".to_string(),
                file_name: "Xaeros_Minimap_24.0.3_Fabric_1.20.4.jar".to_string(),
                sha256: "placeholder".to_string(),
                size: 800000,
                required: true,
            },
        ],
        resource_pack: Some(ResourcePackInfo {
            name: "Hegemonia Resource Pack".to_string(),
            version: "1.0.0".to_string(),
            file_name: "hegemonia.zip".to_string(),
            sha256: "placeholder".to_string(),
            size: 5000000,
        }),
    })
}

/// Download a file with progress reporting
pub async fn download_file_with_progress(
    window: &Window,
    url: &str,
    destination: &PathBuf,
    file_name: &str,
) -> Result<(), String> {
    use std::io::Write;

    // Create parent directory if needed
    if let Some(parent) = destination.parent() {
        fs::create_dir_all(parent)
            .map_err(|e| format!("Failed to create directory: {}", e))?;
    }

    let client = reqwest::Client::new();
    let response = client.get(url)
        .send()
        .await
        .map_err(|e| format!("Download failed: {}", e))?;

    let total_size = response.content_length().unwrap_or(0);
    let mut downloaded: u64 = 0;

    let mut file = fs::File::create(destination)
        .map_err(|e| format!("Failed to create file: {}", e))?;

    let mut stream = response.bytes_stream();
    use futures_util::StreamExt;

    while let Some(chunk) = stream.next().await {
        let chunk = chunk.map_err(|e| format!("Error reading chunk: {}", e))?;
        file.write_all(&chunk)
            .map_err(|e| format!("Error writing chunk: {}", e))?;

        downloaded += chunk.len() as u64;

        let progress = DownloadProgress {
            file_name: file_name.to_string(),
            current: downloaded,
            total: total_size,
            percentage: if total_size > 0 { (downloaded as f32 / total_size as f32) * 100.0 } else { 0.0 },
            status: "downloading".to_string(),
        };

        window.emit("download-progress", &progress).ok();
    }

    Ok(())
}

/// Install the Hegemonia modpack
#[tauri::command]
pub async fn install_modpack(window: Window) -> Result<String, String> {
    let hegemonia_dir = get_hegemonia_dir();
    let mods_dir = hegemonia_dir.join("mods");
    let resourcepacks_dir = hegemonia_dir.join("resourcepacks");

    // Create directories
    fs::create_dir_all(&mods_dir)
        .map_err(|e| format!("Failed to create mods directory: {}", e))?;
    fs::create_dir_all(&resourcepacks_dir)
        .map_err(|e| format!("Failed to create resourcepacks directory: {}", e))?;

    // Emit starting status
    window.emit("install-status", "Fetching modpack manifest...").ok();

    let manifest = fetch_modpack_manifest().await?;

    // Download mods
    for (i, mod_info) in manifest.mods.iter().enumerate() {
        let status = format!("Downloading {} ({}/{})", mod_info.name, i + 1, manifest.mods.len());
        window.emit("install-status", &status).ok();

        let mod_url = format!("{}/mods/{}", HEGEMONIA_CDN, mod_info.file_name);
        let mod_path = mods_dir.join(&mod_info.file_name);

        // For now, skip download if file exists (in production, check hash)
        if !mod_path.exists() {
            match download_file_with_progress(&window, &mod_url, &mod_path, &mod_info.file_name).await {
                Ok(_) => {},
                Err(e) => {
                    // Log error but continue with other mods
                    window.emit("install-warning", &format!("Failed to download {}: {}", mod_info.name, e)).ok();
                }
            }
        }
    }

    // Download resource pack
    if let Some(resource_pack) = &manifest.resource_pack {
        window.emit("install-status", "Downloading resource pack...").ok();

        let rp_url = format!("{}/resourcepacks/{}", HEGEMONIA_CDN, resource_pack.file_name);
        let rp_path = resourcepacks_dir.join(&resource_pack.file_name);

        if !rp_path.exists() {
            match download_file_with_progress(&window, &rp_url, &rp_path, &resource_pack.file_name).await {
                Ok(_) => {},
                Err(e) => {
                    window.emit("install-warning", &format!("Failed to download resource pack: {}", e)).ok();
                }
            }
        }
    }

    // Create version info file
    let version_file = hegemonia_dir.join("version.json");
    let version_info = serde_json::json!({
        "pack_version": manifest.version,
        "minecraft_version": manifest.minecraft_version,
        "fabric_version": manifest.fabric_version,
        "installed_at": chrono::Utc::now().to_rfc3339(),
    });
    fs::write(&version_file, serde_json::to_string_pretty(&version_info).unwrap())
        .map_err(|e| format!("Failed to write version file: {}", e))?;

    window.emit("install-status", "Installation complete!").ok();

    Ok("Modpack installed successfully".to_string())
}

/// Create Minecraft profile for Hegemonia
#[tauri::command]
pub async fn create_minecraft_profile() -> Result<String, String> {
    let minecraft_dir = get_minecraft_dir();
    let profiles_file = minecraft_dir.join("launcher_profiles.json");

    if !profiles_file.exists() {
        return Err("Minecraft launcher profiles not found. Please launch Minecraft at least once.".to_string());
    }

    let content = fs::read_to_string(&profiles_file)
        .map_err(|e| format!("Failed to read profiles: {}", e))?;

    let mut profiles: serde_json::Value = serde_json::from_str(&content)
        .map_err(|e| format!("Failed to parse profiles: {}", e))?;

    let hegemonia_dir = get_hegemonia_dir();

    // Create Hegemonia profile
    let hegemonia_profile = serde_json::json!({
        "created": chrono::Utc::now().to_rfc3339(),
        "gameDir": hegemonia_dir.to_str().unwrap(),
        "icon": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
        "javaArgs": "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M",
        "lastVersionId": format!("fabric-loader-{}-{}", FABRIC_VERSION, MINECRAFT_VERSION),
        "name": "Hegemonia",
        "type": "custom"
    });

    if let Some(profiles_obj) = profiles.get_mut("profiles") {
        if let Some(obj) = profiles_obj.as_object_mut() {
            obj.insert("hegemonia".to_string(), hegemonia_profile);
        }
    }

    fs::write(&profiles_file, serde_json::to_string_pretty(&profiles).unwrap())
        .map_err(|e| format!("Failed to write profiles: {}", e))?;

    Ok("Hegemonia profile created successfully".to_string())
}
