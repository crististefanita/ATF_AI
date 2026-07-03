package com.endava.ai.skills.atlas.rules.scoring;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Map;

public final class AtlasScoreSupportTraceRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-support.trace";
    }

    @Override
    public String expected() {
        return "Every score card is backed by score-support rows that repeat the same score and cite evidence surfaces.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Map<String, Integer> cards = AtlasScoreContract.scoreCards(context.atlasText());
        if (cards.isEmpty()) {
            result.add(id(), "No score cards found for support trace validation", context.atlasHtml(),
                    "Publish score cards before adding score support trace.");
            return;
        }

        String html = context.lowerAtlasText();
        String support = AtlasScoreContract.normalized(AtlasText.sectionBetween(html, "4.5 suport scor", "4.6 formula overall")
                + " " + AtlasText.sectionBetween(html, "5.1 suport scor", "</details>"));
        if (support.isBlank()) {
            result.add(id(), "Missing score support sections 4.5 / 5.1", context.atlasHtml(),
                    "Add score-support rows under Business Evidence Tables and Source-to-Business Trace.");
            return;
        }

        for (Map.Entry<String, Integer> card : cards.entrySet()) {
            String label = card.getKey();
            if ("overall".equals(label)) {
                continue;
            }
            String row = AtlasScoreContract.rowContext(support, label);
            if (row.isBlank()) {
                result.add(id(), "Score card has no support row: " + label, context.atlasHtml(),
                        "Every score-card axis must be backed by a score-support row in 4.5 or 5.1.");
                continue;
            }
            if (!row.contains(String.valueOf(card.getValue()))) {
                result.add(id(), "Score support row does not repeat card score: " + label,
                        context.atlasHtml(), "Keep score card value and support-table value synchronized.");
            }
            if (!AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.EVIDENCE_SURFACE_MARKERS)) {
                result.add(id(), "Score support row is not tied to evidence: " + label,
                        context.atlasHtml(), "Explain score support through table, trace, source, layer, object, flow or proof evidence.");
            }
        }
    }

}
