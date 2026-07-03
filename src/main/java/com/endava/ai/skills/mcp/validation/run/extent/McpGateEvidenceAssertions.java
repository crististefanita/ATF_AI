package com.endava.ai.skills.mcp.validation.run.extent;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.run.extent.evidence.McpFrozenExtentReportEvidence;
import com.endava.ai.skills.mcp.validation.run.extent.evidence.McpFrozenGateResultEvidence;
import com.endava.ai.skills.mcp.validation.run.extent.proof.McpExtentProofClassPublication;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class McpGateEvidenceAssertions {
    private final McpPackageAuditValidation audit;
    private final McpFrozenExtentReportEvidence frozenExtentReport;
    private final McpFrozenGateResultEvidence frozenGateResult;
    private final McpExtentProofClassPublication proofClassPublication;

    public McpGateEvidenceAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
        this.frozenExtentReport = new McpFrozenExtentReportEvidence(audit);
        this.frozenGateResult = new McpFrozenGateResultEvidence(audit);
        this.proofClassPublication = new McpExtentProofClassPublication(audit);
    }

    public void assertFrozenMcpExtentReportContainsReadableContractDetails() throws IOException {
        Path report = audit.context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        List<String> blockers = new ArrayList<>();
        frozenExtentReport.collectViolations(report, blockers);
        audit.assertNoSyntheticViolations("Frozen MCP Extent report detail contract", blockers, report,
                "mcp.extent.details.missing",
                audit.details(
                        "Asserts: the saved MCP Extent report is readable as a contract audit, not only a green shell.",
                        "Phase: frozen-evidence validation after phase-1 package audit and phase-2 artifact save.",
                        "Artifact: " + McpArtifactPaths.MCP_EXTENT_REPORT + ".",
                        "Expected detail tokens: Asserts, Artifacts, Expected, runtime keys, package-state, run-state, diff, graph, score."));
    }

    public void assertFrozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
        Path gate = audit.context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT);
        List<String> blockers = new ArrayList<>();
        frozenGateResult.collectViolations(gate, blockers);
        audit.assertNoSyntheticViolations("Frozen MCP gate result freshness", blockers, gate,
                "mcp.gate-result.stale-pass",
                audit.details(
                        "Asserts: frozen " + McpArtifactPaths.MCP_GATE_RESULT
                                + " agrees with the current validator state.",
                        "Phase: frozen-evidence validation after the MCP suite has produced validation/mcp artifacts.",
                        "Artifact: " + McpArtifactPaths.MCP_GATE_RESULT + ".",
                        "Expected: no PASS/no-blocker claim when the current MCP validator reports blockers; "
                                + "no PENDING claim after the current validator is clean.",
                        "Reason: MCP evidence must be regenerated after changing rules, sources, report, or package artifacts."));

    }

    public void assertExtentProofClassIsPublishedAndReflectedInScoring() throws IOException {
        McpExtentProofClassPublication.Result result = proofClassPublication.evaluate();
        audit.assertNoViolations("Extent proof class is published and reflected in scoring",
                result.violations(),
                audit.details(
                        "Asserts: per-run Extent proof is classified as spark, standalone, or synthetic.",
                        "Artifacts: runs/run_##/validation/reports/ExtentReport.html, README.md, final report, package-state.json.",
                        "Expected: synthetic/standalone proof remains weak evidence and keeps Overall capped; "
                                + "Spark proof may support stronger evidence only with UI-owned product proof.",
                        "Observed classes: " + result.distinctClasses()));
    }
}
