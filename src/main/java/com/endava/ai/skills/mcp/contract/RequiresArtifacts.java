package com.endava.ai.skills.mcp.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lists the package artifacts a suite or scenario depends on.
 *
 * <p>Use stable artifact families such as {@code runs/run_##/...} when the
 * exact run number is intentionally variable.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresArtifacts {
    String[] value();
}
