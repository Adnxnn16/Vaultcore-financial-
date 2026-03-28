package com.vaultcore.security;

import com.vaultcore.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect for automatic audit logging.
 * Wraps all controller methods to capture:
 * - method name
 * - parameters
 * - result
 * - userId (from security context)
 * - timestamp
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("execution(* com.vaultcore.controller..*(..))")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String params = Arrays.toString(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            auditService.logAction(
                    extractAction(methodName),
                    methodName,
                    params,
                    result != null ? result.toString() : "void",
                    "SUCCESS",
                    null
            );
            return result;
        } catch (Exception ex) {
            auditService.logAction(
                    extractAction(methodName),
                    methodName,
                    params,
                    null,
                    "FAILURE",
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private String extractAction(String methodName) {
        if (methodName.contains("transfer")) return "TRANSFER";
        if (methodName.contains("getBalance")) return "BALANCE_CHECK";
        if (methodName.contains("getAccount")) return "ACCOUNT_VIEW";
        if (methodName.contains("getPortfolio")) return "PORTFOLIO_VIEW";
        if (methodName.contains("getStatement")) return "STATEMENT_DOWNLOAD";
        if (methodName.contains("getAudit")) return "AUDIT_VIEW";
        return "API_CALL";
    }
}
