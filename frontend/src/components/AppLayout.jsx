import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Activity, ClipboardList, History, LayoutDashboard, LogOut, Shield, Users, UserRoundCog } from 'lucide-react';
import { useAuth } from '../state/AuthContext.jsx';

export default function AppLayout() {
  const navigate = useNavigate();
  const { currentUser, logout, hasPermission } = useAuth();

  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, show: true },
    { to: '/employees', label: 'Employees', icon: Users, show: hasPermission('EMPLOYEE_VIEW') },
    { to: '/users', label: 'Users', icon: UserRoundCog, show: hasPermission('USER_VIEW') },
    { to: '/audit', label: 'Audit Log', icon: ClipboardList, show: hasPermission('AUDIT_VIEW') },
    { to: '/login-history', label: 'Login History', icon: History, show: hasPermission('LOGIN_HISTORY_VIEW') },
    { to: '/security', label: 'Security', icon: Shield, show: hasPermission('AUDIT_VIEW') || hasPermission('LOGIN_HISTORY_VIEW') },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <Activity size={22} />
          <span>SEMS</span>
        </div>
        <nav className="nav-list">
          {navItems.filter((item) => item.show).map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
      <main className="main-panel">
        <header className="topbar">
          <div>
            <div className="topbar-title">Security Administration Portal</div>
            <div className="topbar-subtitle">{currentUser?.username} · {currentUser?.roles?.join(', ')}</div>
          </div>
          <button className="btn btn-outline-secondary btn-sm icon-button" onClick={handleLogout}>
            <LogOut size={16} />
            Logout
          </button>
        </header>
        <section className="content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
