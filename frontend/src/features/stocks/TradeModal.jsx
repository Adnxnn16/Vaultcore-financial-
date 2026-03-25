import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { executeTrade, clearTradeResult, fetchQuote, clearQuote } from './stockSlice';
import { fetchAccounts } from '../accounts/accountSlice';

export default function TradeModal({ isOpen, onClose, initialSymbol = '', initialType = 'BUY' }) {
  const dispatch = useDispatch();
  const [form, setForm] = useState({
    symbol: initialSymbol,
    type: initialType,
    quantity: '',
    sourceAccountNumber: '',
  });

  const { tradeLoading, tradeError, tradeResult, quote, quoteLoading } = useSelector((s) => s.stocks);
  const { accounts, loading: accountsLoading } = useSelector((s) => s.accounts);
  const { user } = useSelector((s) => s.auth);

  // Reset on open
  useEffect(() => {
    if (isOpen) {
      setForm({ symbol: initialSymbol, type: initialType, quantity: '', sourceAccountNumber: '' });
      dispatch(clearTradeResult());
      dispatch(clearQuote());
      if (user?.id && (!accounts || accounts.length === 0)) {
        dispatch(fetchAccounts(user.id));
      }
    }
  }, [isOpen, initialSymbol, initialType, dispatch, user, accounts]);

  // Fetch live price whenever symbol changes (with debounce)
  useEffect(() => {
    if (!form.symbol || form.symbol.length < 1) return;
    const timer = setTimeout(() => {
      dispatch(fetchQuote(form.symbol));
    }, 600); // 600ms debounce
    return () => clearTimeout(timer);
  }, [form.symbol, dispatch]);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: name === 'symbol' ? value.toUpperCase() : value }));
  };

  const estimatedTotal =
    quote?.currentPrice && form.quantity
      ? (parseFloat(quote.currentPrice) * parseInt(form.quantity, 10)).toFixed(2)
      : null;

  const handleSubmit = (e) => {
    e.preventDefault();
    dispatch(executeTrade({
      symbol: form.symbol,
      type: form.type,
      quantity: parseInt(form.quantity, 10),
      sourceAccountNumber: form.sourceAccountNumber,
      userId: user?.id,
    }));
  };

  const handleClose = () => {
    dispatch(clearTradeResult());
    dispatch(clearQuote());
    onClose();
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{form.type === 'BUY' ? '🟢 Buy' : '🔴 Sell'} Stock</h2>
          <button className="close-btn" onClick={handleClose}>&times;</button>
        </div>

        {tradeResult ? (
          <div className="trade-success">
            <div className="success-icon">✅</div>
            <h3>Trade Executed!</h3>
            <div className="trade-details">
              <p>Ref: <strong>{tradeResult.referenceNumber}</strong></p>
              <p>{tradeResult.type} {tradeResult.quantity} × {tradeResult.symbol}</p>
              <p>Price: <strong>${parseFloat(tradeResult.price || tradeResult.executedPrice || 0).toFixed(2)}</strong></p>
              <p>Total: <strong>${parseFloat(tradeResult.totalAmount || 0).toLocaleString()}</strong></p>
            </div>
            <button className="btn-primary" onClick={handleClose}>Done</button>
          </div>
        ) : (
          <form className="trade-form" onSubmit={handleSubmit}>

            {/* Action */}
            <div className="form-group">
              <label>Action</label>
              <select name="type" value={form.type} onChange={handleChange} required>
                <option value="BUY">BUY</option>
                <option value="SELL">SELL</option>
              </select>
            </div>

            {/* Symbol + Live Price */}
            <div className="form-group">
              <label>Symbol</label>
              <input
                name="symbol"
                value={form.symbol}
                onChange={handleChange}
                placeholder="e.g. AAPL"
                required
              />
              {/* Live Quote Badge */}
              {quoteLoading && (
                <div className="quote-badge loading">⏳ Fetching live price...</div>
              )}
              {quote && !quoteLoading && (
                <div className={`quote-badge ${parseFloat(quote.changePercent) >= 0 ? 'positive' : 'negative'}`}>
                  <span className="quote-price">${parseFloat(quote.currentPrice).toFixed(2)}</span>
                  <span className="quote-change">
                    {parseFloat(quote.changePercent) >= 0 ? '▲' : '▼'}
                    {Math.abs(parseFloat(quote.changePercent)).toFixed(2)}%
                  </span>
                  <span className="quote-source">{quote.source === 'FINNHUB' ? '🟢 Live' : '🟡 Ref'}</span>
                  <div className="quote-detail">
                    H: ${parseFloat(quote.high).toFixed(2)} · L: ${parseFloat(quote.low).toFixed(2)} · Prev: ${parseFloat(quote.previousClose).toFixed(2)}
                  </div>
                </div>
              )}
            </div>

            {/* Quantity */}
            <div className="form-group">
              <label>Quantity</label>
              <input
                name="quantity"
                type="number"
                min="1"
                value={form.quantity}
                onChange={(e) => setForm((p) => ({ ...p, quantity: e.target.value }))}
                required
              />
              {/* Estimated Total */}
              {estimatedTotal && (
                <div className="estimated-total">
                  Estimated {form.type === 'BUY' ? 'Cost' : 'Proceeds'}:
                  <strong> ${parseFloat(estimatedTotal).toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                </div>
              )}
            </div>

            {/* Source Account */}
            <div className="form-group">
              <label>Linked Account</label>
              {accountsLoading ? (
                <p>Loading accounts...</p>
              ) : (
                <select
                  name="sourceAccountNumber"
                  value={form.sourceAccountNumber}
                  onChange={(e) => setForm((p) => ({ ...p, sourceAccountNumber: e.target.value }))}
                  required
                >
                  <option value="">Select funding account</option>
                  {accounts?.map((a) => (
                    <option key={a.id} value={a.accountNumber}>
                      {a.accountNumber} — ${parseFloat(a.balance).toLocaleString()}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {tradeError && <div className="error-message">{tradeError}</div>}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={handleClose}>Cancel</button>
              <button type="submit" className="btn-primary" disabled={tradeLoading || accountsLoading}>
                {tradeLoading ? 'Processing...' : `Confirm ${form.type}`}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
