package com.endava.ai.mcp.validation.report;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpReportSections;
import com.endava.ai.mcp.validation.profile.McpReportTemplateProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class McpReportTemplateAssertions {
    private final McpPackageAuditValidation audit;

    public McpReportTemplateAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertFinalReportHasRequiredBusinessStructure() {
        audit.assertNoBlockers("Final report business structure",
                violation -> violation.code().startsWith("report.")
                        && !violation.code().equals("report.link.broken"),
                audit.details(
                        "Asserts: final HTML report has the canonical business-first structure.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: " + McpReportSections.BUSINESS_FLOW_GRAPH + " default-open, "
                                + String.join(", ", McpReportSections.COLLAPSED_MAIN) + ".",
                        "Expected: no external calibration language such as html-ex/teacher/benchmark/comparison."));
    }

    public void assertFinalReportContainsCanonicalBusinessSections() throws IOException {
        String html = audit.readFinalReport();
        audit.assertContainsAll("Final report canonical business sections", html, audit.finalReport(),
                requiredBusinessSectionTokens(),
                audit.details(
                        "Asserts: canonical report sections are present by exact section names.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Required tokens: " + String.join(", ", requiredBusinessSectionTokens()) + "."));
    }

    public void assertFinalReportMainSectionsAreCollapsibleWithOnlyGraphOpen() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        List<McpPackageAuditValidation.DetailsBlock> details = audit.detailsBlocks(html);
        long openCount = details.stream().filter(McpPackageAuditValidation.DetailsBlock::open).count();
        if (openCount != 1) {
            blockers.add("Expected exactly one default-open details section, found " + openCount);
        }
        if (details.stream().noneMatch(block -> block.open()
                && block.summary().contains(McpReportSections.BUSINESS_FLOW_GRAPH))) {
            blockers.add(McpReportSections.BUSINESS_FLOW_GRAPH + " must be the only default-open details section");
        }
        for (String required : audit.canonicalReportSections()) {
            if (details.stream().noneMatch(block -> block.summary().contains(required))) {
                blockers.add("Missing collapsible details section: " + required);
            }
        }
        for (McpPackageAuditValidation.DetailsBlock block : details) {
            if (!block.summary().contains(McpReportSections.BUSINESS_FLOW_GRAPH) && block.open()) {
                blockers.add("Non-graph section is open by default: " + block.summary());
            }
        }
        audit.assertNoSyntheticViolations("Final report collapsible section contract", blockers, audit.finalReport(),
                "report.details.contract",
                audit.details(
                        "Asserts: all main report sections are collapsible details blocks.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: only Business Flow Graph is open by default.",
                        "Expected sections: " + String.join(", ", audit.canonicalReportSections()) + "."));
    }

    public void assertFinalReportOpeningMatchesCanonicalHtmlExampleShape() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        audit.requireToken(html, "class=\"score-wall\"", "Opening missing score-wall", blockers);
        audit.requireToken(html, "class=\"score-card\"", "Opening missing score-card", blockers);
        audit.requireToken(html, "Business Substance", "Score cards missing Business Substance", blockers);
        audit.requireToken(html, "Business Graph", "Score cards missing Business Graph", blockers);
        audit.requireToken(html, "Historical Reopenability", "Score cards missing Historical Reopenability", blockers);
        audit.requireToken(html, "Proof Chain", "Score cards missing Proof Chain", blockers);
        audit.requireAnyToken(html, List.of("Contract Artifact", "Contract Artefact", "Artifact"),
                "Score cards missing Contract Artifact", blockers);
        audit.requireToken(html, McpReportSections.OVERALL, "Score cards missing Overall", blockers);
        audit.requireToken(html, "executive-context", "Opening missing Executive Context class", blockers);
        audit.requireToken(html, McpReportSections.EXECUTIVE_CONTEXT, "Opening missing Executive Context label", blockers);
        audit.requireAnyToken(html, List.of("winner", "cap", "audit", "owned", "not owned", "blocked", "next"),
                "Executive Context must summarize winner, cap, audit jump, or next pressure", blockers);
        int scoreWall = html.indexOf("class=\"score-wall\"");
        int executive = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
        int graph = html.indexOf(McpReportSections.BUSINESS_FLOW_GRAPH);
        if (scoreWall < 0 || executive < 0 || graph < 0) {
            blockers.add("Cannot verify opening order: score wall -> Executive Context -> Business Flow Graph");
        } else if (!(scoreWall < executive && executive < graph)) {
            blockers.add("Opening order must be score wall -> Executive Context -> Business Flow Graph");
        }
        if (html.contains("`n")) {
            blockers.add("Final report contains escaped newline artifacts (`n), which break polished HTML output");
        }
        audit.assertNoSyntheticViolations("Final report canonical opening shape", blockers, audit.finalReport(),
                "report.opening.shape",
                audit.details(
                        "Asserts: report opening follows the reusable html-ex shape.",
                        "Expected order: score cards, Executive Context, then default-open Business Flow Graph.",
                        "Expected score cards: Business Substance, Business Graph, Historical Reopenability, Proof Chain, Contract Artifact, Overall.",
                        "Expected: no raw escaped newline artifacts."));
    }

    public void assertFinalReportFollowsPrimaryHtmlExampleTemplateProfile() throws IOException {
        String html = audit.readFinalReport();
        McpReportTemplateProfile reference = McpReportTemplateProfile.primaryReference();
        McpReportTemplateProfile actual = McpReportTemplateProfile.fromHtml(html);
        List<String> blockers = new ArrayList<>();

        if (!reference.scoreCardLabels().equals(actual.scoreCardLabels())) {
            blockers.add("Score-card labels/order drift from primary html-ex profile. Expected "
                    + reference.scoreCardLabels() + " but found " + actual.scoreCardLabels());
        }
        if (!markersInOrder(html, reference.openingSequence())) {
            blockers.add("Opening sequence must follow primary html-ex rhythm: "
                    + String.join(" -> ", reference.openingSequence()));
        }
        blockers.addAll(missing("reusable visual classes", reference.visualClasses(), actual.visualClasses()));
        blockers.addAll(missing("default-open sections", reference.openSections(), actual.openSections()));
        blockers.addAll(missing("collapsed sections", reference.collapsedSections(), actual.collapsedSections()));
        blockers.addAll(missing("Proof Mapping headers", reference.proofHeaders(), actual.proofHeaders()));
        blockers.addAll(missing("Run Audit evidence tokens", reference.runAuditEvidenceTokens(), actual.runAuditEvidenceTokens()));
        blockers.addAll(missing("Artifact Index evidence tokens", reference.artifactIndexTokens(), actual.artifactIndexTokens()));

        audit.assertNoSyntheticViolations("Final report follows primary html-ex template profile",
                blockers, audit.finalReport(), "report.template-profile.drift",
                audit.details(
                        "Asserts: report shape is learned from the primary docs/html-ex structural reference, without copying business text or scores.",
                        "Reference source: docs/html-ex/README.md -> Primary structural reference report.",
                        "Checked profile: score-card order, opening rhythm, collapse policy, Proof Mapping headers, Run Audit evidence, Artifact Index evidence.",
                        "Ignored by design: exact business labels, exact SVG nodes, exact scores, and prose wording."));
    }

    public void assertFinalReportPublishesRuntimeConstantsAndCounters() throws IOException {
        String html = audit.readFinalReport();
        audit.assertContainsAll("Final report runtime constants and counters", html, audit.finalReport(),
                List.of("RUN_EXPECTED_DELTA_QUALITY", "ROUND_TARGET_TEST_COUNT",
                        "META_ITERATION_COUNT", "runs", "tests"),
                audit.details(
                        "Asserts: report publishes active runtime constants and visible run/test counters.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected constants: RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT.",
                        audit.context.runtimeSummary()));
    }

    public void assertFinalReportDoesNotLeakExternalCalibrationSources() throws IOException {
        String html = audit.readFinalReport();
        String lower = html.toLowerCase(Locale.ROOT);
        List<String> leaks = new ArrayList<>();
        if (lower.contains("html-ex")) {
            leaks.add("html-ex");
        }
        if (lower.contains("teacher")) {
            leaks.add("teacher");
        }
        if (lower.contains("benchmark")) {
            leaks.add("benchmark");
        }
        if (html.contains("C:/work/ex/java") || html.contains("C:\\work\\ex\\java")) {
            leaks.add("external absolute workspace path");
        }
        audit.assertNoSyntheticViolations("Final report external calibration leak check", leaks, audit.finalReport(), "report.external-calibration.leak",
                audit.details(
                        "Asserts: final report describes the package itself, not the calibration library.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden tokens: html-ex, teacher, benchmark, external absolute C:/work/ex/java paths."));
    }
    private boolean markersInOrder(String html, List<String> markers) {
        int cursor = -1;
        for (String marker : markers) {
            int index = html.indexOf(marker, cursor + 1);
            if (index < 0) {
                return false;
            }
            cursor = index;
        }
        return true;
    }

    private List<String> requiredBusinessSectionTokens() {
        List<String> required = new ArrayList<>(McpReportSections.MAIN);
        required.add(McpReportSections.OVERALL);
        return required;
    }

    private List<String> missing(String label, List<String> expected, List<String> actual) {
        List<String> blockers = new ArrayList<>();
        for (String value : expected) {
            if (!actual.contains(value)) {
                blockers.add(label + " missing value from primary html-ex profile: " + value);
            }
        }
        return blockers;
    }
}
