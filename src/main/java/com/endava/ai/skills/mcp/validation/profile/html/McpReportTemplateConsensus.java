package com.endava.ai.skills.mcp.validation.profile.html;

import com.endava.ai.skills.mcp.validation.profile.McpReportTemplateProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpReportTemplateConsensus {
    private final McpHtmlExampleReferenceCorpus corpus;
    private final McpReportTemplateProfile primary;
    private final List<McpReportTemplateProfile> profiles;

    private McpReportTemplateConsensus(McpHtmlExampleReferenceCorpus corpus,
                                       McpReportTemplateProfile primary,
                                       List<McpReportTemplateProfile> profiles) {
        this.corpus = corpus;
        this.primary = primary;
        this.profiles = List.copyOf(profiles);
    }

    public static McpReportTemplateConsensus fromHtmlExCorpus() throws IOException {
        McpHtmlExampleReferenceCorpus corpus = McpHtmlExampleReferenceCorpus.load();
        List<McpReportTemplateProfile> profiles = corpus.reusableReferences().stream()
                .map(McpReportTemplateConsensus::profile)
                .collect(Collectors.toUnmodifiableList());
        McpReportTemplateProfile primary = profile(corpus.primaryReference());
        return new McpReportTemplateConsensus(corpus, primary, profiles);
    }

    public List<String> scoreCardLabels() {
        return commonInPrimaryOrder(McpReportTemplateProfile::scoreCardLabels);
    }

    public List<String> openingSequence() {
        return commonInPrimaryOrder(McpReportTemplateProfile::openingSequence);
    }

    public List<String> visualClasses() {
        return commonInPrimaryOrder(McpReportTemplateProfile::visualClasses);
    }

    public List<String> openSections() {
        return commonInPrimaryOrder(McpReportTemplateProfile::openSections);
    }

    public List<String> collapsedSections() {
        return commonInPrimaryOrder(McpReportTemplateProfile::collapsedSections);
    }

    public List<String> proofHeaders() {
        return commonInPrimaryOrder(McpReportTemplateProfile::proofHeaders);
    }

    public List<String> runAuditEvidenceTokens() {
        return commonInPrimaryOrder(McpReportTemplateProfile::runAuditEvidenceTokens);
    }

    public List<String> artifactIndexTokens() {
        return commonInPrimaryOrder(McpReportTemplateProfile::artifactIndexTokens);
    }

    public boolean scoreCardLabelsStable() {
        List<String> expected = primary.scoreCardLabels();
        return profiles.stream().allMatch(profile -> expected.equals(profile.scoreCardLabels()));
    }

    public List<String> sources() {
        return corpus.reusableReferences().stream()
                .map(McpHtmlExampleReference::exampleName)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> commonInPrimaryOrder(Function<McpReportTemplateProfile, List<String>> extractor) {
        List<String> baseline = extractor.apply(primary);
        return baseline.stream()
                .filter(token -> profiles.stream().allMatch(profile -> extractor.apply(profile).contains(token)))
                .collect(Collectors.toUnmodifiableList());
    }

    private static McpReportTemplateProfile profile(McpHtmlExampleReference reference) {
        try {
            return McpReportTemplateProfile.fromHtml(Files.readString(reference.report()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read html-ex report: " + reference.report(), e);
        }
    }
}
