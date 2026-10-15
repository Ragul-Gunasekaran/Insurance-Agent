import React, { useEffect, useState } from 'react';
import { fetchVehicles } from '../services/apiService';

export default function VehicleListTable({ onSelectVehicle, refreshTrigger }) {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const loadVehicles = async (pageToLoad = 0) => {
    setLoading(true);
    setError(null);
    const result = await fetchVehicles(pageToLoad, 6);
    setLoading(false);

    if (result.success && result.data) {
      setVehicles(result.data.content || []);
      setPage(result.data.page);
      setTotalPages(result.data.totalPages || 1);
    } else {
      setError(result.error || 'Failed to load vehicles');
    }
  };

  useEffect(() => {
    loadVehicles(page);
  }, [refreshTrigger, page]);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'VERIFIED':
        return <span className="badge badge-success" style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}>Verified</span>;
      case 'REJECTED':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}>Rejected</span>;
      case 'FLAGGED':
        return <span className="badge badge-warning" style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}>Flagged</span>;
      case 'UNVERIFIED':
      default:
        return <span className="badge badge-info" style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}>Unverified</span>;
    }
  };

  return (
    <div className="card" style={{ marginTop: '1.5rem' }}>
      <div className="card-header">
        <h3 className="card-title" style={{ fontSize: '1.15rem' }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="8" y1="6" x2="21" y2="6" />
            <line x1="8" y1="12" x2="21" y2="12" />
            <line x1="8" y1="18" x2="21" y2="18" />
            <line x1="3" y1="6" x2="3.01" y2="6" />
            <line x1="3" y1="12" x2="3.01" y2="12" />
            <line x1="3" y1="18" x2="3.01" y2="18" />
          </svg>
          Registered Platform Vehicles
        </h3>
        <button
          className="btn btn-secondary"
          onClick={() => loadVehicles(page)}
          disabled={loading}
          style={{ fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}
        >
          {loading ? 'Refreshing...' : 'Refresh List'}
        </button>
      </div>

      {loading && vehicles.length === 0 && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          <span className="spinner" style={{ marginRight: '0.5rem' }} /> Loading registered fleet...
        </div>
      )}

      {error && (
        <div style={{ padding: '1rem', color: 'var(--color-danger)', fontSize: '0.88rem' }}>
          {error}
        </div>
      )}

      {!loading && vehicles.length === 0 && !error && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          No vehicles registered yet. Submit the registration form above to onboard your first vehicle.
        </div>
      )}

      {vehicles.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '0.75rem 0.5rem' }}>ID</th>
                <th style={{ padding: '0.75rem 0.5rem' }}>Reg Number</th>
                <th style={{ padding: '0.75rem 0.5rem' }}>Vehicle</th>
                <th style={{ padding: '0.75rem 0.5rem' }}>Year</th>
                <th style={{ padding: '0.75rem 0.5rem' }}>Fuel / Type</th>
                <th style={{ padding: '0.75rem 0.5rem' }}>Status</th>
                <th style={{ padding: '0.75rem 0.5rem', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.map((v) => (
                <tr key={v.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                  <td style={{ padding: '0.75rem 0.5rem', fontFamily: 'var(--font-mono)' }}>#{v.id}</td>
                  <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600, color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                    {v.vehicleNumber}
                  </td>
                  <td style={{ padding: '0.75rem 0.5rem' }}>{v.make} {v.model}</td>
                  <td style={{ padding: '0.75rem 0.5rem' }}>{v.manufacturingYear}</td>
                  <td style={{ padding: '0.75rem 0.5rem' }}>{v.fuelType} &bull; {v.vehicleType}</td>
                  <td style={{ padding: '0.75rem 0.5rem' }}>{getStatusBadge(v.verificationStatus)}</td>
                  <td style={{ padding: '0.75rem 0.5rem', textAlign: 'right' }}>
                    <button
                      className="btn btn-secondary"
                      onClick={() => onSelectVehicle(v)}
                      style={{ fontSize: '0.75rem', padding: '0.25rem 0.6rem' }}
                    >
                      View / Verify
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-subtle)' }}>
              <button
                className="btn btn-secondary"
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem' }}
              >
                Previous
              </button>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Page {page + 1} of {totalPages}
              </span>
              <button
                className="btn btn-secondary"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem' }}
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
