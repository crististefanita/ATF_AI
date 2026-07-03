package com.ai.skills.mcp.validation.run.extent;

import lombok.RequiredArgsConstructor;

import com.ai.skills.mcp.validation.run.extent.html.McpHtmlTagScanner;
import com.ai.skills.mcp.validation.run.extent.html.McpHtmlTagSnapshot;
import com.ai.skills.mcp.validation.run.extent.html.McpScreenshotVisibility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class McpExtentScreenshotEvidence {
    private static final String DATA_URI_PREFIX = "data:image/png;base64,";

    private McpExtentScreenshotEvidence() {
    }

    public static List<EmbeddedScreenshot> findEmbedded(String html) {
        List<EmbeddedScreenshot> screenshots = new ArrayList<>();
        String content = html == null ? "" : html;
        for (McpHtmlTagSnapshot tag : McpHtmlTagScanner.tags(content)) {
            if (("img".equals(tag.element()) || "a".equals(tag.element()))) {
                int dataUriLength = dataUriLength(tag);
                if (dataUriLength > 0) {
                    screenshots.add(new EmbeddedScreenshot(tag.raw(), tag.start(), tag.end(), dataUriLength));
                }
            }
        }
        return screenshots;
    }

    public static boolean containsUsefulEmbeddedScreenshot(String html) {
        return findEmbedded(html).stream()
                .anyMatch(screenshot -> screenshot.dataUriLength() >= 500);
    }

    private static int dataUriLength(McpHtmlTagSnapshot tag) {
        String src = tag.attribute("src");
        if (startsWithDataUri(src)) {
            return src.length();
        }
        String href = tag.attribute("href");
        return startsWithDataUri(href) ? href.length() : 0;
    }

    private static boolean startsWithDataUri(String value) {
        return value.toLowerCase(Locale.ROOT).startsWith(DATA_URI_PREFIX);
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EmbeddedScreenshot {
        private final String tag;
        private final int start;
        private final int end;
        private final int dataUriLength;

        public boolean isHiddenIn(String html) {
            return McpScreenshotVisibility.isHidden(tag, html, start);
        }

        public String textContext(String html) {
            int from = Math.max(0, start - 500);
            int to = Math.min(html.length(), end + 250);
            return html.substring(from, to).replaceAll("(?is)<[^>]+>", " ");
        }
    }
}
