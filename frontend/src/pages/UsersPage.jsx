import { useEffect, useState } from 'react';
import { Lock, RefreshCw, Trash2, Unlock } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../state/AuthContext.jsx';

export default function UsersPage() {
  const { hasPermission, currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadUsers = async () => {
    setError('');
    setLoading(true);
    try {
      const { data } = await api.get('/users');
      setUsers(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Cannot load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const patchUser = async (id, action) => {
    try {
      await api.patch(`/users/${id}/${action}`);
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed');
    }
  };

  const deleteUser = async (id) => {
    if (!window.confirm(`Delete user ${id}?`)) {
      return;
    }
    try {
      await api.delete(`/users/${id}`);
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <>
      <PageHeader
        title="User Management"
        subtitle="Admin-only account security and role assignment surface."
        action={
          <button className="btn btn-outline-secondary btn-sm icon-button" onClick={loadUsers}>
            <RefreshCw size={16} /> Refresh
          </button>
        }
      />
      <AlertBox message={error} />
      <section className="panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Locked</th>
                <th>Failed</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading && <tr><td colSpan="7">Loading...</td></tr>}
              {!loading && users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.roles?.join(', ')}</td>
                  <td>{user.locked ? <span className="badge text-bg-danger">Locked</span> : <span className="badge text-bg-success">Active</span>}</td>
                  <td>{user.failedAttempts}</td>
                  <td className="text-end action-cell">
                    {hasPermission('USER_EDIT') && currentUser?.id !== user.id && (
                      user.locked ? (
                        <button className="btn btn-outline-success btn-sm icon-only" onClick={() => patchUser(user.id, 'unlock')} title="Unlock user">
                          <Unlock size={16} />
                        </button>
                      ) : (
                        <button className="btn btn-outline-warning btn-sm icon-only" onClick={() => patchUser(user.id, 'lock')} title="Lock user">
                          <Lock size={16} />
                        </button>
                      )
                    )}
                    {hasPermission('USER_DELETE') && currentUser?.id !== user.id && (
                      <button className="btn btn-outline-danger btn-sm icon-only" onClick={() => deleteUser(user.id)} title="Delete user">
                        <Trash2 size={16} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
