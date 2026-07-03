package com.ai.skills.atlas.rules.scoring.limits;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasSuiteCompletenessSeparationRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.suite-completeness.separation";
    }

    @Override
    public String expected() {
        return "Atlas Overall is framed as a business-understanding score and keeps suite completeness/product coverage as a boundary concept.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (!mentionsOverallScore(html)) {
            result.add(id(), "Atlas does not frame Overall as the atlas score",
                    context.atlasHtml(),
                    "Name Overall as the atlas-level comprehension score, independent of wording style.");
        }
        if (!mentionsBusinessUnderstanding(html)) {
            result.add(id(), "Atlas Overall is not tied to business understanding",
                    context.atlasHtml(),
                    "Explain the score through business understanding/comprehension, not through raw test inventory.");
        }
        if (!mentionsCompletenessBoundary(html)) {
            result.add(id(), "Atlas does not expose completeness or coverage as a boundary concept",
                    context.atlasHtml(),
                    "Mention product/suite completeness, coverage, end-to-end limits or maturity caps as boundaries around the atlas score.");
        }
        if (!nearOverall(html, "intelegere", "understanding", "comprehension", "business")
                || !nearOverall(html, "complet", "coverage", "acoperire", "limit", "boundary", "plafon", "cap")) {
            result.add(id(), "Atlas Overall is not locally separated from completeness boundaries",
                    context.atlasHtml(),
                    "Keep the Overall explanation close to both the business-understanding metric and the completeness/coverage boundary.");
        }
    }

    private static boolean mentionsOverallScore(String html) {
        return html.contains("atlas overall")
                || (html.contains("overall") && (html.contains("score") || html.contains("scor")));
    }

    private static boolean mentionsBusinessUnderstanding(String html) {
        return AtlasSemanticVocabulary.containsAny(html, AtlasSemanticVocabulary.BUSINESS_UNDERSTANDING_MARKERS);
    }

    private static boolean mentionsCompletenessBoundary(String html) {
        return AtlasSemanticVocabulary.containsAny(html, AtlasSemanticVocabulary.COMPLETENESS_BOUNDARY_MARKERS);
    }

    private static boolean nearOverall(String html, String... tokens) {
        int overall = html.indexOf("overall");
        if (overall < 0) {
            return false;
        }
        int from = Math.max(0, overall - 1200);
        int to = Math.min(html.length(), overall + 1600);
        return AtlasSemanticVocabulary.containsAny(html.substring(from, to), tokens);
    }
}
