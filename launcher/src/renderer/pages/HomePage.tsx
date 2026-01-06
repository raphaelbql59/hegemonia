import React, { useState } from 'react';
import '../styles/HomePage.css';

const HomePage: React.FC = () => {
  const [isLaunching, setIsLaunching] = useState(false);
  const [status, setStatus] = useState('');
  const [progress, setProgress] = useState(0);

  const handleLaunch = async () => {
    try {
      setIsLaunching(true);
      setStatus('Checking for updates...');

      // Check for mod updates
      const updates = await window.api.checkUpdates();

      if (updates.needsUpdate) {
        setStatus('Downloading mods...');
        await window.api.downloadMods();
      }

      setStatus('Launching Minecraft...');

      const config = await window.api.getConfig();
      await window.api.launchGame({
        username: config.username || 'Player',
        ram: config.ram || 4096
      });

      setStatus('Game launched!');

      if (!config.keepLauncherOpen) {
        setTimeout(() => {
          window.api.close();
        }, 2000);
      }
    } catch (error) {
      console.error('Launch error:', error);
      setStatus('Launch failed! Check console.');
    } finally {
      setIsLaunching(false);
    }
  };

  // Listen to download progress
  React.useEffect(() => {
    window.api.onDownloadProgress((progress: number) => {
      setProgress(progress);
    });

    window.api.onGameStatus((status: string) => {
      setStatus(status);
    });
  }, []);

  return (
    <div className="home-page">
      <div className="hero-section">
        <h1 className="hero-title">HEGEMONIA</h1>
        <p className="hero-subtitle">Earth Nations • RP • Warfare • Economy</p>
      </div>

      <div className="launch-section">
        <button
          className="launch-button"
          onClick={handleLaunch}
          disabled={isLaunching}
        >
          {isLaunching ? 'LAUNCHING...' : 'PLAY NOW'}
        </button>

        {isLaunching && (
          <div className="launch-status">
            <p>{status}</p>
            {progress > 0 && progress < 100 && (
              <div className="progress-bar">
                <div
                  className="progress-fill"
                  style={{ width: `${progress}%` }}
                />
              </div>
            )}
          </div>
        )}
      </div>

      <div className="server-info">
        <div className="info-card">
          <h3>Players Online</h3>
          <p className="info-value">0 / 100</p>
        </div>
        <div className="info-card">
          <h3>Active Nations</h3>
          <p className="info-value">0</p>
        </div>
        <div className="info-card">
          <h3>Active Wars</h3>
          <p className="info-value">0</p>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
