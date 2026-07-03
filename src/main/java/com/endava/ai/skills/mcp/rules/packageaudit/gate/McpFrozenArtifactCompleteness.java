package com.endava.ai.skills.mcp.rules.packageaudit.gate;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class McpFrozenArtifactCompleteness {
    private McpFrozenArtifactCompleteness() {
    }

    public static void validate(McpPackageContext context,
                                McpPackageValidationResult result,
                                String gateText,
                                Path gate) {
        McpCloseoutArtifactSpec closeout = ContractCatalog.primaryCloseoutArtifactSpec();
        for (List<String> artifacts : closeout.requiredArtifacts()) {
            requireAnyArtifact(context, result, artifacts,
                    "Frozen MCP PASS must publish required MCP package close-out artifact");
        }
        for (List<String> mentions : closeout.requiredGateResultMentions()) {
            requireAnyGateText(gateText, gate, result, mentions,
                    "Frozen MCP gate result must name required MCP package close-out artifact");
        }
    }

    private static void requireAnyArtifact(McpPackageContext context,
                                           McpPackageValidationResult result,
                                           List<String> artifacts,
                                           String message) {
        boolean present = artifacts.stream()
                .anyMatch(artifact -> Files.isRegularFile(context.packageRoot().resolve(artifact)));
        if (!present) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.complete-artifacts-missing",
                    requiredArtifactMessage(message, artifacts),
                    context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT)));
        }
    }

    private static void requireAnyGateText(String text,
                                           Path gate,
                                           McpPackageValidationResult result,
                                           List<String> artifacts,
                                           String message) {
        boolean present = artifacts.stream()
                .anyMatch(artifact -> text.contains(artifact) || text.contains(Path.of(artifact).getFileName().toString()));
        if (!present) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.complete-artifacts-missing",
                    message + ": " + String.join(" or ", artifacts), gate));
        }
    }

    private static String requiredArtifactMessage(String message, List<String> artifacts) {
        if (artifacts.contains(McpArtifactPaths.MCP_EXTENT_REPORT)) {
            return message + " Extent report: " + String.join(" or ", artifacts);
        }
        return message + ": " + String.join(" or ", artifacts);
    }
}
