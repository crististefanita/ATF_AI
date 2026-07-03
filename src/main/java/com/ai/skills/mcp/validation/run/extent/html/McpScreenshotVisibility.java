package com.ai.skills.mcp.validation.run.extent.html;

import java.util.Locale;

public final class McpScreenshotVisibility {
    private McpScreenshotVisibility() {
    }

    public static boolean isHidden(String tag, String html, int start) {
        String lowerTag = tag.toLowerCase(Locale.ROOT);
        if (containsHiddenStyle(lowerTag)) {
            return true;
        }
        String prefix = html.substring(Math.max(0, start - 700), start).toLowerCase(Locale.ROOT);
        return hasOpenHiddenContainer(prefix, "section")
                || hasOpenHiddenContainer(prefix, "div")
                || hasOpenHiddenContainer(prefix, "figure");
    }

    private static boolean hasOpenHiddenContainer(String prefix, String tagName) {
        int open = prefix.lastIndexOf("<" + tagName);
        if (open < 0 || open < prefix.lastIndexOf("</" + tagName)) {
            return false;
        }
        return containsHiddenStyle(prefix.substring(open));
    }

    private static boolean containsHiddenStyle(String value) {
        return value.contains("display:none")
                || value.contains("display: none")
                || value.contains("visibility:hidden")
                || value.contains("visibility: hidden")
                || value.contains(" hidden");
    }
}
