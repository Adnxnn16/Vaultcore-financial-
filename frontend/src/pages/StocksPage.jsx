import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchPortfolio } from '../features/stocks/stockSlice';
import { PieChart, Pie, Cell, BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#6c5ce7', '#00cec9', '#fd79a8', '#fdcb6e', '#00b894', '#e17055'];

const mockPriceHistory = [
  { date: 'Mon', AAPL: 190, GOOGL: 153, MSFT: 410, AMZN: 182, TSLA: 258 },
  { date: 'Tue', AAPL: 191, GOOGL: 154, MSFT: 412, AMZN: 183, TSLA: 260 },
  { date: 'Wed', AAPL: 189, GOOGL: 155, MSFT: 413, AMZN: 184, TSLA: 259 },
  { date: 'Thu', AAPL: 192, GOOGL: 156, MSFT: 414, AMZN: 185, TSLA: 261 },
  { date: 'Fri', AAPL: 192, GOOGL: 156, MSFT: 415, AMZN: 186, TSLA: 262 },
];

export default function StocksPage() {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { portfolio, loading } = useSelector((state) => state.stocks);

  useEffect(() => {
    if (user?.id) dispatch(fetchPortfolio(user.id));
  }, [dispatch, user]);

  const pieData = portfolio?.positions?.map((p) => ({
    name: p.symbol,
    value: parseFloat(p.totalValue),
  })) || [];

  const barData = portfolio?.positions?.map((p) => ({
    name: p.symbol,
    gainLoss: parseFloat(p.gainLoss),
    percent: parseFloat(p.gainLossPercent),
  })) || [];

  return (
    <div className="stocks-page">
      <div className="page-header">
        <h1>Stock Portfolio</h1>
        <p className="page-subtitle">Track your investments</p>
      </div>

      {loading ? (<div className="loading-spinner">Loading portfolio...</div>) : portfolio && (
        <>
          <div className="stats-grid">
            <div className="stat-card gradient-purple">
              <div className="stat-icon">💎</div>
              <div className="stat-content">
                <span className="stat-label">Total Value</span>
                <span className="stat-value">${portfolio.totalValue?.toLocaleString()}</span>
              </div>
            </div>
            <div className="stat-card gradient-blue">
              <div className="stat-icon">💵</div>
              <div className="stat-content">
                <span className="stat-label">Total Cost</span>
                <span className="stat-value">${portfolio.totalCost?.toLocaleString()}</span>
              </div>
            </div>
            <div className={`stat-card ${portfolio.totalGainLoss >= 0 ? 'gradient-green' : 'gradient-red'}`}>
              <div className="stat-icon">{portfolio.totalGainLoss >= 0 ? '📈' : '📉'}</div>
              <div className="stat-content">
                <span className="stat-label">Total Gain/Loss</span>
                <span className="stat-value">{portfolio.totalGainLoss >= 0 ? '+' : ''}${portfolio.totalGainLoss?.toLocaleString()} ({portfolio.totalGainLossPercent}%)</span>
              </div>
            </div>
          </div>

          <div className="charts-grid">
            <div className="chart-card">
              <h3>Portfolio Allocation</h3>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" outerRadius={100} dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                    {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#1a202c', border: '1px solid #4a5568', borderRadius: '8px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="chart-card">
              <h3>Gain/Loss by Stock</h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" />
                  <XAxis dataKey="name" stroke="#a0aec0" />
                  <YAxis stroke="#a0aec0" />
                  <Tooltip contentStyle={{ background: '#1a202c', border: '1px solid #4a5568', borderRadius: '8px' }} />
                  <Bar dataKey="gainLoss" fill="#6c5ce7" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            <div className="chart-card full-width">
              <h3>Price History (Last Week)</h3>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={mockPriceHistory}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" />
                  <XAxis dataKey="date" stroke="#a0aec0" />
                  <YAxis stroke="#a0aec0" />
                  <Tooltip contentStyle={{ background: '#1a202c', border: '1px solid #4a5568', borderRadius: '8px' }} />
                  <Legend />
                  <Line type="monotone" dataKey="AAPL" stroke="#6c5ce7" strokeWidth={2} />
                  <Line type="monotone" dataKey="GOOGL" stroke="#00cec9" strokeWidth={2} />
                  <Line type="monotone" dataKey="MSFT" stroke="#fd79a8" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="positions-table">
            <h3>Positions</h3>
            <table>
              <thead>
                <tr>
                  <th>Symbol</th><th>Company</th><th>Qty</th><th>Avg Cost</th><th>Current</th><th>Value</th><th>Gain/Loss</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.positions?.map((p) => (
                  <tr key={p.id}>
                    <td className="symbol">{p.symbol}</td>
                    <td>{p.companyName}</td>
                    <td>{p.quantity}</td>
                    <td>${p.averageCost}</td>
                    <td>${p.currentPrice}</td>
                    <td>${p.totalValue}</td>
                    <td className={parseFloat(p.gainLoss) >= 0 ? 'positive' : 'negative'}>
                      {parseFloat(p.gainLoss) >= 0 ? '+' : ''}${p.gainLoss} ({p.gainLossPercent}%)
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
