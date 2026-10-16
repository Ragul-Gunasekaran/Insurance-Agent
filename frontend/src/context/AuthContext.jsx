import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { fetchCurrentUser, loginUser, logoutUser, registerCustomer, fetchCsrfToken } from '../services/apiService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState(null);

  // Restore authenticated session on initial mount
  const checkSession = useCallback(async () => {
    try {
      setLoading(true);
      // Initialize CSRF cookie
      await fetchCsrfToken();
      const res = await fetchCurrentUser();
      if (res.success && res.data) {
        setUser(res.data);
        setAuthError(null);
      } else {
        setUser(null);
      }
    } catch (err) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkSession();
  }, [checkSession]);

  const login = async (credentials) => {
    setAuthError(null);
    const res = await loginUser(credentials);
    if (res.success && res.data) {
      setUser(res.data);
      return { success: true, user: res.data };
    } else {
      setAuthError(res.error || 'Authentication failed');
      return { success: false, error: res.error };
    }
  };

  const register = async (credentials) => {
    setAuthError(null);
    const res = await registerCustomer(credentials);
    if (res.success && res.data) {
      // Auto-login after successful registration
      return await login({ email: credentials.email, password: credentials.password });
    } else {
      setAuthError(res.error || 'Registration failed');
      return { success: false, error: res.error };
    }
  };

  const logout = async () => {
    try {
      await logoutUser();
    } finally {
      setUser(null);
      setAuthError(null);
    }
  };

  const value = {
    user,
    role: user?.role || null,
    isInsurer: user?.role === 'INSURER',
    isCustomer: user?.role === 'CUSTOMER',
    isAuthenticated: !!user,
    loading,
    authError,
    setAuthError,
    login,
    register,
    logout,
    refreshUser: checkSession
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
