package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.AggregateManifestWriter;
import com.ai.core.reporting.aggregate.artifact.AggregateReportFiles;
import com.ai.core.reporting.aggregate.model.AggregateSummary;

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
                descriptor,
                summary,
                cases
        );
    }


}
