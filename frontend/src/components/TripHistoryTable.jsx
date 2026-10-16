import React, { useState, useEffect } from 'react';
import { fetchTripsForVehicle, stitchTrips, calculateVehicleFeatures, fetchVehicles } from '../services/apiService';

export default function TripHistoryTable({ selectedVehicleId, onSelectTrip, onVehicleChange, refreshTrigger }) {
  const [vehicles, setVehicles] = useState([]);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(false);
  const [stitching, setStitching] = useState(false);
  const [batchCalculating, setBatchCalculating] = useState(false);
  const [error, setError] = useState(null);
  const [feedbackMessage, setFeedbackMessage] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Load registered vehicles for fleet selector
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

  const loadTrips = async (pageToLoad = 0) => {
    if (!selectedVehicleId) return;

    setLoading(true);
    setError(null);

    const res = await fetchTripsForVehicle(selectedVehicleId, pageToLoad, 8);
    setLoading(false);

    if (res.success && res.data) {
      setTrips(res.data.content || []);
      setPage(res.data.page);
      setTotalPages(res.data.totalPages || 1);
      setTotalElements(res.data.totalElements || 0);

      // Select first trip automatically if none selected
      if (res.data.content?.length > 0) {
        onSelectTrip(res.data.content[0]);
      }
    } else {
      setError(res.error || 'Failed to load trip sessions.');
    }
  };

  useEffect(() => {
    loadTrips(page);
  }, [selectedVehicleId, refreshTrigger, page]);

  const handleStitchTrips = async () => {
    if (!selectedVehicleId) return;
    setStitching(true);
    setFeedbackMessage(null);
    setError(null);

    const res = await stitchTrips(selectedVehicleId);
    setStitching(false);

    if (res.success && res.data) {
      setFeedbackMessage(`Stitched ${res.data.length} driving journeys from telematics stream.`);
      loadTrips(0);
    } else {
      setError(res.error || 'Failed to stitch trips.');
    }
  };

  const handleBatchCalculate = async () => {
    if (!selectedVehicleId) return;
    setBatchCalculating(true);
    setFeedbackMessage(null);
    setError(null);

    const res = await calculateVehicleFeatures(selectedVehicleId);
    setBatchCalculating(false);

    if (res.success && res.data) {
      setFeedbackMessage(`Calculated driving features for ${res.data.length} journeys.`);
      loadTrips(page);
    } else {
      setError(res.error || 'Failed to batch calculate features.');
    }
  };

  return (
    <div className="card" style={{ marginTop: '1.5rem' }}>
      <div className="card-header">
        <h3 className="card-title">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
          </svg>
          Vehicle Journey Sessions (Step 4)
        </h3>
        <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
          <button
            className="btn btn-secondary"
            onClick={handleStitchTrips}
            disabled={stitching || !selectedVehicleId}
            style={{ fontSize: '0.82rem', padding: '0.4rem 0.85rem' }}
          >
            {stitching ? (
              <>
                <span className="spinner" /> Stitching...
              </>
            ) : (
              <>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                </svg>
                Stitch &amp; Process Telemetry
              </>
            )}
          </button>

          <button
            className="btn btn-secondary"
            onClick={handleBatchCalculate}
            disabled={batchCalculating || trips.length === 0}
            style={{ fontSize: '0.82rem', padding: '0.4rem 0.85rem' }}
          >
            {batchCalculating ? (
              <>
                <span className="spinner" /> Calculating...
              </>
            ) : (
              <>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                </svg>
                Batch Feature Extraction
              </>
            )}
          </button>
        </div>
      </div>

      {/* Target Vehicle Selector */}
      <div style={{ marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: '240px' }}>
          <label className="form-label" style={{ fontSize: '0.82rem', marginBottom: '0.3rem' }}>Filter by Vehicle:</label>
          <select
            className="form-input"
            value={selectedVehicleId || ''}
            onChange={(e) => onVehicleChange(Number(e.target.value))}
            disabled={loading || stitching}
          >
            {vehicles.map(v => (
              <option key={v.id} value={v.id}>
                {v.vehicleNumber} &bull; {v.make} {v.model}
              </option>
            ))}
          </select>
        </div>
        <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', paddingTop: '1.2rem' }}>
          {totalElements} journey sessions recorded
        </div>
      </div>

      {feedbackMessage && (
        <div style={{ marginBottom: '1rem', padding: '0.75rem 1rem', background: 'var(--color-success-bg)', border: '1px solid var(--color-success-border)', borderRadius: 'var(--radius-sm)', color: 'var(--color-success)', fontSize: '0.85rem' }}>
          {feedbackMessage}
        </div>
      )}

      {error && (
        <div style={{ marginBottom: '1rem', padding: '0.75rem 1rem', background: 'var(--color-danger-bg)', border: '1px solid var(--color-danger-border)', borderRadius: 'var(--radius-sm)', color: 'var(--color-danger)', fontSize: '0.85rem' }}>
          {error}
        </div>
      )}

      {loading && trips.length === 0 && (
        <div style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
          <span className="spinner" style={{ marginRight: '0.5rem' }} /> Loading trip sessions...
        </div>
      )}

      {!loading && trips.length === 0 && !error && (
        <div style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          No stitched journeys found for this vehicle. Ensure telematics pings are streamed, then click <strong>Stitch &amp; Process Telemetry</strong> above.
        </div>
      )}

      {trips.length > 0 && (
        <>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Trip ID</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Start Time</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Status</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Distance (GPS)</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Duration</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Telemetry Pings</th>
                  <th style={{ padding: '0.65rem 0.5rem' }}>Features</th>
                  <th style={{ padding: '0.65rem 0.5rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {trips.map(t => (
                  <tr
                    key={t.id}
                    style={{
                      borderBottom: '1px solid rgba(255, 255, 255, 0.03)',
                      transition: 'background 0.2s'
                    }}
                  >
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--accent-primary)' }}>
                      #{t.id}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', color: 'var(--text-primary)' }}>
                      {new Date(t.startTime).toLocaleString()}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      {t.status === 'COMPLETED' ? (
                        <span className="badge badge-success" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>Completed</span>
                      ) : (
                        <span className="badge badge-warning" style={{ fontSize: '0.72rem', padding: '0.15rem 0.45rem' }}>In Progress</span>
                      )}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {t.distanceKm != null ? `${t.distanceKm} km` : '0.0 km'}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)' }}>
                      {t.durationFormatted}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      <span style={{ color: 'var(--color-success)', fontWeight: 600 }}>{t.validRecordCount} valid</span>
                      {t.anomalousRecordCount > 0 && (
                        <span style={{ color: 'var(--color-warning)', marginLeft: '0.4rem', fontSize: '0.75rem' }}>
                          ({t.anomalousRecordCount} filtered)
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem' }}>
                      {t.featuresCalculated ? (
                        <span className="badge badge-info" style={{ fontSize: '0.7rem', padding: '0.15rem 0.45rem' }}>Calculated</span>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Pending</span>
                      )}
                    </td>
                    <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right' }}>
                      <button
                        className="btn btn-secondary"
                        onClick={() => onSelectTrip(t)}
                        style={{ fontSize: '0.78rem', padding: '0.3rem 0.65rem' }}
                      >
                        Inspect Details
                      </button>
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
