package com.endava.ai.skills.mcp.validation.profile.html;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpHtmlExampleReference {
    private static final List<String> REUSABLE_CLOSEOUT_ARTIFACTS = List.of(
            McpArtifactPaths.FINAL_REPORT,
            McpArtifactPaths.REPORT_CSS,
            McpArtifactPaths.PACKAGE_STATE,
            McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
            McpArtifactPaths.ROOT_DIFF,
            McpArtifactPaths.MCP_EXTENT_REPORT,
            McpArtifactPaths.MCP_EXTENT_SHA256,
            McpArtifactPaths.MCP_EXTENT_ORIGIN,
            McpArtifactPaths.MCP_GATE_RESULT,
            McpArtifactPaths.MCP_METHOD_LEDGER,
            McpArtifactPaths.MCP_TESTNG_XML,
            McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML);
    private static final List<String> REUSABLE_REPORT_TOKENS = List.of(
            "class=\"score-wall\"",
            "Executive Context",
            "Business Flow Graph",
            "Score Overview",
            "Proof Mapping",
            "Run Audit",
            "MCP Gate References",
            "Artifact Index",
            "Close-Out",
            "shared-report-reference.css");
    private static final Pattern SCORE_CARD = Pattern.compile(
            "(?is)<(?:article|div)\\s+class=[\"'][^\"']*\\bscore-card\\b[^\"']*[\"'][^>]*>\\s*<div\\s+class=[\"']label[\"']>\\s*([^<]+?)\\s*</div>");

    private final Path readme;
    private final Path root;
    private final Path report;
    private final String exampleName;
    private final List<String> scoreCardLabels;

    private McpHtmlExampleReference(Path readme,
                                    Path root,
                                    Path report,
                                    String exampleName,
                                    List<String> scoreCardLabels) {
        this.readme = readme;
        this.root = root;
        this.report = report;
        this.exampleName = exampleName;
        this.scoreCardLabels = List.copyOf(scoreCardLabels);
    }

    public static McpHtmlExampleReference load() throws IOException {
        return McpHtmlExampleReferenceCorpus.load().primaryReference();
    }

    public boolean hasReusableCloseoutSpine() {
        return REUSABLE_CLOSEOUT_ARTIFACTS.stream()
                .map(root::resolve)
                .allMatch(Files::isRegularFile);
    }

    public boolean hasReusableReportShape() {
        try {
            String html = Files.readString(report);
            return REUSABLE_REPORT_TOKENS.stream().allMatch(html::contains)
                    && !scoreCardLabels(html).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> missingReusableCloseoutArtifacts() {
        return REUSABLE_CLOSEOUT_ARTIFACTS.stream()
                .filter(relative -> !Files.isRegularFile(root.resolve(relative)))
                .collect(Collectors.toList());
    }

    public List<String> missingReusableReportTokens() throws IOException {
        String html = Files.readString(report);
        return REUSABLE_REPORT_TOKENS.stream()
                .filter(token -> !html.contains(token))
                .collect(Collectors.toList());
    }

    static McpHtmlExampleReference fromExample(Path libraryReadme, Path exampleRoot) throws IOException {
        Path report = exampleRoot.resolve(McpArtifactPaths.FINAL_REPORT).normalize();
        if (!Files.isRegularFile(report)) {
            throw new IOException("html-ex structural reference report is missing: " + report);
        }
        return new McpHtmlExampleReference(
                libraryReadme,
                exampleRoot.normalize(),
                report,
                exampleRoot.getFileName().toString(),
                scoreCardLabels(Files.readString(report)));
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
