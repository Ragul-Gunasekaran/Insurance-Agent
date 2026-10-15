import React from 'react';

const PIPELINE_MODULES = [
  { id: '1', name: 'System Skeleton & Configuration', status: 'Completed', tag: 'Step 1' },
  { id: '2', name: 'Vehicle Registration & Verification', status: 'Up Next', tag: 'Step 2' },
  { id: '3', name: 'Telematics Ingestion & Validation', status: 'Planned', tag: 'Step 3' },
  { id: '4', name: 'Trip Stitching & Feature Engineering', status: 'Planned', tag: 'Step 4' },
  { id: '5', name: 'Behavioral Risk Scoring Engine', status: 'Planned', tag: 'Step 5' },
  { id: '6', name: 'Actuarial Pricing & Explanation', status: 'Planned', tag: 'Step 6' },
  { id: '7', name: 'Customer & Insurer Dashboards', status: 'Planned', tag: 'Step 7' }
];

export default function ArchitectureOverviewCard() {
  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          Telematics Architecture Pipeline
        </h2>
        <span className="badge badge-info">Modular Monolith</span>
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', marginBottom: '1rem' }}>
        The platform decouples raw sensor processing, behavioral risk scoring, and actuarial pricing into distinct, testable modules:
      </p>

      <div className="pipeline-steps">
        {PIPELINE_MODULES.map((step) => {
          let badgeClass = 'badge-info';
          if (step.status === 'Completed') badgeClass = 'badge-success';
          if (step.status === 'Up Next') badgeClass = 'badge-warning';

          return (
            <div key={step.id} className="step-item">
              <span className="step-num">{step.id}</span>
              <span className="step-name">{step.name}</span>
              <span className={`badge ${badgeClass} step-status`}>
                {step.status}
              </span>
            </div>
          );
        })}
      </div>

      <div style={{
        marginTop: '1.25rem',
        padding: '0.75rem 1rem',
        background: 'rgba(255, 255, 255, 0.03)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-sm)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        fontSize: '0.85rem'
      }}>
        <span style={{ color: 'var(--text-muted)' }}>Current Project Phase:</span>
        <span style={{ color: '#a5b4fc', fontWeight: 600 }}>Step 1: Setup & Architecture Completed</span>
      </div>
    </div>
  );
}
