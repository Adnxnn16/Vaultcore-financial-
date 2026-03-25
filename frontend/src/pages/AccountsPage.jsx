import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAccounts } from '../features/accounts/accountSlice';

export default function AccountsPage() {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { accounts, loading } = useSelector((state) => state.accounts);

  useEffect(() => {
    if (user?.id) dispatch(fetchAccounts(user.id));
  }, [dispatch, user]);

  return (
    <div className="accounts-page">
      <header className="page-header">
        <span className="header-badge">ACCOUNT PORTFOLIO</span>
        <h1>My Accounts</h1>
        <p className="page-subtitle">Manage your institution-linked banking nodes.</p>
      </header>

      <div className="accounts-grid">
        {loading ? (
          <div className="loading-spinner">Encrypting session...</div>
        ) : accounts.map((acc) => (
          <div key={acc.id} className="account-card">
            <div className="account-card-header">
              <span className={`account-type-badge ${acc.accountType.toLowerCase()}`}>
                {acc.accountType}
              </span>
              <span className={`account-status ${acc.active ? 'active' : ''}`}>
                <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>
                  {acc.active ? 'check_circle' : 'error'}
                </span>
                {acc.active ? 'VERIFIED' : 'PENDING'}
              </span>
            </div>
            
            <div className="account-card-body">
              <div className="balance-display">
                <span className="balance-label">Available Balance</span>
                <span className="balance-amount">
                  ${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </div>
              
              <div className="account-number-display" style={{ marginTop: '24px' }}>
                <span style={{ color: 'var(--text-muted)' }}>•••• •••• •••• </span>
                {acc.accountNumber.slice(-4)}
              </div>
            </div>

            <div className="account-card-footer" style={{ borderTop: '1px solid var(--border-ghost)', paddingTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--accent-primary)' }}>public</span>
                <span style={{ fontSize: '12px', fontWeight: 600 }}>{acc.currency} / Global</span>
              </div>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>MEMBER SINCE {new Date(acc.createdAt).getFullYear()}</span>
            </div>
          </div>
        ))}
      </div>
    </div>

  );
}
