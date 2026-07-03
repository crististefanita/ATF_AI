package com.endava.ai.skills.mcp.rules.packageaudit.closeout;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpViolationBuilder;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.endava.ai.skills.mcp.rules.packageaudit.gate.McpExtentReportSurfaceEvidence;
import com.endava.ai.skills.mcp.rules.packageaudit.gate.McpFrozenArtifactCompleteness;
import com.endava.ai.skills.mcp.rules.packageaudit.gate.McpMethodLedgerEvidence;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "mcp.gate.package-closeout",
        category = McpRuleCategory.MCP_GATE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "validation/mcp/mcp-gate-result.md")
public final class FullMcpGateRule implements McpValidationRule {
    public static final String REQUIRE_FROZEN_EVIDENCE_PROPERTY = "mcp.frozen.evidence.required";
    public static final String ALLOW_PHASE_ONE_PENDING_EVIDENCE_PROPERTY = "mcp.frozen.evidence.phase1.allowed";

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path gate = context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT);
        if (!Files.isRegularFile(gate)) {
            addMissingGateResultViolation(context, result, gate);
            return;
        }

        String text = context.files().read(gate, result);
        McpGateEvidence evidence = McpGateEvidence.from(context, text);

        if (evidence.claimsPass() && !evidence.publishableMcpMarker()) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.not-publishable-profile",
                    "Frozen MCP gate claims PASS but does not prove an MCP package close-out or recognized MCP health profile was executed", gate));
        }
        if (evidence.claimsPass() && evidence.requiresFrozenEvidence() && !evidence.finalCloseoutMarker()) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.not-package-closeout",
                    "Final frozen MCP gate must name "
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName()
                            + "; full sweep, package-audit and candidate profiles are not the final publishability subset",
                    gate));
        }
        if (evidence.subsetOnlyMarker()) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.subset-only",
                    "Frozen MCP gate looks like a narrow latest-package subset, not the publishable MCP package close-out contract", gate));
        }
        String packageName = context.packageRoot().getFileName().toString();
        if (!evidence.explicitPackageRoot()) {
            result.add(McpViolationBuilder.blocker("mcp.gate-result.package-root-missing")
                    .message("Frozen MCP gate must publish the audited package root/timestamp explicitly: " + packageName)
                    .path(gate)
                    .meaning("without the audited root, a PASS result can be replayed against a different latest package")
                    .repair("rerun MCP with explicit package root or write package_root=<timestamp> into mcp-gate-result.md")
                    .artifact(McpArtifactPaths.MCP_GATE_RESULT)
                    .build());
        }
        if (evidence.claimsPass() || evidence.requiresFrozenEvidence()) {
            McpFrozenArtifactCompleteness.validate(context, result, text, gate);
            McpExtentReportSurfaceEvidence.validate(context, result);
            McpExtentProvenanceEvidence.validate(context, result);
            McpMethodLedgerEvidence.validate(context, result);
            McpFrozenEvidenceConsistency.validate(context, result, text);
        }
    }

    private void addMissingGateResultViolation(McpPackageContext context,
                                               McpPackageValidationResult result,
                                               Path gate) {
        McpGateEvidence evidence = McpGateEvidence.from(context, "");
        if (!evidence.allowsPhaseOnePendingEvidence() || evidence.requiresFrozenEvidence()) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.missing",
                    "Missing frozen MCP package close-out gate result; final close-out is forbidden until "
                            + McpArtifactPaths.MCP_GATE_RESULT + ", " + McpArtifactPaths.MCP_EXTENT_REPORT
                            + ", close-out XML and method ledger are published",
                    gate));
            return;
        }
        result.add(McpViolationBuilder.warning("mcp.gate-result.pending-freeze")
                .message("Frozen MCP gate result is not present yet; allowed only because explicit phase-1 pending evidence mode is enabled")
                .path(gate)
                .meaning("this mode is for internal pre-freeze diagnostics only; it must never be used as final package close-out")
                .repair("run the phase-3 frozen evidence contract after writing "
                        + McpArtifactPaths.MCP_EXTENT_REPORT + " and " + McpArtifactPaths.MCP_GATE_RESULT)
                .artifact(McpArtifactPaths.MCP_GATE_RESULT)
                .build());
    }
}

