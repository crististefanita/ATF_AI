package com.endava.ai.atf.core.reporting.adapters;

import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@NoFrameworkReporting
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_ADAPTER)
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentAdapterDirectoryContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_STABLE_ARTIFACT)
    public void extentAdapterCreatesNestedReportsDirectoryBeforeFlush() throws Exception {
        Path fixtureRoot = Files.createTempDirectory("atf-extent-nested-reports-");
        Path missingNestedReportsDir = fixtureRoot
                .resolve("missing")
                .resolve("nested")
                .resolve("reports");

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, missingNestedReportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapter adapter = ExtentAdapter.getInstance();
            adapter.resetForTests();

            adapter.startTest("Test Suite", "missing nested reports dir");
            adapter.endTest(TestStatus.PASS);
            adapter.flush();

            Path report = adapter.currentReportPath();
            assertEquals(
                    report.getParent().toAbsolutePath().normalize().toString(),
                    missingNestedReportsDir.toAbsolutePath().normalize().toString()
            );
            assertTrue(Files.isRegularFile(report));
        } finally {
            ExtentAdapter.getInstance().resetForTests();
            deleteRecursively(fixtureRoot);
        }
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception exception) {
                            throw new IllegalStateException("Could not delete " + path, exception);
                        }
                    });
        }
    }
}
