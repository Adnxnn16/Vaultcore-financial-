package com.vaultcore.controller;

import com.vaultcore.client.AlphaVantageStockClient;
import com.vaultcore.dto.PortfolioResponse;
import com.vaultcore.dto.StockPositionResponse;
import com.vaultcore.dto.StockQuoteResponse;
import com.vaultcore.dto.StockTradeRequest;
import com.vaultcore.dto.StockTradeResponse;
import com.vaultcore.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;
    private final AlphaVantageStockClient stockClient;

    public StockController(StockService stockService, AlphaVantageStockClient stockClient) {
        this.stockService = stockService;
        this.stockClient = stockClient;
    }

    /**
     * PRD §4.5 — watchlist symbols, comma-separated (e.g. {@code ?symbols=AAPL,MSFT}).
     */
    @GetMapping("/prices")
    public ResponseEntity<List<StockQuoteResponse>> getPrices(@RequestParam String symbols) {
        List<StockQuoteResponse> out = new ArrayList<>();
        for (String raw : symbols.split(",")) {
            String s = raw.trim();
            if (!s.isEmpty()) {
                out.add(stockClient.getQuote(s));
            }
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(stockService.getPortfolio(userId));
    }

    @GetMapping("/positions/{symbol}")
    public ResponseEntity<StockPositionResponse> getStock(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(userId, symbol));
    }

    /**
     * Real-time stock quote from Finnhub.
     * Cached 5s, circuit-broken with fallback prices.
     * GET /api/v1/stocks/quote/AAPL
     */
    @GetMapping("/quote/{symbol}")
    public ResponseEntity<StockQuoteResponse> getQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(stockClient.getQuote(symbol.toUpperCase()));
    }

    @PostMapping("/trade")
    public ResponseEntity<StockTradeResponse> executeTrade(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody StockTradeRequest request) {
        return ResponseEntity.ok(stockService.executeTrade(userId, request));
    }
}
