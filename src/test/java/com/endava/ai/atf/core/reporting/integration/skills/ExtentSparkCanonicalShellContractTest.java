package com.endava.ai.atf.core.reporting.integration.skills;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.atf.core.reporting.adapters.assertions.ExtentSparkCanonicalShellAssertions;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_CANONICAL_SHELL)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_CANONICAL_SHELL_CONTRACT)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkCanonicalShellContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void mcpAtlasCoreAndRuntimeReportsShareCanonicalOverviewShell() throws Exception {
        Map<String, Path> componentReports = new LinkedHashMap<>();
        componentReports.put("mcp closeout", componentFamilyReport(
                "mcp-closeout",
                "com.endava.ai.skills.mcp.tests.packageaudit.McpConfiguredPackageAuditTest",
                "validatesConfiguredOrLatestGeneratedPackage",
                TestStatus.PASS
        ));
        componentReports.put("atlas", componentFamilyReport(
                "atlas",
                "com.endava.ai.skills.atlas.tests.BusinessTestAtlasContractTest",
                "mapsBusinessClaimsToExecutableTests",
                TestStatus.PASS
        ));
        componentReports.put("atf core", componentFamilyReport(
                "atf-core",
                "com.endava.ai.atf.core.AtfContractCatalogTest",
                "contractCatalogPublishesReadableInventory",
                TestStatus.PASS
        ));
        componentReports.put("mixed ui api core", componentFamilyReport(
                "mixed-ui-api-core",
                "com.endava.ai.atf.core.reporting.MixedUiApiCoreReportTest",
                "keepsMixedCollectionReportShellStandard",
                TestStatus.PASS
        ));

        for (Map.Entry<String, Path> report : componentReports.entrySet()) {
            ExtentSparkCanonicalShellAssertions.assertComponentAdapterShell(
                    report.getKey(),
                    Files.readString(report.getValue())
            );
        }

        ExtentSparkCanonicalShellAssertions.assertRuntimeAuditedShell(Files.readString(runtimeReport()));
    }

    @Test
    public void mcpLikeComponentFailureReportKeepsStandardFailTab() throws Exception {
        Path report = componentFamilyReport(
                "mcp-closeout-fail",
                "com.endava.ai.skills.mcp.tests.packageaudit.McpConfiguredPackageAuditTest",
                "mcpCloseoutFailureIsVisibleInFailTab",
                TestStatus.FAIL
        );

        ExtentSparkCanonicalShellAssertions.assertComponentAdapterShell(
                "mcp closeout failure",
                Files.readString(report)
        );

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                    + "if (!statusButton) { return 'missing-status-button'; }"
                    + "statusButton.click();"
                    + "const menu = document.querySelector('#status-toggle');"
                    + "const fail = menu && Array.from(menu.querySelectorAll('[status=\"fail\"]')).find(visible);"
                    + "if (!fail) { return 'missing-fail-item'; }"
                    + "fail.click();"
                    + "const main = (document.querySelector('.test-content')?.innerText || '').replace(/\\s+/g, ' ');"
                    + "const side = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                    + ".filter(visible).map(el => (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()).join('|');"
                    + "return ['main=' + main, 'side=' + side].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("mcpCloseoutFailureIsVisibleInFailTab")
                            && state.contains("McpConfiguredPackageAuditTest")
                            && state.contains("Expected failure details for canonical Fail tab"),
                    "Fail tab must expose the failed MCP-like component report, not a blank/raw Spark view. State: " + state
            );
            Assert.assertTrue(
                    state.contains("matching test case(s)"),
                    "Fail tab must reuse the canonical status-selection shell even without a runtime audit ledger. State: " + state
            );
        });
    }

    private Path componentFamilyReport(
            String filePrefix,
            String className,
            String methodName,
            TestStatus status
    ) throws Exception {
        resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_FILE_NAME, filePrefix + "-ExtentReport.html");

        ExtentSyntheticReportBuilder builder = ExtentSyntheticReportBuilder.in(contractReportsDir)
                .test(className, methodName, "Canonical shell contract for " + filePrefix)
                .step("TEST BODY")
                .detail("family=" + filePrefix)
                .detail("class=" + className);
        if (status == TestStatus.FAIL) {
            builder.fail("controlled mcp closeout failure", "Expected failure details for canonical Fail tab");
        } else {
            builder.pass("canonical shell evidence recorded");
        }
        return builder.end(status).flushAndFind(methodName).report();
    }

    private Path runtimeReport() throws Exception {
        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_ENABLED)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_FILE_NAME, "");
        return runtimeAuditedDashboardReport();
    }
}
