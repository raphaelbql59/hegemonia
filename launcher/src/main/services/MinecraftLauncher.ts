import { spawn } from 'child_process';
import path from 'path';
import fs from 'fs-extra';
import { app } from 'electron';

interface LaunchOptions {
  username: string;
  ram: number;
  javaPath?: string;
}

export class MinecraftLauncher {
  private minecraftPath: string;

  constructor() {
    this.minecraftPath = path.join(app.getPath('appData'), '.hegemonia');
  }

  async launch(
    options: LaunchOptions,
    onStatus?: (status: string) => void
  ): Promise<void> {
    try {
      onStatus?.('Preparing launch...');

      const javaPath = options.javaPath || 'java';
      const ram = options.ram || 4096;

      // Ensure directories exist
      await fs.ensureDir(this.minecraftPath);
      await fs.ensureDir(path.join(this.minecraftPath, 'mods'));

      // Build Java arguments
      const javaArgs = [
        `-Xmx${ram}M`,
        `-Xms${Math.floor(ram / 2)}M`,
        '-XX:+UseG1GC',
        '-XX:+ParallelRefProcEnabled',
        '-XX:MaxGCPauseMillis=200',
        '-XX:+UnlockExperimentalVMOptions',
        '-XX:+DisableExplicitGC',
        '-XX:G1NewSizePercent=30',
        '-XX:G1MaxNewSizePercent=40',
        '-XX:G1HeapRegionSize=8M',
        '-XX:G1ReservePercent=20',
        '-XX:InitiatingHeapOccupancyPercent=15',
        '-Dminecraft.applet.TargetDirectory=' + this.minecraftPath,
        '-Dfml.ignoreInvalidMinecraftCertificates=true',
        '-Dfml.ignorePatchDiscrepancies=true',
        '-jar',
        path.join(this.minecraftPath, 'fabric-loader.jar'),
        '--username',
        options.username,
        '--version',
        '1.20.1',
        '--gameDir',
        this.minecraftPath,
        '--assetsDir',
        path.join(this.minecraftPath, 'assets')
      ];

      onStatus?.('Launching Minecraft...');

      const minecraft = spawn(javaPath, javaArgs, {
        cwd: this.minecraftPath,
        detached: true
      });

      minecraft.stdout?.on('data', (data) => {
        console.log(`Minecraft: ${data}`);
      });

      minecraft.stderr?.on('data', (data) => {
        console.error(`Minecraft Error: ${data}`);
      });

      minecraft.on('close', (code) => {
        onStatus?.(code === 0 ? 'Game closed' : 'Game crashed');
      });

      onStatus?.('Game launched successfully');
    } catch (error) {
      console.error('Launch error:', error);
      onStatus?.('Launch failed');
      throw error;
    }
  }

  getJavaPath(): string {
    // Try to find Java installation
    const possiblePaths = [
      'java',
      'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe',
      'C:\\Program Files\\Eclipse Adoptium\\jdk-17\\bin\\java.exe',
      '/usr/bin/java',
      '/usr/local/bin/java'
    ];

    for (const javaPath of possiblePaths) {
      try {
        const { spawnSync } = require('child_process');
        const result = spawnSync(javaPath, ['-version']);
        if (result.status === 0) {
          return javaPath;
        }
      } catch (error) {
        continue;
      }
    }

    return 'java';
  }
}
