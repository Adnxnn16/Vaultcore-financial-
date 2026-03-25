import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

/**
 * Fetch all accounts for the logged-in user.
 * Backend: GET /api/v1/accounts  (X-User-Id header injected by axios interceptor)
 */
export const fetchAccounts = createAsyncThunk(
  'accounts/fetchAccounts',
  async (userId, { rejectWithValue }) => {
    try {
      // X-User-Id is automatically added by the axios request interceptor
      const response = await api.get('/accounts', {
        headers: { 'X-User-Id': userId },
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to load accounts');
    }
  }
);

export const fetchBalance = createAsyncThunk(
  'accounts/fetchBalance',
  async (accountId) => {
    const response = await api.get(`/accounts/${accountId}/balance`);
    return { accountId, balance: response.data };
  }
);

const accountSlice = createSlice({
  name: 'accounts',
  initialState: { accounts: [], loading: false, error: null },
  reducers: {
    clearAccountError: (state) => { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAccounts.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(fetchAccounts.fulfilled, (state, action) => { state.loading = false; state.accounts = action.payload; })
      .addCase(fetchAccounts.rejected,  (state, action) => { state.loading = false; state.error = action.payload; });
  },
});

export const { clearAccountError } = accountSlice.actions;
export default accountSlice.reducer;
