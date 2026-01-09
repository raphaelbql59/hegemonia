use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fs;
use std::path::PathBuf;
use std::process::Command;
use tauri::Window;

const MINECRAFT_VERSION: &str = "1.20.4";
const FABRIC_LOADER_VERSION: &str = "0.16.9";
const VERSION_MANIFEST_URL: &str = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
const FABRIC_META_URL: &str = "https://meta.fabricmc.net/v2";

// ============================================================================
// Mojang API Structures
// ============================================================================

#[derive(Debug, Deserialize)]
struct VersionManifest {
    versions: Vec<VersionInfo>,
}

#[derive(Debug, Deserialize)]
struct VersionInfo {
    id: String,
    url: String,
}

#[derive(Debug, Deserialize)]
struct VersionMeta {
    id: String,
    #[serde(rename = "assetIndex")]
    asset_index: AssetIndex,
    assets: String,
    downloads: Downloads,
    libraries: Vec<Library>,
    #[serde(rename = "mainClass")]
    main_class: String,
    #[serde(rename = "minecraftArguments", default)]
    minecraft_arguments: Option<String>,
    arguments: Option<Arguments>,
}

#[derive(Debug, Deserialize)]
struct AssetIndex {
    id: String,
    url: String,
    sha1: String,
    size: u64,
    #[serde(rename = "totalSize")]
    total_size: u64,
}

#[derive(Debug, Deserialize)]
struct Downloads {
    client: DownloadInfo,
}

#[derive(Debug, Deserialize)]
struct DownloadInfo {
    sha1: String,
    size: u64,
    url: String,
}

#[derive(Debug, Deserialize)]
struct Library {
    name: String,
    downloads: Option<LibraryDownloads>,
    rules: Option<Vec<Rule>>,
    natives: Option<HashMap<String, String>>,
}

#[derive(Debug, Deserialize)]
struct LibraryDownloads {
    artifact: Option<Artifact>,
    classifiers: Option<HashMap<String, Artifact>>,
}

#[derive(Debug, Deserialize)]
struct Artifact {
    path: String,
    sha1: String,
    size: u64,
    url: String,
}

#[derive(Debug, Deserialize)]
struct Rule {
    action: String,
    os: Option<OsRule>,
}

#[derive(Debug, Deserialize)]
struct OsRule {
    name: Option<String>,
}

#[derive(Debug, Deserialize)]
struct Arguments {
    game: Vec<serde_json::Value>,
    jvm: Vec<serde_json::Value>,
}

#[derive(Debug, Deserialize)]
struct AssetIndexData {
    objects: HashMap<String, AssetObject>,
}

#[derive(Debug, Deserialize)]
struct AssetObject {
    hash: String,
    size: u64,
}

// ============================================================================
// Fabric API Structures
// ============================================================================

#[derive(Debug, Deserialize)]
struct FabricProfile {
    id: String,
    #[serde(rename = "mainClass")]
    main_class: String,
    libraries: Vec<FabricLibrary>,
}

#[derive(Debug, Deserialize)]
struct FabricLibrary {
    name: String,
    url: Option<String>,
}

// ============================================================================
// Progress tracking
// ============================================================================

#[derive(Debug, Serialize, Clone)]
pub struct LaunchProgress {
    pub stage: String,
    pub message: String,
    pub current: u64,
    pub total: u64,
}

fn emit_progress(window: &Window, stage: &str, message: &str, current: u64, total: u64) {
    let progress = LaunchProgress {
        stage: stage.to_string(),
        message: message.to_string(),
        current,
        total,
    };
    window.emit("launch-progress", &progress).ok();
    window.emit("install-status", message).ok();
}

// ============================================================================
// Directory helpers
// ============================================================================

pub fn get_game_dir() -> PathBuf {
    let home = std::env::var("HOME")
        .or_else(|_| std::env::var("USERPROFILE"))
        .unwrap_or_else(|_| ".".to_string());

    let path = PathBuf::from(&home);

    #[cfg(target_os = "windows")]
    { path.join("AppData").join("Roaming").join(".hegemonia") }

    #[cfg(target_os = "macos")]
    { path.join("Library").join("Application Support").join("hegemonia") }

    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    { path.join(".hegemonia") }
}

fn get_natives_dir() -> PathBuf {
    get_game_dir().join("natives")
}

fn get_libraries_dir() -> PathBuf {
    get_game_dir().join("libraries")
}

fn get_assets_dir() -> PathBuf {
    get_game_dir().join("assets")
}

fn get_versions_dir() -> PathBuf {
    get_game_dir().join("versions")
}

// ============================================================================
// Download helpers
// ============================================================================

