package com.endava.ai.atf.core.reporting.aggregate.parallel;

import com.endava.ai.core.reporting.aggregate.parallel.AggregateCollectionResult;
import com.endava.ai.core.reporting.aggregate.parallel.AggregateCollectionSpec;
import com.endava.ai.core.reporting.aggregate.parallel.AggregateCollectionTestCase;
import com.endava.ai.core.reporting.aggregate.parallel.AggregateParallelCollectionRunner;
import com.endava.ai.core.reporting.aggregate.parallel.AggregateParallelExtentPublisher;
import com.endava.ai.core.reporting.aggregate.parallel.AggregateProcessCollectionExecutor;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.AGGREGATE_PUBLISHED_ARTIFACT_AUDIT)
public class AggregateParallelFullSweepContractTest {

    @Test
    public void full_atf_core_package_sweep_runs_as_isolated_parallel_collections() throws Exception {
        AggregateParallelContractFixtures.deleteDirectory(AggregateParallelContractFixtures.FULL_ROOT);
        Files.createDirectories(AggregateParallelContractFixtures.FULL_ROOT);

        List<AggregateCollectionSpec> specs = AggregateParallelContractFixtures.fullPackageSpecs();
        int maxWorkers = Integer.getInteger("atf.core.aggregate.parallel.maxWorkers", Math.min(10, specs.size()));
        long started = System.currentTimeMillis();
        List<AggregateCollectionResult> results = new AggregateParallelCollectionRunner(
                new AggregateProcessCollectionExecutor(Path.of(""))
        ).runAll(specs, maxWorkers);
        long elapsed = System.currentTimeMillis() - started;

        AggregateParallelExtentPublisher.publish(AggregateParallelContractFixtures.FULL_COLLECTION_REPORT, results);
        List<AggregateCollectionTestCase> testCases = AggregateParallelContractFixtures.methodCases(results);
        AggregateParallelExtentPublisher.publishMethodCases(
                AggregateParallelContractFixtures.FULL_METHOD_REPORT, testCases);

        Assert.assertTrue(elapsed > 0L, "Parallel aggregate full run must record wall-clock time.");
        Assert.assertFalse(testCases.isEmpty(), "Parallel aggregate full run must parse TestNG method evidence.");
        Assert.assertTrue(testCases.size() >= AggregateParallelContractFixtures.FULL_METHOD_EVIDENCE_FLOOR,
                "Parallel aggregate full run must not become fast by losing method evidence. Expected at least "
                        + AggregateParallelContractFixtures.FULL_METHOD_EVIDENCE_FLOOR
                        + " test cases, got " + testCases.size());
        Assert.assertTrue(Files.isRegularFile(AggregateParallelContractFixtures.FULL_COLLECTION_REPORT),
                "Missing full aggregate collection Extent closeout report.");
        Assert.assertTrue(Files.isRegularFile(AggregateParallelContractFixtures.FULL_METHOD_REPORT),
                "Missing full aggregate method Extent closeout report.");
        for (AggregateCollectionResult result : results) {
            assertCollectionEvidence(result);
            Assert.assertEquals(result.exitCode(), 0,
                    "Collection failed: " + result.spec().id() + " -> " + result.stackTrace());
        }
    }

    private static void assertCollectionEvidence(AggregateCollectionResult result) {
        Assert.assertTrue(Files.isRegularFile(result.spec().outputRoot().resolve("command.out.log")),
                "Missing aggregate command log for " + result.spec().id());
        Assert.assertTrue(Files.isRegularFile(result.spec().testNgResultsXml()),
                "Missing isolated TestNG XML for " + result.spec().id());
        Assert.assertTrue(Files.isRegularFile(result.spec().extentReport()),
                "Missing isolated Extent report for " + result.spec().id());
    }
}
