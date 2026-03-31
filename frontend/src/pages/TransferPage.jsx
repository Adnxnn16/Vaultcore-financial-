import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { executeTransfer, resetTransfer } from '../features/transfer/transferSlice';

export default function TransferPage() {
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({ sourceAccountNumber: '', destinationAccountNumber: '', amount: '', description: '', otpCode: '' });
  const dispatch = useDispatch();
  const { result, loading, error, mfaRequired } = useSelector((state) => state.transfers);
  const { accounts } = useSelector((state) => state.accounts);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const nextStep = () => setStep((s) => Math.min(s + 1, 3));
  const prevStep = () => setStep((s) => Math.max(s - 1, 1));

  const handleSubmit = () => {
    dispatch(executeTransfer({
      sourceAccountNumber: form.sourceAccountNumber,
      destinationAccountNumber: form.destinationAccountNumber,
      amount: parseFloat(form.amount),
      description: form.description,
      otpCode: form.otpCode || undefined,
    }));
  };

  const handleReset = () => {
    setStep(1);
    setForm({ sourceAccountNumber: '', destinationAccountNumber: '', amount: '', description: '', otpCode: '' });
    dispatch(resetTransfer());
  };

  return (
    <div className="transfer-page">
      <div className="page-header">
        <h1>Transfer Funds</h1>
        <p className="page-subtitle">Send money between accounts</p>
      </div>

      <div className="transfer-wizard">
        <div className="wizard-steps">
          <div className={`wizard-step ${step >= 1 ? 'active' : ''}`}><span>1</span> Select Accounts</div>
          <div className={`wizard-step ${step >= 2 ? 'active' : ''}`}><span>2</span> Amount</div>
          <div className={`wizard-step ${step >= 3 ? 'active' : ''}`}><span>3</span> Confirm</div>
        </div>

        <div className="wizard-body">
          {step === 1 && (
            <div className="wizard-panel">
              <h3>Select Accounts</h3>
              <div className="form-group">
                <label>From Account</label>
                <select name="sourceAccountNumber" value={form.sourceAccountNumber} onChange={handleChange}>
                  <option value="">Select source account</option>
                  {accounts.map((a) => <option key={a.id} value={a.accountNumber}>{a.accountNumber} — ${parseFloat(a.balance).toLocaleString()}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>To Account</label>
                <input name="destinationAccountNumber" value={form.destinationAccountNumber} onChange={handleChange} placeholder="Enter destination account number" />
              </div>
              <div className="wizard-actions">
                <button className="btn-primary" onClick={nextStep} disabled={!form.sourceAccountNumber || !form.destinationAccountNumber}>Next →</button>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="wizard-panel">
              <h3>Enter Amount</h3>
              <div className="form-group">
                <label>Amount (USD)</label>
                <input name="amount" type="number" step="0.01" min="0.01" value={form.amount} onChange={handleChange} placeholder="0.00" className="amount-input" />
              </div>
              <div className="form-group">
                <label>Description (optional)</label>
                <input name="description" value={form.description} onChange={handleChange} placeholder="What is this transfer for?" />
              </div>
              {parseFloat(form.amount) > 10000 && (
                <div className="mfa-warning">⚠️ High-value transfer — OTP verification will be required</div>
              )}
              <div className="wizard-actions">
                <button className="btn-secondary" onClick={prevStep}>← Back</button>
                <button className="btn-primary" onClick={nextStep} disabled={!form.amount || parseFloat(form.amount) <= 0}>Next →</button>
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="wizard-panel">
              <h3>Confirm Transfer</h3>
              <div className="transfer-summary">
                <div className="summary-row"><span>From:</span><span>{form.sourceAccountNumber}</span></div>
                <div className="summary-row"><span>To:</span><span>{form.destinationAccountNumber}</span></div>
                <div className="summary-row highlight"><span>Amount:</span><span>${parseFloat(form.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span></div>
                {form.description && <div className="summary-row"><span>Note:</span><span>{form.description}</span></div>}
              </div>

              {mfaRequired && (
                <div className="form-group mfa-section">
                  <label>Enter OTP Code</label>
                  <input name="otpCode" value={form.otpCode} onChange={handleChange} placeholder="6-digit code" maxLength={6} />
                  <p className="mfa-hint">An OTP has been sent. Check the server logs for the demo code.</p>
                </div>
              )}

              {error && <div className="error-message">{error}</div>}
              {result && <div className="success-message">✅ Transfer completed! Ref: {result.referenceNumber}</div>}

              <div className="wizard-actions">
                {!result && <button className="btn-secondary" onClick={prevStep}>← Back</button>}
                {!result && <button className="btn-primary" onClick={handleSubmit} disabled={loading}>{loading ? 'Processing...' : 'Confirm Transfer'}</button>}
                {result && <button className="btn-primary" onClick={handleReset}>New Transfer</button>}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