async fn download_file(url: &str, path: &PathBuf) -> Result<(), String> {
    if path.exists() {
        return Ok(());
    }

    if let Some(parent) = path.parent() {
        fs::create_dir_all(parent).map_err(|e| format!("Failed to create directory: {}", e))?;
    }

    let client = reqwest::Client::new();
    let response = client.get(url)
        .send()
        .await
        .map_err(|e| format!("Download failed: {}", e))?;

    if !response.status().is_success() {
        return Err(format!("HTTP error: {}", response.status()));
    }

    let bytes = response.bytes()
        .await
        .map_err(|e| format!("Failed to read response: {}", e))?;

    fs::write(path, &bytes).map_err(|e| format!("Failed to write file: {}", e))?;

    Ok(())
}

// ============================================================================
// OS detection
// ============================================================================

fn get_os_name() -> &'static str {
    #[cfg(target_os = "windows")]
    { "windows" }
    #[cfg(target_os = "macos")]
    { "osx" }
    #[cfg(target_os = "linux")]
    { "linux" }
}

fn get_arch() -> &'static str {
    if cfg!(target_arch = "x86_64") {
        "64"
    } else {
        "32"
    }
}

fn get_classpath_separator() -> &'static str {
    #[cfg(target_os = "windows")]
    { ";" }
    #[cfg(not(target_os = "windows"))]
    { ":" }
}

// ============================================================================
// Library path from Maven name
// ============================================================================

fn maven_to_path(name: &str) -> String {
    let parts: Vec<&str> = name.split(':').collect();
    if parts.len() < 3 {
        return name.to_string();
    }

    let group = parts[0].replace('.', "/");
    let artifact = parts[1];
    let version = parts[2];
    let classifier = if parts.len() > 3 { Some(parts[3]) } else { None };

    if let Some(cls) = classifier {
        format!("{}/{}/{}/{}-{}-{}.jar", group, artifact, version, artifact, version, cls)
    } else {
        format!("{}/{}/{}/{}-{}.jar", group, artifact, version, artifact, version)
    }
}

// ============================================================================
// Check if library applies to current OS
// ============================================================================

fn library_applies(rules: &Option<Vec<Rule>>) -> bool {
    match rules {
        None => true,
        Some(rules) => {
            let os_name = get_os_name();
            let mut dominated = false;
            let mut dominated_result = false;

            for rule in rules {
                if rule.os.is_none() {
                    dominated = true;
                    dominated_result = rule.action == "allow";
                } else if let Some(os) = &rule.os {
                    if os.name.as_deref() == Some(os_name) {
                        return rule.action == "allow";
                    }
                }
            }

            if dominated { dominated_result } else { true }
        }
    }
}

// ============================================================================
// Main launch function
// ============================================================================

