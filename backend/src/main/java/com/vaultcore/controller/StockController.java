package com.vaultcore.controller;

import com.vaultcore.dto.PortfolioResponse;
import com.vaultcore.dto.StockPositionResponse;
import com.vaultcore.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

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
}
