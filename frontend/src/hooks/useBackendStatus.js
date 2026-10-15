import { useState, useEffect, useCallback } from 'react';
import { fetchHealth, fetchInfo } from '../services/apiService';

/**
 * Custom React Hook for monitoring backend status and connectivity.
 */
export function useBackendStatus() {
  const [status, setStatus] = useState('idle'); // 'idle' | 'loading' | 'success' | 'error'
  const [healthData, setHealthData] = useState(null);
  const [infoData, setInfoData] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);
  const [lastChecked, setLastChecked] = useState(null);

  const checkConnection = useCallback(async () => {
    setStatus('loading');
    setErrorMessage(null);

    const [healthResult, infoResult] = await Promise.all([
      fetchHealth(),
      fetchInfo()
    ]);

    setLastChecked(new Date().toLocaleTimeString());

    if (healthResult.success) {
      setStatus('success');
      setHealthData(healthResult.data);
      if (infoResult.success) {
        setInfoData(infoResult.data);
      }
    } else {
      setStatus('error');
      setErrorMessage(healthResult.error || 'Failed to establish connection to Spring Boot backend.');
      setHealthData(null);
      setInfoData(null);
    }
  }, []);

  useEffect(() => {
    // Initial connection check on mount
    checkConnection();
  }, [checkConnection]);

  return {
    status,
    healthData,
    infoData,
    errorMessage,
    lastChecked,
    checkConnection
  };
}
