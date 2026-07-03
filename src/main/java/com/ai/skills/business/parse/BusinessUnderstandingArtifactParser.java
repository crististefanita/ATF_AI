package com.ai.skills.business.parse;

import com.ai.skills.business.taxonomy.*;

import com.ai.skills.business.model.*;

import java.util.List;
import java.util.Objects;

/**
 * Neutral facade for reducing artifacts to shared business semantics.
 *
 * <p>The parser normalizes input and delegates extraction/detection. MCP and
 * Atlas still own publishability, artifact ownership and score decisions.</p>
 */
public final class BusinessUnderstandingArtifactParser {
    private BusinessUnderstandingArtifactParser() {
    }

    public static ArtifactSummary parseHtml(BusinessUnderstandingInputType inputType,
                                            String sourceName,
                                            String html) {
        Objects.requireNonNull(inputType, "inputType");
        String source = requireText(sourceName, "sourceName");
        String raw = requireText(html, "html");
        BusinessHtmlArtifactReducer.HtmlArtifact artifact = BusinessHtmlArtifactReducer.reduce(raw);
        return summary(
                inputType,
                source,
                artifact.visibleText(),
                artifact.sections(),
                artifact.links(),
                artifact.scoreValues()
        );
    }

    public static ArtifactSummary parseText(BusinessUnderstandingInputType inputType,
                                            String sourceName,
                                            String text) {
        Objects.requireNonNull(inputType, "inputType");
        return summary(
                inputType,
                requireText(sourceName, "sourceName"),
                BusinessHtmlArtifactReducer.normalizeText(requireText(text, "text")),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static ArtifactSummary parseSourceStructure(String sourceName, String text) {
        return parseText(BusinessUnderstandingInputType.SOURCE_STRUCTURE, sourceName, text);
    }

    public static ArtifactSummary parseGitDiff(String sourceName, String text) {
        return parseText(BusinessUnderstandingInputType.GIT_DIFF, sourceName, text);
    }

    public static ArtifactSummary parseExtentReport(String sourceName, String html) {
        return parseHtml(BusinessUnderstandingInputType.EXTENT_REPORT, sourceName, html);
    }

    public static ArtifactSummary parseFinalBusinessReport(String sourceName, String html) {
        return parseHtml(BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT, sourceName, html);
    }

    public static ArtifactSummary parseAtlasReport(String sourceName, String html) {
        return parseHtml(BusinessUnderstandingInputType.ATLAS_REPORT, sourceName, html);
    }

    private static ArtifactSummary summary(BusinessUnderstandingInputType inputType,
                                           String sourceName,
                                           String visibleText,
                                           List<String> sections,
                                           List<String> links,
                                           List<Integer> scoreValues) {
        return new ArtifactSummary(
                inputType,
                sourceName,
                visibleText,
                sections,
                links,
                scoreValues,
                BusinessConceptDetector.detect(visibleText)
        );
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return text;
    }

    /**
     * Compatibility type kept for existing MCP/Atlas consumers.
     */
    public static final class ArtifactSummary extends BusinessUnderstandingArtifactSummary {
        private ArtifactSummary(BusinessUnderstandingInputType inputType,
                                String sourceName,
                                String visibleText,
                                List<String> sections,
                                List<String> links,
                                List<Integer> scoreValues,
                                java.util.Set<BusinessUnderstandingConcept> concepts) {
            super(inputType, sourceName, visibleText, sections, links, scoreValues, concepts);
        }
    }
}
