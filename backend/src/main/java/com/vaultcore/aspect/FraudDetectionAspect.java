package com.vaultcore.aspect;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.dto.StockTradeRequest;
import com.vaultcore.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Aspect
@Component
public class FraudDetectionAspect {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionAspect.class);

    private final AuditService auditService;

    @Value("${app.fraud.threshold:10000.00}")
    private BigDecimal fraudThreshold;

    public FraudDetectionAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Before("execution(* com.vaultcore.service.TransferService.transfer(..)) && args(request, ..)")
    public void detectTransferFraud(JoinPoint joinPoint, TransferRequest request) {
        if (request.getAmount() != null && request.getAmount().compareTo(fraudThreshold) > 0) {
            log.warn("FRAUD ALERT: High-value transfer detected from {} to {} for amount {}", 
                request.getSourceAccountNumber(), request.getDestinationAccountNumber(), request.getAmount());
            
            auditService.logAction("FRAUD_ALERT", "detectTransferFraud", 
                String.format("Transfer: %s from %s", request.getAmount(), request.getSourceAccountNumber()),
                "ALERT_LOGGED", "WARN", null);
        }
    }

    @Before("execution(* com.vaultcore.service.StockService.executeTrade(..)) && args(userId, request)")
    public void detectStockFraud(JoinPoint joinPoint, java.util.UUID userId, StockTradeRequest request) {
        // High-level monitoring: We intercept and log the trade for user tracking and fraud prevention.
        log.info("Fraud Check: Intercepting stock trade for user {} and symbol {}", userId, request.getSymbol());
    }
}
