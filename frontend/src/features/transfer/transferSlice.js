import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

/**
 * Execute a bank-to-bank transfer.
 *
 * POST /api/v1/transfers
 * Body: { sourceAccountNumber, destinationAccountNumber, amount, description, currency }
 *
 * Possible responses:
 *  200 COMPLETED     — Transfer succeeded, referenceNumber returned
 *  403 PENDING_MFA   — Amount exceeds fraud threshold; OTP dispatched
 *  400 BAD_REQUEST   — Validation error, insufficient balance, same account etc.
 */
export const executeTransfer = createAsyncThunk(
  'transfers/executeTransfer',
  async (transferData, { rejectWithValue }) => {
    try {
      const response = await api.post('/transfers', {
        sourceAccountNumber: transferData.sourceAccountNumber,
        destinationAccountNumber: transferData.destinationAccountNumber,
        amount: transferData.amount,
        description: transferData.description || '',
        currency: transferData.currency || 'USD',
      });

      return response.data;

    } catch (err) {
      const data = err.response?.data;
      const isMfaPending = data?.error === 'PENDING_MFA';
      return rejectWithValue({
        error: data?.error || null,
        mfaRequired: isMfaPending,
        message: data?.message || data?.error || err.message || 'Transfer failed',
      });
    }
  }
);

/**
 * Verify the MFA OTP.
 * POST /api/v1/auth/mfa/verify
 * Body: { userId: UUID, otp: string }
 * 
 * After successful verification, the caller should retry executeTransfer.
 * The backend FraudInterceptor sets a one-time "mfa_verified:<userId>" Redis key.
 */
export const verifyTransferMfa = createAsyncThunk(
  'transfers/verifyTransferMfa',
  async ({ userId, otp }, { rejectWithValue }) => {
    try {
      const response = await api.post('/auth/mfa/verify', { userId, otp });
      return response.data;
    } catch (err) {
      const data = err.response?.data;
      return rejectWithValue({
        message: data?.message || data?.error || err.message || 'OTP verification failed',
      });
    }
  }
);

const transferSlice = createSlice({
  name: 'transfers',
  initialState: {
    result: null,
    loading: false,
    error: null,
    mfaRequired: false,
    mfaVerifying: false,
    mfaMessage: null,
    mfaVerified: false, // true after OTP verified; triggers UI to show "retry" prompt
  },
  reducers: {
    resetTransfer: (state) => {
      state.result      = null;
      state.error       = null;
      state.loading     = false;
      state.mfaRequired  = false;
      state.mfaVerifying = false;
      state.mfaMessage   = null;
      state.mfaVerified  = false;
    },
    clearMfaVerified: (state) => {
      state.mfaVerified = false;
      state.mfaMessage  = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // ── executeTransfer ──────────────────────────────────────────
      .addCase(executeTransfer.pending, (state) => {
        state.loading    = true;
        state.error      = null;
        state.mfaMessage = null;
        state.mfaVerified = false;
      })
      .addCase(executeTransfer.fulfilled, (state, action) => {
        state.loading    = false;
        state.result     = action.payload;
        state.mfaRequired = false;
        state.mfaVerified = false;
      })
      .addCase(executeTransfer.rejected, (state, action) => {
        state.loading    = false;
        state.error      = action.payload?.mfaRequired
          ? null  // don't show error when MFA is required — show the MFA panel instead
          : (action.payload?.message || 'Transfer failed');
        state.mfaRequired = !!action.payload?.mfaRequired;
      })
      // ── verifyTransferMfa ────────────────────────────────────────
      .addCase(verifyTransferMfa.pending, (state) => {
        state.mfaVerifying = true;
        state.error = null;
      })
      .addCase(verifyTransferMfa.fulfilled, (state) => {
        state.mfaVerifying = false;
        state.mfaRequired  = false;
        state.mfaVerified  = true;  // signal: retry transfer now
        state.mfaMessage   = 'OTP verified — please click Authorize to complete your transfer.';
      })
      .addCase(verifyTransferMfa.rejected, (state, action) => {
        state.mfaVerifying = false;
        state.error = action.payload?.message || 'OTP verification failed';
      });
  },
});

export const { resetTransfer, clearMfaVerified } = transferSlice.actions;
export default transferSlice.reducer;
