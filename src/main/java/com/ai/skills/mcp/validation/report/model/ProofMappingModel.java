package com.ai.skills.mcp.validation.report.model;

import com.ai.skills.mcp.validation.profile.McpReportSections;
import com.ai.skills.mcp.validation.support.McpHtmlAuditSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProofMappingModel {
    private static final Pattern TABLE_HEADER = Pattern.compile("(?is)<th[^>]*>\\s*(.*?)\\s*</th>");

    private final String sectionHtml;

    private ProofMappingModel(String sectionHtml) {
        this.sectionHtml = sectionHtml == null ? "" : sectionHtml;
    }

    public static ProofMappingModel fromHtml(String html) {
        return new ProofMappingModel(McpHtmlAuditSupport.detailsBody(html, McpReportSections.PROOF_MAPPING));
    }

    public List<String> headers() {
        Set<String> headers = new LinkedHashSet<>();
        Matcher matcher = TABLE_HEADER.matcher(sectionHtml);
        while (matcher.find()) {
            headers.add(normalize(McpHtmlAuditSupport.stripTags(matcher.group(1))));
        }
        return new ArrayList<>(headers);
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
