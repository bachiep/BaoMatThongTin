import { Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from './components/AppLayout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import VerifyOtpPage from './pages/VerifyOtpPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import EmployeesPage from './pages/EmployeesPage.jsx';
import UsersPage from './pages/UsersPage.jsx';
import AuditLogPage from './pages/AuditLogPage.jsx';
import LoginHistoryPage from './pages/LoginHistoryPage.jsx';
import SecurityDashboardPage from './pages/SecurityDashboardPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/verify-otp" element={<VerifyOtpPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="employees" element={<EmployeesPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="audit" element={<AuditLogPage />} />
        <Route path="login-history" element={<LoginHistoryPage />} />
        <Route path="security" element={<SecurityDashboardPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
