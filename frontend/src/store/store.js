import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import accountReducer from '../features/accounts/accountSlice';
import transferReducer from '../features/transfer/transferSlice';
import stockReducer from '../features/stocks/stockSlice';
import auditReducer from '../features/audit/auditSlice';
import statementReducer from '../features/statements/statementSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    accounts: accountReducer,
    transfers: transferReducer,
    stocks: stockReducer,
    audit: auditReducer,
    statements: statementReducer,
  },
});
