package com.endava.ai.skills.mcp.validation.report.model;

import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.support.McpHtmlAuditSupport;

import java.util.ArrayList;
import java.util.List;

public final class ArtifactIndexModel {
    private final String sectionHtml;

    private ArtifactIndexModel(String sectionHtml) {
        this.sectionHtml = sectionHtml == null ? "" : sectionHtml;
    }

    public static ArtifactIndexModel fromHtml(String html) {
        return new ArtifactIndexModel(McpHtmlAuditSupport.detailsBody(html, McpReportSections.ARTIFACT_INDEX));
    }

    public List<String> presentTokens(List<String> candidates) {
        List<String> tokens = new ArrayList<>();
        for (String candidate : candidates) {
            if (sectionHtml.contains(candidate)) {
                tokens.add(candidate);
            }
        }
        return tokens;
    }
}
