package com.endava.ai.core.support.html;

/**
 * Small shared HTML/Markdown escaping helpers for generated contract artifacts.
 */
public final class HtmlText {
    private HtmlText() {
    }

    public static String html(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String markdownCell(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|")
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String document(String title, String css, String body) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>"
                + html(title)
                + "</title><style>"
                + (css == null ? "" : css)
                + "</style></head><body>"
                + (body == null ? "" : body)
                + "</body></html>";
    }

    public static String diagnosticDocument(String title, String body) {
        return document(title, diagnosticCss(), body);
    }

    public static String diagnosticCss() {
        return "body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#172033;line-height:1.45}"
                + "section{margin:0 0 24px}"
                + "table{border-collapse:collapse;width:100%;font-size:13px}"
                + "th,td{border:1px solid #cbd5e1;padding:8px;vertical-align:top}"
                + "th{background:#eef2f7}"
                + ".pass{color:#166534;font-weight:700}"
                + ".fail{color:#991b1b;font-weight:700}"
                + ".risk{color:#b45309;font-weight:700}"
                + ".info{color:#1d4ed8;font-weight:700}"
                + "code,pre{white-space:normal;color:#334155}"
                + "pre{background:#f8fafc;border:1px solid #cbd5e1;padding:12px;overflow:auto}";
    }
}
