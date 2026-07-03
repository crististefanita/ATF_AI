package com.endava.ai.core.support.html;

import java.util.Locale;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Shared column schema for diagnostic tables rendered as HTML and Markdown.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
public final class HtmlTableColumn<T> {
    @Getter
    private final String heading;
    private final Function<T, String> value;
    private final CellKind cellKind;
    private final boolean numeric;

    public static <T> HtmlTableColumn<T> text(String heading, Function<T, String> value) {
        return new HtmlTableColumn<>(heading, value, CellKind.TEXT, false);
    }

    public static <T> HtmlTableColumn<T> code(String heading, Function<T, String> value) {
        return new HtmlTableColumn<>(heading, value, CellKind.CODE, false);
    }

    public static <T> HtmlTableColumn<T> status(String heading, Function<T, String> value) {
        return new HtmlTableColumn<>(heading, value, CellKind.STATUS, false);
    }

    public static <T> HtmlTableColumn<T> numeric(String heading, Function<T, String> value) {
        return new HtmlTableColumn<>(heading, value, CellKind.TEXT, true);
    }

    public String markdownValue(T row) {
        return HtmlText.markdownCell(value(row));
    }

    public String markdownSeparator() {
        return numeric ? "---:" : "---";
    }

    public HtmlTable.Cell htmlCell(T row) {
        String rendered = value(row);
        if (cellKind == CellKind.CODE) {
            return HtmlTable.Cell.code(rendered);
        }
        if (cellKind == CellKind.STATUS) {
            return HtmlTable.Cell.classed(rendered.toLowerCase(Locale.ROOT), rendered);
        }
        return HtmlTable.Cell.text(rendered);
    }

    private String value(T row) {
        String rendered = value.apply(row);
        return rendered == null ? "" : rendered;
    }

    private enum CellKind {
        TEXT,
        CODE,
        STATUS
    }
}
