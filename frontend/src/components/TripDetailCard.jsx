import React, { useState, useEffect } from 'react';
import { fetchTripFeatures, calculateTripFeatures } from '../services/apiService';

export default function TripDetailCard({ trip, onFeaturesUpdated }) {
  const [features, setFeatures] = useState(null);
  const [loading, setLoading] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [error, setError] = useState(null);

  const loadFeatures = async () => {
    if (!trip) return;
    setLoading(true);
    setError(null);
    const res = await fetchTripFeatures(trip.id);
    setLoading(false);

    if (res.success && res.data) {
      setFeatures(res.data);
    } else {
      setFeatures(null);
    }
  };

  useEffect(() => {
    loadFeatures();
  }, [trip]);

  const handleCalculate = async () => {
    if (!trip) return;
    setCalculating(true);
    setError(null);

    const res = await calculateTripFeatures(trip.id);
    setCalculating(false);

    if (res.success && res.data) {
      setFeatures(res.data);
      if (onFeaturesUpdated) {
        onFeaturesUpdated(res.data);
      }
    } else {
      setError(res.error || 'Failed to calculate features.');
    }
  };

  if (!trip) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem', color: 'var(--text-muted)' }}>
        <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ margin: '0 auto 1rem', opacity: 0.6 }}>
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
        <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.4rem' }}>No Trip Selected</h4>
        <p style={{ fontSize: '0.88rem' }}>
          Select a trip from the table below to inspect route metrics and actuarial driving features.
        </p>
      </div>
    );
  }

  const getQualityBadge = (status) => {
    switch (status) {
      case 'EXCELLENT':
        return <span className="badge badge-success">Data Quality: Excellent</span>;
      case 'DEGRADED':
        return <span className="badge badge-warning">Data Quality: Degraded</span>;
      case 'POOR':
      default:
        return <span className="badge badge-danger">Data Quality: Poor</span>;
    }
  };

  return (
    <div className="card" style={{ border: '1px solid var(--border-accent)' }}>
      <div className="card-header">
        <h3 className="card-title">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          Trip Journey Analysis: <span style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>#{trip.id}</span>
        </h3>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {trip.status === 'COMPLETED' ? (
            <span className="badge badge-success">Completed Journey</span>
          ) : (
            <span className="badge badge-warning">Active In-Progress</span>
          )}
          {features && getQualityBadge(features.dataQualityStatus)}
        </div>
      </div>

      {/* Trip Journey Overview Metrics */}
      <div className="metrics-list" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.85rem' }}>
        <div className="metric-row">
          <span className="metric-label">Estimated Distance</span>
          <span className="metric-value" style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>
            {trip.distanceKm ? `${trip.distanceKm} km` : '0.0 km'}
            <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block' }}>Estimated GPS Haversine</span>
          </span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Journey Duration</span>
          <span className="metric-value">{trip.durationFormatted || `${trip.durationSeconds}s`}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Start Coordinates</span>
          <span className="metric-value">
            {trip.startLatitude ? `${trip.startLatitude.toFixed(4)}, ${trip.startLongitude.toFixed(4)}` : 'N/A'}
          </span>
        </div>
        <div className="metric-row">
          <span className="metric-label">End Coordinates</span>
          <span className="metric-value">
            {trip.endLatitude ? `${trip.endLatitude.toFixed(4)}, ${trip.endLongitude.toFixed(4)}` : 'In Progress'}
          </span>
        </div>
      </div>

      {/* Features Breakdown */}
      <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border-subtle)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h4 style={{ fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
            Engineered Driving Behavioral Features
          </h4>
          <button
            className="btn btn-primary"
            onClick={handleCalculate}
            disabled={calculating}
            style={{ fontSize: '0.82rem', padding: '0.4rem 0.85rem' }}
          >
            {calculating ? (
              <>
                <span className="spinner" /> Calculating...
              </>
            ) : (
              <>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l6-6" />
                </svg>
                {features ? 'Recalculate Features' : 'Calculate Features'}
              </>
            )}
          </button>
        </div>

        {error && (
          <div style={{ padding: '0.75rem', background: 'var(--color-danger-bg)', color: 'var(--color-danger)', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', marginBottom: '1rem' }}>
            {error}
          </div>
        )}

        {loading && !features && (
          <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
            <span className="spinner" style={{ marginRight: '0.5rem' }} /> Loading features...
          </div>
        )}

        {!loading && !features && !error && (
          <div style={{ textAlign: 'center', padding: '1.5rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', color: 'var(--text-muted)', fontSize: '0.88rem' }}>
            Features have not yet been extracted for this journey. Click <strong>Calculate Features</strong> to compute speed variance, harsh braking rates, and night exposure.
          </div>
        )}

        {features && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '0.75rem' }}>
            <div style={{ padding: '0.75rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Avg Speed</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                {features.averageSpeedKmh} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>km/h</span>
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Max: {features.maxSpeedKmh} km/h</div>
            </div>

            <div style={{ padding: '0.75rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Harsh Braking</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: features.harshBrakingCount > 0 ? 'var(--color-danger)' : 'var(--color-success)' }}>
                {features.harshBrakingCount} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>events</span>
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {features.harshBrakeRatePer100Km != null ? `${features.harshBrakeRatePer100Km} / 100km` : 'Rate: N/A (<0.1km)'}
              </div>
            </div>

            <div style={{ padding: '0.75rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Rapid Accel</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: features.rapidAccelerationCount > 0 ? 'var(--color-warning)' : 'var(--text-primary)' }}>
                {features.rapidAccelerationCount} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>events</span>
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {features.rapidAccelRatePer100Km != null ? `${features.rapidAccelRatePer100Km} / 100km` : 'Rate: N/A (<0.1km)'}
              </div>
            </div>

            <div style={{ padding: '0.75rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Night Driving</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: features.nightDrivingMinutes > 0 ? 'var(--color-warning)' : 'var(--text-primary)' }}>
                {features.nightDrivingMinutes} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>min</span>
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {Math.round((features.nightDrivingRatio || 0) * 100)}% of journey
              </div>
            </div>

            <div style={{ padding: '0.75rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Speeding</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: features.speedingObservationsCount > 0 ? 'var(--color-danger)' : 'var(--color-success)' }}>
                {features.speedingObservationsCount} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>pings</span>
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {features.speedingPct}% of trip
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
