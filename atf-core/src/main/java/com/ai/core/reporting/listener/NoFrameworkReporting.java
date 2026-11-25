package com.ai.core.reporting.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opts a test class out of the framework-managed reporting lifecycle.
 *
 * <p>Use sparingly for self-auditing renderer or adapter tests that must inspect
 * raw report output without TestListener adding its own contract steps.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoFrameworkReporting {
}
