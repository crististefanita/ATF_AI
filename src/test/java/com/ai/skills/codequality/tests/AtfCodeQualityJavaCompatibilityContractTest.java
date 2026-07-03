package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.CodeQualityReceiptWriter;
import com.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityRule;
import com.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityScanner;
import com.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityViolation;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class AtfCodeQualityJavaCompatibilityContractTest {

    @Test
    public void javaSourcesAvoidApisOutsideTheProjectBaseline() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        List<CodeQualityJavaCompatibilityRule> rules = policy.javaCompatibilityRules();
        List<CodeQualityJavaCompatibilityViolation> violations =
                CodeQualityJavaCompatibilityScanner.javaCompatibilityViolations(policy.sourceRoots(), rules);

        CodeQualityReceiptWriter.publishJavaCompatibility(rules, violations);

        Assert.assertTrue(violations.isEmpty(),
                "Java sources must avoid APIs outside the project baseline: " + violations);
    }
}
