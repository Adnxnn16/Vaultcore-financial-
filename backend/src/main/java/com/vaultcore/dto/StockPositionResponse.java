package com.vaultcore.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPositionResponse {
    private String id;
    private String symbol;
    private String companyName;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercent;
}

