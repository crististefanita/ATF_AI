package com.endava.ai.core.support.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Groups an ATF contract test class into a capability profile.
 *
 * <p>Use this beside {@link AtfContract} so the generated report can explain
 * both the suite family and the concrete framework invariant under test.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtfContractSuite {
    /** Execution profile or report boundary for the suite. */
    AtfContractProfile profile();

    /** Dominant framework area covered by the suite. */
    AtfContractArea area();

    /** Short capability name shown in report metadata. */
    String capability();

    /** Why this suite exists. */
    String purpose();

    /** Optional report-specific repair or inspection hint. */
    String reportHint() default "";
}
