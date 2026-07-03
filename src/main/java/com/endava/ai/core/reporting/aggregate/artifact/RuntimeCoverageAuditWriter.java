package com.endava.ai.core.reporting.aggregate.artifact;

import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.ReportLogger;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;

import java.util.List;

public final class RuntimeCoverageAuditWriter {
    private RuntimeCoverageAuditWriter() {
    }

    public static void write(
            AggregateExtentReportWriter.SummaryRecord summary,
            List<? extends AggregateExtentReportWriter.CaseRecord> cases
    ) {
        write(AggregateReportDescriptor.defaults(), summary, cases);
    }

    public static void write(
            AggregateReportDescriptor descriptor,
            AggregateExtentReportWriter.SummaryRecord summary,
            List<? extends AggregateExtentReportWriter.CaseRecord> cases
    ) {
        ReportLogger logger = ExtentAdapter.getInstance();
        logger.startTest(
                descriptor.testTitle() + " Coverage Audit",
                "Inventory of every test case observed during " + descriptor.scopeLabel() + "."
        );

        logger.startStep("Expected aggregate coverage contract");
        logger.logDetail("One runtime artifact should expose the full aggregate inventory.");
        logger.logDetail("Some framework tests intentionally isolate reporting adapters, so they may not appear as normal runtime test nodes.");
        logger.logDetail("This audit node lists the complete test-case inventory inside the same artifact.");
        logger.pass("Expected Extent coverage contract recorded");

        logger.startStep("Observed test-case inventory");
        logger.logDetail("Total test cases: " + summary.total());
        logger.logDetail("Passed: " + summary.passed());
        logger.logDetail("Failed: " + summary.failed());
        logger.logDetail("Skipped: " + summary.skipped());
        logger.logCodeBlock(AggregateInventoryLines.fromCases(cases));
        logger.pass("Observed invocation inventory recorded");

        logger.startStep("Cold Extent coverage verdict");
        if (summary.failed() == 0 && summary.skipped() == 0) {
            logger.pass("All test cases are green and listed in this artifact.");
            logger.endTest(TestStatus.PASS);
        } else if (summary.failed() > 0) {
            logger.fail(
                    "The aggregate sweep contains failed test cases.",
                    "Failed test cases: " + summary.failed()
            );
            logger.endTest(TestStatus.FAIL);
        } else {
            logger.logDetail("Skipped test cases: " + summary.skipped());
            logger.endTest(TestStatus.SKIP);
        }
        logger.flush();
    }
}
