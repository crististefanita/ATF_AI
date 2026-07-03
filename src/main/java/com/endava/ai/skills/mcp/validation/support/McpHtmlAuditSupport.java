package com.endava.ai.skills.mcp.validation.support;

import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpHtmlAuditSupport {
    private static final Pattern DETAILS_BLOCK = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary>(.*?)</summary>(.*?)</details>");
    private static final Pattern GRAPH_LEGEND = Pattern.compile(
            "(?is)<details\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bgraph-legend\\b[^\"']*[\"'][^>]*>\\s*<summary>.*?Legend.*?</summary>(.*?)</details>");

    private McpHtmlAuditSupport() {
    }

    public static List<DetailsBlock> detailsBlocks(String html) {
        Matcher matcher = DETAILS_BLOCK.matcher(html);
        List<DetailsBlock> blocks = new ArrayList<>();
        while (matcher.find()) {
            blocks.add(new DetailsBlock(
                    matcher.group(1).toLowerCase(Locale.ROOT).contains("open"),
                    stripTags(matcher.group(2)).trim(),
                    matcher.group(3)));
        }
        return blocks;
    }

    public static String detailsBody(String html, String summary) {
        return detailsBlocks(html).stream()
                .filter(block -> block.summary().contains(summary))
                .map(DetailsBlock::body)
                .findFirst()
                .orElse("");
    }

    public static String graphLegendBody(String html) {
        Matcher matcher = GRAPH_LEGEND.matcher(html);
        return matcher.find() ? matcher.group(1) : "";
    }

    public static String stripTags(String value) {
        return value.replaceAll("(?is)<[^>]+>", " ");
    }

    public static boolean isExternalOrInline(String href) {
        String lower = href.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("mailto:")
                || lower.startsWith("data:")
                || lower.startsWith("javascript:")
                || lower.startsWith("#");
    }

    public static Path resolve(Path reportDirectory, String href) {
        try {
            URI uri = URI.create(href.replace("\\", "/"));
            if (uri.getScheme() != null && uri.getScheme().length() == 1) {
                return Path.of(href).toAbsolutePath().normalize();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to filesystem resolution.
        }

        Path path = Path.of(href);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return reportDirectory.resolve(path).normalize();
    }

    public static List<Path> findHtml(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .collect(Collectors.toList());
        }
    }

    public static List<Path> extentReports(Path runRoot) throws IOException {
        Path validation = runRoot.resolve(McpRunArtifacts.VALIDATION_ROOT);
        List<Path> reports = new ArrayList<>();
        reports.addAll(findHtml(runRoot.resolve(McpRunArtifacts.REPORTS_ROOT)));
        reports.addAll(findHtml(validation).stream()
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).contains("extentreport"))
                .collect(Collectors.toList()));
        return reports.stream().distinct().collect(Collectors.toList());
    }

    @Value
    @Accessors(fluent = true)
    public static class DetailsBlock {
        boolean open;
        String summary;
        String body;
    }
}
