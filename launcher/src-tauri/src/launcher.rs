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

// Adoptium Java 21 download URLs
const JAVA_DOWNLOAD_WIN: &str = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jre_x64_windows_hotspot_21.0.5_11.zip";
const JAVA_DOWNLOAD_MAC: &str = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jre_x64_mac_hotspot_21.0.5_11.tar.gz";
const JAVA_DOWNLOAD_LINUX: &str = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jre_x64_linux_hotspot_21.0.5_11.tar.gz";

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
    #[allow(dead_code)]
    id: String,
    #[serde(rename = "assetIndex")]
    asset_index: AssetIndex,
    #[allow(dead_code)]
    assets: String,
    downloads: Downloads,
    libraries: Vec<Library>,
    #[allow(dead_code)]
    #[serde(rename = "mainClass")]
    main_class: String,
    #[allow(dead_code)]
    #[serde(rename = "minecraftArguments", default)]
    minecraft_arguments: Option<String>,
    #[allow(dead_code)]
    arguments: Option<Arguments>,
}

#[derive(Debug, Deserialize)]
struct AssetIndex {
    id: String,
    url: String,
    #[allow(dead_code)]
    sha1: String,
    #[allow(dead_code)]
    size: u64,
    #[allow(dead_code)]
    #[serde(rename = "totalSize")]
    total_size: u64,
}

#[derive(Debug, Deserialize)]
struct Downloads {
    client: DownloadInfo,
}

#[derive(Debug, Deserialize)]
struct DownloadInfo {
    #[allow(dead_code)]
    sha1: String,
    #[allow(dead_code)]
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
    #[allow(dead_code)]
    sha1: String,
    #[allow(dead_code)]
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
    #[allow(dead_code)]
    game: Vec<serde_json::Value>,
    #[allow(dead_code)]
    jvm: Vec<serde_json::Value>,
}

#[derive(Debug, Deserialize)]
struct AssetIndexData {
    objects: HashMap<String, AssetObject>,
}

#[derive(Debug, Deserialize)]
struct AssetObject {
    hash: String,
    #[allow(dead_code)]
    size: u64,
}

// ============================================================================
// Fabric API Structures
// ============================================================================

