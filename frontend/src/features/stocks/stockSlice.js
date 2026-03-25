import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

// ── Async Thunks ─────────────────────────────────────────────────────────────

export const fetchPortfolio = createAsyncThunk('stocks/fetchPortfolio', async (userId) => {
  const response = await api.get(`/stocks/portfolio`, { headers: { 'X-User-Id': userId } });
  return response.data;
});

/**
 * Fetch a real-time stock quote from Finnhub (via backend proxy).
 * GET /api/v1/stocks/quote/{symbol}
 */
export const fetchQuote = createAsyncThunk('stocks/fetchQuote', async (symbol, { rejectWithValue }) => {
  try {
    const response = await api.get(`/stocks/quote/${symbol.toUpperCase()}`);
    return response.data; // { symbol, currentPrice, change, changePercent, high, low, source, ... }
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to fetch quote');
  }
});

export const executeTrade = createAsyncThunk('stocks/executeTrade', async (tradeData, { rejectWithValue }) => {
  try {
    const response = await api.post('/stocks/trade', {
      symbol: tradeData.symbol,
      type: tradeData.type,
      quantity: tradeData.quantity,
      sourceAccountNumber: tradeData.sourceAccountNumber,
    }, { headers: { 'X-User-Id': tradeData.userId } });
    return response.data;
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || err.message || 'Trade execution failed');
  }
});

// ── Slice ─────────────────────────────────────────────────────────────────────

const stockSlice = createSlice({
  name: 'stocks',
  initialState: {
    portfolio: null,
    loading: false,
    error: null,
    // Quote lookup
    quote: null,
    quoteLoading: false,
    quoteError: null,
    // Trade
    tradeResult: null,
    tradeLoading: false,
    tradeError: null,
  },
  reducers: {
    clearTradeResult: (state) => {
      state.tradeResult = null;
      state.tradeError = null;
    },
    clearQuote: (state) => {
      state.quote = null;
      state.quoteError = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Portfolio
      .addCase(fetchPortfolio.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(fetchPortfolio.fulfilled, (state, action) => { state.loading = false; state.portfolio = action.payload; })
      .addCase(fetchPortfolio.rejected,  (state, action) => { state.loading = false; state.error = action.error.message; })
      // Quote
      .addCase(fetchQuote.pending,   (state) => { state.quoteLoading = true; state.quoteError = null; state.quote = null; })
      .addCase(fetchQuote.fulfilled, (state, action) => { state.quoteLoading = false; state.quote = action.payload; })
      .addCase(fetchQuote.rejected,  (state, action) => { state.quoteLoading = false; state.quoteError = action.payload; })
      // Trade
      .addCase(executeTrade.pending,   (state) => { state.tradeLoading = true; state.tradeError = null; state.tradeResult = null; })
      .addCase(executeTrade.fulfilled, (state, action) => { state.tradeLoading = false; state.tradeResult = action.payload; })
      .addCase(executeTrade.rejected,  (state, action) => { state.tradeLoading = false; state.tradeError = action.payload; });
  },
});

export const { clearTradeResult, clearQuote } = stockSlice.actions;
export default stockSlice.reducer;
