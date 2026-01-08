// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod commands;
mod minecraft;

use commands::*;
use minecraft::*;

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![
            // Game commands
            launch_minecraft,
            check_minecraft_installed,
            get_minecraft_path,
            get_system_info,
            download_file,
            check_java_installed,
            get_java_path,
            // Minecraft/Modpack commands
            check_installation_status,
            fetch_modpack_manifest,
            install_modpack,
            install_fabric,
            create_minecraft_profile
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
