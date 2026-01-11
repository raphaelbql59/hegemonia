import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { toast, Toaster } from 'sonner';
import { invoke } from '@tauri-apps/api/tauri';
import { listen } from '@tauri-apps/api/event';
import { appWindow } from '@tauri-apps/api/window';

const LAUNCHER_VERSION = '1.2.0';
const SERVER_IP = '51.75.31.173';
const SERVER_PORT = '25577';

interface LaunchProgress {
  stage: string;
  message: string;
  current: number;
  total: number;
}

function App() {
  const [username, setUsername] = useState(() => localStorage.getItem('hegemonia_username') || '');
  const [launching, setLaunching] = useState(false);
  const [progress, setProgress] = useState<LaunchProgress | null>(null);
  const [serverOnline, setServerOnline] = useState<boolean | null>(null);
  const [playerCount, setPlayerCount] = useState(0);
  const [showSettings, setShowSettings] = useState(false);
  const [ram, setRam] = useState(() => parseInt(localStorage.getItem('hegemonia_ram') || '4096'));

  // Check server status
  useEffect(() => {
    const checkServer = async () => {
      try {
        const response = await fetch(`https://api.mcsrvstat.us/3/${SERVER_IP}:${SERVER_PORT}`);
        const data = await response.json();
        setServerOnline(data.online);
        setPlayerCount(data.players?.online || 0);
      } catch {
        setServerOnline(false);
      }
    };
    checkServer();
    const interval = setInterval(checkServer, 30000);
    return () => clearInterval(interval);
  }, []);

  // Listen for launch progress
  useEffect(() => {
    const unlistenProgress = listen<LaunchProgress>('launch-progress', (event) => {
      setProgress(event.payload);
    });
    const unlistenStatus = listen<string>('install-status', (event) => {
      setProgress(prev => prev ? { ...prev, message: event.payload } : null);
    });
    return () => {
      unlistenProgress.then(f => f());
      unlistenStatus.then(f => f());
    };
  }, []);

  const handleLaunch = async () => {
    if (!username.trim()) {
      toast.error('Entre ton pseudo pour jouer !');
      return;
    }
    if (username.length < 3 || username.length > 16) {
      toast.error('Le pseudo doit faire entre 3 et 16 caractères');
      return;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      toast.error('Le pseudo ne peut contenir que des lettres, chiffres et _');
      return;
    }

    localStorage.setItem('hegemonia_username', username);
    localStorage.setItem('hegemonia_ram', ram.toString());
    setLaunching(true);
    setProgress({ stage: 'init', message: 'Initialisation...', current: 0, total: 100 });

    try {
      // Generate offline UUID from username
      const uuid = await generateOfflineUUID(username);

      // Install mods if needed
      setProgress({ stage: 'mods', message: 'Vérification des mods...', current: 5, total: 100 });
      try {
        await invoke('install_modpack');
      } catch (e) {
        console.log('Mod installation skipped or failed:', e);
      }

      // Launch game
      setProgress({ stage: 'launch', message: 'Lancement de Minecraft...', current: 10, total: 100 });
      await invoke('launch_game', {
        username: username,
        uuid: uuid,
        accessToken: '0',
        serverIp: SERVER_IP,
        serverPort: SERVER_PORT,
        ramMb: ram,
      });

      toast.success('Minecraft lancé ! Bon jeu sur Hegemonia !');

      // Keep disabled briefly
      setTimeout(() => {
        setLaunching(false);
        setProgress(null);
      }, 5000);
    } catch (error: any) {
      console.error('Launch error:', error);
      toast.error(error?.message || error || 'Erreur lors du lancement');
      setLaunching(false);
      setProgress(null);
    }
  };

  const generateOfflineUUID = async (name: string): Promise<string> => {
    const encoder = new TextEncoder();
    const data = encoder.encode('OfflinePlayer:' + name);
    const hashBuffer = await crypto.subtle.digest('MD5', data).catch(() => null);

    if (!hashBuffer) {
      // Fallback: simple hash
      let hash = 0;
      for (let i = 0; i < name.length; i++) {
        hash = ((hash << 5) - hash) + name.charCodeAt(i);
        hash = hash & hash;
      }
      return `00000000-0000-0000-0000-${Math.abs(hash).toString(16).padStart(12, '0')}`;
    }

    const hashArray = Array.from(new Uint8Array(hashBuffer));
    hashArray[6] = (hashArray[6] & 0x0f) | 0x30;
    hashArray[8] = (hashArray[8] & 0x3f) | 0x80;
    const hex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return `${hex.slice(0,8)}-${hex.slice(8,12)}-${hex.slice(12,16)}-${hex.slice(16,20)}-${hex.slice(20,32)}`;
  };

  return (
    <div className="h-screen w-screen overflow-hidden bg-[#0a0a0f] text-white font-sans select-none">
      <Toaster position="top-center" richColors theme="dark" />

      {/* Animated Background */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-purple-900/20 via-transparent to-cyan-900/20" />
        <motion.div
          className="absolute -top-1/2 -left-1/2 w-full h-full bg-gradient-radial from-purple-600/10 to-transparent"
          animate={{ rotate: 360 }}
          transition={{ duration: 60, repeat: Infinity, ease: "linear" }}
        />
        <motion.div
          className="absolute -bottom-1/2 -right-1/2 w-full h-full bg-gradient-radial from-cyan-600/10 to-transparent"
          animate={{ rotate: -360 }}
          transition={{ duration: 80, repeat: Infinity, ease: "linear" }}
        />
        {/* Particles */}
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-1 h-1 bg-white/20 rounded-full"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              y: [0, -100, 0],
              opacity: [0, 1, 0],
            }}
            transition={{
              duration: 3 + Math.random() * 2,
              repeat: Infinity,
              delay: Math.random() * 2,
            }}
          />
        ))}
      </div>

      {/* Main Content */}
      <div className="relative z-10 h-full flex flex-col">
        {/* Header - Draggable Title Bar */}
        <header
          data-tauri-drag-region
          className="flex items-center justify-between px-6 py-3 bg-black/30 backdrop-blur-xl border-b border-white/5"
        >
          <div className="flex items-center gap-4" data-tauri-drag-region>
            <motion.div
              className="w-9 h-9 rounded-xl bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center font-bold text-sm"
              whileHover={{ scale: 1.1, rotate: 5 }}
            >
              H
            </motion.div>
            <div data-tauri-drag-region>
              <h1 className="text-lg font-bold tracking-wide">HEGEMONIA</h1>
              <p className="text-xs text-gray-500">v{LAUNCHER_VERSION}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {/* Server Status */}
            <div className="flex items-center gap-2 px-3 py-1.5 bg-white/5 rounded-full">
              <motion.div
                className={`w-2 h-2 rounded-full ${serverOnline ? 'bg-green-400' : serverOnline === false ? 'bg-red-400' : 'bg-yellow-400'}`}
                animate={serverOnline ? { scale: [1, 1.2, 1] } : {}}
                transition={{ repeat: Infinity, duration: 2 }}
              />
              <span className="text-xs text-gray-300">
                {serverOnline ? `${playerCount} joueurs` : serverOnline === false ? 'Hors ligne' : '...'}
              </span>
            </div>

            {/* Settings */}
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => setShowSettings(!showSettings)}
              className="w-8 h-8 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center transition-colors"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </motion.button>

            {/* Window Controls */}
            <div className="flex items-center gap-1 ml-2">
              <button
                onClick={() => appWindow.minimize()}
                className="w-8 h-8 rounded-lg hover:bg-white/10 flex items-center justify-center transition-colors"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                </svg>
              </button>
              <button
                onClick={() => appWindow.toggleMaximize()}
                className="w-8 h-8 rounded-lg hover:bg-white/10 flex items-center justify-center transition-colors"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                </svg>
              </button>
              <button
                onClick={() => appWindow.close()}
                className="w-8 h-8 rounded-lg hover:bg-red-500/80 flex items-center justify-center transition-colors"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        </header>

        {/* Main Area */}
        <main className="flex-1 flex items-center justify-center p-8">
          <div className="w-full max-w-lg">
            {/* Logo Animation */}
            <motion.div
              className="text-center mb-8"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
            >
              <motion.h2
                className="text-5xl font-black tracking-wider bg-gradient-to-r from-purple-400 via-pink-400 to-cyan-400 bg-clip-text text-transparent"
                animate={{ backgroundPosition: ['0%', '100%', '0%'] }}
                transition={{ duration: 5, repeat: Infinity }}
                style={{ backgroundSize: '200%' }}
              >
                HEGEMONIA
              </motion.h2>
              <p className="text-gray-400 mt-2">Serveur Minecraft Geopolitique</p>
            </motion.div>

            {/* Username Input */}
            <motion.div
              className="mb-6"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <label className="block text-sm text-gray-400 mb-2">Ton pseudo Minecraft</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Entre ton pseudo..."
                maxLength={16}
                disabled={launching}
                className="w-full px-5 py-4 bg-white/5 border border-white/10 rounded-2xl text-lg placeholder-gray-500 focus:outline-none focus:border-purple-500/50 focus:bg-white/10 transition-all disabled:opacity-50"
                onKeyDown={(e) => e.key === 'Enter' && handleLaunch()}
              />
            </motion.div>

            {/* Progress Bar */}
            <AnimatePresence>
              {progress && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="mb-6"
                >
                  <div className="flex justify-between text-sm text-gray-400 mb-2">
                    <span>{progress.message}</span>
                    <span>{Math.round(progress.current)}%</span>
                  </div>
                  <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                    <motion.div
                      className="h-full bg-gradient-to-r from-purple-500 to-cyan-500"
                      initial={{ width: 0 }}
                      animate={{ width: `${progress.current}%` }}
                      transition={{ duration: 0.3 }}
                    />
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Play Button */}
            <motion.button
              onClick={handleLaunch}
              disabled={launching || !username.trim()}
              className="w-full py-5 rounded-2xl font-bold text-xl relative overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed group"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              whileHover={{ scale: launching ? 1 : 1.02 }}
              whileTap={{ scale: launching ? 1 : 0.98 }}
            >
              {/* Button background */}
              <div className="absolute inset-0 bg-gradient-to-r from-purple-600 via-pink-600 to-cyan-600 group-hover:opacity-90 transition-opacity" />
              <motion.div
                className="absolute inset-0 bg-gradient-to-r from-purple-600 via-pink-600 to-cyan-600 opacity-0 group-hover:opacity-100 blur-xl transition-opacity"
                animate={{ backgroundPosition: ['0%', '100%', '0%'] }}
                transition={{ duration: 3, repeat: Infinity }}
                style={{ backgroundSize: '200%' }}
              />

              {/* Button content */}
              <span className="relative z-10 flex items-center justify-center gap-3">
                {launching ? (
                  <>
                    <motion.div
                      className="w-6 h-6 border-3 border-white/30 border-t-white rounded-full"
                      animate={{ rotate: 360 }}
                      transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                    />
                    Lancement en cours...
                  </>
                ) : (
                  <>
                    <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M8 5v14l11-7z" />
                    </svg>
                    JOUER
                  </>
                )}
              </span>
            </motion.button>

            {/* Info */}
            <motion.p
              className="text-center text-gray-500 text-sm mt-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              Minecraft 1.20.4 avec Fabric
            </motion.p>
          </div>
        </main>

        {/* Footer */}
        <footer className="px-8 py-4 bg-black/30 backdrop-blur-xl border-t border-white/5">
          <div className="flex items-center justify-between text-sm text-gray-500">
            <span>hegemonia.fr</span>
            <span>Discord: discord.gg/hegemonia</span>
          </div>
        </footer>
      </div>

      {/* Settings Modal */}
      <AnimatePresence>
        {showSettings && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowSettings(false)}
          >
            <motion.div
              className="w-full max-w-md bg-[#12121a] border border-white/10 rounded-2xl p-6 m-4"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-xl font-bold mb-6">Parametres</h3>

              <div className="space-y-6">
                {/* RAM Setting */}
                <div>
                  <label className="block text-sm text-gray-400 mb-2">
                    Memoire RAM: {ram / 1024} GB
                  </label>
                  <input
                    type="range"
                    min="2048"
                    max="16384"
                    step="1024"
                    value={ram}
                    onChange={(e) => setRam(parseInt(e.target.value))}
                    className="w-full h-2 bg-white/10 rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-4 [&::-webkit-slider-thumb]:h-4 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-purple-500"
                  />
                  <div className="flex justify-between text-xs text-gray-500 mt-1">
                    <span>2 GB</span>
                    <span>16 GB</span>
                  </div>
                </div>

                {/* Game Dir */}
                <div>
                  <label className="block text-sm text-gray-400 mb-2">Dossier du jeu</label>
                  <div className="px-4 py-3 bg-white/5 rounded-xl text-sm text-gray-300 font-mono">
                    %APPDATA%\.hegemonia
                  </div>
                </div>

                {/* Java Info */}
                <div>
                  <label className="block text-sm text-gray-400 mb-2">Java</label>
                  <p className="text-sm text-gray-300">
                    Le launcher utilise automatiquement le Java de Minecraft.
                  </p>
                </div>
              </div>

              <motion.button
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => {
                  localStorage.setItem('hegemonia_ram', ram.toString());
                  setShowSettings(false);
                  toast.success('Parametres sauvegardes');
                }}
                className="w-full mt-6 py-3 bg-purple-600 hover:bg-purple-500 rounded-xl font-semibold transition-colors"
              >
                Fermer
              </motion.button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default App;
