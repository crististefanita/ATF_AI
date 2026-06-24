package com.endava.ai.mcp.contract;

import com.endava.ai.mcp.McpViolationSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface McpContract {
    String id();

    McpRuleCategory category();

    McpViolationSeverity severity() default McpViolationSeverity.BLOCKER;

    String htmlReq();

    String artifact();
}
