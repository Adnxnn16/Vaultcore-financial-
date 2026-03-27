package com.vaultcore.aspect;

import com.vaultcore.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

@Aspect
@Component("serviceAuditAspect")
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.vaultcore.service.*.*(..)) && !execution(* com.vaultcore.service.AuditService.*(..))")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        String params = "";
        
        try {
            params = objectMapper.writeValueAsString(joinPoint.getArgs());
        } catch (Exception e) {
            params = Arrays.toString(joinPoint.getArgs());
        }

        try {
            Object result = joinPoint.proceed();
            String resultStr = "";
            try {
                resultStr = result != null ? objectMapper.writeValueAsString(result) : "null";
            } catch (Exception e) {
                resultStr = result != null ? result.toString() : "null";
            }
            
            // Log successful execution (we don't block the actual execution by doing this after)
            auditService.logAction("SERVICE_EXEC", methodName, params, resultStr, "SUCCESS", null);
            return result;
        } catch (Throwable e) {
            // Log failed execution
            auditService.logAction("SERVICE_EXEC", methodName, params, null, "FAILED", e.getMessage());
            throw e; // rethrow 
        }
    }
}
