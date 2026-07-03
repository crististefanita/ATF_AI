package com.endava.ai.core.reporting.aggregate;

import com.aventstack.extentreports.ExtentReports;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterFactory;

import java.nio.file.Path;
import java.util.List;

public final class AggregateExtentReportWriter {
    private static final AggregateExtentPublicationPipeline PUBLICATION_PIPELINE =
            new AggregateExtentPublicationPipeline();
    private static final AggregateExtentOverviewWriter OVERVIEW_WRITER =
            new AggregateExtentOverviewWriter();
    private static final AggregateExtentCaseNodeWriter CASE_NODE_WRITER =
            new AggregateExtentCaseNodeWriter();

    public void write(Path report, String engine, SummaryRecord summary, List<? extends CaseRecord> cases) {
        write(report, engine, AggregateReportDescriptor.defaults(), summary, cases);
    }

    public void write(
            Path report,
            String engine,
            AggregateReportDescriptor descriptor,
            SummaryRecord summary,
            List<? extends CaseRecord> cases
    ) {
        ExtentReports extent = new ExtentReports();
        extent.attachReporter(ExtentSparkReporterFactory.darkReporter(
                report,
                descriptor.documentTitle(),
                descriptor.reportName()
        ));

        if (descriptor.includeOverviewTest()) {
            OVERVIEW_WRITER.write(extent, engine, descriptor, summary);
        }
        CASE_NODE_WRITER.write(extent, cases);

        extent.flush();
        PUBLICATION_PIPELINE.publish(report, descriptor, summary, cases);
    }

    public interface SummaryRecord {
        int total();

        long passed();

        long failed();

        long skipped();
    }

    public interface CaseRecord {
        String suiteName();

        String contextName();

        String className();

        String methodName();

        String parameters();

        String suiteProfile();

        String capability();

        String purpose();

        String area();

        String owner();

        String expected();

        String observed();

        String repairHint();

        String statusName();

        AggregateStatus status();

        long startMillis();

        long endMillis();

        String stackTrace();

        default String throwableMessage() {
            return stackTrace().lines()
                    .findFirst()
                    .orElse(statusName() + ": " + displayName());
        }

        default String fullName() {
            return className() + "." + methodName();
        }

        String displayName();

        String description();

        long durationMillis();
    }
}
