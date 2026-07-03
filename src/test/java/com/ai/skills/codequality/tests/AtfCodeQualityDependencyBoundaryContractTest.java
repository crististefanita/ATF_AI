package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.CodeQualityReceiptWriter;
import com.ai.skills.codequality.tests.support.dependency.CodeQualityDependencyBoundary;
import com.ai.skills.codequality.tests.support.dependency.CodeQualityDependencyScanner;
import com.ai.skills.codequality.tests.support.dependency.CodeQualityImportViolation;
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
public class AtfCodeQualityDependencyBoundaryContractTest {

    @Test
    public void declaredDependencyBoundariesDoNotImportForbiddenPackages() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        List<CodeQualityDependencyBoundary> boundaries = policy.dependencyBoundaries();
        List<CodeQualityImportViolation> violations =
                CodeQualityDependencyScanner.forbiddenImportViolations(boundaries);

        CodeQualityReceiptWriter.publishDependencyBoundaries(boundaries, violations);

        Assert.assertTrue(violations.isEmpty(),
                "Code Quality dependency boundaries must not import forbidden packages: " + violations);
    }
}
