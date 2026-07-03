package com.endava.ai.skills.mcp.validation.report.evidence;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.report.link.McpReportLinkProbe;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class McpReportFrozenMcpEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    public void assertLinksFrozenMcpContractEvidence() throws IOException {
        String html = audit.readFinalReport();
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        McpCloseoutArtifactSpec closeout = ContractCatalog.primaryCloseoutArtifactSpec();
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(html, List.of("MCP", "mcp"), "Final report does not mention MCP contract evidence", blockers);
        audit.requireAnyToken(artifactSection, List.of("MCP", "mcp"),
                "Artifact Index does not expose MCP contract artifacts", blockers);
        requireReportLinks(html, closeout.requiredFinalReportLinks(), blockers);
        requireArtifactIndexLinks(artifactSection, closeout.requiredArtifactIndexLinks(), blockers);
        requireFrozenArtifacts(closeout.requiredArtifacts(), blockers);
        audit.assertNoSyntheticViolations("Final report frozen MCP evidence links", blockers, audit.finalReport(),
                "report.mcp-evidence.missing",
                audit.details(
                        "Asserts: MCP gate is published as the primary package close-out artifact, not only left in target/reports.",
                        "Expected artifacts: " + closeoutArtifactSummary(closeout) + ".",
                        "Expected report links: Artifact Index and proof area expose the primary MCP close-out Extent report, provenance, gate result, XML, method ledger, KPI matrix MD/HTML, formula trace, repair board MD/HTML, and score cap rationale."));

    }

    private void requireReportLinks(String html, List<List<String>> artifactGroups, List<String> blockers) {
        for (List<String> artifactGroup : artifactGroups) {
            McpReportLinkProbe.in(html).requireAnyArtifactHref(artifactGroup,
                    "Final report does not link required MCP close-out artifact", blockers);
        }
    }

    private void requireArtifactIndexLinks(String artifactSection, List<List<String>> artifactGroups, List<String> blockers) {
        for (List<String> artifactGroup : artifactGroups) {
            McpReportLinkProbe.in(artifactSection).requireAnyArtifactHref(artifactGroup,
                    "Artifact Index must hyperlink required MCP close-out artifact", blockers);
        }
    }

    private void requireFrozenArtifacts(List<List<String>> artifactGroups, List<String> blockers) {
        for (List<String> artifactGroup : artifactGroups) {
            boolean present = artifactGroup.stream()
                    .anyMatch(artifact -> Files.isRegularFile(audit.context.packageRoot().resolve(artifact)));
            if (!present) {
                blockers.add("Missing frozen MCP close-out artifact: " + String.join(" or ", artifactGroup));
            }
        }
    }

    private static String closeoutArtifactSummary(McpCloseoutArtifactSpec closeout) {
        return closeout.requiredArtifacts().stream()
                .map(group -> group.stream()
                        .map(McpReportFrozenMcpEvidenceAssertions::readableArtifact)
                        .collect(Collectors.joining(" or ")))
                .collect(Collectors.joining(", "));
    }

    private static String readableArtifact(String artifact) {
        return artifact.startsWith(McpArtifactPaths.MCP_VALIDATION_ROOT + "/")
                ? artifact
                : McpArtifactPaths.MCP_VALIDATION_ROOT + "/" + artifact;
    }
}
