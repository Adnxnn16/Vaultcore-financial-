import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

/**
 * Generate and download a PDF statement.
 * GET /api/v1/statements/{accountId}/pdf?startDate=&endDate=
 * Returns a blob (PDF).
 */
export const downloadStatement = createAsyncThunk(
  'statements/downloadStatement',
  async ({ accountId, startDate, endDate }, { rejectWithValue }) => {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate)   params.append('endDate', endDate);
      const response = await api.get(
        `/statements/${accountId}/pdf?${params.toString()}`,
        { responseType: 'blob' }
      );
      // Trigger browser download
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statement-${accountId}-${startDate || 'all'}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      return { success: true, accountId };
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to download statement');
    }
  }
);

const statementSlice = createSlice({
  name: 'statements',
  initialState: {
    downloading: false,
    downloadError: null,
    lastDownloaded: null,
  },
  reducers: {
    clearStatementError: (state) => { state.downloadError = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(downloadStatement.pending,   (state) => { state.downloading = true; state.downloadError = null; })
      .addCase(downloadStatement.fulfilled, (state, action) => {
        state.downloading    = false;
        state.lastDownloaded = action.payload.accountId;
      })
      .addCase(downloadStatement.rejected,  (state, action) => {
        state.downloading  = false;
        state.downloadError = action.payload;
      });
  },
});

export const { clearStatementError } = statementSlice.actions;
export default statementSlice.reducer;
