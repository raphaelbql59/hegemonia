import axios from 'axios';
import fs from 'fs-extra';
import path from 'path';
import crypto from 'crypto';
import { app } from 'electron';

interface ModFile {
  fileName: string;
  version: string;
  hash: string;
  size: number;
  downloadUrl: string;
  isRequired: boolean;
  category: string;
}

export class ModDownloader {
  private apiUrl: string;
  private minecraftPath: string;

  constructor() {
    this.apiUrl = process.env.API_URL || 'http://localhost:3000';
    this.minecraftPath = path.join(app.getPath('appData'), '.hegemonia');
  }

  async checkForUpdates(): Promise<{ needsUpdate: boolean; mods: ModFile[] }> {
    try {
      const response = await axios.get(`${this.apiUrl}/api/launcher/mods`);
      const remoteMods: ModFile[] = response.data.mods;

      const modsPath = path.join(this.minecraftPath, 'mods');
      await fs.ensureDir(modsPath);

      const needsUpdate: ModFile[] = [];

      for (const mod of remoteMods) {
        const localPath = path.join(modsPath, mod.fileName);

        if (!fs.existsSync(localPath)) {
          needsUpdate.push(mod);
          continue;
        }

        // Verify hash
        const localHash = await this.calculateFileHash(localPath);
        if (localHash !== mod.hash) {
          needsUpdate.push(mod);
        }
      }

      return {
        needsUpdate: needsUpdate.length > 0,
        mods: needsUpdate
      };
    } catch (error) {
      console.error('Error checking for updates:', error);
      throw error;
    }
  }

  async downloadMods(onProgress?: (progress: number) => void): Promise<void> {
    const { mods } = await this.checkForUpdates();

    if (mods.length === 0) {
      return;
    }

    const modsPath = path.join(this.minecraftPath, 'mods');
    await fs.ensureDir(modsPath);

    let downloaded = 0;
    const total = mods.length;

    for (const mod of mods) {
      await this.downloadFile(mod.downloadUrl, path.join(modsPath, mod.fileName));
      downloaded++;
      onProgress?.(Math.round((downloaded / total) * 100));
    }
  }

  private async downloadFile(url: string, dest: string): Promise<void> {
    const response = await axios.get(url, { responseType: 'stream' });
    const writer = fs.createWriteStream(dest);

    response.data.pipe(writer);

    return new Promise((resolve, reject) => {
      writer.on('finish', resolve);
      writer.on('error', reject);
    });
  }

  private async calculateFileHash(filePath: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const hash = crypto.createHash('sha256');
      const stream = fs.createReadStream(filePath);

      stream.on('data', (data) => hash.update(data));
      stream.on('end', () => resolve(hash.digest('hex')));
      stream.on('error', reject);
    });
  }
}
