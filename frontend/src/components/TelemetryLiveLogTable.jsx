import React, { useState, useEffect } from 'react';
import { fetchVehicleTelemetry } from '../services/apiService';

export default function TelemetryLiveLogTable({ vehicleId, refreshTrigger }) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const loadData = async (targetPage = 0) => {
    if (!vehicleId) return;

    setLoading(true);
    setError(null);

    const res = await fetchVehicleTelemetry(vehicleId, targetPage, 12);
    setLoading(false);

    if (res.success && res.data) {
      setRecords(res.data.content || []);
      setPage(res.data.page);
      setTotalPages(res.data.totalPages || 1);
      setTotalElements(res.data.totalElements || 0);
    } else {
      setError(res.error || 'Failed to load telemetry records.');
    }
  };

  useEffect(() => {
    loadData(page);
  }, [vehicleId, refreshTrigger, page]);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'VALID':
        return <span className="badge badge-success" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>VALID</span>;
      case 'SPEED_ANOMALY':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>SPEED SPIKE</span>;
      case 'GPS_OUT_OF_BOUNDS':
        return <span className="badge badge-warning" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>GPS ANOMALY</span>;
      case 'ACCELERATION_ANOMALY':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>G-SPIKE</span>;
      case 'TIMESTAMP_ANOMALY':
      default:
        return <span className="badge badge-info" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>{status}</span>;
    }
  };

  const getAccelIndicator = (g) => {
    if (g <= -0.35) {
      return (
        <span style={{ color: 'var(--color-danger)', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
          {g > 0 ? `+${g}` : g}G <span style={{ fontSize: '0.7rem' }}>[HARSH BRAKE]</span>
        </span>
      );
    }
    if (g >= 0.30) {
      return (
        <span style={{ color: 'var(--color-warning)', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
          +{g}G <span style={{ fontSize: '0.7rem' }}>[RAPID ACCEL]</span>
        </span>
      );
    }
    return (
      <span style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)' }}>
        {g > 0 ? `+${g}` : g}G
      </span>
    );
  };

  if (!vehicleId) {
    return null;
  }

  return (
    <div className="card" style={{ marginTop: '1.5rem' }}>
      <div className="card-header">
        <h3 className="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="8" y1="6" x2="21" y2="6" />
            <line x1="8" y1="12" x2="21" y2="12" />
            <line x1="8" y1="18" x2="21" y2="18" />
            <line x1="3" y1="6" x2="3.01" y2="6" />
            <line x1="3" y1="12" x2="3.01" y2="12" />
            <line x1="3" y1="18" x2="3.01" y2="18" />
          </svg>
          Ingested Telematics Audit Stream
        </h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {totalElements} total pings logged
          </span>
          <button
            className="btn btn-secondary"
            onClick={() => loadData(page)}
            disabled={loading}
            style={{ fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}
          >
            {loading ? 'Refreshing...' : 'Refresh'}
          </button>
        </div>
      </div>

      {loading && records.length === 0 && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          <span className="spinner" style={{ marginRight: '0.5rem' }} /> Loading time-series telemetry...
        </div>
      )}

      {error && (
        <div style={{ padding: '1rem', color: 'var(--color-danger)', fontSize: '0.88rem' }}>
          {error}
        </div>
      )}

      {!loading && records.length === 0 && !error && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          No telematics data ingested yet. Click <strong>Stream Telemetry Pings</strong> above to begin data flow.
        </div>
      )}

      {records.length > 0 && (
        <>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Timestamp</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Coordinates</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Speed (km/h)</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Accel / Brake</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Engine RPM</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Calibration Status</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Diagnostic Notes</th>
                </tr>
              </thead>
              <tbody>
                {records.map(r => (
                  <tr
                    key={r.id}
                    style={{
                      borderBottom: '1px solid rgba(255, 255, 255, 0.03)',
                      background: r.validationStatus !== 'VALID' ? 'rgba(239, 68, 68, 0.05)' : 'transparent'
                    }}
                  >
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                      {new Date(r.timestamp).toLocaleTimeString()}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', fontSize: '0.8rem' }}>
                      {r.latitude.toFixed(4)}, {r.longitude.toFixed(4)}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{
                          fontFamily: 'var(--font-mono)',
                          fontWeight: 700,
                          color: r.speedKmh >= 120 ? 'var(--color-danger)' : 'var(--text-primary)'
                        }}>
                          {r.speedKmh.toFixed(1)}
                        </span>
                        <div style={{
                          width: '45px',
                          height: '5px',
                          background: 'rgba(255, 255, 255, 0.1)',
                          borderRadius: '3px',
                          overflow: 'hidden'
                        }}>
                          <div style={{
                            width: `${Math.min(100, (r.speedKmh / 160) * 100)}%`,
                            height: '100%',
                            background: r.speedKmh >= 120 ? 'var(--color-danger)' : 'var(--accent-primary)'
                          }} />
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      {getAccelIndicator(r.accelerationG)}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)' }}>
                      {r.engineRpm ? `${r.engineRpm} RPM` : '-'}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      {getStatusBadge(r.validationStatus)}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', color: 'var(--text-muted)', fontSize: '0.78rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={r.validationNotes}>
                      {r.validationNotes || 'Clean'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid var(--border-subtle)', fontSize: '0.85rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>
                Page {page + 1} of {totalPages}
              </span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  className="btn btn-secondary"
                  disabled={page === 0 || loading}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem' }}
                >
                  Previous
                </button>
                <button
                  className="btn btn-secondary"
                  disabled={page >= totalPages - 1 || loading}
                  onClick={() => setPage(p => p + 1)}
                  style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem' }}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
