import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchPortfolio = createAsyncThunk('stocks/fetchPortfolio', async (userId) => {
  const response = await api.get(`/stocks/portfolio/${userId}`);
  return response.data;
});

const stockSlice = createSlice({
  name: 'stocks',
  initialState: { portfolio: null, loading: false, error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPortfolio.pending, (state) => { state.loading = true; })
      .addCase(fetchPortfolio.fulfilled, (state, action) => { state.loading = false; state.portfolio = action.payload; })
      .addCase(fetchPortfolio.rejected, (state, action) => { state.loading = false; state.error = action.error.message; });
  },
});

export default stockSlice.reducer;
