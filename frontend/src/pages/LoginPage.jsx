import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BriefcaseBusiness, KeyRound, ShieldCheck, UserRound } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import { useAuth } from '../state/AuthContext.jsx';

const demoAccounts = [
  {
    role: 'ADMIN',
    username: 'admin',
    password: 'Password@123',
    icon: ShieldCheck,
  },
  {
    role: 'MANAGER',
    username: 'manager',
    password: 'Password@123',
    icon: BriefcaseBusiness,
  },
  {
    role: 'EMPLOYEE',
    username: 'employee',
    password: 'Password@123',
    icon: UserRound,
  },
];

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

  const selectAccount = (account) => {
    setError('');
    setForm({ username: account.username, password: account.password });
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-icon"><KeyRound size={28} /></div>
        <h1>SEMS Login</h1>
        <p>Authenticate with password before email OTP verification.</p>
        <AlertBox message={error} />
        <div className="demo-account-grid" aria-label="Demo accounts">
          {demoAccounts.map((account) => {
            const Icon = account.icon;
            const selected = form.username === account.username;
            return (
              <button
                key={account.username}
                className={`demo-account ${selected ? 'selected' : ''}`}
                type="button"
                onClick={() => selectAccount(account)}
              >
                <Icon size={18} />
                <span>
                  <strong>{account.role}</strong>
                  <small>{account.username}</small>
                </span>
              </button>
            );
          })}
        </div>
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
