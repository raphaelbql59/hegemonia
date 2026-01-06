import React, { useState, useEffect } from 'react';
import TitleBar from './components/TitleBar';
import Sidebar from './components/Sidebar';
import HomePage from './pages/HomePage';
import NewsPage from './pages/NewsPage';
import MapPage from './pages/MapPage';
import SettingsPage from './pages/SettingsPage';
import './styles/App.css';

type Page = 'home' | 'news' | 'map' | 'settings';

const App: React.FC = () => {
  const [currentPage, setCurrentPage] = useState<Page>('home');
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    // Check for updates on launch
    checkUpdates();
  }, []);

  const checkUpdates = async () => {
    try {
      const result = await window.api.checkUpdates();
      if (result.needsUpdate) {
        console.log('Updates available:', result.mods);
      }
      setIsReady(true);
    } catch (error) {
      console.error('Failed to check updates:', error);
      setIsReady(true);
    }
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <HomePage />;
      case 'news':
        return <NewsPage />;
      case 'map':
        return <MapPage />;
      case 'settings':
        return <SettingsPage />;
      default:
        return <HomePage />;
    }
  };

  return (
    <div className="app">
      <TitleBar />
      <div className="app-content">
        <Sidebar currentPage={currentPage} onNavigate={setCurrentPage} />
        <main className="main-content">
          {isReady ? renderPage() : <div className="loading">Loading...</div>}
        </main>
      </div>
    </div>
  );
};

export default App;
