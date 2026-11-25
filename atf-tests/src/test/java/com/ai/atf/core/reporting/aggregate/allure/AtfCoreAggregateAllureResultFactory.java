package com.ai.atf.core.reporting.aggregate.allure;

import com.ai.core.support.contract.ids.AtfContractReportLabels;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.core.reporting.ReportText;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateStatus;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;

import java.util.List;
import java.util.UUID;

public final class AtfCoreAggregateAllureResultFactory {
    private AtfCoreAggregateAllureResultFactory() {
    }

    public static TestResult summaryResult(AggregateExtentReportWriter.SummaryRecord summary) {
        Status status = summary.failed() > 0 ? Status.FAILED : summary.skipped() > 0 ? Status.SKIPPED : Status.PASSED;
        String uuid = UUID.randomUUID().toString();
        return new TestResult()
                .setUuid(uuid)
                .setHistoryId("atf-core-aggregate-summary")
                .setFullName("ATF Core Aggregate Summary")
                .setName("ATF Core Aggregate Summary")
                .setStatus(status)
                .setDescription("One Allure result summarizing all ATF core test cases in "
                        + AtfSourcePaths.ATF_ROOT_PACKAGE + ".")
                .setLabels(List.of(label("suite", "ATF Core Aggregate Audit"), label("component", "atf-core")))
                .setParameters(List.of(
                        parameter("total", summary.total()),
                        parameter("passed", summary.passed()),
                        parameter("failed", summary.failed()),
                        parameter("skipped", summary.skipped()),
                        parameter("engine", System.getProperty(
                                AtfCoreAggregateConfig.ENGINE_KEY,
                                AtfCoreAggregateConfig.DEFAULT_ENGINE))
                ));
    }

    public static TestResult result(AggregateExtentReportWriter.CaseRecord testCase) {
        TestResult result = new TestResult()
                .setUuid(UUID.randomUUID().toString())
                .setHistoryId(testCase.fullName())
                .setFullName(testCase.fullName())
                .setName(testCase.displayName())
                .setStatus(allureStatus(testCase.status()))
                .setStart(testCase.startMillis())
                .setStop(testCase.endMillis())
                .setDescription(testCase.description())
                .setLabels(List.of(
                        label("suite", testCase.suiteName()),
                        label("testContext", testCase.contextName()),
                        label("package", ReportText.packageName(testCase.className())),
                        label("class", testCase.className()),
                        label("atfProfile", ReportText.printable(testCase.suiteProfile())),
                        label("atfArea", ReportText.printable(testCase.area()))
                ))
                .setParameters(List.of(
                        parameter("method", testCase.methodName()),
                        parameter("parameters", testCase.parameters()),
                        parameter("durationMs", testCase.durationMillis()),
                        parameter("capability", ReportText.printable(testCase.capability())),
                        parameter("owner", ReportText.printable(testCase.owner())),
                        parameter("expectedContract", ReportText.printable(testCase.expected())),
                        parameter("observedValidatorResult", ReportText.printable(testCase.observed())),
                        parameter("repairHint", ReportText.printable(testCase.repairHint()))
                ))
                .setSteps(List.of(
                        expectedContractStep(testCase),
                        observedFrameworkResultStep(testCase),
                        coldFrameworkVerdictStep(testCase)
                ));

        if (!testCase.status().passed() && !testCase.stackTrace().isBlank()) {
            result.setStatusDetails(new StatusDetails()
                    .setMessage(testCase.throwableMessage())
                    .setTrace(testCase.stackTrace()));
        }
        return result;
    }

    private static StepResult expectedContractStep(AggregateExtentReportWriter.CaseRecord testCase) {
        return new StepResult()
                .setName(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT)
                .setStatus(Status.PASSED)
                .setParameters(List.of(
                        parameter(AtfContractReportLabels.SUITE_PROFILE,
                                ReportText.printable(testCase.suiteProfile())),
                        parameter(AtfContractReportLabels.CAPABILITY,
                                ReportText.printable(testCase.capability())),
                        parameter(AtfContractReportLabels.SUITE_PURPOSE,
                                ReportText.printable(testCase.purpose())),
                        parameter(AtfContractReportLabels.AREA, ReportText.printable(testCase.area())),
                        parameter(AtfContractReportLabels.OWNER, ReportText.printable(testCase.owner())),
                        parameter(AtfContractReportLabels.EXPECTED_INVARIANT,
                                ReportText.printable(testCase.expected())),
                        parameter(AtfContractReportLabels.REPORT_HINT,
                                ReportText.printable(testCase.repairHint()))
                ));
    }

    private static StepResult observedFrameworkResultStep(AggregateExtentReportWriter.CaseRecord testCase) {
        return new StepResult()
                .setName(AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT)
                .setStatus(Status.PASSED)
                .setParameters(List.of(
                        parameter("TestNG status", testCase.status().name()),
                        parameter("Method", testCase.methodName()),
                        parameter("Parameters", ReportText.printable(testCase.parameters())),
                        parameter("Duration ms", testCase.durationMillis()),
                        parameter(AtfContractReportLabels.OBSERVED_VALIDATOR_RESULT,
                                ReportText.printable(testCase.observed()))
                ));
    }

    private static StepResult coldFrameworkVerdictStep(AggregateExtentReportWriter.CaseRecord testCase) {
        Status verdictStatus = allureStatus(testCase.status());
        String why = testCase.status().passed()
                ? "The observed framework behavior satisfied the declared ATF contract."
                : testCase.status().skipped()
                        ? "The framework contract was skipped before completion."
                        : "The framework contract did not complete successfully.";

        return new StepResult()
                .setName(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT)
                .setStatus(verdictStatus)
                .setParameters(List.of(
                        parameter(AtfContractReportLabels.PASS_FAIL,
                                testCase.status().passed() ? "PASS" : testCase.status().name()),
                        parameter(AtfContractReportLabels.WHY, why),
                        parameter(AtfContractReportLabels.REPAIR_HINT_IF_THIS_FAILS,
                                ReportText.printable(testCase.repairHint()))
                ));
    }

    private static Label label(String name, String value) {
        return new Label().setName(name).setValue(value);
    }

    private static Status allureStatus(AggregateStatus status) {
        if (status.passed()) {
            return Status.PASSED;
        }
        if (status.skipped()) {
            return Status.SKIPPED;
        }
        return Status.FAILED;
    }

    private static Parameter parameter(String name, Object value) {
        return new Parameter().setName(name).setValue(String.valueOf(value));
    }
}
