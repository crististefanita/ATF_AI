package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.assertion.CodeQualityAssertionDensity;
import com.ai.skills.codequality.tests.support.assertion.CodeQualityAssertionDensityPolicy;
import com.ai.skills.codequality.tests.support.assertion.CodeQualityAssertionDensityReceiptWriter;
import com.ai.skills.codequality.tests.support.assertion.CodeQualityAssertionDensityScanner;
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
public class AtfCodeQualityAssertionDensityContractTest {

    @Test
    public void testClassesKeepExecutableAssertionCues() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        CodeQualityAssertionDensityPolicy densityPolicy = policy.assertionDensityPolicy();
        List<CodeQualityAssertionDensity> densities =
                CodeQualityAssertionDensityScanner.testClassDensities(policy.sourceRoots());
        long lowDensityClasses = densities.stream()
                .filter(density -> density.isBelow(densityPolicy))
                .count();
        long zeroCueClasses = densities.stream()
                .filter(CodeQualityAssertionDensity::isZeroCue)
                .count();

        CodeQualityAssertionDensityReceiptWriter.publish(
                densityPolicy, densities, lowDensityClasses, zeroCueClasses);

        Assert.assertTrue(lowDensityClasses <= densityPolicy.getMaxLowDensityClasses(),
                "Low assertion-density test classes drifted upward. Current=" + lowDensityClasses
                        + ", max=" + densityPolicy.getMaxLowDensityClasses());
        Assert.assertTrue(zeroCueClasses <= densityPolicy.getMaxZeroCueClasses(),
                "Zero assertion-cue test classes drifted upward. Current=" + zeroCueClasses
                        + ", max=" + densityPolicy.getMaxZeroCueClasses());
    }
}
