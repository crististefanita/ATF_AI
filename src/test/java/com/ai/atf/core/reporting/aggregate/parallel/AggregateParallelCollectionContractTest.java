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
import com.ai.core.support.contract.wait.AtfTestWaiter;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

    @Test
    public void parallel_collection_runner_can_throttle_heavy_collections_without_serializing_everything() {
        List<AggregateCollectionSpec> specs = List.of(
                AggregateParallelContractFixtures.spec("parallel-throttle", "core-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "business-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "reporting-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "mcp-sanity"),
                AggregateParallelContractFixtures.spec("parallel-throttle", "atlas-sanity")
        );
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();

        List<AggregateCollectionResult> results = new AggregateParallelCollectionRunner(spec -> {
            long started = System.currentTimeMillis();
            int current = active.incrementAndGet();
            maxActive.updateAndGet(previous -> Math.max(previous, current));
            try {
                AtfTestWaiter.pause(Duration.ofMillis(75));
            } finally {
                active.decrementAndGet();
            }
            return AggregateCollectionResult.pass(spec, started, System.currentTimeMillis(), List.of());
        }).runAll(specs, 2);

        Assert.assertEquals(results.size(), specs.size());
        Assert.assertTrue(maxActive.get() > 1,
                "Throttled aggregate execution must still run collections concurrently.");
        Assert.assertTrue(maxActive.get() <= 2,
                "Throttled aggregate execution must respect maxWorkers to avoid oversubscribing Maven processes.");
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
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);

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

    @Test
    public void full_parallel_aggregate_slices_cover_atf_api_core_and_ui_packages() throws Exception {
        assertPackageSlice("testng-atf-core-api.xml", "com.ai.atf.api.*");
        assertPackageSlice("testng-atf-core-ui.xml", "com.ai.atf.ui.*");
        assertClassSlice("testng-atf-core-reporting-adapters-status.xml",
                "ExtentSparkStatusDropdownContractTest",
                "ExtentSparkRuntimeTimelineContractTest");
        assertClassSlice("testng-atf-core-reporting-adapters-tags.xml",
                "ExtentSparkTagNavigationContractTest",
                "ExtentSparkRuntimeTagTablesContractTest",
                "ExtentAdapterTest");
        assertClassSlice("testng-atf-core-reporting-adapters-shell.xml",
                "ExtentSparkShellStyleContractTest",
                "ExtentSparkFailureTriageContractTest",
                "ExtentSparkTreeCollapseContractTest");
        assertClassSlice("testng-atf-core-reporting-runtime-audit.xml",
                "ExtentSparkRuntimeAuditContractTest");
        assertClassSlice("testng-atf-core-reporting-visual-zoom.xml",
                "UIZoomRuntimeReportingContractTest");
        assertClassSlice("testng-atf-core-reporting-visual-browser.xml",
                "UiEngineBrowserWindowContractSmokeTest",
                "UiEngineConfiguredRuntimeParityContractTest");
        assertClassSlice("testng-atf-core-reporting-visual-screenshot.xml",
                "UiEngineScreenshotSmokeTest");
        assertClassSlice("testng-atf-core-reporting-visual-rest.xml",
                "UIWindowSizeResolverContractTest",
                "FinalUiScreenshotContractTest");
        assertClassSlice("testng-atf-core-reporting-rest.xml",
                "CoreReportingMatrixContractTest",
                "ExtentFailurePlacementContractTest",
                "RuntimeAuditPublisherCoordinatorContractTest");
        assertPackageSlice("testng-atf-core-support-skills.xml", "com.ai.atf.core.support.*");
        assertPackageSlice("testng-atf-core-support-skills.xml", "com.ai.atf.core.skills.*");
    }

    @Test
    public void full_parallel_aggregate_specs_start_heavy_core_slices_first() {
        List<String> ids = AggregateParallelContractFixtures.fullPackageSpecs().stream()
                .map(AggregateCollectionSpec::id)
                .collect(Collectors.toList());

        Assert.assertEquals(ids.subList(0, 4), List.of(
                        "core-adapters-status",
                        "core-adapters-tags",
                        "core-adapters-shell",
                        "core-runtime-audit"),
                "Full aggregate must submit the slow Core reporting slices first so maxWorkers=4 can reduce wall time.");
        Assert.assertFalse(ids.contains("core-package"),
                "Final aggregate must not run the legacy unsplit core-package slice beside split Core slices.");
    }

    @Test
    public void full_parallel_aggregate_reuses_compiled_workspace_and_isolates_only_reports() {
        for (AggregateCollectionSpec spec : AggregateParallelContractFixtures.fullPackageSpecs()) {
            Assert.assertTrue(spec.command().contains("mvn -q surefire:test"),
                    "Full aggregate must avoid recompiling the workspace inside every slice: " + spec.id());
            Assert.assertFalse(spec.command().contains("-Datf.build.directory="),
                    "Full aggregate speed depends on reusing the parent Maven compilation: " + spec.id());
            Assert.assertTrue(spec.command().contains("'-Dsurefire.reportsDirectory="),
                    "Full aggregate must still isolate Surefire evidence per slice: " + spec.id());
            Assert.assertTrue(spec.command().contains("'-Dreports.dir="),
                    "Full aggregate must still isolate Extent reports per slice: " + spec.id());
        }
    }

    @Test
    public void full_parallel_aggregate_has_a_dedicated_testng_suite() throws Exception {
        String xml = Files.readString(
                Path.of("src/test/resources/atf/core/suites/aggregate/testng-atf-core-aggregate-parallel-full.xml"),
                StandardCharsets.UTF_8);

        Assert.assertTrue(xml.contains("AggregateParallelFullSweepContractTest"));
        Assert.assertFalse(xml.contains("AggregateParallelCollectionContractTest"),
                "Full sweep must stay isolated from fast aggregate collection contracts.");
    }

    private static void assertPackageSlice(String suiteFile, String packageName) throws Exception {
        Path suite = Path.of("src/test/resources/atf/core/parallel").resolve(suiteFile);
        String xml = Files.readString(suite, StandardCharsets.UTF_8);

        Assert.assertTrue(xml.contains("<package name=\"" + packageName + "\"/>"));
        assertCommonSliceContract(xml);
    }

    private static void assertClassSlice(String suiteFile, String... classNameFragments) throws Exception {
        Path suite = Path.of("src/test/resources/atf/core/parallel").resolve(suiteFile);
        String xml = Files.readString(suite, StandardCharsets.UTF_8);

        for (String classNameFragment : classNameFragments) {
            Assert.assertTrue(xml.contains(classNameFragment),
                    suiteFile + " must contain " + classNameFragment);
        }
        assertCommonSliceContract(xml);
    }

    private static void assertCommonSliceContract(String xml) {
        Assert.assertTrue(xml.contains("<exclude name=\"aggregate-published-artifact-audit\"/>"),
                "Full parallel slices must not recursively run the parallel full-run orchestrator.");
        Assert.assertTrue(xml.contains("AtfCoreAggregateReportListener"),
                "Full parallel slices must still run under the aggregate reporting listener.");
    }

    private static String psQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

}
