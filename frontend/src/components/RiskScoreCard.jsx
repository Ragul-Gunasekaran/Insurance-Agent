import React, { useState, useEffect } from 'react';
import { fetchTripRiskScore, calculateTripRiskScore } from '../services/apiService';

export default function RiskScoreCard({ trip, onScoreUpdated }) {
  const [riskScore, setRiskScore] = useState(null);
  const [loading, setLoading] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [error, setError] = useState(null);

  const loadScore = async () => {
    if (!trip) return;
    setLoading(true);
    setError(null);
    const res = await fetchTripRiskScore(trip.id);
    setLoading(false);

    if (res.success && res.data) {
      setRiskScore(res.data);
    } else {
      setRiskScore(null);
    }
  };

  useEffect(() => {
    loadScore();
  }, [trip]);

  const handleCalculate = async () => {
    if (!trip) return;
    setCalculating(true);
    setError(null);

    const res = await calculateTripRiskScore(trip.id);
    setCalculating(false);

    if (res.success && res.data) {
      setRiskScore(res.data);
      if (onScoreUpdated) {
        onScoreUpdated(res.data);
      }
    } else {
      setError(res.error || 'Failed to calculate safety score.');
    }
  };

  if (!trip) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem', color: 'var(--text-muted)' }}>
        <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ margin: '0 auto 1rem', opacity: 0.6 }}>
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
        <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.4rem' }}>No Trip Selected</h4>
        <p style={{ fontSize: '0.88rem' }}>
          Select a trip from the table to evaluate behavioral safety scores and transparent factor explanations.
        </p>
      </div>
    );
  }

  const getScoreColor = (category, score) => {
    if (score === null || score === undefined) return 'var(--text-muted)';
    if (score >= 85) return 'var(--success)';
    if (score >= 70) return 'var(--accent)';
    if (score >= 50) return '#f59e0b';
    return 'var(--danger)';
  };

  const getCategoryBadgeClass = (category) => {
    switch (category) {
      case 'EXCELLENT':
        return 'badge-success';
      case 'GOOD':
        return 'badge-verified';
      case 'MODERATE':
        return 'badge-warning';
      case 'HIGH_RISK':
        return 'badge-stolen';
      default:
        return 'badge-unverified';
    }
  };

  return (
    <div className="card" style={{ position: 'relative', overflow: 'hidden' }}>
      {/* Card Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.3rem' }}>
            <span style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent)', fontWeight: 600 }}>
              Step 5: Risk Scoring Engine
            </span>
            <span className={`badge ${trip.status === 'COMPLETED' ? 'badge-verified' : 'badge-warning'}`} style={{ fontSize: '0.72rem' }}>
              Trip #{trip.id} ({trip.status})
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '1.3rem', color: 'var(--text-primary)' }}>
            Behavioral Safety Score
          </h3>
        </div>

        <button
          className="btn btn-primary"
          onClick={handleCalculate}
          disabled={calculating || loading}
          style={{ fontSize: '0.85rem', padding: '0.45rem 0.9rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
        >
          {calculating ? (
            <>
              <span className="spinner" style={{ width: '14px', height: '14px' }}></span>
              Evaluating...
            </>
          ) : (
            <>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M23 4v6h-6"></path>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
              </svg>
              {riskScore ? 'Recalculate Score' : 'Calculate Score'}
            </>
          )}
        </button>
      </div>

      {error && (
        <div style={{
          padding: '0.75rem 1rem',
          background: 'rgba(239, 68, 68, 0.1)',
          border: '1px solid rgba(239, 68, 68, 0.3)',
          borderRadius: '8px',
          color: 'var(--danger)',
          fontSize: '0.85rem',
          marginBottom: '1rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          <div className="spinner" style={{ margin: '0 auto 0.75rem' }}></div>
          <p style={{ fontSize: '0.88rem' }}>Loading behavioral risk evaluations...</p>
        </div>
      ) : !riskScore ? (
        <div style={{
          padding: '2rem 1.5rem',
          textAlign: 'center',
          background: 'rgba(255, 255, 255, 0.02)',
          border: '1px dashed var(--border-color)',
          borderRadius: '10px'
        }}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ color: 'var(--text-muted)', margin: '0 auto 0.75rem' }}>
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          </svg>
          <h4 style={{ margin: '0 0 0.4rem', color: 'var(--text-primary)', fontSize: '1rem' }}>Score Not Yet Evaluated</h4>
          <p style={{ margin: '0 0 1rem', color: 'var(--text-muted)', fontSize: '0.85rem', maxWidth: '420px', marginLeft: 'auto', marginRight: 'auto' }}>
            Driving features must be calculated to produce a behavioral safety score. Click below to execute deterministic risk scoring.
          </p>
          <button className="btn btn-outline" onClick={handleCalculate} disabled={calculating}>
            Score This Journey Now
          </button>
        </div>
      ) : (
        <div>
          {/* Main Score Display Banner */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '140px 1fr',
            gap: '1.25rem',
            padding: '1.25rem',
            background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.7) 0%, rgba(15, 23, 42, 0.9) 100%)',
            border: '1px solid var(--border-color)',
            borderRadius: '12px',
            marginBottom: '1.25rem',
            alignItems: 'center'
          }}>
            {/* Score Ring / Gauge */}
            <div style={{
              width: '120px',
              height: '120px',
              borderRadius: '50%',
              border: `6px solid ${getScoreColor(riskScore.scoreCategory, riskScore.score)}`,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: `0 0 20px ${riskScore.score !== null ? 'rgba(56, 189, 248, 0.15)' : 'none'}`,
              background: 'rgba(0, 0, 0, 0.3)'
            }}>
              {riskScore.score !== null ? (
                <>
                  <span style={{ fontSize: '2.1rem', fontWeight: 800, color: getScoreColor(riskScore.scoreCategory, riskScore.score), lineHeight: 1 }}>
                    {riskScore.score}
                  </span>
                  <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    out of 100
                  </span>
                </>
              ) : (
                <>
                  <span style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-muted)' }}>
                    N/A
                  </span>
                  <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>
                    UNRATED
                  </span>
                </>
              )}
            </div>

            {/* Score Summary & Status */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.4rem' }}>
                <span className={`badge ${getCategoryBadgeClass(riskScore.scoreCategory)}`} style={{ fontSize: '0.8rem', padding: '0.2rem 0.6rem' }}>
                  {riskScore.scoreCategory} DRIVER
                </span>
                <span className={`badge ${riskScore.dataQualityStatus === 'SUFFICIENT' ? 'badge-verified' : 'badge-warning'}`} style={{ fontSize: '0.75rem' }}>
                  Quality: {riskScore.dataQualityStatus}
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Model: {riskScore.scoringVersion}
                </span>
              </div>

              <p style={{ margin: '0 0 0.6rem', fontSize: '0.88rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                {riskScore.summaryExplanation}
              </p>

              <div style={{ display: 'flex', gap: '1rem', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                <span>Base: <strong>100.0 pts</strong></span>
                <span>Total Deductions: <strong style={{ color: riskScore.totalPenalties > 0 ? 'var(--danger)' : 'var(--success)' }}>
                  -{riskScore.totalPenalties} pts
                </strong></span>
                <span>Evaluated: {new Date(riskScore.calculatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
              </div>
            </div>
          </div>

          {/* Insufficient Evidence Warning Banner */}
          {!riskScore.isScorable && (
            <div style={{
              padding: '0.85rem 1rem',
              background: 'rgba(245, 158, 11, 0.1)',
              border: '1px solid rgba(245, 158, 11, 0.3)',
              borderRadius: '8px',
              color: '#f59e0b',
              fontSize: '0.84rem',
              marginBottom: '1.25rem',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '0.6rem'
            }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <div>
                <strong>Insufficient Data Notice: </strong>
                This trip does not meet minimum statistical evidence thresholds. In accordance with actuarial fairness guidelines, missing or insufficient data is never penalized as bad driving, and an unrated status is assigned instead of a fabricated score.
              </div>
            </div>
          )}

          {/* Factor Deductions Grid */}
          <h4 style={{ margin: '0 0 0.75rem', fontSize: '0.95rem', color: 'var(--text-primary)' }}>
            Transparent Factor Breakdown & Deductions
          </h4>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginBottom: '1.25rem' }}>
            {/* Harsh Braking Card */}
            <div style={{
              padding: '0.85rem 1rem',
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-color)',
              borderRadius: '8px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.3rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Harsh Braking</span>
                <span style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  color: riskScore.harshBrakingPenalty > 0 ? 'var(--danger)' : 'var(--success)'
                }}>
                  {riskScore.harshBrakingPenalty > 0 ? `-${riskScore.harshBrakingPenalty} pts` : '0 pts'}
                </span>
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                Weight: 2.0 / event / 100km (Max: 30)
              </div>
            </div>

            {/* Rapid Accel Card */}
            <div style={{
              padding: '0.85rem 1rem',
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-color)',
              borderRadius: '8px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.3rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Rapid Acceleration</span>
                <span style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  color: riskScore.rapidAccelerationPenalty > 0 ? 'var(--danger)' : 'var(--success)'
                }}>
                  {riskScore.rapidAccelerationPenalty > 0 ? `-${riskScore.rapidAccelerationPenalty} pts` : '0 pts'}
                </span>
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                Weight: 1.5 / event / 100km (Max: 25)
              </div>
            </div>

            {/* Speeding Card */}
            <div style={{
              padding: '0.85rem 1rem',
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-color)',
              borderRadius: '8px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.3rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Speeding</span>
                <span style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  color: riskScore.speedingPenalty > 0 ? 'var(--danger)' : 'var(--success)'
                }}>
                  {riskScore.speedingPenalty > 0 ? `-${riskScore.speedingPenalty} pts` : '0 pts'}
                </span>
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                Weight: 2.5 / 100km + severity (Max: 35)
              </div>
            </div>

            {/* Night Driving Card */}
            <div style={{
              padding: '0.85rem 1rem',
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px solid var(--border-color)',
              borderRadius: '8px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.3rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Night Driving</span>
                <span style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  color: riskScore.nightDrivingPenalty > 0 ? 'var(--danger)' : 'var(--success)'
                }}>
                  {riskScore.nightDrivingPenalty > 0 ? `-${riskScore.nightDrivingPenalty} pts` : '0 pts'}
                </span>
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                22:00-05:00 exposure ratio (Max: 5)
              </div>
            </div>
          </div>

          {/* Factor Explanations Table / List */}
          <div style={{
            background: 'rgba(0, 0, 0, 0.2)',
            borderRadius: '8px',
            border: '1px solid var(--border-color)',
            padding: '1rem',
            marginBottom: '1rem'
          }}>
            <h5 style={{ margin: '0 0 0.6rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Detailed Telematics Explanations
            </h5>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {riskScore.factorExplanations && riskScore.factorExplanations.length > 0 ? (
                riskScore.factorExplanations.map((factor, idx) => (
                  <div key={idx} style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '0.6rem',
                    fontSize: '0.82rem',
                    padding: '0.4rem 0',
                    borderBottom: idx < riskScore.factorExplanations.length - 1 ? '1px solid rgba(255, 255, 255, 0.05)' : 'none'
                  }}>
                    <span style={{
                      display: 'inline-block',
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      background: factor.included
                        ? (factor.penaltyApplied > 0 ? 'var(--danger)' : 'var(--success)')
                        : 'var(--text-muted)',
                      marginTop: '5px',
                      flexShrink: 0
                    }}></span>

                    <div style={{ flex: 1 }}>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)', marginRight: '0.4rem' }}>
                        {factor.factorName.replace('_', ' ')}:
                      </span>
                      <span style={{ color: 'var(--text-secondary)' }}>
                        {factor.explanation}
                      </span>
                    </div>

                    {factor.penaltyApplied > 0 && (
                      <span style={{ color: 'var(--danger)', fontWeight: 600, fontSize: '0.8rem', flexShrink: 0 }}>
                        -{factor.penaltyApplied}
                      </span>
                    )}
                  </div>
                ))
              ) : (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                  No factor explanations recorded.
                </div>
              )}
            </div>
          </div>

          {/* Actuarial & Prototype Boundary Notice */}
          <div style={{
            padding: '0.65rem 0.9rem',
            background: 'rgba(56, 189, 248, 0.05)',
            border: '1px solid rgba(56, 189, 248, 0.2)',
            borderRadius: '6px',
            fontSize: '0.74rem',
            color: 'var(--text-muted)',
            lineHeight: 1.4
          }}>
            <strong style={{ color: 'var(--accent)' }}>Prototype Behavioral Score Disclaimer: </strong>
            This safety score is a rule-based behavioral indicator based on deterministic heuristics. It does not calculate insurance premiums, apply rating multipliers, or predict guaranteed loss probability. Actuarial pricing is decoupled and performed in Step 6.
          </div>
        </div>
      )}
    </div>
  );
}
