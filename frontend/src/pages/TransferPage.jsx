import { useState, useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { executeTransfer, resetTransfer, verifyTransferMfa, clearMfaVerified } from '../features/transfer/transferSlice';
import { fetchAccounts } from '../features/accounts/accountSlice';
import { fetchPortfolio } from '../features/stocks/stockSlice';

export default function TransferPage() {
  const dispatch = useDispatch();
  const { user } = useSelector((s) => s.auth);
  const { accounts = [] } = useSelector((s) => s.accounts);
  const {
    result, loading, error,
    mfaRequired, mfaVerifying, mfaMessage, mfaVerified,
  } = useSelector((s) => s.transfers);
  const { portfolio } = useSelector((s) => s.stocks);

  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    sourceAccountNumber: '',
    destinationAccountNumber: '',
    amount: '',
    description: '',
  });
  const [otp, setOtp] = useState('');

  // Capture the pending transfer data so we can retry after MFA
  const [pendingTransfer, setPendingTransfer] = useState(null);

  // Load accounts on mount
  useEffect(() => {
    if (user?.id) {
      dispatch(fetchAccounts(user.id));
      dispatch(fetchPortfolio(user.id));
    }
  }, [dispatch, user]);

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const nextStep = () => setStep((s) => Math.min(s + 1, 3));
  const prevStep = () => setStep((s) => Math.max(s - 1, 1));

  const buildTransferPayload = useCallback(() => ({
    sourceAccountNumber: form.sourceAccountNumber,
    destinationAccountNumber: form.destinationAccountNumber,
    amount: parseFloat(form.amount),
    description: form.description,
    currency: 'USD',
  }), [form]);

  const handleSubmit = () => {
    const payload = buildTransferPayload();
    setPendingTransfer(payload);
    dispatch(clearMfaVerified());
    dispatch(executeTransfer(payload));
  };

  // When MFA is verified, auto-retry the transfer
  useEffect(() => {
    if (mfaVerified && pendingTransfer) {
      dispatch(executeTransfer(pendingTransfer));
    }
  }, [mfaVerified, pendingTransfer, dispatch]);

  const handleMfaVerify = () => {
    if (!otp || !user?.id) return;
    dispatch(verifyTransferMfa({ userId: user.id, otp }));
  };

  const handleReset = () => {
    setForm({ sourceAccountNumber: '', destinationAccountNumber: '', amount: '', description: '' });
    setOtp('');
    setPendingTransfer(null);
    setStep(1);
    dispatch(resetTransfer());
  };

  const selectedAccount = accounts.find((a) => a.accountNumber === form.sourceAccountNumber);
  const numericAmount = parseFloat(form.amount || '0');
  const insufficientFunds = selectedAccount && numericAmount > parseFloat(selectedAccount.balance || 0);
  const isCompleted = result?.status === 'COMPLETED';

  return (
    <div className="transfer-page">
      <header className="page-header" style={{ marginBottom: '40px' }}>
        <span className="header-badge">CAPITAL MOVEMENT</span>
        <h1>Transfer &amp; Remittance</h1>
        <p className="page-subtitle">Secure, multi-layered fund allocation across your institutional node.</p>
      </header>

      <div className="transfer-wizard" style={{ maxWidth: '800px' }}>

        {/* ── Progress Steps ── */}
        <div className="wizard-steps" style={{ display: 'flex', gap: '48px', marginBottom: '48px', borderBottom: '1px solid var(--border-ghost)', paddingBottom: '24px' }}>
          {['Source & Destination', 'Allocation Details', 'Authorization'].map((label, i) => (
            <div key={i} className={`wizard-step ${step >= i + 1 ? 'active' : ''}`} style={{
              display: 'flex', alignItems: 'center', gap: '12px',
              color: step >= i + 1 ? 'var(--text-primary)' : 'var(--text-muted)',
              fontWeight: step >= i + 1 ? 800 : 500,
              opacity: step >= i + 1 ? 1 : 0.4,
              fontSize: '14px',
              textTransform: 'uppercase',
              letterSpacing: '1px'
            }}>
              <span style={{
                width: '32px', height: '32px', borderRadius: '50%',
                background: step >= i + 1 ? 'var(--accent-primary)' : 'var(--bg-surface-low)',
                color: step >= i + 1 ? 'white' : 'var(--text-muted)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '12px', fontWeight: 900,
                border: step >= i + 1 ? 'none' : '1px solid var(--border-ghost)',
              }}>{i + 1}</span>
              {label}
            </div>
          ))}
        </div>

        <div className="wizard-body" style={{ background: 'var(--bg-surface-container)', borderRadius: 'var(--radius-xl)', padding: '48px', border: '1px solid var(--border-ghost)' }}>

          {/* ────── STEP 1: Accounts ────── */}
          {step === 1 && (
            <div className="wizard-panel">
              <h3 style={{ fontSize: '24px', marginBottom: '32px', fontWeight: 900 }}>Select Nodes</h3>

              <div className="form-group" style={{ marginBottom: '32px' }}>
                <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Originating Account</label>
                <select
                  name="sourceAccountNumber"
                  value={form.sourceAccountNumber}
                  onChange={handleChange}
                  style={{
                    width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
                    borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '15px', fontWeight: 600
                  }}
                >
                  <option value="">Select Origin Node</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.accountNumber}>
                      {a.accountNumber} · {a.accountType} (Available: ${parseFloat(a.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: '40px' }}>
                <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Recipient Identifier</label>
                <input
                  name="destinationAccountNumber"
                  value={form.destinationAccountNumber}
                  onChange={handleChange}
                  placeholder="Enter account number (e.g. VC-100002)"
                  style={{
                    width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
                    borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '15px', fontWeight: 600
                  }}
                />
                {form.sourceAccountNumber && form.destinationAccountNumber && form.sourceAccountNumber === form.destinationAccountNumber && (
                  <p style={{ color: 'var(--accent-error)', fontSize: '12px', marginTop: '8px', fontWeight: 700 }}>
                    ⚠ Source and destination accounts must be different.
                  </p>
                )}
              </div>

              <div className="wizard-actions" style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <button
                  className="btn-primary"
                  onClick={nextStep}
                  disabled={!form.sourceAccountNumber || !form.destinationAccountNumber || form.sourceAccountNumber === form.destinationAccountNumber}
                  style={{ padding: '16px 40px' }}
                >
                  NEXT STEP
                </button>
              </div>
            </div>
          )}

          {/* ────── STEP 2: Amount ────── */}
          {step === 2 && (
            <div className="wizard-panel">
              <h3 style={{ fontSize: '24px', marginBottom: '32px', fontWeight: 900 }}>Allocation Details</h3>

              {selectedAccount && (
                <div style={{ background: 'var(--bg-surface-lowest)', borderRadius: 'var(--radius-md)', padding: '16px 20px', border: '1px solid var(--border-ghost)', marginBottom: '24px' }}>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '4px' }}>AVAILABLE BALANCE</div>
                  <div style={{ fontSize: '28px', fontWeight: 900, color: 'var(--accent-primary)' }}>
                    ${parseFloat(selectedAccount.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>{selectedAccount.accountNumber} · {selectedAccount.accountType}</div>
                </div>
              )}

              <div className="form-group" style={{ marginBottom: '32px' }}>
                <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Transfer Amount (USD)</label>
                <div style={{ position: 'relative' }}>
                  <span style={{ position: 'absolute', left: '20px', top: '50%', transform: 'translateY(-50%)', fontWeight: 900, fontSize: '24px', color: 'var(--text-muted)' }}>$</span>
                  <input
                    name="amount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={form.amount}
                    onChange={handleChange}
                    placeholder="0.00"
                    style={{
                      width: '100%', padding: '24px 24px 24px 48px', background: 'var(--bg-surface-lowest)',
                      border: `1px solid ${insufficientFunds ? 'var(--accent-error)' : 'var(--border-ghost)'}`,
                      borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '32px', fontWeight: 900,
                      letterSpacing: '-1px'
                    }}
                  />
                </div>
                {insufficientFunds && (
                  <p style={{ color: 'var(--accent-error)', fontSize: '12px', marginTop: '8px', fontWeight: 700 }}>
                    ⚠ Insufficient funds. Available: ${parseFloat(selectedAccount.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </p>
                )}
                {numericAmount > 1000 && (
                  <p style={{ color: 'var(--accent-tertiary)', fontSize: '12px', marginTop: '8px', fontWeight: 700 }}>
                    ⚡ Transfers over $1,000 require 2FA authorization.
                  </p>
                )}
              </div>

              <div className="form-group" style={{ marginBottom: '40px' }}>
                <label style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Ledger Memo (Optional)</label>
                <input
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  placeholder="e.g. Institutional Settlement #4402"
                  style={{
                    width: '100%', padding: '16px', background: 'var(--bg-surface-lowest)', border: '1px solid var(--border-ghost)',
                    borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '15px'
                  }}
                />
              </div>

              <div className="wizard-actions" style={{ display: 'flex', justifyContent: 'space-between' }}>
                <button className="btn-small" style={{ background: 'transparent', color: 'var(--text-muted)', border: '1px solid var(--border-ghost)' }} onClick={prevStep}>BACK</button>
                <button
                  className="btn-primary"
                  onClick={nextStep}
                  disabled={!form.amount || numericAmount <= 0 || insufficientFunds}
                  style={{ padding: '16px 40px' }}
                >
                  REVIEW ALLOCATION
                </button>
              </div>
            </div>
          )}

          {/* ────── STEP 3: Confirm ────── */}
          {step === 3 && (
            <div className="wizard-panel">
              {isCompleted ? (
                /* ── SUCCESS STATE ── */
                <div style={{ textAlign: 'center', padding: '40px 0' }}>
                  <div style={{ width: '80px', height: '80px', background: 'var(--accent-secondary)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
                    <span className="material-symbols-outlined" style={{ color: 'white', fontSize: '48px' }}>check_circle</span>
                  </div>
                  <h3 style={{ fontSize: '32px', fontWeight: 900, marginBottom: '8px' }}>Settlement Complete</h3>
                  <p style={{ color: 'var(--text-muted)', marginBottom: '40px' }}>Your transaction has been written to the master ledger.</p>

                  <div style={{ background: 'var(--bg-surface-lowest)', borderRadius: 'var(--radius-lg)', padding: '24px', textAlign: 'left', border: '1px solid var(--border-ghost)', marginBottom: '40px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--border-ghost)' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>REFERENCE</span>
                      <span style={{ fontWeight: 800, fontFamily: 'monospace' }}>{result.referenceNumber}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--border-ghost)' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>ALLOCATION</span>
                      <span style={{ fontWeight: 800 }}>${parseFloat(result.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })} {result.currency || 'USD'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--border-ghost)' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>FROM</span>
                      <span style={{ fontWeight: 700, fontFamily: 'monospace' }}>{result.sourceAccountNumber}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>TO</span>
                      <span style={{ fontWeight: 700, fontFamily: 'monospace' }}>{result.destinationAccountNumber}</span>
                    </div>
                  </div>

                  <button className="btn-primary" style={{ padding: '16px 48px' }} onClick={handleReset}>
                    NEW ALLOCATION
                  </button>
                </div>
              ) : (
                /* ── REVIEW & AUTHORIZE ── */
                <>
                  <h3 style={{ fontSize: '24px', marginBottom: '32px', fontWeight: 900 }}>Final Authorization</h3>
                  <div className="transfer-summary" style={{ background: 'var(--bg-surface-lowest)', borderRadius: 'var(--radius-lg)', padding: '32px', border: '1px solid var(--border-ghost)', marginBottom: '32px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                      <span style={{ color: 'var(--text-muted)', fontSize: '12px', fontWeight: 700 }}>DEBIT FROM</span>
                      <span style={{ fontWeight: 800, fontFamily: 'monospace' }}>{form.sourceAccountNumber}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                      <span style={{ color: 'var(--text-muted)', fontSize: '12px', fontWeight: 700 }}>CREDIT TO</span>
                      <span style={{ fontWeight: 800, fontFamily: 'monospace' }}>{form.destinationAccountNumber}</span>
                    </div>
                    {form.description && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                        <span style={{ color: 'var(--text-muted)', fontSize: '12px', fontWeight: 700 }}>MEMO</span>
                        <span style={{ fontWeight: 600, maxWidth: '60%', textAlign: 'right' }}>{form.description}</span>
                      </div>
                    )}
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '24px', paddingTop: '24px', borderTop: '1px solid var(--border-ghost)' }}>
                      <span style={{ fontWeight: 900, fontSize: '18px' }}>TOTAL ALLOCATION</span>
                      <span style={{ fontWeight: 900, fontSize: '24px', color: 'var(--accent-primary)' }}>${parseFloat(form.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })} USD</span>
                    </div>
                    {selectedAccount && (
                      <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--text-muted)', textAlign: 'right' }}>
                        Post-transfer balance: ${(parseFloat(selectedAccount.balance) - numericAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </div>
                    )}
                    {numericAmount > 1000 && !mfaRequired && !mfaVerified && (
                      <div style={{ marginTop: '12px', padding: '10px 14px', background: 'rgba(255,185,0,0.08)', borderRadius: '8px', border: '1px solid rgba(255,185,0,0.2)', fontSize: '12px', color: 'var(--accent-tertiary)', fontWeight: 700 }}>
                        ⚡ Amount exceeds $1,000 — 2FA verification will be required.
                      </div>
                    )}
                  </div>

                  {/* ── MFA Panel ── */}
                  {mfaRequired && !mfaVerified && (
                    <div style={{ marginBottom: '24px', padding: '24px', borderRadius: '12px', border: '1px solid rgba(255,185,0,0.3)', background: 'rgba(255,185,0,0.05)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                        <span className="material-symbols-outlined" style={{ color: 'var(--accent-tertiary)', fontSize: '24px' }}>security</span>
                        <h4 style={{ margin: 0, color: 'var(--accent-tertiary)' }}>2FA Required</h4>
                      </div>
                      <p style={{ color: 'var(--text-muted)', marginBottom: '16px', fontSize: '13px' }}>
                        This transfer exceeds the fraud threshold. A 6-digit OTP has been dispatched to your registered channel. Enter it below to authorize.
                      </p>
                      <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <input
                          type="text"
                          inputMode="numeric"
                          value={otp}
                          onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                          placeholder="Enter 6-digit OTP"
                          style={{ width: '200px', padding: '12px 16px', borderRadius: '8px', border: '1px solid var(--border-ghost)', background: 'var(--bg-surface-container)', color: 'var(--text-primary)', fontFamily: 'monospace', fontSize: '18px', letterSpacing: '4px', textAlign: 'center' }}
                        />
                        <button
                          className="btn-primary"
                          onClick={handleMfaVerify}
                          disabled={mfaVerifying || otp.length !== 6}
                          style={{ padding: '12px 24px' }}
                        >
                          {mfaVerifying ? 'VERIFYING...' : 'VERIFY OTP'}
                        </button>
                      </div>
                    </div>
                  )}

                  {/* ── MFA Verified Confirmation ── */}
                  {mfaVerified && (
                    <div style={{ marginBottom: '24px', padding: '16px 20px', borderRadius: '10px', border: '1px solid rgba(16,185,129,0.3)', background: 'rgba(16,185,129,0.06)', display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span className="material-symbols-outlined" style={{ color: 'var(--accent-secondary)' }}>verified</span>
                      <span style={{ color: 'var(--accent-secondary)', fontWeight: 700, fontSize: '13px' }}>OTP Verified — Retrying transfer automatically...</span>
                    </div>
                  )}

                  {mfaMessage && !mfaVerified && (
                    <p style={{ marginBottom: '16px', color: 'var(--accent-secondary)', fontSize: '13px', fontWeight: 700 }}>{mfaMessage}</p>
                  )}

                  <div className="wizard-actions" style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <button className="btn-small" style={{ background: 'transparent', color: 'var(--text-muted)', border: '1px solid var(--border-ghost)' }} onClick={prevStep} disabled={loading}>BACK</button>
                    {!mfaRequired && (
                      <button
                        className="btn-primary"
                        onClick={handleSubmit}
                        disabled={loading || insufficientFunds}
                        style={{ padding: '16px 48px' }}
                      >
                        {loading ? 'PROCESSING...' : 'AUTHORIZE TRANSACTION'}
                      </button>
                    )}
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {/* ── Error Banner ── */}
      {error && (
        <div className="error-message" style={{ marginTop: '20px', maxWidth: '800px' }}>
          <span className="material-symbols-outlined" style={{ marginRight: '8px', fontSize: '18px' }}>error</span>
          {error}
        </div>
      )}



      {/* ── Demo Account Reference ── */}
      <div style={{ marginTop: 32, padding: 20, background: 'var(--bg-surface-container)', borderRadius: 12, border: '1px solid var(--border-ghost)', maxWidth: 640 }}>
        <h4 style={{ fontSize: 13, fontWeight: 800, marginBottom: 12, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '1px' }}>📋 Demo Account Numbers</h4>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
          {[
            { label: 'John Doe - Checking', num: 'VC-100001' },
            { label: 'John Doe - Savings',  num: 'VC-100002' },
            { label: 'Admin - Checking',    num: 'VC-200001' },
          ].map((a) => (
            <div
              key={a.num}
              style={{ background: 'var(--bg-surface-lowest)', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--border-ghost)', cursor: 'pointer' }}
              onClick={() => setForm(prev => ({ ...prev, destinationAccountNumber: a.num }))}
              title="Click to use as destination"
            >
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>{a.label}</div>
              <div style={{ fontFamily: 'monospace', fontWeight: 700, color: 'var(--accent-primary)', fontSize: 14 }}>{a.num}</div>
            </div>
          ))}
        </div>
        <p style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 10 }}>Click any account to set as destination</p>
      </div>
    </div>
  );
}
