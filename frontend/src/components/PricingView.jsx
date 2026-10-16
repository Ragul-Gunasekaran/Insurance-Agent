import React, { useState, useEffect } from 'react';
import { fetchVehicles, calculatePremiumQuote, fetchVehicleQuotes, fetchPricingConfiguration } from '../services/apiService';
import PricingQuoteCard from './PricingQuoteCard';

export default function PricingView({ initialVehicleId }) {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState(initialVehicleId || '');
  const [annualMileageInput, setAnnualMileageInput] = useState('');
  const [activeQuote, setActiveQuote] = useState(null);
  const [quotes, setQuotes] = useState([]);
  const [pricingConfig, setPricingConfig] = useState(null);
  const [loadingConfig, setLoadingConfig] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [loadingQuotes, setLoadingQuotes] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Load registered vehicles
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

  // Load safe pricing configuration
  useEffect(() => {
    async function loadConfig() {
      setLoadingConfig(true);
      const res = await fetchPricingConfiguration();
      setLoadingConfig(false);
      if (res.success && res.data) {
        setPricingConfig(res.data);
      }
    }
    loadConfig();
  }, []);

  // Load quote history for selected vehicle
  const loadQuotes = async () => {
    if (!selectedVehicleId) return;
    setLoadingQuotes(true);
    const res = await fetchVehicleQuotes(selectedVehicleId, page, 10);
    setLoadingQuotes(false);

    if (res.success && res.data) {
      const list = res.data.content || [];
      setQuotes(list);
      setTotalPages(res.data.totalPages || 1);
      if (list.length > 0 && !activeQuote) {
        setActiveQuote(list[0]);
      }
    } else {
      setQuotes([]);
    }
  };

  useEffect(() => {
    loadQuotes();
  }, [selectedVehicleId, page]);

  const handleCalculateQuote = async () => {
    if (!selectedVehicleId) return;
    setCalculating(true);
    setError(null);

    const body = {};
    if (annualMileageInput && !isNaN(Number(annualMileageInput)) && Number(annualMileageInput) > 0) {
      body.annualDistanceKmAssumption = Number(annualMileageInput);
    }

    const res = await calculatePremiumQuote(selectedVehicleId, body);
    setCalculating(false);

    if (res.success && res.data) {
      setActiveQuote(res.data);
      loadQuotes();
    } else {
      setError(res.error || 'Failed to calculate actuarial premium quote.');
    }
  };

  const selectedVehicleObj = vehicles.find(v => String(v.id) === String(selectedVehicleId));

  const formatCurrency = (val) => {
    if (val === null || val === undefined) return 'N/A';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(val);
  };

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
          Actuarial Pricing Engine (Step 6)
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '860px', margin: 0 }}>
          Generates explainable, illustrative Usage-Based Insurance (UBI) premium estimates in INR (₹)
          by combining baseline actuarial tariffs, eligible driving safety risk scores, and annual mileage road exposure.
        </p>
      </div>

      {/* Top Configuration & Control Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem 1.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem', alignItems: 'flex-end' }}>
          {/* Vehicle Selector */}
          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.4rem' }}>
              Select Vehicle
            </label>
            <select
              className="input-field"
              value={selectedVehicleId}
              onChange={(e) => {
                setSelectedVehicleId(e.target.value);
                setActiveQuote(null);
                setPage(0);
              }}
              style={{ width: '100%' }}
            >
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.vehicleNumber} — {v.make} {v.model} ({v.manufacturingYear})
                </option>
              ))}
            </select>
          </div>

          {/* Optional Annual Mileage Input */}
          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.4rem' }}>
              Projected Annual Mileage (km) <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(Optional Override)</span>
            </label>
            <input
              type="number"
              className="input-field"
              placeholder="e.g. 10000 (leaves blank to use telemetry)"
              value={annualMileageInput}
              onChange={(e) => setAnnualMileageInput(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>

          {/* Calculate Action */}
          <div>
            <button
              className="btn btn-primary"
              onClick={handleCalculateQuote}
              disabled={calculating || !selectedVehicleId}
              style={{ width: '100%', height: '42px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
            >
              {calculating ? (
                <>
                  <span className="spinner" style={{ width: '14px', height: '14px' }}></span>
                  Generating Quote...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10" />
                    <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
                    <path d="M12 18V6" />
                  </svg>
                  Calculate Estimated Premium
                </>
              )}
            </button>
          </div>
        </div>

        {error && (
          <div style={{
            marginTop: '1rem',
            padding: '0.65rem 1rem',
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

      {/* Pricing Assumptions Quick Strip */}
      {pricingConfig && (
        <div style={{
          display: 'flex',
          gap: '1.25rem',
          flexWrap: 'wrap',
          fontSize: '0.78rem',
          color: 'var(--text-muted)',
          padding: '0.6rem 1rem',
          background: 'rgba(0, 0, 0, 0.2)',
          borderRadius: '8px',
          border: '1px solid rgba(255, 255, 255, 0.05)',
          marginBottom: '1.5rem',
          alignItems: 'center'
        }}>
          <span>Base Tariff: <strong style={{ color: 'var(--text-primary)' }}>{formatCurrency(pricingConfig.baseAnnualPremiumInr)}</strong></span>
          <span>&bull;</span>
          <span>Limits: <strong style={{ color: 'var(--text-primary)' }}>{formatCurrency(pricingConfig.minPremiumInr)} to {formatCurrency(pricingConfig.maxPremiumInr)}</strong></span>
          <span>&bull;</span>
          <span>Multipliers: <strong>EXCELLENT (0.90) &bull; GOOD (1.00) &bull; MODERATE (1.15) &bull; HIGH_RISK (1.30)</strong></span>
          <span>&bull;</span>
          <span>Telemetry Min: <strong>&ge;20 km &amp; &ge;7 days</strong></span>
        </div>
      )}

      {/* Main Quote Result Card */}
      <PricingQuoteCard quote={activeQuote} />

      {/* Historical Quote Snapshots Table */}
      <div className="card" style={{ marginTop: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text-primary)' }}>
              Historical Quote Snapshots
            </h3>
            <p style={{ margin: '0.2rem 0 0', fontSize: '0.84rem', color: 'var(--text-muted)' }}>
              Every quote preserves historical multipliers, raw vs final amounts, and exact actuarial assumptions at calculation time.
            </p>
          </div>

          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Showing {quotes.length} quotes
          </div>
        </div>

        {loadingQuotes ? (
          <div style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
            <div className="spinner" style={{ margin: '0 auto 0.75rem' }}></div>
            <p style={{ fontSize: '0.88rem' }}>Loading quote history...</p>
          </div>
        ) : quotes.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem 1.5rem', color: 'var(--text-muted)' }}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ opacity: 0.5, margin: '0 auto 0.75rem' }}>
              <circle cx="12" cy="12" r="10" />
              <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
              <path d="M12 18V6" />
            </svg>
            <h4 style={{ margin: '0 0 0.3rem', color: 'var(--text-primary)' }}>No Quotes Generated Yet</h4>
            <p style={{ fontSize: '0.85rem', maxWidth: '440px', margin: '0 auto' }}>
              Click "Calculate Estimated Premium" above to generate the first usage-based insurance quote for this vehicle.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', textAlign: 'left', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem' }}>Quote Reference</th>
                  <th style={{ padding: '0.75rem' }}>Date</th>
                  <th style={{ padding: '0.75rem' }}>Score</th>
                  <th style={{ padding: '0.75rem' }}>Risk Multiplier</th>
                  <th style={{ padding: '0.75rem' }}>Usage Multiplier</th>
                  <th style={{ padding: '0.75rem' }}>Raw Premium</th>
                  <th style={{ padding: '0.75rem' }}>Final Premium</th>
                  <th style={{ padding: '0.75rem' }}>Status</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {quotes.map((q) => {
                  const isSelected = activeQuote && activeQuote.id === q.id;
                  return (
                    <tr
                      key={q.id}
                      style={{
                        borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
                        background: isSelected ? 'rgba(56, 189, 248, 0.08)' : 'transparent',
                        cursor: 'pointer'
                      }}
                      onClick={() => setActiveQuote(q)}
                    >
                      <td style={{ padding: '0.75rem', fontWeight: 600, color: isSelected ? 'var(--accent)' : 'inherit', fontFamily: 'monospace' }}>
                        {q.quoteReference}
                      </td>
                      <td style={{ padding: '0.75rem', fontSize: '0.82rem' }}>
                        {new Date(q.calculatedAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {q.safetyScore !== null ? `${q.safetyScore} (${q.riskCategory})` : 'UNRATED'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {q.riskMultiplier ? q.riskMultiplier : 'N/A'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {q.usageMultiplier ? q.usageMultiplier : 'N/A'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {q.rawAnnualPremiumInr !== null ? formatCurrency(q.rawAnnualPremiumInr) : 'N/A'}
                      </td>
                      <td style={{ padding: '0.75rem', fontWeight: 700, color: q.finalAnnualPremiumInr !== null ? 'var(--accent)' : 'var(--text-muted)' }}>
                        {q.finalAnnualPremiumInr !== null ? formatCurrency(q.finalAnnualPremiumInr) : 'N/A'}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className={`badge ${q.status === 'READY' ? 'badge-success' : 'badge-warning'}`} style={{ fontSize: '0.72rem' }}>
                          {q.status}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                        <button
                          className={`btn ${isSelected ? 'btn-primary' : 'btn-outline'}`}
                          style={{ fontSize: '0.78rem', padding: '0.3rem 0.65rem' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            setActiveQuote(q);
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
