package com.endava.ai.atf.core.reporting.aggregate.allure;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractReportLabels;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.endava.ai.atf.core.reporting.aggregate.AtfCoreAggregateReportListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_ALLURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_ALLURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.ALLURE_ONLY)
public class AtfCoreAggregateAllureContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_ALLURE_CONTRACT_TEST_AGGREGATE_ALLURE_RESULT_EXPOSES_CONTRACT_DIAGNOSTICS_AS_STEPS)
    public void aggregate_allure_result_exposes_contract_diagnostics_as_steps() throws Exception {
        TestResult result = aggregateResult(Status.PASSED, "", "");

        assertStep(result, AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT, Status.PASSED,
                AtfContractReportLabels.EXPECTED_INVARIANT, AtfContractReportLabels.REPORT_HINT);
        assertStep(result, AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT, Status.PASSED,
                "TestNG status", AtfContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        assertStep(result, AtfContractReportLabels.COLD_FRAMEWORK_VERDICT, Status.PASSED,
                AtfContractReportLabels.PASS_FAIL, AtfContractReportLabels.REPAIR_HINT_IF_THIS_FAILS);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_ALLURE_CONTRACT_TEST_AGGREGATE_ALLURE_RESULT_KEEPS_FAILED_COLD_VERDICT_VISIBLE)
    public void aggregate_allure_result_keeps_failed_cold_verdict_visible() throws Exception {
        TestResult result = aggregateResult(
                Status.FAILED,
                "simulated failure",
                "java.lang.AssertionError: simulated failure"
        );

        Assert.assertEquals(result.getStatus(), Status.FAILED);
        Assert.assertNotNull(result.getStatusDetails());
        assertStep(result, AtfContractReportLabels.COLD_FRAMEWORK_VERDICT, Status.FAILED,
                AtfContractReportLabels.PASS_FAIL,
                AtfContractReportLabels.WHY,
                AtfContractReportLabels.REPAIR_HINT_IF_THIS_FAILS);
    }

    private static TestResult aggregateResult(Status status, String message, String trace) throws Exception {
        Object aggregateCase = new AtfCoreAggregateCase(
                "ATF Core Package Sweep",
                AtfSourcePaths.ATF_ROOT_PACKAGE,
                AtfSourcePaths.ATF_ROOT_PACKAGE + ".reporting.SampleContractTest",
                "sample_contract",
                "[case-a]",
                "REPORTING_CONTRACT",
                "Readable aggregate reporting",
                "Validate aggregate Allure diagnostics.",
                "REPORTING_ADAPTER",
                "Aggregate listener",
                "Expected contract text",
                "Observed validator text",
                "Repair hint text",
                status,
                100L,
                125L,
                message,
                trace
        );

        Method allureResult = AtfCoreAggregateReportListener.class
                .getDeclaredMethod("allureResult", AtfCoreAggregateCase.class);
        allureResult.setAccessible(true);
        return (TestResult) allureResult.invoke(null, aggregateCase);
    }

    private static void assertStep(
            TestResult result,
            String name,
            Status expectedStatus,
            String... parameterNames
    ) {
        StepResult step = result.getSteps().stream()
                .filter(candidate -> name.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Allure step: " + name));

        Assert.assertEquals(step.getStatus(), expectedStatus);

        List<String> actualParameterNames = step.getParameters().stream()
                .map(parameter -> parameter.getName())
                .collect(Collectors.toList());
        for (String parameterName : parameterNames) {
            Assert.assertTrue(
                    actualParameterNames.contains(parameterName),
                    "Step '" + name + "' must expose parameter: " + parameterName
            );
        }
    }
}
