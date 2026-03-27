package com.vaultcore.service;

import com.vaultcore.dto.PortfolioResponse;
import com.vaultcore.dto.StockPositionResponse;
import com.vaultcore.entity.StockPosition;
import com.vaultcore.repository.StockPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockPositionRepository stockPositionRepository;

    @Cacheable(value = "portfolio", key = "#userId")
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID userId) {
        List<StockPosition> positions = stockPositionRepository.findByUserId(userId);

        List<StockPositionResponse> positionResponses = positions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        BigDecimal totalValue = positionResponses.stream()
                .map(StockPositionResponse::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = positions.stream()
                .map(p -> p.getAverageCost().multiply(p.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGainLoss = totalValue.subtract(totalCost);
        BigDecimal totalGainLossPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalGainLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return PortfolioResponse.builder()
                .totalValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                .totalCost(totalCost.setScale(2, RoundingMode.HALF_UP))
                .totalGainLoss(totalGainLoss.setScale(2, RoundingMode.HALF_UP))
                .totalGainLossPercent(totalGainLossPercent.setScale(2, RoundingMode.HALF_UP))
                .positions(positionResponses)
                .build();
    }

    @Cacheable(value = "stock", key = "#symbol")
    public StockPositionResponse getStockBySymbol(UUID userId, String symbol) {
        StockPosition position = stockPositionRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new RuntimeException("Stock position not found: " + symbol));
        return toResponse(position);
    }

    private StockPositionResponse toResponse(StockPosition position) {
        BigDecimal totalValue = position.getCurrentPrice() != null
                ? position.getCurrentPrice().multiply(position.getQuantity())
                : BigDecimal.ZERO;
        BigDecimal totalCost = position.getAverageCost().multiply(position.getQuantity());
        BigDecimal gainLoss = totalValue.subtract(totalCost);
        BigDecimal gainLossPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? gainLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return StockPositionResponse.builder()
                .id(position.getId().toString())
                .symbol(position.getSymbol())
                .companyName(position.getCompanyName())
                .quantity(position.getQuantity())
                .averageCost(position.getAverageCost())
                .currentPrice(position.getCurrentPrice())
                .totalValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                .gainLoss(gainLoss.setScale(2, RoundingMode.HALF_UP))
                .gainLossPercent(gainLossPercent.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
