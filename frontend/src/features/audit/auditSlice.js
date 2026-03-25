import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

/**
 * Fetch audit logs — admin only.
 * GET /api/v1/audit/logs?page=0&size=20&sort=createdAt,desc
 */
export const fetchAuditLogs = createAsyncThunk(
  'audit/fetchAuditLogs',
  async ({ page = 0, size = 20, userId = null, method = null } = {}, { rejectWithValue }) => {
    try {
      const params = new URLSearchParams({ page, size, sort: 'createdAt,desc' });
      if (userId) params.append('userId', userId);
      if (method) params.append('method', method);
      const response = await api.get(`/audit/logs?${params.toString()}`);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to load audit logs');
    }
  }
);

const auditSlice = createSlice({
  name: 'audit',
  initialState: {
    logs: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    loading: false,
    error: null,
  },
  reducers: {
    clearAuditError: (state) => { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAuditLogs.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(fetchAuditLogs.fulfilled, (state, action) => {
        state.loading = false;
        // Spring Page response structure
        const data = action.payload;
        if (data?.content) {
          state.logs          = data.content;
          state.totalElements = data.totalElements;
          state.totalPages    = data.totalPages;
          state.currentPage   = data.number;
        } else {
          // Fallback if backend returns plain array
          state.logs = Array.isArray(data) ? data : [];
        }
      })
      .addCase(fetchAuditLogs.rejected,  (state, action) => {
        state.loading = false;
        state.error   = action.payload;
      });
  },
});

export const { clearAuditError } = auditSlice.actions;
export default auditSlice.reducer;
