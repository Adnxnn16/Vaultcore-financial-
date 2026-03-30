import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchAccounts = createAsyncThunk('accounts/fetchAccounts', async (userId) => {
  const response = await api.get(`/accounts/user/${userId}`);
  return response.data;
});

export const fetchBalance = createAsyncThunk('accounts/fetchBalance', async (accountId) => {
  const response = await api.get(`/accounts/${accountId}/balance`);
  return { accountId, balance: response.data };
});

const accountSlice = createSlice({
  name: 'accounts',
  initialState: { accounts: [], loading: false, error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchAccounts.pending, (state) => { state.loading = true; })
      .addCase(fetchAccounts.fulfilled, (state, action) => { state.loading = false; state.accounts = action.payload; })
      .addCase(fetchAccounts.rejected, (state, action) => { state.loading = false; state.error = action.error.message; });
  },
});

export default accountSlice.reducer;
