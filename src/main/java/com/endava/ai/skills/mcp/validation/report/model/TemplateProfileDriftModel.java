package com.endava.ai.skills.mcp.validation.report.model;

import com.endava.ai.skills.mcp.validation.profile.McpReportTemplateProfile;
import com.endava.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TemplateProfileDriftModel {
    private final String html;
    private final McpReportTemplateConsensus reference;
    private final McpReportTemplateProfile actual;
    private final ProofMappingModel proofMapping;
    private final ArtifactIndexModel artifactIndex;

    private TemplateProfileDriftModel(String html,
                                      McpReportTemplateConsensus reference,
                                      McpReportTemplateProfile actual,
                                      ProofMappingModel proofMapping,
                                      ArtifactIndexModel artifactIndex) {
        this.html = html == null ? "" : html;
        this.reference = reference;
        this.actual = actual;
        this.proofMapping = proofMapping;
        this.artifactIndex = artifactIndex;
    }

    public static TemplateProfileDriftModel fromHtml(String html) throws IOException {
        return new TemplateProfileDriftModel(
                html,
                McpReportTemplateProfile.htmlExConsensus(),
                McpReportTemplateProfile.fromHtml(html),
                ProofMappingModel.fromHtml(html),
                ArtifactIndexModel.fromHtml(html));
    }

    public List<String> blockers() {
        List<String> blockers = new ArrayList<>();
        if (!reference.scoreCardLabels().equals(actual.scoreCardLabels())) {
            blockers.add("Score-card labels/order drift from html-ex consensus profile. Expected "
                    + reference.scoreCardLabels() + " but found " + actual.scoreCardLabels()
                    + ". Reference corpus: " + reference.sources());
        }
        if (!markersInOrder(reference.openingSequence())) {
            blockers.add("Opening sequence must follow html-ex consensus rhythm: "
                    + String.join(" -> ", reference.openingSequence())
                    + ". Reference corpus: " + reference.sources());
        }
        blockers.addAll(missing("reusable visual classes", reference.visualClasses(), actual.visualClasses()));
        blockers.addAll(missing("default-open sections", reference.openSections(), actual.openSections()));
        blockers.addAll(missing("collapsed sections", reference.collapsedSections(), actual.collapsedSections()));
        blockers.addAll(missing("Proof Mapping headers", reference.proofHeaders(), proofMapping.headers()));
        blockers.addAll(missing("Run Audit evidence tokens", reference.runAuditEvidenceTokens(), actual.runAuditEvidenceTokens()));
        blockers.addAll(missing(
                "Artifact Index evidence tokens",
                reference.artifactIndexTokens(),
                artifactIndex.presentTokens(reference.artifactIndexTokens())));
        return blockers;
    }

    private boolean markersInOrder(List<String> markers) {
        int cursor = -1;
        for (String marker : markers) {
            int index = html.indexOf(marker, cursor + 1);
            if (index < 0) {
                return false;
            }
            cursor = index;
        }
        return true;
    }

    private List<String> missing(String label, List<String> expected, List<String> actual) {
        List<String> blockers = new ArrayList<>();
        for (String value : expected) {
            if (!actual.contains(value)) {
                blockers.add(label + " missing value from html-ex consensus profile: " + value);
            }
        }
        return blockers;
    }
}
