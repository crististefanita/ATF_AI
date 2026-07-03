package com.ai.skills.mcp.rules.source.kpi.render;

import com.ai.core.support.html.HtmlTable;
import com.ai.core.support.html.HtmlTableColumn;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;

import java.util.List;

public final class KpiMatrixHtmlRenderer {
    private static final String TITLE = "MCP Source Evolution KPI Matrix";
    private static final String DESCRIPTION = "Generated from executable MCP KPI rules. "
            + "Repair targets point to the first artifact an agent should inspect or update.";
    private static final List<HtmlTableColumn<SourceEvolutionKpiRow>> COLUMNS = List.of(
            HtmlTableColumn.text("Profile", row -> row.spec().profile()),
            HtmlTableColumn.text("Category", row -> row.spec().category()),
            HtmlTableColumn.text("Severity", row -> row.spec().severity()),
            HtmlTableColumn.code("KPI", row -> row.spec().id()),
            HtmlTableColumn.text("Expected", row -> String.valueOf(row.expected())),
            HtmlTableColumn.text("Declared", SourceEvolutionKpiRow::declaredText),
            HtmlTableColumn.status("Status", SourceEvolutionKpiRow::status),
            HtmlTableColumn.text("First blocker", SourceEvolutionKpiRow::firstBlocker),
            HtmlTableColumn.text("Why", row -> row.spec().why()),
            HtmlTableColumn.text("Quality meaning", row -> row.spec().qualityMeaning()),
            HtmlTableColumn.code("Observed artifact", row -> row.spec().repairTargetModel().observedArtifact()),
            HtmlTableColumn.text("Formula", row -> row.spec().formula()),
            HtmlTableColumn.code("Primary repair artifact", row -> row.spec().repairTargetModel().primaryRepairArtifact()),
            HtmlTableColumn.code("Supporting artifact", row -> row.spec().repairTargetModel().supportingArtifact()),
            HtmlTableColumn.text("Route", row -> row.spec().repairTargetModel().route()),
            HtmlTableColumn.text("Agent action", row -> row.spec().agentAction()),
            HtmlTableColumn.text("Cap impact", row -> row.spec().capImpact()),
            HtmlTableColumn.text("Publishability impact", row -> row.spec().publishabilityImpact()),
            HtmlTableColumn.code("Repair command", row -> row.spec().repairCommand())
    );

    private KpiMatrixHtmlRenderer() {
    }

    public static String renderMatrix(List<SourceEvolutionKpiRow> rows) {
        HtmlTable table = HtmlTable.withHeadings(COLUMNS.stream().map(HtmlTableColumn::heading).toArray(String[]::new));
        for (SourceEvolutionKpiRow row : rows) {
            table.row(COLUMNS.stream().map(column -> column.htmlCell(row)).toArray(HtmlTable.Cell[]::new));
        }
        String html = "<h1>" + HtmlText.html(TITLE) + "</h1>"
                + "<p>" + HtmlText.html(DESCRIPTION) + "</p>"
                + table.render();
        return HtmlText.diagnosticDocument("MCP KPI Matrix", html);
    }

}
