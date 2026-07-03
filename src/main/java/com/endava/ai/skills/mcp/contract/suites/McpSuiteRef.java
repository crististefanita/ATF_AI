package com.endava.ai.skills.mcp.contract.suites;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * References MCP suite metadata stored in YAML.
 *
 * <p>Use this on TestNG suite classes so profile/category/title/htmlReq/purpose
 * stay centralized in {@code src/main/resources/skills/mcp/contracts/suites/mcp-suites.yml}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface McpSuiteRef {
    /** Stable suite id declared in the MCP YAML suite catalog. */
    String value();
}
