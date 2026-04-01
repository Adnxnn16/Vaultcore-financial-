package com.vaultcore.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultcore.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private AuditAspect auditAspect;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.vaultcore.service.DummyService");
        when(signature.getName()).thenReturn("doSomething");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1"});
    }

    @Test
    void testAuditMethod_Success() throws Throwable {
        // Arrange
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"arg1\"]");
        when(joinPoint.proceed()).thenReturn("RESULT");
        when(objectMapper.writeValueAsString("RESULT")).thenReturn("\"RESULT\"");

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertEquals("RESULT", result);
        verify(auditService, times(1)).logAction(
                eq("SERVICE_EXEC"),
                eq("com.vaultcore.service.DummyService.doSomething"),
                eq("[\"arg1\"]"),
                eq("\"RESULT\""),
                eq("SUCCESS"),
                isNull()
        );
    }

    @Test
    void testAuditMethod_ExceptionThrown() throws Throwable {
        // Arrange
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"arg1\"]");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> {
            auditAspect.auditMethod(joinPoint);
        });

        assertEquals("Test Exception", ex.getMessage());

        verify(auditService, times(1)).logAction(
                eq("SERVICE_EXEC"),
                eq("com.vaultcore.service.DummyService.doSomething"),
                eq("[\"arg1\"]"),
                isNull(),
                eq("FAILED"),
                eq("Test Exception")
        );
    }
    @Test
    void testAuditMethod_JsonProcessingExceptionInArgs() throws Throwable {
        // Arrange
        when(objectMapper.writeValueAsString(any(Object[].class))).thenThrow(new RuntimeException("JSON Parse Error"));
        when(joinPoint.proceed()).thenReturn("RESULT");
        when(objectMapper.writeValueAsString("RESULT")).thenReturn("\"RESULT\"");

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertEquals("RESULT", result);
        verify(auditService, times(1)).logAction(
                eq("SERVICE_EXEC"),
                eq("com.vaultcore.service.DummyService.doSomething"),
                eq("[arg1]"), // fallback
                eq("\"RESULT\""),
                eq("SUCCESS"),
                isNull()
        );
    }

    @Test
    void testAuditMethod_JsonProcessingExceptionInResult() throws Throwable {
        // Arrange
        when(objectMapper.writeValueAsString(any(Object[].class))).thenReturn("[\"arg1\"]");
        Object weirdResult = new Object() {
            @Override
            public String toString() {
                return "WeirdResultStr";
            }
        };
        when(joinPoint.proceed()).thenReturn(weirdResult);
        when(objectMapper.writeValueAsString(weirdResult)).thenThrow(new RuntimeException("JSON Parse Error Result"));

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertEquals(weirdResult, result);
        verify(auditService, times(1)).logAction(
                eq("SERVICE_EXEC"),
                eq("com.vaultcore.service.DummyService.doSomething"),
                eq("[\"arg1\"]"),
                eq("WeirdResultStr"), // fallback
                eq("SUCCESS"),
                isNull()
        );
    }
}
