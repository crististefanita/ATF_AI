package com.endava.ai.skills.mcp.contract;

import com.endava.ai.skills.mcp.McpViolationSeverity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Declares the executable contract guarded by a concrete {@code McpValidationRule}.
 *
 * <p>The annotation is intentionally small: the rule owns the check logic, while
 * {@link McpRuleCatalog} enriches this cold metadata with profile-specific why
 * and repair text for reports and diagnostics.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface McpContract {
    /** Stable contract id used in matrices, reports, and negative tests. */
    String id();

    /** Functional area protected by the rule. */
    McpRuleCategory category();

    /** Default severity emitted when the contract is violated. */
    McpViolationSeverity severity() default McpViolationSeverity.BLOCKER;

    /** Requirement or prompt page that explains the expectation. */
    String htmlReq();

    /** Primary package artifact family inspected by this rule. */
    String artifact();
}
