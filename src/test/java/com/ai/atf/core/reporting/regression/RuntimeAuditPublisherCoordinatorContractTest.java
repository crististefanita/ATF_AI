package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishOutcome;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishResult;
import com.ai.core.reporting.listener.internal.runtime.audit.RuntimeAuditPublisherCoordinator;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditTarget;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.RUNTIME_AUDIT_PUBLISHER_COORDINATOR_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.RUNTIME_AUDIT_PUBLISHER_COORDINATOR_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
public class RuntimeAuditPublisherCoordinatorContractTest {
    private static final String CURRENT_REPORT_FIXTURE_PREFIX = "runtime-audit-current-report-";
    private static final String ATLAS_VALIDATION_REPORT_FILE =
            "framework-under-test_atlas-tests-extent-report.html";

    @Test(groups = AtfTestGroups.EXTENT_ONLY)
    public void coordinatorReturnsTypedSkippedResultWhenNoReportCandidateExists() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-publish-empty-reports");
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter.getInstance().resetForTests();

            RuntimeAuditPublishResult result = RuntimeAuditPublisherCoordinator.publishCurrentReport(List.of(
                    new ExtentRuntimeInvocation(
                            "PASSED",
                            "com.example.PositiveUserTests",
                            "create_user_success",
                            1L,
                            "",
                            List.of("@API")
                    )
            ));

            Assert.assertEquals(result.outcome(), RuntimeAuditPublishOutcome.SKIPPED_NO_CANDIDATE);
            Assert.assertEquals(result.targetKind(), RuntimeAuditTarget.Kind.NO_CANDIDATE);
            Assert.assertFalse(result.armsPublishGuard(),
                    "A skipped publish without a candidate must not arm the publish-once guard.");
            Assert.assertNull(result.report());
            Assert.assertEquals(result.invocationCount(), 1);
            Assert.assertTrue(result.reason().contains("No Extent report candidate exists"));
        }
    }

    @Test(groups = AtfTestGroups.EXTENT_ONLY)
    @AtfContractRef(AtfContractNarrativeIds.Contract.RUNTIME_AUDIT_PUBLISHER_COORDINATOR_CONTRACT_TEST_COORDINATOR_ALLOWS_ACTIVE_PUBLISHED_REPORT_TO_RECEIVE_RUNTIME_AUDIT_SHELL)
    public void coordinatorAllowsActivePublishedReportToReceiveRuntimeAuditShell() throws Exception {
        Path fixtureRoot = newFixtureRoot();
        Path reportsDir = fixtureRoot
                .resolve("active-published-artifacts")
                .resolve("run_01")
                .resolve("validation")
                .resolve("reports");

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_FILE_NAME, ExtentReportFiles.STABLE_REPORT_FILE_NAME)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter adapter = ExtentAdapter.getInstance();
            adapter.resetForTests();
            adapter.startTest(
                    "com.ai.ui.tests.refunds.staff.ExternalBankSettlementLifecycleTest",
                    "bankFinalizationPublishesIdentityStatusAndReconciliation",
                    "Active published runtime audit contract"
            );
            adapter.pass("business UI proof recorded");
            adapter.endTest(TestStatus.PASS);
            adapter.flush();

            RuntimeAuditPublishResult result = RuntimeAuditPublisherCoordinator.publishCurrentReport(List.of(
                    new ExtentRuntimeInvocation(
                            "PASSED",
                            "com.ai.ui.tests.refunds.staff.ExternalBankSettlementLifecycleTest",
                            "bankFinalizationPublishesIdentityStatusAndReconciliation",
                            1L,
                            "",
                            List.of("@UI", "@BUSINESS")
                    )
            ));

            Path report = result.report();
            String html = Files.readString(report);

            Assert.assertEquals(result.outcome(), RuntimeAuditPublishOutcome.WRITTEN);
            Assert.assertEquals(result.targetKind(), RuntimeAuditTarget.Kind.CURRENT_REPORT);
            Assert.assertTrue(html.contains(ExtentRuntimeOverviewTables.attributeEquals(
                            ExtentDataAttributes.REPORT_MODE,
                            ExtentReportModeType.RUNTIME_AUDITED.value()
                    )),
                    "Active published reports must finish as runtime-audited, not component-adapter.");
            ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(html);
            Assert.assertFalse(html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                    "Active published reports must not be capped to the component-adapter Overview fallback.");

            adapter.resetForTests();
        } finally {
            deleteRecursively(fixtureRoot);
        }
    }

    @Test(groups = AtfTestGroups.EXTENT_ONLY)
    @AtfContractRef(AtfContractNarrativeIds.Contract.RUNTIME_AUDIT_PUBLISHER_COORDINATOR_CONTRACT_TEST_COORDINATOR_FLUSHES_ACTIVE_VALIDATION_REPORT_BEFORE_RUNTIME_AUDIT_LOCATE)
    public void coordinatorFlushesActiveValidationReportBeforeRuntimeAuditLocate() throws Exception {
        Path fixtureRoot = newFixtureRoot();
        Path reportsDir = fixtureRoot.resolve("active-validation-artifacts");

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_FILE_NAME, ATLAS_VALIDATION_REPORT_FILE)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter adapter = ExtentAdapter.getInstance();
            adapter.resetForTests();
            adapter.startTest(
                    "com.ai.skills.atlas.tests.candidate.AtlasCandidateSkillReadinessContractTest",
                    "businessTestAtlasDocumentsReopenableEvidence",
                    "Active validation runtime audit contract"
            );
            adapter.pass("atlas validation evidence recorded");
            adapter.endTest(TestStatus.PASS);

            RuntimeAuditPublishResult result = RuntimeAuditPublisherCoordinator.publishCurrentReport(List.of(
                    new ExtentRuntimeInvocation(
                            "PASSED",
                            "com.ai.skills.atlas.tests.candidate.AtlasCandidateSkillReadinessContractTest",
                            "businessTestAtlasDocumentsReopenableEvidence",
                            1L,
                            "",
                            List.of("@ATLAS", "@CONTRACT")
                    )
            ));

            Path report = result.report();
            String html = Files.readString(report);

            Assert.assertEquals(result.outcome(), RuntimeAuditPublishOutcome.WRITTEN);
            Assert.assertEquals(result.targetKind(), RuntimeAuditTarget.Kind.CURRENT_REPORT);
            ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(html);
            Assert.assertFalse(html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                    "Active validation reports must not remain on the component-adapter Overview fallback.");

            adapter.resetForTests();
        } finally {
            deleteRecursively(fixtureRoot);
        }
    }

    private static Path newFixtureRoot() throws Exception {
        Files.createDirectories(AtfSourcePaths.TARGET_ROOT);
        return Files.createTempDirectory(AtfSourcePaths.TARGET_ROOT, CURRENT_REPORT_FIXTURE_PREFIX);
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            throw new IllegalStateException("Could not delete " + path, e);
                        }
                    });
        }
    }
}
