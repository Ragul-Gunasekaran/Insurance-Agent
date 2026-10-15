import React from 'react';

export default function TelemetryMetricsCard({ summary, loading }) {
  if (loading) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2.5rem' }}>
        <span className="spinner" style={{ marginRight: '0.5rem' }} /> Loading telemetry risk indicators...
      </div>
    );
  }

  if (!summary || summary.totalPoints === 0) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ margin: '0 auto 1rem', opacity: 0.6 }}>
          <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
        </svg>
        <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.5rem' }}>No Telemetry Streamed Yet</h4>
        <p style={{ fontSize: '0.88rem' }}>
          Select a driving profile and click <strong>Stream Telemetry Pings</strong> above to calculate real-time driving behavior and safety indicators.
        </p>
      </div>
    );
  }

  const validPct = summary.totalPoints > 0
    ? Math.round((summary.validPoints / summary.totalPoints) * 100)
    : 0;

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
          </svg>
          Telematics Risk & Anomaly Metrics
        </h3>
        <span className="badge badge-success">Calibrated Stream</span>
      </div>

      {/* KPI Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ padding: '0.9rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Pings</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-primary)', marginTop: '0.2rem' }}>
            {summary.totalPoints}
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--color-success)', marginTop: '0.1rem' }}>
            {validPct}% Validated
          </div>
        </div>

        <div style={{ padding: '0.9rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Filtered Anomalies</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: summary.anomalousPoints > 0 ? 'var(--color-warning)' : 'var(--text-primary)', marginTop: '0.2rem' }}>
            {summary.anomalousPoints}
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>
            Sensor / GPS glitches
          </div>
        </div>

        <div style={{ padding: '0.9rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Peak Speed</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: (summary.maxSpeedKmh || 0) >= 120 ? 'var(--color-danger)' : 'var(--text-primary)', marginTop: '0.2rem' }}>
            {summary.maxSpeedKmh || 0} <span style={{ fontSize: '0.8rem', fontWeight: 400 }}>km/h</span>
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>
            Avg: {summary.averageSpeedKmh || 0} km/h
          </div>
        </div>

        <div style={{ padding: '0.9rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Harsh Braking</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: summary.harshBrakingEvents > 0 ? 'var(--color-danger)' : 'var(--color-success)', marginTop: '0.2rem' }}>
            {summary.harshBrakingEvents} <span style={{ fontSize: '0.8rem', fontWeight: 400 }}>events</span>
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>
            Peak: {summary.maxDecelerationG || 0}G
          </div>
        </div>

        <div style={{ padding: '0.9rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Rapid Accel</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: summary.rapidAccelerationEvents > 0 ? 'var(--color-warning)' : 'var(--text-primary)', marginTop: '0.2rem' }}>
            {summary.rapidAccelerationEvents} <span style={{ fontSize: '0.8rem', fontWeight: 400 }}>events</span>
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>
            Peak: +{summary.maxAccelerationG || 0}G
          </div>
        </div>
      </div>

      {/* Actuarial Risk Flags Alert */}
      <div style={{
        padding: '0.85rem 1.1rem',
        borderRadius: 'var(--radius-sm)',
        background: summary.harshBrakingEvents > 0 || summary.speedingEvents > 0
          ? 'rgba(239, 68, 68, 0.08)'
          : 'rgba(16, 185, 129, 0.08)',
        border: summary.harshBrakingEvents > 0 || summary.speedingEvents > 0
          ? '1px solid rgba(239, 68, 68, 0.3)'
          : '1px solid rgba(16, 185, 129, 0.3)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '0.88rem'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          {summary.harshBrakingEvents > 0 || summary.speedingEvents > 0 ? (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-danger)" strokeWidth="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          ) : (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-success)" strokeWidth="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
          )}
          <span>
            {summary.harshBrakingEvents > 0 || summary.speedingEvents > 0
              ? `Elevated Risk Profile: ${summary.harshBrakingEvents} harsh stops & ${summary.speedingEvents} speed violations detected.`
              : 'Clean Behavioral Profile: Low g-force variance and disciplined highway speed compliance.'}
          </span>
        </div>
        <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
          Vehicle ID #{summary.vehicleId}
        </span>
      </div>
    </div>
  );
}
