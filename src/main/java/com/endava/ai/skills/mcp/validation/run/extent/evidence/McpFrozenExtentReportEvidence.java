package com.endava.ai.skills.mcp.validation.run.extent.evidence;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentReportMode;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpFrozenExtentReportEvidence {
    private static final Pattern PASS_TOKEN = Pattern.compile("(?i)\\bpass\\b");
    private static final List<String> MCP_SUITE_TOKENS = List.of("MCP", "mcp");
    private static final List<String> AUDIT_STEP_TOKENS = List.of(
            "Complete MCP generated package audit",
            "MCP audit passed",
            "Validation axis:",
            "Generated package validator"
    );

    private static final List<String> DETAIL_TOKENS = List.of(
            "Asserts:",
            "Artifacts:",
            "Expected:",
            "Runtime keys:",
            "Relative path",
            "Outcome:"
    );
    private static final List<String> PACKAGE_ARTIFACT_TOKENS = List.of(
            "package-state.json",
            "run-state.json",
            "root-source-thin-no-index.diff",
            "Business Flow Graph",
            "Score Overview"
    );

    private final McpPackageAuditValidation audit;

    public void collectViolations(Path report, List<String> blockers) throws IOException {
        if (!Files.isRegularFile(report)) {
            blockers.add("Missing frozen MCP Extent report: " + McpArtifactPaths.MCP_EXTENT_REPORT);
            return;
        }
        String html = Files.readString(report);
        audit.requireAnyToken(html, MCP_SUITE_TOKENS,
                "MCP Extent report does not identify the MCP contract suite", blockers);
        audit.requireAnyToken(html, AUDIT_STEP_TOKENS,
                "MCP Extent report does not expose readable audit steps/details", blockers);
        audit.requireAnyToken(html, DETAIL_TOKENS,
                "MCP Extent report lacks contract details explaining what was validated", blockers);
        audit.requireAnyToken(html, PACKAGE_ARTIFACT_TOKENS,
                "MCP Extent report lacks concrete package artifact names", blockers);

        McpExtentReportMode.fromHtml(html)
                .publishedValidationViolations(report, html)
                .forEach(violation -> blockers.add(violation.message()));
        long passTokens = PASS_TOKEN.matcher(html).results().count();
        if (passTokens < 5) {
            blockers.add("MCP Extent report has too little visible pass/detail evidence: pass token count "
                    + passTokens);
        }
    }
}
