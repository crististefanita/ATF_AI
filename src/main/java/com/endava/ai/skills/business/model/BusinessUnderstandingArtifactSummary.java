package com.endava.ai.skills.business.model;

import com.endava.ai.skills.business.taxonomy.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Input-agnostic semantic summary extracted from a source, diff or report artifact.
 */
public class BusinessUnderstandingArtifactSummary {
    private final BusinessUnderstandingInputType inputType;
    private final String sourceName;
    private final String visibleText;
    private final List<String> sections;
    private final List<String> links;
    private final List<Integer> scoreValues;
    private final Set<BusinessUnderstandingConcept> concepts;

    public BusinessUnderstandingArtifactSummary(BusinessUnderstandingInputType inputType,
                                         String sourceName,
                                         String visibleText,
                                         List<String> sections,
                                         List<String> links,
                                         List<Integer> scoreValues,
                                         Set<BusinessUnderstandingConcept> concepts) {
        this.inputType = inputType;
        this.sourceName = sourceName;
        this.visibleText = visibleText;
        this.sections = List.copyOf(sections);
        this.links = List.copyOf(links);
        this.scoreValues = List.copyOf(scoreValues);
        this.concepts = Set.copyOf(concepts);
    }

    public BusinessUnderstandingInputType inputType() {
        return inputType;
    }

    public String sourceName() {
        return sourceName;
    }

    public String visibleText() {
        return visibleText;
    }

    public List<String> sections() {
        return sections;
    }

    public List<String> links() {
        return links;
    }

    public List<Integer> scoreValues() {
        return scoreValues;
    }

    public Set<BusinessUnderstandingConcept> concepts() {
        return concepts;
    }

    public boolean hasSectionContaining(String token) {
        String expected = token.toLowerCase(Locale.ROOT);
        return sections.stream().anyMatch(section -> section.toLowerCase(Locale.ROOT).contains(expected));
    }

    public boolean hasLinkContaining(String token) {
        String expected = token.toLowerCase(Locale.ROOT);
        return links.stream().anyMatch(link -> link.toLowerCase(Locale.ROOT).contains(expected));
    }

    public boolean hasConcept(BusinessUnderstandingConcept concept) {
        return concepts.contains(concept);
    }

    public boolean hasConcepts(Set<BusinessUnderstandingConcept> expectedConcepts) {
        return concepts.containsAll(expectedConcepts);
    }

    public boolean hasPublishableScoreBand() {
        return !scoreValues.isEmpty()
                && scoreValues.stream().allMatch(score -> score >= 0 && score <= 100);
    }

    public boolean looksLikeMcpFinalBusinessReport() {
        return inputType == BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT
                && hasSectionContaining("Business Flow")
                && hasSectionContaining("Score")
                && hasSectionContaining("Proof")
                && hasConcepts(Set.of(
                BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                BusinessUnderstandingConcept.PROOF_CHAIN,
                BusinessUnderstandingConcept.SCORE_RATIONALE));
    }

    public boolean looksLikeAtlasBusinessAudit() {
        return inputType == BusinessUnderstandingInputType.ATLAS_REPORT
                && hasSectionContaining("Business Flow")
                && hasConcepts(Set.of(
                BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                BusinessUnderstandingConcept.SOURCE_TRACE,
                BusinessUnderstandingConcept.SCORE_RATIONALE));
    }
}
