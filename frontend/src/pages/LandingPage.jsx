import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import BackendStatusCard from '../components/BackendStatusCard';
import ArchitectureOverviewCard from '../components/ArchitectureOverviewCard';
import VehicleFleetView from '../components/VehicleFleetView';
import TelemetryView from '../components/TelemetryView';
import TripsInsightsView from '../components/TripsInsightsView';
import RiskScoringView from '../components/RiskScoringView';
import PricingView from '../components/PricingView';
import CustomerScorecardView from '../components/CustomerScorecardView';
import InsurerAnalyticsView from '../components/InsurerAnalyticsView';
import AuthModal from '../components/AuthModal';
import { useBackendStatus } from '../hooks/useBackendStatus';
import { useAuth } from '../context/AuthContext';

export default function LandingPage() {
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedVehicleId, setSelectedVehicleId] = useState(null);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [authModalMode, setAuthModalMode] = useState('login');

  const { user, isAuthenticated, isInsurer, isCustomer, role, login } = useAuth();

  const {
    status,
    healthData,
    infoData,
    errorMessage,
    lastChecked,
    checkConnection
  } = useBackendStatus();

  const handleOpenAuth = (mode = 'login') => {
    setAuthModalMode(mode);
    setIsAuthModalOpen(true);
  };

  const handleNavigateToTelemetry = (vehicleId) => {
    setSelectedVehicleId(vehicleId);
    setActiveTab('telemetry');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenAuth={handleOpenAuth}
      />

      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        initialMode={authModalMode}
      />

      <main className="container" style={{ flex: 1, paddingTop: activeTab === 'overview' ? '0' : '2.5rem' }}>
        {/* Tab 1: Overview & System Health */}
        {activeTab === 'overview' && (
          <>
            <section className="hero-section">
              <div className="hero-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
                Step 8: Authentication, Authorization &amp; Security Hardening
              </div>

              <h1 className="hero-title">
                Usage-Based <span className="hero-gradient-text">Insurance</span> Platform
              </h1>

              <p className="hero-description">
                Modernize automotive insurance through IoT telematics. Enforced with Spring Security 6 session-based
                authentication, customer-to-vehicle ownership verification, and role-based access controls for
                customers and insurer underwriters.
              </p>

              {/* Authentication Status Badge / Quick Access */}
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  padding: '0.6rem 1.25rem',
                  background: 'rgba(17, 24, 39, 0.75)',
                  border: '1px solid var(--border-accent)',
                  borderRadius: 'var(--radius-full)',
                  marginBottom: '2rem',
                  backdropFilter: 'var(--glass-blur)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span
                    style={{
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      background: isAuthenticated ? 'var(--color-success)' : 'var(--color-warning)',
                      boxShadow: isAuthenticated ? '0 0 8px var(--color-success)' : '0 0 8px var(--color-warning)'
                    }}
                  />
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    Session Status:
                  </span>
                  <strong style={{ fontSize: '0.85rem', color: 'var(--text-primary)' }}>
                    {isAuthenticated ? `${user?.email} (${role})` : 'Anonymous (Public Access)'}
                  </strong>
                </div>

                {!isAuthenticated && (
                  <button
                    className="btn btn-primary"
                    onClick={() => handleOpenAuth('login')}
                    style={{ padding: '0.25rem 0.75rem', fontSize: '0.78rem' }}
                  >
                    Sign In
                  </button>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.85rem', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
                <button
                  className="btn btn-primary"
                  onClick={() => setActiveTab('vehicles')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <rect x="1" y="3" width="15" height="13" />
                    <polygon points="16 8 20 8 23 11 23 16 16 16 8" />
                  </svg>
                  Fleet Hub (Step 2)
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setActiveTab('telemetry')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                  </svg>
                  IoT Telematics (Step 3)
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setActiveTab('trips')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <circle cx="12" cy="12" r="10" />
                    <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" />
                  </svg>
                  Trips &amp; Insights (Step 4)
                </button>
                <button
                  className="btn btn-outline"
                  onClick={() => setActiveTab('risk')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                  Risk Scoring Engine (Step 5)
                </button>
                <button
                  className="btn btn-outline"
                  onClick={() => setActiveTab('pricing')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <circle cx="12" cy="12" r="10" />
                    <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
                    <path d="M12 18V6" />
                  </svg>
                  Pricing &amp; Quotes (Step 6)
                </button>
                <button
                  className="btn btn-outline"
                  onClick={() => setActiveTab('scorecard')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="8.5" cy="7" r="4" />
                  </svg>
                  Customer Scorecard (Step 7)
                </button>
                <button
                  className="btn btn-outline"
                  onClick={() => setActiveTab('insurer')}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M3 3v18h18" />
                    <path d="M18 17V9" />
                    <path d="M13 17V5" />
                    <path d="M8 17v-3" />
                  </svg>
                  Insurer Analytics (Step 7)
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
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="Vehicle Fleet Management" />
          ) : (
            <VehicleFleetView onNavigateToTelemetry={handleNavigateToTelemetry} />
          )
        )}

        {/* Tab 3: IoT Telematics Ingestion & Simulator */}
        {activeTab === 'telemetry' && (
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="IoT Telematics & Simulator" />
          ) : (
            <TelemetryView initialVehicleId={selectedVehicleId} />
          )
        )}

        {/* Tab 4: Trip Stitching & Feature Engineering */}
        {activeTab === 'trips' && (
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="Trip Stitching & Insights" />
          ) : (
            <TripsInsightsView initialVehicleId={selectedVehicleId} />
          )
        )}

        {/* Tab 5: Behavioral Risk Scoring Engine */}
        {activeTab === 'risk' && (
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="Behavioral Risk Scores" />
          ) : (
            <RiskScoringView initialVehicleId={selectedVehicleId} />
          )
        )}

        {/* Tab 6: Actuarial Pricing Engine */}
        {activeTab === 'pricing' && (
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="Actuarial Pricing & Quotes" />
          ) : (
            <PricingView initialVehicleId={selectedVehicleId} />
          )
        )}

        {/* Tab 7.A: Customer Driver Scorecard */}
        {activeTab === 'scorecard' && (
          !isAuthenticated ? (
            <AuthRequiredNotice onOpenAuth={handleOpenAuth} feature="Customer Driver Scorecard" />
          ) : (
            <CustomerScorecardView initialVehicleId={selectedVehicleId} />
          )
        )}

        {/* Tab 7.B: Insurer Analytics (Step 8 Role Guarded) */}
        {activeTab === 'insurer' && (
          !isAuthenticated ? (
            <AuthRequiredNotice
              onOpenAuth={handleOpenAuth}
              feature="Fleet Underwriting & Insurer Analytics"
              requiredRole="INSURER"
            />
          ) : isCustomer ? (
            <ForbiddenNotice userEmail={user?.email} onOpenAuth={handleOpenAuth} />
          ) : (
            <InsurerAnalyticsView />
          )
        )}
      </main>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <p>
            Usage-Based Insurance (UBI) Platform Prototype &bull; Step 8 Complete (Authentication &amp; RBAC Hardened)
          </p>
          <p style={{ marginTop: '0.4rem', fontSize: '0.78rem' }}>
            Built with React 18, Vite 5, Spring Boot 3.3.4 (Java 21 LTS), Spring Security 6, and MySQL.
          </p>
        </div>
      </footer>
    </div>
  );
}

