package com.endava.ai.mcp.tests.candidate;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.McpViolationSeverity;
import com.endava.ai.mcp.contract.McpContractMatrix;
import com.endava.ai.mcp.context.McpProjectLocator;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import com.endava.ai.mcp.rules.McpDefaultRules;
import com.endava.ai.mcp.validation.McpDiagnostic;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@ValidatesMcpContract("html-example-candidate")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
public class HtmlExampleCandidateMcpContractTest {
    @Test
    @ValidatesMcpContract("candidate.score.teacher-threshold")
    public void latestPackageIsTeacherGradeHtmlExampleCandidate() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = McpProjectLocator.packageRoot(projectRoot);

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);
        List<McpPackageViolation> blockers = result.violations().stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.BLOCKER)
                .collect(Collectors.toList());

        McpDiagnostic.builder("HTML example candidate contract")
                .packageRoot(packageRoot)
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

        StepLogger.startStep("Candidate rule matrix");
        StepLogger.logCodeBlock(McpContractMatrix.ruleMatrix(McpDefaultRules.htmlExampleCandidateContract()));
        StepLogger.pass("Recorded candidate MCP rule -> HTML req -> artifact matrix");
        if (!blockers.isEmpty()) {
            StepLogger.startStep("Candidate blocker matrix");
            StepLogger.logCodeBlock(McpContractMatrix.blockerMatrix(packageRoot, blockers));
            StepLogger.pass("Recorded candidate blocker failure-code matrix");
            StepLogger.startStep("Candidate contract failure matrix");
            StepLogger.logCodeBlock(McpContractMatrix.contractFailureMatrix(
                    McpDefaultRules.htmlExampleCandidateContract(),
                    blockers));
            StepLogger.pass("Recorded MCP rule -> HTML req -> artifact -> failure code matrix");
            throw new AssertionError("\nPackage is MCP-valid but not html-ex candidate ready:\n"
                    + blockers.stream().map(violation -> "- " + violation).collect(Collectors.joining("\n"))
                    + "\n\nRule matrix:\n"
                    + McpContractMatrix.ruleMatrix(McpDefaultRules.htmlExampleCandidateContract())
                    + "\n\nBlocker matrix:\n"
                    + McpContractMatrix.blockerMatrix(packageRoot, blockers)
                    + "\n\nContract failure matrix:\n"
                    + McpContractMatrix.contractFailureMatrix(
                    McpDefaultRules.htmlExampleCandidateContract(),
                    blockers));
        }
        StepLogger.pass("Package is teacher-grade html-ex candidate");
    }
}
