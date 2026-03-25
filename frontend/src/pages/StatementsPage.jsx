import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { downloadStatement } from '../features/statements/statementSlice';
import { fetchAccounts } from '../features/accounts/accountSlice';

export default function StatementsPage() {
  const dispatch = useDispatch();
  const { user } = useSelector((s) => s.auth);
  const { accounts } = useSelector((s) => s.accounts);
  const { downloading, downloadError, lastDownloaded } = useSelector((s) => s.statements);

  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [downloadSuccess, setDownloadSuccess] = useState(false);

  useEffect(() => {
    if (user?.id) dispatch(fetchAccounts(user.id));
  }, [dispatch, user]);

  // Pre-select first account
  useEffect(() => {
    if (accounts.length > 0 && !selectedAccountId) {
      setSelectedAccountId(accounts[0].id);
    }
  }, [accounts]);

  // Show success flash
  useEffect(() => {
    if (lastDownloaded) {
      setDownloadSuccess(true);
      const timer = setTimeout(() => setDownloadSuccess(false), 3000);
      return () => clearTimeout(timer);
    }
  }, [lastDownloaded]);

  const handleDownload = () => {
    if (!selectedAccountId) return;
    dispatch(downloadStatement({
      accountId: selectedAccountId,
      startDate: startDate || undefined,
      endDate: endDate || undefined,
    }));
  };

  // Derive default date range (last 3 months)
  const today = new Date().toISOString().split('T')[0];
  const threeMonthsAgo = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  return (
    <div className="statements-page">
      <header className="page-header" style={{ marginBottom: '40px' }}>
        <span className="header-badge">REPORTS &amp; LEDGERS</span>
        <h1>Account Statements</h1>
        <p className="page-subtitle">Access your institutional financial history and authenticated reports.</p>
      </header>

      <div className="statement-form-card" style={{
        background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-xl)', padding: '48px',
        border: '1px solid var(--border-ghost)', maxWidth: '600px', marginBottom: '64px'
      }}>
        <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '32px' }}>Generate PDF Statement</h3>

        <div className="form-group" style={{ marginBottom: '24px' }}>
          <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Account</label>
          <select
            value={selectedAccountId}
            onChange={(e) => setSelectedAccountId(e.target.value)}
            style={{
              width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
              borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '15px'
            }}
          >
            <option value="">Select Account</option>
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.accountNumber} — {a.accountType} (${parseFloat(a.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group" style={{ marginBottom: '32px' }}>
          <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Statement Period</label>
          <div style={{ display: 'flex', gap: '16px' }}>
            <div style={{ flex: 1 }}>
              <label style={{ fontSize: '10px', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>FROM</label>
              <input
                type="date"
                value={startDate}
                max={today}
                onChange={(e) => setStartDate(e.target.value)}
                style={{
                  width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
                  borderRadius: 'var(--radius-md)', color: 'var(--text-primary)'
                }}
              />
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ fontSize: '10px', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>TO</label>
              <input
                type="date"
                value={endDate}
                max={today}
                onChange={(e) => setEndDate(e.target.value)}
                style={{
                  width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
                  borderRadius: 'var(--radius-md)', color: 'var(--text-primary)'
                }}
              />
            </div>
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Leave blank to include all transactions.
          </p>
        </div>

        {downloadError && (
          <div className="error-message" style={{ marginBottom: '16px', padding: '12px 16px' }}>
            {downloadError}
          </div>
        )}

        {downloadSuccess && (
          <div style={{ marginBottom: '16px', padding: '12px 16px', background: 'rgba(16,185,129,0.08)', borderRadius: '8px', border: '1px solid rgba(16,185,129,0.2)', color: 'var(--accent-secondary)', fontWeight: 700, fontSize: '13px' }}>
            ✓ Statement downloaded successfully.
          </div>
        )}

        <button
          className="btn-primary"
          style={{ width: '100%', padding: '16px' }}
          onClick={handleDownload}
          disabled={downloading || !selectedAccountId}
        >
          {downloading ? (
            <span>GENERATING PDF...</span>
          ) : (
            <>
              <span className="material-symbols-outlined" style={{ marginRight: '8px', fontSize: '18px', verticalAlign: 'middle' }}>download</span>
              GENERATE &amp; DOWNLOAD PDF
            </>
          )}
        </button>

        <p className="statement-note" style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '24px', textAlign: 'center' }}>
          All generated documents are digitally signed and cryptographically verified.
        </p>
      </div>

      {/* ── Account Summaries ── */}
      {accounts.length > 0 && (
        <>
          <h3 style={{ fontSize: '20px', fontWeight: 900, marginBottom: '24px' }}>Account Summary</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px,1fr))', gap: '20px', maxWidth: '900px' }}>
            {accounts.map((acc) => (
              <div key={acc.id} style={{
                background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-lg)', padding: '28px',
                border: '1px solid var(--border-ghost)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
                  <div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '4px', textTransform: 'uppercase' }}>{acc.accountType}</div>
                    <div style={{ fontFamily: 'monospace', fontWeight: 800, color: 'var(--accent-primary)', fontSize: '14px' }}>{acc.accountNumber}</div>
                  </div>
                  <span className="material-symbols-outlined" style={{ color: 'var(--accent-primary)', fontSize: '28px' }}>account_balance</span>
                </div>
                <div style={{ fontSize: '28px', fontWeight: 900 }}>
                  ${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>{acc.currency}</div>
                <button
                  className="btn-small"
                  style={{ marginTop: '20px', background: 'var(--bg-surface-highest)', border: 'none', color: 'var(--text-primary)' }}
                  onClick={() => {
                    setSelectedAccountId(acc.id);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                >
                  GENERATE STATEMENT
                </button>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
