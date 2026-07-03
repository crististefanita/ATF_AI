package com.endava.ai.atf.core.reporting.adapters.visual;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport;
import com.endava.ai.atf.core.reporting.adapters.support.ExtentSparkStatusDropdownFixtures;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_STATUS_DROPDOWN)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_STATUS_DROPDOWN_CONTRACT)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkStatusDropdownVisualReportTest {

    public static final Path VISUAL_REPORT_DIR = Path.of("target", "status-dropdown-visual-report")
            .toAbsolutePath()
            .normalize();
    public static final Path VISUAL_REPORT = VISUAL_REPORT_DIR.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME);

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_STATUS_DROPDOWN_CONTRACT)
    @Test
    public void generate_viewable_pass_fail_skip_extent_report_with_skip_dropdown_contract() throws Exception {
        ExtentAdapterContractSupport.deleteRecursively(VISUAL_REPORT_DIR);
        Files.createDirectories(VISUAL_REPORT_DIR);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, VISUAL_REPORT_DIR.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ExtentAdapterContractSupport.resetExtentRuntime();
            Path generated = ExtentSparkStatusDropdownFixtures.passFailSkipStatusDropdownReport(VISUAL_REPORT_DIR);
            if (!generated.equals(VISUAL_REPORT)) {
                Files.move(generated, VISUAL_REPORT, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            ExtentAdapterContractSupport.resetExtentRuntime();
        }
        deleteNonCanonicalHtmlReports();

        String html = Files.readString(VISUAL_REPORT);
        Assert.assertTrue(html.contains("status_dropdown_pass_contract"),
                "Viewable report must contain the pass fixture.");
        Assert.assertTrue(html.contains("status_dropdown_fail_contract"),
                "Viewable report must contain the fail fixture.");
        Assert.assertTrue(html.contains("status_dropdown_skip_contract"),
                "Viewable report must contain the skip fixture.");
        Assert.assertTrue(
                html.contains("status=\"skip\"") || html.contains("status=\"skipped\""),
                "Viewable report must expose a skipped status item."
        );
        Assert.assertTrue(
                html.contains("skip-bg") && html.contains(">Skip</span>"),
                "Viewable report must expose the skipped status with the agreed visible Skip label."
        );
        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(html);
        Assert.assertEquals(htmlReportCount(), 1,
                "Visual generator must leave exactly one Extent HTML report in the output directory.");
    }

    private static void deleteNonCanonicalHtmlReports() throws Exception {
        try (Stream<Path> files = Files.list(VISUAL_REPORT_DIR)) {
            files.filter(path -> path.getFileName().toString().endsWith(".html"))
                    .filter(path -> !path.equals(VISUAL_REPORT))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception exception) {
                            throw new IllegalStateException("Could not delete extra Extent report " + path, exception);
                        }
                    });
        }
    }

    private static long htmlReportCount() throws Exception {
        try (Stream<Path> files = Files.list(VISUAL_REPORT_DIR)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .count();
        }
    }
}
