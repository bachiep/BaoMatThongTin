import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => localStorage.getItem('sems_token'));
  const [pendingUsername, setPendingUsername] = useState(() => localStorage.getItem('sems_pending_username') || '');
  const [currentUser, setCurrentUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(Boolean(token));

  const setToken = (nextToken) => {
    if (nextToken) {
      localStorage.setItem('sems_token', nextToken);
    } else {
      localStorage.removeItem('sems_token');
    }
    setTokenState(nextToken);
  };

  const setOtpUsername = (username) => {
    localStorage.setItem('sems_pending_username', username);
    setPendingUsername(username);
  };

  const loadCurrentUser = async () => {
    if (!localStorage.getItem('sems_token')) {
      setCurrentUser(null);
      setLoadingUser(false);
      return;
    }

    setLoadingUser(true);
    try {
      const { data } = await api.get('/auth/me');
      setCurrentUser(data);
    } finally {
      setLoadingUser(false);
    }
  };

  useEffect(() => {
    loadCurrentUser();
  }, [token]);

  const logout = () => {
    setToken(null);
    setCurrentUser(null);
    localStorage.removeItem('sems_pending_username');
    setPendingUsername('');
  };

  const hasPermission = (permission) => currentUser?.permissions?.includes(permission);
  const hasRole = (role) => currentUser?.roles?.includes(role);

  const value = useMemo(
    () => ({
      token,
      currentUser,
      loadingUser,
      pendingUsername,
      setOtpUsername,
      setToken,
      loadCurrentUser,
      logout,
      hasPermission,
      hasRole,
      isAuthenticated: Boolean(token),
    }),
    [token, currentUser, loadingUser, pendingUsername],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
