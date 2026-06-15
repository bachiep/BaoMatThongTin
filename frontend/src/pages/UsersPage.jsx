import { useEffect, useState } from 'react';
import { Lock, Plus, RefreshCw, Trash2, Unlock } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../state/AuthContext.jsx';

const initialCreateForm = {
  username: '',
  email: '',
  password: 'Password@123',
  role: 'EMPLOYEE',
};

export default function UsersPage() {
  const { hasPermission, hasRole, currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [createForm, setCreateForm] = useState(initialCreateForm);

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

  const createUser = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    setCreating(true);
    try {
      const payload = {
        username: createForm.username.trim(),
        email: createForm.email.trim(),
        password: createForm.password,
        roles: [createForm.role],
      };
      const { data } = await api.post('/users', payload);
      setSuccess(`Created ${data.username} with ${createForm.role} role`);
      setCreateForm(initialCreateForm);
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Create user failed');
    } finally {
      setCreating(false);
    }
  };

  const patchUser = async (id, action) => {
    setError('');
    setSuccess('');
    try {
      await api.patch(`/users/${id}/${action}`);
      setSuccess(`User ${action} completed`);
      await loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed');
    }
  };

  const deleteUser = async (id) => {
    if (!window.confirm(`Delete user ${id}?`)) {
      return;
    }
    setError('');
    setSuccess('');
    try {
      await api.delete(`/users/${id}`);
      setSuccess(`Deleted user ${id}`);
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
      <AlertBox type="success" message={success} />

      {hasRole('ADMIN') && hasPermission('USER_CREATE') && (
        <section className="panel mb-3">
          <div className="panel-heading">
            <Plus size={18} />
            <h2>Create user account</h2>
          </div>
          <form className="user-create-grid" onSubmit={createUser}>
            <div>
              <label className="form-label">Username</label>
              <input
                className="form-control"
                value={createForm.username}
                onChange={(event) => setCreateForm({ ...createForm, username: event.target.value })}
                minLength={3}
                maxLength={50}
                required
              />
            </div>
            <div>
              <label className="form-label">Email</label>
              <input
                className="form-control"
                type="email"
                value={createForm.email}
                onChange={(event) => setCreateForm({ ...createForm, email: event.target.value })}
                required
              />
            </div>
            <div>
              <label className="form-label">Password</label>
              <input
                className="form-control"
                type="password"
                value={createForm.password}
                onChange={(event) => setCreateForm({ ...createForm, password: event.target.value })}
                minLength={8}
                maxLength={72}
                required
              />
            </div>
            <div>
              <label className="form-label">Role</label>
              <select
                className="form-select"
                value={createForm.role}
                onChange={(event) => setCreateForm({ ...createForm, role: event.target.value })}
              >
                <option value="EMPLOYEE">EMPLOYEE</option>
                <option value="MANAGER">MANAGER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <button className="btn btn-primary icon-button user-create-submit" disabled={creating}>
              <Plus size={16} />
              {creating ? 'Creating...' : 'Create'}
            </button>
          </form>
        </section>
      )}

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
