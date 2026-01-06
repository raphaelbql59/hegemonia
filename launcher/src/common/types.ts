export interface IElectronAPI {
  // Window controls
  minimize: () => Promise<void>;
  maximize: () => Promise<void>;
  close: () => Promise<void>;

  // Config
  getConfig: () => Promise<any>;
  setConfig: (config: any) => Promise<void>;

  // Launcher
  checkUpdates: () => Promise<{ needsUpdate: boolean; mods: any[] }>;
  downloadMods: () => Promise<void>;
  launchGame: (options: any) => Promise<void>;

  // Events
  onDownloadProgress: (callback: (progress: number) => void) => void;
  onGameStatus: (callback: (status: string) => void) => void;
  onUpdateAvailable: (callback: () => void) => void;
  onUpdateDownloaded: (callback: () => void) => void;
}

declare global {
  interface Window {
    api: IElectronAPI;
  }
}
