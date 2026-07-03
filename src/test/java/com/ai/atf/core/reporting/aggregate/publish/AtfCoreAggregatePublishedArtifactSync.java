package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.artifact.AggregateReportFiles;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AtfCoreAggregatePublishedArtifactSync {
    public static final String PUBLISH_ENABLED_KEY = AtfCoreAggregateConfig.PUBLISH_ENABLED_KEY;
    public static final String PUBLISH_DIR_KEY = AtfCoreAggregateConfig.PUBLISH_DIR_KEY;

    private AtfCoreAggregatePublishedArtifactSync() {
    }

    public static void publishExtent(
            Path runtimeRoot,
            Path runtimeExtentReport,
            String engineName,
            AggregateExtentReportWriter.SummaryRecord summary,
            List<? extends AggregateExtentReportWriter.CaseRecord> cases
    ) {
        if (!publishEnabled()) {
            return;
        }
        if (runtimeExtentReport == null || !Files.isRegularFile(runtimeExtentReport)) {
            return;
        }

        AtfCoreAggregatePublishSettings settings = AtfCoreAggregatePublishSettings.fromSystemProperties();
        Path validationDir = settings.directory();
        Path publishedReport = validationDir.resolve(AtfCoreAggregateArtifacts.PUBLISHED_EXTENT_REPORT);

        AggregateReportFiles.createDirectory(validationDir);
        copyRuntimeReport(runtimeExtentReport, publishedReport);
        AtfCoreAggregatePublishedManifestWriter.write(
                validationDir,
                runtimeRoot,
                engineName,
                summary,
                AggregateTimingSummary.from(cases)
        );
    }

    private static boolean publishEnabled() {
        return AtfCoreAggregatePublishSettings.fromSystemProperties().enabled();
    }

    private static void copyRuntimeReport(Path runtimeExtentReport, Path publishedReport) {
        try {
            Files.copy(
                    runtimeExtentReport,
                    publishedReport,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to publish aggregate Extent report to configured validation directory: " + publishedReport,
                    e
            );
        }
    }
}
