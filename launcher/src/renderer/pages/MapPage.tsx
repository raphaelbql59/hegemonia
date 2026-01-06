import React from 'react';
import '../styles/MapPage.css';

const MapPage: React.FC = () => {
  return (
    <div className="map-page">
      <h1 className="page-title">World Map</h1>
      <div className="map-container">
        <div className="map-placeholder">
          <p>🗺️</p>
          <p>Interactive world map coming soon...</p>
          <p className="map-description">
            View territories, nations, and active conflicts in real-time
          </p>
        </div>
      </div>
    </div>
  );
};

export default MapPage;
