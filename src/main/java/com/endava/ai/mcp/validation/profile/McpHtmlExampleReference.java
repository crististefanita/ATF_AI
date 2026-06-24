package com.endava.ai.mcp.validation.profile;

import com.endava.ai.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpHtmlExampleReference {
    private static final Pattern PRIMARY_REFERENCE = Pattern.compile(
            "(?is)Primary structural reference report:\\s*`([^`]+)`");
    private static final Pattern ANY_EXAMPLE_REFERENCE = Pattern.compile("`([^`]+)`");
    private static final Pattern SCORE_CARD = Pattern.compile(
            "(?is)<(?:article|div)\\s+class=[\"'][^\"']*\\bscore-card\\b[^\"']*[\"'][^>]*>\\s*<div\\s+class=[\"']label[\"']>\\s*([^<]+?)\\s*</div>");

    private final Path readme;
    private final Path report;
    private final List<String> scoreCardLabels;

    private McpHtmlExampleReference(Path readme, Path report, List<String> scoreCardLabels) {
        this.readme = readme;
        this.report = report;
        this.scoreCardLabels = List.copyOf(scoreCardLabels);
    }

    public static McpHtmlExampleReference load() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path htmlExamples = projectRoot.resolve("docs/html-ex");
        Path readme = htmlExamples.resolve("README.md");
        String readmeText = Files.readString(readme);
        String reference = primaryReference(readmeText, htmlExamples);
        if (reference == null) {
            throw new IOException("docs/html-ex/README.md does not publish a readable html-ex reference folder");
        }
        Path report = htmlExamples.resolve(reference)
                .resolve("analysis/langgraph-business-understanding.html")
                .normalize();
        if (!Files.isRegularFile(report)) {
            throw new IOException("Primary structural reference report is missing: " + report);
        }
        return new McpHtmlExampleReference(readme, report, scoreCardLabels(Files.readString(report)));
    }

    private static String primaryReference(String readmeText, Path htmlExamples) {
        Matcher explicit = PRIMARY_REFERENCE.matcher(readmeText);
        if (explicit.find()) {
            return explicit.group(1);
        }
        Matcher any = ANY_EXAMPLE_REFERENCE.matcher(readmeText);
        while (any.find()) {
            String candidate = any.group(1);
            if (Files.isRegularFile(htmlExamples.resolve(candidate)
                    .resolve("analysis/langgraph-business-understanding.html"))) {
                return candidate;
            }
        }
        return null;
    }

    public List<String> scoreCardLabels() {
        return scoreCardLabels;
    }

    public Path readme() {
        return readme;
    }

    public Path report() {
        return report;
    }

    public static List<String> scoreCardLabels(String html) {
        List<String> labels = new ArrayList<>();
        Matcher matcher = SCORE_CARD.matcher(scoreWall(html));
        while (matcher.find()) {
            labels.add(matcher.group(1).replaceAll("\\s+", " ").trim());
        }
        return labels;
    }

    public static String scoreWall(String html) {
        int scoreWall = html.indexOf("class=\"score-wall\"");
        if (scoreWall < 0) {
            scoreWall = html.indexOf("class='score-wall'");
        }
        if (scoreWall < 0) {
            return "";
        }
        int executive = html.indexOf("Executive Context", scoreWall);
        int graph = html.indexOf("Business Flow Graph", scoreWall);
        int end = executive > 0 ? executive : graph;
        return end > scoreWall ? html.substring(scoreWall, end) : html.substring(scoreWall);
    }
}
