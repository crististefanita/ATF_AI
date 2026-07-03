package com.endava.ai.skills.mcp.validation.report.evidence;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorRenderer;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.report.link.McpReportLinkProbe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpReportGovernanceEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    public void assertLinksPackageGovernanceArtifacts() throws IOException {
        String html = audit.readFinalReport();
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        McpReportLinkProbe links = McpReportLinkProbe.in(artifactSection);
        List<String> missing = new ArrayList<>();
        for (String artifact : governanceArtifacts()) {
            links.requireArtifactHref(artifact,
                    "Artifact Index must hyperlink " + artifact.substring(artifact.lastIndexOf('/') + 1), missing);
        }
        audit.assertNoSyntheticViolations("Final report package governance artifact links", missing, audit.finalReport(),
                "report.governance-evidence.missing",
                audit.details(
                        "Asserts: final report Artifact Index links the saved package governance spine, not only run proof.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected links: package-state.json, root-source-thin-no-index.diff, source-evolution-ledger.md, package-close-out.md, execution-gate-card.md, frozen-package-inventory.md, local-link-check.txt, package-manifest.md, input-snapshot.md."));

    }

    public void assertPublishesAiTaskAdvisorSection() throws IOException {
        String html = audit.readFinalReport();
        List<String> missing = new ArrayList<>();
        requireToken(html, "id=\"" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + "\"",
                "stable advisor section anchor", missing);
        requireToken(html, "<details id=\"" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + "\"",
                "collapsed advisor details container", missing);
        requireToken(html, McpAiTaskAdvisorRenderer.TITLE, "advisor title", missing);
        requireToken(html, "INFO/WARN", "advisor severity vocabulary", missing);
        requireToken(html, "does not override MCP blockers", "advisor non-blocking promise", missing);
        if (html.contains("<section id=\"" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + "\"")) {
            missing.add("advisor section must not be rendered as an always-open <section>; use collapsed <details>");
        }
        audit.assertNoSyntheticViolations("Final report AI task advisor section", missing, audit.finalReport(),
                "report.ai-task-advisor.missing",
                audit.details(
                        "Asserts: final report publishes the non-blocking MCP AI Task Advisor section as a collapsed details block.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected anchor: #" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + ".",
                        "Repair: embed McpAiTaskAdvisorRenderer.renderFinalReportSection(...) after final close-out evidence."));
    }

    private static List<String> governanceArtifacts() {
        return List.of(
                McpArtifactPaths.PACKAGE_STATE,
                McpArtifactPaths.ROOT_DIFF,
                McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
                McpArtifactPaths.PACKAGE_CLOSE_OUT,
                McpArtifactPaths.EXECUTION_GATE_CARD,
                McpArtifactPaths.FROZEN_PACKAGE_INVENTORY,
                McpArtifactPaths.LOCAL_LINK_CHECK,
                McpArtifactPaths.PACKAGE_MANIFEST,
                McpArtifactPaths.INPUT_SNAPSHOT
        );
    }

    private static void requireToken(String html, String token, String label, List<String> missing) {
        if (!html.contains(token)) {
            missing.add("missing " + label + ": " + token);
        }
    }
}
