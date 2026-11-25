package com.ai.atf.core.reporting.aggregate.parallel;

import com.ai.core.reporting.aggregate.parallel.AggregateCollectionResult;
import com.ai.core.reporting.aggregate.parallel.AggregateCollectionSpec;
import com.ai.core.reporting.aggregate.parallel.AggregateParallelCollectionRunner;
import com.ai.core.reporting.aggregate.parallel.AggregateProcessCollectionExecutor;
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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.AGGREGATE_PUBLISHED_ARTIFACT_AUDIT)
public class AggregateParallelCollectionContractTest {
    @Test
    public void parallel_collection_runner_starts_all_collections_before_waiting_for_completion() {
        List<AggregateCollectionSpec> specs = AggregateParallelContractFixtures.specs("parallel-start");
        CountDownLatch allStarted = new CountDownLatch(specs.size());
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();

        List<AggregateCollectionResult> results = new AggregateParallelCollectionRunner(spec -> {
            long started = System.currentTimeMillis();
            int current = active.incrementAndGet();
            maxActive.updateAndGet(previous -> Math.max(previous, current));
            allStarted.countDown();
            Assert.assertTrue(allStarted.await(2, TimeUnit.SECONDS),
                    "All aggregate collections must be submitted before any collection is allowed to finish.");
            active.decrementAndGet();
            return AggregateCollectionResult.pass(
                    spec,
                    started,
                    System.currentTimeMillis(),
                    List.of(spec.outputRoot().resolve("testng-results.xml").toString())
            );
        }).runAll(specs);

        Assert.assertEquals(results.size(), specs.size());
        Assert.assertTrue(maxActive.get() > 1,
                "Aggregate collection orchestration must use concurrent workers, not sequential command execution.");
    }

    @Test(timeOut = 15_000)
    public void parallel_collection_runner_can_throttle_heavy_collections_without_serializing_everything() {
        List<AggregateCollectionSpec> specs = List.of(
                AggregateParallelContractFixtures.spec("parallel-throttle", "core-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "business-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "reporting-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "ui-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "api-sanity")
        );
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        CountDownLatch firstPairStarted = new CountDownLatch(2);

        List<AggregateCollectionResult> results = new AggregateParallelCollectionRunner(spec -> {
            long started = System.currentTimeMillis();
            int current = active.incrementAndGet();
            maxActive.updateAndGet(previous -> Math.max(previous, current));
            try {
                firstPairStarted.countDown();
                Assert.assertTrue(firstPairStarted.await(5, TimeUnit.SECONDS),
                        "Two configured workers must enter before either finishes; scheduler speed is not the oracle.");
            } finally {
                active.decrementAndGet();
            }
            return AggregateCollectionResult.pass(spec, started, System.currentTimeMillis(), List.of());
        }).runAll(specs, 2);

        Assert.assertEquals(results.size(), specs.size());
        Assert.assertEquals(maxActive.get(), 2,
                "The observed worker overlap must reach and respect the configured ceiling.");
        Assert.assertTrue(results.stream().allMatch(result -> result.exitCode() == 0),
                "A failed worker or rendezvous must not be hidden behind the returned result count.");
        Assert.assertEquals(results.stream().map(result -> result.spec().id()).collect(Collectors.toList()),
                specs.stream().map(AggregateCollectionSpec::id).collect(Collectors.toList()),
                "All requested collections must produce results in their declared order.");
    }

    @Test
    public void parallel_collection_runner_rejects_invalid_worker_count() {
        IllegalArgumentException error = Assert.expectThrows(IllegalArgumentException.class, () ->
                new AggregateParallelCollectionRunner(spec -> AggregateCollectionResult.pass(
                        spec,
                        1L,
                        2L,
                        List.of()
                )).runAll(AggregateParallelContractFixtures.specs("parallel-invalid-workers"), 0));

        Assert.assertTrue(error.getMessage().contains("maxWorkers must be at least 1"));
    }

    @Test
    public void parallel_collection_runner_rejects_shared_output_roots() {
        Path root = AtfSourcePaths.TARGET_ROOT.resolve("parallel-aggregate-duplicate-root");
        IllegalArgumentException error = Assert.expectThrows(IllegalArgumentException.class, () ->
                new AggregateParallelCollectionRunner(spec -> AggregateCollectionResult.pass(
                        spec,
                        1L,
                        2L,
                        List.of()
                )).runAll(List.of(
                        AggregateCollectionSpec.of("first", "First", "mvn test first", root),
                        AggregateCollectionSpec.of("second", "Second", "mvn test second", root)
                )));

        Assert.assertTrue(error.getMessage().contains("output roots must be unique"));
    }

