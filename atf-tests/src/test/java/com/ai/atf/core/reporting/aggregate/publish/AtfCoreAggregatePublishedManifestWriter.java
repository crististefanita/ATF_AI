package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateRunCommands;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AtfCoreAggregatePublishedManifestWriter {
    private AtfCoreAggregatePublishedManifestWriter() {
    }

    static void write(
            Path validationDir,
            Path runtimeRoot,
            String engineName,
            AggregateExtentReportWriter.SummaryRecord summary,
            AggregateTimingSummary timing
    ) {
        Path manifest = validationDir.resolve(AtfCoreAggregateArtifacts.PUBLISHED_MANIFEST);
        try {
            Files.write(manifest, lines(validationDir, runtimeRoot, engineName, summary, timing));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to publish aggregate manifest to configured validation directory: " + manifest,
                    e
            );
        }
    }

    private static List<String> lines(
            Path validationDir,
            Path runtimeRoot,
            String engineName,
            AggregateExtentReportWriter.SummaryRecord summary,
            AggregateTimingSummary timing
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("# ATF Core Aggregate Report");
        lines.add("");
        lines.add("This folder is the configured aggregate reporting artifact for the `"
                + AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE + "` TestNG sweep.");
        lines.add("Adapter-local reports may exist elsewhere, but they are support artifacts, not the final audit surface.");
        lines.add("Each aggregate test node includes TestNG status plus available `@AtfContract` and `@AtfContractSuite` metadata.");
        lines.add("");
        lines.add("| Field | Value |");
        lines.add("| --- | --- |");
        lines.add("| Engine | `" + engineName.toLowerCase(Locale.ROOT) + "` |");
        lines.add("| Total test cases | `" + summary.total() + "` |");
        lines.add("| Passed | `" + summary.passed() + "` |");
        lines.add("| Failed | `" + summary.failed() + "` |");
        lines.add("| Skipped | `" + summary.skipped() + "` |");
        lines.add("| Wall-clock duration | `" + timing.wallClockDuration() + "` |");
        lines.add("| Cumulative test duration | `" + timing.cumulativeDuration() + "` |");
        lines.add("| Slowest test | `" + timing.slowestTest() + "` |");
        lines.add("| Slowest class/group | `" + timing.slowestClass() + "` |");
        lines.add("| Extent validation artifact | `" + AtfCoreAggregateArtifacts.PUBLISHED_EXTENT_REPORT + "` |");
        lines.add("| Runtime aggregate artifact | `" + runtimeArtifactPath(runtimeRoot) + "` |");
        lines.add("| Standard Extent shell | `Test Report` document title, `Automation Framework` report name |");
        lines.add("| Standard dashboard evidence | runtime dashboard row, timeline, tag audit, navigation guard, FontAwesome bug tag menu |");
        lines.add("");
        lines.add("Run the Extent aggregate artifact with:");
        lines.add("");
        lines.add("```bash");
        lines.add(extentRunCommand(validationDir, runtimeRoot));
        lines.add("```");
        lines.add("");
        return lines;
    }

    private static String runtimeArtifactPath(Path runtimeRoot) {
        Path root = runtimeRoot == null ? AtfCoreAggregateArtifacts.defaultExtentRuntimeRoot() : runtimeRoot;
        return workspaceRelative(AtfCoreAggregateArtifacts.extentReport(root));
    }

    private static String extentRunCommand(Path validationDir, Path runtimeRoot) {
        String suite = System.getProperty(
                AtfCoreAggregateConfig.SUREFIRE_SUITE_XML_FILES_KEY,
                AtfCoreAggregateRunCommands.CORE_SUITE_XML);
        Path root = runtimeRoot == null ? AtfCoreAggregateArtifacts.defaultExtentRuntimeRoot() : runtimeRoot;
        return AtfCoreAggregateRunCommands.mavenQuietTest(
                AtfCoreAggregateRunCommands.resetSurefireTestFilterProperty(),
                AtfCoreAggregateRunCommands.property(
                        AtfCoreAggregateConfig.SUREFIRE_SUITE_XML_FILES_KEY,
                        workspaceRelative(Path.of(suite))),
                AtfCoreAggregateRunCommands.property(
                        AtfCoreAggregateConfig.FORK_COUNT_KEY,
                        AtfCoreAggregateRunCommands.FORK_COUNT_ZERO),
                AtfCoreAggregateRunCommands.property(
                        ReportingConfigKeys.REPORTING_ENGINE,
                        ReportingConfigKeys.ENGINE_EXTENT),
                AtfCoreAggregateRunCommands.property(
                        ReportingConfigKeys.REPORTS_DIR,
                        ReportingConfigKeys.DEFAULT_REPORTS_DIR),
                AtfCoreAggregateRunCommands.property(
                        ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED,
                        true),
                AtfCoreAggregateRunCommands.property(UiRuntimeOptions.UI_HEADLESS_KEY, true),
                AtfCoreAggregateRunCommands.property(AtfCoreAggregateConfig.ENABLED_KEY, true),
                AtfCoreAggregateRunCommands.property(
                        AtfCoreAggregateConfig.ENGINE_KEY,
                        ReportingConfigKeys.ENGINE_EXTENT),
                AtfCoreAggregateRunCommands.property(AtfCoreAggregateConfig.ROOT_KEY, workspaceRelative(root)),
                AtfCoreAggregateRunCommands.property(AtfCoreAggregateConfig.PUBLISH_ENABLED_KEY, true),
                AtfCoreAggregateRunCommands.property(
                        AtfCoreAggregateConfig.PUBLISH_DIR_KEY,
                        workspaceRelative(validationDir)),
                AtfCoreAggregateRunCommands.property(AtfCoreAggregateConfig.CLEAN_KEY, false)
        );
    }

    private static String workspaceRelative(Path path) {
        Path artifact = path.toAbsolutePath().normalize();
        Path workspace = Path.of("").toAbsolutePath().normalize();
        if (artifact.startsWith(workspace)) {
            return AtfSourcePaths.unixPath(workspace.relativize(artifact));
        }
        return AtfSourcePaths.unixPath(artifact);
    }
}
