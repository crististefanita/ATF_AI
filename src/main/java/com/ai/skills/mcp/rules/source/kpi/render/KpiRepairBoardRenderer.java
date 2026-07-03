package com.ai.skills.mcp.rules.source.kpi.render;

import com.ai.core.support.html.HtmlTable;
import com.ai.core.support.html.HtmlTableColumn;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;

import java.util.List;
import java.util.stream.Collectors;

public final class KpiRepairBoardRenderer {
    private static final List<HtmlTableColumn<SourceEvolutionKpiRow>> HTML_COLUMNS = List.of(
            HtmlTableColumn.code("KPI", row -> row.spec().id()),
            HtmlTableColumn.status("Status", SourceEvolutionKpiRow::status),
            HtmlTableColumn.text("First blocker/risk", KpiRepairBoardRenderer::firstRepairText),
            HtmlTableColumn.code("First file", row -> row.spec().repairTargetModel().primaryRepairArtifact()),
            HtmlTableColumn.text("Agent action", row -> row.spec().agentAction()),
            HtmlTableColumn.text("Cap impact", row -> row.spec().capImpact())
    );

    private KpiRepairBoardRenderer() {
    }

    public static String renderMarkdown(List<SourceEvolutionKpiRow> rows) {
        StringBuilder board = new StringBuilder();
        board.append("# MCP KPI Repair Board").append(System.lineSeparator()).append(System.lineSeparator());
        board.append("Compact action board generated from the full KPI matrix. Fix rows in this order before asking for a higher score.")
                .append(System.lineSeparator()).append(System.lineSeparator());
        appendBoardSection(board, "Top 3 blockers", statusRows(rows, "FAIL"));
        appendBoardSection(board, "Top 3 INFO risks", statusRows(rows, "RISK"));
        board.append("## Rerun command").append(System.lineSeparator()).append(System.lineSeparator())
                .append("```bash").append(System.lineSeparator())
                .append(closeoutCommand())
                .append(System.lineSeparator()).append("```").append(System.lineSeparator()).append(System.lineSeparator());
        board.append("Full audit: `").append(McpArtifactPaths.MCP_KPI_MATRIX).append("`, `")
                .append(McpArtifactPaths.MCP_KPI_MATRIX_HTML).append("`, `")
                .append(McpArtifactPaths.MCP_KPI_FORMULA_TRACE).append("`, `")
                .append(McpArtifactPaths.MCP_SCORE_CAP_RATIONALE).append("`.")
                .append(System.lineSeparator());
        return board.toString();
    }

    public static String renderHtml(List<SourceEvolutionKpiRow> rows) {
        StringBuilder html = new StringBuilder();
        html.append("<h1>MCP KPI Repair Board</h1>")
                .append("<p>Compact action board generated from the full KPI matrix.</p>");
        appendHtmlBoardSection(html, "Top 3 blockers", statusRows(rows, "FAIL"));
        appendHtmlBoardSection(html, "Top 3 INFO risks", statusRows(rows, "RISK"));
        html.append("<section><h2>Rerun command</h2><pre>")
                .append(HtmlText.html(closeoutCommand()))
                .append("</pre></section>");
        return HtmlText.diagnosticDocument("MCP KPI Repair Board", html.toString());
    }

    private static List<SourceEvolutionKpiRow> statusRows(List<SourceEvolutionKpiRow> rows, String status) {
        return rows.stream()
                .filter(row -> status.equals(row.status()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private static void appendBoardSection(StringBuilder board, String title, List<SourceEvolutionKpiRow> rows) {
        board.append("## ").append(title).append(System.lineSeparator()).append(System.lineSeparator());
        if (rows.isEmpty()) {
            board.append("- none").append(System.lineSeparator()).append(System.lineSeparator());
            return;
        }
        for (SourceEvolutionKpiRow row : rows) {
            board.append("- `").append(row.spec().id()).append("` [").append(row.status()).append("] ")
                    .append(row.firstBlocker().isBlank() ? row.spec().qualityMeaning() : row.firstBlocker())
                    .append(System.lineSeparator())
                    .append("  First file: `").append(row.spec().repairTargetModel().primaryRepairArtifact()).append("`")
                    .append(System.lineSeparator())
                    .append("  Agent action: ").append(row.spec().agentAction())
                    .append(System.lineSeparator())
                    .append("  Cap impact: ").append(row.spec().capImpact())
                    .append(System.lineSeparator());
        }
        board.append(System.lineSeparator());
    }

    private static void appendHtmlBoardSection(StringBuilder html, String title, List<SourceEvolutionKpiRow> rows) {
        html.append("<section><h2>").append(HtmlText.html(title)).append("</h2>");
        if (rows.isEmpty()) {
            html.append("<p>none</p></section>");
            return;
        }
        HtmlTable table = HtmlTable.withHeadings(
                HTML_COLUMNS.stream().map(HtmlTableColumn::heading).toArray(String[]::new));
        for (SourceEvolutionKpiRow row : rows) {
            table.row(HTML_COLUMNS.stream().map(column -> column.htmlCell(row)).toArray(HtmlTable.Cell[]::new));
        }
        html.append(table.render()).append("</section>");
    }

    private static String firstRepairText(SourceEvolutionKpiRow row) {
        return row.firstBlocker().isBlank() ? row.spec().qualityMeaning() : row.firstBlocker();
    }

    private static String closeoutCommand() {
        return ContractCatalog.closeoutEvidence().packageCloseoutCommandTemplate();
    }
}
