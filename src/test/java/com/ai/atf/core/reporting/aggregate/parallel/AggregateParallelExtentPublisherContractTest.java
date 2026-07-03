package com.ai.atf.core.reporting.aggregate.parallel;

import com.ai.core.reporting.aggregate.parallel.AggregateCollectionResult;
import com.ai.core.reporting.aggregate.parallel.AggregateCollectionSpec;
import com.ai.core.reporting.aggregate.parallel.AggregateCollectionTestCase;
import com.ai.core.reporting.aggregate.parallel.AggregateParallelExtentPublisher;
import com.ai.core.reporting.aggregate.parallel.AggregateTestNgResultParser;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.AGGREGATE_PUBLISHED_ARTIFACT_AUDIT)
public class AggregateParallelExtentPublisherContractTest {
    private static final Path CONTRACT_REPORT =
            AtfSourcePaths.TARGET_ROOT.resolve("atf-core-parallel-aggregate").resolve("ExtentReport.html");
    private static final Path FAILURE_PRESERVATION_REPORT =
            AtfSourcePaths.TARGET_ROOT.resolve("atf-core-parallel-aggregate-failure-contract")
                    .resolve("ExtentReport.html");

    @Test
    public void parallel_collection_extent_report_preserves_every_collection_result() throws Exception {
        Files.createDirectories(FAILURE_PRESERVATION_REPORT.getParent());
        Files.deleteIfExists(FAILURE_PRESERVATION_REPORT);
        List<AggregateCollectionSpec> specs = AggregateParallelContractFixtures.specs("parallel-report");
        long now = System.currentTimeMillis();
        List<AggregateCollectionResult> results = List.of(
                AggregateCollectionResult.pass(specs.get(0), now, now + 10, List.of("target/a/testng-results.xml")),
                AggregateCollectionResult.pass(specs.get(1), now + 20, now + 40, List.of("target/b/testng-results.xml")),
                AggregateCollectionResult.fail(specs.get(2), 1, now + 50, now + 90,
                        List.of("target/c/testng-results.xml"), "simulated package audit failure")
        );

        String html = Files.readString(
                AggregateParallelExtentPublisher.publish(FAILURE_PRESERVATION_REPORT, results),
                StandardCharsets.UTF_8);

        Assert.assertTrue(html.contains("Parallel Aggregate Collection Close-Out"));
        Assert.assertTrue(html.contains("parallel-report-core-sanity"));
        Assert.assertTrue(html.contains("parallel-report-reporting-sanity"));
        Assert.assertTrue(html.contains("parallel-report-mcp-sanity"));
        Assert.assertTrue(html.contains("simulated package audit failure"));
        Assert.assertTrue(html.contains("Tests Passed") && html.contains(">2<"));
        Assert.assertTrue(html.contains("Tests Failed") && html.contains(">1<"));
    }

    @Test
    public void parallel_collection_extent_report_publishes_all_standard_sanity_collections() throws Exception {
        Files.createDirectories(CONTRACT_REPORT.getParent());
        Files.deleteIfExists(CONTRACT_REPORT);
        long now = System.currentTimeMillis();
        List<AggregateCollectionSpec> specs = AggregateParallelContractFixtures.standardSanitySpecs();
        List<AggregateCollectionResult> results = List.of(
                AggregateCollectionResult.pass(specs.get(0), now, now + 10, List.of("target/core/testng-results.xml")),
                AggregateCollectionResult.pass(specs.get(1), now + 20, now + 40, List.of("target/business/testng-results.xml")),
                AggregateCollectionResult.pass(specs.get(2), now + 50, now + 80, List.of("target/reporting/testng-results.xml")),
                AggregateCollectionResult.pass(specs.get(3), now + 90, now + 130, List.of("target/mcp/testng-results.xml")),
                AggregateCollectionResult.pass(specs.get(4), now + 140, now + 190, List.of("target/atlas/testng-results.xml"))
        );

        String html = Files.readString(AggregateParallelExtentPublisher.publish(CONTRACT_REPORT, results),
                StandardCharsets.UTF_8);

        Assert.assertTrue(html.contains("core-sanity"));
        Assert.assertTrue(html.contains("business-understanding-sanity"));
        Assert.assertTrue(html.contains("reporting-sanity"));
        Assert.assertTrue(html.contains("mcp-sanity"));
        Assert.assertTrue(html.contains("atlas-sanity"));
        Assert.assertTrue(html.contains("Tests Passed") && html.contains(">5<"));
        Assert.assertTrue(html.contains("Tests Failed") && html.contains(">0<"));
    }

    @Test
    public void method_level_aggregate_report_is_built_from_testng_results_not_collection_summaries() throws Exception {
        Path root = Files.createTempDirectory("parallel-method-results");
        Path xml = root.resolve("testng-results.xml");
        Files.writeString(xml, syntheticTestNgResults(), StandardCharsets.UTF_8);
        AggregateCollectionSpec spec = AggregateCollectionSpec.of(
                "synthetic-methods", "Synthetic methods", "mvn -q synthetic", root);

        List<AggregateCollectionTestCase> cases = AggregateTestNgResultParser.parse(xml, spec);
        Path report = root.resolve("ExtentReport.html");
        String html = Files.readString(AggregateParallelExtentPublisher.publishMethodCases(report, cases),
                StandardCharsets.UTF_8);

        Assert.assertEquals(cases.size(), 3, "Config methods must not become aggregate test cases.");
        Assert.assertTrue(html.contains("synthetic-methods | firstContract"));
        Assert.assertTrue(html.contains("synthetic-methods | secondContract"));
        Assert.assertTrue(html.contains("synthetic-methods | thirdContract"));
        Assert.assertFalse(html.contains("synthetic-methods | setUp"));
        Assert.assertTrue(html.contains("mvn -q synthetic"),
                "Method-level aggregate report must keep the source command visible in the final report.");
        Assert.assertTrue(html.contains("Passed</th><td>2</td>"));
        Assert.assertTrue(html.contains("Skipped</th><td>1</td>"));
    }

    private static String syntheticTestNgResults() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<testng-results total=\"3\" passed=\"2\" failed=\"0\" skipped=\"1\">"
                + "<suite name=\"Synthetic\"><test name=\"synthetic\"><class name=\"example.ContractTest\">"
                + "<test-method name=\"setUp\" is-config=\"true\" status=\"PASS\" duration-ms=\"1\"/>"
                + "<test-method name=\"firstContract\" status=\"PASS\" duration-ms=\"5\"/>"
                + "<test-method name=\"secondContract\" status=\"PASS\" duration-ms=\"7\"/>"
                + "<test-method name=\"thirdContract\" status=\"SKIP\" duration-ms=\"3\"/>"
                + "</class></test></suite></testng-results>";
    }
}
