package com.vaultcore.service;

import com.vaultcore.client.AlphaVantageStockClient;
import com.vaultcore.dto.PortfolioResponse;
import com.vaultcore.dto.StockPositionResponse;
import com.vaultcore.dto.StockTradeRequest;
import com.vaultcore.dto.StockTradeResponse;
import com.vaultcore.entity.*;
import com.vaultcore.exception.InsufficientBalanceException;
import com.vaultcore.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockPositionRepository stockPositionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final AlphaVantageStockClient alphaVantageClient;

    @Value("${app.fraud.threshold:10000.00}")
    private BigDecimal fraudThreshold;

    private static final String MARKET_ACCOUNT_NUMBER = "VC-MARKET-001";

    public StockService(StockPositionRepository stockPositionRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository,
                        LedgerEntryRepository ledgerEntryRepository,
                        UserRepository userRepository,
                        AlphaVantageStockClient alphaVantageClient) {
        this.stockPositionRepository = stockPositionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.userRepository = userRepository;
        this.alphaVantageClient = alphaVantageClient;
    }

    @CircuitBreaker(name = "stockService", fallbackMethod = "getPortfolioFallback")
    @Cacheable(value = "portfolio", key = "#userId")
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID userId) {
        List<StockPosition> positions = stockPositionRepository.findByUser_Id(userId);
        
        List<StockPositionResponse> posDtos = positions.stream()
                .map(p -> {
                    BigDecimal marketValue = p.getCurrentPrice().multiply(p.getQuantity());
                    BigDecimal totalCost = p.getAverageCost().multiply(p.getQuantity());
                    return StockPositionResponse.builder()
                            .symbol(p.getSymbol())
                            .companyName(p.getCompanyName())
                            .quantity(p.getQuantity().intValue())
                            .averageCost(p.getAverageCost())
                            .currentPrice(p.getCurrentPrice())
                            .marketValue(marketValue)
                            .unrealizedGain(marketValue.subtract(totalCost))
                            .unrealizedGainPercent(totalCost.compareTo(BigDecimal.ZERO) > 0 ? 
                                marketValue.subtract(totalCost).divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                                BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = posDtos.stream()
                .map(StockPositionResponse::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PortfolioResponse.builder()
                .userId(userId)
                .positions(posDtos)
                .totalMarketValue(totalMarketValue)
                .build();
    }

    @Transactional(readOnly = true)
    public StockPositionResponse getStockBySymbol(UUID userId, String symbol) {
        StockPosition p = stockPositionRepository.findByUser_IdAndSymbol(userId, symbol)
                .orElseThrow(() -> new IllegalArgumentException("Position not found for symbol: " + symbol));

        BigDecimal marketValue = p.getCurrentPrice().multiply(p.getQuantity());
        BigDecimal totalCost = p.getAverageCost().multiply(p.getQuantity());

        return StockPositionResponse.builder()
                .symbol(p.getSymbol())
                .companyName(p.getCompanyName())
                .quantity(p.getQuantity().intValue())
                .averageCost(p.getAverageCost())
                .currentPrice(p.getCurrentPrice())
                .marketValue(marketValue)
                .unrealizedGain(marketValue.subtract(totalCost))
                .unrealizedGainPercent(totalCost.compareTo(BigDecimal.ZERO) > 0 ? 
                    marketValue.subtract(totalCost).divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                    BigDecimal.ZERO)
                .build();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @CircuitBreaker(name = "stockService", fallbackMethod = "executeTradeFallback")
    @CacheEvict(value = "portfolio", key = "#userId")
    public StockTradeResponse executeTrade(UUID userId, StockTradeRequest request) {
        log.info("Executing {} trade for user {} and symbol {}", request.getType(), userId, request.getSymbol());

        // ── Live price from Alpha Vantage (with circuit-breaker fallback) ──
        BigDecimal currentPrice = alphaVantageClient.getQuote(request.getSymbol()).getCurrentPrice();
        log.info("[TRADE] Live price for {}: ${}", request.getSymbol(), currentPrice);
        BigDecimal totalTradeAmount = currentPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        // Fraud Threshold Check removed

        Account userAccount = accountRepository.findByUserIdAndAccountNumber(userId, request.getSourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("User account not found: " + request.getSourceAccountNumber()));

        Account marketAccount = accountRepository.findByAccountNumber(MARKET_ACCOUNT_NUMBER)
                .orElseThrow(() -> new IllegalStateException("System Market account not found. Critical initialization error."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User entity not found: " + userId));

        StockPosition position = stockPositionRepository.findByUser_IdAndSymbol(userId, request.getSymbol())
                .orElseGet(() -> {
                    StockPosition newPos = new StockPosition();
                    newPos.setUser(user);
                    newPos.setSymbol(request.getSymbol());
                    newPos.setCompanyName(request.getSymbol() + " Corp");
                    newPos.setQuantity(BigDecimal.ZERO);
                    newPos.setAverageCost(BigDecimal.ZERO);
                    newPos.setCurrentPrice(currentPrice);
                    return newPos;
                });

        position.setCurrentPrice(currentPrice);

        String txTypeLabel = "STOCK_BUY";
        Account source = userAccount;
        Account destination = marketAccount;

        if ("BUY".equalsIgnoreCase(request.getType())) {
            if (userAccount.getBalance().compareTo(totalTradeAmount) < 0) {
                throw new InsufficientBalanceException("Insufficient funds for trade.");
            }
            BigDecimal newQuantity = position.getQuantity().add(BigDecimal.valueOf(request.getQuantity()));
            BigDecimal totalOldCost = position.getAverageCost().multiply(position.getQuantity());
            position.setAverageCost(totalOldCost.add(totalTradeAmount).divide(newQuantity, 4, RoundingMode.HALF_UP));
            position.setQuantity(newQuantity);
            
            userAccount.setBalance(userAccount.getBalance().subtract(totalTradeAmount));
            marketAccount.setBalance(marketAccount.getBalance().add(totalTradeAmount));
        } else {
            if (position.getQuantity().compareTo(BigDecimal.valueOf(request.getQuantity())) < 0) {
                throw new IllegalArgumentException("Insufficient stock shares to sell.");
            }
            txTypeLabel = "STOCK_SELL";
            source = marketAccount;
            destination = userAccount;
            position.setQuantity(position.getQuantity().subtract(BigDecimal.valueOf(request.getQuantity())));
            
            userAccount.setBalance(userAccount.getBalance().add(totalTradeAmount));
            marketAccount.setBalance(marketAccount.getBalance().subtract(totalTradeAmount));
        }

        if (position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stockPositionRepository.delete(position);
        } else {
            stockPositionRepository.save(position);
        }

        accountRepository.save(userAccount);
        accountRepository.save(marketAccount);

        // Record Transaction (Double-entry compliant)
        Transaction tx = Transaction.builder()
                .referenceNumber("STK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(totalTradeAmount)
                .status("COMPLETED")
                .description("Stock " + request.getType() + ": " + request.getQuantity() + " " + request.getSymbol())
                .completedAt(LocalDateTime.now())
                .transactionType(txTypeLabel)
                .build();
        transactionRepository.save(tx);

        // Write Ledger Entries
        ledgerEntryRepository.save(LedgerEntry.builder()
                .transaction(tx).account(source).entryType("DEBIT").amount(totalTradeAmount).balanceAfter(source.getBalance()).build());
        ledgerEntryRepository.save(LedgerEntry.builder()
                .transaction(tx).account(destination).entryType("CREDIT").amount(totalTradeAmount).balanceAfter(destination.getBalance()).build());

        return StockTradeResponse.builder()
                .transactionId(tx.getId() != null ? tx.getId().toString() : UUID.randomUUID().toString())
                .referenceNumber(tx.getReferenceNumber())
                .symbol(request.getSymbol())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(currentPrice)
                .totalAmount(totalTradeAmount)
                .status("COMPLETED")
                .message("Trade executed successfully")
                .build();
    }

    // Fallbacks
    public PortfolioResponse getPortfolioFallback(UUID userId, Throwable t) {
        log.error("Portfolio fallback for user {}: {}", userId, t.getMessage());
        return PortfolioResponse.builder()
                .userId(userId)
                .positions(List.of())
                .totalMarketValue(BigDecimal.ZERO)
                .build();
    }

    public StockTradeResponse executeTradeFallback(UUID userId, StockTradeRequest request, Throwable t) {
        log.error("Trade fallback for user {} and symbol {}: {}", userId, request.getSymbol(), t.getMessage());
        return StockTradeResponse.builder()
                .status("FAILED_CIRCUIT_OPEN")
                .symbol(request.getSymbol())
                .message("Service is currently unavailable. Please try again later.")
                .build();
    }
}
