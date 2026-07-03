package com.ai.skills.mcp.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the MCP contract validated by a suite or individual scenario.
 *
 * <p>Class-level values describe the suite family; method-level values narrow a
 * specific positive or negative scenario to one contract id.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ValidatesMcpContract {
    String value();
}
