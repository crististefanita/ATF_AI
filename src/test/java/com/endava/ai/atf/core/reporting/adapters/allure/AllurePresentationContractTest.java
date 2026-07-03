package com.endava.ai.atf.core.reporting.adapters.allure;

import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.AllureAdapter;
import com.endava.ai.atf.core.reporting.support.AllureResultFixture;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;

import static com.endava.ai.atf.core.reporting.support.AllureResultFixture.array;
import static com.endava.ai.atf.core.reporting.support.AllureResultFixture.containsParameter;
import static com.endava.ai.atf.core.reporting.support.AllureResultFixture.hasStep;
import static com.endava.ai.atf.core.reporting.support.AllureResultFixture.parametersForStep;
import static com.endava.ai.atf.core.reporting.support.AllureResultFixture.string;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ALLURE_PRESENTATION)
@AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_PRESENTATION_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.ALLURE_ONLY)
public class AllurePresentationContractTest {

    private static final Set<String> UI_DETAIL_KEYS = Set.of(
            "base.url",
            "relativePath",
            "locator",
            "value",
            "text"
    );

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_PRESENTATION_NO_ROOT_PARAMETERS)
    public void allure_step_details_do_not_pollute_test_level_parameters() throws Exception {
        JsonObject result = generateAllureResultWithUiDetails("allureCleanParameters");

        JsonArray rootParameters = array(result, "parameters");
        Assert.assertTrue(
                rootParameters == null || rootParameters.size() == 0,
                "Allure top-level parameters must stay empty; UI details belong inside steps."
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_PRESENTATION_CLEAN_TEST_NAME)
    public void allure_test_name_stays_clean_while_step_keeps_ui_details() throws Exception {
        String testName = "allureReadableTestName";
        JsonObject result = generateAllureResultWithUiDetails(testName);

        Assert.assertEquals(string(result, "name"), testName);
        for (String key : UI_DETAIL_KEYS) {
            Assert.assertFalse(
                    string(result, "name").contains(key),
                    "Allure test name must not be polluted by detail key: " + key
            );
        }

        JsonArray stepParameters = parametersForStep(result, "Fill registration form");
        Assert.assertTrue(
                containsParameter(stepParameters, "locator", "button[data-test='register-submit']"),
                "Allure step parameters must preserve locator evidence under the step."
        );
        Assert.assertTrue(
                containsParameter(stepParameters, "text", "A customer with this email address already exists."),
                "Allure step parameters must preserve text evidence under the step."
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_PRESENTATION_CONTRACT_STEPS)
    public void allure_contract_diagnostics_are_published_as_execution_steps() throws Exception {
        String testName = "allureContractDiagnostics";
        JsonObject result = generateAllureResultWithContractDiagnostics(testName);
        JsonArray steps = array(result, "steps");

        Assert.assertNotNull(steps, "Allure result must contain execution steps");
        Assert.assertTrue(hasStep(steps, "Expected contract"), "Expected contract must be a visible Allure step");
        Assert.assertTrue(hasStep(steps, "Observed validator result"), "Observed validator result must be a visible Allure step");
        Assert.assertTrue(hasStep(steps, "Cold verdict"), "Cold verdict must be a visible Allure step");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_PRESENTATION_BUFFERED_STEPS)
    public void allure_pre_case_steps_are_buffered_then_flushed_as_steps() throws Exception {
        String testName = "allureBufferedPreCaseStep";
        JsonObject result = generateAllureResultWithBufferedPreCaseStep(testName);
        JsonArray steps = array(result, "steps");

        Assert.assertTrue(hasStep(steps, "Buffered pre-case step"), "Buffered step must be flushed as an Allure step");
        Assert.assertTrue(hasStep(steps, "Active step"), "Active step must still be published after buffer flush");
        Assert.assertTrue(
                containsParameter(parametersForStep(result, "Buffered pre-case step"), "phase", "before-current-case"),
                "Buffered step parameters must remain attached to the buffered step."
        );
        Assert.assertTrue(
                array(result, "parameters") == null || array(result, "parameters").size() == 0,
                "Buffered step details must not leak to test-level parameters."
        );
    }

    private JsonObject generateAllureResultWithUiDetails(String testName) throws Exception {
        try (AllureResultFixture allure = AllureResultFixture.open(testName)) {
            String uuid = allure.startTestCase(testName, "Allure presentation contract");

            AllureAdapter adapter = allure.adapter();
            adapter.startStep("Fill registration form");
            adapter.logDetail("base.url=https://practicesoftwaretesting.com");
            adapter.logDetail("relativePath=/auth/register");
            adapter.logDetail("locator=button[data-test='register-submit']");
            adapter.logDetail("value=John");
            adapter.logDetail("text=A customer with this email address already exists.");
            adapter.pass("Registration evidence captured");
            adapter.endTest(TestStatus.PASS);

            allure.writePassedTestCase(uuid);
            return allure.resultJson(testName);
        }
    }

    private JsonObject generateAllureResultWithBufferedPreCaseStep(String testName) throws Exception {
        try (AllureResultFixture allure = AllureResultFixture.open(testName)) {
            AllureAdapter adapter = allure.adapter();
            adapter.startTest(testName, "Allure buffered step contract");
            adapter.startStep("Buffered pre-case step");
            adapter.logDetail("phase=before-current-case");
            adapter.pass("Buffered step recorded");

            String uuid = allure.startTestCase(testName, "Allure buffered step contract");

            adapter.startStep("Active step");
            adapter.logDetail("phase=current-case");
            adapter.pass("Active step recorded");
            adapter.endTest(TestStatus.PASS);

            allure.writePassedTestCase(uuid);
            return allure.resultJson(testName);
        }
    }

    private JsonObject generateAllureResultWithContractDiagnostics(String testName) throws Exception {
        try (AllureResultFixture allure = AllureResultFixture.open(testName)) {
            String uuid = allure.startTestCase(testName, "Allure contract diagnostics");

            AllureAdapter adapter = allure.adapter();
            adapter.startStep("Expected contract");
            adapter.logDetail("Expected contract=diagnostics are visible as execution steps");
            adapter.pass("Expected contract recorded");
            adapter.startStep("Observed validator result");
            adapter.logDetail("Observed validator result=step nodes contain the audit payload");
            adapter.pass("Observed validator result recorded");
            adapter.startStep("Cold verdict");
            adapter.logDetail("Cold verdict=PASS");
            adapter.pass("Cold verdict recorded");
            adapter.endTest(TestStatus.PASS);

            allure.writePassedTestCase(uuid);
            return allure.resultJson(testName);
        }
    }
}
