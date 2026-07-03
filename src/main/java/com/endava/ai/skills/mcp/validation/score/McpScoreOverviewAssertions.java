package com.endava.ai.skills.mcp.validation.score;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.profile.McpScoreOverviewProfile;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpScoreOverviewAssertions {
    private static final String FINAL_REPORT_ARTIFACT = "Artifact: " + McpArtifactPaths.FINAL_REPORT + ".";

    private final McpPackageAuditValidation audit;

    public void assertFinalReportScoreOverviewUsesCanonicalBusinessAxes() throws IOException {
        String html = audit.readFinalReport();
        String scoreSection = audit.detailsBody(html, McpReportSections.SCORE_OVERVIEW);
        List<String> blockers = McpScoreOverviewProfile.canonicalBlockers(scoreSection);
        audit.assertNoSyntheticViolations("Final report Score Overview canonical axes", blockers, audit.finalReport(),
                "report.score.axes",
                audit.details(
                        "Asserts: Score Overview is not only a number; it publishes the core business/audit axes and weights.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected axes: " + McpScoreOverviewProfile.axesSummary() + ".",
                        "Expected table: " + McpScoreOverviewProfile.tableSummary() + "."));
    }

    public void assertExecutiveContextCarriesVerdictAndRuntimeAsCollapsedSubsections() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        int executiveContext = html.indexOf("executive-context");
        int graph = html.indexOf(McpReportSections.BUSINESS_FLOW_GRAPH);
        if (executiveContext < 0 || graph < 0 || executiveContext > graph) {
            blockers.add("Cannot locate Executive Context before Business Flow Graph");
        } else {
            String executiveSection = html.substring(executiveContext, graph);
            if (!executiveSection.contains("<details")) {
                blockers.add("Executive Context must contain collapsed subsections for verdict/runtime detail");
            }
            audit.requireAnyToken(executiveSection, List.of("Verdict", "Ownership verdict", "Business verdict"),
                    "Executive Context missing collapsed verdict subsection", blockers);
            audit.requireAnyToken(executiveSection, List.of("Runtime", "Runtime accounting", "Runtime contract"),
                    "Executive Context missing collapsed runtime subsection", blockers);
            audit.requireToken(executiveSection, "RUN_EXPECTED_DELTA_QUALITY",
                    "Executive Context runtime subsection missing RUN_EXPECTED_DELTA_QUALITY", blockers);
            audit.requireToken(executiveSection, "ROUND_TARGET_TEST_COUNT",
                    "Executive Context runtime subsection missing ROUND_TARGET_TEST_COUNT", blockers);
            audit.requireToken(executiveSection, "META_ITERATION_COUNT",
                    "Executive Context runtime subsection missing META_ITERATION_COUNT", blockers);
        }
        audit.assertNoSyntheticViolations("Executive Context collapsed verdict/runtime subsections",
                blockers, audit.finalReport(), "report.executive-context.subsections",
                audit.details(
                        "Asserts: Executive Context carries detailed verdict/runtime information without polluting the visible opening.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: collapsed details for verdict and runtime/accounting before Business Flow Graph.",
                        "Expected runtime tokens: RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT."));
    }

    public void assertExecutiveContextIsCollapsedByDefault() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        int executiveContext = html.indexOf("executive-context");
        int graph = html.indexOf(McpReportSections.BUSINESS_FLOW_GRAPH);
        if (executiveContext < 0 || graph < 0 || executiveContext > graph) {
            blockers.add("Cannot locate Executive Context before Business Flow Graph");
        } else {
            int detailsTag = html.lastIndexOf("<details", executiveContext);
            int divTag = html.lastIndexOf("<div", executiveContext);
            int sectionStart = Math.max(detailsTag, divTag);
            String executiveSection = sectionStart >= 0
                    ? html.substring(sectionStart, graph)
                    : html.substring(executiveContext, graph);
            int openingTagEnd = executiveSection.indexOf('>');
            if (!executiveSection.trim().startsWith("<details")) {
                blockers.add("Executive Context must be a top-level <details> section, not a visible div/card");
            } else if (openingTagEnd > 0 && executiveSection.substring(0, openingTagEnd).contains(" open")) {
                blockers.add("Executive Context must be collapsed by default; remove the open attribute");
            }
        }
        audit.assertNoSyntheticViolations("Executive Context is collapsed by default",
                blockers, audit.finalReport(), "report.executive-context.default-open",
                audit.details(
                        "Asserts: Executive Context is useful but not visually dominant.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: <details id=\"executive-context\" ...> without the open attribute.",
                        "Only Business Flow Graph should be open by default."));
    }
}
