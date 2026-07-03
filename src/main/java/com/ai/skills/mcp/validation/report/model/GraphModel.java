package com.ai.skills.mcp.validation.report.model;

import com.ai.skills.mcp.validation.support.McpHtmlAuditSupport;

import java.util.ArrayList;
import java.util.List;

public final class GraphModel {
    private final List<McpHtmlAuditSupport.DetailsBlock> detailsBlocks;
    private final String graphSectionName;

    private GraphModel(List<McpHtmlAuditSupport.DetailsBlock> detailsBlocks, String graphSectionName) {
        this.detailsBlocks = List.copyOf(detailsBlocks);
        this.graphSectionName = graphSectionName;
    }

    public static GraphModel fromHtml(String html, String graphSectionName) {
        return new GraphModel(McpHtmlAuditSupport.detailsBlocks(html), graphSectionName);
    }

    public long openSectionCount() {
        return detailsBlocks.stream().filter(McpHtmlAuditSupport.DetailsBlock::open).count();
    }

    public boolean hasDefaultOpenBusinessGraph() {
        return detailsBlocks.stream().anyMatch(block ->
                block.open() && block.summary().contains(graphSectionName));
    }

    public List<String> missingSections(List<String> requiredSections) {
        List<String> missing = new ArrayList<>();
        for (String required : requiredSections) {
            if (detailsBlocks.stream().noneMatch(block -> block.summary().contains(required))) {
                missing.add(required);
            }
        }
        return missing;
    }

    public List<String> nonGraphOpenSections() {
        List<String> open = new ArrayList<>();
        for (McpHtmlAuditSupport.DetailsBlock block : detailsBlocks) {
            if (block.open() && !block.summary().contains(graphSectionName)) {
                open.add(block.summary());
            }
        }
        return open;
    }
}
