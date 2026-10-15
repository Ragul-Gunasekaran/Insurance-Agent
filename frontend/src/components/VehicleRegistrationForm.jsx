import React, { useState } from 'react';
import { registerVehicle } from '../services/apiService';

const FUEL_TYPES = [
  { value: 'PETROL', label: 'Petrol' },
  { value: 'DIESEL', label: 'Diesel' },
  { value: 'ELECTRIC', label: 'Electric (EV)' },
  { value: 'HYBRID', label: 'Hybrid' },
  { value: 'CNG', label: 'Compressed Natural Gas (CNG)' }
];

const VEHICLE_TYPES = [
  { value: 'CAR', label: 'Sedan / Hatchback (Car)' },
  { value: 'SUV', label: 'SUV / Crossover' },
  { value: 'MOTORCYCLE', label: 'Motorcycle / Scooter' },
  { value: 'COMMERCIAL', label: 'Commercial Vehicle' },
  { value: 'OTHER', label: 'Other Classification' }
];

export default function VehicleRegistrationForm({ onVehicleRegistered }) {
  const currentYear = new Date().getFullYear();

  const [formData, setFormData] = useState({
    vehicleNumber: '',
    make: '',
    model: '',
    manufacturingYear: currentYear - 1,
    engineCapacityCc: 1498,
    fuelType: 'PETROL',
    vehicleType: 'CAR'
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState(null);

  // Validation function
  const validate = () => {
    const errors = {};
    const regPattern = /^[A-Za-z0-9 -]{4,20}$/;

    if (!formData.vehicleNumber.trim()) {
      errors.vehicleNumber = 'Registration number is required.';
    } else if (!regPattern.test(formData.vehicleNumber.trim())) {
      errors.vehicleNumber = 'Must be 4–20 alphanumeric characters (spaces & hyphens allowed).';
    }

    if (!formData.make.trim()) {
      errors.make = 'Manufacturer / make is required.';
    } else if (formData.make.trim().length < 2) {
      errors.make = 'Manufacturer name must be at least 2 characters.';
    }

    if (!formData.model.trim()) {
      errors.model = 'Vehicle model is required.';
    }

    const year = Number(formData.manufacturingYear);
    if (!year || isNaN(year)) {
      errors.manufacturingYear = 'Valid manufacturing year is required.';
    } else if (year < 1990) {
      errors.manufacturingYear = 'Year must be 1990 or newer.';
    } else if (year > currentYear + 1) {
      errors.manufacturingYear = `Year cannot exceed ${currentYear + 1}.`;
    }

    if (formData.fuelType !== 'ELECTRIC') {
      const cc = Number(formData.engineCapacityCc);
      if (!cc || isNaN(cc) || cc < 49) {
        errors.engineCapacityCc = 'Engine capacity must be at least 49 CC for combustion engines.';
      } else if (cc > 10000) {
        errors.engineCapacityCc = 'Engine capacity cannot exceed 10,000 CC.';
      }
    }

    if (!formData.fuelType) {
      errors.fuelType = 'Please select a fuel type.';
    }

    if (!formData.vehicleType) {
      errors.vehicleType = 'Please select a vehicle category.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
    // Clear field-specific error as user types
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: null
      }));
    }
    setServerError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError(null);

    if (!validate()) {
      return;
    }

    setSubmitting(true);

    const payload = {
      vehicleNumber: formData.vehicleNumber.trim(),
      make: formData.make.trim(),
      model: formData.model.trim(),
      manufacturingYear: parseInt(formData.manufacturingYear, 10),
      engineCapacityCc: formData.fuelType === 'ELECTRIC' ? null : parseInt(formData.engineCapacityCc, 10),
      fuelType: formData.fuelType,
      vehicleType: formData.vehicleType
    };

    const result = await registerVehicle(payload);
    setSubmitting(false);

    if (result.success) {
      onVehicleRegistered(result.data);
      // Reset registration number for next entry
      setFormData((prev) => ({
        ...prev,
        vehicleNumber: ''
      }));
    } else {
      setServerError(result.error || 'Failed to onboard vehicle.');
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="1" y="3" width="15" height="13" />
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8" />
            <circle cx="5.5" cy="18.5" r="2.5" />
            <circle cx="18.5" cy="18.5" r="2.5" />
          </svg>
          Vehicle Registration & Onboarding
        </h2>
        <span className="badge badge-info">Step 2 Feature</span>
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', marginBottom: '1.25rem' }}>
        Register vehicle specifications into the platform. Once onboarded, telematics streams and actuarial
        scoring models will link directly to this profile.
      </p>

      {serverError && (
        <div style={{
          background: 'var(--color-danger-bg)',
          border: '1px solid var(--color-danger-border)',
          borderRadius: 'var(--radius-sm)',
          padding: '0.85rem 1rem',
          color: '#fca5a5',
          fontSize: '0.88rem',
          marginBottom: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem'
        }}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <div>
            <strong>Registration Error:</strong> {serverError}
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
          {/* Registration Number */}
          <div>
            <label className="form-label" htmlFor="vehicleNumber">
              Registration Number <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <input
              id="vehicleNumber"
              name="vehicleNumber"
              type="text"
              placeholder="e.g. MH02AB1234 or DL01AB9999"
              value={formData.vehicleNumber}
              onChange={handleChange}
              className={`form-input ${fieldErrors.vehicleNumber ? 'input-error' : ''}`}
            />
            {fieldErrors.vehicleNumber && (
              <span className="error-text">{fieldErrors.vehicleNumber}</span>
            )}
            <small className="form-hint">Normalized automatically (uppercase, trimmed).</small>
          </div>

          {/* Manufacturer / Make */}
          <div>
            <label className="form-label" htmlFor="make">
              Manufacturer (Make) <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <input
              id="make"
              name="make"
              type="text"
              placeholder="e.g. Toyota, Honda, Hyundai, Tata"
              value={formData.make}
              onChange={handleChange}
              className={`form-input ${fieldErrors.make ? 'input-error' : ''}`}
            />
            {fieldErrors.make && (
              <span className="error-text">{fieldErrors.make}</span>
            )}
          </div>

          {/* Model */}
          <div>
            <label className="form-label" htmlFor="model">
              Vehicle Model <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <input
              id="model"
              name="model"
              type="text"
              placeholder="e.g. Camry, Civic, Creta, Nexon"
              value={formData.model}
              onChange={handleChange}
              className={`form-input ${fieldErrors.model ? 'input-error' : ''}`}
            />
            {fieldErrors.model && (
              <span className="error-text">{fieldErrors.model}</span>
            )}
          </div>

          {/* Manufacturing Year */}
          <div>
            <label className="form-label" htmlFor="manufacturingYear">
              Manufacturing Year <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <input
              id="manufacturingYear"
              name="manufacturingYear"
              type="number"
              min="1990"
              max={currentYear + 1}
              value={formData.manufacturingYear}
              onChange={handleChange}
              className={`form-input ${fieldErrors.manufacturingYear ? 'input-error' : ''}`}
            />
            {fieldErrors.manufacturingYear && (
              <span className="error-text">{fieldErrors.manufacturingYear}</span>
            )}
          </div>

          {/* Fuel Type */}
          <div>
            <label className="form-label" htmlFor="fuelType">
              Fuel Type <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <select
              id="fuelType"
              name="fuelType"
              value={formData.fuelType}
              onChange={handleChange}
              className="form-input"
            >
              {FUEL_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
            {fieldErrors.fuelType && (
              <span className="error-text">{fieldErrors.fuelType}</span>
            )}
          </div>

          {/* Vehicle Type */}
          <div>
            <label className="form-label" htmlFor="vehicleType">
              Vehicle Classification <span style={{ color: 'var(--color-danger)' }}>*</span>
            </label>
            <select
              id="vehicleType"
              name="vehicleType"
              value={formData.vehicleType}
              onChange={handleChange}
              className="form-input"
            >
              {VEHICLE_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
            {fieldErrors.vehicleType && (
              <span className="error-text">{fieldErrors.vehicleType}</span>
            )}
          </div>

          {/* Engine Capacity CC */}
          <div>
            <label className="form-label" htmlFor="engineCapacityCc">
              Engine Capacity (CC) {formData.fuelType === 'ELECTRIC' ? '(N/A for EV)' : '*'}
            </label>
            <input
              id="engineCapacityCc"
              name="engineCapacityCc"
              type="number"
              min="49"
              max="10000"
              placeholder={formData.fuelType === 'ELECTRIC' ? 'Not applicable for EV' : 'e.g. 1199, 1498, 1998'}
              disabled={formData.fuelType === 'ELECTRIC'}
              value={formData.fuelType === 'ELECTRIC' ? '' : formData.engineCapacityCc}
              onChange={handleChange}
              className={`form-input ${fieldErrors.engineCapacityCc ? 'input-error' : ''}`}
            />
            {fieldErrors.engineCapacityCc && (
              <span className="error-text">{fieldErrors.engineCapacityCc}</span>
            )}
          </div>
        </div>

        <div style={{ marginTop: '1.75rem', display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <span className="spinner" />
                Registering Vehicle...
              </>
            ) : (
              <>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                  <polyline points="17 21 17 13 7 13 7 21" />
                  <polyline points="7 3 7 8 15 8" />
                </svg>
                Complete Vehicle Registration
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
