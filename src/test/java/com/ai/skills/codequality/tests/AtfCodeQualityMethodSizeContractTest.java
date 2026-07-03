package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.method.CodeQualityMethodSize;
import com.ai.skills.codequality.tests.support.method.CodeQualityMethodSizePolicy;
import com.ai.skills.codequality.tests.support.method.CodeQualityMethodSizeReceiptWriter;
import com.ai.skills.codequality.tests.support.method.CodeQualityMethodSizeScanner;
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
public class AtfCodeQualityMethodSizeContractTest {

    @Test
    public void javaMethodsStayUnderMethodSizeBudget() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        CodeQualityMethodSizePolicy methodSize = policy.methodSizePolicy();
        List<CodeQualityMethodSize> methods = CodeQualityMethodSizeScanner.methodSizes(policy.sourceRoots());
        long warningMethods = methods.stream()
                .filter(method -> method.getLines() >= methodSize.getWarningLines())
                .count();
        long overMaxMethods = methods.stream()
                .filter(method -> method.getLines() > methodSize.getMaxLines())
                .count();

        CodeQualityMethodSizeReceiptWriter.publish(methodSize, methods, warningMethods, overMaxMethods);

        Assert.assertTrue(warningMethods <= methodSize.getMaxWarningMethods(),
                "Warning-level method count drifted upward. Current=" + warningMethods
                        + ", max=" + methodSize.getMaxWarningMethods());
        Assert.assertTrue(overMaxMethods <= methodSize.getMaxOverMaxMethods(),
                "Over-max method count drifted upward. Current=" + overMaxMethods
                        + ", max=" + methodSize.getMaxOverMaxMethods());
    }
}
