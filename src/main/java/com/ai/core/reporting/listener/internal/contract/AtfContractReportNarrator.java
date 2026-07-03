package com.ai.core.reporting.listener.internal.contract;

import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.ContractMetadata;
import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.AtlasSuiteMetadata;
import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.McpCheckMetadata;
import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.McpSuiteMetadata;
import com.ai.core.reporting.listener.internal.contract.AtfContractReportNarrativeResolver.SuiteMetadata;
import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.StepLogger;
import com.ai.core.support.contract.ids.AtfContractNarrationState;
import org.testng.ITestResult;

public final class AtfContractReportNarrator {
    private AtfContractReportNarrator() {
    }

    public static void logExpected(ITestResult result) {
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        McpCheckMetadata mcpCheck = AtfContractReportNarrativeResolver.findMcpCheck(result);
        AtlasSuiteMetadata atlasSuite = AtfContractReportNarrativeResolver.findAtlasSuite(result);
        if (contract == null && mcpCheck == null && (atlasSuite == null || isLightweightSuite(result))) {
            return;
        }
        SuiteMetadata suite = AtfContractReportNarrativeResolver.findSuite(result);
        McpSuiteMetadata mcpSuite = AtfContractReportNarrativeResolver.findMcpSuite(result);

        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT);
            if (suite != null) {
                logDetail(FrameworkContractReportLabels.SUITE_PROFILE, suite.profile);
                logDetail(FrameworkContractReportLabels.CAPABILITY, suite.capability);
                logDetail(FrameworkContractReportLabels.SUITE_PURPOSE, suite.purpose);
            }
            if (contract != null) {
                logDetail(FrameworkContractReportLabels.AREA, contract.area);
                logDetail(FrameworkContractReportLabels.OWNER, contract.owner);
                logDetail(FrameworkContractReportLabels.EXPECTED_INVARIANT, contract.expected);
            }
            if (suite != null && !suite.reportHint.isBlank()) {
                logDetail(FrameworkContractReportLabels.REPORT_HINT, suite.reportHint);
            }
            if (mcpSuite != null) {
                logDetail(FrameworkContractReportLabels.MCP_SUITE_REF, mcpSuite.id);
                logDetail(FrameworkContractReportLabels.MCP_SUITE_PROFILE, mcpSuite.profile);
                logDetail(FrameworkContractReportLabels.MCP_SUITE_CATEGORY, mcpSuite.category);
                logDetail(FrameworkContractReportLabels.MCP_SUITE_TITLE, mcpSuite.title);
                logDetail(FrameworkContractReportLabels.MCP_HTML_REQUIREMENT, mcpSuite.htmlReq);
                logDetail(FrameworkContractReportLabels.MCP_SUITE_PURPOSE, mcpSuite.purpose);
            }
            if (mcpCheck != null) {
                logDetail(FrameworkContractReportLabels.MCP_CHECK_REF, mcpCheck.id);
                logDetail(FrameworkContractReportLabels.MCP_EXPECTED_CHECK, mcpCheck.expected);
                if (!mcpCheck.artifacts.isBlank()) {
                    logDetail(FrameworkContractReportLabels.MCP_ASSERTED_ARTIFACTS, mcpCheck.artifacts);
                }
                logDetail(FrameworkContractReportLabels.MCP_FAILURE_MEANING, mcpCheck.failureMeaning);
                logDetail(FrameworkContractReportLabels.MCP_REPAIR_HINT, mcpCheck.repairHint);
            }
            if (atlasSuite != null && !isLightweightSuite(result)) {
                logAtlasSuite(atlasSuite);
            }
            StepLogger.pass(FrameworkContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT_RECORDED);
            AtfContractNarrationState.markAutoExpectedPublished();
        });
    }

    public static void logObserved(ITestResult result) {
        if (AtfContractNarrationState.manualObservedPublished()) {
            return;
        }
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        McpCheckMetadata mcpCheck = AtfContractReportNarrativeResolver.findMcpCheck(result);
        AtlasSuiteMetadata atlasSuite = AtfContractReportNarrativeResolver.findAtlasSuite(result);
        if (contract == null && (mcpCheck == null && atlasSuite == null || isLightweightSuite(result))) {
            return;
        }

        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_FRAMEWORK_RESULT);
            if (contract != null) {
                logDetail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT, contract.observed);
            } else if (mcpCheck != null) {
                logDetail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT,
                        "MCP YAML-backed validator completed for " + mcpCheck.id + ".");
                logDetail(FrameworkContractReportLabels.MCP_CHECK_REF, mcpCheck.id);
                if (!mcpCheck.artifacts.isBlank()) {
                    logDetail(FrameworkContractReportLabels.MCP_ASSERTED_ARTIFACTS, mcpCheck.artifacts);
                }
                logDetail(FrameworkContractReportLabels.MCP_FAILURE_MEANING, mcpCheck.failureMeaning);
            } else {
                logDetail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT,
                        "Atlas YAML-backed suite contract completed for " + atlasSuite.id + ".");
                logAtlasSuite(atlasSuite);
            }
            StepLogger.pass(FrameworkContractReportLabels.OBSERVED_FRAMEWORK_RESULT_RECORDED);
        });
    }

    public static void logVerdict(ITestResult result, boolean passed, String why) {
        if (AtfContractNarrationState.manualVerdictPublished()) {
            return;
        }
        ContractMetadata contract = AtfContractReportNarrativeResolver.findContract(result);
        McpCheckMetadata mcpCheck = AtfContractReportNarrativeResolver.findMcpCheck(result);
        AtlasSuiteMetadata atlasSuite = AtfContractReportNarrativeResolver.findAtlasSuite(result);
        if (contract == null && (mcpCheck == null && atlasSuite == null || isLightweightSuite(result))) {
            return;
        }

        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep(FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT);
            logDetail(FrameworkContractReportLabels.PASS_FAIL, passed ? "PASS" : "FAIL");
            logDetail(FrameworkContractReportLabels.WHY, why);
            logDetail(FrameworkContractReportLabels.REPAIR_HINT_IF_THIS_FAILS,
                    repairHint(contract, mcpCheck, atlasSuite));
            if (passed) {
                StepLogger.pass(FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED);
            } else {
                StepLogger.fail(
                        FrameworkContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED,
                        FrameworkContractReportLabels.ATF_FRAMEWORK_CONTRACT_DID_NOT_PASS
                );
            }
        });
    }

    private static void logDetail(String label, String value) {
        StepLogger.logDetail(label + ": " + value);
    }

    private static String repairHint(
            ContractMetadata contract,
            McpCheckMetadata mcpCheck,
            AtlasSuiteMetadata atlasSuite
    ) {
        if (contract != null) {
            return contract.repairHint;
        }
        if (mcpCheck != null) {
            return mcpCheck.repairHint;
        }
        return "Repair Atlas source or generated atlas artifact declared by " + atlasSuite.id
                + ", then rerun the Atlas contract profile.";
    }

    private static void logAtlasSuite(AtlasSuiteMetadata atlasSuite) {
        logDetail("Atlas suite ref", atlasSuite.id);
        logDetail("Atlas profile", atlasSuite.profile);
        if (!atlasSuite.htmlOwner.isBlank()) {
            logDetail("Atlas HTML owner", atlasSuite.htmlOwner);
        }
        if (!atlasSuite.artifact.isBlank()) {
            logDetail("Atlas artifact", atlasSuite.artifact);
        }
        if (!atlasSuite.sourceRoot.isBlank()) {
            logDetail("Atlas source root", atlasSuite.sourceRoot);
        }
        logDetail("Atlas suite purpose", atlasSuite.purpose);
    }

    private static boolean isLightweightSuite(ITestResult result) {
        if (result == null || result.getTestContext() == null) {
            return false;
        }
        String suiteName = result.getTestContext().getSuite() == null
                ? ""
                : result.getTestContext().getSuite().getName();
        String testName = result.getTestContext().getName();
        String joined = (suiteName + " " + testName).toLowerCase(java.util.Locale.ROOT);
        return joined.contains("sanity") || joined.contains("smoke");
    }
}
