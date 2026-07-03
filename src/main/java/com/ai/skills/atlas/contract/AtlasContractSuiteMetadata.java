package com.ai.skills.atlas.contract;

import com.ai.skills.atlas.contract.yaml.AtlasYamlSuiteCatalog;
import com.ai.skills.atlas.contract.yaml.AtlasYamlSuiteSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasContractSuiteMetadata {
    private final String suiteId;
    private final AtlasContractProfile profile;
    private final String htmlOwner;
    private final String artifact;
    private final String sourceRoot;
    private final String purpose;

    public static AtlasContractSuiteMetadata require(Class<?> suiteClass) {
        AtlasContractSuiteMetadata metadata = from(suiteClass);
        if (metadata == null) {
            throw new IllegalStateException("Atlas contract tests must declare @AtlasContractSuite or "
                    + "@AtlasContractSuiteRef: " + suiteClass.getName());
        }
        return metadata;
    }

    public static AtlasContractSuiteMetadata from(Class<?> suiteClass) {
        AtlasContractSuite suite = suiteClass.getAnnotation(AtlasContractSuite.class);
        AtlasContractSuiteRef suiteRef = suiteClass.getAnnotation(AtlasContractSuiteRef.class);
        if (suite != null && suiteRef != null) {
            throw new IllegalStateException("Atlas contract suite must not declare both @AtlasContractSuite and "
                    + "@AtlasContractSuiteRef: " + suiteClass.getName());
        }
        if (suite != null) {
            return new AtlasContractSuiteMetadata(
                    suiteClass.getName(),
                    suite.profile(),
                    suite.htmlOwner(),
                    suite.artifact(),
                    suite.sourceRoot(),
                    suite.purpose());
        }
        if (suiteRef == null) {
            return null;
        }
        AtlasYamlSuiteSpec spec = AtlasYamlSuiteCatalog.require(suiteRef.value());
        return new AtlasContractSuiteMetadata(
                spec.id(),
                spec.profile(),
                spec.htmlOwner(),
                spec.artifact(),
                spec.sourceRoot(),
                spec.purpose());
    }
}
