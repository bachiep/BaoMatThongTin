import { useEffect, useState } from 'react';
import { RefreshCw, Trash2 } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../state/AuthContext.jsx';

export default function EmployeesPage() {
  const { hasPermission } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadEmployees = async () => {
    setError('');
    setLoading(true);
    try {
      const { data } = await api.get('/employees');
      setEmployees(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Cannot load employees');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEmployees();
  }, []);

  const deleteEmployee = async (id) => {
    if (!window.confirm(`Delete employee ${id}?`)) {
      return;
    }
    try {
      await api.delete(`/employees/${id}`);
      await loadEmployees();
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <>
      <PageHeader
        title="Employee Management"
        subtitle="Employees are the protected business data in this security demo."
        action={
          <button className="btn btn-outline-secondary btn-sm icon-button" onClick={loadEmployees}>
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
                <th>Name</th>
                <th>Department</th>
                <th>Phone</th>
                <th>User</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading && <tr><td colSpan="6">Loading...</td></tr>}
              {!loading && employees.map((employee) => (
                <tr key={employee.id}>
                  <td>{employee.id}</td>
                  <td>{employee.fullName}</td>
                  <td>{employee.department || '-'}</td>
                  <td>{employee.phoneNumber || '-'}</td>
                  <td>{employee.username || employee.userId || '-'}</td>
                  <td className="text-end">
                    {hasPermission('EMPLOYEE_DELETE') ? (
                      <button className="btn btn-outline-danger btn-sm icon-only" onClick={() => deleteEmployee(employee.id)} title="Delete employee">
                        <Trash2 size={16} />
                      </button>
                    ) : (
                      <span className="text-muted small">View only</span>
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
