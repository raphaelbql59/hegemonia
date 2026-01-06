import { app, BrowserWindow, ipcMain } from 'electron';
import { autoUpdater } from 'electron-updater';
import path from 'path';
import { ModDownloader } from './services/ModDownloader';
import { MinecraftLauncher } from './services/MinecraftLauncher';
import { ConfigManager } from './services/ConfigManager';

let mainWindow: BrowserWindow | null = null;
const modDownloader = new ModDownloader();
const minecraftLauncher = new MinecraftLauncher();
const configManager = new ConfigManager();

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 1000,
    minHeight: 700,
    frame: false,
    backgroundColor: '#0a0a0a',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, '../preload/preload.js')
    }
  });

  // Load renderer
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:3001');
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, '../renderer/index.html'));
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

// App events
app.on('ready', () => {
  createWindow();

  // Check for updates
  if (process.env.NODE_ENV === 'production') {
    autoUpdater.checkForUpdatesAndNotify();
  }
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (mainWindow === null) {
    createWindow();
  }
});

// ============================================
// IPC HANDLERS
// ============================================

// Window controls
ipcMain.handle('window:minimize', () => {
  mainWindow?.minimize();
});

ipcMain.handle('window:maximize', () => {
  if (mainWindow?.isMaximized()) {
    mainWindow.unmaximize();
  } else {
    mainWindow?.maximize();
  }
});

ipcMain.handle('window:close', () => {
  mainWindow?.close();
});

// Config
ipcMain.handle('config:get', () => {
  return configManager.getConfig();
});

ipcMain.handle('config:set', (event, config) => {
  return configManager.setConfig(config);
});

// Launcher
ipcMain.handle('launcher:check-updates', async () => {
  return await modDownloader.checkForUpdates();
});

ipcMain.handle('launcher:download-mods', async () => {
  return await modDownloader.downloadMods((progress) => {
    mainWindow?.webContents.send('download:progress', progress);
  });
});

ipcMain.handle('launcher:launch-game', async (event, options) => {
  return await minecraftLauncher.launch(options, (status) => {
    mainWindow?.webContents.send('game:status', status);
  });
});

ipcMain.handle('launcher:get-java-path', () => {
  return minecraftLauncher.getJavaPath();
});

// Auto updater events
autoUpdater.on('update-available', () => {
  mainWindow?.webContents.send('updater:update-available');
});

autoUpdater.on('update-downloaded', () => {
  mainWindow?.webContents.send('updater:update-downloaded');
});

autoUpdater.on('download-progress', (progress) => {
  mainWindow?.webContents.send('updater:progress', progress);
});

ipcMain.handle('updater:install', () => {
  autoUpdater.quitAndInstall();
});
