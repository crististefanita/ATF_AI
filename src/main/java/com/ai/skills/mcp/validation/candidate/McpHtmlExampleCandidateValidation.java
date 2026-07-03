package com.ai.skills.mcp.validation.candidate;

import com.ai.core.reporting.StepLogger;
import com.ai.skills.mcp.McpGeneratedPackageValidator;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationSeverity;
import com.ai.skills.mcp.context.McpProjectLocator;
import com.ai.skills.mcp.contract.McpContractMatrix;
import com.ai.skills.mcp.rules.McpDefaultRules;
import com.ai.skills.mcp.validation.diagnostics.McpDiagnostic;
import com.ai.skills.mcp.validation.McpPackageAuditContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class McpHtmlExampleCandidateValidation {
    private McpHtmlExampleCandidateValidation() {
    }

    public static void assertContract(McpPackageAuditContext context) throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(context.packageRoot());
        List<McpPackageViolation> blockers = blockers(result);

        publishDiagnostic(context, result, blockers);
        publishRuleMatrix();
        if (!blockers.isEmpty()) {
            publishFailureMatrices(context, blockers);
            throw new AssertionError(failureMessage(context, blockers));
        }
        StepLogger.pass("Package is teacher-grade html-ex candidate");
    }

    private static List<McpPackageViolation> blockers(McpPackageValidationResult result) {
        return result.violations().stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.BLOCKER)
                .collect(Collectors.toList());
    }

    private static void publishDiagnostic(McpPackageAuditContext context,
                                          McpPackageValidationResult result,
                                          List<McpPackageViolation> blockers) {
        McpDiagnostic.builder("HTML example candidate contract")
                .packageRoot(context.packageRoot())
                .htmlOwner("score-overview-reference.html + business-flow-graph-rubric.html + reporting.html")
                .profile("htmlExampleCandidateContract")
                .requiredArtifacts("review/package-state.json; analysis/langgraph-business-understanding.html; run state/diff/proof chain")
                .expectedInvariant("MCP-valid is not enough; candidate must meet runtime teacher threshold and semantic evidence trace")
                .status(blockers.isEmpty() ? "PASS" : "FAIL")
                .violations(result.violations())
                .evidenceArtifact("review/package-state.json + analysis/langgraph-business-understanding.html")
                .coldReason(blockers.isEmpty()
                        ? "package satisfies the candidate-grade threshold and evidence trace."
                        : "candidate blockers prove the package is MCP-valid but not teacher-grade yet.")
                .repairHint(blockers.isEmpty()
                        ? "No repair required now; rerun candidate MCP after any report or artifact edit."
                        : "Repair candidate blockers, regenerate affected artifacts, then rerun htmlExampleCandidateContract.")
                .build()
                .log();
    }

    private static void publishRuleMatrix() {
        StepLogger.startStep("Candidate rule matrix");
        StepLogger.logCodeBlock(McpContractMatrix.ruleMatrix(McpDefaultRules.htmlExampleCandidateContract()));
        StepLogger.pass("Recorded candidate MCP rule -> HTML req -> artifact matrix");
    }

    private static void publishFailureMatrices(McpPackageAuditContext context, List<McpPackageViolation> blockers) {
        StepLogger.startStep("Candidate blocker matrix");
        StepLogger.logCodeBlock(McpContractMatrix.blockerMatrix(context.packageRoot(), blockers));
        StepLogger.pass("Recorded candidate blocker failure-code matrix");
        StepLogger.startStep("Candidate contract failure matrix");
        StepLogger.logCodeBlock(McpContractMatrix.contractFailureMatrix(
                McpDefaultRules.htmlExampleCandidateContract(),
                blockers));
        StepLogger.pass("Recorded MCP rule -> HTML req -> artifact -> failure code matrix");
    }

    private static String failureMessage(McpPackageAuditContext context, List<McpPackageViolation> blockers) {
        return "\nPackage is MCP-valid but not html-ex candidate ready:\n"
                + blockers.stream().map(violation -> "- " + violation).collect(Collectors.joining("\n"))
                + "\n\nRule matrix:\n"
                + McpContractMatrix.ruleMatrix(McpDefaultRules.htmlExampleCandidateContract())
                + "\n\nBlocker matrix:\n"
                + McpContractMatrix.blockerMatrix(context.packageRoot(), blockers)
                + "\n\nContract failure matrix:\n"
                + McpContractMatrix.contractFailureMatrix(
                McpDefaultRules.htmlExampleCandidateContract(),
                blockers);
    }
}
