package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.aggregate.allure.AtfCoreAggregateAllureResultFactory;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.AggregateManifestWriter;
import com.ai.core.reporting.aggregate.artifact.AggregateReportFiles;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.model.TestResult;

import java.nio.file.Path;
import java.util.List;

public final class AtfCoreAggregateReportPublisher {
    private AtfCoreAggregateReportPublisher() {
    }

    public static void publish(
            AtfCoreAggregateConfig config,
            AggregateReportDescriptor descriptor,
            AggregateSummary summary,
            List<AtfCoreAggregateCase> cases
    ) {
        AggregateReportFiles.prepareRoot(config.root(), config.clean());

        if (config.engine().extentEnabled()) {
            Path extentReport = AggregateReportFiles.extentReport(config.root());
            writeExtentReport(extentReport, AtfCoreAggregateConfig.engineName(), descriptor, summary, cases);
            AtfCoreAggregatePublishedArtifactSync.publishExtent(
                    config.root(),
                    extentReport,
                    config.engine().name(),
                    summary,
                    cases
            );
        }
        if (config.engine().allureEnabled()) {
            writeAllureResults(AggregateReportFiles.allureResults(config.root()), summary, cases);
        }
        writeManifest(config, descriptor, summary, cases);
    }

    public static void writeExtentReport(
            Path report,
            String engineName,
            AggregateReportDescriptor descriptor,
            AggregateSummary summary,
            List<AtfCoreAggregateCase> cases
    ) {
        AggregateReportFiles.createParent(report);
        new AggregateExtentReportWriter()
                .write(report, engineName, descriptor, summary, cases);
    }

    private static void writeAllureResults(
            Path resultsDir,
            AggregateSummary summary,
            List<AtfCoreAggregateCase> cases
    ) {
        AggregateReportFiles.createDirectory(resultsDir);
        FileSystemResultsWriter writer = new FileSystemResultsWriter(resultsDir);

        writer.write(summaryResult(summary));
        for (AtfCoreAggregateCase testCase : cases) {
            writer.write(allureResult(testCase));
        }
    }

    private static void writeManifest(
            AtfCoreAggregateConfig config,
            AggregateReportDescriptor descriptor,
            AggregateSummary summary,
            List<AtfCoreAggregateCase> cases
    ) {
        AggregateManifestWriter.write(
                config.root(),
                config.engine().name(),
                config.engine().extentEnabled(),
                config.engine().allureEnabled(),
                descriptor,
                summary,
                cases
        );
    }

    private static TestResult summaryResult(AggregateSummary summary) {
        return AtfCoreAggregateAllureResultFactory.summaryResult(summary);
    }

    private static TestResult allureResult(AtfCoreAggregateCase testCase) {
        return AtfCoreAggregateAllureResultFactory.result(testCase);
    }
}
