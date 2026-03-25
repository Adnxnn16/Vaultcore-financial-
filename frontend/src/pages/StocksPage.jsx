import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchPortfolio } from '../features/stocks/stockSlice';
import TradeModal from '../features/stocks/TradeModal';
import api from '../api/axios';
import {
  PieChart, Pie, Cell, BarChart, Bar, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';

const COLORS = ['#0066FF', '#10B981', '#FFB59D', '#FFB4AB', '#31353C', '#10141A'];
const TOP_SYMBOLS = ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'AMZN'];

export default function StocksPage() {
  const dispatch = useDispatch();
  const { user } = useSelector((s) => s.auth);
  const { portfolio, loading, tradeResult } = useSelector((s) => s.stocks);

  const [isTradeModalOpen, setIsTradeModalOpen] = useState(false);
  const [tradeContext, setTradeContext] = useState({ symbol: '', type: 'BUY' });

  // Live quote state (keyed by symbol)
  const [liveQuotes, setLiveQuotes] = useState({});
  const [quotesLoading, setQuotesLoading] = useState(false);
  const [topMovers, setTopMovers] = useState([]);
  const [moversLoading, setMoversLoading] = useState(false);

  useEffect(() => {
    if (user?.id) dispatch(fetchPortfolio(user.id));
  }, [dispatch, user, tradeResult]);

  // Fetch Top Movers
  useEffect(() => {
    let cancelled = false;
    const fetchMovers = async () => {
      setMoversLoading(true);
      try {
        const results = await Promise.allSettled(
          TOP_SYMBOLS.map(sym => api.get(`/stocks/quote/${sym}`))
        );
        if (!cancelled) {
          const movers = results.map((r, i) => {
            if (r.status === 'fulfilled') {
              return {
                symbol: TOP_SYMBOLS[i],
                price: parseFloat(r.value.data.currentPrice || 0),
                change: parseFloat(r.value.data.changePercent || 0)
              };
            }
            return { symbol: TOP_SYMBOLS[i], price: null, change: null };
          });
          setTopMovers(movers);
        }
      } finally {
        if (!cancelled) setMoversLoading(false);
      }
    };
    fetchMovers();
    const intervalId = setInterval(fetchMovers, 60000); // refresh movers every 1 min
    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, []);

  // Fetch live prices for all positions automatically and refresh every 5s.
  useEffect(() => {
    if (!portfolio?.positions?.length) return;
    let cancelled = false;

    const refreshQuotes = async () => {
      setQuotesLoading(true);
      const symbols = portfolio.positions.map((p) => p.symbol);
      const results = await Promise.allSettled(
        symbols.map(async (sym) => {
          const { data } = await api.get(`/stocks/quote/${sym}`);
          return { symbol: sym, quote: data };
        })
      );

      if (cancelled) return;

      const map = {};
      results.forEach((r) => {
        if (r.status === 'fulfilled') {
          map[r.value.symbol] = r.value.quote;
        }
      });
      setLiveQuotes(map);
      setQuotesLoading(false);
    };

    refreshQuotes();
    const intervalId = setInterval(refreshQuotes, 10000); // 10s to avoid rate limits
    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, [portfolio]);

  const openTradeModal = (symbol = '', type = 'BUY') => {
    setTradeContext({ symbol, type });
    setIsTradeModalOpen(true);
  };

  const pieData = portfolio?.positions?.map((p) => ({
    name: p.symbol,
    value: parseFloat(p.marketValue || 0),
  })) || [];

  const barData = portfolio?.positions?.map((p) => ({
    name: p.symbol,
    gainLoss: parseFloat(p.unrealizedGain || 0),
  })) || [];

  // Build price chart from live quotes
  const priceChartData = Object.entries(liveQuotes).slice(0, 5).map(([sym, q]) => ({
    symbol: sym,
    price: parseFloat(q?.currentPrice || 0),
    change: parseFloat(q?.changePercent || 0),
  }));

  return (
    <div className="stocks-page">
      <header className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' }}>
        <div>
          <span className="header-badge">EQUITY MARKETS</span>
          <h1>Custody Portfolio</h1>
          <p className="page-subtitle">
            Real-time market valuation via institutional-grade data feeds.
            <span style={{ marginLeft: 8, color: quotesLoading ? 'var(--accent-tertiary)' : 'var(--accent-secondary)', fontWeight: 800 }}>
              {quotesLoading ? ' ● SYNCHRONIZING...' : ' ● LIVE'}
            </span>
          </p>
        </div>
        <button className="btn-primary" onClick={() => openTradeModal('', 'BUY')} style={{ padding: '16px 32px' }}>
          <span className="material-symbols-outlined" style={{ marginRight: '8px', fontSize: '18px' }}>add_chart</span>
          EXECUTE ORDER
        </button>
      </header>

      <TradeModal
        isOpen={isTradeModalOpen}
        onClose={() => setIsTradeModalOpen(false)}
        initialSymbol={tradeContext.symbol}
        initialType={tradeContext.type}
      />

      {/* ── Top Market Movers (Always Visible) ── */}
      <section className="top-movers-section" style={{ marginBottom: '48px' }}>
        <h3 style={{ fontSize: '18px', fontWeight: 900, marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="material-symbols-outlined" style={{ color: 'var(--accent-primary)' }}>trending_up</span>
          Top Market Movers
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px' }}>
          {moversLoading && topMovers.length === 0 ? (
            <div style={{ padding: '24px', color: 'var(--text-muted)' }}>Loading market data...</div>
          ) : (
            topMovers.map((m) => {
              const isUp = m.change >= 0;
              return (
                <div 
                  key={m.symbol} 
                  className="mover-card"
                  onClick={() => openTradeModal(m.symbol, 'BUY')}
                  style={{ 
                    background: 'var(--bg-surface-container)', 
                    border: '1px solid var(--border-ghost)', 
                    borderRadius: 'var(--radius-lg)', 
                    padding: '24px',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseEnter={(e) => { e.currentTarget.style.borderColor = 'var(--accent-primary)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
                  onMouseLeave={(e) => { e.currentTarget.style.borderColor = 'var(--border-ghost)'; e.currentTarget.style.transform = 'none'; }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                    <span style={{ fontWeight: 900, fontSize: '18px', color: 'var(--text-primary)' }}>{m.symbol}</span>
                    <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--text-muted)' }}>shopping_cart</span>
                  </div>
                  {m.price ? (
                    <>
                      <div style={{ fontSize: '24px', fontWeight: 800, marginBottom: '8px' }}>${m.price.toFixed(2)}</div>
                      <div style={{ fontSize: '13px', fontWeight: 700, color: isUp ? 'var(--accent-secondary)' : 'var(--accent-error)' }}>
                        {isUp ? '↑' : '↓'} {Math.abs(m.change).toFixed(2)}%
                      </div>
                    </>
                  ) : (
                    <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Data unavailable</div>
                  )}
                </div>
              );
            })
          )}
        </div>
      </section>

      {loading ? (
        <div className="loading-spinner">Initializing ledger...</div>
      ) : portfolio && (
        <>
          <section className="stats-grid" style={{ marginBottom: '48px' }}>
            <div className="stat-card">
              <div className="stat-label">Total Asset Value</div>
              <div className="stat-value">${parseFloat(portfolio.totalMarketValue || 0).toLocaleString()}</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">Positions</div>
              <div className="stat-value">{portfolio.positions?.length || 0}</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">Data Feed</div>
              <div className="stat-value">{quotesLoading ? 'Syncing' : 'Live'}</div>
              <div className="stat-trend">Refresh every 10s</div>
            </div>
          </section>

          {portfolio.positions?.length > 0 ? (
            <>
              <div className="charts-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px', marginBottom: '48px' }}>
                <div className="chart-card" style={{ background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-lg)', padding: '32px', border: '1px solid var(--border-ghost)' }}>
                  <h3>Allocation by Symbol</h3>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                        {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip contentStyle={{ background: 'var(--bg-surface-highest)', border: 'none', borderRadius: 'var(--radius-md)', color: 'var(--text-primary)' }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="chart-card" style={{ background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-lg)', padding: '32px', border: '1px solid var(--border-ghost)' }}>
                  <h3>Performance Variance</h3>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={barData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                      <XAxis dataKey="name" stroke="var(--text-muted)" axisLine={false} tickLine={false} />
                      <YAxis stroke="var(--text-muted)" axisLine={false} tickLine={false} />
                      <Tooltip contentStyle={{ background: 'var(--bg-surface-highest)', border: 'none', borderRadius: 'var(--radius-md)', color: 'var(--text-primary)' }} />
                      <Bar dataKey="gainLoss" fill="var(--accent-primary)" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="positions-table" style={{ background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-lg)', padding: '32px', border: '1px solid var(--border-ghost)', overflow: 'hidden' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                  <h3>Active Holdings</h3>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 700 }}>
                    FEED SOURCE: ALPHA VANTAGE / L3 LAYER
                  </span>
                </div>
                <table className="premium-table">
                  <thead>
                    <tr>
                      <th>Symbol</th>
                      <th>Quantity</th>
                      <th>Avg Cost</th>
                      <th>Market Price</th>
                      <th>Change</th>
                      <th>Position Value</th>
                      <th>Gain / Loss</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {portfolio.positions.map((p) => {
                      const q = liveQuotes[p.symbol];
                      const livePrice = q ? parseFloat(q.currentPrice) : null;
                      const changePct = q ? parseFloat(q.changePercent) : null;
                      const isUp = changePct !== null && changePct >= 0;

                      return (
                        <tr key={p.id}>
                          <td style={{ fontWeight: 900, color: 'var(--accent-primary)', fontSize: '16px' }}>{p.symbol}</td>
                          <td style={{ fontWeight: 600 }}>{p.quantity}</td>
                          <td style={{ color: 'var(--text-muted)' }}>${parseFloat(p.averageCost).toFixed(2)}</td>
                          <td style={{ fontWeight: 800 }}>
                            {livePrice
                              ? <span style={{ color: isUp ? 'var(--accent-secondary)' : 'var(--accent-error)' }}>${livePrice.toFixed(2)}</span>
                              : <span style={{ color: 'var(--text-muted)' }}>—</span>}
                          </td>
                          <td>
                            {changePct !== null
                              ? <span style={{ color: isUp ? 'var(--accent-secondary)' : 'var(--accent-error)', fontWeight: 700 }}>
                                  {isUp ? '↑' : '↓'} {Math.abs(changePct).toFixed(2)}%
                                </span>
                              : '—'}
                          </td>
                          <td style={{ fontWeight: 700 }}>${parseFloat(p.marketValue || 0).toLocaleString()}</td>
                          <td style={{ color: parseFloat(p.unrealizedGain || 0) >= 0 ? 'var(--accent-secondary)' : 'var(--accent-error)', fontWeight: 700 }}>
                            {parseFloat(p.unrealizedGain || 0) >= 0 ? '+' : ''}${parseFloat(p.unrealizedGain || 0).toFixed(2)}
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: '8px' }}>
                              <button className="btn-small" style={{ background: 'var(--bg-surface-highest)', color: 'var(--text-primary)', border: 'none' }} onClick={() => openTradeModal(p.symbol, 'BUY')}>BUY</button>
                              <button className="btn-small" style={{ background: 'var(--bg-surface-lowest)', color: 'var(--text-muted)', border: '1px solid var(--border-ghost)' }} onClick={() => openTradeModal(p.symbol, 'SELL')}>SELL</button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '64px', background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-ghost)' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '48px', color: 'var(--text-muted)', marginBottom: '16px' }}>account_balance_wallet</span>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '8px' }}>No Active Positions</h3>
              <p style={{ color: 'var(--text-muted)', marginBottom: '24px' }}>Your institutional custody portfolio is currently empty. Execute orders from the market movers above or search a symbol.</p>
              <button className="btn-primary" onClick={() => openTradeModal('', 'BUY')} style={{ padding: '16px 32px' }}>
                START TRADING
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
