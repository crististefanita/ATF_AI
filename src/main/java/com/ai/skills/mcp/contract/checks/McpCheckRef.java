package com.ai.skills.mcp.contract.checks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * References MCP check metadata stored in YAML.
 *
 * <p>Use this when the executable TestNG method should stay concise and the
 * contract narrative belongs in src/main/resources/skills/mcp/contracts/checks.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface McpCheckRef {
    /** Stable check id declared in the MCP YAML check catalog. */
    String value();
}
