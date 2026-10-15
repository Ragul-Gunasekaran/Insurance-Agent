import React from 'react';
import { API_BASE_URL } from '../services/apiService';

export default function BackendStatusCard({
  status,
  healthData,
  infoData,
  errorMessage,
  lastChecked,
  onRefresh
}) {
  const isLoading = status === 'loading';
  const isSuccess = status === 'success';
  const isError = status === 'error';

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
          </svg>
          Backend & Database Connectivity
        </h2>

        <div>
          {isLoading && (
            <span className="badge badge-info">
              <span className="spinner" /> Checking...
            </span>
          )}
          {isSuccess && (
            <span className="badge badge-success">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              Backend Connected
            </span>
          )}
          {isError && (
            <span className="badge badge-danger">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
              Connection Failed
            </span>
          )}
        </div>
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', marginBottom: '1.25rem' }}>
        Verifies live REST communication between this React SPA and the Spring Boot backend service,
        including real-time MySQL database connectivity testing.
      </p>

      {/* Connection Endpoint Metric */}
      <div className="metrics-list">
        <div className="metric-row">
          <span className="metric-label">Target REST API Base</span>
          <span className="metric-value">{API_BASE_URL}</span>
        </div>

        <div className="metric-row">
          <span className="metric-label">Frontend Status</span>
          <span className="badge badge-success" style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem' }}>
            Running (Vite 5)
          </span>
        </div>

        {isSuccess && healthData && (
          <>
            <div className="metric-row">
              <span className="metric-label">Backend Service</span>
              <span className="metric-value" style={{ color: 'var(--color-success)' }}>
                {healthData.application} (v{healthData.version})
              </span>
            </div>

            <div className="metric-row">
              <span className="metric-label">MySQL Database Status</span>
              {healthData.database?.status === 'CONNECTED' ? (
                <span className="badge badge-success" style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem' }}>
                  Connected ({healthData.database.catalog || 'usage_based_insurance'})
                </span>
              ) : (
                <span className="badge badge-warning" style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem' }}>
                  Disconnected (MySQL Offline)
                </span>
              )}
            </div>

            {healthData.database?.message && healthData.database?.status !== 'CONNECTED' && (
              <div style={{
                background: 'var(--color-warning-bg)',
                border: '1px solid var(--color-warning-border)',
                borderRadius: 'var(--radius-sm)',
                padding: '0.65rem 0.85rem',
                fontSize: '0.8rem',
                color: 'var(--color-warning)',
                marginTop: '0.5rem'
              }}>
                <strong>Database Note:</strong> {healthData.database.message}
              </div>
            )}

            <div className="metric-row">
              <span className="metric-label">Java Runtime</span>
              <span className="metric-value">
                JDK {healthData.runtime?.javaVersion} ({healthData.runtime?.jvmVendor})
              </span>
            </div>

            <div className="metric-row">
              <span className="metric-label">Backend Uptime</span>
              <span className="metric-value">{healthData.runtime?.uptimeSeconds}s</span>
            </div>
          </>
        )}

        {isError && (
          <div style={{
            background: 'var(--color-danger-bg)',
            border: '1px solid var(--color-danger-border)',
            borderRadius: 'var(--radius-sm)',
            padding: '0.85rem 1rem',
            marginTop: '0.5rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-danger)', fontWeight: 600, fontSize: '0.9rem', marginBottom: '0.35rem' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              Backend Unreachable
            </div>
            <p style={{ color: '#fca5a5', fontSize: '0.82rem', margin: '0 0 0.5rem' }}>
              {errorMessage}
            </p>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.78rem' }}>
              Troubleshooting: Ensure the Spring Boot backend is running via <code>.\mvnw.cmd spring-boot:run</code> on port 8080.
            </div>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid var(--border-subtle)' }}>
        <button
          className="btn btn-primary"
          onClick={onRefresh}
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <span className="spinner" />
              Pinging Backend...
            </>
          ) : (
            <>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="23 4 23 10 17 10" />
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
              </svg>
              Check Backend Connection
            </>
          )}
        </button>

        {lastChecked && (
          <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
            Last checked: {lastChecked}
          </span>
        )}
      </div>
    </div>
  );
}
