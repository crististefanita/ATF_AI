package com.endava.ai.atf.core.reporting.adapters.support;

import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import com.endava.ai.core.reporting.adapters.extent.synthetic.SyntheticRuntimeInvocation;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExtentSparkStatusDropdownFixtures {
    private ExtentSparkStatusDropdownFixtures() {
    }

    public static Path passFailSkipStatusDropdownReport(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        String className = "com.endava.ai.atf.core.reporting.StatusDropdownFixture";
        String passMarker = "status_dropdown_pass_contract";
        String failMarker = "status_dropdown_fail_contract";
        String marker = "status_dropdown_skip_contract";
        long start = System.currentTimeMillis();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(className, passMarker, "Status dropdown pass fixture")
                .step("TEST BODY")
                .pass("pass fixture completed")
                .end(TestStatus.PASS)
                .test(className, failMarker, "Status dropdown fail fixture")
                .step("TEST BODY")
                .fail("controlled dropdown failure", "synthetic failure for status dropdown")
                .end(TestStatus.FAIL)
                .test(className, marker, "Status dropdown skip fixture")
                .step("TEST BODY")
                .detail("controlled skipped invocation for dropdown")
                .end(TestStatus.SKIP)
                .flushFindAndPublishRuntimeAudit(
                        marker,
                        List.of(
                                SyntheticRuntimeInvocation.passed(className, passMarker, start),
                                SyntheticRuntimeInvocation.failed(
                                        className,
                                        failMarker,
                                        start + 1,
                                        "synthetic failure for status dropdown"
                                ),
                                SyntheticRuntimeInvocation.skipped(className, marker, start + 2)
                        )
                )
                .report();
        assertDropdownFixture(report, passMarker, failMarker, marker);
        return report;
    }

    private static void assertDropdownFixture(Path report,
                                              String passMarker,
                                              String failMarker,
                                              String skipMarker) throws IOException {
        String html = Files.readString(report);
        Assert.assertTrue(html.contains(passMarker), "Dropdown fixture must include a passed test.");
        Assert.assertTrue(html.contains(failMarker), "Dropdown fixture must include a failed test.");
        Assert.assertTrue(html.contains(skipMarker), "Dropdown fixture must include a skipped test.");
        Assert.assertTrue(html.contains("status=\"skip\"") || html.contains("status=\"skipped\""),
                "Dropdown fixture must expose a skipped top-level test item.");
        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(html);
    }
}
