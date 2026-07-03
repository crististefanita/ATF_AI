package com.ai.core.support.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Points a class or method contract narrative to a YAML row.
 *
 * <p>Use this when expected/observed/repair text is long, shared, or needs
 * agent-facing calibration outside Java source. Do not combine it with
 * {@link AtfContract} on the same class or method; the ref is the contract
 * narrative source for that target.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AtfContractRef {
    /** Stable id from {@code atf-core/contracts/narratives/contract-narratives.yml}. */
    String value();
}
