import { contextBridge, ipcRenderer } from 'electron';

contextBridge.exposeInMainWorld('api', {
  // Window controls
  minimize: () => ipcRenderer.invoke('window:minimize'),
  maximize: () => ipcRenderer.invoke('window:maximize'),
  close: () => ipcRenderer.invoke('window:close'),

  // Config
  getConfig: () => ipcRenderer.invoke('config:get'),
  setConfig: (config: any) => ipcRenderer.invoke('config:set', config),

  // Launcher
  checkUpdates: () => ipcRenderer.invoke('launcher:check-updates'),
  downloadMods: () => ipcRenderer.invoke('launcher:download-mods'),
  launchGame: (options: any) => ipcRenderer.invoke('launcher:launch-game', options),

  // Events
  onDownloadProgress: (callback: (progress: number) => void) => {
    ipcRenderer.on('download:progress', (event, progress) => callback(progress));
  },
  onGameStatus: (callback: (status: string) => void) => {
    ipcRenderer.on('game:status', (event, status) => callback(status));
  },
  onUpdateAvailable: (callback: () => void) => {
    ipcRenderer.on('updater:update-available', callback);
  },
  onUpdateDownloaded: (callback: () => void) => {
    ipcRenderer.on('updater:update-downloaded', callback);
  }
});
