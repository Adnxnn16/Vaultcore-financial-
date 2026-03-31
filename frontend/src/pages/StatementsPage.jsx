import { useState } from 'react';
import api from '../api/axios';

export default function StatementsPage() {
  const [month, setMonth] = useState('');
  const [accountId, setAccountId] = useState('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleDownload = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/statements/${month}?accountId=${accountId}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statement-${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Failed to download statement. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="statements-page">
      <div className="page-header">
        <h1>Statements</h1>
        <p className="page-subtitle">Download your account statements</p>
      </div>

      <div className="statement-form-card">
        <div className="form-group">
          <label>Select Month</label>
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
        </div>
        <div className="form-group">
          <label>Account ID</label>
          <input value={accountId} onChange={(e) => setAccountId(e.target.value)} placeholder="Enter account UUID" />
        </div>
        {error && <div className="error-message">{error}</div>}
        <button className="btn-primary" onClick={handleDownload} disabled={!month || loading}>
          {loading ? 'Generating...' : '📄 Download Statement'}
        </button>
        <p className="statement-note">Statements are encrypted with AES-128 for security.</p>
      </div>
    </div>
  );
}
