package com.ai.atf.core.reporting.htmlcontract;

import com.ai.core.reporting.htmlcontract.model.HtmlReportContract;
import com.ai.core.reporting.htmlcontract.model.HtmlReportShellProfile;
import com.ai.core.reporting.htmlcontract.validation.HtmlReportShellValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class HtmlReportShellValidatorContractTest {
    private final HtmlReportShellValidator validator = new HtmlReportShellValidator();

    @Test
    public void atlasProfileRequiresAtlasOwnedSections() {
        String html = reportWithLanguage("ro",
                "<details open><summary>Executive Context</summary></details>"
                        + "<details><summary>Business Flow Graph</summary></details>"
                        + "<details><summary>Source-to-Business Trace</summary></details>"
                        + "<details><summary>Atlas Test Audit</summary></details>");

        var violations = validator.validate(html, HtmlReportShellProfile.ATLAS_FINAL_REPORT);

        Assert.assertTrue(violations.stream().anyMatch(v -> v.message().contains("Business Flow Understanding")));
        Assert.assertTrue(violations.stream().anyMatch(v -> v.message().contains("Business Evidence Tables")));
    }

    @Test
    public void atlasProfileAcceptsCanonicalSharedShell() {
        String html = reportWithLanguage("ro",
                "<details><summary>Executive Context</summary></details>"
                        + "<details open><summary>Business Flow Graph</summary></details>"
                        + "<details><summary>Business Flow Understanding</summary></details>"
                        + "<details><summary>Business Evidence Tables</summary></details>"
                        + "<details><summary>Source-to-Business Trace</summary></details>"
                        + "<details><summary>Atlas Test Audit</summary></details>");

        Assert.assertTrue(validator.validate(html, HtmlReportShellProfile.ATLAS_FINAL_REPORT).isEmpty());
    }

    @Test
    public void atlasProfileLanguageComesFromYamlContract() {
        String html = reportWithLanguage("en",
                "<details><summary>Executive Context</summary></details>"
                        + "<details open><summary>Business Flow Graph</summary></details>"
                        + "<details><summary>Business Flow Understanding</summary></details>"
                        + "<details><summary>Business Evidence Tables</summary></details>"
                        + "<details><summary>Source-to-Business Trace</summary></details>"
                        + "<details><summary>Atlas Test Audit</summary></details>");

        var violations = validator.validate(html, HtmlReportShellProfile.ATLAS_FINAL_REPORT);

        Assert.assertTrue(violations.stream().anyMatch(v -> v.code().equals("report.language")),
                "Atlas profile should require html lang=ro from skills/atlas/contracts/report-sections.yml.");
        Assert.assertEquals(HtmlReportContract.forProfile(HtmlReportShellProfile.ATLAS_FINAL_REPORT)
                .expectedLanguage(), "ro");
    }

    @Test
    public void sharedProfileDoesNotRequireMcpOrAtlasSectionVocabulary() {
        String html = report("<details open><summary>Custom Shared Report Body</summary></details>");

        Assert.assertTrue(validator.validate(html, HtmlReportShellProfile.SHARED_FINAL_REPORT).isEmpty());
    }

    @Test
    public void sharedProfileUsesContractResultForReportingSkillExtraction() {
        String html = report("<details open><summary>Custom Shared Report Body</summary></details>");

        var result = validator.validateContract(html,
                HtmlReportContract.forProfile(HtmlReportShellProfile.SHARED_FINAL_REPORT));

        Assert.assertTrue(result.passed());
        Assert.assertTrue(result.violations().isEmpty());
    }

    @Test
    public void shellRejectsSemanticClassesThatAreNotCssBacked() {
        String html = report("<details open><summary>Custom Shared Report Body</summary></details>")
                .replace("class=\"wrap\"", "class=\"report-shell\"")
                .replace("class=\"hero\"", "class=\"report-hero\"");

        var violations = validator.validate(html, HtmlReportShellProfile.SHARED_FINAL_REPORT);

        Assert.assertTrue(violations.stream().anyMatch(v -> v.code().equals("report.visual-shell.container")),
                "report-shell must not pass unless the shared CSS actually styles it.");
        Assert.assertTrue(violations.stream().anyMatch(v -> v.code().equals("report.visual-shell.hero")),
                "report-hero must not pass unless the shared CSS actually styles it.");
    }

    @Test
    public void mcpProfileRequiresMcpGateReferencesInAdditionToSharedSections() {
        String html = report(
                "<details><summary>Executive Context</summary></details>"
                        + "<details open><summary>Business Flow Graph</summary></details>"
                        + scoreOverviewDetails()
                        + "<details><summary>Proof Mapping</summary></details>"
                        + "<details><summary>Run Audit</summary></details>"
                        + "<details id=\"mcp-ai-task-advisor\"><summary>MCP AI Task Advisor</summary></details>"
                        + "<details><summary>Artifact Index</summary></details>"
                        + "<details><summary>Close-Out</summary></details>");

        var violations = validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT);

        Assert.assertEquals(violations.size(), 1);
        Assert.assertTrue(violations.get(0).message().contains("MCP Gate References"));
    }

    @Test
    public void mcpAiTaskAdvisorMustBeCollapsedDetailsNotAlwaysOpenSection() {
        String html = report(mcpSections(
                "<section id=\"mcp-ai-task-advisor\"><h2>MCP AI Task Advisor</h2>"
                        + "<p>INFO/WARN guidance for the next agentic run.</p></section>"));

        var violations = validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT);

        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.code().equals("report.section.collapsed-details.missing")),
                "MCP AI Task Advisor must fail when rendered as an always-open section.");
        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.message().contains("MCP AI Task Advisor")
                                && v.message().contains("collapsed <details>")),
                "The failure message must tell the agent to use a collapsed details section.");
    }

    @Test
    public void mcpAiTaskAdvisorMustNotBeOpenByDefault() {
        String html = report(mcpSections(
                "<details id=\"mcp-ai-task-advisor\" open><summary>MCP AI Task Advisor</summary>"
                        + "<p>INFO/WARN guidance for the next agentic run.</p></details>"));

        var violations = validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT);

        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.code().equals("report.section.collapsed-details.open")),
                "MCP AI Task Advisor must fail when its details element is open by default.");
    }

    @Test
    public void mcpAiTaskAdvisorAcceptsCollapsedCanonicalDetails() {
        String html = report(mcpSections(
                "<details id=\"mcp-ai-task-advisor\"><summary><span class=\"summary-title\">MCP AI Task Advisor</span>"
                        + "<span class=\"sumhint\">advisory guidance</span></summary>"
                        + "<p>INFO/WARN guidance for the next agentic run; this section does not override MCP blockers.</p>"
                        + "</details>"));

        Assert.assertTrue(validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT).isEmpty());
    }

    @Test
    public void scoreOverviewRejectsOverallAsWeightedAxisRow() {
        String html = report(mcpSectionsWithScoreOverview(
                "<details><summary><span class=\"summary-title\">Score Overview</span></summary>"
                        + "<table class=\"score-table\"><thead><tr><th>Axis</th><th>Score</th><th>Weight</th><th>Why it sits here</th></tr></thead><tbody>"
                        + weightedRows()
                        + "<tr><td>Overall</td><td>92</td><td>100%</td><td>Cap result, not an axis.</td></tr>"
                        + "</tbody></table></details>",
                "<details id=\"mcp-ai-task-advisor\"><summary>MCP AI Task Advisor</summary></details>"));

        var violations = validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT);

        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.code().equals("report.score-overview.overall-in-weighted-table")),
                "Overall must not be published as a weighted axis row.");
    }

    @Test
    public void scoreOverviewRejectsMalformedWeightedRows() {
        String html = report(mcpSectionsWithScoreOverview(
                "<details><summary><span class=\"summary-title\">Score Overview</span></summary>"
                        + "<table class=\"score-table\"><thead><tr><th>Axis</th><th>Score</th><th>Weight</th><th>Why it sits here</th></tr></thead><tbody>"
                        + weightedRows()
                        + "<tr><td>Overall 92</td><td>semantic final</td><td>Cap prose placed inside table.</td></tr>"
                        + "</tbody></table></details>",
                "<details id=\"mcp-ai-task-advisor\"><summary>MCP AI Task Advisor</summary></details>"));

        var violations = validator.validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT);

        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.code().equals("report.score-overview.table.row-shape")),
                "Score Overview weighted table rows must have the same cells as the header.");
        Assert.assertTrue(violations.stream()
                        .anyMatch(v -> v.code().equals("report.score-overview.overall-in-weighted-table")),
                "Malformed Overall rows must still be identified as result/cap rows.");
    }

    @Test
    public void graphPrimitivesMustTargetVisibleSvgShapes() {
        String html = report(
                "<details open><summary>Custom Shared Report Body</summary>"
                        + "<div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 200 100\">"
                        + "<g class=\"node\"><rect width=\"120\" height=\"50\"></rect>"
                        + "<text class=\"nt\">Title</text><text class=\"ns\">Subtitle</text></g>"
                        + "<path class=\"edge\" d=\"M0 0 H10\"></path>"
                        + "</svg></div></details>");

        var violations = validator.validate(html, HtmlReportShellProfile.SHARED_FINAL_REPORT);

        Assert.assertTrue(violations.stream().anyMatch(v -> v.code().equals("report.graph.node-shape")),
                "Wrapper-only graph nodes must not pass as visible styled nodes.");
        Assert.assertTrue(violations.stream().anyMatch(v -> v.code().equals("report.graph.node-wrapper")),
                "The common report shell should reject class=\"node\" on <g> wrappers.");
    }

    @Test
    public void graphPrimitivesAcceptHtmlExampleStyleVisibleNodes() {
        String html = report(
                "<details open><summary>Custom Shared Report Body</summary>"
                        + "<div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 200 100\">"
                        + "<rect class=\"node\" width=\"120\" height=\"50\"></rect>"
                        + "<text class=\"nt\" x=\"60\" text-anchor=\"middle\">Title</text>"
                        + "<text class=\"ns\" x=\"60\" text-anchor=\"middle\">Subtitle</text>"
                        + "<path class=\"edge\" d=\"M0 0 H10\"></path>"
                        + "</svg></div></details>");

        Assert.assertTrue(validator.validate(html, HtmlReportShellProfile.SHARED_FINAL_REPORT).isEmpty());
    }

    private static String report(String sections) {
        return reportWithLanguage("", sections);
    }

    private static String mcpSections(String advisorSection) {
        return mcpSectionsWithScoreOverview(scoreOverviewDetails(), advisorSection);
    }

    private static String mcpSectionsWithScoreOverview(String scoreOverview, String advisorSection) {
        return "<details><summary>Executive Context</summary></details>"
                + "<details open><summary>Business Flow Graph</summary></details>"
                + scoreOverview
                + "<details><summary>Proof Mapping</summary></details>"
                + "<details><summary>Run Audit</summary></details>"
                + "<details><summary>MCP Gate References</summary></details>"
                + advisorSection
                + "<details><summary>Artifact Index</summary></details>"
                + "<details><summary>Close-Out</summary></details>";
    }

    private static String scoreOverviewDetails() {
        return "<details><summary><span class=\"summary-title\">Score Overview</span></summary>"
                + "<table class=\"score-table\"><thead><tr><th>Axis</th><th>Score</th><th>Weight</th><th>Why it sits here</th></tr></thead><tbody>"
                + weightedRows()
                + "</tbody></table>"
                + "<table class=\"score-calculation-table\"><tbody><tr><th>Overall</th><td>92 capped by current business frontier.</td></tr></tbody></table>"
                + "</details>";
    }

    private static String weightedRows() {
        return "<tr><td>Business Substance</td><td>93</td><td>40%</td><td>Business object is real.</td></tr>"
                + "<tr><td>Business Graph</td><td>92</td><td>20%</td><td>Graph is application-first.</td></tr>"
                + "<tr><td>Historical Reopenability</td><td>92</td><td>15%</td><td>Run artifacts reopen.</td></tr>"
                + "<tr><td>Proof Chain</td><td>93</td><td>15%</td><td>Claims link to proof.</td></tr>"
                + "<tr><td>Contract Artifact</td><td>92</td><td>10%</td><td>MCP evidence is linked.</td></tr>";
    }

    private static String reportWithLanguage(String language, String sections) {
        String langAttribute = language == null || language.isBlank() ? "" : " lang=\"" + language + "\"";
        return "<!doctype html>"
                + "<html" + langAttribute + ">"
                + "<head>"
                + "<meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Report</title>"
                + "<link rel=\"stylesheet\" href=\"shared-report-reference.css\">"
                + "</head>"
                + "<body>"
                + "<main class=\"wrap\">"
                + "<header class=\"hero\"><h1>Report</h1></header>"
                + "<section class=\"score-wall\">"
                + "<article class=\"score-card\"><div class=\"label\">A</div><div class=\"score\">90</div></article>"
                + "<article class=\"score-card\"><div class=\"label\">B</div><div class=\"score\">90</div></article>"
                + "<article class=\"score-card\"><div class=\"label\">Overall</div><div class=\"score\">90</div></article>"
                + "</section>"
                + sections
                + "</main>"
                + "</body>"
                + "</html>";
    }
}
