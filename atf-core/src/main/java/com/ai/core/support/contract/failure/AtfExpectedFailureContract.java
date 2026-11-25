package com.ai.core.support.contract.failure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents an intentionally failing scenario that must remain controlled.
 *
 * <p>Use this for negative framework contracts where the failure is the proof,
 * not a broken test run.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AtfExpectedFailureContract {
    /** Why this controlled failure is part of the contract. */
    String reason();

    /** Observable signal that proves the expected failure was captured. */
    String expectedSignal();
}
