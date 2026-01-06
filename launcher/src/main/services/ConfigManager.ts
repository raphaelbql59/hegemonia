import fs from 'fs-extra';
import path from 'path';
import { app } from 'electron';

export interface LauncherConfig {
  apiUrl: string;
  minecraftPath: string;
  javaPath: string;
  username: string;
  ram: number;
  autoUpdate: boolean;
  keepLauncherOpen: boolean;
}

const DEFAULT_CONFIG: LauncherConfig = {
  apiUrl: 'http://localhost:3000',
  minecraftPath: path.join(app.getPath('appData'), '.hegemonia'),
  javaPath: 'java',
  username: '',
  ram: 4096,
  autoUpdate: true,
  keepLauncherOpen: false
};

export class ConfigManager {
  private configPath: string;
  private config: LauncherConfig;

  constructor() {
    this.configPath = path.join(app.getPath('userData'), 'config.json');
    this.config = this.loadConfig();
  }

  private loadConfig(): LauncherConfig {
    try {
      if (fs.existsSync(this.configPath)) {
        const data = fs.readJsonSync(this.configPath);
        return { ...DEFAULT_CONFIG, ...data };
      }
    } catch (error) {
      console.error('Error loading config:', error);
    }
    return DEFAULT_CONFIG;
  }

  getConfig(): LauncherConfig {
    return this.config;
  }

  setConfig(newConfig: Partial<LauncherConfig>): void {
    this.config = { ...this.config, ...newConfig };
    fs.ensureDirSync(path.dirname(this.configPath));
    fs.writeJsonSync(this.configPath, this.config, { spaces: 2 });
  }
}
