import React from 'react';

export default function Navbar({ activeTab, onTabChange }) {
  return (
    <nav className="navbar">
      <div className="container nav-container">
        <div className="brand-wrapper">
          <div className="brand-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </div>
          <div>
            <div className="brand-title">Usage-Based Insurance</div>
          </div>
          <span className="brand-badge">PS-008 Prototype</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{ display: 'flex', gap: '0.4rem' }}>
            <button
              className={`nav-tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => onTabChange('overview')}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7" />
                <rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" />
                <rect x="3" y="14" width="7" height="7" />
              </svg>
              Overview & Health
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'vehicles' ? 'active' : ''}`}
              onClick={() => onTabChange('vehicles')}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="1" y="3" width="15" height="13" />
                <polygon points="16 8 20 8 23 11 23 16 16 16 8" />
                <circle cx="5.5" cy="18.5" r="2.5" />
                <circle cx="18.5" cy="18.5" r="2.5" />
              </svg>
              Vehicle Fleet (Step 2)
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'telemetry' ? 'active' : ''}`}
              onClick={() => onTabChange('telemetry')}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
              </svg>
              IoT Telematics (Step 3)
            </button>
          </div>

          <div className="nav-status">
            <span className="pulsing-dot" />
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
              Frontend Active <span style={{ color: 'var(--text-muted)' }}>(5173)</span>
            </span>
          </div>
        </div>
      </div>
    </nav>
  );
}
