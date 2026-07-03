package com.endava.ai.skills.mcp.contracts.advisor;

import com.endava.ai.core.support.html.HtmlTable;
import com.endava.ai.core.support.html.HtmlText;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders advisory guidance separately from MCP blockers.
 */
public final class McpAiTaskAdvisorRenderer {
    public static final String TITLE = "MCP AI Task Advisor";
    public static final String SECTION_ANCHOR = "mcp-ai-task-advisor";

    private McpAiTaskAdvisorRenderer() {
    }

    public static String renderMarkdown(Collection<McpAiTaskAdvisorSpec> specs) {
        StringBuilder markdown = new StringBuilder()
                .append("# ").append(TITLE).append(System.lineSeparator()).append(System.lineSeparator())
                .append("Advisor findings are INFO/WARN guidance only; integrity issues remain separate MCP blockers.")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Severity | Advisor | Signal | Model tier | Acceptance criteria | Stop condition | Value")
                .append(System.lineSeparator())
                .append("--- | --- | --- | --- | --- | --- | ---")
                .append(System.lineSeparator());
        for (McpAiTaskAdvisorSpec spec : sorted(specs)) {
            markdown.append(HtmlText.markdownCell(spec.severity())).append(" | ")
                    .append(HtmlText.markdownCell(spec.id())).append(" | ")
                    .append(HtmlText.markdownCell(spec.signal())).append(" | ")
                    .append(HtmlText.markdownCell(spec.modelTier())).append(" | ")
                    .append(HtmlText.markdownCell(spec.acceptanceCriteria())).append(" | ")
                    .append(HtmlText.markdownCell(spec.stopCondition())).append(" | ")
                    .append(HtmlText.markdownCell(spec.valueRationale()))
                    .append(System.lineSeparator());
        }
        return markdown.toString();
    }

    public static String renderHtml(Collection<McpAiTaskAdvisorSpec> specs) {
        HtmlTable table = HtmlTable.withHeadings(
                "Severity", "Advisor", "Signal", "Recommendation", "Model tier",
                "Acceptance criteria", "Stop condition", "Value");
        for (McpAiTaskAdvisorSpec spec : sorted(specs)) {
            table.row(
                    HtmlTable.Cell.classed(spec.severity().toLowerCase(), spec.severity()),
                    HtmlTable.Cell.code(spec.id()),
                    HtmlTable.Cell.text(spec.signal()),
                    HtmlTable.Cell.text(spec.recommendation()),
                    HtmlTable.Cell.text(spec.modelTier()),
                    HtmlTable.Cell.text(spec.acceptanceCriteria()),
                    HtmlTable.Cell.text(spec.stopCondition()),
                    HtmlTable.Cell.text(spec.valueRationale()));
        }
        String body = "<section id=\"" + HtmlText.html(SECTION_ANCHOR) + "\">"
                + "<h1>" + HtmlText.html(TITLE) + "</h1>"
                + "<p>Advisor findings are INFO/WARN guidance only; integrity issues remain separate MCP blockers.</p>"
                + table.render()
                + "</section>";
        return HtmlText.diagnosticDocument(TITLE, body);
    }

    public static String renderFinalReportSection(Collection<McpAiTaskAdvisorSpec> specs) {
        StringBuilder html = new StringBuilder()
                .append("<details id=\"").append(HtmlText.html(SECTION_ANCHOR)).append("\">")
                .append("<summary><span class=\"summary-title\">").append(HtmlText.html(TITLE)).append("</span>")
                .append("<span class=\"sumhint\">advisory guidance</span></summary>")
                .append("<p>INFO/WARN guidance for the next agentic run; this section does not override MCP blockers.</p>")
                .append("<ul>");
        for (McpAiTaskAdvisorSpec spec : sorted(specs)) {
            html.append("<li><strong>").append(HtmlText.html(spec.severity())).append("</strong> ")
                    .append("<code>").append(HtmlText.html(spec.id())).append("</code>: ")
                    .append(HtmlText.html(spec.recommendation())).append("</li>");
        }
        html.append("</ul></details>");
        return html.toString();
    }

    private static List<McpAiTaskAdvisorSpec> sorted(Collection<McpAiTaskAdvisorSpec> specs) {
        return specs.stream()
                .sorted(Comparator.comparing(McpAiTaskAdvisorSpec::severity)
                        .thenComparing(McpAiTaskAdvisorSpec::id))
                .collect(Collectors.toUnmodifiableList());
    }
}
