import { Navigate } from 'react-router-dom';
import { useAuth } from '../state/AuthContext.jsx';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, loadingUser } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (loadingUser) {
    return <div className="page-loader">Loading secure session...</div>;
  }

  return children;
}
