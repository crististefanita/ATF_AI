package com.ai.skills.atlas.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Points to Atlas suite metadata in {@code skills/atlas/contracts/suites.yml}.
 *
 * <p>Use this for stable Atlas test-suite narratives so Java test classes keep
 * only the executable assertions. Prefer ids from {@link AtlasSuiteIds} instead
 * of inline string literals so suite references stay rename-safe.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtlasContractSuiteRef {
    String value();
}
