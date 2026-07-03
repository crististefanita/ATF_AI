package com.endava.ai.skills.mcp.validation.report;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpReportOpeningProfile;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.report.model.GraphModel;
import com.endava.ai.skills.mcp.validation.report.model.ReportOpeningModel;
import com.endava.ai.skills.mcp.validation.report.model.TemplateProfileDriftModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class McpReportTemplateAssertions {
    private final McpPackageAuditValidation audit;

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
        GraphModel graph = GraphModel.fromHtml(html, McpReportSections.BUSINESS_FLOW_GRAPH);
        List<String> blockers = new ArrayList<>();
        long openCount = graph.openSectionCount();
        if (openCount != 1) {
            blockers.add("Expected exactly one default-open details section, found " + openCount);
        }
        if (!graph.hasDefaultOpenBusinessGraph()) {
            blockers.add(McpReportSections.BUSINESS_FLOW_GRAPH + " must be the only default-open details section");
        }
        for (String missing : graph.missingSections(audit.canonicalReportSections())) {
            blockers.add("Missing collapsible details section: " + missing);
        }
        for (String nonGraphOpen : graph.nonGraphOpenSections()) {
            blockers.add("Non-graph section is open by default: " + nonGraphOpen);
        }
        audit.assertNoSyntheticViolations("Final report collapsible section contract", blockers, audit.finalReport(),
                "report.details.contract",
                audit.details(
                        "Asserts: all main report sections are collapsible details blocks.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: only Business Flow Graph is open by default.",
                        "Expected sections: " + String.join(", ", audit.canonicalReportSections()) + "."));
    }

    public void assertFinalReportDoesNotExposeRunSliceIndexBeforeExecutiveContext() throws IOException {
        String html = audit.readFinalReport();
        ReportOpeningModel opening = ReportOpeningModel.fromHtml(html);
        List<String> blockers = new ArrayList<>();
        if (!opening.hasExecutiveContextLabel()) {
            blockers.add("Cannot verify opening discipline because Executive Context is missing");
        } else if (opening.hasRunSliceIndexBeforeExecutiveContext()) {
            blockers.add("Opening exposes a standalone run-slice index before Executive Context; move run/claim/slice evidence into collapsed Executive Context, Proof Mapping, or Run Audit");
        }
        if (opening.hasRunClaimSliceHeaderBeforeExecutiveContext()) {
            blockers.add("Opening exposes a Run / Claim / Slice table before Executive Context; the opening must stay score cards + pills + Executive Context, not procedural run accounting");
        }
        audit.assertNoSyntheticViolations("Final report opening has no pre-context run-slice index",
                blockers, audit.finalReport(), "report.opening.run-slice-index",
                audit.details(
                        "Asserts: the report opening stays business-first and does not publish procedural run accounting before Executive Context.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden before Executive Context: table.run-slice-index or Run / Claim / Slice index tables.",
                        "Expected: run evidence belongs in collapsed Executive Context subsections, Proof Mapping, or Run Audit."));
    }

    public void assertFinalReportOpeningMatchesCanonicalHtmlExampleShape() throws IOException {
        String html = audit.readFinalReport();
        ReportOpeningModel opening = ReportOpeningModel.fromHtml(html);
        List<String> blockers = new ArrayList<>();
        if (!opening.hasScoreWall()) {
            blockers.add("Opening missing score-wall");
        }
        if (!opening.hasScoreCard()) {
            blockers.add("Opening missing score-card");
        }
        for (String scoreCard : McpReportOpeningProfile.canonicalScoreCards()) {
            audit.requireToken(html, scoreCard, "Score cards missing " + scoreCard, blockers);
        }
        if (!opening.hasAnyContractArtifactLabel(McpReportOpeningProfile.contractArtifactLabels())) {
            blockers.add("Score cards missing Contract Artifact");
        }
        if (!opening.hasExecutiveContextClass()) {
            blockers.add("Opening missing Executive Context class");
        }
        if (!opening.hasExecutiveContextLabel()) {
            blockers.add("Opening missing Executive Context label");
        }
        if (!opening.hasAnyExecutiveEvidence(McpReportOpeningProfile.executiveContextEvidence())) {
            blockers.add("Executive Context must summarize winner, cap, audit jump, or next pressure");
        }
        if (!opening.scoreWallBeforeExecutiveContextBeforeBusinessGraph()) {
            blockers.add("Opening order must be score wall -> Executive Context -> Business Flow Graph");
        }
        if (opening.hasEscapedNewlineArtifact()) {
            blockers.add("Final report contains escaped newline artifacts (`n), which break polished HTML output");
        }
        audit.assertNoSyntheticViolations("Final report canonical opening shape", blockers, audit.finalReport(),
                "report.opening.shape",
                audit.details(
                        "Asserts: report opening follows the reusable html-ex shape.",
                        "Expected order: score cards, Executive Context, then default-open Business Flow Graph.",
                        "Expected score cards: " + String.join(", ", McpReportOpeningProfile.canonicalScoreCards())
                                + " plus one Contract Artifact label.",
                        "Expected: no raw escaped newline artifacts."));
    }

    public void assertFinalReportFollowsPrimaryHtmlExampleTemplateProfile() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = TemplateProfileDriftModel.fromHtml(html).blockers();

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
                McpReportOpeningProfile.runtimeConstantsAndCounters(),
                audit.details(
                        "Asserts: report publishes active runtime constants and visible run/test counters.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected constants/counters: "
                                + String.join(", ", McpReportOpeningProfile.runtimeConstantsAndCounters()) + ".",
                        audit.context.runtimeSummary()));
    }

    public void assertFinalReportDoesNotLeakExternalCalibrationSources() throws IOException {
        String html = audit.readFinalReport();
        String lower = html.toLowerCase(Locale.ROOT);
        List<String> leaks = new ArrayList<>();
        for (String token : McpReportOpeningProfile.forbiddenCalibrationTokens()) {
            if (lower.contains(token)) {
                leaks.add(token);
            }
        }
        if (html.contains("C:/work/ex/java") || html.contains("C:\\work\\ex\\java")) {
            leaks.add("external absolute workspace path");
        }
        audit.assertNoSyntheticViolations("Final report external calibration leak check", leaks, audit.finalReport(), "report.external-calibration.leak",
                audit.details(
                        "Asserts: final report describes the package itself, not the calibration library.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden tokens: "
                                + String.join(", ", McpReportOpeningProfile.forbiddenCalibrationTokens())
                                + ", external absolute C:/work/ex/java paths."));

    }
    private List<String> requiredBusinessSectionTokens() {
        List<String> required = new ArrayList<>(McpReportSections.MAIN);
        required.add(McpReportSections.OVERALL);
        return required;
    }
}
