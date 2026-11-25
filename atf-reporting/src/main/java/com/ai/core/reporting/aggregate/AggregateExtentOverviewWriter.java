package com.ai.core.reporting.aggregate;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import java.util.Date;
import java.util.function.Function;

final class AggregateExtentOverviewWriter {
    private static final long OVERVIEW_NODE_DURATION_MILLIS = 1_000L;

    private enum OverviewLine {
        ENGINE("Configured engine", context -> context.engine),
        TOTAL_TEST_CASES("Total test cases", context -> String.valueOf(context.summary.total())),
        PASSED("Passed", context -> String.valueOf(context.summary.passed())),
        FAILED("Failed", context -> String.valueOf(context.summary.failed())),
        SKIPPED("Skipped", context -> String.valueOf(context.summary.skipped())),
        SCOPE("Scope", context -> context.descriptor.scopeLabel()),
        WHY_THIS_EXISTS("Why this exists", context -> context.descriptor.fragmentationReason()),
        AUDIT_SHAPE("Audit shape", context -> context.descriptor.auditShape());

        private final String label;
        private final Function<OverviewContext, String> value;

        OverviewLine(String label, Function<OverviewContext, String> value) {
            this.label = label;
            this.value = value;
        }

        void writeTo(ExtentTest overview, OverviewContext context) {
            overview.info(label + ": " + value.apply(context));
        }
    }

    void write(
            ExtentReports extent,
            String engine,
            AggregateReportDescriptor descriptor,
            AggregateExtentReportWriter.SummaryRecord summary
    ) {
        ExtentTest overview = extent.createTest(descriptor.testTitle(), descriptor.description());
        long startedAt = System.currentTimeMillis();
        overview.getModel().setStartTime(new Date(startedAt));
        overview.getModel().setEndTime(new Date(startedAt + OVERVIEW_NODE_DURATION_MILLIS));
        OverviewContext context = new OverviewContext(engine, descriptor, summary);
        for (OverviewLine line : OverviewLine.values()) {
            line.writeTo(overview, context);
        }
        writeRunCommands(overview, descriptor);
        writeSummaryStatus(overview, summary);
    }

    private static void writeRunCommands(ExtentTest overview, AggregateReportDescriptor descriptor) {
        int index = 1;
        for (String command : descriptor.runCommands()) {
            overview.info("Run command " + index + ": " + command);
            index++;
        }
    }

    private static void writeSummaryStatus(
            ExtentTest overview,
            AggregateExtentReportWriter.SummaryRecord summary
    ) {
        if (summary.failed() == 0 && summary.skipped() == 0) {
            overview.pass("Aggregate suite status is green.");
        } else if (summary.failed() > 0) {
            overview.fail("Aggregate suite contains failures.");
        } else {
            overview.skip("Aggregate suite contains skipped tests.");
        }
    }

    private static final class OverviewContext {
        private final String engine;
        private final AggregateReportDescriptor descriptor;
        private final AggregateExtentReportWriter.SummaryRecord summary;

        private OverviewContext(
                String engine,
                AggregateReportDescriptor descriptor,
                AggregateExtentReportWriter.SummaryRecord summary
        ) {
            this.engine = engine;
            this.descriptor = descriptor;
            this.summary = summary;
        }
    }
}
