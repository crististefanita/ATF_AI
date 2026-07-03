package com.endava.ai.core.support.html;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Tiny table builder for generated diagnostic HTML artifacts.
 */
public final class HtmlTable {
    private final StringBuilder html = new StringBuilder();
    private final int columnCount;

    private HtmlTable(List<String> headings) {
        if (headings.isEmpty()) {
            throw new IllegalArgumentException("HTML table must declare at least one heading.");
        }
        this.columnCount = headings.size();
        html.append("<table><thead><tr>");
        for (String heading : headings) {
            html.append("<th>").append(HtmlText.html(heading)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
    }

    public static HtmlTable withHeadings(String... headings) {
        return new HtmlTable(List.of(headings));
    }

    public HtmlTable row(String... cells) {
        requireColumnCount(cells);
        Cell[] rendered = new Cell[cells.length];
        for (int index = 0; index < cells.length; index++) {
            rendered[index] = Cell.text(cells[index]);
        }
        return row(rendered);
    }

    public HtmlTable row(Cell... cells) {
        requireColumnCount(cells);
        html.append("<tr>");
        for (Cell cell : cells) {
            html.append(cell.render());
        }
        html.append("</tr>");
        return this;
    }

    public HtmlTable codeRow(String... cells) {
        requireColumnCount(cells);
        Cell[] rendered = new Cell[cells.length];
        for (int index = 0; index < cells.length; index++) {
            rendered[index] = Cell.code(cells[index]);
        }
        return row(rendered);
    }

    public String render() {
        return html + "</tbody></table>";
    }

    private void requireColumnCount(String[] cells) {
        if (cells.length != columnCount) {
            throw new IllegalArgumentException("HTML table row has " + cells.length
                    + " cells but table declares " + columnCount + " headings.");
        }
    }

    private void requireColumnCount(Cell[] cells) {
        if (cells.length != columnCount) {
            throw new IllegalArgumentException("HTML table row has " + cells.length
                    + " cells but table declares " + columnCount + " headings.");
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Cell {
        private final String value;
        private final boolean code;
        private final String cssClass;

        public static Cell text(String value) {
            return new Cell(value, false, "");
        }

        public static Cell code(String value) {
            return new Cell(value, true, "");
        }

        public static Cell classed(String cssClass, String value) {
            return new Cell(value, false, cssClass);
        }

        private String render() {
            String classAttribute = cssClass == null || cssClass.isBlank()
                    ? ""
                    : " class=\"" + HtmlText.html(cssClass) + "\"";
            String content = HtmlText.html(value);
            if (code) {
                content = "<code>" + content + "</code>";
            }
            return "<td" + classAttribute + ">" + content + "</td>";
        }
    }
}
