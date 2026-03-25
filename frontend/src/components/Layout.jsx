import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../features/auth/authSlice';

export default function Layout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="material-symbols-outlined logo-icon">shield</span>
            <span className="logo-text">VaultCore</span>
          </div>
        </div>
        
        <nav className="sidebar-nav">
          <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="material-symbols-outlined">dashboard</span> Dashboard
          </NavLink>
          
          {!isAdmin && (
            <>
              <NavLink to="/accounts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <span className="material-symbols-outlined">account_balance</span> Accounts
              </NavLink>
              <NavLink to="/transfer" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <span className="material-symbols-outlined">payments</span> Transfer
              </NavLink>
              <NavLink to="/stocks" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <span className="material-symbols-outlined">trending_up</span> Stocks
              </NavLink>
            </>
          )}

          <NavLink to="/statements" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="material-symbols-outlined">description</span> Statements
          </NavLink>

          {isAdmin && (
            <NavLink to="/audit" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="material-symbols-outlined">verified_user</span> Audit Log
            </NavLink>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{user?.fullName?.[0] || 'U'}</div>
            <div className="user-details">
              <span className="user-name">{user?.fullName || 'User'}</span>
              <span className="user-role">{user?.role || 'Account Holder'}</span>
            </div>
          </div>
          <button className="nav-link" onClick={handleLogout} style={{ width: '100%', cursor: 'pointer', border: 'none', background: 'transparent' }}>
            <span className="material-symbols-outlined">logout</span> Sign Out
          </button>
        </div>
      </aside>
      
      <main className="main-content">
        <Outlet />
      </main>
    </div>

  );
}
