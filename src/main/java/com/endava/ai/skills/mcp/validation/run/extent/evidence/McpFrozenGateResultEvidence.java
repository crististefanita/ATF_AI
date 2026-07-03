package com.endava.ai.skills.mcp.validation.run.extent.evidence;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpFrozenGateResultEvidence {
    private static final Pattern GATE_PASS_CLAIM = Pattern.compile(
            "(?i)\\bresult\\s*:\\s*pass\\b|no\\s+mcp\\s+blocker\\s+remains|passed=\\d+"
    );
    private static final Pattern GATE_PENDING_CLAIM = Pattern.compile(
            "(?i)\\b(result|status)\\s*:\\s*pending\\b|\\bpending\\b"
    );

    private final McpPackageAuditValidation audit;

    public void collectViolations(Path gate, List<String> blockers) throws IOException {
        if (!Files.isRegularFile(gate)) {
            blockers.add("Missing frozen MCP gate result: " + McpArtifactPaths.MCP_GATE_RESULT);
            return;
        }
        String text = Files.readString(gate);
        if (GATE_PASS_CLAIM.matcher(text).find() && audit.context.result().hasBlockers()) {
            blockers.add("Frozen MCP gate result claims PASS, but the current validator has "
                    + audit.context.result().blockerCount() + " blocker(s)");
        }
        if (GATE_PENDING_CLAIM.matcher(text).find() && !audit.context.result().hasBlockers()) {
            blockers.add("Frozen MCP gate result is still PENDING, but the current validator has no blocker");
        }
    }
}
