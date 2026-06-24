package com.endava.ai.mcp.validation.profile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpReportTemplateModel {
    private static final List<String> OPENING_MARKERS = List.of(
            "class=\"hero\"",
            "class=\"score-wall\"",
            McpReportSections.EXECUTIVE_CONTEXT,
            McpReportSections.BUSINESS_FLOW_GRAPH);
    private static final List<String> RUN_AUDIT_EVIDENCE_TOKENS = List.of(
            "thin-no-index.diff",
            "ExtentReport.html",
            "testng-results.xml",
            "run-state.json",
            "command-result.md");
    private static final List<String> ARTIFACT_INDEX_TOKENS = List.of(
            "package-state.json",
            "execution-gate-card.md",
            "root-source-thin-no-index.diff",
            "canonical-render-check.png",
            "local-link-check.txt",
            "mcp-gate-result.md",
            "ExtentReport.html");
    private static final List<String> REUSABLE_VISUAL_CLASS_CANDIDATES = List.of(
            "wrap",
            "hero",
            "score-wall",
            "score-card",
            "pill-row",
            "pill",
            "executive-context",
            "graph-legend",
            "graph-legend-list",
            "score-table",
            "score-calculation-table",
            "proof-table",
            "run-audit-list",
            "artifact-links",
            "closeout-summary");
    private static final Pattern SUMMARY_TITLE = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary>.*?<span\\s+class=[\"']summary-title[\"']>\\s*([^<]+?)\\s*</span>");
    private static final Pattern TABLE_HEADER = Pattern.compile("(?is)<th[^>]*>\\s*(.*?)\\s*</th>");
    private static final Pattern CLASS_ATTRIBUTE = Pattern.compile("(?is)\\bclass\\s*=\\s*[\"']([^\"']+)[\"']");

    private final List<String> scoreCardLabels;
    private final List<String> openingSequence;
    private final List<String> visualClasses;
    private final List<String> openSections;
    private final List<String> collapsedSections;
    private final List<String> proofHeaders;
    private final List<String> runAuditEvidenceTokens;
    private final List<String> artifactIndexTokens;

    private McpReportTemplateModel(List<String> scoreCardLabels,
                                   List<String> openingSequence,
                                   List<String> visualClasses,
                                   List<String> openSections,
                                   List<String> collapsedSections,
                                   List<String> proofHeaders,
                                   List<String> runAuditEvidenceTokens,
                                   List<String> artifactIndexTokens) {
        this.scoreCardLabels = List.copyOf(scoreCardLabels);
        this.openingSequence = List.copyOf(openingSequence);
        this.visualClasses = List.copyOf(visualClasses);
        this.openSections = List.copyOf(openSections);
        this.collapsedSections = List.copyOf(collapsedSections);
        this.proofHeaders = List.copyOf(proofHeaders);
        this.runAuditEvidenceTokens = List.copyOf(runAuditEvidenceTokens);
        this.artifactIndexTokens = List.copyOf(artifactIndexTokens);
    }

    public static McpReportTemplateModel fromHtml(String html) {
        List<String> open = new ArrayList<>();
        List<String> collapsed = new ArrayList<>();
        Matcher summaries = SUMMARY_TITLE.matcher(html);
        while (summaries.find()) {
            String title = normalize(summaries.group(2));
            if (!McpReportSections.TEMPLATE_ORDER.contains(title)) {
                continue;
            }
            if (summaries.group(1).toLowerCase(Locale.ROOT).contains("open")) {
                open.add(title);
            } else {
                collapsed.add(title);
            }
        }
        return new McpReportTemplateModel(
                McpHtmlExampleReference.scoreCardLabels(html),
                presentOpeningMarkers(html),
                presentClasses(html, REUSABLE_VISUAL_CLASS_CANDIDATES),
                open,
                collapsed,
                tableHeaders(section(html, McpReportSections.PROOF_MAPPING)),
                presentTokens(section(html, McpReportSections.RUN_AUDIT), RUN_AUDIT_EVIDENCE_TOKENS),
                presentTokens(section(html, McpReportSections.ARTIFACT_INDEX), ARTIFACT_INDEX_TOKENS));
    }

    public List<String> scoreCardLabels() {
        return scoreCardLabels;
    }

    public List<String> openingSequence() {
        return openingSequence;
    }

    public List<String> visualClasses() {
        return visualClasses;
    }

    public List<String> openSections() {
        return openSections;
    }

    public List<String> collapsedSections() {
        return collapsedSections;
    }

    public List<String> proofHeaders() {
        return proofHeaders;
    }

    public List<String> runAuditEvidenceTokens() {
        return runAuditEvidenceTokens;
    }

    public List<String> artifactIndexTokens() {
        return artifactIndexTokens;
    }

    private static List<String> presentOpeningMarkers(String html) {
        List<String> markers = new ArrayList<>();
        for (String marker : OPENING_MARKERS) {
            if (html.contains(marker)) {
                markers.add(marker);
            }
        }
        return markers;
    }

    private static List<String> tableHeaders(String html) {
        Set<String> headers = new LinkedHashSet<>();
        Matcher matcher = TABLE_HEADER.matcher(html);
        while (matcher.find()) {
            headers.add(normalize(stripTags(matcher.group(1))));
        }
        return new ArrayList<>(headers);
    }

    private static List<String> presentTokens(String html, List<String> candidates) {
        List<String> tokens = new ArrayList<>();
        for (String candidate : candidates) {
            if (html.contains(candidate)) {
                tokens.add(candidate);
            }
        }
        return tokens;
    }

    private static List<String> presentClasses(String html, List<String> candidates) {
        Set<String> classes = new LinkedHashSet<>();
        Matcher matcher = CLASS_ATTRIBUTE.matcher(html);
        while (matcher.find()) {
            for (String token : matcher.group(1).split("\\s+")) {
                classes.add(token.trim());
            }
        }
        List<String> present = new ArrayList<>();
        for (String candidate : candidates) {
            if (classes.contains(candidate)) {
                present.add(candidate);
            }
        }
        return present;
    }

    private static String section(String html, String summaryTitle) {
        int start = html.indexOf("<span class=\"summary-title\">" + summaryTitle + "</span>");
        if (start < 0) {
            start = html.indexOf(summaryTitle);
        }
        if (start < 0) {
            return "";
        }
        int next = html.indexOf("<span class=\"summary-title\">", start + summaryTitle.length());
        return next > start ? html.substring(start, next) : html.substring(start);
    }

    private static String stripTags(String value) {
        return value.replaceAll("(?is)<[^>]+>", " ");
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
