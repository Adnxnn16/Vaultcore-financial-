package com.vaultcore.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be excluded from the global AuditAspect logging.
 * Commonly used for methods interacting with highly sensitive raw data 
 * (like passwords or MFA tokens) that should never appear in audit trails.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAudit {
}
