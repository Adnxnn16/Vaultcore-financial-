import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const executeTransfer = createAsyncThunk('transfers/executeTransfer', async (transferData, { rejectWithValue }) => {
  try {
    const response = await api.post('/transfers', transferData);
    return response.data;
  } catch (err) {
    return rejectWithValue(err.response?.data || { message: 'Transfer failed' });
  }
});

const transferSlice = createSlice({
  name: 'transfers',
  initialState: { result: null, loading: false, error: null, mfaRequired: false },
  reducers: {
    resetTransfer: (state) => { state.result = null; state.error = null; state.mfaRequired = false; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(executeTransfer.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(executeTransfer.fulfilled, (state, action) => {
        state.loading = false;
        state.result = action.payload;
        state.mfaRequired = action.payload.mfaRequired;
      })
      .addCase(executeTransfer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || 'Transfer failed';
        state.mfaRequired = action.payload?.error === 'MFA_REQUIRED';
      });
  },
});

export const { resetTransfer } = transferSlice.actions;
export default transferSlice.reducer;
