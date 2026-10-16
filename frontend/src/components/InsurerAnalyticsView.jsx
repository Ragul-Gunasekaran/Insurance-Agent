import React, { useState, useEffect } from 'react';
import {
  fetchInsurerOverview,
  fetchInsurerRiskDistribution,
  fetchInsurerDrivingBehavior,
  fetchInsurerUsageAnalytics,
  fetchInsurerPricingAnalytics
} from '../services/apiService';

export default function InsurerAnalyticsView() {
  const [overview, setOverview] = useState(null);
  const [riskDist, setRiskDist] = useState(null);
  const [behavior, setBehavior] = useState(null);
  const [usage, setUsage] = useState(null);
  const [pricing, setPricing] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadAllAnalytics = async () => {
    setLoading(true);
    setError(null);

    try {
      const [ovRes, riskRes, behRes, useRes, prcRes] = await Promise.all([
        fetchInsurerOverview(),
        fetchInsurerRiskDistribution(),
        fetchInsurerDrivingBehavior(),
        fetchInsurerUsageAnalytics(),
        fetchInsurerPricingAnalytics()
      ]);

      if (ovRes.success) setOverview(ovRes.data);
      if (riskRes.success) setRiskDist(riskRes.data);
      if (behRes.success) setBehavior(behRes.data);
      if (useRes.success) setUsage(useRes.data);
      if (prcRes.success) setPricing(prcRes.data);

      if (!ovRes.success && !riskRes.success && !behRes.success) {
        setError(ovRes.error || 'Failed to fetch insurer fleet analytics');
      }
    } catch (err) {
      setError(`Failed to load insurer analytics: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllAnalytics();
  }, []);

  const getCategoryColor = (category) => {
    switch (category) {
      case 'EXCELLENT': return '#10b981';
      case 'GOOD': return '#06b6d4';
      case 'MODERATE': return '#f59e0b';
      case 'HIGH_RISK': return '#ef4444';
      case 'UNRATED': return '#64748b';
      default: return '#8b5cf6';
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem', paddingBottom: '3.5rem' }}>
      {/* Header Banner */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1.25rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(14, 165, 233, 0.12)', border: '1px solid rgba(14, 165, 233, 0.3)', padding: '0.3rem 0.75rem', borderRadius: 'var(--radius-full)', fontSize: '0.8rem', color: '#7dd3fc', marginBottom: '0.5rem' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M3 3v18h18" />
              <path d="M18 17V9" />
              <path d="M13 17V5" />
              <path d="M8 17v-3" />
            </svg>
            Step 7: Prototype Insurer Analytics
          </div>
          <h2 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Fleet Underwriting &amp; Actuarial Analytics</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
            Aggregated telematics risk distributions, mileage coverage, and illustrative pricing across the insured fleet.
          </p>
        </div>

        <button
          className="btn btn-outline"
          onClick={loadAllAnalytics}
          disabled={loading}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="23 4 23 10 17 10" />
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
          </svg>
          {loading ? 'Refreshing...' : 'Refresh Aggregates'}
        </button>
      </div>

      {/* Prototype Disclaimer Banner */}
      <div style={{ background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.25)', borderRadius: 'var(--radius-md)', padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ fontSize: '1.4rem' }}>⚠️</div>
        <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
          <strong style={{ color: 'var(--color-warning)' }}>Simulated Insurer Demonstration — Public Prototype View:</strong>
          {' '}This hackathon prototype presents real aggregated database records for simulated telematics. It is not connected to a live insurance carrier or underwriting system and does not implement role-based access control (RBAC). All monetary quotes are illustrative estimates in INR (₹).
        </div>
      </div>

      {loading && !overview && (
        <div style={{ textAlign: 'center', padding: '3.5rem', color: 'var(--text-secondary)' }}>
          <div className="pulsing-dot" style={{ margin: '0 auto 1rem', width: '14px', height: '14px' }} />
          Computing fleet aggregations from database records...
        </div>
      )}

      {error && (
        <div className="card" style={{ borderColor: 'var(--color-danger-border)', background: 'var(--color-danger-bg)', padding: '1.25rem' }}>
          <div style={{ color: 'var(--color-danger)', fontWeight: 700, marginBottom: '0.3rem' }}>Error Loading Insurer Analytics</div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{error}</p>
        </div>
      )}

      {overview && (
        <>
          {/* Section A: Fleet Macro Overview */}
          <div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.85rem' }}>Fleet Underwriting Overview</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
              <div className="card" style={{ padding: '1.15rem' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Registered Vehicles</div>
                <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--text-primary)', marginTop: '0.2rem' }}>
                  {overview.totalRegisteredVehicles}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.3rem' }}>
                  {overview.vehiclesWithTelematics} with telematics &bull; {overview.vehiclesWithTrips} with trips
                </div>
              </div>

              <div className="card" style={{ padding: '1.15rem' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Completed Trips</div>
                <div style={{ fontSize: '1.85rem', fontWeight: 800, color: '#38bdf8', marginTop: '0.2rem' }}>
                  {overview.completedTripsCount}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.3rem' }}>
                  {overview.scorableTripsCount} scorable &bull; {overview.unratedTripsCount} unrated
                </div>
              </div>

              <div className="card" style={{ padding: '1.15rem' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Observed Distance</div>
                <div style={{ fontSize: '1.85rem', fontWeight: 800, color: '#34d399', marginTop: '0.2rem' }}>
                  {overview.totalObservedDistanceKm} <span style={{ fontSize: '1rem', fontWeight: 600 }}>km</span>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.3rem' }}>
                  Validated GPS trips only
                </div>
              </div>

              <div className="card" style={{ padding: '1.15rem' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Fleet Avg Safety Score</div>
                <div style={{ fontSize: '1.85rem', fontWeight: 800, color: overview.averageSafetyScore ? '#818cf8' : 'var(--text-muted)', marginTop: '0.2rem' }}>
                  {overview.averageSafetyScore ? `${overview.averageSafetyScore}/100` : 'N/A'}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.3rem' }}>
                  {overview.vehiclesWithEligibleQuotes} vehicles with eligible quotes
                </div>
              </div>
            </div>
          </div>

          {/* Section B: Risk Distributions (Trip-Level & Vehicle-Level) */}
          {riskDist && (
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.85rem' }}>Driving Risk Distributions</h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.25rem' }}>
                {/* Trip-Level Risk Categories */}
                <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>Trip-Level Score Categories</h4>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      Total: {riskDist.totalScoredTrips + riskDist.totalUnratedTrips} trips
                    </span>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    {riskDist.tripCategories && riskDist.tripCategories.map((c, i) => (
                      <div key={i} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem' }}>
                          <span style={{ fontWeight: 600, color: getCategoryColor(c.category) }}>{c.category}</span>
                          <span style={{ color: 'var(--text-secondary)' }}>
                            <strong>{c.count}</strong> trips ({c.percentage}%)
                          </span>
                        </div>
                        <div style={{ height: '8px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                          <div
                            style={{
                              height: '100%',
                              width: `${Math.min(100, c.percentage)}%`,
                              backgroundColor: getCategoryColor(c.category),
                              borderRadius: 'var(--radius-full)'
                            }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                  <div style={{ marginTop: 'auto', fontSize: '0.75rem', color: 'var(--text-muted)', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)' }}>
                    Unrated trips are tracked separately from scored categories to avoid skewing fleet averages.
                  </div>
                </div>

                {/* Vehicle-Level Risk Categories */}
                <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>Vehicle Portfolio Risk Profile</h4>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      Total: {riskDist.totalScoredVehicles} scored vehicles
                    </span>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                    {riskDist.vehicleCategories && riskDist.vehicleCategories.map((c, i) => (
                      <div key={i} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem' }}>
                          <span style={{ fontWeight: 600, color: getCategoryColor(c.category) }}>{c.category}</span>
                          <span style={{ color: 'var(--text-secondary)' }}>
                            <strong>{c.count}</strong> vehicles ({c.percentage}%)
                          </span>
                        </div>
                        <div style={{ height: '8px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                          <div
                            style={{
                              height: '100%',
                              width: `${Math.min(100, c.percentage)}%`,
                              backgroundColor: getCategoryColor(c.category),
                              borderRadius: 'var(--radius-full)'
                            }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                  <div style={{ marginTop: 'auto', fontSize: '0.75rem', color: 'var(--text-muted)', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)' }}>
                    Derived from each vehicle's latest valid safety score. Avoids double-counting multiple trips per car.
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Section C: Fleet Driving Behavior (Weighted Aggregates) */}
          {behavior && (
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.85rem' }}>
                Fleet Driving Behavioral Aggregates (Distance-Weighted)
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Harsh Braking Rate</div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: behavior.fleetHarshBrakeRatePer100Km > 3 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {behavior.fleetHarshBrakeRatePer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Total {behavior.totalHarshBrakingEvents} events across {behavior.totalDistanceEvaluatedKm} km
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Rapid Acceleration Rate</div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: behavior.fleetRapidAccelRatePer100Km > 3 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {behavior.fleetRapidAccelRatePer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Total {behavior.totalRapidAccelerationEvents} events across {behavior.totalDistanceEvaluatedKm} km
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Speeding Observations</div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: behavior.fleetSpeedingObsPer100Km > 5 ? '#ef4444' : '#10b981', marginTop: '0.2rem' }}>
                    {behavior.fleetSpeedingObsPer100Km}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Total {behavior.totalSpeedingObservations} points across {behavior.totalDistanceEvaluatedKm} km
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Telematics Anomaly Rate</div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: behavior.fleetAnomalyRatePct > 10 ? '#ef4444' : '#38bdf8', marginTop: '0.2rem' }}>
                    {behavior.fleetAnomalyRatePct}%
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    {behavior.totalAnomalousRecords} anomalous / {behavior.totalValidRecords + behavior.totalAnomalousRecords} records
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Section D: Usage Analytics & Vehicle Mileage Coverage */}
          {usage && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.85rem' }}>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Usage &amp; Mileage Coverage Analytics</h3>
                <div style={{ display: 'flex', gap: '0.75rem', fontSize: '0.8rem' }}>
                  <span style={{ color: '#10b981' }}>&bull; {usage.vehiclesWithSufficientUsage} Sufficient Usage</span>
                  <span style={{ color: '#f59e0b' }}>&bull; {usage.vehiclesWithInsufficientUsage} Insufficient Usage</span>
                </div>
              </div>

              <div className="card" style={{ padding: '0.5rem 0' }}>
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid var(--border-subtle)', textAlign: 'left', color: 'var(--text-muted)' }}>
                        <th style={{ padding: '0.6rem 1rem' }}>Vehicle ID</th>
                        <th style={{ padding: '0.6rem 1rem' }}>Registration Number</th>
                        <th style={{ padding: '0.6rem 1rem' }}>Make &amp; Model</th>
                        <th style={{ padding: '0.6rem 1rem' }}>Completed Trips</th>
                        <th style={{ padding: '0.6rem 1rem' }}>Observed Distance</th>
                        <th style={{ padding: '0.6rem 1rem' }}>Coverage Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {usage.vehicleMileageSummaries && usage.vehicleMileageSummaries.map((v) => (
                        <tr key={v.vehicleId} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                          <td style={{ padding: '0.65rem 1rem', color: 'var(--text-muted)' }}>#{v.vehicleId}</td>
                          <td style={{ padding: '0.65rem 1rem', fontWeight: 600, color: 'var(--text-primary)' }}>{v.vehicleNumber}</td>
                          <td style={{ padding: '0.65rem 1rem', color: 'var(--text-secondary)' }}>{v.make} {v.model}</td>
                          <td style={{ padding: '0.65rem 1rem', fontWeight: 600 }}>{v.completedTrips} trips</td>
                          <td style={{ padding: '0.65rem 1rem', fontWeight: 700, color: '#38bdf8' }}>{v.observedDistanceKm} km</td>
                          <td style={{ padding: '0.65rem 1rem' }}>
                            <span className={`badge ${v.hasSufficientUsage ? 'badge-success' : 'badge-warning'}`} style={{ fontSize: '0.72rem' }}>
                              {v.hasSufficientUsage ? 'Sufficient Evidence' : 'Needs More Mileage'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* Section E: Illustrative Premium Analytics */}
          {pricing && (
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.85rem' }}>
                Illustrative Actuarial Premium Analytics
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1.25rem' }}>
                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Eligible Quotes</div>
                  <div style={{ fontSize: '1.7rem', fontWeight: 800, color: '#10b981', marginTop: '0.2rem' }}>
                    {pricing.eligibleQuotesCount}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    {pricing.ineligibleQuotesCount} ineligible / insufficient data
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Average Estimated Premium</div>
                  <div style={{ fontSize: '1.7rem', fontWeight: 800, color: '#a78bfa', marginTop: '0.2rem' }}>
                    {pricing.averageEstimatedPremiumInr ? `₹${Number(pricing.averageEstimatedPremiumInr).toLocaleString('en-IN')}` : 'N/A'}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Across {pricing.eligibleQuotesCount} eligible quotes
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Median Estimated Premium</div>
                  <div style={{ fontSize: '1.7rem', fontWeight: 800, color: '#818cf8', marginTop: '0.2rem' }}>
                    {pricing.medianEstimatedPremiumInr ? `₹${Number(pricing.medianEstimatedPremiumInr).toLocaleString('en-IN')}` : 'N/A'}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    50th percentile of quoted fleet
                  </div>
                </div>

                <div className="card" style={{ padding: '1rem' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Premium Range</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.2rem' }}>
                    {pricing.minEstimatedPremiumInr ? `₹${Number(pricing.minEstimatedPremiumInr).toLocaleString('en-IN')} - ₹${Number(pricing.maxEstimatedPremiumInr).toLocaleString('en-IN')}` : 'N/A'}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    Minimum to maximum quote range
                  </div>
                </div>
              </div>

              {/* Pricing breakdown details */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem' }}>
                <div className="card" style={{ padding: '1.25rem' }}>
                  <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.75rem' }}>
                    Quoted Portfolio Risk Distribution
                  </h4>
                  {pricing.quotedRiskDistribution && pricing.quotedRiskDistribution.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                      {pricing.quotedRiskDistribution.map((c, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', padding: '0.4rem 0', borderBottom: '1px solid var(--border-subtle)' }}>
                          <span style={{ fontWeight: 600, color: getCategoryColor(c.category) }}>{c.category}</span>
                          <span style={{ color: 'var(--text-secondary)' }}><strong>{c.count}</strong> quotes ({c.percentage}%)</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No eligible quotes calculated yet.</div>
                  )}
                </div>

                <div className="card" style={{ padding: '1.25rem' }}>
                  <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.75rem' }}>
                    Pricing Engine Versions
                  </h4>
                  {pricing.pricingVersionDistribution && pricing.pricingVersionDistribution.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                      {pricing.pricingVersionDistribution.map((v, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', padding: '0.4rem 0', borderBottom: '1px solid var(--border-subtle)' }}>
                          <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{v.pricingVersion}</span>
                          <span style={{ color: 'var(--text-secondary)' }}><strong>{v.count}</strong> snapshots</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No quotes generated yet.</div>
                  )}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
