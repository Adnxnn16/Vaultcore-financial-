import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function AuditPage() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');

  const fetchLogs = async (p = 0) => {
    setLoading(true);
    try {
      const url = search
        ? `/admin/audit/search?action=${search}&page=${p}&size=20`
        : `/admin/audit?page=${p}&size=20`;
      const response = await api.get(url);
      setLogs(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      console.error('Failed to fetch audit logs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchLogs(); }, []);

  return (
    <div className="audit-page">
      <div className="page-header">
        <h1>Audit Logs</h1>
        <p className="page-subtitle">System activity monitoring</p>
      </div>

      <div className="audit-controls">
        <div className="search-bar">
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search by action..." />
          <button className="btn-primary" onClick={() => fetchLogs(0)}>Search</button>
        </div>
      </div>

      <div className="audit-table-container">
        {loading ? <div className="loading-spinner">Loading...</div> : (
          <table className="audit-table">
            <thead>
              <tr>
                <th>Timestamp</th><th>Action</th><th>Method</th><th>Status</th><th>User ID</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{new Date(log.createdAt).toLocaleString()}</td>
                  <td><span className={`action-badge ${log.action?.toLowerCase()}`}>{log.action}</span></td>
                  <td className="method-cell">{log.methodName}</td>
                  <td><span className={`status-badge ${log.status?.toLowerCase()}`}>{log.status}</span></td>
                  <td className="uuid-cell">{log.userId?.substring(0, 8)}...</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {totalPages > 1 && (
          <div className="pagination">
            <button disabled={page === 0} onClick={() => fetchLogs(page - 1)}>← Prev</button>
            <span>Page {page + 1} of {totalPages}</span>
            <button disabled={page >= totalPages - 1} onClick={() => fetchLogs(page + 1)}>Next →</button>
          </div>
        )}
      </div>
    </div>
  );
}
