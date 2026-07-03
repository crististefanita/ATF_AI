package com.endava.ai.skills.mcp.validation.report.model;

import com.endava.ai.skills.mcp.validation.profile.McpReportSections;

import java.util.List;
import java.util.Locale;

public final class ReportOpeningModel {
    private final String html;
    private final String beforeExecutiveContext;

    private ReportOpeningModel(String html) {
        this.html = html == null ? "" : html;
        int executiveContext = firstIndexOf(
                this.html,
                "id=\"executive-context\"",
                "id='executive-context'",
                McpReportSections.EXECUTIVE_CONTEXT
        );
        this.beforeExecutiveContext = executiveContext < 0 ? "" : this.html.substring(0, executiveContext);
    }

    public static ReportOpeningModel fromHtml(String html) {
        return new ReportOpeningModel(html);
    }

    public boolean hasScoreWall() {
        return html.contains("class=\"score-wall\"") || html.contains("class='score-wall'");
    }

    public boolean hasScoreCard() {
        return html.contains("class=\"score-card\"") || html.contains("class='score-card'");
    }

    public boolean hasAnyContractArtifactLabel(List<String> labels) {
        return containsAny(html, labels);
    }

    public boolean hasExecutiveContextClass() {
        return html.contains("executive-context");
    }

    public boolean hasExecutiveContextLabel() {
        return html.contains(McpReportSections.EXECUTIVE_CONTEXT);
    }

    public boolean hasAnyExecutiveEvidence(List<String> evidenceTokens) {
        return containsAny(html.toLowerCase(Locale.ROOT), evidenceTokens);
    }

    public boolean scoreWallBeforeExecutiveContextBeforeBusinessGraph() {
        int scoreWall = html.indexOf("score-wall");
        int executive = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
        int graph = html.indexOf(McpReportSections.BUSINESS_FLOW_GRAPH);
        return scoreWall >= 0 && executive >= 0 && graph >= 0
                && scoreWall < executive
                && executive < graph;
    }

    public boolean hasRunSliceIndexBeforeExecutiveContext() {
        return beforeExecutiveContext.contains("run-slice-index");
    }

    public boolean hasRunClaimSliceHeaderBeforeExecutiveContext() {
        String compact = stripTags(beforeExecutiveContext)
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
        return compact.contains("run claim slice");
    }

    public boolean hasVisiblePillRowBeforeExecutiveContext() {
        return beforeExecutiveContext.contains("class=\"pill-row\"")
                || beforeExecutiveContext.contains("class='pill-row'");
    }

    public boolean hasVisiblePillBeforeExecutiveContext() {
        return beforeExecutiveContext.contains("class=\"pill\"")
                || beforeExecutiveContext.contains("class='pill'");
    }

    public boolean hasEscapedNewlineArtifact() {
        return html.contains("`n");
    }

    private static boolean containsAny(String source, List<String> tokens) {
        for (String token : tokens) {
            if (source.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static int firstIndexOf(String source, String... markers) {
        int first = -1;
        for (String marker : markers) {
            int index = source.indexOf(marker);
            if (index >= 0 && (first < 0 || index < first)) {
                first = index;
            }
        }
        return first;
    }

    private static String stripTags(String value) {
        return value.replaceAll("(?is)<[^>]+>", " ");
    }
}
