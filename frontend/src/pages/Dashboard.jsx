import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAccounts } from '../features/accounts/accountSlice';
import { fetchPortfolio } from '../features/stocks/stockSlice';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, 
  ResponsiveContainer, AreaChart, Area, BarChart, Bar 
} from 'recharts';

const mockBalanceHistory = [
  { month: 'Jan', balance: 42000 }, { month: 'Feb', balance: 45000 },
  { month: 'Mar', balance: 48000 }, { month: 'Apr', balance: 46000 },
  { month: 'May', balance: 52000 }, { month: 'Jun', balance: 55000 },
  { month: 'Jul', balance: 58000 }, { month: 'Aug', balance: 54000 },
  { month: 'Sep', balance: 60000 }, { month: 'Oct', balance: 63000 },
  { month: 'Nov', balance: 68000 }, { month: 'Dec', balance: 75000 },
];

const mockSystemVolume = [
  { day: 'Mon', volume: 1200000 }, { day: 'Tue', volume: 1500000 },
  { day: 'Wed', volume: 1100000 }, { day: 'Thu', volume: 1800000 },
  { day: 'Fri', volume: 2100000 }, { day: 'Sat', volume: 900000 },
  { day: 'Sun', volume: 700000 },
];

export default function Dashboard() {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { accounts = [] } = useSelector((state) => state.accounts);
  const { portfolio } = useSelector((state) => state.stocks);

  useEffect(() => {
    if (user?.id && user?.role !== 'ADMIN') {
      dispatch(fetchAccounts(user.id));
      dispatch(fetchPortfolio(user.id));
    }
  }, [dispatch, user]);

  // --- ADMIN VIEW ---
  if (user?.role === 'ADMIN') {
    return (
      <div className="dashboard admin-dashboard">
        <header className="page-header">
          <div className="header-badge">SYSTEM OVERVIEW</div>
          <h1>Network Command</h1>
          <p className="page-subtitle">Real-time monitoring of VaultCore Financial ledger activity.</p>
        </header>

        <section className="stats-grid">
          <div className="stat-card">
            <span className="material-symbols-outlined stat-icon">group</span>
            <div className="stat-label">Active Users</div>
            <div className="stat-value">1,284</div>
            <div className="stat-trend positive">↑ 12% this month</div>
          </div>
          <div className="stat-card">
            <span className="material-symbols-outlined stat-icon">payments</span>
            <div className="stat-label">Daily Volume</div>
            <div className="stat-value">$2.4M</div>
            <div className="stat-trend positive">↑ 8.4% today</div>
          </div>
          <div className="stat-card">
            <span className="material-symbols-outlined stat-icon">shield</span>
            <div className="stat-label">Security Nodes</div>
            <div className="stat-value">Active</div>
            <div className="stat-trend neutral">All systems nominal</div>
          </div>
          <div className="stat-card">
            <span className="material-symbols-outlined stat-icon">memory</span>
            <div className="stat-label">Core Latency</div>
            <div className="stat-value">12ms</div>
            <div className="stat-trend positive">Optimized</div>
          </div>
        </section>

        <div className="dashboard-charts">
          <div className="chart-card wide">
            <div className="chart-header">
              <h3>Network Transaction Volume (Last 7 Days)</h3>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={mockSystemVolume}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis dataKey="day" stroke="var(--text-muted)" axisLine={false} tickLine={false} />
                <YAxis stroke="var(--text-muted)" axisLine={false} tickLine={false} tickFormatter={(v) => `$${v/1000000}M`} />
                <Tooltip 
                  contentStyle={{ background: 'var(--bg-surface-highest)', border: 'none', borderRadius: 'var(--radius-md)', color: 'var(--text-primary)' }}
                  cursor={{ fill: 'rgba(255, 255, 255, 0.05)' }}
                />
                <Bar dataKey="volume" fill="var(--accent-primary)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="chart-card">
            <h3>System Controls</h3>
            <div className="admin-actions-grid">
               <a href="/audit" className="admin-action-btn">
                  <span className="material-symbols-outlined">verified_user</span>
                  <div className="text">
                    <strong>Audit Logs</strong>
                    <span>Review immutable ledger</span>
                  </div>
               </a>
               <a href="/accounts" className="admin-action-btn">
                  <span className="material-symbols-outlined">manage_accounts</span>
                  <div className="text">
                    <strong>User Accounts</strong>
                    <span>Manage permissions</span>
                  </div>
               </a>
               <a href="/statements" className="admin-action-btn">
                  <span className="material-symbols-outlined">description</span>
                  <div className="text">
                    <strong>Statements</strong>
                    <span>Automated reports</span>
                  </div>
               </a>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // --- USER VIEW ---
  const totalBalance = accounts.reduce((sum, acc) => sum + parseFloat(acc.balance || 0), 0);

  return (
    <div className="dashboard">
      <header className="page-header">
        <span className="header-badge">WEALTH OVERVIEW</span>
        <h1>Welcome, {user?.fullName?.split(' ')[0] || 'User'}</h1>
        <p className="page-subtitle">Your unified financial command center.</p>
      </header>

      <section className="stats-grid">
        <div className="stat-card" style={{ gridColumn: '1 / -1' }}>
          <div className="stat-label">Total Net Liquidity</div>
          <div className="stat-value" style={{ fontSize: '48px' }}>${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
          <div className="stat-trend positive" style={{ marginTop: '12px' }}>
            <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>trending_up</span> +2.4% vs last month
          </div>
        </div>

      </section>

      <div className="dashboard-charts">
        <div className="chart-card wide">
          <h3>Asset Performance</h3>
          <ResponsiveContainer width="100%" height={350}>
            <AreaChart data={mockBalanceHistory}>
              <defs>
                <linearGradient id="balanceGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="var(--accent-primary)" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="var(--accent-primary)" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
              <XAxis dataKey="month" stroke="var(--text-muted)" axisLine={false} tickLine={false} />
              <YAxis stroke="var(--text-muted)" axisLine={false} tickLine={false} />
              <Tooltip 
                contentStyle={{ background: 'var(--bg-surface-highest)', border: 'none', borderRadius: 'var(--radius-md)', color: 'var(--text-primary)' }}
              />
              <Area type="monotone" dataKey="balance" stroke="var(--accent-primary)" fill="url(#balanceGrad)" strokeWidth={4} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Connected Accounts</h3>
          <div className="accounts-list-mini">
            {accounts.length > 0 ? accounts.map((acc) => (
              <div key={acc.id} className="account-mini-card">
                <div className="account-mini-info">
                  <span className="material-symbols-outlined" style={{ color: 'var(--accent-primary)' }}>account_balance</span>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '14px' }}>{acc.accountType}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>•••• {acc.accountNumber.slice(-4)}</div>
                  </div>
                </div>
                <div style={{ fontWeight: 800 }}>${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
              </div>
            )) : (
              <div className="empty-state">No accounts linked.</div>
            )}
          </div>
          <a href="/accounts" className="btn-text-link">Manage All Accounts →</a>
        </div>
        
        <div className="chart-card" style={{ background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)' }}>
          <h3 style={{ color: 'var(--accent-tertiary)' }}>System Reference</h3>
          <div className="demo-reference-list">
            <div className="demo-ref-item">
              <span className="demo-ref-label">Admin Target</span>
              <code className="demo-ref-value">VC-200001</code>
            </div>
            <div className="demo-ref-item">
              <span className="demo-ref-label">Funding Source</span>
              <code className="demo-ref-value">VC-100002</code>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
