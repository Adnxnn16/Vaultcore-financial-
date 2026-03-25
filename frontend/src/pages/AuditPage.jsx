import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAuditLogs } from '../features/audit/auditSlice';
import DOMPurify from 'dompurify';

export default function AuditPage() {
  const dispatch = useDispatch();
  const { logs, totalElements, totalPages, currentPage, loading, error } = useSelector((s) => s.audit);
  const [searchMethod, setSearchMethod] = useState('');

  const load = (page = 0) => {
    dispatch(fetchAuditLogs({ page, size: 20, method: searchMethod || null }));
  };

  useEffect(() => {
    load(0);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    load(0);
  };

  return (
    <div className="audit-page">
      <header className="page-header" style={{ marginBottom: '40px' }}>
        <span className="header-badge">SECURITY &amp; COMPLIANCE</span>
        <h1>Immutable Audit Logs</h1>
        <p className="page-subtitle">Cryptographically verified record of every node operation and ledger mutation.</p>
      </header>

      <div className="audit-controls" style={{ marginBottom: '32px' }}>
        <form onSubmit={handleSearch} style={{
          display: 'flex', gap: '16px', background: 'var(--bg-surface-container)',
          padding: '12px 24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-ghost)'
        }}>
          <span className="material-symbols-outlined" style={{ color: 'var(--text-muted)', alignSelf: 'center' }}>search</span>
          <input
            type="text"
            placeholder="FILTER BY METHOD NAME..."
            value={searchMethod}
            onChange={(e) => setSearchMethod(e.target.value)}
            style={{
              flex: 1, background: 'transparent', border: 'none', color: 'var(--text-primary)',
              fontSize: '13px', fontWeight: 600, outline: 'none', letterSpacing: '0.5px'
            }}
          />
          <button
            type="submit"
            className="btn-small"
            style={{ background: 'var(--bg-surface-highest)', color: 'var(--text-primary)', border: 'none' }}
          >
            SEARCH
          </button>
          <button
            type="button"
            className="btn-small"
            onClick={() => { setSearchMethod(''); dispatch(fetchAuditLogs({ page: 0 })); }}
            style={{ background: 'transparent', border: '1px solid var(--border-ghost)', color: 'var(--text-muted)' }}
          >
            CLEAR
          </button>
        </form>
      </div>

      {loading && (
        <div className="loading-spinner" style={{ marginBottom: '24px' }}>Loading audit logs...</div>
      )}

      {error && (
        <div className="error-message" style={{ marginBottom: '24px' }}>
          <span className="material-symbols-outlined" style={{ marginRight: '8px', fontSize: '18px' }}>error</span>
          {error}
        </div>
      )}

      <div className="audit-table-container" style={{
        background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-xl)', padding: '32px',
        border: '1px solid var(--border-ghost)', overflow: 'hidden'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h3 style={{ margin: 0 }}>Activity Records</h3>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 700 }}>
            {totalElements.toLocaleString()} ENTRIES
          </span>
        </div>
        <table className="premium-table">
          <thead>
            <tr>
              <th>TIMESTAMP (UTC)</th>
              <th>OPERATION</th>
              <th>METHOD</th>
              <th>NODE IDENTIFIER</th>
              <th>STATUS</th>
            </tr>
          </thead>
          <tbody>
            {logs.length === 0 && !loading ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  No audit logs found.
                </td>
              </tr>
            ) : logs.map((log) => (
              <tr key={log.id}>
                <td style={{ fontFamily: 'monospace', color: 'var(--text-muted)', fontSize: '12px' }}>
                  {new Date(log.createdAt).toLocaleString('en-US', { hour12: false })}
                </td>
                <td style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{DOMPurify.sanitize(log.action || '')}</td>
                <td style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                  {DOMPurify.sanitize(log.methodName || '')}
                </td>
                <td style={{ fontFamily: 'monospace', color: 'var(--accent-primary)', fontSize: '11px' }}>
                  {log.userId ? DOMPurify.sanitize(String(log.userId).substring(0, 16) + '...') : '—'}
                </td>
                <td>
                  <span style={{
                    display: 'inline-flex', alignItems: 'center', gap: '6px',
                    padding: '4px 12px', borderRadius: '20px', fontSize: '10px', fontWeight: 900,
                    background: log.status === 'SUCCESS' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                    color: log.status === 'SUCCESS' ? 'var(--accent-secondary)' : 'var(--accent-error)'
                  }}>
                    <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'currentColor' }}></span>
                    {DOMPurify.sanitize(log.status || '')}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="pagination" style={{ display: 'flex', justifyContent: 'center', gap: '16px', marginTop: '40px', alignItems: 'center' }}>
          <button
            className="btn-small"
            disabled={currentPage === 0}
            onClick={() => load(currentPage - 1)}
            style={{ background: 'transparent', border: '1px solid var(--border-ghost)', color: currentPage === 0 ? 'var(--text-muted)' : 'var(--text-primary)', opacity: currentPage === 0 ? 0.3 : 1 }}
          >
            PREVIOUS
          </button>
          <span style={{ fontSize: '12px', fontWeight: 800, color: 'var(--text-muted)', letterSpacing: '1px' }}>
            PAGE {currentPage + 1} / {totalPages}
          </span>
          <button
            className="btn-small"
            disabled={currentPage >= totalPages - 1}
            onClick={() => load(currentPage + 1)}
            style={{ background: 'transparent', border: '1px solid var(--border-ghost)', color: currentPage >= totalPages - 1 ? 'var(--text-muted)' : 'var(--text-primary)', opacity: currentPage >= totalPages - 1 ? 0.3 : 1 }}
          >
            NEXT
          </button>
        </div>
      )}
    </div>
  );
}
