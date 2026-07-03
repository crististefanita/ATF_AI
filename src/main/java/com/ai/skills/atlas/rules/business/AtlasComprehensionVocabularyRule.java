package com.ai.skills.atlas.rules.business;

import com.ai.skills.business.parse.BusinessUnderstandingArtifactParser;
import com.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

import java.util.List;
import java.util.Set;

public final class AtlasComprehensionVocabularyRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_TERMS = List.of(
            "business object coverage",
            "source-to-business trace",
            "repeated business corridors",
            "comprehension limits");

    private static final List<String> LEGACY_TERMS = List.of(
            "coverage matrix",
            "test-to-business mapping",
            "repetition ledger",
            "observed gaps graph",
            "next frontier graph");

    @Override
    public String id() {
        return "atlas.candidate.vocabulary.business-comprehension";
    }

    @Override
    public String expected() {
        return "Atlas uses business-comprehension vocabulary, not planning/readiness/coverage-first vocabulary.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseHtml(
                        BusinessUnderstandingInputType.ATLAS_REPORT,
                        context.atlasHtml().toString(),
                        context.atlasText());
        if (!summary.hasConcepts(Set.of(
                BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                BusinessUnderstandingConcept.SOURCE_TRACE,
                BusinessUnderstandingConcept.UNDERSTANDING_LIMIT))) {
            result.add(id(), "Atlas report must reduce to shared Business Understanding semantics: "
                            + "business flow story, source trace and understanding limits",
                    context.atlasHtml(),
                    "Use the shared Business Understanding vocabulary while keeping Atlas as the owner of "
                            + "source-backed business audit rules.");
        }
        for (String term : REQUIRED_TERMS) {
            if (!html.contains(term)) {
                result.add(id(), "Missing canonical comprehension term: " + term,
                        context.atlasHtml(),
                        "Use the vocabulary from business-test-atlas-reference.html: Business Object Coverage, Source-to-Business Trace, Repeated Business Corridors, Comprehension Limits.");
            }
        }
        for (String term : LEGACY_TERMS) {
            if (html.contains(term)) {
                result.add(id(), "Legacy planning/coverage term still present: " + term,
                        context.atlasHtml(),
                        "Rename legacy sections so the atlas reads as business comprehension, not coverage/planning/readiness.");
            }
        }
    }
}
