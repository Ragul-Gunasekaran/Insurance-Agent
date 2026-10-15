import React, { useState, useEffect } from 'react';
import { simulateTelemetry, clearVehicleTelemetry, fetchVehicles } from '../services/apiService';

const PROFILES = [
  {
    id: 'SAFE_COMMUTE',
    title: 'Safe Commute',
    badge: 'Low Risk',
    badgeClass: 'badge-success',
    desc: 'Smooth gradual accelerations, gentle braking (-0.1G to -0.2G), cruising at 40–65 km/h.',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <path d="M9 12l2 2 4-4" />
      </svg>
    )
  },
  {
    id: 'AGGRESSIVE_DRIVING',
    title: 'Aggressive Driving',
    badge: 'High Risk',
    badgeClass: 'badge-danger',
    desc: 'Repeated harsh braking (< -0.4G), sudden accelerations (> +0.35G), erratic stop-and-go.',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
    )
  },
  {
    id: 'SPEEDING_HIGHWAY',
    title: 'Speeding Highway',
    badge: 'Speeding Risk',
    badgeClass: 'badge-warning',
    desc: 'Sustained highway expressway run exceeding 125–150 km/h with high engine RPM.',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <circle cx="12" cy="12" r="10" />
        <polyline points="12 6 12 12 16 14" />
      </svg>
    )
  },
  {
    id: 'ANOMALOUS_SENSOR',
    title: 'Sensor Glitch & Cleaning',
    badge: 'Anomaly Test',
    badgeClass: 'badge-info',
    desc: 'Injects corrupt speed bursts (340 km/h) & GPS drift to test real-time calibration filters.',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
        <line x1="12" y1="9" x2="12" y2="13" />
        <line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
    )
  }
];

