package com.endava.ai.core.support.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Points a contract suite narrative to a YAML row.
 *
 * <p>Use this when suite capability, purpose, and report hint should be managed
 * declaratively. Do not combine it with {@link AtfContractSuite} on the same
 * class; the ref is the suite narrative source for that target.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtfContractSuiteRef {
    /** Stable suite id from {@code atf-core/contracts/narratives/contract-narratives.yml}. */
    String value();
}
