import React, { useState } from 'react';
import VehicleRegistrationForm from './VehicleRegistrationForm';
import VehicleListTable from './VehicleListTable';
import VehicleDetailCard from './VehicleDetailCard';

export default function VehicleFleetView({ onNavigateToTelemetry }) {
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleVehicleRegistered = (newVehicle) => {
    setSelectedVehicle(newVehicle);
    setRefreshTrigger(prev => prev + 1);
  };

  const handleSelectVehicle = (vehicle) => {
    setSelectedVehicle(vehicle);
    window.scrollTo({ top: 300, behavior: 'smooth' });
  };

  const handleVerificationUpdated = (updatedVehicle) => {
    setSelectedVehicle(updatedVehicle);
    setRefreshTrigger(prev => prev + 1);
  };

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem' }}>
          Vehicle Fleet & Verification Hub
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '800px' }}>
          Register policyholder vehicles, inspect normalized motor profiles, and run automated verification checks
          against safety databases before streaming IoT telematics pings.
        </p>
      </div>

      <div className="grid-two-col">
        <VehicleRegistrationForm onVehicleRegistered={handleVehicleRegistered} />
        {selectedVehicle ? (
          <div>
            <VehicleDetailCard
              vehicle={selectedVehicle}
              onVerificationUpdated={handleVerificationUpdated}
            />
            {onNavigateToTelemetry && (
              <div style={{ marginTop: '1rem', textAlign: 'right' }}>
                <button
                  className="btn btn-primary"
                  onClick={() => onNavigateToTelemetry(selectedVehicle.id)}
                  style={{ width: '100%', fontSize: '0.9rem' }}
                >
                  Stream Telematics for {selectedVehicle.vehicleNumber} &rarr;
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', textAlign: 'center', padding: '3rem 2rem' }}>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>
              <rect x="1" y="3" width="15" height="13" />
              <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" />
              <circle cx="5.5" cy="18.5" r="2.5" />
              <circle cx="18.5" cy="18.5" r="2.5" />
            </svg>
            <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.5rem' }}>No Vehicle Selected</h4>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', maxWidth: '340px' }}>
              Select any vehicle from the fleet table below or onboard a new vehicle to inspect full specifications and run mock verification.
            </p>
          </div>
        )}
      </div>

      <VehicleListTable
        onSelectVehicle={handleSelectVehicle}
        refreshTrigger={refreshTrigger}
      />
    </div>
  );
}
