package com.ai.skills.atlas.rules.scoring;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;

public final class AtlasScoreGqmTraceabilityRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_CHAIN = List.of(
            "business goal",
            "flow question",
            "source/layer evidence",
            "atlas score");

    @Override
    public String id() {
        return "atlas.candidate.score-gqm-traceability";
    }

    @Override
    public String expected() {
        return "Atlas scoring follows business goal -> flow question -> source/layer evidence -> atlas score.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = AtlasScoreContract.normalized(context.lowerAtlasText());
        String scoreArea = scoreAuditArea(html);
        if (scoreArea.isBlank()) {
            result.add(id(), "Cannot audit GQM traceability because score audit sections are missing",
                    context.atlasHtml(),
                    "Add score rationale, score support, Overall formula and source trace before claiming atlas score quality.");
            return;
        }

        for (String token : REQUIRED_CHAIN) {
            if (!scoreArea.contains(token)) {
                result.add(id(), "Missing scoring chain element: " + token,
                        context.atlasHtml(),
                        "Explain atlas score with the chain: business goal -> flow question -> source/layer evidence -> atlas score.");
            }
        }

        for (String axis : AtlasScoreContract.BUSINESS_AXES) {
            String row = AtlasScoreContract.rowContext(scoreArea, axis);
            if (row.isBlank()) {
                result.add(id(), "Missing GQM traceability row for score axis: " + axis,
                        context.atlasHtml(),
                        "Each score axis needs its own row with business goal, flow question, source/layer evidence and atlas score.");
                continue;
            }
            for (String token : REQUIRED_CHAIN) {
                if (!row.contains(token)) {
                    result.add(id(), "Score axis is not individually GQM-traceable: " + axis + " missing " + token,
                            context.atlasHtml(),
                            "Do not rely on one global sentence; each score axis must expose business goal, flow question, source/layer evidence and atlas score.");
                }
            }
        }

        if (!AtlasSemanticVocabulary.containsAny(scoreArea, "intrebare", "question")) {
            result.add(id(), "Score audit does not expose the question answered by each metric",
                    context.atlasHtml(),
                    "Each score axis should answer a flow-comprehension question, not just list a numeric grade.");
        }
        if (!AtlasSemanticVocabulary.containsAny(scoreArea, "sursa", "source", "layer", "layere")) {
            result.add(id(), "Score audit is not tied to source/layer evidence",
                    context.atlasHtml(),
                    "Tie each score to source methods and layer traversal so the score is traceable.");
        }
    }

    private static String scoreAuditArea(String html) {
        StringBuilder area = new StringBuilder();
        appendSection(area, html, "score rationale", "limite de intelegere");
        appendSection(area, html, "4.5 suport scor", "4.6 formula overall");
        appendSection(area, html, "4.6 formula overall", "5. trace");
        appendSection(area, html, "5.1 suport scor", "</details>");
        return area.toString();
    }

    private static void appendSection(StringBuilder area, String html, String startToken, String endToken) {
        int start = html.indexOf(startToken);
        if (start < 0) {
            return;
        }
        int end = html.indexOf(endToken, start + startToken.length());
        area.append(' ');
        area.append(end < 0 ? html.substring(start) : html.substring(start, end));
    }

}