    @Test
    public void standard_parallel_sanity_commands_use_isolated_maven_outputs() {
        for (AggregateCollectionSpec spec : AggregateParallelContractFixtures.standardSanitySpecs()) {
            Assert.assertTrue(spec.usesIsolatedMavenOutputs(),
                    "Parallel aggregate command must write Surefire and Extent artifacts under its outputRoot: "
                            + spec.command());
            Assert.assertFalse(spec.command().contains("target/surefire-reports"),
                    "Parallel aggregate commands must not share Maven's default surefire-reports directory.");
            Assert.assertTrue(spec.command().contains("'-Dsurefire.reportsDirectory="),
                    "Parallel aggregate Maven arguments must be single-quoted so PowerShell preserves dotted keys.");
            Assert.assertTrue(spec.command().contains("'-Datf.build.directory="),
                    "Parallel aggregate commands must isolate target/classes, not only Surefire reports.");
            Assert.assertFalse(spec.command().contains("-Dtest="),
                    "Parallel aggregate suite commands must not pass an empty -Dtest selector.");
            Assert.assertTrue(spec.testNgResultsXml().startsWith(spec.outputRoot()));
            Assert.assertTrue(spec.extentReport().startsWith(spec.outputRoot()));
        }
    }

    @Test
    public void pom_wires_surefire_reports_directory_to_parallel_aggregate_property() throws Exception {
        String pom = Files.readString(AtfSourcePaths.projectPath("atf-tests/pom.xml"), StandardCharsets.UTF_8);

        Assert.assertTrue(pom.contains("<surefire.reportsDirectory>${project.build.directory}/surefire-reports</surefire.reportsDirectory>"),
                "POM must keep the default Surefire report directory behind an overridable property.");
        Assert.assertTrue(pom.contains("<atf.build.directory>${project.basedir}/target</atf.build.directory>"),
                "POM must keep the Maven build directory overridable for parallel aggregate isolation.");
        Assert.assertTrue(pom.contains("<directory>${atf.build.directory}</directory>"),
                "Parallel aggregate commands depend on Maven honoring -Datf.build.directory.");
        Assert.assertTrue(pom.contains("<reportsDirectory>${surefire.reportsDirectory}</reportsDirectory>"),
                "Parallel aggregate commands depend on Surefire honoring -Dsurefire.reportsDirectory.");
    }

    @Test
    public void process_collection_executor_rebuilds_missing_extent_from_green_testng_xml() throws Exception {
        Path root = AtfSourcePaths.TARGET_ROOT.resolve("parallel-aggregate-missing-extent-rebuild");
        AggregateParallelContractFixtures.deleteDirectory(root);
        Path surefire = root.resolve("surefire-reports");
        String xml = "<testng-results total='1' passed='1' failed='0' skipped='0'>"
                + "<suite name='fixture' duration-ms='1'><test name='fixture'>"
                + "<class name='com.ai.atf.core.reporting.aggregate.parallel.Fixture'>"
                + "<test-method name='passes' status='PASS' duration-ms='1'/>"
                + "</class></test></suite></testng-results>";
        String encodedXml = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));
        String command = "$dir = " + psQuote(surefire.toAbsolutePath().normalize().toString())
                + "; New-Item -ItemType Directory -Force -Path $dir | Out-Null"
                + "; [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String("
                + psQuote(encodedXml)
                + ")) | Set-Content -LiteralPath (Join-Path $dir 'testng-results.xml')";

        AggregateCollectionSpec spec = AggregateCollectionSpec.of(
                "missing-extent-rebuild",
                "Missing Extent rebuild",
                command,
                root
        );

        AggregateCollectionResult result = new AggregateProcessCollectionExecutor(Path.of("")).execute(spec);

        Assert.assertEquals(result.exitCode(), 0);
        Assert.assertTrue(Files.isRegularFile(spec.testNgResultsXml()));
        Assert.assertTrue(Files.isRegularFile(spec.extentReport()),
                "A passed slice with TestNG XML must still publish an Extent artifact for aggregate closeout.");
        Assert.assertTrue(Files.readString(spec.extentReport(), StandardCharsets.UTF_8).contains("passes"));
    }

    private static String psQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

}
