package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.util.Locale;

final class McpGateEvidence {
    private final String text;
    private final String lowerText;
    private final String packageName;
    private final boolean phaseOnePendingAllowed;
    private final boolean frozenEvidencePropertyRequired;
    private final boolean frozenArtifactsPresent;

    private McpGateEvidence(String text,
                            String packageName,
                            boolean phaseOnePendingAllowed,
                            boolean frozenEvidencePropertyRequired,
                            boolean frozenArtifactsPresent) {
        this.text = text == null ? "" : text;
        this.lowerText = this.text.toLowerCase(Locale.ROOT);
        this.packageName = packageName == null ? "" : packageName;
        this.phaseOnePendingAllowed = phaseOnePendingAllowed;
        this.frozenEvidencePropertyRequired = frozenEvidencePropertyRequired;
        this.frozenArtifactsPresent = frozenArtifactsPresent;
    }

    static McpGateEvidence from(McpPackageContext context, String text) {
        boolean phaseOneAllowed = Boolean.getBoolean(FullMcpGateRule.ALLOW_PHASE_ONE_PENDING_EVIDENCE_PROPERTY)
                && !Boolean.getBoolean(FullMcpGateRule.REQUIRE_FROZEN_EVIDENCE_PROPERTY);
        boolean required = Boolean.getBoolean(FullMcpGateRule.REQUIRE_FROZEN_EVIDENCE_PROPERTY);
        boolean artifactsPresent = Files.exists(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT))
                || Files.isDirectory(context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT));
        return new McpGateEvidence(
                text,
                context.packageRoot().getFileName().toString(),
                phaseOneAllowed,
                required,
                artifactsPresent);
    }

    static McpGateEvidence fromText(String text, String packageName) {
        return new McpGateEvidence(text, packageName, false, false, false);
    }

    String text() {
        return text;
    }

    boolean claimsPass() {
        return lowerText.matches("(?s).*\\b(result|status)\\s*:\\s*pass\\b.*")
                || lowerText.contains("no mcp blocker remains")
                || lowerText.matches("(?s).*\\bpassed\\s*=\\s*\\d+.*");
    }

    boolean finalCloseoutMarker() {
        return McpCloseoutCommandProfile.hasFinalCloseoutCommand(text);
    }

    boolean publishableMcpMarker() {
        return McpCloseoutCommandProfile.hasPublishableCommand(text)
                || (lowerText.contains("complete mcp") && lowerText.contains("mcpgeneratedpackagevalidatortest"));
    }

    boolean subsetOnlyMarker() {
        return lowerText.contains("mcplatestpackage")
                && !lowerText.contains("mcpgeneratedpackagevalidatortest")
                && !publishableMcpMarker();
    }

    boolean explicitPackageRoot() {
        return text.contains("-Dmcp.package.root")
                || lowerText.contains("package_root")
                || lowerText.contains("package root")
                || (!packageName.isBlank() && text.contains(packageName));
    }

    boolean requiresFrozenEvidence() {
        return !allowsPhaseOnePendingEvidence()
                || frozenEvidencePropertyRequired
                || frozenArtifactsPresent;
    }

    boolean allowsPhaseOnePendingEvidence() {
        return phaseOnePendingAllowed && !frozenEvidencePropertyRequired;
    }
}