#[derive(Debug, Deserialize)]
struct FabricProfile {
    #[allow(dead_code)]
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

fn get_java_dir() -> PathBuf {
    get_game_dir().join("java")
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

    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(300))
        .build()
        .map_err(|e| format!("Failed to create client: {}", e))?;

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
// Java detection and download
// ============================================================================

/// Verify that a Java executable actually works
fn verify_java(path: &str) -> bool {
    match Command::new(path).arg("-version").output() {
        Ok(output) => {
            let stderr = String::from_utf8_lossy(&output.stderr);
            let stdout = String::from_utf8_lossy(&output.stdout);
            let version_output = format!("{}{}", stderr, stdout);

            // Must have some version output and not be 32-bit
            let is_valid = (version_output.contains("version") || version_output.contains("openjdk"))
                && !version_output.contains("32-Bit");

            if is_valid {
                eprintln!("Found valid Java: {}", path);
            }
            is_valid
        }
        Err(_) => false,
    }
}

fn find_java() -> Result<String, String> {
    eprintln!("Searching for Java...");

    // 1. Check our bundled Java first
    let bundled_java = get_java_dir();
    #[cfg(target_os = "windows")]
    let bundled_paths = vec![
        bundled_java.join("jdk-21.0.5+11-jre").join("bin").join("javaw.exe"),
        bundled_java.join("bin").join("javaw.exe"),
    ];
    #[cfg(target_os = "macos")]
    let bundled_paths = vec![
        bundled_java.join("jdk-21.0.5+11-jre").join("Contents").join("Home").join("bin").join("java"),
    ];
    #[cfg(target_os = "linux")]
    let bundled_paths = vec![
        bundled_java.join("jdk-21.0.5+11-jre").join("bin").join("java"),
    ];

    for p in bundled_paths {
        if p.exists() {
            let p_str = p.to_string_lossy().to_string();
            if verify_java(&p_str) {
                return Ok(p_str);
            }
        }
    }

    // 2. Windows: Try Minecraft's bundled Java runtime
    #[cfg(target_os = "windows")]
    {
        let appdata = std::env::var("APPDATA").unwrap_or_default();
        let localappdata = std::env::var("LOCALAPPDATA").unwrap_or_default();

        // All possible Minecraft Java paths
        let mc_java_paths = vec![
            // Standard launcher - java-runtime-delta (Java 21)
            format!("{}\\.minecraft\\runtime\\java-runtime-delta\\windows-x64\\java-runtime-delta\\bin\\javaw.exe", appdata),
            // Standard launcher - java-runtime-gamma (Java 17)
            format!("{}\\.minecraft\\runtime\\java-runtime-gamma\\windows-x64\\java-runtime-gamma\\bin\\javaw.exe", appdata),
            // Microsoft Store launcher
            format!("{}\\Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local\\runtime\\java-runtime-delta\\windows-x64\\java-runtime-delta\\bin\\javaw.exe", localappdata),
            format!("{}\\Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local\\runtime\\java-runtime-gamma\\windows-x64\\java-runtime-gamma\\bin\\javaw.exe", localappdata),
            // Old launcher paths
            format!("{}\\.minecraft\\runtime\\java-runtime-beta\\windows-x64\\java-runtime-beta\\bin\\javaw.exe", appdata),
            format!("{}\\.minecraft\\runtime\\jre-legacy\\windows-x64\\jre-legacy\\bin\\javaw.exe", appdata),
        ];

        for p in &mc_java_paths {
            eprintln!("Checking: {}", p);
            if PathBuf::from(p).exists() && verify_java(p) {
                return Ok(p.to_string());
            }
        }

        // 3. Common 64-bit installation paths
        let common_paths = vec![
            "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.5.11-hotspot\\bin\\javaw.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-21\\bin\\javaw.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jre-21.0.5.11-hotspot\\bin\\javaw.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.13.11-hotspot\\bin\\javaw.exe",
            "C:\\Program Files\\Eclipse Adoptium\\jdk-17\\bin\\javaw.exe",
            "C:\\Program Files\\Java\\jdk-21\\bin\\javaw.exe",
            "C:\\Program Files\\Java\\jdk-17\\bin\\javaw.exe",
            "C:\\Program Files\\Java\\jre-21\\bin\\javaw.exe",
            "C:\\Program Files\\Java\\jre-17\\bin\\javaw.exe",
            "C:\\Program Files\\Microsoft\\jdk-21.0.5.11-hotspot\\bin\\javaw.exe",
            "C:\\Program Files\\Microsoft\\jdk-17.0.13.11-hotspot\\bin\\javaw.exe",
            "C:\\Program Files\\Zulu\\zulu-21\\bin\\javaw.exe",
            "C:\\Program Files\\Zulu\\zulu-17\\bin\\javaw.exe",
            "C:\\Program Files\\BellSoft\\LibericaJDK-21\\bin\\javaw.exe",
            "C:\\Program Files\\BellSoft\\LibericaJDK-17\\bin\\javaw.exe",
            "C:\\Program Files\\Amazon Corretto\\jdk21.0.5_11\\bin\\javaw.exe",
            "C:\\Program Files\\Amazon Corretto\\jdk17.0.13_11\\bin\\javaw.exe",
        ];

        for p in &common_paths {
            if PathBuf::from(p).exists() && verify_java(p) {
                return Ok(p.to_string());
            }
        }
    }

    // macOS: try Minecraft's bundled Java and common paths
    #[cfg(target_os = "macos")]
    {
        let home = std::env::var("HOME").unwrap_or_default();
        let mc_java_paths = vec![
            format!("{}/Library/Application Support/minecraft/runtime/java-runtime-delta/mac-os/java-runtime-delta/jre.bundle/Contents/Home/bin/java", home),
            format!("{}/Library/Application Support/minecraft/runtime/java-runtime-delta/mac-os-arm64/java-runtime-delta/jre.bundle/Contents/Home/bin/java", home),
            format!("{}/Library/Application Support/minecraft/runtime/java-runtime-gamma/mac-os/java-runtime-gamma/jre.bundle/Contents/Home/bin/java", home),
            format!("{}/Library/Application Support/minecraft/runtime/java-runtime-gamma/mac-os-arm64/java-runtime-gamma/jre.bundle/Contents/Home/bin/java", home),
            "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java".to_string(),
            "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java".to_string(),
        ];
        for p in &mc_java_paths {
            if PathBuf::from(p).exists() && verify_java(p) {
                return Ok(p.to_string());
            }
        }
    }

    // Linux: try Minecraft's bundled Java and common paths
    #[cfg(target_os = "linux")]
    {
        let home = std::env::var("HOME").unwrap_or_default();
        let linux_paths = vec![
            format!("{}/.minecraft/runtime/java-runtime-delta/linux/java-runtime-delta/bin/java", home),
            format!("{}/.minecraft/runtime/java-runtime-gamma/linux/java-runtime-gamma/bin/java", home),
            "/usr/lib/jvm/java-21-openjdk/bin/java".to_string(),
            "/usr/lib/jvm/java-17-openjdk/bin/java".to_string(),
            "/usr/lib/jvm/temurin-21-jdk/bin/java".to_string(),
            "/usr/lib/jvm/temurin-17-jdk/bin/java".to_string(),
        ];
        for p in &linux_paths {
            if PathBuf::from(p).exists() && verify_java(p) {
                return Ok(p.to_string());
            }
        }
    }

    // 4. Try JAVA_HOME
    if let Ok(java_home) = std::env::var("JAVA_HOME") {
        #[cfg(target_os = "windows")]
        let java_path = PathBuf::from(&java_home).join("bin").join("javaw.exe");
        #[cfg(not(target_os = "windows"))]
        let java_path = PathBuf::from(&java_home).join("bin").join("java");

        if java_path.exists() {
            let p_str = java_path.to_string_lossy().to_string();
            if verify_java(&p_str) {
                return Ok(p_str);
            }
        }
    }

    // 5. Try PATH (but avoid javapath shim on Windows)
    #[cfg(target_os = "windows")]
    {
        if let Ok(output) = Command::new("where").arg("java").output() {
            let paths = String::from_utf8_lossy(&output.stdout);
            for path in paths.lines() {
                let path = path.trim();
                // Skip javapath shim and verify it works
                if !path.to_lowercase().contains("javapath") {
                    let javaw_path = path.replace("java.exe", "javaw.exe");
                    if PathBuf::from(&javaw_path).exists() && verify_java(&javaw_path) {
                        return Ok(javaw_path);
                    }
                    if verify_java(path) {
                        return Ok(path.to_string());
                    }
                }
            }
        }
    }

    #[cfg(not(target_os = "windows"))]
    {
        if verify_java("java") {
            return Ok("java".to_string());
        }
    }

    Err("Java non trouve. Le launcher va le telecharger automatiquement.".to_string())
}

/// Download Java automatically if not found
async fn download_java(window: &Window) -> Result<String, String> {
    emit_progress(window, "java", "Telechargement de Java 21...", 0, 100);

    let java_dir = get_java_dir();
    fs::create_dir_all(&java_dir).map_err(|e| format!("Failed to create java dir: {}", e))?;

    #[cfg(target_os = "windows")]
    let (download_url, archive_name) = (JAVA_DOWNLOAD_WIN, "java.zip");
    #[cfg(target_os = "macos")]
    let (download_url, archive_name) = (JAVA_DOWNLOAD_MAC, "java.tar.gz");
    #[cfg(target_os = "linux")]
    let (download_url, archive_name) = (JAVA_DOWNLOAD_LINUX, "java.tar.gz");

    let archive_path = java_dir.join(archive_name);

    // Download
    emit_progress(window, "java", "Telechargement de Java 21...", 10, 100);

    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(600))
        .build()
        .map_err(|e| format!("Failed to create client: {}", e))?;

