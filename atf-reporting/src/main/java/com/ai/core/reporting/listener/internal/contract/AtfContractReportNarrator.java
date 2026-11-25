package com.ai.core.reporting.listener.internal.contract;

import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.ContractMetadata;
import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.SuiteMetadata;
import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.StepLogger;
import com.ai.core.support.contract.ids.AtfContractNarrationState;
import org.testng.ITestResult;

public final class AtfContractReportNarrator {
    private AtfContractReportNarrator() {}
    public static void logExpected(ITestResult result) {
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        if (contract == null) return;
        SuiteMetadata suite = AtfContractReportNarrativeResolver.findSuite(result);
        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT);
            if (suite != null) {
                detail(FrameworkContractReportLabels.SUITE_PROFILE, suite.profile);
                detail(FrameworkContractReportLabels.CAPABILITY, suite.capability);
                detail(FrameworkContractReportLabels.SUITE_PURPOSE, suite.purpose);
                if (!suite.reportHint.isBlank()) detail(FrameworkContractReportLabels.REPORT_HINT, suite.reportHint);
            }
            detail(FrameworkContractReportLabels.AREA, contract.area);
            detail(FrameworkContractReportLabels.OWNER, contract.owner);
            detail(FrameworkContractReportLabels.EXPECTED_INVARIANT, contract.expected);
            StepLogger.pass(FrameworkContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT_RECORDED);
            AtfContractNarrationState.markAutoExpectedPublished();
        });
    }
    public static void logObserved(ITestResult result) {
        if (AtfContractNarrationState.manualObservedPublished()) return;
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        if (contract == null) return;
        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_FRAMEWORK_RESULT);
            detail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT, contract.observed);
            StepLogger.pass(FrameworkContractReportLabels.OBSERVED_FRAMEWORK_RESULT_RECORDED);
        });
    }
    public static void logVerdict(ITestResult result, boolean passed, String why) {
        if (AtfContractNarrationState.manualVerdictPublished()) return;
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        if (contract == null) return;
        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT);
            detail(FrameworkContractReportLabels.PASS_FAIL, passed ? "PASS" : "FAIL");
            detail(FrameworkContractReportLabels.WHY, why);
            detail(FrameworkContractReportLabels.REPAIR_HINT_IF_THIS_FAILS, contract.repairHint);
            if (passed) StepLogger.pass(FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED);
            else StepLogger.fail(FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED,
                    FrameworkContractReportLabels.ATF_FRAMEWORK_CONTRACT_DID_NOT_PASS);
        });
    }
    private static void detail(String label, String value) { StepLogger.logDetail(label + ": " + value); }
}
