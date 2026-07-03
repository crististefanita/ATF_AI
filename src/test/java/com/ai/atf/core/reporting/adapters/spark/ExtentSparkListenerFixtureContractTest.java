package com.ai.atf.core.reporting.adapters.spark;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.aventstack.extentreports.ExtentTest;
import com.ai.core.reporting.TestStatus;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.support.base.BaseTestAPIContractTest;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport;
import com.ai.atf.ui.RegistrationTests;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_LISTENER_FIXTURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_LISTENER_FIXTURE_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkListenerFixtureContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_listener_does_not_repeat_method_name_as_child_node() throws Exception {
        resetExtentRuntime();

        String methodName = "extent_method_hierarchy_contract_" + UUID.randomUUID().toString().replace("-", "_");
        TestListener listener = new TestListener();
        ITestResult result = successResult(methodName);

        listener.onTestStart(result);
        StepLogger.startStep("Business readable step");
        StepLogger.logDetail("Only the business step should be nested under the method node.");
        StepLogger.pass("Business step recorded");

        ExtentTest current = ExtentAdapterContractSupport.currentExtentTest(ExtentAdapter.getInstance());
        Assert.assertTrue(
                current.getModel().getName().contains(methodName),
                "Current Extent node must be the method node: " + methodName
        );
        List<String> childNames = current.getModel().getChildren().stream()
                .map(child -> child.getName())
                .collect(Collectors.toList());
        Assert.assertFalse(
                childNames.stream().anyMatch(name -> name.contains(methodName)),
                "Method node must not contain a duplicate child with the same method name: " + childNames
        );
        Assert.assertTrue(
                childNames.stream().anyMatch(name -> name.contains("TEST BODY")),
                "Method node must own the canonical TEST BODY child: " + childNames
        );

        listener.onTestSuccess(result);
        listener.onFinish((org.testng.ISuite) null);

        String html = Files.readString(extentReportContaining(methodName));
        Assert.assertTrue(
                html.contains(ExtentDataAttributes.REPORT_MODE + "=\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\"")
                        && html.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"false\""),
                "Listener onFinish must leave reports without a runtime ledger component-standardized, not raw Spark."
        );
        Assert.assertEquals(
                countOccurrences(html, ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                1,
                "Listener onFinish must publish one canonical component tag-audit zone when runtime audit is not applicable."
        );
        Assert.assertEquals(
                countOccurrences(html, ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()),
                3,
                "Listener onFinish must publish exactly three component tag tables."
        );
        Assert.assertFalse(
                html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL)
                        || html.contains(ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()),
                "Listener onFinish must not fake runtime tag tables when the runtime ledger intentionally filtered the fixture."
        );
        Assert.assertFalse(
                html.contains("<h6 class=\"card-title\"></h6>")
                        || html.contains("<div class=\"card-header\"><p>Tags</p></div>"),
                "Listener onFinish must not leave raw Spark Overview cards in the final report."
        );

        Assert.assertTrue(
                html.contains("Business readable step"),
                "Business step must stay nested under the single method node."
        );
    }

    @Test
    public void extent_screenshot_title_is_rendered_once_per_attachment() throws Exception {
        String screenshotTitle = "Extent Unique Screenshot Label " + UUID.randomUUID();
        ExtentAdapter adapter = ExtentAdapter.getInstance();

        adapter.startTest("extent-single-screenshot-label-contract", "Screenshot title should not be duplicated");
        adapter.attachScreenshotBase64(ONE_PIXEL_PNG, screenshotTitle);
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        String html = Files.readString(extentReportContaining(screenshotTitle));
        Assert.assertEquals(
                countOccurrences(html, screenshotTitle),
                1,
                "Extent screenshot title must be visible once; the thumbnail title must not duplicate the log detail."
        );
    }

}
