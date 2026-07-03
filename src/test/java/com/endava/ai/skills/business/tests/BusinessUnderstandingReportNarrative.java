package com.endava.ai.skills.business.tests;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.skills.business.model.BusinessUnderstandingArtifactSummary;
import com.endava.ai.core.support.contract.ids.AtfContractReportLabels;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.util.Map;

final class BusinessUnderstandingReportNarrative {
    private BusinessUnderstandingReportNarrative() {
    }

    static void verify(String resource, String contractId, String observed, CheckedRunnable assertions) {
        Map<String, String> contract = contract(resource, contractId);
        verify(
                contractId,
                required(contract, "expected", resource),
                observed,
                required(contract, "repair", resource),
                assertions
        );
    }

    static void verify(String title,
                       String expected,
                       String observed,
                       String repairHint,
                       CheckedRunnable assertions) {
        expected(title, expected, repairHint);
        try {
            assertions.run();
            observed(observed);
            verdict("PASS", "Business-understanding contract satisfied by the observed semantic evidence.", repairHint);
        } catch (AssertionError | RuntimeException failure) {
            observed("FAILED: " + observed + " | failure=" + failure.getMessage());
            verdict("FAIL", "Business-understanding contract did not satisfy the declared semantic evidence.", repairHint);
            throw failure;
        } catch (Exception failure) {
            observed("FAILED: " + observed + " | failure=" + failure.getMessage());
            verdict("FAIL", "Business-understanding contract did not satisfy the declared semantic evidence.", repairHint);
            throw new AssertionError("Business-understanding contract execution failed.", failure);
        }
    }

    static String observedSummary(BusinessUnderstandingArtifactSummary summary) {
        return "Artifact semantic summary: inputType=" + summary.inputType()
                + ", source=" + summary.sourceName()
                + ", sections=" + summary.sections()
                + ", links=" + summary.links()
                + ", scoreValues=" + summary.scoreValues()
                + ", concepts=" + summary.concepts();
    }

    private static void expected(String title, String expected, String repairHint) {
        StepLogger.startStep(AtfContractReportLabels.EXPECTED_CONTRACT);
        detail("Contract", title);
        detail(AtfContractReportLabels.EXPECTED_INVARIANT, expected);
        detail(AtfContractReportLabels.REPAIR_HINT, repairHint);
        StepLogger.pass(AtfContractReportLabels.EXPECTED_CONTRACT_RECORDED);
    }

    private static void observed(String observed) {
        StepLogger.startStep(AtfContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail(observed);
        StepLogger.pass(AtfContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
    }

    private static void verdict(String status, String why, String repairHint) {
        StepLogger.startStep(AtfContractReportLabels.COLD_VERDICT);
        detail(AtfContractReportLabels.PASS_FAIL, status);
        detail(AtfContractReportLabels.WHY, why);
        detail(AtfContractReportLabels.REPAIR_HINT_IF_THIS_FAILS, repairHint);
        if ("PASS".equals(status)) {
            StepLogger.pass(AtfContractReportLabels.COLD_VERDICT_RECORDED);
            return;
        }
        StepLogger.fail(
                AtfContractReportLabels.COLD_VERDICT_RECORDED,
                AtfContractReportLabels.ATF_FRAMEWORK_CONTRACT_DID_NOT_PASS
        );
    }

    private static Map<String, String> contract(String resource, String contractId) {
        return SimpleYamlContractLoader.load(resource).stream()
                .filter(entry -> contractId.equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing business-understanding contract "
                        + contractId + " in " + resource));
    }

    private static String required(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    private static void detail(String label, Object value) {
        StepLogger.logDetail(label + ": " + value);
    }

    @FunctionalInterface
    interface CheckedRunnable {
        void run() throws Exception;
    }
}
