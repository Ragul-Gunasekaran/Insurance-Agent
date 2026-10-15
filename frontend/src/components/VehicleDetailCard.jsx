import React, { useState } from 'react';
import { verifyVehicle } from '../services/apiService';

export default function VehicleDetailCard({ vehicle, onVerificationUpdated }) {
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [verificationError, setVerificationError] = useState(null);

  if (!vehicle) return null;

  const handleVerify = async () => {
    setVerifying(true);
    setVerificationError(null);

    const result = await verifyVehicle({
      vehicleId: vehicle.id,
      vehicleNumber: vehicle.vehicleNumber,
      make: vehicle.make,
      model: vehicle.model,
      manufacturingYear: vehicle.manufacturingYear
    });

    setVerifying(false);

    if (result.success) {
      setVerificationResult(result.data);
      if (onVerificationUpdated) {
        onVerificationUpdated({
          ...vehicle,
          verificationStatus: result.data.verificationStatus,
          verificationNotes: result.data.explanation
        });
      }
    } else {
      setVerificationError(result.error || 'Failed to complete mock verification.');
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'VERIFIED':
        return <span className="badge badge-success">Verified (Mock)</span>;
      case 'REJECTED':
        return <span className="badge badge-danger">Rejected</span>;
      case 'FLAGGED':
        return <span className="badge badge-warning">Flagged (Inspection)</span>;
      case 'UNVERIFIED':
      default:
        return <span className="badge badge-info">Unverified</span>;
    }
  };

  return (
    <div className="card" style={{ border: '1px solid var(--border-accent)', marginTop: '1.5rem' }}>
      <div className="card-header">
        <h3 className="card-title" style={{ fontSize: '1.15rem' }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          </svg>
          Active Vehicle Profile: <span style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>{vehicle.vehicleNumber}</span>
        </h3>
        <div>
          {getStatusBadge(vehicle.verificationStatus)}
        </div>
      </div>

      <div className="metrics-list" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', display: 'grid', gap: '1rem' }}>
        <div className="metric-row">
          <span className="metric-label">Vehicle ID</span>
          <span className="metric-value">#{vehicle.id}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Make & Model</span>
          <span className="metric-value">{vehicle.make} {vehicle.model}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Manufacturing Year</span>
          <span className="metric-value">{vehicle.manufacturingYear}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Propulsion / Fuel</span>
          <span className="metric-value">{vehicle.fuelType}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Classification</span>
          <span className="metric-value">{vehicle.vehicleType}</span>
        </div>
        <div className="metric-row">
          <span className="metric-label">Engine Capacity</span>
          <span className="metric-value">{vehicle.engineCapacityCc ? `${vehicle.engineCapacityCc} CC` : 'Electric / Zero CC'}</span>
        </div>
      </div>

      {/* Mock Verification Trigger Section */}
      <div style={{
        marginTop: '1.5rem',
        padding: '1.25rem',
        background: 'rgba(255, 255, 255, 0.02)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-sm)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h4 style={{ fontSize: '0.95rem', marginBottom: '0.2rem' }}>Vehicle Identity Verification</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', margin: 0 }}>
              Simulates automated underwriting verification against mock motor registry rules.
            </p>
          </div>

          <button
            className="btn btn-secondary"
            onClick={handleVerify}
            disabled={verifying}
            style={{ fontSize: '0.85rem', padding: '0.5rem 1rem' }}
          >
            {verifying ? (
              <>
                <span className="spinner" />
                Running Verification...
              </>
            ) : (
              <>
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                Run Mock Verification (Demo)
              </>
            )}
          </button>
        </div>

        {verificationError && (
          <div style={{ marginTop: '1rem', color: 'var(--color-danger)', fontSize: '0.85rem' }}>
            {verificationError}
          </div>
        )}

        {verificationResult && (
          <div style={{
            marginTop: '1.25rem',
            padding: '1rem',
            background: verificationResult.verified ? 'var(--color-success-bg)' : 'var(--color-warning-bg)',
            border: `1px solid ${verificationResult.verified ? 'var(--color-success-border)' : 'var(--color-warning-border)'}`,
            borderRadius: 'var(--radius-sm)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontWeight: 600, fontSize: '0.9rem', color: verificationResult.verified ? 'var(--color-success)' : 'var(--color-warning)' }}>
                Verification Outcome: {verificationResult.verificationStatus}
              </span>
              <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                Provider: {verificationResult.provider}
              </span>
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)', marginBottom: '0.65rem' }}>
              {verificationResult.explanation}
            </p>

            {verificationResult.verificationDetails && (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.5rem' }}>
                {Object.entries(verificationResult.verificationDetails).map(([k, v]) => (
                  <span key={k} style={{
                    fontSize: '0.74rem',
                    padding: '0.2rem 0.5rem',
                    background: 'rgba(0, 0, 0, 0.25)',
                    borderRadius: '4px',
                    fontFamily: 'var(--font-mono)'
                  }}>
                    {k}: {v}
                  </span>
                ))}
              </div>
            )}

            <div style={{ marginTop: '0.75rem', fontSize: '0.75rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
              Notice: This is a simulated demo check for Hackathon PS-008. It does not connect to government APIs.
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
