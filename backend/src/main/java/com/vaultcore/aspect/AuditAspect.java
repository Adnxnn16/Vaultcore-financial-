package com.vaultcore.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultcore.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * AOP Aspect for automatic audit logging of all service-layer method calls.
 * Captures method name, parameters, result, and execution status.
 * 
 * PRD Week 4: Audit Logging — every method call's parameters and return values
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Around("(execution(* com.vaultcore.service..*(..)) || execution(* com.vaultcore.controller..*(..)))"
            + " && !execution(* com.vaultcore.service.AuditService.*(..))"
            + " && !@annotation(com.vaultcore.aspect.NoAudit)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String declaringType = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = declaringType + "." + methodName;

        // Serialize arguments (with fallback and filtering)
        String params;
        try {
            Object[] args = Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> !(arg instanceof Jwt))
                    .toArray();
            params = objectMapper.writeValueAsString(args);
        } catch (Throwable t) {
            params = Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> !(arg instanceof Jwt))
                    .map(arg -> arg == null ? "null" : arg.toString())
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        params = maskSensitiveData(params);

        log.debug("Audit intercepted: {}", fullMethodName);
        try {
            Object result = joinPoint.proceed();

            // Serialize result (with fallback)
            String resultStr;
            try {
                resultStr = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                resultStr = result != null ? result.toString() : null;
            }
            resultStr = maskSensitiveData(resultStr);

            auditService.logAction(
                    "SERVICE_EXEC",
                    fullMethodName,
                    params,
                    resultStr,
                    "SUCCESS",
                    null
            );
            return result;

        } catch (Exception ex) {
            auditService.logAction(
                    "SERVICE_EXEC",
                    fullMethodName,
                    params,
                    null,
                    "FAILED",
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private String maskSensitiveData(String data) {
        if (data == null) {
            return null;
        }
        // Basic PII/Sensitive data masking for logs
        data = data.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")", "$1***$2");
        data = data.replaceAll("(?i)(\"otp\"\\s*:\\s*\")[^\"]+(\")", "$1***$2");
        data = data.replaceAll("(?i)(\"otpCode\"\\s*:\\s*\")[^\"]+(\")", "$1***$2");
        data = data.replaceAll("(?i)(\"token\"\\s*:\\s*\")[^\"]+(\")", "$1***$2");
        return data;
    }
}
