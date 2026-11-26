package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.artifact.AggregateManifestInspector;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.atf.core.reporting.aggregate.AtfCoreAggregateExtentFixtures.aggregateCase;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateManifestContractAssertions.assertSlowestFieldIsPublishable;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateManifestContractAssertions.assertTimingFieldIsPublishable;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.AGGREGATE_PUBLISHED_ARTIFACT_AUDIT)
public class AtfCoreAggregatePublishedManifestContractTest {
    @Test
    public void aggregate_published_manifest_uses_explicit_publish_directory_when_enabled() throws Exception {
        Path runtimeRoot = Files.createTempDirectory("atf-core-runtime-manifest-");
        Path publishedRoot = Files.createTempDirectory("atf-core-published-manifest-")
                .resolve("framework-under-test");
        Path runtimeReport = AtfCoreAggregateArtifacts.extentReport(runtimeRoot);
        Files.createDirectories(runtimeReport.getParent());
        Files.writeString(runtimeReport, "<html><body>aggregate runtime</body></html>", StandardCharsets.UTF_8);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(AtfCoreAggregatePublishedArtifactSync.PUBLISH_ENABLED_KEY, "true")
                .setSystemProperty(AtfCoreAggregatePublishedArtifactSync.PUBLISH_DIR_KEY, publishedRoot.toString())) {
            AggregateSummary summary = new AggregateSummary(1, 1, 0, 0);
            AtfCoreAggregatePublishedArtifactSync.publishExtent(
                    runtimeRoot,
                    runtimeReport,
                    "EXTENT",
                    summary,
                    java.util.List.of((AggregateExtentReportWriter.CaseRecord) aggregateCase(
                            "published_manifest_contract",
                            10L))
            );
        }

        Path publishedManifest = publishedRoot.resolve(AtfCoreAggregateArtifacts.PUBLISHED_MANIFEST);
        Path publishedReport = publishedRoot.resolve(AtfCoreAggregateArtifacts.PUBLISHED_EXTENT_REPORT);

        Assert.assertTrue(Files.isRegularFile(publishedReport),
                "Aggregate publication must copy the runtime Extent report only to the explicit publish directory.");
        Assert.assertTrue(Files.isRegularFile(publishedManifest),
                "Aggregate publication must write its manifest only to the explicit publish directory.");
        Assert.assertEquals(Files.readAllBytes(publishedReport), Files.readAllBytes(runtimeReport),
                "Published report bytes must match the observed runtime artifact.");

        String manifest = Files.readString(publishedManifest);
        AggregateManifestInspector fields = AggregateManifestInspector.inspect(manifest);
        Assert.assertEquals(fields.field("Engine"), "extent");
        Assert.assertEquals(fields.field("Total test cases"), "1");
        Assert.assertEquals(fields.field("Passed"), "1");
        Assert.assertEquals(fields.field("Failed"), "0");
        Assert.assertEquals(fields.field("Skipped"), "0");
        Assert.assertEquals(fields.field("Extent validation artifact"), AtfCoreAggregateArtifacts.PUBLISHED_EXTENT_REPORT);
        assertTimingFieldIsPublishable(manifest, "published", "Wall-clock duration");
        assertTimingFieldIsPublishable(manifest, "published", "Cumulative test duration");
        assertSlowestFieldIsPublishable(manifest, "published", "Slowest test");
        assertSlowestFieldIsPublishable(manifest, "published", "Slowest class/group");
        Assert.assertTrue(manifest.contains(com.ai.atf.core.reporting.aggregate.AtfCoreAggregateRunCommands.resetSurefireTestFilterProperty()),
                "Published Extent commands must reset stale class/method filters before the complete aggregate sweep.");
    }
}
