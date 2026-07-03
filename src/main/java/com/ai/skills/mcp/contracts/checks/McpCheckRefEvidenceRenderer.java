package com.ai.skills.mcp.contracts.checks;

import com.ai.core.support.html.HtmlTable;
import com.ai.core.support.html.HtmlTableColumn;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders YAML-backed @McpCheckRef metadata into agent-facing evidence.
 */
public final class McpCheckRefEvidenceRenderer {
    private static final String TITLE = "MCP CheckRef Matrix";
    private static final String DESCRIPTION = "Generated from @McpCheckRef usages and skills/mcp/contracts/checks/mcp-checks-*.yml.";
    private static final List<HtmlTableColumn<McpCheckSpec>> COLUMNS = List.of(
            HtmlTableColumn.code("Check ref", McpCheckSpec::id),
            HtmlTableColumn.text("Expected", McpCheckSpec::expected),
            HtmlTableColumn.code("Artifacts", spec -> String.join(", ", spec.artifacts())),
            HtmlTableColumn.text("Failure meaning", McpCheckSpec::failureMeaning),
            HtmlTableColumn.text("Repair hint", McpCheckSpec::repairHint)
    );

    private McpCheckRefEvidenceRenderer() {
    }

    public static String renderMatrix(Collection<String> referencedIds) {
        StringBuilder matrix = new StringBuilder()
                .append("# ").append(TITLE).append(System.lineSeparator()).append(System.lineSeparator())
                .append(DESCRIPTION)
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append(markdownHeader())
                .append(System.lineSeparator())
                .append(markdownSeparator())
                .append(System.lineSeparator());
        for (McpCheckSpec spec : specs(referencedIds)) {
            matrix.append(markdownRow(spec))
                    .append(System.lineSeparator());
        }
        return matrix.toString();
    }

    public static String renderHtmlMatrix(Collection<String> referencedIds) {
        HtmlTable table = HtmlTable.withHeadings(COLUMNS.stream().map(HtmlTableColumn::heading).toArray(String[]::new));
        for (McpCheckSpec spec : specs(referencedIds)) {
            table.row(COLUMNS.stream().map(column -> column.htmlCell(spec)).toArray(HtmlTable.Cell[]::new));
        }
        String html = "<h1>" + HtmlText.html(TITLE) + "</h1>"
                + "<p>" + HtmlText.html(DESCRIPTION) + "</p>"
                + table.render();
        return HtmlText.diagnosticDocument(TITLE, html);
    }

    public static String renderFinalReportSection(Collection<String> referencedIds) {
        StringBuilder section = new StringBuilder()
                .append("<section id=\"mcp-check-ref-evidence\">")
                .append("<h2>MCP CheckRef Evidence</h2>")
                .append("<p>Every YAML-backed @McpCheckRef used by MCP tests is listed here and in the MCP CheckRef Matrix.</p>")
                .append("<ul>");
        for (McpCheckSpec spec : specs(referencedIds)) {
            section.append("<li><code>").append(HtmlText.html(spec.id())).append("</code> - ")
                    .append(HtmlText.html(spec.expected())).append("</li>");
        }
        section.append("</ul></section>");
        return section.toString();
    }

    private static List<McpCheckSpec> specs(Collection<String> referencedIds) {
        return referencedIds.stream()
                .distinct()
                .sorted()
                .map(ContractCatalog::requireMcpCheck)
                .sorted(Comparator.comparing(McpCheckSpec::id))
                .collect(Collectors.toUnmodifiableList());
    }

    private static String markdown(String value) {
        return HtmlText.markdownCell(value);
    }

    private static String markdownHeader() {
        return COLUMNS.stream()
                .map(HtmlTableColumn::heading)
                .map(McpCheckRefEvidenceRenderer::markdown)
                .collect(Collectors.joining(" | ", "| ", " |"));
    }

    private static String markdownSeparator() {
        return COLUMNS.stream()
                .map(HtmlTableColumn::markdownSeparator)
                .collect(Collectors.joining("|", "|", "|"));
    }

    private static String markdownRow(McpCheckSpec spec) {
        return COLUMNS.stream()
                .map(column -> column.markdownValue(spec))
                .collect(Collectors.joining(" | ", "| ", " |"));
    }
}
