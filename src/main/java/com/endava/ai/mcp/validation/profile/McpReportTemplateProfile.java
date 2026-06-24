package com.endava.ai.mcp.validation.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class McpReportTemplateProfile {
    private final McpReportTemplateModel model;

    private McpReportTemplateProfile(McpReportTemplateModel model) {
        this.model = model;
    }

    public static McpReportTemplateProfile primaryReference() throws IOException {
        McpHtmlExampleReference reference = McpHtmlExampleReference.load();
        return fromHtml(Files.readString(reference.report()));
    }

    public static McpReportTemplateProfile fromHtml(String html) {
        return new McpReportTemplateProfile(McpReportTemplateModel.fromHtml(html));
    }

    public McpReportTemplateModel model() {
        return model;
    }

    public List<String> scoreCardLabels() {
        return model.scoreCardLabels();
    }

    public List<String> openingSequence() {
        return model.openingSequence();
    }

    public List<String> visualClasses() {
        return model.visualClasses();
    }

    public List<String> openSections() {
        return model.openSections();
    }

    public List<String> collapsedSections() {
        return model.collapsedSections();
    }

    public List<String> proofHeaders() {
        return model.proofHeaders();
    }

    public List<String> runAuditEvidenceTokens() {
        return model.runAuditEvidenceTokens();
    }

    public List<String> artifactIndexTokens() {
        return model.artifactIndexTokens();
    }
}
