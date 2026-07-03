package com.ai.skills.mcp.validation.run.extent.proof;

import java.util.Locale;

public enum McpExtentProofClass {
    SPARK("spark"),
    STANDALONE("standalone"),
    SYNTHETIC("synthetic");

    private final String label;

    McpExtentProofClass(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static McpExtentProofClass fromHtml(String html) {
        String lower = html == null ? "" : html.toLowerCase(Locale.ROOT);
        if (lower.contains("spark-style.css") || lower.contains("cdn.jsdelivr") || lower.contains("extent.spark")) {
            return SPARK;
        }
        if (lower.contains("spark-replacement") || lower.contains("single-file-proof")
                || lower.contains("frozen audit view from executed xml")) {
            return SYNTHETIC;
        }
        if (lower.contains("extent-framework") || lower.contains("extent report") || lower.contains("extentreports")) {
            return STANDALONE;
        }
        return SYNTHETIC;
    }
}