export default function TelemetrySimulatorCard({ selectedVehicleId, onVehicleChange, onSimulationCompleted }) {
  const [vehicles, setVehicles] = useState([]);
  const [selectedProfile, setSelectedProfile] = useState('SAFE_COMMUTE');
  const [pointCount, setPointCount] = useState(25);
  const [loading, setLoading] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [resultMessage, setResultMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  // Load registered vehicles for dropdown
  useEffect(() => {
    let mounted = true;
    fetchVehicles(0, 50).then(res => {
      if (mounted && res.success && res.data) {
        setVehicles(res.data.content || []);
        if (!selectedVehicleId && res.data.content?.length > 0) {
          onVehicleChange(res.data.content[0].id);
        }
      }
    });
    return () => { mounted = false; };
  }, [selectedVehicleId]);

  const handleSimulate = async () => {
    if (!selectedVehicleId) {
      setErrorMessage('Please select a target vehicle from your fleet.');
      return;
    }

    setLoading(true);
    setResultMessage(null);
    setErrorMessage(null);

    const res = await simulateTelemetry({
      vehicleId: Number(selectedVehicleId),
      profile: selectedProfile,
      pointCount: Number(pointCount)
    });

    setLoading(false);

    if (res.success && res.data) {
      setResultMessage(res.data.message || `Successfully ingested ${res.data.totalIngested} telematics readings.`);
      if (onSimulationCompleted) {
        onSimulationCompleted(res.data);
      }
    } else {
      setErrorMessage(res.error || 'Failed to simulate telematics ingestion.');
    }
  };

  const handleClear = async () => {
    if (!selectedVehicleId) return;
    if (!window.confirm('Clear all telemetry points for this vehicle?')) return;

    setClearing(true);
    setResultMessage(null);
    setErrorMessage(null);

    const res = await clearVehicleTelemetry(selectedVehicleId);
    setClearing(false);

    if (res.success) {
      setResultMessage('Telemetry history cleared for selected vehicle.');
      if (onSimulationCompleted) {
        onSimulationCompleted(null);
      }
    } else {
      setErrorMessage(res.error || 'Failed to clear telemetry.');
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
          </svg>
          IoT Telematics Route Simulator (Step 3)
        </h3>
        <span className="badge badge-info">Real-Time Ingestion</span>
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.25rem' }}>
        Inject synthetic, physics-calibrated GPS coordinates, velocities, and acceleration vectors to test
        telemetry cleaning, anomaly detection, and driving risk calculations.
      </p>

      {/* Target Vehicle Selector */}
      <div style={{ marginBottom: '1.25rem' }}>
        <label className="form-label">Target Vehicle Fleet</label>
        {vehicles.length === 0 ? (
          <div style={{ fontSize: '0.85rem', color: 'var(--color-warning)' }}>
            No vehicles registered yet. Switch to the <strong>Vehicle Fleet</strong> tab to onboard a vehicle first.
          </div>
        ) : (
          <select
            className="form-input"
            value={selectedVehicleId || ''}
            onChange={(e) => onVehicleChange(Number(e.target.value))}
            disabled={loading || clearing}
          >
            {vehicles.map(v => (
              <option key={v.id} value={v.id}>
                {v.vehicleNumber} &bull; {v.make} {v.model} ({v.manufacturingYear}) - {v.verificationStatus}
              </option>
            ))}
          </select>
        )}
      </div>

      {/* Driving Profile Selection */}
      <div style={{ marginBottom: '1.25rem' }}>
        <label className="form-label">Simulation Driving Profile</label>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.75rem' }}>
          {PROFILES.map(p => {
            const isSelected = selectedProfile === p.id;
            return (
              <div
                key={p.id}
                onClick={() => setSelectedProfile(p.id)}
                style={{
                  padding: '1rem',
                  borderRadius: 'var(--radius-sm)',
                  background: isSelected ? 'rgba(99, 102, 241, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                  border: isSelected ? '1px solid var(--accent-primary)' : '1px solid var(--border-subtle)',
                  cursor: 'pointer',
                  transition: 'var(--transition)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <div style={{ color: isSelected ? 'var(--accent-primary)' : 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    {p.icon}
                    <strong style={{ fontSize: '0.92rem' }}>{p.title}</strong>
                  </div>
                  <span className={`badge ${p.badgeClass}`} style={{ fontSize: '0.7rem', padding: '0.15rem 0.45rem' }}>
                    {p.badge}
                  </span>
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                  {p.desc}
                </p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Point Count Selector & Actions */}
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border-subtle)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <label className="form-label" style={{ margin: 0, fontSize: '0.88rem' }}>Points to Stream:</label>
          <select
            className="form-input"
            style={{ width: 'auto', padding: '0.45rem 0.8rem' }}
            value={pointCount}
            onChange={(e) => setPointCount(Number(e.target.value))}
            disabled={loading || clearing}
          >
            <option value={15}>15 Samples (~1 min route)</option>
            <option value={25}>25 Samples (~2 min route)</option>
            <option value={50}>50 Samples (~4 min route)</option>
          </select>
        </div>

        <div style={{ display: 'flex', gap: '0.6rem' }}>
          <button
            className="btn btn-secondary"
            onClick={handleClear}
            disabled={loading || clearing || !selectedVehicleId}
            style={{ fontSize: '0.85rem' }}
          >
            {clearing ? 'Clearing...' : 'Reset Fleet Data'}
          </button>
          <button
            className="btn btn-primary"
            onClick={handleSimulate}
            disabled={loading || clearing || !selectedVehicleId}
          >
            {loading ? (
              <>
                <span className="spinner" /> Ingesting Telematics...
              </>
            ) : (
              <>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
                Stream Telemetry Pings
              </>
            )}
          </button>
        </div>
      </div>

      {resultMessage && (
        <div style={{ marginTop: '1rem', padding: '0.75rem 1rem', background: 'var(--color-success-bg)', border: '1px solid var(--color-success-border)', borderRadius: 'var(--radius-sm)', color: 'var(--color-success)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          {resultMessage}
        </div>
      )}

      {errorMessage && (
        <div style={{ marginTop: '1rem', padding: '0.75rem 1rem', background: 'var(--color-danger-bg)', border: '1px solid var(--color-danger-border)', borderRadius: 'var(--radius-sm)', color: 'var(--color-danger)', fontSize: '0.85rem' }}>
          {errorMessage}
        </div>
      )}
    </div>
  );
}
