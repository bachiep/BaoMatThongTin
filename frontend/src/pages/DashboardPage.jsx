import { ShieldCheck, UserCheck } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../state/AuthContext.jsx';

export default function DashboardPage() {
  const { currentUser } = useAuth();

  return (
    <>
      <PageHeader
        title={`Welcome, ${currentUser?.username}`}
        subtitle="Current authenticated identity, roles, and permissions."
      />
      <div className="grid two">
        <section className="panel">
          <div className="panel-heading">
            <UserCheck size={20} />
            <h2>Identity</h2>
          </div>
          <dl className="detail-list">
            <dt>Username</dt>
            <dd>{currentUser?.username}</dd>
            <dt>Email</dt>
            <dd>{currentUser?.email}</dd>
            <dt>Employee ID</dt>
            <dd>{currentUser?.employeeId ?? 'Not linked'}</dd>
          </dl>
        </section>
        <section className="panel">
          <div className="panel-heading">
            <ShieldCheck size={20} />
            <h2>Authorization</h2>
          </div>
          <h3 className="section-label">Roles</h3>
          <div className="tag-row">
            {currentUser?.roles?.map((role) => <span className="tag role" key={role}>{role}</span>)}
          </div>
          <h3 className="section-label mt-4">Permissions</h3>
          <div className="tag-row">
            {currentUser?.permissions?.map((permission) => <span className="tag" key={permission}>{permission}</span>)}
          </div>
        </section>
      </div>
    </>
  );
}
