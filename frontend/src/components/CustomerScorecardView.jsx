import React, { useState, useEffect } from 'react';
import { fetchVehicles, fetchCustomerScorecard, fetchCustomerTrips } from '../services/apiService';

export default function CustomerScorecardView({ initialVehicleId }) {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState(initialVehicleId || '');
  const [scorecard, setScorecard] = useState(null);
  const [loadingScorecard, setLoadingScorecard] = useState(false);
  const [scorecardError, setScorecardError] = useState(null);

  // Paginated trips
  const [trips, setTrips] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalTrips, setTotalTrips] = useState(0);
  const [loadingTrips, setLoadingTrips] = useState(false);
  const [selectedTrip, setSelectedTrip] = useState(null);

  // Load vehicles
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

  // Load scorecard
  useEffect(() => {
    if (!selectedVehicleId) return;
    async function loadScorecard() {
      setLoadingScorecard(true);
      setScorecardError(null);
      const res = await fetchCustomerScorecard(selectedVehicleId);
      setLoadingScorecard(false);
      if (res.success && res.data) {
        setScorecard(res.data);
      } else {
        setScorecard(null);
        setScorecardError(res.error || 'Failed to load driver scorecard');
      }
    }
    loadScorecard();
  }, [selectedVehicleId]);

  // Load trips
  useEffect(() => {
    if (!selectedVehicleId) return;
    async function loadTrips() {
      setLoadingTrips(true);
      const res = await fetchCustomerTrips(selectedVehicleId, page, 8);
      setLoadingTrips(false);
      if (res.success && res.data) {
        setTrips(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setTotalTrips(res.data.totalElements || 0);
      } else {
        setTrips([]);
      }
    }
    loadTrips();
  }, [selectedVehicleId, page]);

  const getScoreColor = (category) => {
    switch (category) {
      case 'EXCELLENT': return '#10b981';
      case 'GOOD': return '#06b6d4';
      case 'MODERATE': return '#f59e0b';
      case 'HIGH_RISK': return '#ef4444';
      default: return '#64748b';
    }
  };

  const getCategoryBadgeClass = (category) => {
    switch (category) {
      case 'EXCELLENT': return 'badge-success';
      case 'GOOD': return 'badge-info';
      case 'MODERATE': return 'badge-warning';
      case 'HIGH_RISK': return 'badge-danger';
      default: return 'badge-neutral';
    }
  };

  const formatDateTime = (ts) => {
    if (!ts) return 'Not available';
    return new Date(ts).toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem', paddingBottom: '3rem' }}>
      {/* Header and Vehicle Selector */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1.25rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(99, 102, 241, 0.12)', border: '1px solid rgba(99, 102, 241, 0.3)', padding: '0.3rem 0.75rem', borderRadius: 'var(--radius-full)', fontSize: '0.8rem', color: '#a5b4fc', marginBottom: '0.5rem' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            Step 7: Customer Driver Scorecard
          </div>
          <h2 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Vehicle Driver Scorecard</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
            Transparent driving safety scoring, explainable penalties, and illustrative premium pricing.
          </p>
        </div>

        {/* Vehicle Selection Dropdown */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', background: 'var(--bg-secondary)', padding: '0.5rem 0.85rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
          <label style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>Vehicle:</label>
          <select
            value={selectedVehicleId}
            onChange={(e) => {
              setSelectedVehicleId(Number(e.target.value));
              setPage(0);
              setSelectedTrip(null);
            }}
            style={{
              background: 'var(--bg-primary)',
              color: 'var(--text-primary)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
              padding: '0.45rem 0.85rem',
              fontSize: '0.9rem',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            {vehicles.map((v) => (
              <option key={v.id} value={v.id}>
                {v.vehicleNumber} ({v.make} {v.model})
              </option>
            ))}
          </select>
        </div>
      </div>

      {loadingScorecard && (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>
          <div className="pulsing-dot" style={{ margin: '0 auto 1rem', width: '14px', height: '14px' }} />
          Loading vehicle scorecard and driving analytics...
        </div>
      )}

      {scorecardError && (
        <div className="card" style={{ borderColor: 'var(--color-danger-border)', background: 'var(--color-danger-bg)', padding: '1.5rem' }}>
          <div style={{ color: 'var(--color-danger)', fontWeight: 700, marginBottom: '0.3rem' }}>Error Loading Scorecard</div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{scorecardError}</p>
        </div>
      )}

      {scorecard && !loadingScorecard && (
        <>
          {/* Section A: Vehicle Summary & Macro Metrics */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem' }}>
            {/* Vehicle Profile Card */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Vehicle Profile</span>
                <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>Mock Verified</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                {scorecard.vehicleNumber}
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                {scorecard.make || 'Tata'} {scorecard.model || 'Nexon'} &bull; {scorecard.manufacturingYear || '2023'}
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: 'auto', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                <span>Fuel: <strong style={{ color: 'var(--text-primary)' }}>{scorecard.fuelType || 'PETROL'}</strong></span>
                <span>&bull;</span>
                <span>Status: <strong style={{ color: 'var(--color-success)' }}>Mock Verified</strong></span>
              </div>
            </div>

            {/* Completed Trips & Distance */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Usage Evidence</div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.9rem', fontWeight: 800, color: '#38bdf8' }}>
                  {scorecard.completedTripsCount}
                </span>
                <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>trips recorded</span>
              </div>
              <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                Total Observed Distance: <strong style={{ color: 'var(--text-primary)' }}>{scorecard.totalObservedDistanceKm} km</strong>
              </div>
              <div style={{ marginTop: 'auto', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                {scorecard.completedTripsCount > 0 ? 'Telematics verified trips' : 'No completed trips yet'}
              </div>
            </div>

            {/* Latest Safety Score Card */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Safety Score</span>
                <span className={`badge ${getCategoryBadgeClass(scorecard.latestRiskCategory)}`}>
                  {scorecard.latestRiskCategory || 'UNRATED'}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                <span style={{ fontSize: '2.1rem', fontWeight: 900, color: getScoreColor(scorecard.latestRiskCategory) }}>
                  {scorecard.latestSafetyScore !== null && scorecard.latestSafetyScore !== undefined
                    ? scorecard.latestSafetyScore
                    : 'Unrated'}
                </span>
                {scorecard.latestSafetyScore !== null && scorecard.latestSafetyScore !== undefined && (
                  <span style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>/ 100</span>
                )}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                {scorecard.latestSafetyScore !== null
                  ? `Scoring Version: ${scorecard.scoringVersion || 'v1.0'}`
                  : (scorecard.unratedReason || 'Insufficient trip distance for scoring')}
              </div>
            </div>

            {/* Latest Estimated Quote */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Illustrative Premium</span>
                <span className="badge badge-neutral" style={{ fontSize: '0.72rem' }}>Prototype Estimate</span>
              </div>
              <div style={{ fontSize: '1.9rem', fontWeight: 900, color: '#a78bfa' }}>
                {scorecard.latestEstimatedAnnualPremiumInr
                  ? `₹${Number(scorecard.latestEstimatedAnnualPremiumInr).toLocaleString('en-IN')}`
                  : 'Not available'}
              </div>
              <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                {scorecard.latestEstimatedAnnualPremiumInr
                  ? `Risk Multiplier: ${scorecard.latestRiskMultiplier || '1.0'}x &bull; Usage: ${scorecard.latestUsageMultiplier || '1.0'}x`
                  : 'Calculate quote in Step 6 tab'}
              </div>
              <div style={{ marginTop: 'auto', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Illustrative estimate &bull; Non-binding
              </div>
            </div>
          </div>

          {/* Section B & C: Driving Performance Scorecard & Insights */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.5rem' }}>
            {/* Behavioral Rates & Factors */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Driving Behavioral Rates (Per 100 km)</h3>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Distance-Weighted</span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                {/* Harsh Braking */}
                <div style={{ background: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Harsh Braking</div>
                  <div style={{ fontSize: '1.45rem', fontWeight: 800, color: scorecard.harshBrakeRatePer100Km > 3 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {scorecard.harshBrakeRatePer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>events / 100 km</div>
                </div>

                {/* Rapid Acceleration */}
                <div style={{ background: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Rapid Acceleration</div>
                  <div style={{ fontSize: '1.45rem', fontWeight: 800, color: scorecard.rapidAccelRatePer100Km > 3 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {scorecard.rapidAccelRatePer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>events / 100 km</div>
                </div>

                {/* Speeding Observations */}
                <div style={{ background: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Speeding Observations</div>
                  <div style={{ fontSize: '1.45rem', fontWeight: 800, color: scorecard.speedingObsPer100Km > 5 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {scorecard.speedingObsPer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>points / 100 km</div>
                </div>

                {/* Night Driving */}
                <div style={{ background: 'var(--bg-secondary)', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Night Driving</div>
                  <div style={{ fontSize: '1.45rem', fontWeight: 800, color: scorecard.nightDrivingRatio > 0.3 ? '#f59e0b' : '#38bdf8', marginTop: '0.2rem' }}>
                    {scorecard.nightDrivingMinutes}m
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Ratio: {Math.round(scorecard.nightDrivingRatio * 100)}% of driving
                  </div>
                </div>
              </div>

              {/* Factor Explanations from backend */}
              {scorecard.scoreExplanations && scorecard.scoreExplanations.length > 0 && (
                <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.6rem' }}>
                    Latest Trip Risk Factor Explanations
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {scorecard.scoreExplanations.map((f, idx) => (
                      <div key={idx} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.6rem', fontSize: '0.82rem', background: 'var(--bg-secondary)', padding: '0.5rem 0.75rem', borderRadius: 'var(--radius-sm)' }}>
                        <span style={{ color: f.penaltyPoints > 0 ? '#ef4444' : '#10b981', fontWeight: 700, minWidth: '45px' }}>
                          {f.penaltyPoints > 0 ? `-${f.penaltyPoints}` : '0 pts'}
                        </span>
                        <div style={{ flex: 1 }}>
                          <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{f.factorName}: </span>
                          <span style={{ color: 'var(--text-secondary)' }}>{f.explanation}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Score History Progression */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Safety Score Progression</h3>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {scorecard.scoreHistory ? `${scorecard.scoreHistory.length} scored trips` : 'No history'}
                </span>
              </div>

              {scorecard.scoreHistory && scorecard.scoreHistory.length >= 2 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <div style={{ background: 'var(--bg-secondary)', padding: '1.25rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                    {/* SVG Sparkline / Bar progression */}
                    <div style={{ display: 'flex', alignItems: 'flex-end', gap: '0.6rem', height: '120px', paddingBottom: '0.5rem' }}>
                      {scorecard.scoreHistory.map((pt, idx) => {
                        const heightPct = pt.isScorable && pt.score !== null ? Math.max(10, Math.min(100, pt.score)) : 5;
                        const barColor = pt.isScorable ? getScoreColor(pt.riskCategory) : '#475569';
                        return (
                          <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%', justifyContent: 'flex-end' }}>
                            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginBottom: '0.2rem', fontWeight: 600 }}>
                              {pt.score !== null ? Math.round(pt.score) : '—'}
                            </div>
                            <div
                              style={{
                                width: '100%',
                                height: `${heightPct}%`,
                                backgroundColor: barColor,
                                borderRadius: '4px 4px 0 0',
                                transition: 'height 0.3s ease'
                              }}
                              title={`Trip #${pt.tripId || idx + 1}: Score ${pt.score || 'Unrated'} (${pt.riskCategory || 'UNRATED'})`}
                            />
                            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>
                              T{idx + 1}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', justifyContent: 'space-between' }}>
                    <span>Oldest trip evaluated</span>
                    <span>Latest trip evaluated</span>
                  </div>
                </div>
              ) : (
                <div style={{ background: 'var(--bg-secondary)', padding: '2rem', borderRadius: 'var(--radius-sm)', textAlign: 'center', color: 'var(--text-muted)' }}>
                  <div style={{ fontSize: '1.8rem', marginBottom: '0.5rem' }}>📈</div>
                  <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Insufficient Historical Trends</div>
                  <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
                    At least 2 completed trips are required to observe historical trend progression. Current records: {scorecard.scoreHistory ? scorecard.scoreHistory.length : 0}.
                  </p>
                </div>
              )}

              {/* Data Quality Disclaimer Box */}
              <div style={{ marginTop: 'auto', background: 'rgba(99, 102, 241, 0.08)', border: '1px solid rgba(99, 102, 241, 0.2)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                <strong style={{ color: '#a5b4fc' }}>Safeguard Note:</strong> Unrated trips (under 1.0 km or with poor telemetry quality) are excluded from score penalties to protect driving records from false positives.
              </div>
            </div>
          </div>

          {/* Section D: Paginated Trip History Table */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Trip Performance History</h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  Select any completed trip to view its underlying features and behavioral safety score.
                </p>
              </div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Total: <strong>{totalTrips}</strong> trips
              </span>
            </div>

            {loadingTrips ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Loading trips...</div>
            ) : trips.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
                No trips recorded for this vehicle yet. Generate synthetic telemetry or ingest pings in Step 3.
              </div>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-subtle)', textAlign: 'left', color: 'var(--text-muted)' }}>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Trip ID</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Start Time</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Distance</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Duration</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Data Quality</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Safety Score</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Risk Category</th>
                      <th style={{ padding: '0.6rem 0.75rem' }}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {trips.map((t) => (
                      <tr
                        key={t.tripId}
                        style={{
                          borderBottom: '1px solid var(--border-subtle)',
                          cursor: 'pointer',
                          background: selectedTrip && selectedTrip.tripId === t.tripId ? 'rgba(99, 102, 241, 0.12)' : 'transparent',
                          transition: 'background 0.2s ease'
                        }}
                        onClick={() => setSelectedTrip(selectedTrip && selectedTrip.tripId === t.tripId ? null : t)}
                      >
                        <td style={{ padding: '0.75rem', fontWeight: 600, color: 'var(--text-primary)' }}>#{t.tripId}</td>
                        <td style={{ padding: '0.75rem', color: 'var(--text-secondary)' }}>{formatDateTime(t.startTime)}</td>
                        <td style={{ padding: '0.75rem', fontWeight: 600 }}>{t.distanceKm} km</td>
                        <td style={{ padding: '0.75rem', color: 'var(--text-secondary)' }}>{Math.round((t.durationSeconds || 0) / 60)}m</td>
                        <td style={{ padding: '0.75rem' }}>
                          <span className={`badge ${t.dataQualityStatus === 'HIGH' ? 'badge-success' : t.dataQualityStatus === 'POOR' ? 'badge-danger' : 'badge-neutral'}`} style={{ fontSize: '0.72rem' }}>
                            {t.dataQualityStatus || 'ACCEPTABLE'}
                          </span>
                        </td>
                        <td style={{ padding: '0.75rem', fontWeight: 700, color: t.isScorable ? getScoreColor(t.riskCategory) : 'var(--text-muted)' }}>
                          {t.isScorable && t.safetyScore !== null ? `${t.safetyScore}/100` : 'Unrated'}
                        </td>
                        <td style={{ padding: '0.75rem' }}>
                          <span className={`badge ${getCategoryBadgeClass(t.riskCategory)}`} style={{ fontSize: '0.72rem' }}>
                            {t.riskCategory || 'UNRATED'}
                          </span>
                        </td>
                        <td style={{ padding: '0.75rem' }}>
                          <button
                            className="btn btn-outline"
                            style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedTrip(selectedTrip && selectedTrip.tripId === t.tripId ? null : t);
                            }}
                          >
                            {selectedTrip && selectedTrip.tripId === t.tripId ? 'Hide Details' : 'View Factors'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.75rem', marginTop: '0.5rem' }}>
                <button
                  className="btn btn-outline"
                  disabled={page === 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                >
                  Previous
                </button>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  className="btn btn-outline"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                  style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                >
                  Next
                </button>
              </div>
            )}

            {/* Selected Trip Detail Panel */}
            {selectedTrip && (
              <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-accent)', borderRadius: 'var(--radius-md)', padding: '1.25rem', marginTop: '0.75rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                  <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                    Trip #{selectedTrip.tripId} Detailed Behavioral Risk Factors
                  </h4>
                  <button
                    className="btn btn-outline"
                    onClick={() => setSelectedTrip(null)}
                    style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                  >
                    Close
                  </button>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginBottom: '1rem' }}>
                  <div style={{ background: 'var(--bg-primary)', padding: '0.6rem 0.85rem', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Harsh Brakes</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{selectedTrip.harshBrakingCount ?? 0} events ({selectedTrip.harshBrakeRatePer100Km ?? 0}/100km)</div>
                  </div>
                  <div style={{ background: 'var(--bg-primary)', padding: '0.6rem 0.85rem', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Rapid Accel</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{selectedTrip.rapidAccelerationCount ?? 0} events ({selectedTrip.rapidAccelRatePer100Km ?? 0}/100km)</div>
                  </div>
                  <div style={{ background: 'var(--bg-primary)', padding: '0.6rem 0.85rem', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Speeding Obs</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{selectedTrip.speedingObservationsCount ?? 0} ({selectedTrip.speedingPct ?? 0}%)</div>
                  </div>
                  <div style={{ background: 'var(--bg-primary)', padding: '0.6rem 0.85rem', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Night Exposure</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{selectedTrip.nightDrivingMinutes ?? 0} min</div>
                  </div>
                </div>

                {selectedTrip.factorExplanations && selectedTrip.factorExplanations.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                    {selectedTrip.factorExplanations.map((f, i) => (
                      <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', fontSize: '0.8rem', background: 'var(--bg-primary)', padding: '0.4rem 0.75rem', borderRadius: 'var(--radius-sm)' }}>
                        <span style={{ color: f.penaltyPoints > 0 ? '#ef4444' : '#10b981', fontWeight: 700, minWidth: '40px' }}>
                          {f.penaltyPoints > 0 ? `-${f.penaltyPoints}` : '0 pts'}
                        </span>
                        <span style={{ fontWeight: 600 }}>{f.factorName}:</span>
                        <span style={{ color: 'var(--text-secondary)' }}>{f.explanation}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    {selectedTrip.unscorableReason || 'No factor breakdown available.'}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Section E: Prototype Disclaimer Footer */}
          <div style={{ textAlign: 'center', padding: '1.25rem', background: 'var(--bg-card)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
            <span style={{ color: '#a5b4fc', fontWeight: 600 }}>Prototype Notice:</span> {scorecard.disclaimer}
          </div>
        </>
      )}
    </div>
  );
}
