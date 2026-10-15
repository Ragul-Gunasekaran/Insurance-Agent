import React, { useState, useEffect } from 'react';
import TelemetrySimulatorCard from './TelemetrySimulatorCard';
import TelemetryMetricsCard from './TelemetryMetricsCard';
import TelemetryLiveLogTable from './TelemetryLiveLogTable';
import { fetchVehicleTelemetrySummary } from '../services/apiService';

export default function TelemetryView({ initialVehicleId }) {
  const [selectedVehicleId, setSelectedVehicleId] = useState(initialVehicleId || null);
  const [summary, setSummary] = useState(null);
  const [loadingSummary, setLoadingSummary] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const loadSummary = async (vehicleId) => {
    if (!vehicleId) {
      setSummary(null);
      return;
    }
    setLoadingSummary(true);
    const res = await fetchVehicleTelemetrySummary(vehicleId);
    setLoadingSummary(false);
    if (res.success && res.data) {
      setSummary(res.data);
    } else {
      setSummary(null);
    }
  };

  useEffect(() => {
    if (initialVehicleId) {
      setSelectedVehicleId(initialVehicleId);
    }
  }, [initialVehicleId]);

  useEffect(() => {
    if (selectedVehicleId) {
      loadSummary(selectedVehicleId);
    }
  }, [selectedVehicleId, refreshTrigger]);

  const handleSimulationCompleted = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '0.5rem' }}>
          IoT Telematics Stream & Anomaly Cleaning Engine
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '820px' }}>
          Ingest high-frequency GPS, accelerometer, and speedometer readings from connected vehicles.
          Sensor noise and physical anomalies are filtered in real-time, extracting clean behavioral risk metrics.
        </p>
      </div>

      <div className="grid-two-col">
        <TelemetrySimulatorCard
          selectedVehicleId={selectedVehicleId}
          onVehicleChange={setSelectedVehicleId}
          onSimulationCompleted={handleSimulationCompleted}
        />

        <TelemetryMetricsCard
          summary={summary}
          loading={loadingSummary}
        />
      </div>

      <TelemetryLiveLogTable
        vehicleId={selectedVehicleId}
        refreshTrigger={refreshTrigger}
      />
    </div>
  );
}
