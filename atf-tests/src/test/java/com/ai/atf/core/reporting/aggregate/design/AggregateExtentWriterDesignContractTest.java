package com.ai.atf.core.reporting.aggregate.design;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateExtentFixtures;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.AggregateInventoryLines;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeAuditSnapshot;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeSummary;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class AggregateExtentWriterDesignContractTest {
    private static final Path RUNTIME_AUDIT_REPORT_FIXTURE = AtfSourcePaths.TARGET_ROOT.resolve("report.html");

    @Test
    public void aggregate_report_descriptor_keeps_writer_generic_and_configurable() {
        AggregateReportDescriptor descriptor = AggregateReportDescriptor.builder()
                .testTitle("Custom Aggregate")
                .description("Custom aggregate report for a non-ATF-core suite.")
                .documentTitle("Custom Document")
                .reportName("Custom Automation")
                .scopeLabel("custom suite")
                .fragmentationReason("custom suite is split by profile")
                .auditShape("custom audit model")
                .addRunCommand("mvn -q test")
                .build();

        Assert.assertEquals(descriptor.testTitle(), "Custom Aggregate");
        Assert.assertEquals(descriptor.documentTitle(), "Custom Document");
        Assert.assertEquals(descriptor.reportName(), "Custom Automation");
        Assert.assertEquals(descriptor.scopeLabel(), "custom suite");
        Assert.assertEquals(descriptor.runCommands(), java.util.List.of("mvn -q test"));

        AggregateReportDescriptor defaults = AggregateReportDescriptor.defaults();
        String defaultText = String.join(" ",
                defaults.testTitle(),
                defaults.description(),
                defaults.scopeLabel(),
                defaults.fragmentationReason(),
                defaults.auditShape());
        Assert.assertFalse(defaultText.contains("ATF Core"),
                "Reusable aggregate descriptor defaults must not be ATF-core branded.");
        Assert.assertFalse(defaultText.contains(AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE),
                "Reusable aggregate descriptor defaults must not depend on contract-test package names.");
    }

    @Test
    public void aggregate_extent_writer_overview_is_descriptor_driven_not_hardcoded_to_atf_core() throws Exception {
        String publicWriter = Files.readString(AtfSourcePaths.mainJava(AggregateExtentReportWriter.class));
        String overviewWriter = Files.readString(AtfSourcePaths.mainJava("com.ai.core.reporting.aggregate.AggregateExtentOverviewWriter"));

        Assert.assertTrue(overviewWriter.contains("descriptor.testTitle()")
                        && overviewWriter.contains("descriptor.description()")
                        && overviewWriter.contains("descriptor.scopeLabel()")
                        && overviewWriter.contains("descriptor.fragmentationReason()")
                        && overviewWriter.contains("descriptor.auditShape()"),
                "Aggregate overview writer must be driven by AggregateReportDescriptor.");
        Assert.assertTrue(publicWriter.contains("OVERVIEW_WRITER.write(extent, engine, descriptor, summary)"),
                "Aggregate public writer must delegate overview details to the focused overview writer.");
        Assert.assertFalse(publicWriter.contains("ATF Core") || overviewWriter.contains("ATF Core"),
                "Aggregate writer must stay generic; ATF-core labels belong in the caller descriptor.");
        Assert.assertFalse(publicWriter.contains(AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE)
                        || overviewWriter.contains(AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE),
                "Aggregate writer must not depend on ATF contract-test package names.");
    }

    @Test
    public void aggregate_extent_writer_stays_orchestrator_over_focused_node_writers() throws Exception {
        String publicWriter = Files.readString(AtfSourcePaths.mainJava(AggregateExtentReportWriter.class));
        String caseNodeWriter = Files.readString(AtfSourcePaths.mainJava("com.ai.core.reporting.aggregate.AggregateExtentCaseNodeWriter"));
        String timingSummary = Files.readString(AtfSourcePaths.mainJava("com.ai.core.reporting.aggregate.model.AggregateTimingSummary"));
        String publicationPipeline = Files.readString(AtfSourcePaths.mainJava("com.ai.core.reporting.aggregate.AggregateExtentPublicationPipeline"));
        String summaryTimingReconciler = Files.readString(AtfSourcePaths.mainJava("com.ai.core.reporting.aggregate.AggregateExtentSummaryNodeTimingReconciler"));

        Assert.assertTrue(publicWriter.contains("AggregateExtentOverviewWriter")
                        && publicWriter.contains("AggregateExtentCaseNodeWriter"),
                "Aggregate public writer must compose focused node writers.");
        Assert.assertFalse(publicWriter.contains("Execution metadata")
                        || publicWriter.contains("Contract proof")
                        || publicWriter.contains("Status evidence"),
                "Aggregate public writer must not own case-section rendering details.");
        Assert.assertTrue(caseNodeWriter.contains("enum CaseSection")
                        && caseNodeWriter.contains("EXECUTION_METADATA")
                        && caseNodeWriter.contains("CONTRACT_PROOF")
                        && caseNodeWriter.contains("STATUS_EVIDENCE"),
                "Aggregate case sections must remain a typed local model, not scattered string literals.");
        Assert.assertTrue(caseNodeWriter.contains("AggregateCaseTiming.from(testCase)")
                        && timingSummary.contains("AggregateCaseTiming.from(testCase)"),
                "Aggregate node timing and final timing summaries must share one normalized case timing model.");
        Assert.assertFalse(caseNodeWriter.contains("class TimingWindow"),
                "Do not duplicate aggregate timing normalization inside the case node writer.");
        Assert.assertTrue(publicationPipeline.contains("AggregateExtentSummaryNodeTimingReconciler.reconcile"),
                "Aggregate publication pipeline must delegate Spark summary-node duration patching.");
        Assert.assertFalse(publicationPipeline.contains("00:00:00:000")
                        || publicationPipeline.contains("String.format"),
                "Aggregate publication pipeline must not own Spark placeholder tokens or duration formatting.");
        Assert.assertTrue(summaryTimingReconciler.contains("ZERO_DURATION_WITH_HOURS")
                        && summaryTimingReconciler.contains("MINIMUM_VISIBLE_DURATION_MILLIS"),
                "Spark summary-node placeholder handling must stay isolated behind named constants.");
    }

    @Test
    public void aggregate_runtime_value_objects_reject_negative_counts_and_missing_hashes() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new AggregateSummary(-1, 0, 0, 0));
        Assert.assertThrows(IllegalArgumentException.class, () -> new AggregateRuntimeSummary(1, -1, 0, 0));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new AggregateRuntimeAuditSnapshot(1, 0, -1, 0, java.util.List.of("case")));
        Assert.assertThrows(NullPointerException.class,
                () -> new com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord(
                        RUNTIME_AUDIT_REPORT_FIXTURE, Instant.now(), null, "abc"));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord(
                        RUNTIME_AUDIT_REPORT_FIXTURE, Instant.now(), "abc", " "));
    }

    @Test
    public void aggregate_inventory_lines_keep_parameterized_test_case_suffixes() throws Exception {
        AggregateExtentReportWriter.CaseRecord first =
                (AggregateExtentReportWriter.CaseRecord) AtfCoreAggregateExtentFixtures.aggregateCase(
                        "parameterized_contract",
                        10L,
                        "[case-a]"
                );
        AggregateExtentReportWriter.CaseRecord second =
                (AggregateExtentReportWriter.CaseRecord) AtfCoreAggregateExtentFixtures.aggregateCase(
                        "parameterized_contract",
                        20L,
                        "[case-b]"
                );

        String lines = AggregateInventoryLines.fromCases(java.util.List.of(first, second));

        Assert.assertTrue(
                lines.contains("PASSED | " + AtfCoreAggregateExtentFixtures.aggregateFixture("AggregateStandardShellFixture")
                        + "#parameterized_contract[case-a](1)"),
                "First parameterized aggregate test case must keep the Surefire-style suffix."
        );
        Assert.assertTrue(
                lines.contains("PASSED | " + AtfCoreAggregateExtentFixtures.aggregateFixture("AggregateStandardShellFixture")
                        + "#parameterized_contract[case-b](2)"),
                "Second parameterized aggregate test case must increment the Surefire-style suffix."
        );
    }
}
