package com.ai.skills.mcp.validation.diagnostics;

import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.StepLogger;

final class McpDiagnosticLogRenderer {
    private McpDiagnosticLogRenderer() {
    }

    static void log(McpDiagnostic diagnostic) {
        StepLogger.startStep(diagnostic.title());
        StepLogger.logDetail("Validation axis: " + diagnostic.title());
        StepLogger.logDetail("Outcome: " + diagnostic.status());
        logExpectedContract(diagnostic);
        logObservedResult(diagnostic);
        logColdVerdict(diagnostic);
        logRepairHint(diagnostic);
    }

    private static void logExpectedContract(McpDiagnostic diagnostic) {
        StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail("HTML owner: " + diagnostic.htmlOwner());
        StepLogger.logDetail("MCP profile: " + diagnostic.profile());
        StepLogger.logDetail("Required artifacts: " + diagnostic.requiredArtifacts());
        logDetail(FrameworkContractReportLabels.EXPECTED_INVARIANT, diagnostic.expectedInvariant());
        StepLogger.pass(FrameworkContractReportLabels.EXPECTED_CONTRACT_RECORDED);
    }

    private static void logObservedResult(McpDiagnostic diagnostic) {
        StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        if (diagnostic.packageRoot() != null) {
            StepLogger.logDetail("Package: " + diagnostic.packageRoot());
        }
        StepLogger.logDetail("Rule: " + diagnostic.title());
        StepLogger.logDetail("Status: " + diagnostic.status());
        StepLogger.logDetail("Blockers: " + diagnostic.blockers().size());
        StepLogger.logDetail("Warnings: " + diagnostic.warnings().size());
        StepLogger.logDetail("Evidence artifact: " + diagnostic.evidenceArtifact());
        diagnostic.blockers().forEach(violation -> StepLogger.logDetail(violation.toString()));
        StepLogger.pass(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
    }

    private static void logColdVerdict(McpDiagnostic diagnostic) {
        StepLogger.startStep(FrameworkContractReportLabels.COLD_VERDICT);
        logDetail(FrameworkContractReportLabels.PASS_FAIL, diagnostic.status());
        logDetail(FrameworkContractReportLabels.WHY, diagnostic.coldReason());
        StepLogger.pass(FrameworkContractReportLabels.COLD_VERDICT_RECORDED);
    }

    private static void logRepairHint(McpDiagnostic diagnostic) {
        StepLogger.startStep(FrameworkContractReportLabels.REPAIR_HINT);
        StepLogger.logDetail(diagnostic.repairHint());
        StepLogger.pass(FrameworkContractReportLabels.REPAIR_HINT_RECORDED);
    }

    private static void logDetail(String label, Object value) {
        StepLogger.logDetail(label + ": " + value);
    }
}
