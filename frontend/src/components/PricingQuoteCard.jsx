import React from 'react';

export default function PricingQuoteCard({ quote }) {
  if (!quote) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem', color: 'var(--text-muted)' }}>
        <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ margin: '0 auto 1rem', opacity: 0.6 }}>
          <circle cx="12" cy="12" r="10" />
          <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
          <path d="M12 18V6" />
        </svg>
        <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.4rem' }}>No Quote Generated</h4>
        <p style={{ fontSize: '0.88rem' }}>
          Select a vehicle and click "Calculate Estimated Premium" to generate an explainable, usage-based insurance quote.
        </p>
      </div>
    );
  }

  const formatInr = (amount) => {
    if (amount === null || amount === undefined) return 'N/A';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'READY':
        return <span className="badge badge-success" style={{ fontSize: '0.78rem' }}>QUOTE READY</span>;
      case 'INSUFFICIENT_RISK_DATA':
        return <span className="badge badge-warning" style={{ fontSize: '0.78rem' }}>INSUFFICIENT RISK DATA</span>;
      case 'INSUFFICIENT_USAGE_DATA':
        return <span className="badge badge-warning" style={{ fontSize: '0.78rem' }}>INSUFFICIENT USAGE DATA</span>;
      default:
        return <span className="badge badge-stolen" style={{ fontSize: '0.78rem' }}>INSUFFICIENT DATA</span>;
    }
  };

  const isReady = quote.status === 'READY' && quote.finalAnnualPremiumInr !== null;

  return (
    <div className="card" style={{ position: 'relative', overflow: 'hidden' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.5rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.3rem' }}>
            <span style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent)', fontWeight: 600 }}>
              Step 6: Actuarial Pricing Engine
            </span>
            {getStatusBadge(quote.status)}
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Ref: {quote.quoteReference}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '1.3rem', color: 'var(--text-primary)' }}>
            Usage-Based Insurance Premium Quote
          </h3>
        </div>

        <div style={{ textAlign: 'right', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
          <div>Model: <strong>{quote.pricingVersion}</strong></div>
          <div>Calculated: {new Date(quote.calculatedAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</div>
        </div>
      </div>

      {/* Main Quote Result Banner */}
      {isReady ? (
        <div style={{
          padding: '1.5rem',
          background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.85) 0%, rgba(15, 23, 42, 0.95) 100%)',
          border: '1px solid var(--border-color)',
          borderRadius: '12px',
          marginBottom: '1.5rem'
        }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.5rem', alignItems: 'center' }}>
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.2rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Estimated Annual Premium
              </div>
              <div style={{ fontSize: '2.6rem', fontWeight: 800, color: 'var(--accent)', lineHeight: 1.1 }}>
                {formatInr(quote.finalAnnualPremiumInr)}
              </div>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                Currency: <strong>{quote.currency} (₹)</strong> &bull; Raw Calculated: {formatInr(quote.rawAnnualPremiumInr)}
              </div>
            </div>

            <div style={{
              background: 'rgba(0, 0, 0, 0.3)',
              padding: '1rem',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.08)'
            }}>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '0.4rem' }}>
                Actuarial Pricing Formula:
              </div>
              <div style={{ fontSize: '0.88rem', fontFamily: 'monospace', color: 'var(--text-primary)', lineHeight: 1.6 }}>
                Base Premium: <strong>{formatInr(quote.baseAnnualPremiumInr)}</strong><br />
                &times; Risk Multiplier: <strong style={{ color: quote.riskMultiplier < 1 ? 'var(--success)' : 'var(--danger)' }}>
                  {quote.riskMultiplier} ({quote.riskCategory})
                </strong><br />
                &times; Usage Multiplier: <strong style={{ color: quote.usageMultiplier < 1 ? 'var(--success)' : 'var(--accent)' }}>
                  {quote.usageMultiplier}
                </strong>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div style={{
          padding: '1.25rem',
          background: 'rgba(245, 158, 11, 0.1)',
          border: '1px solid rgba(245, 158, 11, 0.3)',
          borderRadius: '10px',
          color: '#f59e0b',
          marginBottom: '1.5rem',
          display: 'flex',
          alignItems: 'flex-start',
          gap: '0.75rem'
        }}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <div>
            <h4 style={{ margin: '0 0 0.3rem', fontSize: '0.95rem', color: '#f59e0b' }}>
              Quote Unavailable: {quote.status.replace(/_/g, ' ')}
            </h4>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
              {quote.summaryExplanation}
            </p>
          </div>
        </div>
      )}

      {/* Metrics Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.85rem', marginBottom: '1.5rem' }}>
        {/* Risk Multiplier Card */}
        <div style={{
          padding: '1rem',
          background: 'rgba(255, 255, 255, 0.03)',
          border: '1px solid var(--border-color)',
          borderRadius: '8px'
        }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
            Driving Behavior Factor
          </div>
          <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.2rem' }}>
            {quote.safetyScore !== null ? `${quote.safetyScore}/100` : 'UNRATED'}
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
            Category: <strong>{quote.riskCategory || 'UNRATED'}</strong>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
            Multiplier: {quote.riskMultiplier ? quote.riskMultiplier : 'Ineligible'}
          </div>
        </div>

        {/* Usage Multiplier Card */}
        <div style={{
          padding: '1rem',
          background: 'rgba(255, 255, 255, 0.03)',
          border: '1px solid var(--border-color)',
          borderRadius: '8px'
        }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
            Annual Mileage Exposure
          </div>
          <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.2rem' }}>
            {quote.projectedAnnualDistanceKm !== null ? `${Number(quote.projectedAnnualDistanceKm).toLocaleString()} km/yr` : 'N/A'}
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
            Source: <strong>{quote.annualDistanceSource ? quote.annualDistanceSource.replace(/_/g, ' ') : 'None'}</strong>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
            Multiplier: {quote.usageMultiplier ? quote.usageMultiplier : 'Ineligible'}
          </div>
        </div>

        {/* Observed Telemetry Card */}
        <div style={{
          padding: '1rem',
          background: 'rgba(255, 255, 255, 0.03)',
          border: '1px solid var(--border-color)',
          borderRadius: '8px'
        }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
            Observed Telemetry
          </div>
          <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.2rem' }}>
            {quote.observedDistanceKm !== null ? `${quote.observedDistanceKm} km` : '0.0 km'}
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
            Over {quote.observationDays !== null ? `${quote.observationDays} days` : '0 days'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
            Excluded Trips: {quote.excludedTripsCount || 0}
          </div>
        </div>
      </div>

      {/* Transparent Factor Breakdown */}
      <div style={{
        background: 'rgba(0, 0, 0, 0.25)',
        borderRadius: '8px',
        border: '1px solid var(--border-color)',
        padding: '1rem',
        marginBottom: '1.25rem'
      }}>
        <h4 style={{ margin: '0 0 0.75rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
          Detailed Actuarial Factor Breakdown &amp; Explanations
        </h4>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
          {quote.factorExplanations && quote.factorExplanations.length > 0 ? (
            quote.factorExplanations.map((factor, idx) => (
              <div key={idx} style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '0.6rem',
                fontSize: '0.84rem',
                padding: '0.45rem 0',
                borderBottom: idx < quote.factorExplanations.length - 1 ? '1px solid rgba(255, 255, 255, 0.05)' : 'none'
              }}>
                <span style={{
                  display: 'inline-block',
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: factor.status === 'APPLIED' ? 'var(--success)' : factor.status === 'EXCLUDED' ? 'var(--danger)' : '#f59e0b',
                  marginTop: '5px',
                  flexShrink: 0
                }}></span>

                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                      {factor.description || factor.factorName.replace(/_/g, ' ')}
                    </span>
                    <span style={{ fontSize: '0.78rem', color: 'var(--accent)', fontFamily: 'monospace' }}>
                      {factor.appliedValue}
                    </span>
                  </div>
                  <div style={{ color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                    {factor.explanation}
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
              No factor explanations generated.
            </div>
          )}
        </div>
      </div>

      {/* Prototype Actuarial Disclaimer */}
      <div style={{
        padding: '0.75rem 1rem',
        background: 'rgba(56, 189, 248, 0.05)',
        border: '1px solid rgba(56, 189, 248, 0.2)',
        borderRadius: '8px',
        fontSize: '0.75rem',
        color: 'var(--text-muted)',
        lineHeight: 1.45
      }}>
        <strong style={{ color: 'var(--accent)' }}>Prototype Actuarial Disclaimer: </strong>
        This is a hackathon demonstration for Problem Statement PS-008 using illustrative pricing assumptions. The formula, base tariff, and multipliers are demonstration parameters and are not actuarially validated or regulator-approved. Does not represent a binding insurance offer, underwriting guarantee, or policy change.
      </div>
    </div>
  );
}
