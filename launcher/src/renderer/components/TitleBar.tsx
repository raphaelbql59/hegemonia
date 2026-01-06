import React from 'react';
import '../styles/TitleBar.css';

const TitleBar: React.FC = () => {
  const handleMinimize = () => {
    window.api.minimize();
  };

  const handleMaximize = () => {
    window.api.maximize();
  };

  const handleClose = () => {
    window.api.close();
  };

  return (
    <div className="titlebar">
      <div className="titlebar-drag">
        <div className="titlebar-title">
          <span className="title-logo">HEGEMONIA</span>
          <span className="title-version">v1.0.0</span>
        </div>
      </div>
      <div className="titlebar-controls">
        <button className="titlebar-button" onClick={handleMinimize}>
          <svg width="10" height="1" viewBox="0 0 10 1">
            <rect fill="currentColor" width="10" height="1"/>
          </svg>
        </button>
        <button className="titlebar-button" onClick={handleMaximize}>
          <svg width="10" height="10" viewBox="0 0 10 10">
            <rect fill="currentColor" width="9" height="9" x="0.5" y="0.5" strokeWidth="1" stroke="currentColor" fillOpacity="0"/>
          </svg>
        </button>
        <button className="titlebar-button close" onClick={handleClose}>
          <svg width="10" height="10" viewBox="0 0 10 10">
            <path fill="currentColor" d="M0 0 L10 10 M10 0 L0 10" stroke="currentColor" strokeWidth="1.5"/>
          </svg>
        </button>
      </div>
    </div>
  );
};

export default TitleBar;
