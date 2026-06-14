import { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';

export default function LoginHistoryPage() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadItems = async () => {
    setError('');
    setLoading(true);
    try {
      const { data } = await api.get('/login-history?page=0&size=20');
      setItems(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Cannot load login history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadItems();
  }, []);

  return (
    <>
      <PageHeader
        title="Login History"
        subtitle="Authentication attempts with status and IP address."
        action={<button className="btn btn-outline-secondary btn-sm icon-button" onClick={loadItems}><RefreshCw size={16} /> Refresh</button>}
      />
      <AlertBox message={error} />
      <section className="panel">
        <table className="table align-middle">
          <thead><tr><th>User</th><th>Status</th><th>IP</th><th>Time</th></tr></thead>
          <tbody>
            {loading && <tr><td colSpan="4">Loading...</td></tr>}
            {!loading && items.map((item) => (
              <tr key={item.id}>
                <td>{item.username}</td>
                <td><span className={`badge ${item.status === 'SUCCESS' ? 'text-bg-success' : 'text-bg-danger'}`}>{item.status}</span></td>
                <td>{item.ipAddress || '-'}</td>
                <td>{new Date(item.loginTime).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
