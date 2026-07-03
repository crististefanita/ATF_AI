package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.audit.ExtentRuntimeReportLocator;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditTarget;
import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_RUNTIME_REPORT_LOCATOR_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_RUNTIME_REPORT_LOCATOR_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ExtentRuntimeReportLocatorContractTest {

    @Test
    public void locatorReturnsNullWhenNoCurrentOrCandidateReportExists() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-empty-reports");
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter.getInstance().resetForTests();

            Assert.assertNull(ExtentRuntimeReportLocator.currentOrLatestReport(List.of("PositiveUserTests")));
            RuntimeAuditTarget target = ExtentRuntimeReportLocator.locate(List.of("PositiveUserTests"));
            Assert.assertEquals(target.kind(), RuntimeAuditTarget.Kind.NO_CANDIDATE);
            Assert.assertFalse(target.writable());
        }
    }

    @Test
    public void locatorPrefersMarkerBearingCandidateOverUnrelatedNewerReport() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-marker-reports");
        Path matching = timestampedExtentReport(reportsDir, "2000-01-01_00-00-00");
        Path unrelated = timestampedExtentReport(reportsDir, "2999-01-01_00-00-00");
        Files.writeString(matching, "<html>PositiveUserTests</html>", StandardCharsets.UTF_8);
        Files.writeString(unrelated, "<html>unrelated latest report</html>", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(matching, java.nio.file.attribute.FileTime.fromMillis(1_000L));
        Files.setLastModifiedTime(unrelated, java.nio.file.attribute.FileTime.fromMillis(2_000L));

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter.getInstance().resetForTests();

            Assert.assertEquals(
                    ExtentRuntimeReportLocator.currentOrLatestReport(List.of("PositiveUserTests")),
                    matching.toAbsolutePath().normalize()
            );
            RuntimeAuditTarget target = ExtentRuntimeReportLocator.locate(List.of("PositiveUserTests"));
            Assert.assertEquals(target.kind(), RuntimeAuditTarget.Kind.MARKER_MATCH);
            Assert.assertEquals(target.report(), matching.toAbsolutePath().normalize());
        }
    }

    @Test
    public void locatorDoesNotFallbackToLatestWhenNoCandidateMatchesCurrentInvocation() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-stale-reports");
        Path previousRun = timestampedExtentReport(reportsDir, "2000-01-01_00-00-00");
        Path unrelatedLatest = timestampedExtentReport(reportsDir, "2999-01-01_00-00-00");
        Files.writeString(previousRun, "<html>PreviousRunTests</html>", StandardCharsets.UTF_8);
        Files.writeString(unrelatedLatest, "<html>unrelated latest report</html>", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(previousRun, java.nio.file.attribute.FileTime.fromMillis(1_000L));
        Files.setLastModifiedTime(unrelatedLatest, java.nio.file.attribute.FileTime.fromMillis(2_000L));

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter.getInstance().resetForTests();
            NonBlockingDiagnostic.clear();
            NonBlockingDiagnostic.setSinkForTests(warning -> {
                // The warning is asserted below; keep this contract quiet in core-fast console output.
            });

            Assert.assertNull(
                    ExtentRuntimeReportLocator.currentOrLatestReport(List.of("CurrentRunTests")),
                    "Locator must not rewrite the newest report when no candidate proves it belongs to the current run."
            );
            RuntimeAuditTarget target = ExtentRuntimeReportLocator.locate(List.of("CurrentRunTests"));
            Assert.assertEquals(target.kind(), RuntimeAuditTarget.Kind.AMBIGUOUS_CANDIDATES);
            Assert.assertFalse(target.writable());
            Assert.assertTrue(
                    NonBlockingDiagnostic.warnings().stream()
                            .anyMatch(warning -> warning.contains("runtime audit update skipped")),
                    "Ambiguous runtime audit target must be visible through diagnostics."
            );
        } finally {
            NonBlockingDiagnostic.clear();
            NonBlockingDiagnostic.resetSinkForTests();
        }
    }

    private static Path timestampedExtentReport(Path directory, String timestamp) {
        return directory.resolve(ExtentReportFiles.TIMESTAMP_FILE_PREFIX
                + timestamp
                + ExtentReportFiles.REPORT_FILE_EXTENSION);
    }

}