// Subcomponent: Authentication Required Notice
function AuthRequiredNotice({ onOpenAuth, feature, requiredRole }) {
  return (
    <div
      style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border-accent)',
        borderRadius: 'var(--radius-lg)',
        padding: '3rem 2rem',
        textAlign: 'center',
        maxWidth: '640px',
        margin: '2rem auto',
        backdropFilter: 'var(--glass-blur)'
      }}
    >
      <div
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          background: 'rgba(99, 102, 241, 0.15)',
          color: 'var(--accent-primary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 1.5rem auto'
        }}
      >
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
      </div>

      <h2 style={{ fontSize: '1.5rem', marginBottom: '0.75rem' }}>
        Authentication Required
      </h2>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', lineHeight: 1.6, marginBottom: '1.75rem' }}>
        {requiredRole === 'INSURER'
          ? `Access to ${feature} requires an authenticated INSURER underwriter account. Please sign in with insurer credentials.`
          : `To access ${feature}, please sign in or register a customer account. Private vehicles, telemetry, trips, and quotes are strictly ownership-protected.`}
      </p>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '0.85rem', flexWrap: 'wrap' }}>
        <button
          className="btn btn-primary"
          onClick={() => onOpenAuth('login')}
          style={{ padding: '0.65rem 1.5rem' }}
        >
          Sign In Now
        </button>
        {requiredRole !== 'INSURER' && (
          <button
            className="btn btn-outline"
            onClick={() => onOpenAuth('register')}
            style={{ padding: '0.65rem 1.5rem' }}
          >
            Create Customer Account
          </button>
        )}
      </div>
    </div>
  );
}

// Subcomponent: Forbidden Notice (403)
function ForbiddenNotice({ userEmail, onOpenAuth }) {
  return (
    <div
      style={{
        background: 'var(--bg-card)',
        border: '1px solid rgba(239, 68, 68, 0.4)',
        borderRadius: 'var(--radius-lg)',
        padding: '3rem 2rem',
        textAlign: 'center',
        maxWidth: '640px',
        margin: '2rem auto',
        backdropFilter: 'var(--glass-blur)'
      }}
    >
      <div
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          background: 'rgba(239, 68, 68, 0.15)',
          color: 'var(--color-danger)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 1.5rem auto'
        }}
      >
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="15" y1="9" x2="9" y2="15" />
          <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
      </div>

      <h2 style={{ fontSize: '1.5rem', marginBottom: '0.75rem', color: 'var(--color-danger)' }}>
        403 Forbidden &bull; Insurer Access Required
      </h2>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', lineHeight: 1.6, marginBottom: '1.5rem' }}>
        You are currently authenticated as <strong style={{ color: 'var(--text-primary)' }}>{userEmail}</strong> with role <span style={{ color: '#34d399', fontWeight: 600 }}>CUSTOMER</span>.
        Fleet-level analytics and underwriting views are restricted to users with the <span style={{ color: '#c084fc', fontWeight: 600 }}>INSURER</span> role.
      </p>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '0.85rem', flexWrap: 'wrap' }}>
        <button
          className="btn btn-outline"
          onClick={() => onOpenAuth('login')}
          style={{ padding: '0.65rem 1.5rem' }}
        >
          Switch to Insurer Account
        </button>
      </div>
    </div>
  );
}
