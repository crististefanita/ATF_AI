package com.endava.ai.mcp.validation.report;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpArtifactSpineProfile;
import com.endava.ai.mcp.validation.profile.McpReportSections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class McpReportEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    public McpReportEvidenceAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        String html = audit.readFinalReport();
        String proofSection = audit.detailsBody(html, McpReportSections.PROOF_MAPPING);
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        List<String> blockers = new ArrayList<>();
        for (String token : McpArtifactSpineProfile.proofMappingTokens()) {
            audit.requireToken(proofSection, token, McpReportSections.PROOF_MAPPING + " missing " + token, blockers);
        }
        for (String token : McpArtifactSpineProfile.artifactIndexTokens()) {
            audit.requireToken(artifactSection, token, McpReportSections.ARTIFACT_INDEX + " missing " + token, blockers);
        }
        audit.assertNoSyntheticViolations("Final report proof and artifact link families", blockers, audit.finalReport(),
                "report.proof-family.missing",
                audit.details(
                        "Asserts: report exposes the full audit spine, not just a narrative summary.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected " + McpReportSections.PROOF_MAPPING + ": "
                                + String.join(", ", McpArtifactSpineProfile.proofMappingTokens()) + ".",
                        "Expected " + McpReportSections.ARTIFACT_INDEX + ": "
                                + String.join(", ", McpArtifactSpineProfile.artifactIndexTokens()) + "."));
    }

    public void assertFinalReportRunAuditUsesStructuredEvidenceRows() throws IOException {
        String html = audit.readFinalReport();
        String runAudit = audit.detailsBody(html, McpReportSections.RUN_AUDIT);
        List<String> blockers = new ArrayList<>();
        if (!runAudit.contains("<table") && !runAudit.contains("run-audit-card")
                && !runAudit.contains("run-audit-grid")) {
            blockers.add("Run Audit must use an aligned table/card-grid, not a flat one-line bullet list");
        }
        audit.requireAnyToken(runAudit, List.of("diff", "thin-no-index.diff"),
                "Run Audit rows must link the run diff", blockers);
        audit.requireAnyToken(runAudit, List.of("Extent", "ExtentReport"),
                "Run Audit rows must link Extent proof", blockers);
        audit.requireAnyToken(runAudit, List.of("XML", "testng-results.xml", "TEST-"),
                "Run Audit rows must link machine XML proof", blockers);
        audit.requireToken(runAudit, "run-state.json",
                "Run Audit rows must link run-state.json", blockers);
        audit.assertNoSyntheticViolations("Final report Run Audit structured evidence rows",
                blockers, audit.finalReport(), "report.run-audit.structure",
                audit.details(
                        "Asserts: Run Audit is an aligned audit surface, not prose-only bookkeeping.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected shape: table or card/grid rows per run.",
                        "Expected links per row: run diff, Extent, XML, run-state."));
    }

    public void assertFinalReportLinksFrozenMcpContractEvidence() throws IOException {
        String html = audit.readFinalReport();
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(html, List.of("MCP", "mcp"), "Final report does not mention MCP contract evidence", blockers);
        audit.requireAnyToken(html, List.of("validation/mcp/ExtentReport.html", "../validation/mcp/ExtentReport.html"),
                "Final report does not link frozen MCP Extent report", blockers);
        audit.requireAnyToken(html, List.of("validation/mcp/mcp-gate-result.md", "../validation/mcp/mcp-gate-result.md",
                        "mcp-final-gate.md"),
                "Final report does not link MCP gate result", blockers);
        audit.requireAnyToken(artifactSection, List.of("MCP", "mcp"),
                "Artifact Index does not expose MCP contract artifacts", blockers);
        Path mcpExtent = audit.context.packageRoot().resolve("validation/mcp/ExtentReport.html");
        if (!Files.isRegularFile(mcpExtent)) {
            blockers.add("Missing frozen MCP Extent artifact: validation/mcp/ExtentReport.html");
        }
        audit.assertNoSyntheticViolations("Final report frozen MCP evidence links", blockers, audit.finalReport(),
                "report.mcp-evidence.missing",
                audit.details(
                        "Asserts: MCP gate is published as a package artifact, not only left in target/reports.",
                        "Expected artifacts: validation/mcp/ExtentReport.html and validation/mcp/mcp-gate-result.md or equivalent.",
                        "Expected report links: Artifact Index and proof area expose MCP contract evidence."));
    }

    public void assertFinalReportLinksEveryRunEvidenceFamily() throws IOException {
        String html = audit.readFinalReport();
        List<String> missing = new ArrayList<>();
        for (int index = 1; index <= audit.context.runtime().metaIterationCount(); index++) {
            String run = String.format("run_%02d", index);
            audit.requireContains(html, "../runs/" + run + "/", missing);
            audit.requireContains(html, "../runs/" + run + "/review/thin-no-index.diff", missing);
            audit.requireContains(html, "../runs/" + run + "/review/run-state.json", missing);
            audit.requireContains(html, "../runs/" + run + "/validation/", missing);
        }
        audit.assertNoSyntheticViolations("Final report per-run evidence links", missing, audit.finalReport(), "report.run-evidence.missing",
                audit.details(
                        "Asserts: final report links every run to diff, run-state, and validation proof.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Relative path pattern: ../runs/run_##/review/thin-no-index.diff; ../runs/run_##/review/run-state.json; ../runs/run_##/validation/."));
    }}
