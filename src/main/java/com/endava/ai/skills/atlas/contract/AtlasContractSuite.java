package com.endava.ai.skills.atlas.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inline Atlas suite metadata.
 *
 * <p>Prefer {@link AtlasContractSuiteRef} for stable Atlas contract-test
 * suites so purpose/artifact metadata stays in {@code skills/atlas/contracts/suites.yml}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtlasContractSuite {
    AtlasContractProfile profile();

    String htmlOwner() default AtlasContractArtifacts.HTML_OWNER;

    String artifact() default AtlasContractArtifacts.ATLAS_HTML;

    String sourceRoot() default AtlasContractArtifacts.INPUT_SOURCE_ROOT;

    String purpose();
}
