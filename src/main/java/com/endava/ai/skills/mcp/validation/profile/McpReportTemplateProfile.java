package com.endava.ai.skills.mcp.validation.profile;

import lombok.RequiredArgsConstructor;

import com.endava.ai.skills.mcp.validation.profile.html.McpHtmlExampleReference;
import com.endava.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpReportTemplateProfile {
    private final McpReportTemplateModel model;

    public static McpReportTemplateProfile primaryReference() throws IOException {
        McpHtmlExampleReference reference = McpHtmlExampleReference.load();
        return fromHtml(Files.readString(reference.report()));
    }

    public static McpReportTemplateConsensus htmlExConsensus() throws IOException {
        return McpReportTemplateConsensus.fromHtmlExCorpus();
    }

    public static McpReportTemplateProfile fromHtml(String html) {
        return new McpReportTemplateProfile(McpReportTemplateModel.fromHtml(html));
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
