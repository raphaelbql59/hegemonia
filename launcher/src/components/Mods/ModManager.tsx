import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { invoke } from '@tauri-apps/api/tauri';
import { listen } from '@tauri-apps/api/event';
import { toast } from 'sonner';

interface ModInfo {
  id: string;
  name: string;
  version: string;
  file_name: string;
  size: number;
  required: boolean;
}

interface InstallationStatus {
  minecraft_installed: boolean;
  fabric_installed: boolean;
  mods_installed: boolean;
  mods_outdated: string[];
  resource_pack_installed: boolean;
  needs_update: boolean;
}

interface DownloadProgress {
  file_name: string;
  current: number;
  total: number;
  percentage: number;
  status: string;
}

interface HegemoniaPack {
  version: string;
  minecraft_version: string;
  fabric_version: string;
  mods: ModInfo[];
  resource_pack: {
    name: string;
    version: string;
    file_name: string;
    size: number;
  } | null;
}

export default function ModManager({ onReady }: { onReady: () => void }) {
  const [status, setStatus] = useState<InstallationStatus | null>(null);
  const [manifest, setManifest] = useState<HegemoniaPack | null>(null);
  const [installing, setInstalling] = useState(false);
  const [progress, setProgress] = useState<DownloadProgress | null>(null);
  const [installStatus, setInstallStatus] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkStatus();
    fetchManifest();

    // Listen for download progress
    const unlistenProgress = listen<DownloadProgress>('download-progress', (event) => {
      setProgress(event.payload);
    });

    // Listen for install status
    const unlistenStatus = listen<string>('install-status', (event) => {
      setInstallStatus(event.payload);
    });

    // Listen for warnings
    const unlistenWarning = listen<string>('install-warning', (event) => {
      toast.warning(event.payload);
    });

    return () => {
      unlistenProgress.then(f => f());
      unlistenStatus.then(f => f());
      unlistenWarning.then(f => f());
    };
  }, []);

  const checkStatus = async () => {
    try {
      const result = await invoke<InstallationStatus>('check_installation_status');
      setStatus(result);
      if (!result.needs_update) {
        onReady();
      }
    } catch (error) {
      console.error('Failed to check status:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchManifest = async () => {
    try {
      const result = await invoke<HegemoniaPack>('fetch_modpack_manifest');
      setManifest(result);
    } catch (error) {
      console.error('Failed to fetch manifest:', error);
    }
  };

  const handleInstall = async () => {
    setInstalling(true);
    setProgress(null);
    setInstallStatus('Démarrage de l\'installation...');

    try {
      await invoke('install_modpack');
      await invoke('create_minecraft_profile');
      toast.success('Installation terminée !');
      await checkStatus();
    } catch (error: any) {
      toast.error(error?.message || error || 'Erreur lors de l\'installation');
    } finally {
      setInstalling(false);
      setProgress(null);
      setInstallStatus('');
    }
  };

  const formatSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
      </div>
    );
  }

  if (!status?.needs_update) {
    return null;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="glass rounded-xl p-6 mb-6"
    >
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-display font-bold text-gradient">
          Installation Hegemonia
        </h2>
        {manifest && (
          <span className="text-sm text-gray-400">
            Pack v{manifest.version} • Minecraft {manifest.minecraft_version}
          </span>
        )}
      </div>

      {/* Status indicators */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <StatusItem
          label="Minecraft"
          installed={status?.minecraft_installed ?? false}
        />
        <StatusItem
          label="Fabric"
          installed={status?.fabric_installed ?? false}
        />
        <StatusItem
          label="Mods"
          installed={status?.mods_installed ?? false}
        />
        <StatusItem
          label="Resource Pack"
          installed={status?.resource_pack_installed ?? false}
        />
      </div>

      {/* Mod list */}
      {manifest && (
        <div className="mb-6">
          <h3 className="text-sm font-semibold text-gray-300 mb-3">
            Mods inclus ({manifest.mods.length})
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-2 max-h-40 overflow-y-auto">
            {manifest.mods.map((mod) => (
              <div
                key={mod.id}
                className="flex items-center gap-2 p-2 bg-dark-800 rounded-lg"
              >
                <div className={`w-2 h-2 rounded-full ${mod.required ? 'bg-primary-500' : 'bg-gray-500'}`}></div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{mod.name}</p>
                  <p className="text-xs text-gray-500">{mod.version} • {formatSize(mod.size)}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Progress bar */}
      <AnimatePresence>
        {installing && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="mb-6"
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-300">{installStatus}</span>
              {progress && (
                <span className="text-sm text-gray-400">
                  {progress.percentage.toFixed(0)}%
                </span>
              )}
            </div>
            <div className="h-2 bg-dark-800 rounded-full overflow-hidden">
              <motion.div
                className="h-full bg-gradient-to-r from-primary-500 to-primary-400"
                initial={{ width: 0 }}
                animate={{ width: progress ? `${progress.percentage}%` : '0%' }}
                transition={{ duration: 0.3 }}
              />
            </div>
            {progress && (
              <p className="text-xs text-gray-500 mt-1">
                {progress.file_name} • {formatSize(progress.current)} / {formatSize(progress.total)}
              </p>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* Install button */}
      <motion.button
        whileHover={{ scale: 1.02 }}
        whileTap={{ scale: 0.98 }}
        onClick={handleInstall}
        disabled={installing}
        className="w-full py-4 bg-gradient-to-r from-primary-500 to-primary-600 text-white font-semibold rounded-xl shadow-lg glow disabled:opacity-50 disabled:cursor-not-allowed transition-all"
      >
        {installing ? (
          <span className="flex items-center justify-center gap-2">
            <div className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full"></div>
            Installation en cours...
          </span>
        ) : (
          'Installer le pack Hegemonia'
        )}
      </motion.button>

      <p className="text-xs text-gray-500 text-center mt-3">
        Cette opération peut prendre plusieurs minutes selon votre connexion.
      </p>
    </motion.div>
  );
}

function StatusItem({ label, installed }: { label: string; installed: boolean }) {
  return (
    <div className="flex items-center gap-2 p-3 bg-dark-800 rounded-lg">
      <div className={`w-3 h-3 rounded-full ${installed ? 'bg-green-500' : 'bg-red-500'}`}></div>
      <span className="text-sm">{label}</span>
    </div>
  );
}