    let response = client.get(download_url)
        .send()
        .await
        .map_err(|e| format!("Download failed: {}", e))?;

    if !response.status().is_success() {
        return Err(format!("HTTP error: {}", response.status()));
    }

    let bytes = response.bytes()
        .await
        .map_err(|e| format!("Failed to read response: {}", e))?;

    fs::write(&archive_path, &bytes).map_err(|e| format!("Failed to write file: {}", e))?;

    emit_progress(window, "java", "Extraction de Java...", 50, 100);

    // Extract
    #[cfg(target_os = "windows")]
    {
        // Extract ZIP
        let file = fs::File::open(&archive_path).map_err(|e| e.to_string())?;
        let mut archive = zip::ZipArchive::new(file).map_err(|e| format!("Failed to open archive: {}", e))?;

        for i in 0..archive.len() {
            let mut file = archive.by_index(i).map_err(|e| format!("Failed to extract: {}", e))?;
            let outpath = java_dir.join(file.name());

            if file.name().ends_with('/') {
                fs::create_dir_all(&outpath).ok();
            } else {
                if let Some(parent) = outpath.parent() {
                    fs::create_dir_all(parent).ok();
                }
                let mut outfile = fs::File::create(&outpath).map_err(|e| format!("Failed to create file: {}", e))?;
                std::io::copy(&mut file, &mut outfile).map_err(|e| format!("Failed to write file: {}", e))?;
            }
        }
    }

    #[cfg(not(target_os = "windows"))]
    {
        // Extract tar.gz using tar command
        Command::new("tar")
            .args(&["-xzf", archive_path.to_str().unwrap()])
            .current_dir(&java_dir)
            .output()
            .map_err(|e| format!("Failed to extract: {}", e))?;
    }

    // Clean up archive
    fs::remove_file(&archive_path).ok();

    emit_progress(window, "java", "Java installe!", 100, 100);

