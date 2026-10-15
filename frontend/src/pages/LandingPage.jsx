import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import BackendStatusCard from '../components/BackendStatusCard';
import ArchitectureOverviewCard from '../components/ArchitectureOverviewCard';
import VehicleFleetView from '../components/VehicleFleetView';
import TelemetryView from '../components/TelemetryView';
import { useBackendStatus } from '../hooks/useBackendStatus';

export default function LandingPage() {
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedVehicleId, setSelectedVehicleId] = useState(null);

  const {
    status,
    healthData,
    infoData,
    errorMessage,
    lastChecked,
    checkConnection
  } = useBackendStatus();

  const handleNavigateToTelemetry = (vehicleId) => {
    setSelectedVehicleId(vehicleId);
    setActiveTab('telemetry');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar activeTab={activeTab} onTabChange={setActiveTab} />

      <main className="container" style={{ flex: 1, paddingTop: activeTab === 'overview' ? '0' : '2.5rem' }}>
        {/* Tab 1: Overview & System Health */}
        {activeTab === 'overview' && (
          <>
            <section className="hero-section">
              <div className="hero-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                </svg>
                Hackathon Problem Statement PS-008
              </div>

              <h1 className="hero-title">
                Usage-Based <span className="hero-gradient-text">Insurance</span> Platform
              </h1>

              <p className="hero-description">
                Modernize automotive insurance through IoT telematics. By continuously assessing actual driving
                distance, harsh braking, and speed discipline, our actuarial engine computes a personalized,
                transparent safety score and fair premium discounts.
              </p>

              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.85rem', marginBottom: '2.5rem' }}>
                <button
                  className="btn btn-primary"
                  onClick={() => setActiveTab('vehicles')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <rect x="1" y="3" width="15" height="13" />
                    <polygon points="16 8 20 8 23 11 23 16 16 16 8" />
                  </svg>
                  Manage Vehicle Fleet (Step 2)
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setActiveTab('telemetry')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                  </svg>
                  Stream IoT Telematics (Step 3)
                </button>
              </div>
            </section>

            <section className="grid-two-col">
              <BackendStatusCard
                status={status}
                healthData={healthData}
                infoData={infoData}
                errorMessage={errorMessage}
                lastChecked={lastChecked}
                onRefresh={checkConnection}
              />

              <ArchitectureOverviewCard />
            </section>
          </>
        )}

        {/* Tab 2: Vehicle Fleet Onboarding */}
        {activeTab === 'vehicles' && (
          <VehicleFleetView onNavigateToTelemetry={handleNavigateToTelemetry} />
        )}

        {/* Tab 3: IoT Telematics Ingestion & Simulator */}
        {activeTab === 'telemetry' && (
          <TelemetryView initialVehicleId={selectedVehicleId} />
        )}
      </main>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <p>
            Usage-Based Insurance (UBI) Platform Prototype &bull; Step 1, 2 &amp; 3 Complete
          </p>
          <p style={{ marginTop: '0.4rem', fontSize: '0.78rem' }}>
            Built with React 18, Vite 5, Spring Boot 3.3.4 (Java 21 LTS), and MySQL.
          </p>
        </div>
      </footer>
    </div>
  );
}
