import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import { useAuth } from '../state/AuthContext.jsx';

export default function VerifyOtpPage() {
  const navigate = useNavigate();
  const { pendingUsername, setToken, loadCurrentUser } = useAuth();
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/auth/verify-otp', { username: pendingUsername, otp });
      setToken(data.accessToken);
      await loadCurrentUser();
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'OTP verification failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-icon"><ShieldCheck size={28} /></div>
        <h1>Verify OTP</h1>
        <p>Enter the email OTP for <strong>{pendingUsername || 'your account'}</strong>.</p>
        <AlertBox message={error} />
        <label className="form-label">OTP</label>
        <input
          className="form-control otp-input"
          value={otp}
          onChange={(event) => setOtp(event.target.value)}
          inputMode="numeric"
          maxLength={8}
          autoFocus
        />
        <button className="btn btn-primary w-100 mt-4" disabled={loading || !pendingUsername}>
          {loading ? 'Verifying...' : 'Verify'}
        </button>
      </form>
    </div>
  );
}
