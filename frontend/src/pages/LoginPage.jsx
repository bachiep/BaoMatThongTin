import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { KeyRound } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import { useAuth } from '../state/AuthContext.jsx';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setOtpUsername } = useAuth();
  const [form, setForm] = useState({ username: 'admin', password: 'Password@123' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/login', form);
      setOtpUsername(form.username);
      navigate('/verify-otp');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-icon"><KeyRound size={28} /></div>
        <h1>SEMS Login</h1>
        <p>Authenticate with password before email OTP verification.</p>
        <AlertBox message={error} />
        <label className="form-label">Username</label>
        <input
          className="form-control"
          value={form.username}
          onChange={(event) => setForm({ ...form, username: event.target.value })}
          autoComplete="username"
        />
        <label className="form-label mt-3">Password</label>
        <input
          className="form-control"
          type="password"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          autoComplete="current-password"
        />
        <button className="btn btn-primary w-100 mt-4" disabled={loading}>
          {loading ? 'Sending OTP...' : 'Login'}
        </button>
      </form>
    </div>
  );
}
