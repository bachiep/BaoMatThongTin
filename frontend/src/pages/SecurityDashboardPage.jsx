import { useEffect, useState } from 'react';
import { ClipboardList, History, Lock, ShieldAlert, Users } from 'lucide-react';
import api from '../api/client';
import AlertBox from '../components/AlertBox.jsx';
import PageHeader from '../components/PageHeader.jsx';

export default function SecurityDashboardPage() {
  const [metrics, setMetrics] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadMetrics() {
      setError('');
      try {
        const [usersRes, auditRes, historyRes] = await Promise.allSettled([
          api.get('/users'),
          api.get('/audit-logs?page=0&size=100'),
          api.get('/login-history?page=0&size=100'),
        ]);

        const users = usersRes.status === 'fulfilled' ? usersRes.value.data : [];
        const audit = auditRes.status === 'fulfilled' ? auditRes.value.data.content || [] : [];
        const history = historyRes.status === 'fulfilled' ? historyRes.value.data.content || [] : [];
        setMetrics({
          totalUsers: users.length,
          lockedUsers: users.filter((user) => user.locked).length,
          auditEvents: audit.length,
          loginEvents: history.length,
          failedLogins: history.filter((item) => item.status === 'FAILED').length,
        });
      } catch (err) {
        setError(err.response?.data?.message || 'Cannot load security dashboard');
      }
    }

    loadMetrics();
  }, []);

  const cards = [
    { label: 'Total Users', value: metrics?.totalUsers ?? '-', icon: Users },
    { label: 'Locked Accounts', value: metrics?.lockedUsers ?? '-', icon: Lock },
    { label: 'Login Events', value: metrics?.loginEvents ?? '-', icon: History },
    { label: 'Failed Logins', value: metrics?.failedLogins ?? '-', icon: ShieldAlert },
    { label: 'Audit Events', value: metrics?.auditEvents ?? '-', icon: ClipboardList },
  ];

  return (
    <>
      <PageHeader title="Security Dashboard" subtitle="Operational security status from existing backend logs." />
      <AlertBox message={error} />
      <div className="metric-grid">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <section className="metric-card" key={card.label}>
              <Icon size={22} />
              <span>{card.label}</span>
              <strong>{card.value}</strong>
            </section>
          );
        })}
      </div>
    </>
  );
}
