import React, { useState } from 'react';
import TripHistoryTable from './TripHistoryTable';
import TripDetailCard from './TripDetailCard';

export default function TripsInsightsView({ initialVehicleId }) {
  const [selectedVehicleId, setSelectedVehicleId] = useState(initialVehicleId || null);
  const [selectedTrip, setSelectedTrip] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleSelectTrip = (trip) => {
    setSelectedTrip(trip);
    window.scrollTo({ top: 120, behavior: 'smooth' });
  };

  const handleFeaturesUpdated = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem' }}>
          Trip Stitching &amp; Behavioral Feature Engine (Step 4)
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '840px' }}>
          Automatically segments high-frequency telematics pings into distinct driving sessions based on movement
          and inactivity timeouts. Computes Haversine trip distances, debounced harsh event counts, and night driving exposures.
        </p>
      </div>

      <TripDetailCard
        trip={selectedTrip}
        onFeaturesUpdated={handleFeaturesUpdated}
      />

      <TripHistoryTable
        selectedVehicleId={selectedVehicleId}
        onVehicleChange={setSelectedVehicleId}
        onSelectTrip={handleSelectTrip}
        refreshTrigger={refreshTrigger}
      />
    </div>
  );
}
