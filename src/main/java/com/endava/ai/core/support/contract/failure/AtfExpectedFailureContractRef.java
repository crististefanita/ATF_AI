package com.endava.ai.core.support.contract.failure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Points to an expected-failure narrative in {@code atf-core/contracts/narratives/contract-narratives.yml}.
 *
 * <p>Use this when the controlled failure reason and signal are shared,
 * reusable, or too bulky for inline test metadata.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AtfExpectedFailureContractRef {
    String value();
}
