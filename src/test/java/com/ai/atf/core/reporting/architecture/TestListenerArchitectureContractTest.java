package com.ai.atf.core.reporting.architecture;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.TEST_LISTENER_ARCHITECTURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_ARCHITECTURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class TestListenerArchitectureContractTest {

    private static final Path LISTENER = AtfSourcePaths.mainJava(TestListener.class);
    private static final Path CONFIGURATION_HANDLER = coreSource(
            "reporting.listener.internal.lifecycle.ConfigurationFailureHandler");
    private static final Path GROUP_FLOW_HANDLER = coreSource("reporting.listener.internal.lifecycle.TestGroupFlowHandler");
    private static final Path OUTCOME_HANDLER = coreSource("reporting.listener.internal.lifecycle.TestResultOutcomeHandler");

    @Test
    public void testListenerDelegatesConfigurationFailureHandling() throws IOException {
        String listener = Files.readString(LISTENER);
        String handler = Files.readString(CONFIGURATION_HANDLER);

        Assert.assertTrue(listener.contains("ReportingPaths::ensureAllureResultsDirectory"),
                "TestListener must create the Allure results directory before Allure TestNG writes success/failure JSON.");
        Assert.assertTrue(listener.contains("ConfigurationFailureHandler"));
        Assert.assertTrue(listener.contains("configurationFailures.onConfigurationFailure(result);"));
        Assert.assertFalse(listener.contains("Configuration failed: "));
        Assert.assertFalse(listener.contains("CONFIGURATION FAILURE: "));

        Assert.assertTrue(handler.contains("Configuration failed: "));
        Assert.assertTrue(handler.contains("CONFIGURATION FAILURE: "));
        Assert.assertTrue(handler.contains("ExtentRuntimeAuditRegistry.record(result, \"FAILED\")"));
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_ARCHITECTURE_CONTRACT_TEST_TEST_LISTENER_DELEGATES_GROUP_FLOW_HANDLING)
    public void testListenerDelegatesGroupFlowHandling() throws IOException {
        String listener = Files.readString(LISTENER);
        String groupFlow = Files.readString(GROUP_FLOW_HANDLER);
        String outcomes = Files.readString(OUTCOME_HANDLER);
        long listenerLines = Files.readAllLines(LISTENER).size();

        Assert.assertTrue(listenerLines <= 220,
                "TestListener should remain a compact router, actual lines=" + listenerLines);
        Assert.assertTrue(listener.contains("TestGroupFlowHandler"));
        Assert.assertTrue(listener.contains("groupFlow.start("));
        Assert.assertTrue(outcomes.contains("groupFlow.close("),
                "Result outcome handling must close group flow through TestGroupFlowHandler.");
        Assert.assertFalse(listener.contains("new GroupController("),
                "TestListener must not own group controller decisions directly.");

        Assert.assertTrue(groupFlow.contains("new GroupController("));
        Assert.assertTrue(groupFlow.contains("FLOW"));
        Assert.assertTrue(groupFlow.contains("TEARDOWN"));
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_ARCHITECTURE_CONTRACT_TEST_TEST_LISTENER_DELEGATES_RESULT_OUTCOME_HANDLING)
    public void testListenerDelegatesResultOutcomeHandling() throws IOException {
        String listener = Files.readString(LISTENER);
        String outcomes = Files.readString(OUTCOME_HANDLER);

        Assert.assertTrue(listener.contains("TestResultOutcomeHandler"));
        Assert.assertTrue(listener.contains("outcomes.onSuccess(result);"));
        Assert.assertTrue(listener.contains("outcomes.onFailure(result);"));
        Assert.assertTrue(listener.contains("outcomes.onSkipped(result);"));
        Assert.assertFalse(listener.contains("FailureAttachmentRegistry.onTestFailure"),
                "TestListener must not own failure attachment decisions directly.");
        Assert.assertFalse(listener.contains("logger.endTest("),
                "TestListener must not close report statuses directly.");

        Assert.assertTrue(outcomes.contains("FailureAttachmentRegistry.onTestFailure"));
        Assert.assertTrue(outcomes.contains("ExtentRuntimeAuditRegistry.record"));
        Assert.assertTrue(outcomes.contains("logger.endTest(status)"));
    }

    private static Path coreSource(String classNameSuffix) {
        return AtfSourcePaths.mainJava(AtfSourcePaths.CORE_ROOT_PACKAGE + "." + classNameSuffix);
    }
}
