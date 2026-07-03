package com.endava.ai.core.support.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the framework invariant proved by an ATF contract test.
 *
 * <p>Class-level contracts describe the suite default. Method-level contracts
 * override the class metadata for a specific scenario. The framework listener
 * reads this annotation and publishes expected, observed, verdict, and repair
 * details into the active report.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AtfContract {
    /** Framework area guarded by the test. */
    AtfContractArea area();

    /** Human owner or capability name shown in reports. */
    String owner();

    /** Stable invariant that must hold when the contract passes. */
    String expected();

    /** Concrete evidence produced or inspected by the test. */
    String observed();

    /** First repair action when the contract fails. */
    String repairHint();
}
