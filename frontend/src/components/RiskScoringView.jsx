import React, { useState, useEffect } from 'react';
import { fetchVehicles, fetchTripsForVehicle, calculateVehicleRiskScores, fetchVehicleRiskScores } from '../services/apiService';
import RiskScoreCard from './RiskScoreCard';

export default function RiskScoringView({ initialVehicleId }) {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState(initialVehicleId || '');
  const [trips, setTrips] = useState([]);
  const [selectedTrip, setSelectedTrip] = useState(null);
  const [loadingTrips, setLoadingTrips] = useState(false);
  const [batchScoring, setBatchScoring] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Load registered vehicles for dropdown
  useEffect(() => {
    async function loadVehicles() {
      const res = await fetchVehicles(0, 100);
      if (res.success && res.data) {
        const list = res.data.content || res.data || [];
        setVehicles(list);
        if (list.length > 0 && !selectedVehicleId) {
          setSelectedVehicleId(list[0].id);
        }
      }
    }
    loadVehicles();
  }, []);

  // Load trips when vehicle or page or refresh changes
  useEffect(() => {
    if (!selectedVehicleId) return;

    async function loadTrips() {
      setLoadingTrips(true);
      setError(null);
      const res = await fetchTripsForVehicle(selectedVehicleId, page, 10);
      setLoadingTrips(false);

      if (res.success && res.data) {
        const tripList = res.data.content || [];
        setTrips(tripList);
        setTotalPages(res.data.totalPages || 1);

        // Auto-select first trip if none selected or selected trip not in current list
        if (tripList.length > 0 && (!selectedTrip || !tripList.some(t => t.id === selectedTrip.id))) {
          setSelectedTrip(tripList[0]);
        }
      } else {
        setTrips([]);
        setSelectedTrip(null);
        setError(res.error || 'Failed to fetch trips for vehicle.');
      }
    }

    loadTrips();
  }, [selectedVehicleId, page, refreshTrigger]);

  const handleBatchScore = async () => {
    if (!selectedVehicleId) return;
    setBatchScoring(true);
    setMessage(null);
    setError(null);

    const res = await calculateVehicleRiskScores(selectedVehicleId);
    setBatchScoring(false);

    if (res.success && res.data) {
      setMessage(`Successfully scored ${res.data.length} trips for this vehicle.`);
      setRefreshTrigger(prev => prev + 1);
    } else {
      setError(res.error || 'Failed to execute batch risk scoring.');
    }
  };

  const handleSelectTrip = (trip) => {
    setSelectedTrip(trip);
    window.scrollTo({ top: 180, behavior: 'smooth' });
  };

  const handleScoreUpdated = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  const selectedVehicleObj = vehicles.find(v => String(v.id) === String(selectedVehicleId));

  return (
    <div>
      {/* Step Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
          Behavioral Risk Scoring Engine (Step 5)
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '860px', margin: 0 }}>
          Converts extracted trip behavioral features (harsh braking, rapid acceleration, speeding, and night driving exposure)
          into an explainable 0–100 safety score. Completely independent of vehicle valuation, engine capacity, and actuarial insurance premiums.
        </p>
      </div>

      {/* Top Filter and Action Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem 1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <label style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-primary)' }}>
              Select Vehicle:
            </label>
            <select
              className="input-field"
              value={selectedVehicleId}
              onChange={(e) => {
                setSelectedVehicleId(e.target.value);
                setPage(0);
                setSelectedTrip(null);
              }}
              style={{ minWidth: '260px' }}
            >
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.vehicleNumber} — {v.make} {v.model} ({v.manufacturingYear})
                </option>
              ))}
            </select>

            {selectedVehicleObj && (
              <span className={`badge ${selectedVehicleObj.verificationStatus === 'VERIFIED' ? 'badge-verified' : 'badge-unverified'}`}>
                {selectedVehicleObj.verificationStatus}
              </span>
            )}
          </div>

          <button
            className="btn btn-primary"
            onClick={handleBatchScore}
            disabled={batchScoring || !selectedVehicleId}
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            {batchScoring ? (
              <>
                <span className="spinner" style={{ width: '14px', height: '14px' }}></span>
                Scoring All Completed Trips...
              </>
            ) : (
              <>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
                Batch Score Vehicle Trips
              </>
            )}
          </button>
        </div>

        {message && (
          <div style={{
            marginTop: '1rem',
            padding: '0.6rem 0.9rem',
            background: 'rgba(34, 197, 94, 0.1)',
            border: '1px solid rgba(34, 197, 94, 0.3)',
            borderRadius: '6px',
            color: 'var(--success)',
            fontSize: '0.85rem'
          }}>
            ✓ {message}
          </div>
        )}

        {error && (
          <div style={{
            marginTop: '1rem',
            padding: '0.6rem 0.9rem',
            background: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '6px',
            color: 'var(--danger)',
            fontSize: '0.85rem'
          }}>
            ⚠ {error}
          </div>
        )}
      </div>

      {/* Main Scoring Card for Selected Trip */}
      <RiskScoreCard
        trip={selectedTrip}
        onScoreUpdated={handleScoreUpdated}
      />

      {/* Trips & Scoring History Table */}
      <div className="card" style={{ marginTop: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text-primary)' }}>
              Trips &amp; Risk Evaluation History
            </h3>
            <p style={{ margin: '0.2rem 0 0', fontSize: '0.84rem', color: 'var(--text-muted)' }}>
              Select any trip to view factor contributions, penalty explanations, or execute on-demand recalculation.
            </p>
          </div>

          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Showing {trips.length} journeys
          </div>
        </div>

        {loadingTrips ? (
          <div style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
            <div className="spinner" style={{ margin: '0 auto 0.75rem' }}></div>
            <p style={{ fontSize: '0.88rem' }}>Loading trip sessions...</p>
          </div>
        ) : trips.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem 1.5rem', color: 'var(--text-muted)' }}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ opacity: 0.5, margin: '0 auto 0.75rem' }}>
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <h4 style={{ margin: '0 0 0.3rem', color: 'var(--text-primary)' }}>No Stitched Trips Found</h4>
            <p style={{ fontSize: '0.85rem', maxWidth: '440px', margin: '0 auto' }}>
              No trips have been stitched for this vehicle yet. Ingest telemetry or run trip stitching in the "Trips &amp; Insights" tab first.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', textAlign: 'left', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem' }}>Trip ID</th>
                  <th style={{ padding: '0.75rem' }}>Start Time</th>
                  <th style={{ padding: '0.75rem' }}>Duration</th>
                  <th style={{ padding: '0.75rem' }}>Est. Distance</th>
                  <th style={{ padding: '0.75rem' }}>Status</th>
                  <th style={{ padding: '0.75rem' }}>Features</th>
                  <th style={{ padding: '0.75rem' }}>Score Status</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {trips.map((t) => {
                  const isSelected = selectedTrip && selectedTrip.id === t.id;
                  return (
                    <tr
                      key={t.id}
                      style={{
                        borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
                        background: isSelected ? 'rgba(56, 189, 248, 0.08)' : 'transparent',
                        cursor: 'pointer'
                      }}
                      onClick={() => handleSelectTrip(t)}
                    >
                      <td style={{ padding: '0.75rem', fontWeight: 600, color: isSelected ? 'var(--accent)' : 'inherit' }}>
                        #{t.id}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {t.startTime ? new Date(t.startTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : 'N/A'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {Math.floor((t.durationSeconds || 0) / 60)}m {(t.durationSeconds || 0) % 60}s
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {t.distanceKm !== null ? `${t.distanceKm} km` : '0.00 km'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className={`badge ${t.status === 'COMPLETED' ? 'badge-verified' : 'badge-warning'}`} style={{ fontSize: '0.72rem' }}>
                          {t.status}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className={`badge ${t.featuresCalculated ? 'badge-verified' : 'badge-unverified'}`} style={{ fontSize: '0.72rem' }}>
                          {t.featuresCalculated ? 'COMPUTED' : 'PENDING'}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className={`badge ${t.scoreCalculated ? 'badge-success' : 'badge-unverified'}`} style={{ fontSize: '0.72rem' }}>
                          {t.scoreCalculated ? 'SCORED' : 'UNRATED'}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                        <button
                          className={`btn ${isSelected ? 'btn-primary' : 'btn-outline'}`}
                          style={{ fontSize: '0.78rem', padding: '0.3rem 0.65rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSelectTrip(t);
                          }}
                        >
                          {isSelected ? 'Selected' : 'Inspect'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', marginTop: '1.25rem' }}>
                <button
                  className="btn btn-outline"
                  disabled={page === 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  style={{ padding: '0.3rem 0.75rem', fontSize: '0.8rem' }}
                >
                  Previous
                </button>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  className="btn btn-outline"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                  style={{ padding: '0.3rem 0.75rem', fontSize: '0.8rem' }}
                >
                  Next
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
