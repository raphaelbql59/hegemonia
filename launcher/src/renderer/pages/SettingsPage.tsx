import React, { useState, useEffect } from 'react';
import '../styles/SettingsPage.css';

const SettingsPage: React.FC = () => {
  const [config, setConfig] = useState({
    username: '',
    ram: 4096,
    autoUpdate: true,
    keepLauncherOpen: false,
    apiUrl: 'http://localhost:3000'
  });

  useEffect(() => {
    loadConfig();
  }, []);

  const loadConfig = async () => {
    const loadedConfig = await window.api.getConfig();
    setConfig(loadedConfig);
  };

  const handleSave = async () => {
    await window.api.setConfig(config);
    alert('Settings saved!');
  };

  return (
    <div className="settings-page">
      <h1 className="page-title">Settings</h1>

      <div className="settings-section">
        <h2>Account</h2>
        <div className="setting-item">
          <label>Username</label>
          <input
            type="text"
            value={config.username}
            onChange={(e) => setConfig({ ...config, username: e.target.value })}
            placeholder="Enter your username"
          />
        </div>
      </div>

      <div className="settings-section">
        <h2>Performance</h2>
        <div className="setting-item">
          <label>Allocated RAM (MB)</label>
          <input
            type="number"
            value={config.ram}
            onChange={(e) => setConfig({ ...config, ram: parseInt(e.target.value) })}
            min={2048}
            max={16384}
            step={512}
          />
          <small>Recommended: 4096 MB (4 GB)</small>
        </div>
      </div>

      <div className="settings-section">
        <h2>Launcher</h2>
        <div className="setting-item">
          <label>
            <input
              type="checkbox"
              checked={config.autoUpdate}
              onChange={(e) => setConfig({ ...config, autoUpdate: e.target.checked })}
            />
            Automatically update mods
          </label>
        </div>
        <div className="setting-item">
          <label>
            <input
              type="checkbox"
              checked={config.keepLauncherOpen}
              onChange={(e) => setConfig({ ...config, keepLauncherOpen: e.target.checked })}
            />
            Keep launcher open after game starts
          </label>
        </div>
      </div>

      <div className="settings-section">
        <h2>Advanced</h2>
        <div className="setting-item">
          <label>API URL</label>
          <input
            type="text"
            value={config.apiUrl}
            onChange={(e) => setConfig({ ...config, apiUrl: e.target.value })}
          />
        </div>
      </div>

      <button className="save-button" onClick={handleSave}>
        Save Settings
      </button>
    </div>
  );
};

export default SettingsPage;
