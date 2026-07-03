package com.endava.ai.skills.atlas.validation;

import com.endava.ai.core.support.html.HtmlTable;
import com.endava.ai.core.support.html.HtmlTableColumn;
import com.endava.ai.core.support.html.HtmlText;

import java.util.List;
import java.util.stream.Collectors;

final class AtlasRuleMatrixRenderer {
    private static final String TITLE = "Atlas Rule Matrix";
    private static final String DESCRIPTION = "Generated from executable Atlas validator rules. "
            + "Repair targets point to the first artifact an agent should inspect or update.";
    private static final List<HtmlTableColumn<AtlasRuleMatrixRow>> COLUMNS = List.of(
            HtmlTableColumn.text("Profile", row -> row.profile().name()),
            HtmlTableColumn.text("Category", row -> row.spec().category().name()),
            HtmlTableColumn.text("Rule severity", row -> row.spec().severity().name()),
            HtmlTableColumn.text("Policy severity", row -> row.policy().severity().name()),
            HtmlTableColumn.code("Rule", row -> row.spec().id()),
            HtmlTableColumn.status("Status", AtlasRuleMatrixRow::status),
            HtmlTableColumn.numeric("Blockers", row -> String.valueOf(row.result().violations().size())),
            HtmlTableColumn.text("Owner", row -> row.policy().owner()),
            HtmlTableColumn.code("Observed artifact", row -> row.repairTarget().observedArtifact().toString()),
            HtmlTableColumn.code("Runtime repair target", row -> row.repairTarget().primaryRepairArtifact().toString()),
            HtmlTableColumn.text("Policy repair target", row -> row.policy().repairTarget()),
            HtmlTableColumn.text("Policy matrix guidance", row -> row.policy().matrix()),
            HtmlTableColumn.text("Cap impact", row -> row.policy().capImpact()),
            HtmlTableColumn.text("Why", row -> row.spec().why()),
            HtmlTableColumn.text("Repair", row -> row.spec().repair()),
            HtmlTableColumn.text("First blocker", AtlasRuleMatrixRow::firstBlocker)
    );

    private AtlasRuleMatrixRenderer() {
    }

    static String toMarkdown(List<AtlasRuleMatrixRow> rows) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(TITLE).append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append(DESCRIPTION)
                .append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append(markdownHeader()).append(System.lineSeparator());
        markdown.append(markdownSeparator()).append(System.lineSeparator());
        for (AtlasRuleMatrixRow row : rows) {
            markdown.append(markdownRow(row)).append(System.lineSeparator());
        }
        return markdown.toString();
    }

    static String toHtml(List<AtlasRuleMatrixRow> rows) {
        HtmlTable table = HtmlTable.withHeadings(COLUMNS.stream().map(HtmlTableColumn::heading).toArray(String[]::new));
        for (AtlasRuleMatrixRow row : rows) {
            table.row(COLUMNS.stream().map(column -> column.htmlCell(row)).toArray(HtmlTable.Cell[]::new));
        }
        String html = "<h1>" + HtmlText.html(TITLE) + "</h1>"
                + "<p>" + HtmlText.html(DESCRIPTION) + "</p>"
                + table.render();
        return HtmlText.diagnosticDocument(TITLE, html);
    }

    private static String markdownHeader() {
        return COLUMNS.stream()
                .map(HtmlTableColumn::heading)
                .map(AtlasRuleMatrixRenderer::escapeMarkdown)
                .collect(Collectors.joining(" | ", "| ", " |"));
    }

    private static String markdownSeparator() {
        return COLUMNS.stream()
                .map(HtmlTableColumn::markdownSeparator)
                .collect(Collectors.joining("|", "|", "|"));
    }

    private static String markdownRow(AtlasRuleMatrixRow row) {
        return COLUMNS.stream()
                .map(column -> column.markdownValue(row))
                .collect(Collectors.joining(" | ", "| ", " |"));
    }

    private static String escapeMarkdown(String value) {
        return HtmlText.markdownCell(value);
    }
}