#[tauri::command]
pub async fn launch_game(
    window: Window,
    username: String,
    uuid: String,
    access_token: String,
    server_ip: String,
    server_port: String,
    ram_mb: u32,
) -> Result<String, String> {
    let game_dir = get_game_dir();
    let libraries_dir = get_libraries_dir();
    let natives_dir = get_natives_dir();
    let assets_dir = get_assets_dir();
    let versions_dir = get_versions_dir();

    // Create directories
    fs::create_dir_all(&game_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&libraries_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&natives_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&assets_dir).map_err(|e| e.to_string())?;
    fs::create_dir_all(&versions_dir).map_err(|e| e.to_string())?;

    // ========================================================================
    // Step 1: Download version manifest
    // ========================================================================
    emit_progress(&window, "manifest", "Récupération des informations Minecraft...", 0, 100);

    let client = reqwest::Client::new();

    let manifest: VersionManifest = client.get(VERSION_MANIFEST_URL)
        .send()
        .await
        .map_err(|e| format!("Failed to fetch manifest: {}", e))?
        .json()
        .await
        .map_err(|e| format!("Failed to parse manifest: {}", e))?;

    let version_info = manifest.versions.iter()
        .find(|v| v.id == MINECRAFT_VERSION)
        .ok_or_else(|| format!("Version {} not found", MINECRAFT_VERSION))?;

    // ========================================================================
    // Step 2: Download version metadata
    // ========================================================================
    emit_progress(&window, "metadata", "Téléchargement des métadonnées...", 5, 100);

    let version_meta: VersionMeta = client.get(&version_info.url)
        .send()
        .await
        .map_err(|e| format!("Failed to fetch version meta: {}", e))?
        .json()
        .await
        .map_err(|e| format!("Failed to parse version meta: {}", e))?;

    // ========================================================================
    // Step 3: Download client JAR
    // ========================================================================
    emit_progress(&window, "client", "Téléchargement du client Minecraft...", 10, 100);

    let client_jar = versions_dir.join(format!("{}.jar", MINECRAFT_VERSION));
    download_file(&version_meta.downloads.client.url, &client_jar).await?;

    // ========================================================================
    // Step 4: Download libraries
    // ========================================================================
    let total_libs = version_meta.libraries.len();
    let mut classpath_entries: Vec<String> = Vec::new();

    for (i, lib) in version_meta.libraries.iter().enumerate() {
        if !library_applies(&lib.rules) {
            continue;
        }

        let progress = 15 + (i * 30 / total_libs) as u64;
        emit_progress(&window, "libraries", &format!("Téléchargement bibliothèque {}/{}", i + 1, total_libs), progress, 100);

        if let Some(downloads) = &lib.downloads {
            // Download main artifact
            if let Some(artifact) = &downloads.artifact {
                let lib_path = libraries_dir.join(&artifact.path);
                download_file(&artifact.url, &lib_path).await?;
                classpath_entries.push(lib_path.to_string_lossy().to_string());
            }

            // Download natives if needed
            if let Some(natives) = &lib.natives {
                let os_name = get_os_name();
                if let Some(native_key) = natives.get(os_name) {
                    let native_key = native_key.replace("${arch}", get_arch());
                    if let Some(classifiers) = &downloads.classifiers {
                        if let Some(native_artifact) = classifiers.get(&native_key) {
                            let native_path = libraries_dir.join(&native_artifact.path);
                            download_file(&native_artifact.url, &native_path).await?;

                            // Extract natives
                            if let Ok(file) = fs::File::open(&native_path) {
                                if let Ok(mut archive) = zip::ZipArchive::new(file) {
                                    for i in 0..archive.len() {
                                        if let Ok(mut entry) = archive.by_index(i) {
                                            let name = entry.name().to_string();
                                            if name.ends_with(".dll") || name.ends_with(".so") || name.ends_with(".dylib") {
                                                let out_path = natives_dir.join(entry.name());
                                                if let Some(parent) = out_path.parent() {
                                                    let _ = fs::create_dir_all(parent);
                                                }
                                                if let Ok(mut out_file) = fs::File::create(&out_path) {
                                                    let _ = std::io::copy(&mut entry, &mut out_file);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ========================================================================
    // Step 5: Download assets
    // ========================================================================
    emit_progress(&window, "assets", "Téléchargement des assets...", 45, 100);

    let asset_index_path = assets_dir.join("indexes").join(format!("{}.json", version_meta.asset_index.id));
    download_file(&version_meta.asset_index.url, &asset_index_path).await?;

    let asset_index: AssetIndexData = serde_json::from_str(
        &fs::read_to_string(&asset_index_path).map_err(|e| e.to_string())?
    ).map_err(|e| format!("Failed to parse asset index: {}", e))?;

    let total_assets = asset_index.objects.len();
    let mut downloaded_assets = 0;

    for (name, obj) in &asset_index.objects {
        let hash_prefix = &obj.hash[0..2];
        let asset_path = assets_dir.join("objects").join(hash_prefix).join(&obj.hash);
        let asset_url = format!("https://resources.download.minecraft.net/{}/{}", hash_prefix, obj.hash);

        if !asset_path.exists() {
            download_file(&asset_url, &asset_path).await?;
        }

        downloaded_assets += 1;
        if downloaded_assets % 50 == 0 {
            let progress = 45 + (downloaded_assets * 20 / total_assets) as u64;
            emit_progress(&window, "assets", &format!("Assets {}/{}", downloaded_assets, total_assets), progress, 100);
        }
    }

    // ========================================================================
    // Step 6: Download Fabric
    // ========================================================================
    emit_progress(&window, "fabric", "Téléchargement de Fabric Loader...", 70, 100);

    let fabric_url = format!(
        "{}/versions/loader/{}/{}/profile/json",
        FABRIC_META_URL, MINECRAFT_VERSION, FABRIC_LOADER_VERSION
    );

    let fabric_profile: FabricProfile = client.get(&fabric_url)
        .send()
        .await
        .map_err(|e| format!("Failed to fetch Fabric profile: {}", e))?
        .json()
        .await
        .map_err(|e| format!("Failed to parse Fabric profile: {}", e))?;

    // Download Fabric libraries
    let total_fabric_libs = fabric_profile.libraries.len();
    for (i, lib) in fabric_profile.libraries.iter().enumerate() {
        let progress = 70 + (i * 15 / total_fabric_libs.max(1)) as u64;
        emit_progress(&window, "fabric", &format!("Fabric lib {}/{}", i + 1, total_fabric_libs), progress, 100);

        let path = maven_to_path(&lib.name);
        let lib_path = libraries_dir.join(&path);

        let base_url = lib.url.as_deref().unwrap_or("https://maven.fabricmc.net/");
        let url = format!("{}{}", base_url, path);

        download_file(&url, &lib_path).await?;
        classpath_entries.push(lib_path.to_string_lossy().to_string());
    }

    // Add client jar to classpath
    classpath_entries.push(client_jar.to_string_lossy().to_string());

    // Store Fabric main class for later
    let fabric_main_class = fabric_profile.main_class.clone();

    // ========================================================================
    // Step 7: Download mods
    // ========================================================================
    emit_progress(&window, "mods", "Vérification des mods...", 85, 100);

    let mods_dir = game_dir.join("mods");
    fs::create_dir_all(&mods_dir).map_err(|e| e.to_string())?;

    // Mods are already downloaded by install_modpack, just verify
    let mod_count = fs::read_dir(&mods_dir)
        .map(|d| d.filter(|e| e.as_ref().map(|e| e.path().extension().map(|ext| ext == "jar").unwrap_or(false)).unwrap_or(false)).count())
        .unwrap_or(0);

    emit_progress(&window, "mods", &format!("{} mods installés", mod_count), 90, 100);

    // ========================================================================
    // Step 8: Build and execute launch command
    // ========================================================================
    emit_progress(&window, "launch", "Lancement du jeu...", 95, 100);

    let sep = get_classpath_separator();
    let classpath = classpath_entries.join(sep);

    let ram_arg = format!("-Xmx{}M", ram_mb);
    let natives_arg = format!("-Djava.library.path={}", natives_dir.to_string_lossy());

    let mut args: Vec<String> = vec![
        ram_arg,
        "-Xms512M".to_string(),
        natives_arg,
        "-cp".to_string(),
        classpath,
        fabric_main_class,
        "--username".to_string(), username.clone(),
        "--version".to_string(), format!("fabric-loader-{}-{}", FABRIC_LOADER_VERSION, MINECRAFT_VERSION),
        "--gameDir".to_string(), game_dir.to_string_lossy().to_string(),
        "--assetsDir".to_string(), assets_dir.to_string_lossy().to_string(),
        "--assetIndex".to_string(), version_meta.asset_index.id.clone(),
        "--uuid".to_string(), uuid.clone(),
        "--accessToken".to_string(), access_token.clone(),
        "--userType".to_string(), "legacy".to_string(),
        "--versionType".to_string(), "release".to_string(),
    ];

    // Add server connection
    if !server_ip.is_empty() {
        args.push("--server".to_string());
        args.push(server_ip);
        if !server_port.is_empty() {
            args.push("--port".to_string());
            args.push(server_port);
        }
    }

    // Log command for debugging
    let log_path = game_dir.join("launch.log");
    let log_content = format!(
        "Launch at: {:?}\nUsername: {}\nUUID: {}\nJava args: {:?}\n",
        std::time::SystemTime::now(),
        username,
        uuid,
        args
    );
    let _ = fs::write(&log_path, &log_content);

    // Find Java
    let java_cmd = find_java()?;

    // Log the full command for debugging
    let full_cmd = format!("\"{}\" {}", java_cmd, args.iter().map(|a| {
        if a.contains(' ') || a.contains(';') { format!("\"{}\"", a) } else { a.clone() }
    }).collect::<Vec<_>>().join(" "));
    let _ = fs::write(&log_path, format!("{}Full command:\n{}\n\n", log_content, full_cmd));

    // Launch with stderr capture to detect early crashes
    use std::process::Stdio;

    match Command::new(&java_cmd)
        .args(&args)
        .current_dir(&game_dir)
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
    {
        Ok(mut child) => {
            let pid = child.id();
            let _ = fs::write(&log_path, format!("{}Full command:\n{}\n\nPID: {}\n", log_content, full_cmd, pid));

            // Wait a short time to check if process crashes immediately
            std::thread::sleep(std::time::Duration::from_millis(2000));

            // Check if process is still running
            match child.try_wait() {
                Ok(Some(status)) => {
                    // Process exited - capture output
                    let mut error_log = format!("{}Full command:\n{}\n\nPID: {}\nProcess exited with: {}\n",
                        log_content, full_cmd, pid, status);

                    if let Some(mut stderr) = child.stderr.take() {
                        use std::io::Read;
                        let mut err_output = String::new();
                        let _ = stderr.read_to_string(&mut err_output);
                        if !err_output.is_empty() {
                            error_log.push_str(&format!("\nSTDERR:\n{}\n", err_output));
                        }
                    }

                    if let Some(mut stdout) = child.stdout.take() {
                        use std::io::Read;
                        let mut out_output = String::new();
                        let _ = stdout.read_to_string(&mut out_output);
                        if !out_output.is_empty() {
                            error_log.push_str(&format!("\nSTDOUT:\n{}\n", out_output));
                        }
                    }

                    let _ = fs::write(&log_path, &error_log);

                    emit_progress(&window, "error", "Le jeu a crashé au démarrage", 100, 100);
                    Err(format!("Minecraft a crashé. Consultez le fichier launch.log pour plus de détails."))
                }
                Ok(None) => {
                    // Process still running - success!
                    emit_progress(&window, "done", "Minecraft lancé !", 100, 100);
                    Ok(format!("Minecraft lancé ! (PID: {})", pid))
                }
                Err(e) => {
                    let err = format!("Erreur lors de la vérification du processus: {}", e);
                    let _ = fs::write(&log_path, format!("{}Full command:\n{}\n\nERROR: {}\n", log_content, full_cmd, err));
                    Err(err)
                }
            }
        }
        Err(e) => {
            let err = format!("Erreur de lancement: {}", e);
            let _ = fs::write(&log_path, format!("{}Full command:\n{}\n\nERROR: {}\n", log_content, full_cmd, err));
            Err(err)
        }
    }
}

fn find_java() -> Result<String, String> {
    // Helper to check if Java is 64-bit
    fn is_java_64bit(java_path: &str) -> bool {
        if let Ok(output) = Command::new(java_path).arg("-version").output() {
            let stderr = String::from_utf8_lossy(&output.stderr);
            // 64-bit Java shows "64-Bit" in version output
            stderr.contains("64-Bit") || stderr.contains("64-bit")
        } else {
            false
        }
    }

    // Windows: try common 64-bit locations first
    #[cfg(target_os = "windows")]
    {
        let paths_64bit = vec![
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.13.11-hotspot\\bin\\java.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.12.7-hotspot\\bin\\java.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.11.9-hotspot\\bin\\java.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17\\bin\\java.exe",
            "C:\\Program Files\\Java\\jdk-17\\bin\\java.exe",
            "C:\\Program Files\\Microsoft\\jdk-17.0.13.11-hotspot\\bin\\java.exe",
            "C:\\Program Files\\Microsoft\\jdk-17\\bin\\java.exe",
            "C:\\Program Files\\Java\\jre-17\\bin\\java.exe",
            "C:\\Program Files\\Zulu\\zulu-17\\bin\\java.exe",
        ];
        for p in &paths_64bit {
            if PathBuf::from(p).exists() {
                return Ok(p.to_string());
            }
        }
    }

    // Try JAVA_HOME
    if let Ok(java_home) = std::env::var("JAVA_HOME") {
        let java_path = PathBuf::from(&java_home).join("bin").join("java");
        if java_path.exists() {
            let path_str = java_path.to_string_lossy().to_string();
            if is_java_64bit(&path_str) {
                return Ok(path_str);
            }
        }
    }

    // Try java in PATH but verify it's 64-bit
    if let Ok(output) = Command::new("java").arg("-version").output() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        if stderr.contains("64-Bit") || stderr.contains("64-bit") {
            return Ok("java".to_string());
        } else if stderr.contains("32-Bit") || stderr.contains("32-bit") {
            return Err(
                "Java 32-bit détecté ! Minecraft 1.20.4 nécessite Java 64-bit.\n\n\
                Téléchargez Java 17 64-bit sur:\nhttps://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64".to_string()
            );
        }
    }

    // Linux/macOS: try java in PATH
    #[cfg(not(target_os = "windows"))]
    {
        if Command::new("java").arg("-version").output().is_ok() {
            return Ok("java".to_string());
        }
    }

    Err("Java non trouvé. Installez Java 17 64-bit:\nhttps://adoptium.net/temurin/releases/?version=17".to_string())
}

/// Check if game files are ready
#[tauri::command]
pub async fn check_game_ready() -> Result<bool, String> {
    let game_dir = get_game_dir();
    let client_jar = get_versions_dir().join(format!("{}.jar", MINECRAFT_VERSION));
    let mods_dir = game_dir.join("mods");

    let client_exists = client_jar.exists();
    let mods_exist = mods_dir.exists() && fs::read_dir(&mods_dir)
        .map(|d| d.count() > 0)
        .unwrap_or(false);

    Ok(client_exists && mods_exist)
}
