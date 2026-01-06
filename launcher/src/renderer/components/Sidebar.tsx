import React from 'react';
import '../styles/Sidebar.css';

interface SidebarProps {
  currentPage: string;
  onNavigate: (page: any) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ currentPage, onNavigate }) => {
  const menuItems = [
    { id: 'home', icon: '🏠', label: 'Home' },
    { id: 'news', icon: '📰', label: 'News' },
    { id: 'map', icon: '🗺️', label: 'Map' },
    { id: 'settings', icon: '⚙️', label: 'Settings' }
  ];

  return (
    <nav className="sidebar">
      <div className="sidebar-menu">
        {menuItems.map((item) => (
          <button
            key={item.id}
            className={`sidebar-item ${currentPage === item.id ? 'active' : ''}`}
            onClick={() => onNavigate(item.id)}
          >
            <span className="sidebar-icon">{item.icon}</span>
            <span className="sidebar-label">{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default Sidebar;
