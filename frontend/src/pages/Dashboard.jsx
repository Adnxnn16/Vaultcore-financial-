import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAccounts } from '../features/accounts/accountSlice';
import { fetchPortfolio } from '../features/stocks/stockSlice';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';

const mockBalanceHistory = [
  { month: 'Jan', balance: 42000 }, { month: 'Feb', balance: 45000 },
  { month: 'Mar', balance: 48000 }, { month: 'Apr', balance: 46000 },
  { month: 'May', balance: 52000 }, { month: 'Jun', balance: 55000 },
  { month: 'Jul', balance: 58000 }, { month: 'Aug', balance: 54000 },
  { month: 'Sep', balance: 60000 }, { month: 'Oct', balance: 63000 },
  { month: 'Nov', balance: 68000 }, { month: 'Dec', balance: 75000 },
];

export default function Dashboard() {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { accounts, loading } = useSelector((state) => state.accounts);
  const { portfolio } = useSelector((state) => state.stocks);

  useEffect(() => {
    if (user?.id) {
      dispatch(fetchAccounts(user.id));
      dispatch(fetchPortfolio(user.id));
    }
  }, [dispatch, user]);

  const totalBalance = accounts.reduce((sum, acc) => sum + parseFloat(acc.balance || 0), 0);

  return (
    <div className="dashboard">
      <div className="page-header">
        <h1>Welcome back, {user?.fullName?.split(' ')[0] || 'User'}</h1>
        <p className="page-subtitle">Here's your financial overview</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card gradient-blue">
          <div className="stat-icon">💰</div>
          <div className="stat-content">
            <span className="stat-label">Total Balance</span>
            <span className="stat-value">${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
          </div>
        </div>
        <div className="stat-card gradient-green">
          <div className="stat-icon">🏦</div>
          <div className="stat-content">
            <span className="stat-label">Accounts</span>
            <span className="stat-value">{accounts.length}</span>
          </div>
        </div>
        <div className="stat-card gradient-purple">
          <div className="stat-icon">📈</div>
          <div className="stat-content">
            <span className="stat-label">Portfolio Value</span>
            <span className="stat-value">${portfolio?.totalValue?.toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}</span>
          </div>
        </div>
        <div className="stat-card gradient-orange">
          <div className="stat-icon">{portfolio?.totalGainLoss >= 0 ? '🟢' : '🔴'}</div>
          <div className="stat-content">
            <span className="stat-label">Portfolio Gain/Loss</span>
            <span className="stat-value" style={{ color: portfolio?.totalGainLoss >= 0 ? '#00e676' : '#ff5252' }}>
              {portfolio?.totalGainLoss >= 0 ? '+' : ''}${portfolio?.totalGainLoss?.toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
            </span>
          </div>
        </div>
      </div>

      <div className="dashboard-charts">
        <div className="chart-card">
          <h3>Balance Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={mockBalanceHistory}>
              <defs>
                <linearGradient id="balanceGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6c5ce7" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#6c5ce7" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" />
              <XAxis dataKey="month" stroke="#a0aec0" />
              <YAxis stroke="#a0aec0" />
              <Tooltip contentStyle={{ background: '#1a202c', border: '1px solid #4a5568', borderRadius: '8px' }} />
              <Area type="monotone" dataKey="balance" stroke="#6c5ce7" fill="url(#balanceGrad)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Accounts Overview</h3>
          <div className="accounts-list-mini">
            {accounts.map((acc) => (
              <div key={acc.id} className="account-mini-card">
                <div className="account-mini-info">
                  <span className="account-type-badge">{acc.accountType}</span>
                  <span className="account-number">{acc.accountNumber}</span>
                </div>
                <span className="account-balance">${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
