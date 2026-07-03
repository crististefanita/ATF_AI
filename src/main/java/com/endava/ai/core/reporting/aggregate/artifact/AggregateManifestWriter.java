package com.endava.ai.core.reporting.aggregate.artifact;

import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import com.endava.ai.core.reporting.internal.ReportingPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AggregateManifestWriter {
    private AggregateManifestWriter() {
    }

    public static void write(
            Path root,
            String engineName,
            boolean extentEnabled,
            boolean allureEnabled,
            AggregateExtentReportWriter.SummaryRecord summary
    ) {
        write(root, engineName, extentEnabled, allureEnabled, AggregateReportDescriptor.defaults(), summary);
    }

    public static void write(
            Path root,
            String engineName,
            boolean extentEnabled,
            boolean allureEnabled,
            AggregateExtentReportWriter.SummaryRecord summary,
            List<? extends AggregateExtentReportWriter.CaseRecord> cases
    ) {
        write(root, engineName, extentEnabled, allureEnabled, AggregateReportDescriptor.defaults(), summary, cases);
    }

    public static void write(
            Path root,
            String engineName,
            boolean extentEnabled,
            boolean allureEnabled,
            AggregateReportDescriptor descriptor,
            AggregateExtentReportWriter.SummaryRecord summary
    ) {
        write(root, engineName, extentEnabled, allureEnabled, descriptor, summary, AggregateTimingSummary.empty());
    }

    public static void write(
            Path root,
            String engineName,
            boolean extentEnabled,
            boolean allureEnabled,
            AggregateReportDescriptor descriptor,
            AggregateExtentReportWriter.SummaryRecord summary,
            List<? extends AggregateExtentReportWriter.CaseRecord> cases
    ) {
        write(root, engineName, extentEnabled, allureEnabled, descriptor, summary, AggregateTimingSummary.from(cases));
    }

    private static void write(
            Path root,
            String engineName,
            boolean extentEnabled,
            boolean allureEnabled,
            AggregateReportDescriptor descriptor,
            AggregateExtentReportWriter.SummaryRecord summary,
            AggregateTimingSummary timing
    ) {
        Path manifest = root.resolve(AggregateReportFiles.MANIFEST_FILE);
        AggregateReportFiles.createParent(manifest);
        Path allureResults = AggregateReportFiles.allureResults(root);

        List<String> lines = new ArrayList<>();
        lines.add("# " + descriptor.testTitle());
        lines.add("");
        lines.add("This folder is the official aggregate reporting artifact for `" + descriptor.scopeLabel() + "`.");
        lines.add("Adapter-local reports may exist elsewhere, but they are support artifacts, not the final audit surface.");
        lines.add(descriptor.auditShape());
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
        if (extentEnabled) {
            lines.add("| Extent final artifact | `" + AggregateReportFiles.extentReportRelativePath() + "` |");
        }
        if (allureEnabled) {
            lines.add("| Allure raw results | `" + AggregateReportFiles.ALLURE_RESULTS_DIR + "/` |");
            lines.add("| Allure HTML render target | `" + AggregateReportFiles.allureHtmlIndexRelativePath()
                    + "` after running `mvn allure:report` with this folder's results path |");
        }
        if (!descriptor.runCommands().isEmpty()) {
            lines.add("");
            lines.add("Run with:");
            lines.add("");
            lines.add("```bash");
            lines.addAll(descriptor.runCommands());
            lines.add("```");
        }
        if (allureEnabled) {
            lines.add("");
            lines.add("Render Allure HTML from the aggregate raw results:");
            lines.add("");
            lines.add("```powershell");
            lines.add("$root=(Resolve-Path '" + commandPath(root) + "').Path");
            lines.add("$results=(Resolve-Path '" + commandPath(allureResults) + "').Path");
            lines.add("$report=(Join-Path $root '" + AggregateReportFiles.ALLURE_HTML_REPORT_DIR + "')");
            lines.add("mvn allure:report \"-D" + ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY
                    + "=$results\" \"-Dallure.report.directory=$report\"");
            lines.add("```");
        }

        try {
            Files.write(manifest, lines);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write aggregate report manifest: " + manifest, e);
        }
    }

    private static String commandPath(Path path) {
        return path.normalize().toString().replace('\\', '/');
    }
}
