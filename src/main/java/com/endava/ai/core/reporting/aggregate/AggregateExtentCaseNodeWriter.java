package com.endava.ai.core.reporting.aggregate;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.endava.ai.core.reporting.FrameworkContractReportLabels;
import com.endava.ai.core.reporting.ReportText;
import com.endava.ai.core.reporting.aggregate.internal.AggregateCaseDisplayNames;
import com.endava.ai.core.reporting.aggregate.model.AggregateCaseTiming;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AggregateExtentCaseNodeWriter {
    private enum CaseSection {
        EXECUTION_METADATA("Execution metadata"),
        CONTRACT_PROOF("Contract proof"),
        STATUS_EVIDENCE("Status evidence");

        private final String title;

        CaseSection(String title) {
            this.title = title;
        }
    }

    void write(ExtentReports extent, List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        Map<String, ExtentTest> classNodes = new LinkedHashMap<>();
        Map<String, AggregateCaseTiming> classTimings = new LinkedHashMap<>();
        AggregateCaseDisplayNames displayNames = AggregateCaseDisplayNames.from(cases);
        for (AggregateExtentReportWriter.CaseRecord testCase : cases) {
            ExtentTest classNode = classNodes.computeIfAbsent(
                    testCase.className(),
                    className -> createClassNode(extent, className)
            );
            AggregateCaseTiming caseTiming = AggregateCaseTiming.from(testCase);
            writeCaseNode(classNode, displayNames.titleFor(testCase), testCase, caseTiming);
            classTimings.merge(testCase.className(), caseTiming, AggregateCaseTiming::include);
        }
        for (Map.Entry<String, ExtentTest> entry : classNodes.entrySet()) {
            AggregateCaseTiming timing = classTimings.get(entry.getKey());
            if (timing != null) {
                applyTiming(entry.getValue(), timing);
            }
        }
    }

    private static ExtentTest createClassNode(ExtentReports extent, String className) {
        ExtentTest node = extent.createTest(ReportText.simpleClassName(className), className);
        node.assignCategory(ReportText.packageName(className));
        node.assignCategory(ReportText.simpleClassName(className));
        return node;
    }

    private void writeCaseNode(
            ExtentTest classNode,
            String title,
            AggregateExtentReportWriter.CaseRecord testCase,
            AggregateCaseTiming caseTiming
    ) {
        ExtentTest node = classNode.createNode(title, testCase.description());
        applyTiming(node, caseTiming);
        node.assignCategory(testCase.status().category());
        node.assignCategory(ReportText.simpleClassName(testCase.className()));
        node.assignCategory(testCase.className());
        writeExecutionMetadata(timedSection(node, CaseSection.EXECUTION_METADATA, caseTiming), testCase, caseTiming);
        writeContractProof(timedSection(node, CaseSection.CONTRACT_PROOF, caseTiming), testCase);
        writeStatus(timedSection(node, CaseSection.STATUS_EVIDENCE, caseTiming), testCase);
    }

    private static ExtentTest timedSection(
            ExtentTest parent,
            CaseSection section,
            AggregateCaseTiming timing
    ) {
        ExtentTest node = parent.createNode(section.title);
        applyTiming(node, timing);
        return node;
    }

    private static void writeExecutionMetadata(
            ExtentTest node,
            AggregateExtentReportWriter.CaseRecord testCase,
            AggregateCaseTiming caseTiming
    ) {
        node.info(MarkupHelper.createTable(new String[][]{
                {"Suite", ReportText.printable(testCase.suiteName())},
                {"Test context", ReportText.printable(testCase.contextName())},
                {"Class", ReportText.printable(testCase.className())},
                {"Method", ReportText.printable(testCase.methodName())},
                {"Parameters", ReportText.printable(testCase.parameters())},
                {"Duration ms", String.valueOf(caseTiming.durationMillis())}
        }));
    }

    private static void writeContractProof(
            ExtentTest node,
            AggregateExtentReportWriter.CaseRecord testCase
    ) {
        node.info(MarkupHelper.createTable(new String[][]{
                {FrameworkContractReportLabels.SUITE_PROFILE, ReportText.printable(testCase.suiteProfile())},
                {FrameworkContractReportLabels.CAPABILITY, ReportText.printable(testCase.capability())},
                {"Purpose", ReportText.printable(testCase.purpose())},
                {FrameworkContractReportLabels.AREA, ReportText.printable(testCase.area())},
                {FrameworkContractReportLabels.OWNER, ReportText.printable(testCase.owner())},
                {FrameworkContractReportLabels.EXPECTED_CONTRACT, ReportText.printable(testCase.expected())},
                {FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT, ReportText.printable(testCase.observed())},
                {FrameworkContractReportLabels.REPAIR_HINT, ReportText.printable(testCase.repairHint())}
        }));
    }

    private static void writeStatus(ExtentTest node, AggregateExtentReportWriter.CaseRecord testCase) {
        if (testCase.status().passed()) {
            node.pass("PASS: test case passed.");
        } else if (testCase.status().skipped()) {
            node.skip("SKIP: test case skipped.");
            writeSkippedStackTraceIfPresent(node, testCase);
        } else {
            node.fail("FAIL: test case failed.");
            writeFailedStackTraceIfPresent(node, testCase);
        }
    }

    private static void writeSkippedStackTraceIfPresent(
            ExtentTest node,
            AggregateExtentReportWriter.CaseRecord testCase
    ) {
        if (!testCase.stackTrace().isBlank()) {
            node.info(MarkupHelper.createCodeBlock(testCase.stackTrace()));
        }
    }

    private static void writeFailedStackTraceIfPresent(
            ExtentTest node,
            AggregateExtentReportWriter.CaseRecord testCase
    ) {
        if (!testCase.stackTrace().isBlank()) {
            node.fail(MarkupHelper.createCodeBlock(testCase.stackTrace()));
        }
    }

    private static void applyTiming(ExtentTest node, AggregateCaseTiming timing) {
        node.getModel().setStartTime(new Date(timing.startMillis()));
        node.getModel().setEndTime(new Date(timing.endMillis()));
    }
}
