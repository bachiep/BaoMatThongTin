import { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';

export default function AuditLogPage() {
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadLogs = async () => {
    setError('');
    setLoading(true);
    try {
      const { data } = await api.get('/audit-logs?page=0&size=20');
      setLogs(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Cannot load audit logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
  }, []);

  return (
    <>
      <PageHeader
        title="Audit Log"
        subtitle="Security-sensitive actions written by the backend."
        action={<button className="btn btn-outline-secondary btn-sm icon-button" onClick={loadLogs}><RefreshCw size={16} /> Refresh</button>}
      />
      <AlertBox message={error} />
      <section className="panel">
        <table className="table align-middle">
          <thead><tr><th>User</th><th>Action</th><th>IP</th><th>Time</th></tr></thead>
          <tbody>
            {loading && <tr><td colSpan="4">Loading...</td></tr>}
            {!loading && logs.map((log) => (
              <tr key={log.id}>
                <td>{log.performedBy}</td>
                <td><code>{log.action}</code></td>
                <td>{log.ipAddress || '-'}</td>
                <td>{new Date(log.timestamp).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