    // Find the extracted java executable
    #[cfg(target_os = "windows")]
    let java_exe = java_dir.join("jdk-21.0.5+11-jre").join("bin").join("javaw.exe");
    #[cfg(target_os = "macos")]
    let java_exe = java_dir.join("jdk-21.0.5+11-jre").join("Contents").join("Home").join("bin").join("java");
    #[cfg(target_os = "linux")]
    let java_exe = java_dir.join("jdk-21.0.5+11-jre").join("bin").join("java");

    if java_exe.exists() {
        Ok(java_exe.to_string_lossy().to_string())
    } else {
        Err("Failed to find extracted Java".to_string())
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

    // Find or download Java first
    emit_progress(&window, "java", "Verification de Java...", 0, 100);
    let java_cmd = match find_java() {
        Ok(path) => path,
        Err(_) => {
            // Auto-download Java
            download_java(&window).await?
        }
    };
    eprintln!("Using Java: {}", java_cmd);

    // ========================================================================
    // Step 1: Download version manifest
    // ========================================================================
    emit_progress(&window, "manifest", "Recuperation des informations Minecraft...", 5, 100);

    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(60))
        .build()
        .map_err(|e| format!("Failed to create client: {}", e))?;

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
    emit_progress(&window, "metadata", "Telechargement des metadonnees...", 8, 100);

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
    emit_progress(&window, "client", "Telechargement du client Minecraft...", 10, 100);

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

        let progress = 15 + (i * 25 / total_libs) as u64;
        emit_progress(&window, "libraries", &format!("Bibliotheque {}/{}", i + 1, total_libs), progress, 100);

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
    emit_progress(&window, "assets", "Telechargement des assets...", 45, 100);

    let asset_index_path = assets_dir.join("indexes").join(format!("{}.json", version_meta.asset_index.id));
    download_file(&version_meta.asset_index.url, &asset_index_path).await?;

    let asset_index: AssetIndexData = serde_json::from_str(
        &fs::read_to_string(&asset_index_path).map_err(|e| e.to_string())?
    ).map_err(|e| format!("Failed to parse asset index: {}", e))?;

    let total_assets = asset_index.objects.len();
    let mut downloaded_assets = 0;

    for (_name, obj) in &asset_index.objects {
        let hash_prefix = &obj.hash[0..2];
        let asset_path = assets_dir.join("objects").join(hash_prefix).join(&obj.hash);
        let asset_url = format!("https://resources.download.minecraft.net/{}/{}", hash_prefix, obj.hash);

        if !asset_path.exists() {
            download_file(&asset_url, &asset_path).await?;
        }

        downloaded_assets += 1;
        if downloaded_assets % 100 == 0 {
            let progress = 45 + (downloaded_assets * 20 / total_assets) as u64;
            emit_progress(&window, "assets", &format!("Assets {}/{}", downloaded_assets, total_assets), progress, 100);
        }
    }

    // ========================================================================
    // Step 6: Download Fabric
    // ========================================================================
    emit_progress(&window, "fabric", "Telechargement de Fabric...", 70, 100);

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
    emit_progress(&window, "mods", "Verification des mods...", 88, 100);

    let mods_dir = game_dir.join("mods");
    fs::create_dir_all(&mods_dir).map_err(|e| e.to_string())?;

    let mod_count = fs::read_dir(&mods_dir)
        .map(|d| d.filter(|e| e.as_ref().map(|e| e.path().extension().map(|ext| ext == "jar").unwrap_or(false)).unwrap_or(false)).count())
        .unwrap_or(0);

    emit_progress(&window, "mods", &format!("{} mods installes", mod_count), 92, 100);

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
        "-Dfile.encoding=UTF-8".to_string(),
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
        "Launch at: {:?}\nUsername: {}\nUUID: {}\nJava: {}\nArgs: {:?}\n",
        std::time::SystemTime::now(),
        username,
        uuid,
        java_cmd,
        args
    );
    let _ = fs::write(&log_path, &log_content);

    // Launch using CREATE_NO_WINDOW on Windows to hide console
    #[cfg(target_os = "windows")]
    use std::os::windows::process::CommandExt;

    let mut command = Command::new(&java_cmd);
    command.args(&args).current_dir(&game_dir);

    #[cfg(target_os = "windows")]
    command.creation_flags(0x08000000); // CREATE_NO_WINDOW

    match command.spawn() {
        Ok(child) => {
            let pid = child.id();
            let _ = fs::write(&log_path, format!("{}\nPID: {}\n", log_content, pid));
            emit_progress(&window, "done", "Minecraft lance!", 100, 100);
            Ok(format!("Minecraft lance! (PID: {})", pid))
        }
        Err(e) => {
            let err = format!("Erreur de lancement: {}", e);
            let _ = fs::write(&log_path, format!("{}\nERROR: {}\n", log_content, err));
            Err(err)
        }
    }
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

