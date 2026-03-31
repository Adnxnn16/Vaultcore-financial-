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
      <div className="page-header">
        <h1>My Accounts</h1>
        <p className="page-subtitle">Manage your bank accounts</p>
      </div>
      <div className="accounts-grid">
        {loading ? (
          <div className="loading-spinner">Loading...</div>
        ) : accounts.map((acc) => (
          <div key={acc.id} className="account-card">
            <div className="account-card-header">
              <span className="account-type-badge large">{acc.accountType}</span>
              <span className="account-status">{acc.active ? '● Active' : '○ Inactive'}</span>
            </div>
            <div className="account-card-body">
              <div className="account-number-display">{acc.accountNumber}</div>
              <div className="account-balance-display">
                <span className="balance-label">Available Balance</span>
                <span className="balance-amount">${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              </div>
            </div>
            <div className="account-card-footer">
              <span className="account-currency">{acc.currency}</span>
              <span className="account-created">Since {new Date(acc.createdAt).toLocaleDateString()}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
