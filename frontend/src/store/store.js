import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import accountReducer from '../features/accounts/accountSlice';
import transferReducer from '../features/transfer/transferSlice';
import stockReducer from '../features/stocks/stockSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    accounts: accountReducer,
    transfers: transferReducer,
    stocks: stockReducer,
  },
});
