package com.endava.ai.skills.mcp.rules.report;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;
import com.endava.ai.skills.mcp.html.HtmlHrefInspector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class ProofMappingArtifactLinkInspector {
    private ProofMappingArtifactLinkInspector() {
    }

    static void requireFrameworkPropertiesHyperlink(String html, Path report, McpPackageValidationResult result) {
        String artifactIndex = artifactIndex(html);
        HtmlHrefInspector links = HtmlHrefInspector.in(artifactIndex);
        if (artifactIndex.isBlank() || !artifactIndex.contains("framework.properties")) {
            result.add(McpPackageViolation.blocker("report.framework-properties-link.missing",
                    "Artifact Index must name review/framework.properties because framework runtime settings drive screenshot and Extent obligations", report));
            return;
        }
        if (!links.hasArtifactHref("review/framework.properties")) {
            result.add(McpPackageViolation.blocker("report.framework-properties-link.missing",
                    "Artifact Index must hyperlink the audited framework.properties snapshot as ../review/framework.properties", report));
        }
    }

    static void requireCoreArtifactIndexHyperlinks(McpPackageContext context,
                                                   String html,
                                                   Path report,
                                                   McpPackageValidationResult result) {
        String artifactIndex = artifactIndex(html);
        HtmlHrefInspector links = HtmlHrefInspector.in(artifactIndex);
        for (String artifact : List.of(
                McpArtifactPaths.PACKAGE_STATE,
                McpArtifactPaths.ROOT_DIFF,
                McpArtifactPaths.LOCAL_LINK_CHECK,
                McpArtifactPaths.RENDER_PROOF,
                McpArtifactPaths.EXECUTION_GATE_CARD,
                McpArtifactPaths.PACKAGE_CLOSE_OUT)) {
            if (Files.isRegularFile(context.packageRoot().resolve(artifact))
                    && !links.hasArtifactHref(artifact)) {
                result.add(McpPackageViolation.blocker("report.artifact-index.core-link.missing",
                        "Artifact Index must hyperlink decisive reopenability artifact: " + artifact,
                        report));
            }
        }
    }

    static void requireFinalMcpEvidenceHyperlinks(McpPackageContext context,
                                                  String html,
                                                  Path report,
                                                  McpPackageValidationResult result) {
        Path mcpRoot = context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT);
        boolean frozenMcpEvidenceStarted = Files.isDirectory(mcpRoot)
                || Files.isRegularFile(context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT))
                || html.contains(McpArtifactPaths.MCP_VALIDATION_ROOT + "/");
        if (!frozenMcpEvidenceStarted) {
            return;
        }

        String artifactIndex = artifactIndex(html);
        HtmlHrefInspector links = HtmlHrefInspector.in(artifactIndex);
        McpCloseoutArtifactSpec closeout = ContractCatalog.primaryCloseoutArtifactSpec();
        for (List<String> artifactGroup : closeout.requiredArtifactIndexLinks()) {
            requireAnyArtifactLink(links, report, result, artifactGroup,
                    "Artifact Index must hyperlink required MCP close-out artifact");
        }
        for (List<String> artifactGroup : closeout.requiredArtifacts()) {
            requireAnyFrozenMcpArtifact(context, report, result, artifactGroup,
                    "Frozen package must contain required MCP close-out artifact");
        }
    }

    private static void requireAnyArtifactLink(HtmlHrefInspector links,
                                               Path report,
                                               McpPackageValidationResult result,
                                               List<String> artifacts,
                                               String message) {
        boolean present = artifacts.stream().anyMatch(links::hasArtifactHref);
        if (!present) {
            result.add(McpPackageViolation.blocker("report.mcp-evidence.missing",
                    message + ": " + String.join(" or ", artifacts), report));
        }
    }

    private static void requireAnyFrozenMcpArtifact(McpPackageContext context,
                                                    Path report,
                                                    McpPackageValidationResult result,
                                                    List<String> artifacts,
                                                    String message) {
        boolean present = artifacts.stream()
                .anyMatch(artifact -> Files.isRegularFile(context.packageRoot().resolve(artifact)));
        if (!present) {
            result.add(McpPackageViolation.blocker("report.mcp-evidence.missing",
                    message + ": " + String.join(" or ", artifacts), report));
        }
    }

    private static String artifactIndex(String html) {
        return ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Artifact Index");
    }
}
