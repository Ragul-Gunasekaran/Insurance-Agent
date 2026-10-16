import React from 'react';
import { useAuth } from '../context/AuthContext';

export default function Navbar({ activeTab, onTabChange, onOpenAuth }) {
  const { user, isAuthenticated, role, isInsurer, logout } = useAuth();

  return (
    <nav className="navbar">
      <div className="container nav-container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
        <div className="brand-wrapper" style={{ cursor: 'pointer' }} onClick={() => onTabChange('overview')}>
          <div className="brand-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </div>
          <div>
            <div className="brand-title">Usage-Based Insurance</div>
          </div>
          <span className="brand-badge">PS-008 Security</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
            <button
              className={`nav-tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => onTabChange('overview')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7" />
                <rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" />
                <rect x="3" y="14" width="7" height="7" />
              </svg>
              Overview
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'vehicles' ? 'active' : ''}`}
              onClick={() => onTabChange('vehicles')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="1" y="3" width="15" height="13" />
                <polygon points="16 8 20 8 23 11 23 16 16 16 8" />
              </svg>
              Vehicles
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'telemetry' ? 'active' : ''}`}
              onClick={() => onTabChange('telemetry')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
              </svg>
              IoT Telematics
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'trips' ? 'active' : ''}`}
              onClick={() => onTabChange('trips')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
              </svg>
              Trips
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'risk' ? 'active' : ''}`}
              onClick={() => onTabChange('risk')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
              Risk Scores
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'pricing' ? 'active' : ''}`}
              onClick={() => onTabChange('pricing')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
                <path d="M12 18V6" />
              </svg>
              Pricing
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'scorecard' ? 'active' : ''}`}
              onClick={() => onTabChange('scorecard')}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="8.5" cy="7" r="4" />
              </svg>
              Scorecard
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'insurer' ? 'active' : ''}`}
              onClick={() => onTabChange('insurer')}
              style={isInsurer ? { borderColor: 'rgba(139, 92, 246, 0.4)' } : {}}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M3 3v18h18" />
                <path d="M18 17V9" />
                <path d="M13 17V5" />
                <path d="M8 17v-3" />
              </svg>
              Insurer {isInsurer ? '🔒' : ''}
            </button>
          </div>

          {/* Authentication controls */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', paddingLeft: '0.5rem', borderLeft: '1px solid var(--border-subtle)' }}>
            {isAuthenticated ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', lineHeight: 1.2 }}>
                  <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                    {user?.email}
                  </span>
                  <span
                    style={{
                      fontSize: '0.65rem',
                      fontWeight: 700,
                      letterSpacing: '0.05em',
                      textTransform: 'uppercase',
                      padding: '0.15rem 0.45rem',
                      borderRadius: '4px',
                      marginTop: '0.15rem',
                      background: isInsurer ? 'rgba(139, 92, 246, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                      color: isInsurer ? '#c084fc' : '#34d399',
                      border: `1px solid ${isInsurer ? 'rgba(139, 92, 246, 0.4)' : 'rgba(16, 185, 129, 0.4)'}`
                    }}
                  >
                    {role || 'USER'}
                  </span>
                </div>
                <button
                  className="btn btn-outline"
                  onClick={logout}
                  title="Sign out of your session"
                  style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  Sign Out
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <button
                  className="btn btn-primary"
                  onClick={() => onOpenAuth('login')}
                  style={{ padding: '0.45rem 0.9rem', fontSize: '0.82rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                  Sign In
                </button>
                <button
                  className="btn btn-outline"
                  onClick={() => onOpenAuth('register')}
                  style={{ padding: '0.45rem 0.85rem', fontSize: '0.82rem' }}
                >
                  Register
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
