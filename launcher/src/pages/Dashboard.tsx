import { useState } from 'react';
import { motion } from 'framer-motion';
import { toast, Toaster } from 'sonner';
import { useQuery } from '@tanstack/react-query';
import { invoke } from '@tauri-apps/api/tauri';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

export default function Dashboard() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [launching, setLaunching] = useState(false);

  // Fetch server status
  const { data: serverStatus } = useQuery({
    queryKey: ['serverStatus'],
    queryFn: async () => {
      const response = await api.get<any>('/server/status');
      return response.data;
    },
    refetchInterval: 30000, // Refresh every 30s
  });

  // Fetch news
  const { data: news, isLoading: newsLoading } = useQuery({
    queryKey: ['news'],
    queryFn: async () => {
      const response = await api.get<any[]>('/news');
      return response.data;
    },
  });

  // Fetch player stats
  const { data: stats } = useQuery({
    queryKey: ['stats', user?.minecraftUuid],
    queryFn: async () => {
      if (!user?.minecraftUuid) return null;
      const response = await api.get<any>(`/stats/${user.minecraftUuid}`);
      return response.data;
    },
    enabled: !!user?.minecraftUuid,
  });

  const handleLogout = () => {
    logout();
    navigate('/login');
    toast.success('Déconnecté avec succès');
  };

  const handleLaunch = async () => {
    if (!user) return;

    setLaunching(true);
    toast.loading('Lancement de Minecraft...');

    try {
      // Check if Minecraft is installed
      const installed = await invoke('check_minecraft_installed');
      if (!installed) {
        toast.error('Minecraft n\'est pas installé. Installez Minecraft Java Edition 1.20.4');
        return;
      }

      // Launch Minecraft
      await invoke('launch_minecraft', {
        username: user.username,
        uuid: user.minecraftUuid || user.uuid,
        serverIp: '51.75.31.173',
        serverPort: '25577',
      });

      toast.success('Minecraft lancé avec succès !');
    } catch (error: any) {
      toast.error(error || 'Erreur lors du lancement');
    } finally {
      setLaunching(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-dark-950 via-dark-900 to-dark-950">
      <Toaster position="top-right" richColors />

      {/* Header */}
      <header className="glass border-b border-dark-700">
        <div className="container mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h1 className="text-2xl font-display font-bold text-gradient">HEGEMONIA</h1>
            <span className="text-sm text-gray-400">Launcher v1.0.0</span>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex items-center gap-3">
              {user?.avatarUrl ? (
                <img
                  src={user.avatarUrl}
                  alt={user.username}
                  className="w-10 h-10 rounded-full border-2 border-primary-500"
                />
              ) : (
                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center text-white font-bold">
                  {user?.username.charAt(0).toUpperCase()}
                </div>
              )}
              <div>
                <p className="text-sm font-medium">{user?.username}</p>
                <p className="text-xs text-gray-400">{user?.role}</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="px-4 py-2 text-sm text-gray-400 hover:text-white transition-colors"
            >
              Déconnexion
            </button>
          </div>
        </div>
      </header>

      {/* Main content */}
      <div className="container mx-auto px-6 py-8">
        <div className="grid grid-cols-12 gap-6">
          {/* Left panel - News */}
          <div className="col-span-3">
            <div className="glass rounded-xl p-6">
              <h2 className="text-xl font-display font-bold mb-4">Actualités</h2>
              {newsLoading ? (
                <div className="space-y-4">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="animate-pulse">
                      <div className="h-4 bg-dark-700 rounded mb-2"></div>
                      <div className="h-3 bg-dark-700 rounded w-3/4"></div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {news?.slice(0, 5).map((item: any) => (
                    <motion.div
                      key={item.id}
                      whileHover={{ x: 4 }}
                      className="border-l-2 border-primary-500 pl-3 py-2 cursor-pointer hover:bg-dark-800 rounded transition-colors"
                    >
                      <h3 className="text-sm font-semibold mb-1">{item.title}</h3>
                      <p className="text-xs text-gray-400">
                        {new Date(item.published_at).toLocaleDateString('fr-FR')}
                      </p>
                    </motion.div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Center panel - Play button */}
          <div className="col-span-6">
            <div className="glass rounded-xl p-8">
              {/* Server status */}
              <div className="mb-8 text-center">
                <div className="inline-flex items-center gap-2 px-4 py-2 bg-dark-900 rounded-full">
                  <div className={`w-3 h-3 rounded-full ${serverStatus?.online ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
                  <span className="text-sm">
                    {serverStatus?.online ? (
                      <>Serveur en ligne • {serverStatus.players.online}/{serverStatus.players.max} joueurs</>
                    ) : (
                      'Serveur hors ligne'
                    )}
                  </span>
                </div>
              </div>

              {/* Play button */}
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={handleLaunch}
                disabled={launching || !serverStatus?.online}
                className="w-full py-6 px-8 bg-gradient-to-r from-primary-500 to-primary-600 text-white text-2xl font-display font-bold rounded-xl shadow-2xl glow disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                {launching ? 'LANCEMENT...' : 'JOUER'}
              </motion.button>

              <p className="text-center text-sm text-gray-400 mt-4">
                Version Minecraft : 1.20.4
              </p>

              {/* MOTD */}
              {serverStatus?.motd && (
                <div className="mt-6 p-4 bg-dark-900 rounded-lg">
                  <p className="text-sm text-center text-gray-300">{serverStatus.motd}</p>
                </div>
              )}
            </div>
          </div>

          {/* Right panel - Stats */}
          <div className="col-span-3">
            <div className="glass rounded-xl p-6">
              <h2 className="text-xl font-display font-bold mb-4">Statistiques</h2>
              {stats ? (
                <div className="space-y-4">
                  {/* Nation */}
                  {stats.nation && (
                    <div>
                      <p className="text-xs text-gray-400 mb-1">Nation</p>
                      <p className="text-sm font-semibold" style={{ color: stats.nation.color }}>
                        [{stats.nation.tag}] {stats.nation.name}
                      </p>
                      <p className="text-xs text-gray-500">{stats.nation.role}</p>
                    </div>
                  )}

                  {/* Playtime */}
                  <div>
                    <p className="text-xs text-gray-400 mb-1">Temps de jeu</p>
                    <p className="text-sm font-semibold">{stats.player.playtime.formatted}</p>
                  </div>

                  {/* Combat */}
                  <div>
                    <p className="text-xs text-gray-400 mb-1">Combat</p>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-green-400">{stats.combat.kills} kills</span>
                      <span className="text-red-400">{stats.combat.deaths} deaths</span>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">K/D: {stats.combat.kdRatio}</p>
                  </div>

                  {/* Economy */}
                  <div>
                    <p className="text-xs text-gray-400 mb-1">Économie</p>
                    <p className="text-sm font-semibold">{stats.player.balance.toFixed(2)} €</p>
                    <p className="text-xs text-gray-500">Réputation: {stats.player.reputation}</p>
                  </div>
                </div>
              ) : (
                <p className="text-sm text-gray-400">Aucune donnée disponible</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
